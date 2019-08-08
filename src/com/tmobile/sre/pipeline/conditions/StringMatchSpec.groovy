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
package com.tmobile.sre.pipeline.conditions

import com.tmobile.sre.pipeline.util.PatternMatchUtils

/**
 *
 * String matching with include/exclude lists and simple patterns.
 *
 * A condition can take:
 *   - a single value or pattern
 *   - a list of values or patterns
 *   - a map with elements `include` and `exclude`, each of which can take a single value or list.
 *
 * Patterns:
 * We implement simple pattern matching, and can match against the following styles:
 * xxx*, *xxx, *xxx*, and xxx*yyy, with an arbitrary number of pattern parts, as well as direct matches.
 *
 * @see com.tmobile.sre.pipeline.util.PatternMatchUtils
 * @see BranchExecutionCondition
 * @see SimpleEnvironmentMatchCondition
 */
class StringMatchSpec implements Serializable {
  final List<String> include = new ArrayList<>()
  final List<String> exclude = new ArrayList<>()

  StringMatchSpec(List<String> include, List<String> exclude) {
    this.include = include;
    this.exclude = exclude;
  }

  static StringMatchSpec fromSpecString(Serializable condition) {
    final List<String> include = new ArrayList<>()
    final List<String> exclude = new ArrayList<>()


    if (condition instanceof Map) {
      include.addAll(patterns((Serializable) condition.include))
      exclude.addAll(patterns((Serializable) condition.exclude))
    } else {
      include.addAll(patterns(condition))
    }

    StringMatchSpec spec = new StringMatchSpec(include, exclude)

    return spec
  }

  static List<String> patterns(m) {
    if (m instanceof String) {
      Collections.singletonList((String) m)
    } else if (m instanceof List) {
      new ArrayList<String>((List<String>) m);
    } else {
      Collections.emptyList()
    }
  }

  def matches(String condition) {
    if (include.isEmpty() || PatternMatchUtils.simpleMatch(include, condition)) {
      if (exclude.isEmpty() || (!PatternMatchUtils.simpleMatch(exclude, condition))) {
        return true
      }
    }

    return false
  }

  @Override
  String toString() {
    return String.format("StringMatchSpec[include=%s, exclude=%s]", include, exclude)
  }
}
