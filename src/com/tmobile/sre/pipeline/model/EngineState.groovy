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
package com.tmobile.sre.pipeline.model

class EngineState implements Serializable {
  PipelineState preState
  PipelineState pipelineState
  PipelineState postState

  EngineState(PipelineState preState, PipelineState pipelineState, PipelineState postState) {
    this.preState = preState
    this.pipelineState = pipelineState
    this.postState = postState
  }

  String getRunId() {
    return pipelineState.runId
  }

  PipelineState getPipelineState() {
    if (preState.status.equals(PipelineStatus.FAILURE)) {
      pipelineState.status = PipelineStatus.FAILURE
    }
    pipelineState
  }

  PipelineState getPostState() {
    if (preState.status.equals(PipelineStatus.FAILURE) || pipelineState.status.equals(PipelineStatus.FAILURE)) {
      postState.status = PipelineStatus.FAILURE
    }
    postState
  }

  PipelineStatus getStatus() {
    if (preState.status.equals(PipelineStatus.FAILURE) || pipelineState.status.equals(PipelineStatus.FAILURE) || postState.status.equals(PipelineStatus.FAILURE)) {
      return PipelineStatus.FAILURE
    }

    PipelineStatus.SUCCESS
  }

  // factory method because we can't call .copy() in constr w/ CPS
  static EngineState create(PipelineState initial) {
    new EngineState(initial.copy(), initial.copy(), initial.copy())
  }
}
