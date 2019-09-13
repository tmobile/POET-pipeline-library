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

class StepState implements Serializable {
	String name;						// friendly name of this step
	String image;						// docker image that implements this step
	String include;						// name of template that this step came from
	List<String> categories = new ArrayList<>();	//  list of category tags
	def startTime;						// start time in milliseconds
	def durationMillis;					// duration of step run in milliseconds
	StepStatus status;
	String explanation;					// a string that explains step execution, especially failure reasons
	
	LinkedHashMap<String, Object> when = new LinkedHashMap<>();
	List<CommandState> commands = [];


	
	StepState(PipelineStep step) {
		this.name = step.name;
		this.image = step.image;
		this.include = step.include;
		this.when.putAll(step.when);
		this.status = StepStatus.SUCCESS;  // assume it works properly
	}

	
	boolean isFailure() {
		return StepStatus.FAILURE.equals(status);
	}

	boolean isSuccess() {
		return StepStatus.SUCCESS.equals(status);
	}
	
	void skipped() {
		this.status = StepStatus.SKIP;
	}

	StepState start() {
		def now = new Date();
		startTime = now.getTime();
		return this;
	}
	
	StepState end() {
		if (!startTime) return;
		def now = new Date();
		durationMillis = now.getTime() - startTime;
		return this;
	}
	
	/**
	 * Command statements should be added just as they are run to provide
	 * running account of what was run and was not if there's a failure.
	 * 
	 * @param command		the most recent command executed
	 */
	void addCommand(CommandState command) {
		commands.add(command);
	}

	def leftShift(CommandState command) {
		addCommand(command);
	}
}
