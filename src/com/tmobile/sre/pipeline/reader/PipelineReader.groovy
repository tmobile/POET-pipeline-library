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

import com.tmobile.sre.pipeline.PipelineLogger

import com.tmobile.sre.pipeline.model.*
import com.tmobile.sre.pipeline.model.template.ConfigTemplate
import com.tmobile.sre.pipeline.model.template.ConfigTemplatePipeline
import com.tmobile.sre.pipeline.model.template.StepTemplate
import com.tmobile.sre.pipeline.reader.templates.ParentLocalFileReader
import com.tmobile.sre.pipeline.reader.templates.TemplateManagerFactory
import com.tmobile.sre.pipeline.reader.validator.EmptyPipelineValidator
import com.tmobile.sre.pipeline.reader.validator.PipelineValidator


/**
 * Read a Pipeline Definition File
 */
class PipelineReader implements Serializable {
  static final int MAX_INCLUDE_DEPTH = 5

  PipelineFileReader fileReader;
  PipelineLogger logger;
  TemplateManagerFactory templateManagerFactory;
  PipelineValidator validator
  PipelineDefaults defaults

  PipelineReader(PipelineFileReader fileReader, TemplateManagerFactory templateManagerFactory,
                 PipelineLogger logger,
                 PipelineValidator validator = new EmptyPipelineValidator(),
                 PipelineDefaults defaults = new PipelineDefaults()
  ) {
    this.fileReader = fileReader
    this.templateManagerFactory = templateManagerFactory
    this.logger = logger
    this.validator = validator
    this.defaults = defaults
  }

  PipelineDefinition read(String path) {
    def yml = new YamlReader()
    def pipelineFile = fileReader.read(path)
    validator.validatePipeline(pipelineFile)
    def pd = defaults.withDefaults(yml.parse(PipelineDefinition.class, pipelineFile.text))

    def templateReader = templateManagerFactory.create(fileReader, pd)

    // global config includes
    mergeConfig(pd, readConfig(0, yml, validator, templateReader, pd.pipeline.include))
    pd.pipeline.steps = readSteps(yml, validator, templateReader, pd.pipeline.steps)

    pd.pipeline.pre = readPrePostSteps("pre", yml, validator.create(), templateReader, pd.pipeline.pre)
    pd.pipeline.post = readPrePostSteps("post", yml, validator.create(), templateReader, pd.pipeline.post)

    pd
  }

  private List<PipelineStep> readPrePostSteps(String desc, YamlReader yml, PipelineValidator validator, PipelineFileReader templateReader, List<PipelineStep> includedSteps) {
    try {
      def steps = readSteps(yml, validator, templateReader, includedSteps)
      if (validator.result.hasErrors()) {
        logger.error(["Error reading ${desc} steps", validator.result.errors])
        return []
      }
      return steps
    } catch(final Exception e) {
      logger.error(["Error reading ${desc} steps", e.toString()])
      return []
    }
  }

  private List<PipelineStep> readSteps(YamlReader yml, PipelineValidator validator, PipelineFileReader templateReader, List<PipelineStep> includedSteps) {
    final List<PipelineStep> steps = new ArrayList<>()

    for (def i=0; i< includedSteps.size(); i++) {
      def step = includedSteps[i]
      if (step.include != null) {
        List<PipelineStep> included = readStepsFromInclude(0, yml, validator, templateReader, step)
        steps.addAll(included)
      } else {
        steps.add(step)
      }
    }

    return steps
  }


  private List<PipelineStep> readStepsFromInclude(int depth, YamlReader yml, PipelineValidator validator, PipelineFileReader templateReader, PipelineStep parent) {
    if (depth > MAX_INCLUDE_DEPTH) {
      throw new MaxIncludeDepthException()
    }

    List<PipelineStep> steps = new ArrayList<>()

    def parentFile = templateReader.read(parent.include)
    validator.validateStepTemplate(parentFile)
    def templateSteps = defaults.withStepDefaults(yml.parse(StepTemplate.class, parentFile.text))
    List<PipelineStep> read = merge(parent, templateSteps.steps)

    for (def i=0; i< read.size(); i++) {
      final PipelineStep step = read[i]
      if (step.include != null) {
        def localReader = templateManagerFactory.create(fileReader, templateSteps)
        def nextReader = new ParentLocalFileReader(localReader, templateReader, parentFile.path)

        List<PipelineStep> included = readStepsFromInclude(depth + 1, yml, validator, nextReader, step)
        steps.addAll(included)
      } else {
        steps.add(step)
      }
    }

    return steps
  }

