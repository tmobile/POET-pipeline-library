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
package com.tmobile.sre.pipeline.jenkins.secrets

import com.tmobile.sre.pipeline.model.PipelineStep
import com.tmobile.sre.pipeline.secrets.SecretProvider

class JenkinsSecretProvider implements SecretProvider {
    def jenkinsContext;

    JenkinsSecretProvider(jenkinsContext) {
        this.jenkinsContext = jenkinsContext
    }

    def genList(PipelineStep step) {
        def ret = []

        for (int i=0; i< step.secrets.size(); i++) {
            def s = step.secrets[i]

            if (s.target.size() > 1) {
                ret << [$class            : 'UsernamePasswordMultiBinding',
                         'credentialsId'   : s.source,
                         'usernameVariable': s.target[0], 'passwordVariable': s.target[1]]
            } else if (s.target.size() == 1) {
                ret << [$class : 'StringBinding', 'credentialsId' : s.source, 'variable': s.target[0]]
            }
        }

        ret
    }
}
