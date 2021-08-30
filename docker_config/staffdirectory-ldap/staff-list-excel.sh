#!/bin/bash

# Builds the "All Staff List.xslx" Excel spreadsheet and copies it to the
# "Staff Directory Information" directory on the Library Samba share.
#
# The following environment variables are expected by this script:
#
#    SMB_HOST - The Samba host (typically '//librfs001v.ad.umd.edu/department$')
#    SMB_DIRECTORY - The Samba directory to upload the file to
#    SMB_USER - the Samba username
#    SMB_PASSWORD - the Samba password
#    EXCEL_MODIFICATION_USER - The user to set on the Excel spreadsheet
#    EXCEL_MODIFICATION_PASSWORD - The password to set on the Excel spreadsheet
#    UPLOAD_EXCEL_FILENAME - The name of the uploaded file
#    SMB_DO_UPLOAD - "true" if the upload should be performed. Any other value prevents upload

if [ -z "$SMB_HOST" ]; then
  echo "Please provide a non-empty 'SMB_HOST' environment variable"
  exit 1
fi

if [ -z "$SMB_DIRECTORY" ]; then
  echo "Please provide a non-empty 'SMB_DIRECTORY' environment variable"
  exit 1
fi

if [ -z "$SMB_USER" ]; then
  echo "Please provide a non-empty 'SMB_USER' environment variable"
  exit 1
fi

if [ -z "$SMB_PASSWORD" ]; then
  echo "Please provide a non-empty 'SMB_PASSWORD' environment variable"
  exit 1
fi

if [ -z "$EXCEL_MODIFICATION_USER" ]; then
  echo "Please provide a non-empty 'EXCEL_MODIFICATION_USER' environment variable"
  exit 1
fi

if [ -z "$EXCEL_MODIFICATION_PASSWORD" ]; then
  echo "Please provide a non-empty 'EXCEL_MODIFICATION_PASSWORD' environment variable"
  exit 1
fi

if [ -z "$UPLOAD_EXCEL_FILENAME" ]; then
  echo "Please provide a non-empty 'UPLOAD_EXCEL_FILENAME' environment variable"
  exit 1
fi

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
export CONFIG_PROPERTIES_FILE=$SCRIPT_DIR/config/config.properties
JSON_FILE="$SCRIPT_DIR/output/persons.json"
EXCEL_FILE="$SCRIPT_DIR/output/all-staff-list-new.xlsx"

echo === Building Excel spreadsheet ===
$SCRIPT_DIR/bin/all-staff-list-builder --user "$EXCEL_MODIFICATION_USER" --password "$EXCEL_MODIFICATION_PASSWORD" --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$EXCEL_FILE"
BUILD_RESULT=$?
if (( $BUILD_RESULT != 0 )); then
  echo "ERROR: An error occurred running all-staff-list-builder."
  echo $SCRIPT_DIR/bin/all-staff-list-builder --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$EXCEL_FILE"
  exit 1
fi

if [ $SMB_DO_UPLOAD != "true" ]; then
  echo === Skipping Excel upload -- SMB_DO_UPLOAD is \'$SMB_DO_UPLOAD\' ===
  exit 0
fi

echo === Uploading updated Excel spreadsheet ===
/usr/bin/smbclient \
  "$SMB_HOST" \
  --user $SMB_USER%$SMB_PASSWORD \
  -D "$SMB_DIRECTORY" \
  -c "put "\""$EXCEL_FILE"\"" "\""$UPLOAD_EXCEL_FILENAME"\"""

TRANSFER_RESULT=$?

if (( $TRANSFER_RESULT != 0 )); then
  echo "ERROR: An error occurred transferring Excel spreadsheet to Samba"
  exit 1
fi
