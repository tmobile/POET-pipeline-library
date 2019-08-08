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


import com.tmobile.sre.pipeline.model.PipelineDefinition
import com.tmobile.sre.pipeline.util.CaseUtils

class PipelineEnvironmentProcessor implements Serializable {
    PipelineEnvironment process(PipelineEnvironment env, PipelineDefinition config) {
        processUserEnvironment(processGlobals(env, config), config)
    }

    PipelineEnvironment processGlobals(PipelineEnvironment env, PipelineDefinition config) {
        Map<String, String> globals = [:]

        def appInfo = config.pipeline.appInfo().collectEntries { k, v ->
            ["PIPELINE_" + CaseUtils.camelToEnvVarName(k), v]
        }

        globals.putAll(appInfo)

        def version = determineVersion(env, config)
        if (version != null) {
            globals["PIPELINE_APP_VERSION"] = version
        }

        return env.with(globals)
    }

    def determineVersion(PipelineEnvironment env, PipelineDefinition config) {
        def branch = env.get("PIPELINE_BRANCH", env.get("BRANCH", "master"))
        def vers = config.pipeline.appVersion?.get(branch)

        if (vers == null) {
            // try master version?
            def mv = config.pipeline.appVersion?.get("master")
            if (mv != null) {
                // TODO: maybe we should just take the last part if a '/' ?...
                vers = "${branch.replaceAll('/','_')}.${mv}"
            }
        }

        vers
    }

    PipelineEnvironment processUserEnvironment(PipelineEnvironment env, PipelineDefinition config) {

        Map<String, String> userEnv = [:]

        def keys = new ArrayList(config.pipeline.environment.keySet())
        for (int i=0; i< keys.size(); i++) {

            // extend the base environment with what we have so far from the user environment
            // so the user environment can reference other user vars
            def subst = new EnvironmentSubstitution(environment: env.with(userEnv))

            def k = keys[i]
            def v = config.pipeline.environment[k]

            userEnv.put(k, subst.substitute(v))
        }

        return env.with(userEnv)
    }
}
