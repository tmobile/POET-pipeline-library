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
import com.tmobile.sre.pipeline.model.PipelineStep

class BranchExecutionConditionTest extends GroovyTestCase {
  def bc = new BranchExecutionCondition()
  def env = new PipelineEnvironment()

  void testShouldRunWhenNoBranchConditionSpecified() {
    def step = new PipelineStep(when: [:])
    def state = new PipelineState("master")

    assertTrue(bc.shouldRun(state, env, step))
  }

  void testShouldRunWhenBranchMatches() {
    def step = new PipelineStep(when: [branch: "feature/*"])

    assertTrue(bc.shouldRun(new PipelineState("feature/abc"), env, step))
    assertTrue(bc.shouldRun(new PipelineState("feature/def"), env, step))
    assertFalse(bc.shouldRun(new PipelineState("master"), env, step))
  }

  void testShouldNotRunWhenBranchDoesNotMatch() {
    def step = new PipelineStep(when: [branch: "feature/something"])
    def state = new PipelineState("master")

    assertFalse(bc.shouldRun(state, env, step))
  }

  void testMultipleBranches() {
    def step = new PipelineStep(when: [branch: ["feature/a", "feature/b"]])

    assertTrue(bc.shouldRun(new PipelineState("feature/a"), env, step))
    assertTrue(bc.shouldRun(new PipelineState("feature/b"), env, step))
    assertFalse(bc.shouldRun(new PipelineState("feature/c"), env, step))
  }

  void testExplicitInclude() {
    def step = new PipelineStep(when: [branch: [include: ["master"]]])

    assertTrue(bc.shouldRun(new PipelineState("master"), env, step))
    assertFalse(bc.shouldRun(new PipelineState("feature/a"), env, step))
  }


  void testExcludeNoInclude() {
    def step = new PipelineStep(when: [branch: [exclude: ["feature/*"]]])

    assertTrue(bc.shouldRun(new PipelineState("master"), env, step))
    assertFalse(bc.shouldRun(new PipelineState("feature/a"), env, step))
  }

  void testExcludeWithInclude() {
    def step = new PipelineStep(when: [branch: [include: ["feature/*"], exclude: ["feature/b"]]])

    assertTrue(bc.shouldRun(new PipelineState("feature/a"), env, step))
    assertFalse(bc.shouldRun(new PipelineState("feature/b"), env, step))
    assertFalse(bc.shouldRun(new PipelineState("master"), env, step))
  }
}
