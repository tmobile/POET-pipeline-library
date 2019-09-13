/*
 * Copyright © 2019 T-Mobile USA, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * See the LICENSE file for additional language around disclaimer of
 * warranties. Trademark Disclaimer: Neither the name of “T-Mobile, USA” nor
 * the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 */
package com.tmobile.sre.pipeline.jenkins


import com.tmobile.sre.pipeline.DockerExecutor
import com.tmobile.sre.pipeline.PipelineExecutor
import com.tmobile.sre.pipeline.PipelineLogger
import com.tmobile.sre.pipeline.environment.GitDetailsEnvironment
import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.environment.PipelineEnvironmentProcessor
import com.tmobile.sre.pipeline.jenkins.environment.JenkinsEnvironment
import com.tmobile.sre.pipeline.jenkins.reader.JenkinsFilesystem
import com.tmobile.sre.pipeline.jenkins.reader.JenkinsLibraryResourceReader
import com.tmobile.sre.pipeline.jenkins.reader.JenkinsLocalPipelineFileReader
import com.tmobile.sre.pipeline.jenkins.reader.JenkinsRepositoryDownloadManager
import com.tmobile.sre.pipeline.model.LogEntryLevel
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.model.PipelineStatus
import com.tmobile.sre.pipeline.model.PipelineDefinition
import com.tmobile.sre.pipeline.reader.PipelineDefaults
import com.tmobile.sre.pipeline.reader.PipelineFilesystem
import com.tmobile.sre.pipeline.reader.PipelineReader
import com.tmobile.sre.pipeline.reader.templates.RepositoryDownloadManager
import com.tmobile.sre.pipeline.reader.templates.StandardTemplateManagerFactory
import com.tmobile.sre.pipeline.reader.templates.TemplateManagerFactory
import com.tmobile.sre.pipeline.reader.validator.PipelineValidator
import com.tmobile.sre.pipeline.reader.validator.ValidationReporter
import com.tmobile.sre.pipeline.reader.validator.Validator

import java.nio.file.NoSuchFileException

def start(options = [:]) {
	PipelineState state = new PipelineState().start()

    def configFileName = options.get('pipeline', 'pipeline.yml')
    def agent_label = options.get('agent_label', 'master')
    def logging_level = options.get('log_level', 'warn')

    echo("configFileName: " + configFileName + ", agent_label: " + agent_label)
    
    node(agent_label) {
        def scm_details

        scm_details = performCheckout()

        def logger = new JenkinsLogger(this, state).withMinLevel(LogEntryLevel.parseLevel(logging_level))

        PipelineFilesystem pipelineDirectory = new JenkinsFilesystem(this)
        final def config = readConfig(configFileName, options, pipelineDirectory, logger)

        def environment = constructEnvironment(config, state, scm_details)

        logger.debug("SCM: " + scm_details)

        currentBuild.displayName = "${environment.get('PIPELINE_APP_VERSION')}.${environment.get('PIPELINE_BUILD_NUMBER')}"
        currentBuild.description = "branch: ${environment.get('PIPELINE_BRANCH')}"

        logger.debug(["displayName":"${currentBuild.displayName}", "description":"${currentBuild.description}"]);

        def docker = new DockerExecutor(this, this.docker)

        final def executor = new PipelineExecutor(logger, docker, environment, new JenkinsPipelineContext(this), pipelineDirectory)

        withEnv(environment.toList()) {
            def engineState = executor.execute(state, config)

            if (engineState.status.equals(PipelineStatus.FAILURE)) {
                error("Failing pipeline because of previous failures")
            }
            // TODO: should we just log the pipelineState (log pre/post as debug?)
            logger.debug(engineState.preState)
            logger.info(engineState.pipelineState)
            logger.debug(engineState.postState)
        }
    }
}


private PipelineDefinition readConfig(String configFileName, def options, PipelineFilesystem pipelineDirectory, PipelineLogger logger) {
    final RepositoryDownloadManager downloadManager = new JenkinsRepositoryDownloadManager(pipelineDirectory, logger, this);
    final TemplateManagerFactory templateManagerFactory = new StandardTemplateManagerFactory(downloadManager, logger)
    final PipelineValidator validator = new Validator(new JenkinsLibraryResourceReader(this))
    final PipelineDefaults defaults = PipelineDefaults.loadDefaults(this.env.PIPELINE_INTERNAL_TEMPLATES)
    final PipelineReader pipelineReader = new PipelineReader(new JenkinsLocalPipelineFileReader(this), templateManagerFactory, logger, validator, defaults)

    PipelineDefinition config

    try {
        config = pipelineReader.read(configFileName)
    } catch (final NoSuchFileException nsfe) {
        logger.error("Could not read file: " + nsfe.getFile())
        // fall through to display validation errors
    } catch(final Exception e) {
        // parse error, fall through to display validation errors
        logger.error(e.getMessage())
    }

    def validation = options.get("validation", true)

    if (validator.getResult().hasErrors()) {
        ValidationReporter.printReport(logger, pipelineReader.validator.getResult())
    }

    if (config == null || (validation && validator.getResult().hasErrors())) {
        error("Invalid pipeline.yml")
    }

    return config
}

private PipelineEnvironment constructEnvironment(def config, def state, def scm_details) {
    def gitEnv = new GitDetailsEnvironment(this).scmEnvironment(scm_details)
    def jenkinsEnv = new JenkinsEnvironment(this).defaultEnvironment()
    def env = jenkinsEnv.with(gitEnv).withNonManagedSource(this.env.getEnvironment()).with(["PIPELINE_RUN_ID" : state.runId.toString()])

    def processed = new PipelineEnvironmentProcessor().process(env, config)

    state.withEnv(processed)
    processed
}

private performCheckout() {
    def scm_details

    // Check if we have all details to checkout a git repository available
    if (env.SCM_BRANCH?.trim() && env.SCM_CREDENTIAL_ID?.trim() && env.SCM_REPOSITORY?.trim()) {
        echo("Checkign out branch \"${env.SCM_BRANCH?.trim()}\" form ${env.SCM_REPOSITORY?.trim()}")
        scm_details = checkout(
                [
                        $class           : 'GitSCM',
                        branches         : [[name: "${env.SCM_BRANCH?.trim()}"]],
                        extensions       : [[$class: 'CleanCheckout']] + [[$class: 'LocalBranch']],
                        userRemoteConfigs: [[credentialsId: "${env.SCM_CREDENTIAL_ID?.trim()}", refspec: "", url: 
                                "${env.SCM_REPOSITORY?.trim()}"]]
                ]
        )

        // inject BRANCH_NAME as it is expected to be there to poputale PIPELINE_BRANCH
        env.BRANCH_NAME = "${env.SCM_BRANCH?.trim()}"

    }
    else {
        scm_details = checkout scm
    }
    
    return scm_details
}