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

class SimpleEnvironmentMatchConditionTest extends GroovyTestCase {
  def ec = new SimpleEnvironmentMatchCondition()
  def state = new PipelineState("master")


  void testShouldRunWhenNoConditionSpecified() {
    def step = new PipelineStep(when: [:])
    def env = new PipelineEnvironment()

    assertTrue(ec.shouldRun(state, env, step))
  }

  void testShouldRunWhenNoConditionsEmpty() {
    def step = new PipelineStep(when: [environment: [:]])
    def env = new PipelineEnvironment()

    assertTrue(ec.shouldRun(state, env, step))
  }

  void testShouldNotRunWhenConditionIsNotPresent() {
    def step = new PipelineStep(when: [environment: [DEPLOY: "true"]])
    def env = new PipelineEnvironment()

    assertFalse(ec.shouldRun(state, env, step))
  }

  void testShouldNotRunWhenConditionDoesNotMatch() {
    def step = new PipelineStep(when: [environment: [DEPLOY: "true"]])
    def env = new PipelineEnvironment(["DEPLOY": "false"])

    assertFalse(ec.shouldRun(state, env, step))
  }

  void testShouldRunWhenConditionMatches() {
    def step = new PipelineStep(when: [environment: [DEPLOY: "true"]])
    def env = new PipelineEnvironment(["DEPLOY": "true"])

    assertTrue(ec.shouldRun(state, env, step))
  }

  void testShouldRunWhenAllConditionsMatch() {
    def step = new PipelineStep(when: [environment: [DEPLOY: "true", DEPLOY_ENV: "qa*"]])
    def env = new PipelineEnvironment(["DEPLOY": "true", "DEPLOY_ENV": "qa01"])

    assertTrue(ec.shouldRun(state, env, step))
  }

  void testShouldNotRunWhenAllConditionsDoNotMatch() {
    def step = new PipelineStep(when: [environment: [DEPLOY: "true", DEPLOY_ENV: "qa*"]])
    def env = new PipelineEnvironment(["DEPLOY": "true", "DEPLOY_ENV": "dev01"])

    assertFalse(ec.shouldRun(state, env, step))
  }

  void testWildcardIncludeMatch() {
    def step = new PipelineStep(when: [environment: [DEPLOY_ENV: [include: ["qa*"]]]])
    def env = new PipelineEnvironment(["DEPLOY_ENV": "qa011"])

    assertTrue(ec.shouldRun(state, env, step))
  }


  void testWildcardExcludeMatch() {
    def step = new PipelineStep(when: [environment: [DEPLOY_ENV: [exclude: ["qa*"]]]])
    def env = new PipelineEnvironment(["DEPLOY_ENV": "qa011"])

    assertFalse(ec.shouldRun(state, env, step))
  }
}
