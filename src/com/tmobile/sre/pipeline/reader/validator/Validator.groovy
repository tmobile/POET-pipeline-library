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
package com.tmobile.sre.pipeline.reader.validator

import com.tmobile.sre.pipeline.reader.PipelineFile
import com.tmobile.sre.pipeline.reader.PipelineFileReader

/**
 * PipelineValidator implementation.
 *
 * This class was intended to pre-read the schemas in its constructor, but
 * that is not possible: https://issues.jenkins-ci.org/browse/JENKINS-26313
 *
 * This class is CPS and wraps the NonCPS methods in JsonSchemaValidator
 * @see: JsonSchemaValidator
 */
class Validator implements PipelineValidator {
  PipelineFileReader fileReader

  ValidationResult result = new ValidationResult()

  Validator(PipelineFileReader fileReader) {
    this.fileReader = fileReader
  }

  private String readSchema(String name) {
    fileReader.read("schema/${name}.yml").text
  }

  def validatePipeline(PipelineFile pipeline) {
    validate(readSchema("pipeline-schema"), pipeline)
  }

  def validateStepTemplate(PipelineFile template) {
    validate(readSchema( "pipeline-step-include-schema"), template)
  }

  def validateConfig(PipelineFile template) {
    validate( readSchema( "pipeline-config-include-schema"), template)
  }

  ValidationResult getResult() {
    return result
  }

  PipelineValidator create() {
    return new Validator(fileReader)
  }

  private void validate(String schema, PipelineFile file) {
    try {
      def sharedDefs = readSchema("pipeline-schema-defs")
      def v = new JsonSchemaValidator(sharedDefs)
      def errors = v.validate(schema, file)

      if (!errors.isEmpty()) {
        result.reportErrors(file.path, errors)
      }
    } catch (final Exception e) {
      // TOOD:
    }
  }
}
