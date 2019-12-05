#!/usr/bin/env bash
#
# _retrieveConfigs.sh - Fetch configs from gitserver
#

# retrieveConfigs() - Fetch configs from a preconfigured git repo
function retrieveConfigs() {
  CONFIG_SKEL_DIR="${CONFIG_SKEL_DIR:-${LIB_DIR}/skel}"

  pushd "${CONFIG_SKEL_DIR}" >/dev/null 2>&1 || die "Unable to change to ${CONFIG_SKEL_DIR}"

  # Instantiate all templates
  while read -r FILE; do
    DEST="${FILE/./""}"
    DEST="${DEST%.j2}"
    mkdir -p "$(dirname ${DEST})"
    jinjaTemplate "${FILE}" "${DEST}" "${CONFIG_YAML}" || die "Unable to instantiate template '${FILE}'"
    copyPermissions "${FILE}" "${DEST}"
  done < <(find . -type f -name \*.j2 -print)

  # Return to original directory
  popd >/dev/null

}

retrieveConfigs || die "Unable to retrieve configs"
