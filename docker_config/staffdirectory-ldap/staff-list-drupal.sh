#!/bin/bash

# Builds the JSON file used by Drupal and uploads it to the Drupal REST
# endpoint.
#
# The following environment variables are expected by this script:
#
# DRUPAL_USER:
#    The Drupal username to perform the JSON upload
# DRUPAL_PASSWORD:
#    The password for the Drupal user
# DRUPAL_HOST:
#   The base URL to the Drupal host, without the trailing slash,
#   for example "https://www.lib.umd.edu'
# DRUPAL_CSRF_TOKEN_URL_PATH:
#   The URL path for retrieving a CSRF token, for example "/session/token"
# DRUPAL_STAFF_DIRECTORY_UPDATE_URL_PATH:
#   The URL path to upload the Staff Directory JSON file to,
#   for example "/staff-directory/updater"

if [ -z "$DRUPAL_USER" ]; then
  echo "ERROR: Please provide a non-empty 'DRUPAL_USER' environment variable"
  exit 1
fi

if [ -z "$DRUPAL_PASSWORD" ]; then
  echo "ERROR: Please provide a non-empty 'DRUPAL_PASSWORD' environment variable"
  exit 1
fi

if [ -z "$DRUPAL_HOST" ]; then
  echo "ERROR: Please provide a non-empty 'DRUPAL_HOST' environment variable"
  exit 1
fi

if [ -z "$DRUPAL_CSRF_TOKEN_URL_PATH" ]; then
  echo "ERROR: Please provide a non-empty 'DRUPAL_CSRF_TOKEN_URL_PATH' environment variable"
  exit 1
fi

if [ -z "ERROR: $DRUPAL_STAFF_DIRECTORY_UPDATE_URL" ]; then
  echo "Please provide a non-empty 'DRUPAL_STAFF_DIRECTORY_UPDATE_URL' environment variable"
  exit 1
fi


if [ -z "ERROR: $DRUPAL_STAFF_DIRECTORY_UPDATE_URL" ]; then
  echo "Please provide a non-empty 'DRUPAL_STAFF_DIRECTORY_UPDATE_URL' environment variable"
  exit 1
fi

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

echo === Drupal Upload ===

echo DRUPAL_USER: $DRUPAL_USER
echo DRUPAL_HOST: $DRUPAL_HOST
echo DRUPAL_CSRF_TOKEN_URL_PATH: $DRUPAL_CSRF_TOKEN_URL_PATH
echo DRUPAL_STAFF_DIRECTORY_UPDATE_URL_PATH: $DRUPAL_STAFF_DIRECTORY_UPDATE_URL_PATH

echo === Retrieving CSRF Token from Drupal ===

CSRF_TOKEN=`curl $DRUPAL_HOST$DRUPAL_CSRF_TOKEN_URL_PATH -d '{"name":"'$DRUPAL_USER'","pass":'$DRUPAL_PASSWORD'"}' -H "Content-type: application/json" -H "Accept: application/json"`

echo === Uploading $DRUPAL_OUTPUT_FILE to Drupal ===

curl --fail --include --request POST --user "$DRUPAL_USER":"$DRUPAL_PASSWORD" --header 'Content-type: application/json' \
    --header 'X-CSRF-Token: '$CSRF_TOKEN'\' $DRUPAL_HOST$DRUPAL_STAFF_DIRECTORY_UPDATE_URL_PATH \
    --data-binary @$DRUPAL_OUTPUT_FILE

UPLOAD_RESULT=$?

if (( $UPLOAD_RESULT != 0 )); then
  echo "ERROR: An error occurred uploading the JSON file to Drupal."
fi

exit $UPLOAD_RESULT
