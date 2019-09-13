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
package com.tmobile.sre.pipeline.secrets

import com.tmobile.sre.pipeline.reader.PipelineReader
import com.tmobile.sre.pipeline.reader.PipelineReaderTest
import com.tmobile.sre.pipeline.reader.ResourcePipelineFileReader
import com.tmobile.sre.pipeline.StubJenkinsContext
import com.tmobile.sre.pipeline.jenkins.secrets.JenkinsSecretProvider
import com.tmobile.sre.pipeline.reader.StubTemplateManagerFactory
import com.tmobile.sre.pipeline.reader.templates.StandardTemplateManagerFactory
import com.tmobile.sre.pipeline.reader.templates.TemplateManagerFactory

class JenkinsSecretProviderTest extends GroovyTestCase {
    def context = new StubJenkinsContext()
    TemplateManagerFactory tmf = new StandardTemplateManagerFactory(new PipelineReaderTest.TestRepositoryDownloadManager(), null)

    def secretProvider = new JenkinsSecretProvider(context)
    PipelineReader reader = new PipelineReader(new ResourcePipelineFileReader(),  tmf, new StubJenkinsContext().logger)

    void test() {
        def config = reader.read("pipeline-secret.yml")
        def step = config.pipeline.steps[0]

        println(step.secrets)
        def l = secretProvider.genList(step)
        println(l)
    }
}
