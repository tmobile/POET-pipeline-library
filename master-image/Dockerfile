# Alpine OS 3.10.2 based AdoptOpenJDK Image
FROM adoptopenjdk/openjdk8:x86_64-alpine-jdk8u222-b10-slim

# Jenkins Configuration
ARG TMP_DIR="/tmp"
ENV USR_BIN_DIR="/usr/bin"
ENV USR_LOCAL_BIN_DIR="/usr/local/bin"
ENV STARTUP_DIR="/startup"
ARG JENKINS_RELEASE=war
ARG JENKINS_VERSION=2.190.1
ARG JENKINS_URL=https://repo.jenkins-ci.org/public/org/jenkins-ci/main/jenkins-war/${JENKINS_VERSION}/jenkins-war-${JENKINS_VERSION}.war
# Update with Jenkins SHA whenever there is a change in Jenkins Version
ARG JENKINS_SHA=46fb1d25d9423fc66aadd648dc74b9772863a7fbbd89bfc14c873cd0c3436f05

# Jenkins Environment Variables
ENV JENKINS_CLI_URL=http://repo.jenkins-ci.org/public/org/jenkins-ci/main/cli/${JENKINS_VERSION}/cli-${JENKINS_VERSION}-jar-with-dependencies.jar
# Update with Jenkins Cli SHA whenever there is a change in Jenkins Cli Version
ENV JENKINS_CLI_SHA=7bb387c327f53fe66e88d04e5b4687ee036868cb6f91b265834c1e68cdebe886
ENV JENKINS_USER "${USER_NAME}"
ENV JENKINS_HOME "/var/jenkins_home"
ENV JENKINS_APP_HOME="/usr/share/jenkins"
ENV JENKINS_LOG_HOME="/var/log/jenkins"
ENV JENKINS_SLAVE_AGENT_PORT "50000"
ENV JENKINS_UC http://updates.jenkins-ci.org/update-center.json
ENV JENKINS_UC_EXPERIMENTAL https://updates.jenkins.io/experimental
ENV JENKINS_INCREMENTALS_REPO_MIRROR=https://repo.jenkins-ci.org/incrementals
ENV COPY_REFERENCE_FILE_LOG ${JENKINS_HOME}/copy_reference_file.log
ARG JRE_HOME=${JAVA_HOME}

# Jenkins User Account
ENV USER_NAME=jsremaster
ENV USER_GROUP=jsremaster
ARG USER_UID=38042
ARG USER_GID=25684
ARG USER_DIR=${JENKINS_HOME}

# Jenkins Ports
ARG HTTP_PORT=8080

# Agent Port Range for NodePort
ARG AGENT_NODE_PORT_MIN=30106
ARG AGENT_NODE_PORT_MAX=30120

# Define Metadata about Jenkins Environment Configuration
LABEL com.application.jenkins.url=${JENKINS_URL} \
      com.application.jenkinscli.url=${JENKINS_CLI_URL} \
      com.application.jenkins.version=${JENKINS_VERSION} \
      com.application.jenkinscli.version=${JENKINS_VERSION} \
      com.application.jenkins.hash=${JENKINS_SHA} \
      com.application.jenkinscli.hash=${JENKINS_CLI_SHA} \
      com.application.jenkins.userid=${USER_NAME} \
      com.application.jenkins.groupid=${GROUP_NAME} \
      com.application.jenkins.http.port=${HTTP_PORT} \
      com.application.jenkins.slaveport=${JENKINS_SLAVE_AGENT_PORT} \
      com.application.jenkins.uc=${JENKINS_UC} \
      com.application.jenkins.plugin.download.url=${JENKINS_UC}

#############################
# Things to be done as root
#############################

# Copy jinja templates and scripts
COPY lib healthcheck startup.sh ${STARTUP_DIR}/lib/