  private List<ConfigTemplate> readConfig(int depth, YamlReader yml, PipelineValidator validator, PipelineFileReader templateReader, List<String> includedConfigs) {
    if (depth > MAX_INCLUDE_DEPTH) {
      throw new MaxIncludeDepthException()
    }

    List<ConfigTemplate> templates = new ArrayList<>()

    for (int i=0; i< includedConfigs.size(); i++) {
      def location = includedConfigs[i]

      def tFile = templateReader.read(location)
      validator.validateConfig(tFile)
      def tConfig = defaults.withConfigDefault(yml.parse(ConfigTemplate.class, tFile.text))

      if (! tConfig.pipeline.include.isEmpty()) {
        def localReader = templateManagerFactory.create(fileReader, tConfig)
        def nextReader = new ParentLocalFileReader(localReader, templateReader, tFile.path)
        List<ConfigTemplate> included = readConfig(depth + 1, yml, validator, nextReader, tConfig.pipeline.include)

        templates.addAll(included)
      }

      templates.add(tConfig)
    }

    return templates;
  }

  def mergeConfig(PipelineDefinition pd, List<ConfigTemplate> configs) {
    LinkedHashMap<String, String> config = new LinkedHashMap<>()
    LinkedHashMap<String, String> versions = new LinkedHashMap<>()

    Map<String, String> configFields = new LinkedHashMap<>()

    for (int i=0; i< configs.size(); i++) {
      ConfigTemplatePipeline ctp = configs[i].pipeline

      config.putAll(ctp.environment)
      versions.putAll(ctp.appVersion)

      // collect any overrides, last one will win
      configFields.putAll(ctp.appInfo())
    }

    // host file should override any included files
    config.putAll(pd.pipeline.environment)
    versions.putAll(pd.pipeline.appVersion)

    pd.pipeline.environment = config
    pd.pipeline.appVersion = versions

    // if host file doesn't have a config property defined, use the last included config
    List<String> configNames = new ArrayList<>(configFields.keySet())
    for (def i=0; i< configNames.size(); i++) {
      def k = configNames[i]
      if (pd.pipeline.getProperty(k) == null) {
        pd.pipeline.setProperty(k, configFields[k])
      }
    }
  }

  List<PipelineStep> merge(PipelineStep step,  List<PipelineStep> added) {
    // copy over explicit step config to included steps
    for(int i=0; i< added.size(); i++) {
      added[i].environment.putAll(step.environment)
      added[i].secrets = mergeSecrets(added[i], step.secrets)
      added[i].when.putAll(step.when)
      added[i].timeoutInMinutes = step.timeoutInMinutes ?: added[i].timeoutInMinutes

      // note we can't use the short form ?: since this is a Boolean value
      added[i].continueOnError = (step.continueOnError != null) ? step.continueOnError : added[i].continueOnError
    }

    return added;
  }

  def mergeSecrets(PipelineStep step, List<Secret> secrets) {
    List<Secret> ret = new ArrayList<>()

    for (int i=0; i< step.secrets.size(); i++) {
      if (!hasEquivalentSecret(step.secrets[i], secrets)) {
        ret.add(step.secrets[i])
      }
    }

    ret.addAll(secrets)
    return ret
  }

  private def hasEquivalentSecret(Secret secret, List<Secret> secrets) {
    for (int i=0; i< secrets.size(); i++) {
      for (int j=0; j< secret.target.size(); j++) {
        if (secrets[i].target.contains(secret.target[j])) {
          return true
        }
      }
    }

    return false
  }

  static class MaxIncludeDepthException extends RuntimeException {

  }
}
