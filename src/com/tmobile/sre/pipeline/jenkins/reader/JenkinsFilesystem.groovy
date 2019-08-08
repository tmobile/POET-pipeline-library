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
package com.tmobile.sre.pipeline.jenkins.reader

import com.tmobile.sre.pipeline.reader.PipelineFilesystem

class JenkinsFilesystem implements PipelineFilesystem {
  def jenkinsContext;

  JenkinsFilesystem(def jenkinsContext) {
    this.jenkinsContext = jenkinsContext
  }

  /**
   * @see https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#pwd-determine-current-directory
   */
  @Override
  String tmp() {
    return jenkinsContext.pwd(tmp:true)
  }

  @Override
  String writeFile(String dir, String filename, String text) {
    def path = "${dir}/${filename}"

    jenkinsContext.writeFile(file: path, text: text)

    return path
  }

  @Override
  void rm(String path) {
    jenkinsContext.sh("rm -f $path")
  }
}
