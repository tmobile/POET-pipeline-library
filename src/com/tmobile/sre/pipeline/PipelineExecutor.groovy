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
package com.tmobile.sre.pipeline


import com.tmobile.sre.pipeline.conditions.ExecutionConditions
import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.environment.PipelineStepProcessor
import com.tmobile.sre.pipeline.model.*
import com.tmobile.sre.pipeline.reader.PipelineFilesystem
import groovy.json.JsonOutput

class PipelineExecutor implements Serializable {
  PipelineLogger logger
  DockerExecutor containerExecutor

  PipelineEnvironment environment;

  PipelineFilesystem fs;

  PipelineStepProcessor stepProcessor = new PipelineStepProcessor()

  PipelineContext pipeline;

  PipelineExecutor(logger, containerExecutor, PipelineEnvironment environment, PipelineContext pipeline, PipelineFilesystem fs) {
    this.logger = logger
    this.containerExecutor = containerExecutor
    this.environment = environment
    this.fs = fs
    this.pipeline = pipeline
  }

  EngineState execute(PipelineState state, PipelineDefinition pipelineConfig) {
    EngineState engineState = EngineState.create(state)

    def internal = new NonReportedStageContext(pipeline)

    executeSteps(internal, engineState.preState, engineState, pipelineConfig.pipeline.pre)
    executeSteps(pipeline, engineState.pipelineState, engineState, pipelineConfig.pipeline.steps)
    executeSteps(internal, engineState.postState, engineState, pipelineConfig.pipeline.post)

    return engineState
  }

  private PipelineState executeSteps(PipelineContext pipelineContext, final PipelineState sectionState, EngineState engineState, List<PipelineStep> steps) {
    def predicate = ExecutionConditions.all();

    for (def i = 0; i < steps.size(); i++) {
      def step = stepProcessor.process(environment, steps[i])
      StepState stepState = sectionState.add(new StepState(step));
      sectionState.end(); // calculate current duration

      withStepEnv(engineState) { PipelineEnvironment stepEnv ->
        if (predicate.shouldRun(sectionState, stepEnv, step)) {
          pipelineContext.stage(step.name) {
            def stepExecutionStatus = containerExecutor.run(sectionState, step, stepState, stepEnv, logger)
            sectionState.updatePipelineStatus(step, stepExecutionStatus)
          }
        } else {
          stepState.skipped();        // mark step as skipped, not effecting pipeline state
        }
      }
    }

    sectionState
  }

  private void withStepEnv(EngineState engineState, Closure body) {
    def pipelineState = engineState.pipelineState

    def pipelineStateFileName = this.fs.writeFile(this.fs.tmp(), ".pipeline_state_" + pipelineState.runId, toJson(pipelineState))
    def engineStateFileName = this.fs.writeFile(this.fs.tmp(), ".engine_state_" + engineState.runId, toJson(engineState))

    def stepEnv = environment.with([
        "PIPELINE_STATUS": pipelineState.status.toString(),
        "PIPELINE_STATE_JSON": pipelineStateFileName,
        "PIPELINE_ENGINE_STATE_JSON" : engineStateFileName
    ])

    body.call(stepEnv)

    this.fs.rm(pipelineStateFileName)
  }


  @com.cloudbees.groovy.cps.NonCPS
  private String toJson(Object o) {
    JsonOutput.toJson(o);
  }

  class NonReportedStageContext implements PipelineContext {
    private PipelineContext context

    NonReportedStageContext(PipelineContext context) {
      this.context = context
    }

    @Override
    void stage(String name, Closure body) {
      context.stage(null, body)
    }
  }
}
