#!/bin/bash

# View Playwright report stored in S3
#   ./view_s3_report.sh [aws-sso-profile-name] [github-run-id] [optional-run-attempt-number]
#

# Abort script on errors and unbound variables
# https://bertvv.github.io/cheat-sheets/Bash.html
set -o errexit   # abort on nonzero exitstatus
set -o nounset   # abort on unbound variable
set -o pipefail  # don't hide errors within pipes

# Set variables
BUCKET_NAME="e2e-reports-bucket-bucket-1p1qz6p48t4uy"
SCRIPT_PATH="$(cd "$(dirname "${0}")"; echo $(pwd))"
REPO_PATH="$(dirname ${SCRIPT_PATH})"
REPO_NAME="$(basename ${REPO_PATH})"
BLOB_DIR="${REPO_PATH}/blob-report/"

# Get parameters from user
if [ "$#" -lt 2 ]; then
  echo "Usage: $0 [aws-sso-profile-name] [github-run-id] [optional-run-attempt-number]"
  echo "* aws-sso-profile-name: AWS SSO profile name, e.g. Developer-{number}"
  echo "* github-run-id: GitHub Run ID of report to view"
  echo "* optional-run-attempt-number: (optional) Run attempt number. Defaults to 1."
  exit
fi
PROFILE="${1}"
RUN_ID="${2}"
RUN_ATTEMPT="${3:-1}"

# Sync blob reports
aws s3 sync \
  s3://"${BUCKET_NAME}"/"${REPO_NAME}-${RUN_ID}-${RUN_ATTEMPT}"/ \
  "${BLOB_DIR}" \
  --profile "${PROFILE}" \
  --delete

# Merge blob reports, then serve as HTML report
yarn e2e:report:blob
