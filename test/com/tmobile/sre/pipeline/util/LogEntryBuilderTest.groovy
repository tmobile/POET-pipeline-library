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
package com.tmobile.sre.pipeline.util

import org.junit.Test

import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.model.PipelineState

import groovy.json.JsonOutput

class LogEntryBuilderTest extends GroovyTestCase {

	static String BUILD_URL = "https://abc.dev.example.com/jenkins/job/Development/job/bworsha1/job/sre-pipeline/job/feature%252Flogging_US123/29/";
	
	static Map envMap = ["PIPELINE_BRANCH":"fake/junk_US123",
		"PIPELINE_JOB_NAME":"Development/john/sre-pipeline/bugfix%2Flogging_US123",
		"PIPELINE_BUILD_URL": BUILD_URL ];
	
	static String SOURCE_ID = "abc.dev.example.com__Development/john/sre-pipeline/bugfix%2Flogging_US123"

	
	@Test
	void testEnv() {
		PipelineEnvironment env = new PipelineEnvironment(envMap);
		PipelineState ps = new PipelineState().withEnv(env);
		
		LogEntryBuilder leb = LogEntryBuilder.forInfo();
		leb.withState(ps);
		String output = JsonOutput.toJson(leb.logEntry);

		assertTrue(output.contains(SOURCE_ID));
	}

	@Test
	void testLoggedMap() {
		LogEntryBuilder leb = LogEntryBuilder.forInfo();
		leb.logEntry.tl_uid = "With message map";
		leb.add("k1", "val 1").add('k2', 'val 2');

		String output = JsonOutput.toJson(leb.logEntry);

		assertTrue(output.contains(':"val 1"'));
	}

	@Test
	void testLoggedMessage() {
		String msg = "The king is a fink!";

		LogEntryBuilder leb = LogEntryBuilder.forInfo();
		leb.logEntry.tl_uid = "With message string";
		leb.set(msg);

		String output = JsonOutput.toJson(leb.logEntry);

		assertTrue(output.contains('"logged":"'+ msg +'"'));
	}
}
