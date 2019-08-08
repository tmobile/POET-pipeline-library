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
package com.tmobile.sre.pipeline.jenkins.reader

import com.tmobile.sre.pipeline.PipelineLogger
import com.tmobile.sre.pipeline.model.Repository
import com.tmobile.sre.pipeline.reader.PipelineFilesystem
import com.tmobile.sre.pipeline.reader.templates.RepositoryDownloadManager

/**
 * Manages remote repository checkouts.
 *
 * Uses the `GitSCM` plugin to pull down any repositories listed in the
 * definition file to a temp directory.
 *
 * Note: files are currently retrieved on calls to `create` and are
 * never cleaned up.  If a repository has already been downloaded, the existing
 * path is returned.
 */
class JenkinsRepositoryDownloadManager implements RepositoryDownloadManager {
  PipelineLogger logger;
  PipelineFilesystem directory;
  def jenkinsContext;

  private Set<String> downloaded = new HashSet<>()


  JenkinsRepositoryDownloadManager(PipelineFilesystem directory,
                                   PipelineLogger logger,
                                   jenkinsContext) {

    this.directory = directory
    this.logger = logger
    this.jenkinsContext = jenkinsContext
  }

  @Override
  String download(Repository repository) {
    def path = localRepoPath(repository)

    if (downloaded.contains(path)) {
      // already downloaded
      return path
    }

    jenkinsContext.dir(path) {
      checkoutGitNoScm(repository.credentials.id, repository.uri, repository.label)
    }

    downloaded.add(path)

    return path
  }

  private def localRepoPath(Repository r) {
    def tmp = directory.tmp()

    def bn = r.label.replace('/', '_')
    def rn = r.uri.replace('/', '_').replace(':', '_')

    return "${tmp}/templates/${rn}_${bn}"
  }

  private def checkoutGitNoScm(
      String credentialId,
      String repoHttpCloneUrl,
      String branchName
  ) {

    def scmVars = jenkinsContext.checkout(
        [
            $class           : 'GitSCM',
            branches         : [[name: "${branchName}"]],
            extensions       : [[$class: 'CleanCheckout']] + [[$class: 'LocalBranch']],
            userRemoteConfigs: [[credentialsId: "${credentialId}", refspec: "", url: "${repoHttpCloneUrl}"]]
        ]
    )

    scmVars.put("credentialId", "${credentialId}")
    scmVars.put("url", "${repoHttpCloneUrl}")
    return scmVars
  }
}
