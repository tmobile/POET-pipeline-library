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
package com.tmobile.sre.pipeline

import com.tmobile.sre.pipeline.reader.PipelineFilesystem

class StubPipelineFilesystem implements PipelineFilesystem {
  String tmp() {
    return System.getProperty("java.io.tmpdir")
  }

  String writeFile(String dir, String filename, String text) {
    def f = new File(dir, filename)
    f.write(text)
    return f.path
  }

  /**
   * Remove a file by path
   *
   * @param path
   */
  void rm(String path) {
    new File(path).delete()
  }
}
