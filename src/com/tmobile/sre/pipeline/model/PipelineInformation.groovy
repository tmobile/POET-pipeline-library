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
package com.tmobile.sre.pipeline.model

class PipelineInformation implements Serializable {
  String appOwner
  String appGroup
  String appName
  String appAkmid

  LinkedHashMap<String, String> appVersion = []
  LinkedHashMap<String, String> environment = new LinkedHashMap<>()

  // backwards for having appName and appVersion under global
  @com.cloudbees.groovy.cps.NonCPS
  void setGlobal(Map<String, Object> global) {
    if (global.get('appName') != null && appName == null) {
      appName = global.get('appName')
    }

    if (global.get('appVersion') != null && appVersion.isEmpty()) {
      Map<String, String> versions = global.get('appVersion') as Map<String, String>
      getAppVersion().putAll(versions)
    }
  }

  /**
   * Return non-null key/value app properties
   */
  Map<String, String> appInfo() {
    getProperties().findAll {
      it.key.startsWith("app") && it.value instanceof String
    }
  }
}
