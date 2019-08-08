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
package com.tmobile.sre.pipeline.reader

import java.nio.file.NoSuchFileException

/**
 * Chains together PipelineFileReader implementations.
 *
 * The first value returned will be used.  If no file can
 * be retrieved the readers should throw NoSuchFileException.
 *
 * This class will also continue to try for IOException.
 *
 * If no reader can successfully be used to retrive the file,
 * NoSuchFileException will be thrown.
 */
class ChainedFileReader implements PipelineFileReader {
  List<PipelineFileReader> readers = new ArrayList<>();
  def logger

  ChainedFileReader(logger, PipelineFileReader... readers) {
    this.logger = logger
    this.readers.addAll(readers);
  }

  @Override
  PipelineFile read(String path) {
    for (int i=0; i< readers.size(); i++) {
      try {
        return readers[i].read(path);
      } catch (final NoSuchFileException nsfe) {
        // keep trying
      } catch (final IOException ioe) {
        // keep trying
      }
    }

    throw new NoSuchFileException(path);
  }
}