RUN set -ex && \
    # Create Application Folder Structure
    mkdir -p \
      ${STARTUP_DIR} \
      ${JENKINS_HOME} \
      ${JENKINS_APP_HOME}/ref/init.groovy.d \
      ${JENKINS_LOG_HOME} && \
    # Move healthcheck to root directory in container
    mv ${STARTUP_DIR}/lib/healthcheck /healthcheck && \
    # Move startup.sh to root directory in container
    mv ${STARTUP_DIR}/lib/startup.sh /startup.sh && \
    # Move plugins.txt to Jenkins ref directory
    mv ${STARTUP_DIR}/lib/misc/plugins.txt ${JENKINS_APP_HOME}/ref/plugins.txt && \
    # Move scripts from /usr/local/bin to destination in container
    cp ${STARTUP_DIR}/lib/skel/usr/local/bin/* /usr/local/bin && \
    rm -rf ${STARTUP_DIR}/lib/skel/usr && \
    # Install Software Packages
    apk add --no-cache \
      "bash" \
      "ca-certificates" \
      "coreutils" \
      "curl" \
      "git" \
      "jq" \
      "openssh-client" \
      "openssl" \
      "procps" \
      "python3" \
      "tini" \
      "ttf-dejavu" \
      "tzdata" \
      "unzip" && \
    # Install Jinja2 Client
    export PIP_DISABLE_PIP_VERSION_CHECK=1 && \
    if [ ! -e /usr/bin/pip ]; then ln -s pip3 /usr/bin/pip ; fi && \
    if [[ ! -e /usr/bin/python ]]; then ln -sf /usr/bin/python3 /usr/bin/python; fi && \
    python -m ensurepip && \
    rm -r /usr/lib/python*/ensurepip && \
    pip install --upgrade pip setuptools && \
    pip install \
      jinja2-cli==0.6.0 \
      shyaml \
      pyyaml==3.12 && \
    # Create Application User and Group
    addgroup -S -g ${USER_GID} ${USER_GROUP} && \
    adduser -S -h ${JENKINS_HOME} -u ${USER_UID} -G ${USER_GROUP} -s /bin/bash -D ${USER_NAME} && \
    # Download ALPN jar
    curl -fsSL https://repo.maven.apache.org/maven2/org/mortbay/jetty/alpn/alpn-boot/8.1.13.v20181017/alpn-boot-8.1.13.v20181017.jar -o ${STARTUP_DIR}/alpn-boot-8.1.13.v20181017.jar && \
    # Download Jenkins War and validate against checksum
    curl -fsSL "${JENKINS_URL}" -o "${JENKINS_APP_HOME}/jenkins.war" && \
    echo "${JENKINS_SHA}  ${JENKINS_APP_HOME}/jenkins.war" | sha256sum -c - && \
    # Install Jenkins cli
    curl -fsSL "${JENKINS_CLI_URL}" -o "${JENKINS_APP_HOME}/cli.jar" && \
    echo "${JENKINS_CLI_SHA}  ${JENKINS_APP_HOME}/cli.jar" | sha256sum -c - && \
    touch /usr/bin/jenkins-cli && \
    touch /usr/bin/cli && \
    # Install Required Jenkins Plugins
    ${USR_LOCAL_BIN_DIR}/install-plugins.sh < ${JENKINS_APP_HOME}/ref/plugins.txt && \
    # Cleanup Temporarily Downloaded Files and unnecessary packages
    apk del --purge \
      openssl && \
    rm -rf \ 
      "${TMP_DIR}/*" \
      /root/.cache \
      /var/cache/apk/* && \
    # Set Permissions on Application Specific Folders
    chown -R ${USER_NAME}:${USER_GROUP} \
      ${STARTUP_DIR} \
      ${JENKINS_HOME} \
      ${USR_LOCAL_BIN_DIR} \
      ${JENKINS_APP_HOME}/ref \
      ${JENKINS_LOG_HOME} \
      ${USR_BIN_DIR}/jenkins-cli \
      ${USR_BIN_DIR}/cli && \
    chmod -R 770 \
      ${JENKINS_HOME} && \
    chmod ug+x \
      ${JENKINS_APP_HOME}/cli.jar \
      ${USR_LOCAL_BIN_DIR}/ \
      ${USR_BIN_DIR}/jenkins-cli \
      ${USR_BIN_DIR}/cli && \
    # Unlink vi
    unlink /usr/bin/vi

# Jenkins home directory is a volume, so configuration and build history can be persisted and survive image upgrades
VOLUME "${JENKINS_HOME}"

# Set Working Directory
WORKDIR "${JENKINS_HOME}"

###################################################
# Change Runtime user to Jenkins Application User
###################################################
USER "${USER_NAME}"

# Expose HTTP Port to access Web Interface
EXPOSE "${HTTP_PORT}"

# Expose Agent Port to be used by Worker Agents
EXPOSE "${JENKINS_SLAVE_AGENT_PORT}"
EXPOSE ${AGENT_NODE_PORT_MIN}-${AGENT_NODE_PORT_MAX}

# Let Docker run healthchecks to determine if application is responding
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=3 CMD /healthcheck

# Tini will propagate signals to all child processes to help with shutting down cleanly
ENTRYPOINT ["/sbin/tini", "--"]

# Start up script gets server config and starts the server
CMD "/startup.sh"
