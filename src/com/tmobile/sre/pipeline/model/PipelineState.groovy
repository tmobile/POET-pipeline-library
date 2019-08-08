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
package com.tmobile.sre.pipeline.model

import org.codehaus.groovy.runtime.InvokerHelper

import java.text.SimpleDateFormat

import com.tmobile.sre.pipeline.environment.PipelineEnvironment


class PipelineState implements Serializable {

	private static String timestampFrom(def date) {
		if ( null == date ) return '';
		// the simplest way to deal with a formatter that is not thread-safe
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");		
		return formatter.format(date);
	}

	private static String hostFrom(def urlString) {
		if ( null == urlString ) return '';
		try {
			def url = new URL(urlString);
			return url.host;
		} catch (MalformedURLException x) {
			return '';
		}	
	}
	
	String version = "1.0"					// schema version
	String runId;  							// the primary unique id for an instance of a pipeline run
	String buildUrl							// for identification
	String host;							// for identification
	String jobName;							// for identification
	String runNumber;						// for identification
	String branch;							// for identification
	def startTime							// start time in milliseconds
	String startTimestamp					// start time in ISO 8601
	def durationMillis						// duration of pipeline run in milliseconds
	String commitId;						// for traceability
	String commitMessage;					// for traceability
	String commitAuthor;					// for traceability
	String appOwner;						// name of team or entity that is responsible for the app and pipeline
	String appGroup;						// name of asset group that contains the asset
	String appName;							// name of asset going through pipeline
	String appVersion;						// the app's current semantic version
	String appAkmid;						// the app's enterprise identity
	PipelineStatus status;
	List<StepState> stepStates = [];

	
	PipelineState(String branch) {
		this.branch = branch;
		this.status = PipelineStatus.SUCCESS;
	}

	PipelineState(String branch, PipelineStatus status) {
		this.branch = branch
		this.status = status
	}

	PipelineState() {
		this.runId = UUID.randomUUID().toString();
		this.status = PipelineStatus.SUCCESS;
	}


	PipelineState withEnv(PipelineEnvironment env) {
		this.buildUrl = env.get('PIPELINE_BUILD_URL');
		this.host = hostFrom(this.buildUrl);
		this.jobName = env.get('PIPELINE_JOB_NAME');		
		this.branch = env.get('PIPELINE_BRANCH');
		this.runNumber = env.get('PIPELINE_BUILD_NUMBER');
		this.commitId = env.get('PIPELINE_COMMIT');
		this.commitMessage = env.get('PIPELINE_COMMIT_MESSAGE');
		this.commitAuthor = env.get('PIPELINE_COMMIT_AUTHOR');		
		this.appOwner = '';
		this.appGroup = '';
		this.appName = env.get('PIPELINE_APP_NAME');
		this.appVersion = env.get('PIPELINE_APP_VERSION');
		return this;
	}

	PipelineState start() {
		def now = new Date();
		startTime = now.getTime();
		startTimestamp = timestampFrom(now);
		return this;
	}
	
	PipelineState end() {
		if (!startTime) return;
		def now = new Date();
		durationMillis = now.getTime() - startTime;
		return this;
	}
	
	StepState add(StepState stepState) {
		stepStates.add(stepState);
		return stepState;
	}

	def updatePipelineStatus(PipelineStep step, StepStatus status) {
		if (! step.continueOnError) {
			if (StepStatus.FAILURE.equals(status)) {
				this.status = PipelineStatus.FAILURE
			}
		}
	}

	PipelineState copy() {
		PipelineState state = new PipelineState()

		InvokerHelper.setProperties(state, this.getProperties())

		// force list copy
		state.stepStates = new ArrayList<>(this.stepStates)

		return state
	}
}
