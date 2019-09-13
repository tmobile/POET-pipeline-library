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
package com.tmobile.sre.pipeline.environment


import com.tmobile.sre.pipeline.model.PipelineStep
import groovy.json.JsonOutput

class PipelineStepProcessor implements Serializable {

    List<String> generateEnvironmentList(PipelineEnvironment environment, Map<String, Object> settings) {
        def m = process(settings)

        def keys = new ArrayList<>(m.keySet())

        def ret = []

        def subst = new EnvironmentSubstitution(environment: environment)

        for(int i=0; i < keys.size(); i++) {
            def v = subst.substitute(m[keys[i]])
            ret.add("${keys[i]}=${v}")
        }

        ret
    }

    PipelineStep process(PipelineEnvironment env, PipelineStep step) {
        PipelineStep s = new PipelineStep()

        def subst = new EnvironmentSubstitution(environment: env)

        s.name = subst.substitute(step.name)
        s.image = subst.substitute(step.image)
        s.commands = step.commands
        s.environment = step.environment
        s.when = step.when
        s.timeoutInMinutes = step.timeoutInMinutes
        s.continueOnError = step.continueOnError

        s.secrets = step.secrets
        for (int i=0; i< step.secrets.size(); i++) {
            s.secrets[i].source = subst.substitute(step.secrets[i].source)
        }

        s
    }

    Map<String, String> process(Map<String, Object> settings) {
        return prefixAndUpper("", flatten(settings))
    }

    Map<String, String> prefixAndUpper(prefix, Map<String, String> settings) {
        def m = settings.collectEntries {
            [(prefix + it.key.toUpperCase()) : it.value ]
        }
        m
    }

    Map<String, String> flatten(Map<String, Object> settings) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>()

        def keys = new ArrayList<>(settings.keySet())
        for (int i=0; i< keys.size(); i++) {
            def k = keys[i]
            def v = settings[k]

            switch (v) {
                case String:
                    m[k] = v;
                    break;
                case { isStringCollection(v) }:
                    m[k] = ((List<String>) v).join(",")
                    break;
                default:
                    m[k] = JsonOutput.toJson(v)
            }
        }

        m
    }

    boolean isStringCollection(Object obj) {
        if (obj instanceof List) {
            def col = (List<Object>) obj
            for (int i = 0; i < col.size(); i++) {
                if (!col[i] instanceof String) {
                    return false
                }
            }
            return true
        }
        return false
    }
}
