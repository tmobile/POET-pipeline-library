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
package com.tmobile.sre.pipeline.reader


import com.tmobile.sre.pipeline.model.PipelineDefinition
import com.tmobile.sre.pipeline.model.PipelineResourceDirectory
import com.tmobile.sre.pipeline.model.PipelineStep
import com.tmobile.sre.pipeline.model.Repository
import com.tmobile.sre.pipeline.model.template.ConfigTemplate
import com.tmobile.sre.pipeline.model.template.StepTemplate

/**
 * Provides a default repository and loads standard pre/post steps.
 *
 * An internal "default" template repository can be provided as a JSON string
 * using Jenkins Global Environment variables, e.g. :
 *
 *   {"uri":"http://repo/team/pipeline-templates.git","label":"master","credentials":{ "id":"scmcreds" }}
 *
 * This repo will automatically be used if a pipeline reference '@templates' without an explicit definition.
 *
 * Additionally, if step templates internal/pre.yml, internal/post.yml are defined in the repository, they will
 * automatically be run before and after the user pipeline.
 */
class PipelineDefaults implements Serializable {
  final Repository defaultRepository
  final Repository internalRepository

  static final List<PipelineStep> DEFAULT_PRE_STEPS = [
      new PipelineStep(include: "internal/pre.yml@_")
  ]

  static final List<PipelineStep> DEFAULT_POST_STEPS = [
      new PipelineStep(include: "internal/post.yml@_")
  ]

  static PipelineDefaults loadDefaults(String defaultRepositoryTemplate) {
    YamlReader yml = new YamlReader()

    if (defaultRepositoryTemplate != null && (! defaultRepositoryTemplate.isEmpty())) {
      try {
        Repository defaultRepo = yml.parse(Repository.class, defaultRepositoryTemplate)
        defaultRepo.name = "templates"

        Repository internalRepo = yml.parse(Repository.class, defaultRepositoryTemplate)
        internalRepo.name = "_"

        return new PipelineDefaults(defaultRepo, internalRepo)
      } catch (final Exception e) {
        // fall through and don't provide defaults
      }
    }

    return new PipelineDefaults()
  }


  PipelineDefaults(Repository defaultRepository, Repository internalRepository) {
    this.defaultRepository = defaultRepository
    this.internalRepository = internalRepository
  }

  PipelineDefaults() {
    this(null, null)
  }

  PipelineDefinition withDefaults(PipelineDefinition config) {
    if (defaultRepository) {
      def referencesDefaultRepo = (configReferencesRepository(defaultRepository, config.pipeline.include) || stepsReferencesRepository(defaultRepository, config.pipeline.steps))

      if (referencesDefaultRepo && (!hasRepositoryDefined(defaultRepository, config))) {
        config.resources.repositories.add(defaultRepository)
      }
    }

    if (internalRepository) {
      config.resources.repositories.add(internalRepository)

      config.pipeline.pre.addAll(DEFAULT_PRE_STEPS)
      config.pipeline.post.addAll(DEFAULT_POST_STEPS)
    }

    config
  }

  StepTemplate withStepDefaults(StepTemplate config) {
    if (defaultRepository) {
      def referencesDefaultRepo = stepsReferencesRepository(defaultRepository, config.steps)

      if (referencesDefaultRepo && (!hasRepositoryDefined(defaultRepository, config))) {
        config.resources.repositories.add(defaultRepository)
      }
    }

    config
  }

  ConfigTemplate withConfigDefault(ConfigTemplate config) {
    if (defaultRepository) {
      def referencesDefaultRepo = configReferencesRepository(defaultRepository, config.pipeline.include)

      if (referencesDefaultRepo && (!hasRepositoryDefined(defaultRepository, config))) {
        config.resources.repositories.addAll(defaultRepository)
      }
    }

    config
  }

  private def hasRepositoryDefined(Repository repository, PipelineResourceDirectory config) {
    for (int i=0; i< config.resources.repositories.size(); i++) {
      if (config.resources.repositories[i].name.equals(repository.name)) {
        return true
      }
    }
    return false
  }

  private def stepsReferencesRepository(Repository repository, List<PipelineStep> steps) {
    for (int i = 0; i < steps.size(); i++) {
      def s = steps[i]
      if (s.include != null && s.include.endsWith("@" + repository.name)) {
        return true;
      }
    }
    return false
  }

  private def configReferencesRepository(Repository repository, List<String> configIncludes) {
    for (int i=0; i< configIncludes.size(); i++) {
      def include = configIncludes[i]
      if (include.endsWith("@" + repository.name)) {
        return true
      }
    }

    return false
  }
}
