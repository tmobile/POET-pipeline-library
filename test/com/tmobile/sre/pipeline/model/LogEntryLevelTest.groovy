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

class LogEntryLevelTest extends GroovyTestCase {
  void testParseLevel() {
    for (input in ["debug", "DEBUG", "Debug"]) {
      assertEquals(LogEntryLevel.DEBUG, LogEntryLevel.parseLevel(input))
    }

    for (input in ["info", "INFO", "Info"]) {
      assertEquals(LogEntryLevel.INFO, LogEntryLevel.parseLevel(input))
    }

    for (input in ["warn", "WARN", "Warn"]) {
      assertEquals(LogEntryLevel.WARN, LogEntryLevel.parseLevel(input))
    }

    for (input in ["error", "ERROR", "Error"]) {
      assertEquals(LogEntryLevel.ERROR, LogEntryLevel.parseLevel(input))
    }
  }

  void testParseBadInputDefaultsToError() {
    assertEquals(LogEntryLevel.WARN, LogEntryLevel.parseLevel(null))
    assertEquals(LogEntryLevel.WARN, LogEntryLevel.parseLevel(""))
    assertEquals(LogEntryLevel.WARN, LogEntryLevel.parseLevel("hello"))
  }
}
