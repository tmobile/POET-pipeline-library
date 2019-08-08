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

import com.tmobile.sre.pipeline.reader.PipelineFilePath


class ValidationResult implements Serializable {
  List<ValidationError> errors = new ArrayList<>()

  ValidationResult reportError(ValidationError error) {
    this.errors.add(error)
    this
  }

  ValidationResult reportErrors(List<ValidationError> errors) {
    this.errors.addAll(errors)
    this
  }

  ValidationResult reportErrors(PipelineFilePath filePath, List<String> errors) {
    ValidationError ve = new ValidationError(path: filePath, messages: errors)
    this.errors.add(ve)
    this
  }

  ValidationResult plus(ValidationResult other) {
    new ValidationResult().reportErrors(errors).reportErrors(other.errors)
  }

  ValidationResult add(ValidationResult other) {
    this.errors.addAll(other.errors)
    this
  }

  boolean hasErrors() {
    return ! errors.isEmpty()
  }


  static ValidationResult empty = new ValidationResult(errors: Collections.emptyList())
}
