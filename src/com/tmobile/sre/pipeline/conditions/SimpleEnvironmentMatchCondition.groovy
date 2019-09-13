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
package com.tmobile.sre.pipeline.conditions

import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.model.PipelineStep

/**
 * Implements environment condition tests for conditional execution of pipeline steps.
 *
 * The environment variables can be standard pipeline variables, job build variables, or user defined variables.
 *
 * Matches against multiple environment variables are supported. All matches must be successful to run the step.
 *
 * Example:
 *
 * Execute a step using include and exclude logic:
 *
 * # skip this step if the commit message contains "skip ci"
 * when:
 *   environment:
 *     PIPELINE_COMMIT_MESSAGE:
 *       exclude: "*skip ci*"
 *
 *
 * @see StringMatchSpec
 */
class SimpleEnvironmentMatchCondition implements ExecutionCondition {
  boolean shouldRun(PipelineState state, PipelineEnvironment environment, PipelineStep step) {
    if (step.when.environment == null) {
      return true
    }

    def keys = step.when.environment.keySet()

    for (int i=0; i < keys.size(); i++) {
      def cond = step.when.environment.get(keys[i])
      def val = environment.get(keys[i])

      final StringMatchSpec spec = StringMatchSpec.fromSpecString(cond)

      if (! spec.matches(val)) {
        return false
      }
    }

    return true
  }
}
