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

import com.tmobile.sre.pipeline.reader.PipelineFile
import com.tmobile.sre.pipeline.reader.PipelineFileReader

import java.nio.file.NoSuchFileException

/**
 * Given a path name of file.yml@name, will return the contents of file.yml
 * from a list of names to directory paths.
 */
class MappedDirectoryPipelineFileReader implements PipelineFileReader {
  PipelineFileReader reader;
  Map<String, String> mappedPaths = new HashMap<>()

  MappedDirectoryPipelineFileReader(PipelineFileReader reader, Map<String, String> mappedPaths) {
    this.reader = reader;
    this.mappedPaths.putAll(mappedPaths)
  }

  @Override
  PipelineFile read(String path) {
    def rf = RemoteFilePath.parse(path)

    if (mappedPaths.containsKey(rf.pathAlias)) {
      def lp = mappedPaths.get(rf.pathAlias)
      return reader.read("${lp}/${rf.file}")
    }

    throw new NoSuchFileException(path)
  }
}
