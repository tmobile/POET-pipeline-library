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

class CommandState implements Serializable {
	String line;						// command line
	def startTime;						// start time in milliseconds
	def durationMillis;					// duration of step run in milliseconds

	
	CommandState(String line) {
		this.line = line;
	}

	
	CommandState start() {
		def now = new Date();
		startTime = now.getTime();
		return this;
	}
	
	CommandState end() {
		if (!startTime) return;
		def now = new Date();
		durationMillis = now.getTime() - startTime;
		return this;
	}
	
}
