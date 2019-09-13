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
 * A local file path
 */
class PipelineFilePath implements Serializable {
  String path

  PipelineFilePath(String path) {
    this.path = path
  }

  /**
   * Returns the parent for this file path, or null if there is no parent.
   */
  PipelineFilePath parent() {
    String[] parts = path.split("/+")
    if (parts.size() < 2) {
      return null
    }

    def parent_parts = parts[0 .. (parts.size() -2)]

    if (parent_parts.size() == 1 && parent_parts[0] == "") {
      return new PipelineFilePath("/")
    }

    return new PipelineFilePath(String.join("/", parent_parts))
  }

  /**
   * Resolves the given path against this path's parent path.
   *
   * This is useful where a file name needs to be replaced with another file name.
   *
   * see: https://docs.oracle.com/javase/7/docs/api/java/nio/file/Path.html#resolveSibling(java.nio.file.Path)
   */
  PipelineFilePath resolveSibling(String sibling) {
    def p = parent()

    if (p == null) {
      return PipelineFilePath(sibling)
    }

    return new PipelineFilePath(p.path + "/" + sibling)
  }

  boolean equals(o) {
    if (this.is(o)) return true
    if (getClass() != o.class) return false

    PipelineFilePath that = (PipelineFilePath) o

    if (path != that.path) return false

    return true
  }

  int hashCode() {
    return (path != null ? path.hashCode() : 0)
  }
}
