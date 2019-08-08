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
package com.tmobile.sre.pipeline

import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.model.PipelineStep
import com.tmobile.sre.pipeline.model.StepState

class DockerExecutorTest extends GroovyTestCase {
  StubJenkinsContext context = new StubJenkinsContext()
  StubDockerContext dockerContext = new StubDockerContext()
  def env = new PipelineEnvironment()
  def executor = new DockerExecutor(context, dockerContext)

  void testDefaultTimeoutUsed() {
    PipelineStep step = new PipelineStep(name: "test")
    def stepState = new StepState(step)
    executor.run(new PipelineState(), step, stepState, env, context.logger)

    assertEquals(DockerExecutor.DEFAULT_STEP_TIMEOUT_MINUTES, context.timeoutProvided)
  }

  void testCustomTimeoutUsed() {
    PipelineStep step = new PipelineStep(name: "test", timeoutInMinutes: 42)
    def stepState = new StepState(step)
    executor.run(new PipelineState(), step, stepState, env, context.logger)

    assertEquals(42, context.timeoutProvided)
  }

  void testTimeoutExplanationProvided() {
    PipelineStep step = new PipelineStep(name: "test")
    def stepState = new StepState(step)

    context.triggerTimeout = true

    executor.run(new PipelineState(), step, stepState, env, context.logger)

    assertEquals("30 minute timeout exceeded.", stepState.explanation)
  }

  void testReadStepSingleCategory() {
    String commandSent
    context.metaClass.sh << { args ->
      commandSent = args['script']
      return "build"
    }

    List<String> categories = executor.readStepCategories("mvn")

    assertEquals(commandSent, "docker inspect -f '{{index .Config.Labels \"com.tmobile.poet.step.categories\"}}' mvn")
    assertEquals(["build"], categories)
  }

  void testReadStepMultiCategories() {
    context.metaClass.sh << { args ->
      return "build, analyze, test"
    }

    assertEquals(["build", "analyze", "test"], executor.readStepCategories("mvn"))
  }

  void testReadStepEmptyOnEmptyTag() {
    context.metaClass.sh << { args ->
      return ""
    }

    assertEquals([], executor.readStepCategories("mvn"))
  }


  void testReadStepEmptyOnDockerError() {
    context.metaClass.sh << { args ->
      throw new RuntimeException()
    }

    assertEquals([], executor.readStepCategories("mvn"))
  }
}
