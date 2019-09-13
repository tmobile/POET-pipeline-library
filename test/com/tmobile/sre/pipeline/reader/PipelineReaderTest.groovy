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

import com.tmobile.sre.pipeline.StubJenkinsContext
import com.tmobile.sre.pipeline.model.*
import com.tmobile.sre.pipeline.reader.templates.RepositoryDownloadManager
import com.tmobile.sre.pipeline.reader.templates.StandardTemplateManagerFactory
import com.tmobile.sre.pipeline.reader.templates.TemplateManagerFactory
import com.tmobile.sre.pipeline.reader.validator.PipelineValidator
import com.tmobile.sre.pipeline.reader.validator.Validator

import java.nio.file.NoSuchFileException


class PipelineReaderTest extends GroovyTestCase {
    TemplateManagerFactory tmf = new StandardTemplateManagerFactory(new TestRepositoryDownloadManager(), null)
    PipelineValidator validator = new Validator(new ResourcePipelineFileReader())
    PipelineDefaults defaults = new PipelineDefaults(new Repository([name: "templates"]), new Repository([name:"_"]))
    PipelineReader reader =
        new PipelineReader( new ResourcePipelineFileReader(), tmf, new StubJenkinsContext().logger, validator, defaults)

    void testRead() {
        for (file in ["pipeline.yml", "pipeline-older.yml"]) {
            def config = reader.read(file)

            assertEquals("1.0", config.version)
            assertEquals("sre-pipeline", config.pipeline.appName)

            def expectedVersions = ['master': '2.4.2', 'feature/jdk_update': 'jdk_update.2.4.1']

            assertEquals(expectedVersions, config.pipeline.appVersion)

            assertEquals(3, config.pipeline.environment.size())

            def steps = config.pipeline.steps

            assertEquals(5, steps.size())

            assertEquals("slack", steps[4].name)
            assertEquals(["channel": "dev_chat"], steps[4].environment)
            assertEquals(["status": ["success", "failure"]], steps[4].when)
            assertFalse(validator.result.hasErrors())
        }

        // newer configuration
        def config = reader.read("pipeline.yml")
        assertEquals("POET", config.pipeline.appOwner)
        assertEquals("sre", config.pipeline.appGroup)
        assertEquals("1234", config.pipeline.appAkmid)
        assertFalse(validator.result.hasErrors())
    }

    void testMinimalPipeline() {
        def config = reader.read("pipeline-small.yml")

        def steps = config.pipeline.steps


        assertFalse(validator.result.hasErrors())

        assertEquals(1, steps.size())
        assertEquals("test", steps[0].name)
        assertEquals("gradle:4.10-jre8-alpine", steps[0].image)
        assertEquals([:], steps[0].when)
    }

    void testSecrets() {
        def config = reader.read("pipeline-secret.yml")

        // check step secrets
        def step = config.pipeline.steps[0]

        assertFalse(validator.result.hasErrors())

        assertEquals("9b2f", step.secrets[0].source)
        assertEquals(["DOCKER_USERNAME", "DOCKER_PASSWORD"], step.secrets[0].target)

        assertEquals("abd", step.secrets[1].source)
        assertEquals(["ANOTHER_SECRET"], step.secrets[1].target)
    }

    void testRepositories() {
        def config = reader.read("pipeline-resources.yml")

        def resources = config.resources;
        Repository repository = resources.repositories.get(0)

        assertFalse(validator.result.hasErrors())

        assertEquals("templates", repository.name)
        assertEquals("https://bitbucket.example.com/scm/PRJ/pipeline-templates.git", repository.uri)
        assertEquals("srebuildmaster", repository.credentials.id)
    }

    void testStepTemplates() {
        def config = reader.read("pipeline-step-templates.yml")

        assertFalse(validator.result.hasErrors())

        assertEquals(4, config.pipeline.steps.size())
        assertEquals(["HELLO": "world"], config.pipeline.steps[1].environment)
        assertEquals(["HELLO": "world"], config.pipeline.steps[2].environment)

        assertEquals(["branch": "master"],config.pipeline.steps[1].when)
        assertEquals(["branch": "master", "status":["success", "failure"]],config.pipeline.steps[2].when)

        assertEquals(3, config.pipeline.steps[1].secrets.size())

        // make sure we replaced the secret that's in both
        def ds = config.pipeline.steps[1].secrets.find {s -> s.source == "9b2f"}
        assertEquals(["DOCKER_USERNAME"], ds.target)

        assertEquals(2, config.pipeline.steps[2].secrets.size())
    }

