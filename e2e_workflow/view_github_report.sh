#!/bin/bash

# View Playwright report stored in GitHub
#   ./view_github_report.sh [github-repo-owner] [github-run-id]
#  

# Abort script on errors and unbound variables
# https://bertvv.github.io/cheat-sheets/Bash.html
set -o errexit   # abort on nonzero exitstatus
set -o nounset   # abort on unbound variable
set -o pipefail  # don't hide errors within pipes

# Set variables
SCRIPT_PATH="$(cd "$(dirname "${0}")"; echo $(pwd))"
REPO_PATH="$(dirname ${SCRIPT_PATH})"
REPO_NAME="$(basename ${REPO_PATH})"
BLOB_DIR="${REPO_PATH}/blob-report/"
REPORT_DIR="${REPO_PATH}/playwright-report/"

# Get parameters from user
if [ "$#" -lt 2 ]; then
  echo "Usage: $0 [github-repo-owner] [github-run-id]"
  echo "* github-repo-owner: GitHub repo owner"
  echo "* github-run-id: GitHub Run ID of report to view"
  exit
fi
REPO_OWNER="${1}"
RUN_ID="${2}"

# Delete previous report files
for file in $(find "${BLOB_DIR}" -type f) $(find "${REPORT_DIR}" -type f); do 
  [ -f "${file}" ] && echo "delete: ${file}" && rm "${file}"
done

# Get workflow artifact id and name
ARTIFACT=$(gh api \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "/repos/${REPO_OWNER}/${REPO_NAME}/actions/runs/${RUN_ID}/artifacts" \
  | jq '.artifacts[] | select(.name | contains("html-report")) | {id, name}')
ARTIFACT_ID=$(echo "${ARTIFACT}" | jq '.id')
ARTIFACT_NAME=$(echo "${ARTIFACT}" | jq -r '.name')

# Download workflow artifact
ARTIFACT_ENDPOINT="/repos/${REPO_OWNER}/${REPO_NAME}/actions/artifacts/${ARTIFACT_ID}/zip"
ARTIFACT_ZIP="${BLOB_DIR}/${ARTIFACT_NAME}.zip"
echo "download: ${ARTIFACT_ENDPOINT} to ${ARTIFACT_ZIP}"
gh api \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "${ARTIFACT_ENDPOINT}" > "${ARTIFACT_ZIP}"

# Unzip file
unzip "${ARTIFACT_ZIP}" -d "${REPORT_DIR}"

# Show report
yarn e2e:report