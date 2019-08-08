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

class PipelineStep implements Serializable {
  String name;
  String image;
  String include;

  Boolean continueOnError
  Integer timeoutInMinutes

  List<String> commands = []

  LinkedHashMap<String, Object> environment = new LinkedHashMap<>()

  LinkedHashMap<String, Object> when = new LinkedHashMap<>()
  List<Secret> secrets = []

  // backwards compatibility as a name for environment
  @com.cloudbees.groovy.cps.NonCPS
  public void setSettings(LinkedHashMap<String, Object> settings) {
    this.environment.putAll(settings);
  }
}