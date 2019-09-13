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
package com.tmobile.sre.pipeline.reader

/**
 * A local file and its contents.
 */
class PipelineFile implements Serializable {
  PipelineFilePath path
  String text

  PipelineFile(String path, String text) {
    this.path = new PipelineFilePath(path)
    this.text = text
  }

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    PipelineFile that = (PipelineFile) o

    if (path != that.path) return false
    if (text != that.text) return false

    return true
  }

  int hashCode() {
    int result
    result = (path != null ? path.hashCode() : 0)
    result = 31 * result + (text != null ? text.hashCode() : 0)
    return result
  }
}
