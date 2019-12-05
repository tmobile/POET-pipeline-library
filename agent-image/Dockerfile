# Docker In Docker Base Image from Dockerhub
FROM docker:19.03.2-dind

# Jenkins User Account
ARG USER_NAME=jenkins
ARG USER_GROUP=jenkins
ARG USER_UID=10000
ARG USER_GID=10000

# Jenkins Slave Remoting Jar Version
ARG VERSION=3.35

# Jenkins Environment Variables
ARG TMP_DIR="/tmp"
ENV LANG C.UTF-8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk/jre
ENV PATH $PATH:/usr/lib/jvm/java-1.8-openjdk/jre/bin:/usr/lib/jvm/java-1.8-openjdk/bin
ENV HOME /home/${USER_NAME}
ENV AGENT_WORKDIR=/home/${USER_NAME}/agent

#############################
# Things to be done as root
#############################
USER root

COPY daemon.json docker-entrypoint.sh /startup/

RUN set -x && \
    # Create Application Folder Structure
    mkdir -p \
      /startup \
      /etc/docker \
      ${HOME}/.jenkins \
      ${AGENT_WORKDIR} && \
    # Move Docker Entrypoint to /
    mv /startup/docker-entrypoint.sh /docker-entrypoint.sh && \
    # Move deamon.json
    mv /startup/daemon.json /etc/docker/ && \
    # Install Software Packages
    apk add --no-cache \
      "bash" \
      "ca-certificates" \
      "curl" \
      "git" \
      "net-tools" \
      "nmap" \
      "openjdk8-jre" \
      "openssh-client" \
      "sudo" \
      "tini" && \
    # Download Jenkins Slave Remoting jar
    curl --create-dirs -sSLo /usr/share/jenkins/slave.jar https://repo.jenkins-ci.org/public/org/jenkins-ci/main/remoting/${VERSION}/remoting-${VERSION}.jar && \
    # Create Application User and Group
    addgroup -g ${USER_GID} ${USER_GROUP} && \
    adduser -D -h ${HOME} -u ${USER_UID} -G ${USER_GROUP} ${USER_NAME} && \
    # Set Permissions on Application Specific Folders
    chown -R ${USER_NAME}:${USER_GROUP} \
      /startup \
      ${HOME} \
      ${AGENT_WORKDIR} && \
    chmod 644 \
      /usr/share/jenkins/slave.jar && \
    chmod 755 \
      /usr/share/jenkins \
      /docker-entrypoint.sh && \
    chmod -R 777 \
      /startup \
      ${HOME} \
      ${AGENT_WORKDIR} && \
    rm -rf \
      "${TMP_DIR}/*" \
      /root/.cache \
      /var/cache/apk/* && \
    # Unlink vi
    unlink /usr/bin/vi

USER ${USER_NAME}

VOLUME ${HOME}/.jenkins
VOLUME ${AGENT_WORKDIR}

WORKDIR ${HOME}

USER root

# Tini will propagate signals to all child processes to help with shutting down cleanly
ENTRYPOINT ["/sbin/tini", "--"]

# Start up script gets server config and starts the server
CMD "/docker-entrypoint.sh"
