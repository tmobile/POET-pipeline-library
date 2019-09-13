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

class ExecutionConditionChainTest extends GroovyTestCase {
  def step = new PipelineStep(when: [:])
  def state = new PipelineState("master")
  def env = new PipelineEnvironment()

  void testShouldRunEmptyChain() {
    assertTrue(new ExecutionConditionChain().shouldRun(state, env, step))
  }


  void testFirstFalseReturnsFalse() {
    def chain = new ExecutionConditionChain(alwaysFalse, alwaysTrue)
    assertFalse(chain.shouldRun(state, env, step))
  }

  void testAllTrueReturnsTrue() {
    def chain = new ExecutionConditionChain(alwaysTrue, alwaysTrue, alwaysTrue)
    assertTrue(chain.shouldRun(state, env, step))
  }

  def alwaysFalse = new ExecutionCondition() {
    @Override
    boolean shouldRun(PipelineState state, PipelineEnvironment environment, PipelineStep step) {
      return false
    }
  }

  def alwaysTrue = new ExecutionConditionChain() {
    @Override
    boolean shouldRun(PipelineState state, PipelineEnvironment environment, PipelineStep step) {
      return true
    }
  }
}
