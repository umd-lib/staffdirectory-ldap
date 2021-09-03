#!/bin/bash

# Builds the JSON file used by Drupal and uploads it to the Drupal REST
# endpoint.
#
# The following environment variables are expected by this script:

export SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
export CONFIG_PROPERTIES_FILE=$SCRIPT_DIR/config/config.properties
JSON_FILE="$SCRIPT_DIR/output/persons.json"
DRUPAL_OUTPUT_FILE="$SCRIPT_DIR/output/drupal.json"


echo === Building JSON file for Drupal ===

$SCRIPT_DIR/bin/drupal-builder --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$DRUPAL_OUTPUT_FILE"
BUILD_RESULT=$?
if (( $BUILD_RESULT != 0 )); then
  echo "ERROR: An error occurred running drupal-builder."
  echo $SCRIPT_DIR/bin/drupal-builder --config "$CONFIG_PROPERTIES_FILE" --input "$JSON_FILE" --output "$DRUPAL_OUTPUT_FILE"
  exit 1
fi

echo === Uploading $DRUPAL_OUTPUT_FILE to Drupal ===
echo TODO
