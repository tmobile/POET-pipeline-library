#!/usr/bin/env bash
# shellcheck disable=SC2155
#
# utility.sh - Utility functions for SCCS startup scripting
#

# log(level,msgs) - Log messages to console at level
function log() {
  LEVEL="${1}" && shift
  STACK=($(printf '%s\n' "${FUNCNAME[@]}" | fgrep -v -e loadModule -e source))
  for line in "$@"; do
    # shellcheck disable=SC2183
    printf "%(%Y-%m-%d %H:%M:%S)T.??? [bash] %-5s %s - %s\n" -1 "${LEVEL}" "${STACK[2]:0:20}" "${line}"
  done
}

# log.<LEVEL>(msgs) - Log msgs at <LEVEL> level
function log.debug() { [ ${DEBUG:-} ] && log "DEBUG" "$@"; }
function log.error() { log "ERROR" "$@"; }
function log.fatal() { log "FATAL" "$@"; }
function log.info()  { log "INFO" "$@"; }
function log.warn()  { log "WARN" "$@"; }

# die(optional: message, optional: exit code) - Display a message and exit
function die() {
  if [ "${1:-}" ] && [[ ! "${1}" =~ ^[0-9]+ ]]; then
    log "FATAL" "$@" >&2 && shift
  fi
  exit "${1:-255}"
}

# copyPermissions(src,dest) - Copy file permissions from src to dest
function copyPermissions() {
  chmod "$(stat -c '%a' ${1})" "${2}" || die "Failed to set permissions on ${2}"
}

# enableStrictErrorChecks() - Enable pedantic bash checking
function enableStrictErrorChecks() {
  # fail on unset variables
  set -u
  # fail if any command fails
  set -Ee
  # fail if any portion of a pipe fails
  set -o pipefail
}

# enableOptionalTracing() - turn shell tracing on/off as needed
function enableOptionalTracing() {
  [ "${1:-${TRACE:-}}" ] && set -x || set +x
}

# function jinjaTemplate(template,output,...)
function jinjaTemplate() {
  [ -f "${1:-}" ] || die "assertion failed: unable to read template '${1}'"
  [ "${2:-}" ] || die "assertion failed: output file not specified"
  log.info "Writing jinja template '${1}' to '${2}'"
  if [ -z "${3:-}" ]; then
    env | jinja2 "${1}" > "${2}"
  else
    jinja2 "${1}" "${3}" > "${2}"
  fi
}

# loadModule(module) - Load a shell module
function loadModule() {
  if [ -f "${1}" ] && [ -r "${1}" ]; then
    # shellcheck disable=SC1090
    . "${1}"
  else
    log.warn "Not loading ${1}"
    return 1
  fi
}

# Only notify if we're not testing
log.info "Loaded utility functions"
