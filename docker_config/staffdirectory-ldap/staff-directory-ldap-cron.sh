#!/bin/bash

# To support Kubernetes volume mounting, this script uses the following
# subdirectories of SCRIPT_DIR
#
#   output/ - Directory containing output of the scripts. Should be mounted in
#             persistent volume.
#   config/ - Directory for providing the configuraton information, such as the
#             Google service account JSON file, and "config.properties" file
#             Can be implemented as a Kubernetes configMap so it does not
#             need to be a persistent volume.

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
export CONFIG_PROPERTIES_FILE=$SCRIPT_DIR/config/config.properties
export LATEST_JSON_FILE=$SCRIPT_DIR/output/persons.json.latest
export CURRENT_JSON_FILE=$SCRIPT_DIR/output/persons.json
export BACKUP_JSON_FILE=$SCRIPT_DIR/output/persons.json.bak

CONFIG_FILE=$SCRIPT_DIR/config/config.properties

cd "$SCRIPT_DIR"

echo === Retrieving staff information ===
bin/staff-retriever --config "$CONFIG_PROPERTIES_FILE" --output "$LATEST_JSON_FILE"
RETRIEVER_RESULT=$?
if [[ "$RETRIEVER_RESULT" -ne "0" ]]; then
  echo "ERROR: bin/staff-retriever failed. Exiting."
  exit 1
fi

# Exit if previous file exists, and it the same as the newly generated file
if [ -f "$CURRENT_JSON_FILE" ]; then
  if diff -q "$CURRENT_JSON_FILE" "$LATEST_JSON_FILE"; then
     echo "$CURRENT_JSON_FILE" and "$LATEST_JSON_FILE" are the same. Exiting.
     exit 0
  fi
fi

if [ -f "$CURRENT_JSON_FILE" ]; then
  echo Moving "$CURRENT_JSON_FILE" to "$BACKUP_JSON_FILE"
  mv "$CURRENT_JSON_FILE" "$BACKUP_JSON_FILE"
fi

echo Moving "$LATEST_JSON_FILE" to "$CURRENT_JSON_FILE"
mv $LATEST_JSON_FILE $CURRENT_JSON_FILE

echo === Calling staff-list-excel.sh ===
./staff-list-excel.sh
EXCEL_RESULT=$?
if [[ "$EXCEL_RESULT" -ne "0" ]]; then
  echo "ERROR: staff-list-excel.sh failed."
fi

echo === Call staff-list-drupal.sh ===
echo TODO