    private def findSecretById(String id, List<Secret> secrets) {
        secrets.find {s -> s.source == "9b2f"}
    }

    void testConfigTemplates() {
        def config = reader.read("pipeline-config-templates.yml")

        def expectedEnv = [
            "UBUNTU_VERSION": "14.04",
            "LOG_LEVEL"     : "warn",
            "A_ONLY"        : "hi",
            "LOG_LEVEL"     : "warn",
            "GREETING"      : "Hello World",
            "B_ONLY"        : "ok"
        ]

        def expectedVersions = [
            "master"   : "2.0.1",
            "feature/a": "1.2.0",
            "feature/b": "1.2.0",
            "feature/c": "1.3.0"
        ]

        assertFalse(validator.result.hasErrors())

        assertEquals(expectedEnv, config.pipeline.environment)
        assertEquals(expectedVersions, config.pipeline.appVersion)
        assertEquals("hello_world", config.pipeline.appName)
    }

    void testConfigTemplatesAllExternal() {
        for (file in ["pipeline-config-templates-external.yml", "pipeline-config-templates-external-older.yml"]) {

            def config = reader.read(file)

            def expectedEnv = [
                "LOG_LEVEL": "debug",
                "A_ONLY"   : "hi",
                "GREETING" : "Hello World",
                "B_ONLY"   : "ok"
            ]

            def expectedVersions = [
                "master"   : "1.0.0",
                "feature/a": "1.2.0",
                "feature/b": "1.2.0",
                "feature/c": "1.3.0"
            ]

            assertEquals(expectedEnv, config.pipeline.environment)
            assertEquals(expectedVersions, config.pipeline.appVersion)
            assertEquals("hello", config.pipeline.appName)
            assertFalse(validator.result.hasErrors())
        }

        // newer fields
        def config = reader.read("pipeline-config-templates-external.yml")
        assertEquals("poet", config.pipeline.appOwner)
        assertEquals("1234", config.pipeline.appAkmid)
        assertFalse(validator.result.hasErrors())
    }

    void testConfigTemplateIncludes() {
        def config = reader.read("pipeline-config-include.yml")

        assertEquals("pipeline-test", config.pipeline.appName)
        assertEquals("pipeline", config.pipeline.appGroup)
        assertEquals("poet", config.pipeline.appOwner)

        def expectedEnv = ["LOG_LEVEL":"warning", "GREETING":"hello", "B_ONLY":"bee", "A_ONLY":"hi"]

        def expectedVersion = ["master":"1.0.0"]

        assertEquals(expectedEnv, config.pipeline.environment)
        assertEquals(expectedVersion, config.pipeline.appVersion)
        assertFalse(validator.result.hasErrors())
    }

    void testConfigTemplateIncludesMissing() {
        shouldFail(NoSuchFileException.class) {
            reader.read("pipeline-config-include-missing.yml")
        }
    }

    void testConfigTemplateIncludeLoop() {
        shouldFail(PipelineReader.MaxIncludeDepthException.class) {
            reader.read("pipeline-config-include-loop.yml")
        }
    }

    void testDefaultRepoAdded() {
        def config = reader.read("pipeline-templates-defaults.yml")
        def repo = config.resources.repositories[0]

        assertFalse(validator.result.hasErrors())
        assertEquals("templates", repo.name)
    }

    void testStepIncludeLoop() {
        shouldFail(PipelineReader.MaxIncludeDepthException.class) {
            def config = reader.read("pipeline-step-loop.yml")
        }
    }

