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

import java.util.regex.Pattern

class EnvironmentSubstitution implements  Serializable {
    PipelineEnvironment environment

    Pattern p = ~/\$\{([^\}]+)\}/

    String substitute(String input) {
        if (input == null) {
            return input
        }

        StringBuffer sb = new StringBuffer()

        def m = p.matcher(input)

        while (m.find()) {
            def parts = m.group(1).split(":-")

            def key = parts[0]
            def _default = parts.length > 1 ? parts[1] : null

            def v = environment.get(key, _default)

            if (v == null) {
                throw new MissingKeyException(key);
            }

            m.appendReplacement(sb, v)
        }
        m.appendTail(sb)
        return sb.toString()
    }

    static class MissingKeyException extends RuntimeException {
        MissingKeyException(String keyName) {
            super("Missing key: " + keyName)
        }
    }
}
