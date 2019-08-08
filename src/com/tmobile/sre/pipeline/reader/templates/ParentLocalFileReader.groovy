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
package com.tmobile.sre.pipeline.reader.templates

import com.tmobile.sre.pipeline.reader.ChainedFileReader
import com.tmobile.sre.pipeline.reader.PipelineFile
import com.tmobile.sre.pipeline.reader.PipelineFilePath
import com.tmobile.sre.pipeline.reader.PipelineFileReader

/**
 * PipelineFileReader that uses a relative path of a parent include to find child included files.
 *
 * I.e. if an included file such as parent.yml@someplace also has its own includes, we allow those
 * child includes to be local to the parent path.
 */
class ParentLocalFileReader implements PipelineFileReader {
  PipelineFileReader currentReader
  PipelineFileReader parentReader
  PipelineFilePath parentPath

  ParentLocalFileReader(
      final PipelineFileReader currentReader,
      final PipelineFileReader parentReader,
      final PipelineFilePath parentPath
  ) {
    this.currentReader = currentReader
    this.parentReader = parentReader
    this.parentPath = parentPath
  }

  @Override
  PipelineFile read(String path) {
    if (RemoteFilePath.isRemotePath(path)) {
      return currentReader.read(path)
    }

    def resolvedPath = parentPath.resolveSibling(path)

      new ChainedFileReader(null,
          new FixedPathFileReader(parentReader, resolvedPath),
          currentReader).read(path)
  }

  class FixedPathFileReader implements PipelineFileReader {
    PipelineFilePath resolvedPath
    PipelineFileReader parentReader

    FixedPathFileReader(PipelineFileReader parentReader, PipelineFilePath resolvedPath) {
      this.parentReader = parentReader
      this.resolvedPath = resolvedPath
    }

    PipelineFile read(String _path) {
      return parentReader.read(resolvedPath.path)
    }
  }
}

