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
package com.tmobile.sre.pipeline


import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.environment.PipelineStepProcessor
import com.tmobile.sre.pipeline.jenkins.reader.JenkinsFilesystem
import com.tmobile.sre.pipeline.jenkins.secrets.JenkinsSecretProvider
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.model.PipelineStep
import com.tmobile.sre.pipeline.model.StepState
import com.tmobile.sre.pipeline.model.StepStatus
import com.tmobile.sre.pipeline.model.CommandState
import com.tmobile.sre.pipeline.reader.PipelineFilesystem
import groovy.json.JsonOutput


class DockerExecutor implements Serializable {
	static final String STEP_CATEGORY_LABEL = "com.tmobile.poet.step.categories"
	static final Integer DEFAULT_STEP_TIMEOUT_MINUTES = 30

	def jenkinsContext;
	def dockerContext;

	DockerExecutor(jenkinsContext, dockerContext) {
		this.jenkinsContext = jenkinsContext;
		this.dockerContext = dockerContext;
	}

	PipelineStepProcessor stepProcessor = new PipelineStepProcessor()

	def StepStatus run(PipelineState state, PipelineStep step, StepState stepState, PipelineEnvironment environment, PipelineLogger logger) {
		Integer stepTimeout
		try {
			def stepEnv = stepProcessor.generateEnvironmentList(environment, step.environment)
			def creds = new JenkinsSecretProvider(jenkinsContext).genList(step)

			stepState.start();
			stepTimeout = step.timeoutInMinutes ?: DEFAULT_STEP_TIMEOUT_MINUTES
				jenkinsContext.timeout(time: stepTimeout, unit: 'MINUTES') {
					jenkinsContext.withEnv(environment.toList()) {
						jenkinsContext.withEnv(stepEnv) {
							jenkinsContext.withCredentials(creds) {
								// https://issues.jenkins-ci.org/browse/JENKINS-38438
								// TODO: make this configurable?

								def image = dockerContext.image(step.image)

								try {
									image.pull()
								} catch (Exception e) {
									//
								}

								stepState.categories = readStepCategories(step.image)

								image.inside("-u 0:0 -v /var/run/docker.sock:/var/run/docker.sock") {
									for (def i = 0; i < step.commands.size(); i++) {
										CommandState commandState = new CommandState(step.commands[i])
										stepState.addCommand(commandState)  // record command progress
										commandState.start()
										jenkinsContext.sh(step.commands[i])
										commandState.end()
									}
								}

								stepState.status = StepStatus.SUCCESS
								logger.debug(stepState)
							}
						}
					}
				}
			}
		catch (Exception e) {
			jenkinsContext.echo(e.toString())
			jenkinsContext.echo(e.getStackTrace().toList().toString())
			stepState.status = StepStatus.FAILURE
			stepState.explanation = e.toString()
			logger.error(e.getStackTrace().toList().toString())
			if (isTimeout(e)) {
				stepState.explanation = "${stepTimeout} minute timeout exceeded."
			}
		}
		finally {
			stepState.end();
		}

		return stepState.status
	}

	List<String> readStepCategories(final String image) {
		String tag = readTag(image, STEP_CATEGORY_LABEL)
		if (tag == null || tag.isEmpty()) {
			return []
		}

		return Arrays.asList(tag.split('\\s*,\\s*'))
	}

	private String readTag(String image, String label) {
		try {
			def cmd = "docker inspect -f '{{index .Config.Labels \"$label\"}}' $image"
			def ret = jenkinsContext.sh(script: cmd, returnStdout: true).trim()
			return ret
		} catch (final Exception e) {
			return null
		}
	}

	private boolean isTimeout(Exception e) {
		// despite the class org.jenkinsci.plugins.workflow.steps.FlowInterruptedException getting thrown, it's
		// somehow not available on the classpath for a proper catch statement (i.e. catch (FlowInterruptedException fie))
		// Given all the problems with library dependencies, it didn't seem worth it to add one for jenkins internals, so
		// we're just looking at the name.  I chose the simpleName to make it easier to add tests.
		//
		// Note there may be cases where FlowInterruptedException happen for user aborts, so we may need to tweak this
		// logic in the future.
		// see: https://stackoverflow.com/questions/51260440/jenkins-timeout-abort-exception
		// looks like jenkins have an addition method getCauses() (note: not getCause), that will be different for user
		// vs timeout

		return "FlowInterruptedException".equals(e.getClass().getSimpleName())
	}
}