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

import com.tmobile.sre.pipeline.StubJenkinsContext
import org.junit.Test
import static org.junit.Assert.*

import java.nio.file.NoSuchFileException

class ChainedFileReaderTest {
  def logger = new StubJenkinsContext().logger

  @Test(expected = NoSuchFileException.class)
  void testEmptyChainThrowsException() {
    new ChainedFileReader(logger).read("file")
  }

  @Test(expected = NoSuchFileException.class)
  void testExceptionWhenNoFile() {
    assertEquals("a", new ChainedFileReader(logger,
        new PipelineFileReader() {
          PipelineFile read(String path) {
            throw new NoSuchFileException(path)
          }
        },
        new PipelineFileReader() {
          PipelineFile read(String path) {
            throw new NoSuchFileException(path)
          }
        }).read("file.yml"))
  }

  @Test
  void testReturnsFirstNonError() {
    assertEquals("a", new ChainedFileReader(logger,
        new PipelineFileReader() {
          PipelineFile read(String path) {
            throw new NoSuchFileException(path)
          }
        },
        new PipelineFileReader() {
          PipelineFile read(String path) {
            return new PipelineFile("a", "a")
          }
        },
        new PipelineFileReader() {
          PipelineFile read(String path) {
            return new PipelineFile("b", "b")
          }
        }).read("file.yml").text)
  }
}
