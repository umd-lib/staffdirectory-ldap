#!/bin/bash

# Builds the "All Staff List.xslx" Excel spreadsheet and copies it to the
# "Staff Directory Information" directory on the Library Samba share.
#
# The following environment variables are expected by this script:
#
#    SMB_USER - the Samba username
#    SMB_PASSWORD - the Samba password
#    EXCEL_MODIFICATION_USER - The user to set on the Excel spreadsheet
#    EXCEL_MODIFICATION_PASSWORD - The password to set on the Excel spreadsheet 

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
export CONFIG_PROPERTIES_FILE=$SCRIPT_DIR/config/config.properties
JSON_FILE="$SCRIPT_DIR/output/persons.json"
EXCEL_FILE="$SCRIPT_DIR/output/All Staff List New.xlsx"
UPLOAD_EXCEL_FILENAME="TEST-All Staff List New-TEST.xlsx"

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

echo === Building Excel spreadsheet ===
$SCRIPT_DIR/bin/all-staff-list-builder --user "$EXCEL_MODIFICATION_USER" --password "$EXCEL_MODIFICATION_PASSWORD" --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$EXCEL_FILE"
BUILD_RESULT=$?
if (( $BUILD_RESULT != 0 )); then
  echo "ERROR: An error occurred running all-staff-list-builder."
  echo $SCRIPT_DIR/bin/all-staff-list-builder --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$EXCEL_FILE"
  exit 1
fi

echo === Uploading updated Excel spreadsheet ===
/usr/bin/smbclient \
  '//librfs001v.ad.umd.edu/department$' \
  --user $SMB_USER%$SMB_PASSWORD \
  -D 'LibraryShare\Staff Directory Information' \
  -c "put "\""$EXCEL_FILE"\"" "\""$UPLOAD_EXCEL_FILENAME"\"""

TRANSFER_RESULT=$?

if (( $TRANSFER_RESULT != 0 )); then
  echo "ERROR: An error occurred transferring Excel spreadsheet to Samba"
  exit 1
fi
