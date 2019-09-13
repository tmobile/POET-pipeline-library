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

import com.tmobile.sre.pipeline.PipelineLogger

/**
 * Validation report formatting.
 *
 * Example output:
 * {
 *     "level": "ERROR",
 *     "time": 1554301125037,
 *     "logged": {
 *         "message": "PIPELINE VALIDATION FAILED",
 *         "errors": [
 *             {
 *               "path": {
 *                     "path": "pipeline.yml"
 *                 },
 *                 "messages": [
 *                     "#/pipeline/steps/1: required key [image] not found",
 *                     "#/pipeline/steps/1: required key [include] not found",
 *                     "#/pipeline/steps/1: extraneous key [name] is not permitted"
 *            ]
 *             }
 *         ]
 *     },
 *     "tl_uid": "8d9c2a39-1db6-457d-8d49-5c4e6e7725fb",
 *    "tl_timestamp": "2019-04-03T14:18:45.037Z"
 * }
 */
class ValidationReporter {
  static printReport(PipelineLogger logger, ValidationResult result) {
    if (result.hasErrors()) {
      // we take advantage that our logger supports json/objects
      logger.error([
          "message": "PIPELINE VALIDATION FAILED",
          "errors": result.errors
      ])
    }
  }
}
