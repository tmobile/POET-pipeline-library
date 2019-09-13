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
package com.tmobile.sre.pipeline.reader.templates

import com.tmobile.sre.pipeline.reader.PipelineFileReader
import com.tmobile.sre.pipeline.reader.ResourcePipelineFileReader
import org.junit.Test
import static org.junit.Assert.*

import java.nio.file.NoSuchFileException

class MappedDirectoryPipelineFileReaderTest {
  final PipelineFileReader reader = new ResourcePipelineFileReader()
  final Map<String, String> mappedPaths = ["templates": "templates"]
  final PipelineFileReader mdpfr = new MappedDirectoryPipelineFileReader(reader, mappedPaths)

  @Test(expected = NoSuchFileException.class)
  void testNoSuchFileWithNoAlias() {
    mdpfr.read("hello.yml")
  }

  @Test(expected = NoSuchFileException.class)
  void testNoSuchFileWithTooManyAliases() {
    mdpfr.read("hello.yml@templates@something")
  }

  @Test(expected = NoSuchFileException.class)
  void testNoSuchFileWhenNoAliasMatch() {
    mdpfr.read("hello.yml@somewhere")
  }

  @Test
  void testReadFileWhenMapped() {
    assertEquals(
        reader.read("templates/one.yml"),
        mdpfr.read("one.yml@templates")
    )
  }
}
