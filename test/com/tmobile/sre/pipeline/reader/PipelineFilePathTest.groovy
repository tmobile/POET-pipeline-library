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

class PipelineFilePathTest extends  GroovyTestCase {
  void testParent() {
    def f = new PipelineFilePath("/a/b/c/d/x.yml")

    assertEquals("/a/b/c/d", f.parent().path)
    assertEquals("/a/b/c", f.parent().parent().path)
    assertEquals("/a/b", f.parent().parent().parent().path)
    assertEquals("/a", f.parent().parent().parent().parent().path)
    assertEquals("/", f.parent().parent().parent().parent().parent().path)
    assertNull(f.parent().parent().parent().parent().parent().parent())
  }

  void testResolveSibling() {
    def f = new PipelineFilePath("/a/b/c/file.yml")

    assertEquals("/a/b/c/file-2.yml", f.resolveSibling("file-2.yml").path)
  }
}
