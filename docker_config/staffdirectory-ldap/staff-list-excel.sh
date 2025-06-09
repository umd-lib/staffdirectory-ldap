#!/bin/bash

# Builds the "All Staff List.xslx" Excel spreadsheet and copies it to the
# "Staff Directory Information" directory on the Library Samba share.
#
# The following environment variables are expected by this script:
#
#    UPLOAD_EXCEL_FILENAME - The name of the uploaded file
#    DO_UPLOAD - "true" if the upload should be performed. Any other value prevents upload
#    UPLOAD_ID - Google Drive ID of file to be uploaded

if [ -z "$UPLOAD_EXCEL_FILENAME" ]; then
  echo "ERROR: Please provide a non-empty 'UPLOAD_EXCEL_FILENAME' environment variable"
  exit 1
fi

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
export CONFIG_PROPERTIES_FILE=$SCRIPT_DIR/config/config.properties
JSON_FILE="$SCRIPT_DIR/output/persons.json"
EXCEL_FILE="$SCRIPT_DIR/output/all-staff-list-new.xlsx"

echo === Building Excel spreadsheet with DO_UPLOAD is $DO_UPLOAD ===
$SCRIPT_DIR/bin/all-staff-list-builder --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$EXCEL_FILE" --upload "$DO_UPLOAD" --uploadId "$UPLOAD_ID"
BUILD_RESULT=$?
if (( $BUILD_RESULT != 0 )); then
  echo "ERROR: An error occurred running all-staff-list-builder."
  echo $SCRIPT_DIR/bin/all-staff-list-builder --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$EXCEL_FILE" --upload "$DO_UPLOAD" --uploadId "$UPLOAD_ID"
  exit 1
fi
