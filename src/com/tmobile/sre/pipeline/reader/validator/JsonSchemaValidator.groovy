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
package com.tmobile.sre.pipeline.reader.validator

import com.tmobile.sre.pipeline.reader.PipelineFile
import com.tmobile.sre.pipeline.reader.YamlReader
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject

@Grab('org.everit.json:org.everit.json.schema:1.5.1')
/**
 * All the NonCPS code is isolated here.
 *
 * Originally, this class did more pre-calculation in its constructor, but
 * that's not possible while running in Jenkins (see: https://issues.jenkins-ci.org/browse/JENKINS-26313)

 */
class JsonSchemaValidator implements Serializable {
  String sharedDefinitions

  JsonSchemaValidator(String sharedDefinitions) {
    this.sharedDefinitions = sharedDefinitions
  }

  @com.cloudbees.groovy.cps.NonCPS
  List<String> validate(String schema, PipelineFile file) {
    try {
      SchemaClient client = new StaticClient(toJson(sharedDefinitions))
      loadSchema(client, schema).validate(toJson(file.text))
      return Collections.emptyList()
    } catch (ValidationException ve) {
      return ve.getAllMessages()
    }

    return Collections.emptyList()
  }

  @com.cloudbees.groovy.cps.NonCPS
  private Schema loadSchema(SchemaClient client, String schemaText) {
    SchemaLoader loader = SchemaLoader.builder()
        .resolutionScope("classpath://schema/")
        .httpClient(client)
        .schemaJson(toJson(schemaText))
        .build();

    
    return loader.load().build()
  }

  @com.cloudbees.groovy.cps.NonCPS
  private def toJson(String yamlText) {
    Map read = new YamlReader().parse(Map.class, yamlText)
    // I know this is crazy, but otherwise getting weird type issues
    // running on jenkins
    return new JSONObject(new JSONObject(read).toString())
  }

  /**
   * Returns a static schema for any external reference.
   *
   * This was meant to be more general-purpose, using the URL to determine
   * which file to load.  NonCPS methods can't call CPS methods, so that
   * became very difficult to manage.  For now we just have one shared reference,
   * so a static return is all we really need.
   */
  static class StaticClient implements SchemaClient {
    byte[] resolvedSchema

    StaticClient(JSONObject resolvedSchema) {
      this.resolvedSchema = resolvedSchema.toString().getBytes()
    }

    @com.cloudbees.groovy.cps.NonCPS
    InputStream get(String url) {
      return new ByteArrayInputStream(resolvedSchema)
    }
  }
}
