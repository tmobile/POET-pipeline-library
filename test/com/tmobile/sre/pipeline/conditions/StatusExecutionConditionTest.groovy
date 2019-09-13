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
import com.tmobile.sre.pipeline.model.PipelineStatus
import com.tmobile.sre.pipeline.model.PipelineStep

class StatusExecutionConditionTest extends GroovyTestCase {
  def p = new StatusExecutionCondition()

  def env = new PipelineEnvironment()

  def success = new PipelineState("master", PipelineStatus.SUCCESS)
  def failure = new PipelineState("master", PipelineStatus.FAILURE)

  void testWithStatusDefaults() {
    def step = new PipelineStep(when: [:])

    assertTrue(p.shouldRun(success, env, step))
    assertFalse(p.shouldRun(failure, env, step))
  }

  void testWithStatusOnlyFailure() {
    def step = new PipelineStep(when: ["status" : "failure"])

    assertFalse(p.shouldRun(success, env, step))
    assertTrue(p.shouldRun(failure, env, step))
  }

  void testWithStatusExplicitSuccess() {
    def step = new PipelineStep(when: ["status" : "success"])

    assertTrue(p.shouldRun(success, env, step))
    assertFalse(p.shouldRun(failure, env, step))
  }

  void testWithStatusSuccessOrFailure() {
    def step = new PipelineStep(when: ["status" : ["success", "failure"]])

    assertTrue(p.shouldRun(success, env, step))
    assertTrue(p.shouldRun(failure, env, step))
  }
}
