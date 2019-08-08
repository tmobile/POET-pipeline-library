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
package com.tmobile.sre.pipeline.conditions

import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.model.PipelineStatus
import com.tmobile.sre.pipeline.model.PipelineStep

/**
 * Evaluates pipeline steps based on the current pipeline status (SUCCESS, FAILURE).
 *
 * If no status condition is defined, will return true if the current status is SUCCESS.
 *
 * usage:
 *
 *  when:
 *    status: [ failure, success]
 *
 * defaults to the equivalent of:
 *
 *   when:
 *     status: [ success ]
 */
class StatusExecutionCondition implements ExecutionCondition {
  boolean shouldRun(PipelineState state, PipelineEnvironment environment, PipelineStep step) {
    if (! step.when.containsKey("status")) {
      // default to only on success
      return PipelineStatus.SUCCESS.equals(state.status)
    }

    // TODO: can we parse the status from config to enum?
    return state.status.toString().toLowerCase() in step.when.status
  }
}
