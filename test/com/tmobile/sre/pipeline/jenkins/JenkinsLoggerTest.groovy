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

import com.tmobile.sre.pipeline.model.LogEntryLevel
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.util.LogEntryBuilder
import groovy.json.JsonSlurper

class JenkinsLoggerTest extends GroovyTestCase {
  def jenkinsContext = new StubJenkinsLoggingContext()
  PipelineState state = new PipelineState()
  def logger = new JenkinsLogger(jenkinsContext, state)

  void testOnlyLevelsAboveOrEqualToMinLevelLogged() {
    logger.minLevel = LogEntryLevel.DEBUG
    logger.debug("test debug")
    logger.info("test info")
    logger.warn("test warn")
    logger.error("test error")

    assertEquals(["test debug", "test info", "test warn", "test error"], jenkinsContext.logged)

    jenkinsContext.logged.clear()
    logger.minLevel = LogEntryLevel.INFO
    logger.debug("test debug")
    logger.info("test info")
    logger.warn("test warn")
    logger.error("test error")

    assertEquals(["test info", "test warn", "test error"], jenkinsContext.logged)

    jenkinsContext.logged.clear()
    logger.minLevel = LogEntryLevel.WARN
    logger.debug("test debug")
    logger.info("test info")
    logger.warn("test warn")
    logger.error("test error")

    assertEquals(["test warn", "test error"], jenkinsContext.logged)

    jenkinsContext.logged.clear()
    logger.minLevel = LogEntryLevel.ERROR
    logger.debug("test debug")
    logger.info("test info")
    logger.info("test warn")
    logger.error("test error")

    assertEquals(["test error"], jenkinsContext.logged)
  }

  void testNoLogLevelSetInEntryTreatedAsInfo() {
    logger.minLevel = LogEntryLevel.INFO
    logger.log(new LogEntryBuilder().set("test info"))
    assertEquals(["test info"], jenkinsContext.logged)
  }

  static class StubJenkinsLoggingContext {
    def logged = new ArrayList();
    def splunkins = new Object() { def send(def s) { } }

    def echo(def s) {
      def msg = new JsonSlurper().parseText(s)
      logged.add(msg['logged']);
    }
  }
}
