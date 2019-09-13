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
package com.tmobile.sre.pipeline.util

import java.text.SimpleDateFormat

import com.tmobile.sre.pipeline.model.LogEntry;
import com.tmobile.sre.pipeline.model.LogEntryLevel;
import com.tmobile.sre.pipeline.model.PipelineState


class LogEntryBuilder implements Serializable {

	static LogEntryBuilder forInfo(){
		return new LogEntryBuilder(LogEntryLevel.INFO);
	}

	static LogEntryBuilder forDebug(){
		return new LogEntryBuilder(LogEntryLevel.DEBUG);
	}

	static LogEntryBuilder forWarn(){
		return new LogEntryBuilder(LogEntryLevel.WARN);
	}

	static LogEntryBuilder forError(){
		return new LogEntryBuilder(LogEntryLevel.ERROR);
	}


	LogEntry logEntry;

	LogEntryBuilder() {
		this.logEntry = new LogEntry();
		def now = new Date();
		logEntry.time = now.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		logEntry.tl_timestamp = formatter.format(now);
	}

	LogEntryBuilder(LogEntryLevel level) {
		this();
		logEntry.level = level;
	}

	
	LogEntryBuilder withState(PipelineState state) {
		if (!state) return this;
		logEntry.tl_uid = state.runId;
		logEntry.tl_source_id = "${state.host}__${state.jobName}";
		return this;
	}

	LogEntryBuilder add(String key, def value) {
		logEntry.loggedMap().put(key, value);
		return this;
	}

	LogEntryBuilder set(def value) {
		logEntry.logged = value;
		return this;
	}

}