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
package com.tmobile.sre.pipeline.util

class CaseUtils implements Serializable {
  private CaseUtils() {}

  /**
   * Really basic camelCase -> CAMEL_CASE conversion
   * for use as env var name.
   *
   * Doesn't handle repeated uppercase, e.g. myHTTPVar
   *
   * @param s camelCase string
   * @return UPPER_SNAKE_CASE version of the string
   */
  static String camelToEnvVarName(String s) {
    return s.replaceAll(/([A-Z])/, /_$1/).toUpperCase()
  }
}
