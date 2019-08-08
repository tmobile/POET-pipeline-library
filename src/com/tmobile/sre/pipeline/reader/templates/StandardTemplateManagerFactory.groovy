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

import com.tmobile.sre.pipeline.PipelineLogger
import com.tmobile.sre.pipeline.model.PipelineResourceDirectory
import com.tmobile.sre.pipeline.model.Repository
import com.tmobile.sre.pipeline.reader.ChainedFileReader
import com.tmobile.sre.pipeline.reader.PipelineFileReader

/**
 * Manages Repository downloads and provides readers for templates.
 */
class StandardTemplateManagerFactory implements TemplateManagerFactory {
  PipelineLogger logger;
  RepositoryDownloadManager downloader;

  StandardTemplateManagerFactory(RepositoryDownloadManager downloader,
                         PipelineLogger logger) {


    this.logger = logger
    this.downloader = downloader
  }

  /**
   * Downloads all defined repositories and returns a PipelineFileReader that can locate
   * remote templates.
   */
  PipelineFileReader create(PipelineFileReader reader, PipelineResourceDirectory config) {
    final Map<String, String> mappedPaths = new HashMap<>()

    for (int i = 0; i < config.resources.repositories.size(); i++) {
      Repository repository = config.resources.repositories.get(i)
      def path = downloader.download(repository)
      mappedPaths.put(repository.name, path)
    }

    return new ChainedFileReader(logger, reader, new MappedDirectoryPipelineFileReader(reader, mappedPaths))
  }
}