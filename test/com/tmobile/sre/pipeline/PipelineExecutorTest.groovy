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
import com.tmobile.sre.pipeline.model.*

class PipelineExecutorTest extends GroovyTestCase {
    def context = new StubJenkinsContext()
    def initial = new PipelineState("master")
    def fs = new StubPipelineFilesystem()
    def env = new PipelineEnvironment()
    def pipeline = new PipelineContextStub()

    void testExecuteEmptyPipeline() {
        def config = new PipelineDefinition(pipeline: new Pipeline())

        def docker = context.dockerExecutor(StepStatus.SUCCESS)

        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config).pipelineState

        assertEquals(PipelineStatus.SUCCESS, state.status)
    }

    void testExecuteSingleStepPipeline() {
        def config = new PipelineDefinition(pipeline: new Pipeline())
        def step = new PipelineStep(name: "test step", image: "_")
        config.pipeline.steps << step

        def docker = context.dockerExecutor(StepStatus.SUCCESS)

        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config).pipelineState

        assertEquals(PipelineStatus.SUCCESS, state.status)
        assertEquals(StepStatus.SUCCESS, state.stepStates[0].status)
    }

    void testExecuteFailedStepFailsJob() {
        def config = new PipelineDefinition(pipeline: new Pipeline())
        def step = new PipelineStep(name: "test step", image: "_")
        config.pipeline.steps << step

        def docker = context.dockerExecutor(StepStatus.FAILURE)


        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config).pipelineState

        assertEquals(PipelineStatus.FAILURE, state.status)
        assertEquals(StepStatus.FAILURE, state.stepStates[0].status)
    }

    void testExecuteFailedStepsDoesntFailJobIfMarkedContinueOnFailure() {
        def config = new PipelineDefinition(pipeline: new Pipeline())
        def step = new PipelineStep(name: "test step", image: "_", continueOnError: true)
        config.pipeline.steps << step

        def docker = context.dockerExecutor(StepStatus.FAILURE)

        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config).pipelineState
        assertEquals(PipelineStatus.SUCCESS, state.status)
    }

    void testExecuteFailedStepsFailsJobIfMarkedContinueIsNull() {
        def config = new PipelineDefinition(pipeline: new Pipeline())
        def step = new PipelineStep(name: "test step", image: "_", continueOnError: null)
        config.pipeline.steps << step

        def docker = context.dockerExecutor(StepStatus.FAILURE)

        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config).pipelineState
        assertEquals(PipelineStatus.FAILURE, state.status)
    }

    void testSkippedStepsMarkedSkip() {
        def config = new PipelineDefinition(pipeline: new Pipeline())
        def when = [environment: [SOMETHING: "NOT_SET"]]
        def step = new PipelineStep(name: "test step", image: "_", continueOnError: true, when: when)
        config.pipeline.steps << step

        // step would fail, but it's skipped so we should never even execute
        def docker = context.dockerExecutor(StepStatus.FAILURE)

        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config).pipelineState
        assertEquals(PipelineStatus.SUCCESS, state.status)
        assertEquals(StepStatus.SKIP, state.stepStates[0].status)
    }

    void testIfPreStepsFailPipelineMarkedFailAndStepsSkippedUnlessMarkedForFailure() {
        def config = new PipelineDefinition(pipeline: new Pipeline())

        config.pipeline.steps << new PipelineStep(name: "test step", image: "_")
        config.pipeline.steps << new PipelineStep(name: "test step 2", image: "_", when: [ status: ["success", "failure"]])
        config.pipeline.pre << new PipelineStep(name: "pre step", image: "_")
        config.pipeline.post << new PipelineStep(name: "post step", image: "_", when: [ status: ["success", "failure"]])

        def docker = context.dockerExecutor(StepStatus.SUCCESS, ["pre step" : StepStatus.FAILURE])

        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config)

        assertEquals(PipelineStatus.FAILURE, state.status)
        assertEquals(PipelineStatus.FAILURE, state.preState.status)
        assertEquals(PipelineStatus.FAILURE, state.pipelineState.status)
        assertEquals(PipelineStatus.FAILURE, state.postState.status)

        assertEquals(StepStatus.FAILURE, state.preState.stepStates[0].status)
        assertEquals(StepStatus.SKIP, state.pipelineState.stepStates[0].status)
        assertEquals(StepStatus.SUCCESS, state.pipelineState.stepStates[1].status)
        assertEquals(StepStatus.SUCCESS, state.postState.stepStates[0].status)
    }

    void testIfPostStepsFailEngineMarkedFail() {
        def config = new PipelineDefinition(pipeline: new Pipeline())

        config.pipeline.steps << new PipelineStep(name: "test step", image: "_")
        config.pipeline.pre << new PipelineStep(name: "pre step", image: "_")
        config.pipeline.post << new PipelineStep(name: "post step", image: "_", when: [ status: ["success", "failure"]])

        def docker = context.dockerExecutor(StepStatus.SUCCESS, ["post step" : StepStatus.FAILURE])

        def state = new PipelineExecutor(context.logger, docker, env, pipeline, fs).execute(initial, config)

        assertEquals(PipelineStatus.FAILURE, state.status)
        assertEquals(PipelineStatus.SUCCESS, state.preState.status)
        assertEquals(PipelineStatus.SUCCESS, state.pipelineState.status)
        assertEquals(PipelineStatus.FAILURE, state.postState.status)

        assertEquals(StepStatus.SUCCESS, state.preState.stepStates[0].status)
        assertEquals(StepStatus.SUCCESS, state.pipelineState.stepStates[0].status)
        assertEquals(StepStatus.FAILURE, state.postState.stepStates[0].status)
    }


    class PipelineContextStub implements PipelineContext {
        void stage(String name, Closure body) {
            body.run()
        }
    }
}