    void testStepIncludeLocalInclude() {
        def config = reader.read("pipeline-step-local-include.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(2, config.pipeline.steps.size())
        assertEquals("step one", config.pipeline.steps[0].name)
        assertEquals("step two", config.pipeline.steps[1].name)
    }

    void testStepIncludeWithLocalRemote() {
        def config = reader.read("pipeline-step-include-local-remote.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(1, config.pipeline.steps.size())
        assertEquals("step alt two", config.pipeline.steps[0].name)
    }

    void testStepIncludeWithMissingLocalRemote() {
        shouldFail (NoSuchFileException.class) {
            reader.read("pipeline-step-include-missing-local-remote.yml")
        }
    }

    void testRemoteStepIncludeWithLocalInclude() {
        def config = reader.read("pipeline-step-include-remote-references-local.yml")

        assertFalse(validator.result.hasErrors())

        assertEquals(2, config.pipeline.steps.size())
        assertEquals("local step", config.pipeline.steps[0].name)
        assertEquals("step alt two", config.pipeline.steps[1].name)
    }

    void testParentRepositoriesNotUsedForLocalInclude() {
        shouldFail(NoSuchFileException.class) {
            reader.read("pipeline-step-include-parent-remote-not-used.yml")
        }
    }

    void testStandardRepoUsedForLocalInclude() {
        def config = reader.read("pipeline-step-include-standard-remote.yml")
        assertFalse(validator.result.hasErrors())
        assertEquals(2, config.pipeline.steps.size())
    }

    void testValidationOfMainPipelineDuringRead() {
        reader.read("pipeline-bad.yml")

        def expected = [
                "#/pipeline/steps/1: required key [name] not found",
                "#/pipeline/steps/1: required key [include] not found",
                "#/pipeline/steps/1: extraneous key [image] is not permitted"
        ]

        assertEquals(expected, validator.result.errors.first().messages)
    }

    void testStepTimeoutRead() {
        def config = reader.read("pipeline-small-timeouts.yml")
        def steps = config.pipeline.steps

        assertFalse(validator.result.hasErrors())
        assertEquals(2, steps[0].timeoutInMinutes)
        assertEquals(120, steps[1].timeoutInMinutes)
        assertEquals(44, steps[2].timeoutInMinutes)
    }

    void testContinueOnErrorRead() {
        def config = reader.read("pipeline-continue-error.yml")
        def steps = config.pipeline.steps
        assertFalse(validator.result.hasErrors())

        assertNull(steps[0].continueOnError)
        assertFalse(steps[1].continueOnError)
        assertTrue(steps[2].continueOnError)

        assertNull(steps[3].continueOnError)
        assertFalse(steps[4].continueOnError)
        assertTrue(steps[5].continueOnError)

        assertTrue(steps[6].continueOnError)
        assertFalse(steps[7].continueOnError)
    }

    void testPreStepsRead() {
        reader.fileReader = new MappedFilePipelineReader(reader.fileReader, ["_/internal/pre.yml" : "_/internal/pre-with-steps.yml"])
        def config = reader.read("pipeline-small.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(1, config.pipeline.pre.size())
        assertEquals("PipelinePreStep", config.pipeline.pre[0].name)
    }

    void testPreStepsNotValidatingDoesntInvalidatePipelineButRemovesPreSteps() {
        reader.fileReader = new MappedFilePipelineReader(reader.fileReader, ["_/internal/pre.yml" : "_/internal/pre-with-invalid-steps.yml"])
        def config = reader.read("pipeline-small.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(0, config.pipeline.pre.size())
    }

    void testErrorRetrievingPreSteps() {
        reader.fileReader = new MappedFilePipelineReader(reader.fileReader, ["_/internal/pre.yml" : "_/internal/MISSING"])
        def config = reader.read("pipeline-small.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(0, config.pipeline.pre.size())
    }

    void testPostStepsRead() {
        reader.fileReader = new MappedFilePipelineReader(reader.fileReader, ["_/internal/post.yml" : "_/internal/post-with-steps.yml"])
        def config = reader.read("pipeline-small.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(1, config.pipeline.post.size())
        assertEquals("PipelineNotifyInflux", config.pipeline.post[0].name)
    }

    void testPostStepsNotValidatingDoesntInvalidatePipelineButRemovesPostSteps() {
        reader.fileReader = new MappedFilePipelineReader(reader.fileReader, ["_/internal/post.yml" : "_/internal/post-with-invalid-steps.yml"])
        def config = reader.read("pipeline-small.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(0, config.pipeline.post.size())
    }

    void testErrorRetrievingPostSteps() {
        reader.fileReader = new MappedFilePipelineReader(reader.fileReader, ["_/internal/post.yml" : "_/internal/MISSING"])
        def config = reader.read("pipeline-small.yml")

        assertFalse(validator.result.hasErrors())
        assertEquals(0, config.pipeline.pre.size())
    }

    class TestRepositoryDownloadManager implements RepositoryDownloadManager {
        String download(Repository repository) {
            return repository.name
        }
    }
}
