#!/usr/bin/env bash
#
# _preflight.sh - pre load configuration
#

# preflightConfigs() - Load configs
function preflightConfigs() {
  PREFLIGHT_SCRIPTS_DIR="${PREFLIGHT_SCRIPTS_DIR:-${STARTUP_DIR}/preflight}"

  pushd "${PREFLIGHT_SCRIPTS_DIR}" >/dev/null 2>&1 || die "Unable to change to ${PREFLIGHT_SCRIPTS_DIR}"

  # Execute Scripts
  while read -r FILE; do
    FILE="${FILE/.\//""}"; 
    [ -x ${FILE} ] && (log.info "Executing Script: ${FILE}"; . "${FILE}")
  done < <(find . -type f -name '[0-9]*_*.sh' -print | sort -n)

  # Return to original directory
  popd >/dev/null

}

preflightConfigs || die "Unable to load configs"
