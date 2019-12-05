#!/usr/bin/env bash
#
# startup.sh - Wrapper Script that will perform all activities to start Jenkins
#

# Handle convenience variables and defaults

#export PIPELINE_ENGINE_NAME=$([[ ! -z "${PIPELINE_ENGINE_NEW}" ]] && echo "${PIPELINE_ENGINE_NEW}" || echo "${PIPELINE_ENGINE_NAME}")
PIPELINE_ENGINE_NAME="${PIPELINE_ENGINE_NEW:-${PIPELINE_ENGINE_NAME}}"
export PIPELINE_ENGINE_NAME="${PIPELINE_ENGINE_NAME}"
export LIB_DIR="${LIB_DIR:-${STARTUP_DIR}/lib}"

git config --global http.sslVerify "false"

# Load utility functions
. "${LIB_DIR}/utility.sh"

log.info "PIPELINE_ENGINE: ${PIPELINE_ENGINE_NAME}"

# Set this flag to ensure setenv.sh is executed one and only once
execSetEnv=true

# Constants
JINJA2_BINARY=jinja2
CONFIG_YAML="${STARTUP_DIR}/config.yml"

# Create Jenkins Config as Code Directory Structure to hold JCasC yml files
if ([[ ! -z "${CASC_JENKINS_CONFIG}" ]]); then
  [[ ! -d "${CASC_JENKINS_CONFIG}" ]] && mkdir -p "${CASC_JENKINS_CONFIG}"
  . "${USR_LOCAL_BIN_DIR}/casc-jenkins-config-repo.sh"
fi

# configureModules() - Load modules
function configureModules() {
  while read -r MODULE; do
    log.info "Loading Module: ${MODULE}"
    if ([[ -f "${STARTUP_DIR}/setenv.sh" ]] && [[ "${execSetEnv}" = true ]]); then
      . "${STARTUP_DIR}/setenv.sh"
      execSetEnv=false
    fi
    loadModule "${MODULE}" || die "Unable to load '${MODULE##*/}'"
  done < <(find "${LIB_DIR}/modules" -maxdepth 1 -type f -name '[0-9]*_*.sh' | sort -n)
}

# main()
enableStrictErrorChecks
enableOptionalTracing
configureModules

# Delete misc directory
rm -rf ${STARTUP_DIR}/lib/misc

# Delete Existing Plugins
rm -rf ${JENKINS_HOME}/plugins/*

# Delete Existing Nodes
rm -rf ${JENKINS_HOME}/nodes/*

# Delete JCasC folder from lib folder
[[ ! -d "${LIB_DIR}/skel${CASC_JENKINS_CONFIG}" ]] && (rm -rf ${LIB_DIR}/skel/var)

if ([[ ! -z "${BACKUP_PIPELINE_ENGINE}" ]] && [[ "${BACKUP_PIPELINE_ENGINE}" = "true" ]]); then
  export GIT_PASSWORD=${PIPELINE_ENGINE_BACKUP_REPO_PASSWORD}
  export GIT_ASKPASS=${USR_LOCAL_BIN_DIR}/git-askpass-helper.sh

  # Provision Pipeline Engine Backup Repo
  . "${USR_LOCAL_BIN_DIR}/pipeline-engine-backup-repo-provisioning.sh"

  # Init Pipeline Engine git Repository if not already set
  . "${USR_LOCAL_BIN_DIR}/pipeline-engine-auto-init-restore.sh"

  # Restore Pipeline Engine if RESTORE_PIPELINE_ENGINE is set to true
  . "${USR_LOCAL_BIN_DIR}/pipeline-engine-conditional-restore.sh"
fi

# Start Jenkins
log.info "Starting Pipeline Engine - ${PIPELINE_ENGINE_NAME}"
exec ${USR_LOCAL_BIN_DIR}/jenkins.sh

