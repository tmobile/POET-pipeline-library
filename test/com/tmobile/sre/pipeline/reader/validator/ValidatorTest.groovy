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
package com.tmobile.sre.pipeline.reader.validator

import com.tmobile.sre.pipeline.reader.ResourcePipelineFileReader

class ValidatorTest extends GroovyTestCase {
  Validator validator = new Validator(new ResourcePipelineFileReader())
  ResourcePipelineFileReader fileReader = new ResourcePipelineFileReader()

  void testValidateMainFile() {
    def t = fileReader.read("pipeline-bad.yml")
    validator.validatePipeline(t)

    assertEquals(1, validator.result.errors.size())

    def error = validator.result.errors.first()

    def expected = ["#/pipeline/steps/1: required key [name] not found",
                    "#/pipeline/steps/1: required key [include] not found",
                    "#/pipeline/steps/1: extraneous key [image] is not permitted"]


    assertEquals(expected, error.messages)
    assertEquals("pipeline-bad.yml", error.path.path)
  }

  void testValidateMainFilePropLength() {
    def t = fileReader.read("pipeline-bad-prop-length.yml")
    validator.validatePipeline(t)

    def error = validator.result.errors.first()
    // this is something we can probably improve in the future -- because
    // steps can be a step or an include, usually if one fails they both fail
    // so we get a lot of error messages
    def expected = [
        "#/pipeline/appVersion/master: expected maxLength: 64, actual: 72",
        "#/pipeline/steps/0/name: expected maxLength: 64, actual: 193",
        "#/pipeline/steps/0: required key [include] not found",
        "#/pipeline/steps/0: extraneous key [image] is not permitted",
        "#/pipeline/steps/0: extraneous key [name] is not permitted",
        "#/pipeline/steps/0: extraneous key [commands] is not permitted"
    ]

    assertEquals(expected, error.messages)
  }

  void testValidateMainMaxSteps() {
    def t = fileReader.read("pipeline-bad-max-steps.yml")
    validator.validatePipeline(t)

    def error = validator.result.errors.first()
    def expected = [
        "#/pipeline/steps: expected maximum item count: 64, found: 120"
    ]

    assertEquals(expected, error.messages)
  }

  void testValidateMainNoSteps() {
    def t = fileReader.read("pipeline-bad-no-steps.yml")
    validator.validatePipeline(t)

    assertFalse(validator.result.hasErrors())
  }

  void testValidateStepInclude() {
    def t = fileReader.read("templates/step-bad-with-pipeline.yml")
    validator.validateStepTemplate(t)

    def error = validator.result.errors.first()
    def expected = [
        "#: required key [steps] not found",
        "#: extraneous key [pipeline] is not permitted"
    ]

    assertEquals(expected, error.messages)
  }

  void testValidateStepIncludeNoCommands() {
    def t = fileReader.read("templates/step-bad-no-commands.yml")
    validator.validateStepTemplate(t)

    def error = validator.result.errors.first()

    // this is something we can probably improve in the future -- because
    // steps can be a step or an include, usually if one fails they both fail
    // so we get a lot of error messages
    def expected = [
        "#/steps/0/commands: expected minimum item count: 1, found: 0",
        "#/steps/0: required key [include] not found",
        "#/steps/0: extraneous key [image] is not permitted",
        "#/steps/0: extraneous key [name] is not permitted",
        "#/steps/0: extraneous key [commands] is not permitted"
    ]

    assertEquals(expected, error.messages)
  }

  void testValidateConfigIncludeBadSteps() {
    def t = fileReader.read("templates/config-bad-with-steps.yml")
    validator.validateConfig(t)

    def error = validator.result.errors.first()
    def expected = [
        "#/pipeline: extraneous key [steps] is not permitted"
    ]

    assertEquals(expected, error.messages)
  }
}
