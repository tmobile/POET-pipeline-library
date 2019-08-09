# Poet Pipeline

![pipeline logo](https://raw.githubusercontent.com/wiki/tmobile/POET-pipeline-library/images/POET_logo_final-480.png?token=AAAEJEG3KEN4GOTRXBBE2XS5K3KZU)

The Poet Pipeline brings modern, container based CI/CD to Jenkins.

## Example Pipeline Configuration

```
# pipeline.yml
pipeline:
  appOwner: POET
  appName: sre-pipeline

  appVersion:
    master: 1.0.0

  steps:
    - name: test pipeline
      image: gradle:5.3-jre8-alpine
      commands:
        - gradle clean test jacocoTestReport
```

## License

The POET Pipeline is released under the [Apache 2.0 License](https://github.com/tmobile/POET-pipeline-library/blob/master/LICENSE)
