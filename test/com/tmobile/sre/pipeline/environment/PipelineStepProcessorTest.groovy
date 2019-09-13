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
package com.tmobile.sre.pipeline.environment

import com.tmobile.sre.pipeline.model.PipelineStep
import com.tmobile.sre.pipeline.model.Secret

class PipelineStepProcessorTest extends GroovyTestCase {
    def processor = new PipelineStepProcessor()


    void testProcessStep() {
        def env = new PipelineEnvironment(["GREETING": "hello", "VERSION": "1.0.0", "MY_SECRET_ID": "jenkins_secret123"])

        def secret = new Secret(source:  '${MY_SECRET_ID}')
        def input = new PipelineStep(name: 'step ${GREETING}', image: 'my_image:${VERSION}', secrets: [secret])

        def step = processor.process(env, input)

        assertEquals("step hello", step.name)
        assertEquals("my_image:1.0.0", step.image)
        assertEquals("jenkins_secret123", step.secrets.first().source)
    }

    void testProcess() {
        def m = ["a":"1", "b": ["x": "1"]]

        assertEquals(["A":"1", "B": '{"x":"1"}'], processor.process(m))
    }

    void testGenerateEnvironmentList() {
        def env = new PipelineEnvironment(["XX": "hello"])

        def m = ["a":"1", "b": ["x": '${XX}+1']]

        def l = processor.generateEnvironmentList(env, m)

        assertEquals(["A=1", 'B={"x":"hello+1"}'], l)
    }

    void testFlattenEmpty() {
        assertEquals([:], processor.flatten([:]))
    }

    void testScalarFlatten() {
        assertEquals(["a": "1"], processor.flatten(["a": "1"]))
    }

    void testNonStringScalar() {
        assertEquals(["a":"1"], processor.flatten(["a":1]))
    }

    void testSimpleListFlatten() {
        assertEquals("a": "1,2,3", processor.flatten(["a": ["1", "2", "3"]]))
    }

    void testMapFlatten() {
        def m = ["a": ["x": "1", "y": "2"]]
        assertEquals("a": '{"x":"1","y":"2"}', processor.flatten(m))
    }

}
