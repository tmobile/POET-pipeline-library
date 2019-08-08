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
package com.tmobile.sre.pipeline.jenkins.environment

import com.tmobile.sre.pipeline.environment.PipelineEnvironment

class JenkinsEnvironment implements Serializable {
  def jenkinsContext

  JenkinsEnvironment(jenkinsContext) {
    this.jenkinsContext = jenkinsContext
  }

  PipelineEnvironment defaultEnvironment() {
    def base = [:]

    base['PIPELINE_BRANCH'] = jenkinsContext.env.BRANCH_NAME
    base['PIPELINE_BUILD_NUMBER'] = jenkinsContext.env.BUILD_NUMBER
    base['PIPELINE_RUN_DISPLAY_URL'] = jenkinsContext.env.RUN_DISPLAY_URL
    base['PIPELINE_WORKSPACE'] = jenkinsContext.env.WORKSPACE

    def build_url = jenkinsContext.env.BUILD_URL ?: ""
    def build_path = build_url.substring((jenkinsContext.env.HUDSON_URL ?: "").length())

    base['PIPELINE_BUILD_URL'] = build_url
    base['PIPELINE_BUILD_URL_PATH'] = build_path

    base['PIPELINE_JOB_NAME'] = jenkinsContext.env.JOB_NAME

    new PipelineEnvironment(base)
  }
}
