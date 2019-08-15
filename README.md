# Poet Pipeline

![pipeline logo](https://raw.githubusercontent.com/wiki/tmobile/POET-pipeline-library/images/POET_logo_final-480.png?token=AAAEJEG3KEN4GOTRXBBE2XS5K3KZU)

The Poet Pipeline brings modern, container based CI/CD to Jenkins.

- Entirely **container based**
    - Common step functionality can be packaged and shared by developers
- [Templates]() allow sharing and standardization of complex or replicated configurations across different projects and teams
- [Conditions]() allow optional behavior and workflows based on branch, job status, or environment variables
- Low reliance on plugins simplifies operations and maintenance


## Example Pipeline Configuration

```
# pipeline.yml
pipeline:
  appOwner: POET
  appName: poet-pipeline

  appVersion:
    master: 1.0.0

  steps:
    - name: test-pipeline
      image: gradle:5.3-jre8-alpine
      commands:
        - gradle clean test jacocoTestReport
```


## Installation and Getting Started

The POET pipeline is packaged as a [Jenkins Shared Library](https://jenkins.io/doc/book/pipeline/shared-libraries/).  See [Installation]() in our [wiki]().

Once the pipeline is installed, see [Getting Started]() in the [wiki]() to configure a project to use the pipeline.

## License

The POET Pipeline is released under the [Apache 2.0 License](https://github.com/tmobile/POET-pipeline-library/blob/master/LICENSE)