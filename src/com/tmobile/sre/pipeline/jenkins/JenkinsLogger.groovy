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
package com.tmobile.sre.pipeline.jenkins

import com.tmobile.sre.pipeline.PipelineLogger
import com.tmobile.sre.pipeline.model.LogEntryLevel
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.util.LogEntryBuilder

import groovy.json.JsonOutput


class JenkinsLogger implements PipelineLogger {
	def jenkinsContext
	PipelineState state

	LogEntryLevel minLevel = LogEntryLevel.WARN

	JenkinsLogger(jenkinsContext, state) {
		this.jenkinsContext = jenkinsContext
		this.state = state
	}

	JenkinsLogger withMinLevel(LogEntryLevel minLevel) {
		this.minLevel = minLevel;
		return this;
	}

	@Override
	void info(Object o) {
		log(LogEntryBuilder.forInfo().set(o));
	}

	@Override
	void debug(Object o) {
		log(LogEntryBuilder.forDebug().set(o));
	}

	@Override
	void warn(Object o) {
		log(LogEntryBuilder.forWarn().set(o));
	}

	@Override
	void error(Object o) {
		log(LogEntryBuilder.forError().set(o));
	}

	void log(LogEntryBuilder leb) {
		// if no level is set in entry, assume info
		if ((leb.logEntry?.level ?: LogEntryLevel.INFO) < minLevel) {
			return;
		}

		leb.withState(state);
		String json = toJson(leb.logEntry);
		jenkinsContext.echo(json);

		// TODO: we should figure out a better way to detect if splunk is installed
		try {
			jenkinsContext.splunkins.send(json);
		} catch (final Exception e) {
			// plugin not installed or splunk error
		}
	}


	@com.cloudbees.groovy.cps.NonCPS
	private String toJson(Object o) {
		JsonOutput.prettyPrint( JsonOutput.toJson(o) );
	}
}
