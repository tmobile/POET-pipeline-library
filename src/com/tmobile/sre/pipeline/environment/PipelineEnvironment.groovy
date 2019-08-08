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

class PipelineEnvironment implements Serializable {
    List<PropertySource> sources = new ArrayList<>()

    List<PropertySource> nonManagedSources = new ArrayList<>()

    PipelineEnvironment() {

    }


    PipelineEnvironment(Collection<PropertySource> sources,Collection<PropertySource> nonManagedSources = [] ) {
        this.sources.addAll(sources)
        this.nonManagedSources.addAll(nonManagedSources)
    }

    PipelineEnvironment(Map<String, String> env) {
        this([new PropertySource(env)])
    }

    String get(String key, String _default = null) {
        for (int i=0; i< sources.size(); i++) {
            def src = sources[i]
            if (src.source.containsKey(key)) {
                return src.source.get(key)
            }
        }

        for (int i=0; i< nonManagedSources.size(); i++) {
            def src = nonManagedSources[i]
            if (src.source.containsKey(key)) {
                return src.source.get(key)
            }
        }

        return _default
    }

    PipelineEnvironment with(Map<String, String> env) {
        PipelineEnvironment e = new PipelineEnvironment(this.sources, this.nonManagedSources)
        e.sources << new PropertySource(env)
        return e
    }

    PipelineEnvironment with(PipelineEnvironment env) {
        new PipelineEnvironment(this.sources + env.sources, this.nonManagedSources)
    }

    PipelineEnvironment withNonManagedSource(Map<String, String> env) {
        PipelineEnvironment e = new PipelineEnvironment(this.sources, this.nonManagedSources)
        e.nonManagedSources << new PropertySource(env)
        return e
    }

    Map<String, String> toMap() {
        Map<String, String> m = [:]

        for (int i=0; i< sources.size(); i++) {
            m.putAll(sources[i].source)
        }

        m
    }

    List<String> toList() {
        List<String> ret = []

        def m = toMap()
        def keys = new ArrayList<>(m.keySet())

        for (int i=0; i< keys.size(); i++) {
            def k = keys[i]

            ret.add("${k}=${m[k]}")
        }
        ret
    }

    static class PropertySource implements Serializable {
        Map<String, String> source = new LinkedHashMap<>()
        PropertySource() {

        }

        PropertySource(Map<String, String> source) {
            this.source.putAll(source);
        }
    }
}
