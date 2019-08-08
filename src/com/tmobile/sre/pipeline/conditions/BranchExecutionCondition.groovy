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

import com.tmobile.sre.pipeline.environment.PipelineEnvironment
import com.tmobile.sre.pipeline.model.PipelineState
import com.tmobile.sre.pipeline.model.PipelineStep
import com.tmobile.sre.pipeline.util.PatternMatchUtils

/**
 * Implements branch condition tests for conditional execution of pipeline steps.
 *
 * A branch condition can take:
 *   - a single value or pattern
 *   - a list of values or patterns
 *   - a map with elements `include` and `exclude`, each of which can take a single value or list.
 *
 * Examples:
 *
 * Execute a step if the branch is on master or release:
 *
 * branch: [master, release]
 *
 * Execute a step if the branch starts with feature/*
 *
 * branch: feature/*
 *
 * Execute a step using include and exclude logic:
 *
 * branch:
 *   include: [ master, feature/* ]
 *   exclude: [ feature/something, feature/old_thing* ]
 *
 * Patterns:
 * We implement simple pattern matching, and can match against the following styles:
 * xxx*, *xxx, *xxx*, and xxx*yyy, with an arbitrary number of pattern parts, as well as direct matches.
 *
 * @see PatternMatchUtils
 */
class BranchExecutionCondition implements ExecutionCondition {
  boolean shouldRun(PipelineState state, PipelineEnvironment environment, PipelineStep step) {
    if (step.when.branch == null) {
      return true
    }

    StringMatchSpec spec = StringMatchSpec.fromSpecString(step.when.branch);
    spec.matches(state.branch)
  }
}
