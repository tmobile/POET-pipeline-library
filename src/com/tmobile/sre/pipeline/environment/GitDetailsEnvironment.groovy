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
 */
package com.tmobile.sre.pipeline.environment

class GitDetailsEnvironment implements Serializable {
    def jenkinsContext

    GitDetailsEnvironment(jenkinsContext) {
        this.jenkinsContext = jenkinsContext
    }

    PipelineEnvironment scmEnvironment(scm_details) {
        def base = [:]

        def changeLogSets = this.jenkinsContext.currentBuild.changeSets

        base['PIPELINE_BRANCH'] = scm_details['GIT_BRANCH']
        base['PIPELINE_COMMIT_BRANCH'] = scm_details['GIT_BRANCH']

        base['PIPELINE_COMMIT'] = scm_details['GIT_COMMIT']
        base['PIPELINE_COMMIT_SHA'] = scm_details['GIT_COMMIT']
        base['PIPELINE_GIT_URL'] = scm_details['GIT_URL']

        def commitInfo = getLastCommitInfo()
        if (commitInfo != null) {
            base['PIPELINE_COMMIT_AUTHOR'] = commitInfo.author
            base['PIPELINE_COMMIT_MESSAGE'] = commitInfo.message
        }

        new PipelineEnvironment(base)
    }

    @com.cloudbees.groovy.cps.NonCPS
    def getLastCommitInfo() {
        def changeLogSets = this.jenkinsContext.currentBuild.changeSets

        def ret = [:]
        for (int i = 0; i < changeLogSets.size(); i++) {
            def entries = changeLogSets[i].items
            if (entries.length > 0) {
                ret['author'] = entries[0].author.toString()
                ret['message'] = entries[0].msg

                return ret
            }
        }
    }
}
