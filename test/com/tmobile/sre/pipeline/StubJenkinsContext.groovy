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
import com.tmobile.sre.pipeline.model.StepStatus


/**
 * Stubbed out JenkinsContext for tests.
 *
 * Note, we don't call all of the closures currently passed to jenkins, e.g. withEnv, etc.
 *
 * This class will probably need to be extended for future tests.
 */
class StubJenkinsContext {
  def currentBuild = ['changeSets': []]
  def env = new StubEnvironment()

  Integer timeoutProvided

  boolean triggerTimeout = false


  def stage(String stage, Closure body) {
    println(stage)
    body.run()
  }

  def echo(String s) {
    println(s)
  }

  def writeFile(def options) {

  }

  def timeout(def options, Closure body) {
    timeoutProvided = options['time']

    body.run()

    if (triggerTimeout) {
      throw new FlowInterruptedException()
    }
  }

  def withEnv(List environment, Closure body) {

  }

  StubDocker dockerExecutor(StepStatus s, Map<String, StepStatus> stepReturn = [:]) {
    return new StubDocker(s, stepReturn)
  }

  class StubDocker extends DockerExecutor {
    StepStatus returnStatus
    Map<String, StepStatus> stepReturn

    StubDocker(StepStatus returnStatus, Map<String, StepStatus> stepReturn = [:]) {
      super(new StubJenkinsContext(), null)
      this.returnStatus = returnStatus
      this.stepReturn = stepReturn
    }

    @Override
    StepStatus run(PipelineState state, PipelineStep step, StepState stepState, PipelineEnvironment environment, PipelineLogger logger) {
      def status = stepReturn.getOrDefault(step.name, returnStatus)

      // this is currently done in DockerExecutor...
      stepState.status = status

      status
    }
  }

  static class StubEnvironment {
    Map<String, String> vars

    StubEnvironment(Map<String, String> vars) {
      this.vars = vars
    }

    def getProperty(String name) {
      return vars.get(name)
    }
  }

  PipelineLogger logger = new PipelineLogger() {
    @Override
    void info(Object o) {
      println(o.toString())
    }

    @Override
    void debug(Object o) {
      println(o.toString())
    }
    @Override
    void warn(Object o) {
      println(o.toString())
    }

    @Override
    void error(Object o) {
      println(o.toString())
    }
  }

  // test exception that will trigger timeout detection
  class FlowInterruptedException extends RuntimeException {

  }
}
