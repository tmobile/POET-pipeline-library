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
package com.tmobile.sre.pipeline.model

import org.junit.Test
import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import groovy.json.JsonOutput


class PipelineStateTest extends GroovyTestCase {
	
	static String BUILD_URL = "https://abc.dev.example.com/jenkins/job/Development/job/bworsha1/job/sre-pipeline/job/feature%252Flogging/29/";
	static String BUILD_HOST = "abc.dev.example.com";
	
	static Map envMap = ["PIPELINE_BRANCH":"fake/junk_US123",
		"PIPELINE_BUILD_NUMBER":"101", "PIPELINE_BUILD_STATUS":"TESTING",
		"PIPELINE_BUILD_URL": BUILD_URL,
		"PIPELINE_JOB_NAME":"Development/john/sre-pipeline/bugfix%2Flogging",
		"PIPELINE_COMMIT":"668", "PIPELINE_COMMIT_MESSAGE":"Not ready for commitment", "PIPELINE_COMMIT_AUTHOR":"john.q.public",
		"PIPELINE_APP_OWNER":"The Man", "PIPELINE_APP_GROUP":"groupie",
		"PIPELINE_APP_NAME":"UnitTestApp", "PIPELINE_APP_VERSION":"0.0.0" ];

	String prettyJson(def obj) {
		JsonOutput.prettyPrint( JsonOutput.toJson(obj) );
	}
	
	@Test
	void test_init_start_end() {		
		PipelineEnvironment env = new PipelineEnvironment(envMap);		
		PipelineState state = new PipelineState().withEnv(env);		
		state.start();
		sleep(1000);
		state.end();
		String output = prettyJson(state);		
		println(output);
		assertTrue(output.contains(BUILD_URL));
		assertNotNull(state.startTime);
		assertTrue( state.durationMillis > 900);
	}
	
	@Test
	void test_no_start() {
		PipelineEnvironment env = new PipelineEnvironment(envMap);
		PipelineState state = new PipelineState().withEnv(env);
		state.end();  // end with out start should yield no duration
		String output = prettyJson(state);
		println(output);
		assertFalse( state.durationMillis > 0 );
	}
	
	@Test
	void test_goodUrl() {
		String hostName = PipelineState.hostFrom(BUILD_URL);
		assertTrue( hostName.equals(BUILD_HOST) );
	}
	
	@Test
	void test_badUrl() {
		String hostName = PipelineState.hostFrom(null);
		assertTrue( hostName.equals('') );
		hostName = PipelineState.hostFrom('http:/this.is  not.a.url');
		assertTrue( hostName.equals('') );
	}
	

}
