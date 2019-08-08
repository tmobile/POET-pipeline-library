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
package com.tmobile.sre.pipeline.environment


import com.tmobile.sre.pipeline.model.Pipeline
import com.tmobile.sre.pipeline.model.PipelineDefinition

class PipelineEnvironmentProcessorTest extends GroovyTestCase {

    def processor = new PipelineEnvironmentProcessor()

    void testVersionAdded() {
        Pipeline p = new Pipeline(appName: "my-app", appVersion: ["master": "0.0.1"])

        def ret = processor.processGlobals(new PipelineEnvironment(), new PipelineDefinition(pipeline: p))
        assertEquals("0.0.1", ret.get("PIPELINE_APP_VERSION"))
    }

    void testBasicInfo() {
        Pipeline p = new Pipeline(appName:  "my-app", appGroup: "my-group", appOwner: "my-owner", appAkmid: "my-akmid")
        def ret = processor.processGlobals(new PipelineEnvironment(), new PipelineDefinition(pipeline: p))

        assertEquals("my-app", ret.get("PIPELINE_APP_NAME"))
        assertEquals("my-group", ret.get("PIPELINE_APP_GROUP"))
        assertEquals("my-owner", ret.get("PIPELINE_APP_OWNER"))
        assertEquals("my-akmid", ret.get("PIPELINE_APP_AKMID"))
    }


    void testVersionAddedForBranch() {
        Pipeline p = new Pipeline(appName: "my-app", appVersion: ["master": "0.0.1"])

        def ret = processor.processGlobals(new PipelineEnvironment(), new PipelineDefinition(pipeline: p))
        assertEquals("0.0.1", ret.get("PIPELINE_APP_VERSION"))
    }

    void testNoVersionAddedIfNoVersions() {
        Pipeline p = new Pipeline(appName: "my-app", appVersion: ["master": "0.0.1"])

        def ret = processor.processGlobals(new PipelineEnvironment(), new PipelineDefinition(pipeline: p))
        assertEquals("0.0.1", ret.get("PIPELINE_APP_VERSION"))
    }


    void testMasterVersionWithBranchNameUsed() {
        Pipeline p = new Pipeline(appName: "my-app", appVersion: ["master": "0.0.1"])

        def ret = processor.processGlobals(new PipelineEnvironment(["PIPELINE_BRANCH": "my_branch"]), new PipelineDefinition(pipeline: p))
        assertEquals("my_branch.0.0.1", ret.get("PIPELINE_APP_VERSION"))

        ret = processor.processGlobals(new PipelineEnvironment(["PIPELINE_BRANCH": "feature/my_branch"]), new PipelineDefinition(pipeline: p))
        assertEquals("feature_my_branch.0.0.1", ret.get("PIPELINE_APP_VERSION"))
    }

    void testProcessGlobals() {
        PipelineEnvironment env = new PipelineEnvironment()
        Pipeline p = new Pipeline(appName: "my-app")

        def ret = processor.processGlobals(env, new PipelineDefinition(pipeline: p))

        assertEquals(["PIPELINE_APP_NAME" : "my-app"], ret.toMap())
    }

    void testProcessUserEnvironment() {
        PipelineEnvironment env = new PipelineEnvironment(["X": "zzz"])

        Pipeline p = new Pipeline(environment: ["A": "1", "B": '${X}zz', "C": '${A}'])

        def ret = processor.processUserEnvironment(env, new PipelineDefinition(pipeline: p))

        assertEquals(["A": "1", "B": "zzzzz", "C": "1", "X": "zzz"], ret.toMap())
    }
}
