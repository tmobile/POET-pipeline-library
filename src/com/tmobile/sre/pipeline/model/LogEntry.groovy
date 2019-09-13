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

class LogEntry implements Serializable {

	LogEntryLevel level;
	def tl_timestamp;					// time in ISO 8601
	def tl_uid;  							// the primary unique id for an instance of a pipeline run
	def tl_source_id;  				// key that describes pipeline engine and run
	def time;									// time in millis
	def logged;								// message or payload
	
	
	LogEntry() {
	}

	LogEntry(LogEntryLevel level) {
		this.level = level;
	}

	Map loggedMap () {
		if (null == logged) logged = new HashMap();
		return logged;
	}

}
