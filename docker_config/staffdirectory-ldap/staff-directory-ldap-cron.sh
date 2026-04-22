#!/bin/bash

# Script run via Cron to retrieve the Staff Directory information from
# the Google Sheets document and LDAP, and generate the "All Staff List.xslx"
# Excel spreadsheet and update Drupal
#
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

# Retry configuration
MAX_RETRIES=3
RETRY_DELAY=5
RETRIEVER_RESULT=1

for attempt in $(seq 1 $MAX_RETRIES); do
  if [ $attempt -gt 1 ]; then
    echo "Retry attempt $attempt/$MAX_RETRIES (waiting ${RETRY_DELAY}s)..."
    sleep $RETRY_DELAY
    # Exponential backoff: increase delay for next retry
    RETRY_DELAY=$((RETRY_DELAY * 2))
  fi
  
  # Run with 5 minute timeout
  timeout 300 bin/staff-retriever --config "$CONFIG_PROPERTIES_FILE" --output "$LATEST_JSON_FILE"
  RETRIEVER_RESULT=$?
  
  # Success (0) or timeout (124) or other errors
  if [ $RETRIEVER_RESULT -eq 0 ]; then
    echo "Staff retriever completed successfully."
    break
  elif [ $RETRIEVER_RESULT -eq 124 ]; then
    echo "Staff retriever timed out (attempt $attempt/$MAX_RETRIES)"
  else
    echo "Staff retriever failed with exit code $RETRIEVER_RESULT (attempt $attempt/$MAX_RETRIES)"
  fi
done

if [[ "$RETRIEVER_RESULT" -ne "0" ]]; then
  echo "ERROR: bin/staff-retriever failed after $MAX_RETRIES attempts. Likely networking issue. Try again later. Exiting."
  exit 1
fi

# Check length of LATEST_JSON_FILE and exit if it is empty or nearly empty.
latest_json_filesize=$(wc -c $LATEST_JSON_FILE | awk '{print $1}')
echo "'$LATEST_JSON_FILE' is $latest_json_filesize bytes"

if (( $latest_json_filesize < 1000 )); then
  echo "'$LATEST_JSON_FILE' is only $latest_json_filesize bytes."
  echo "Probable retrieval failure. Exiting."
  exit 1
fi

if [ -f "$CURRENT_JSON_FILE" ]; then
  echo Moving "$CURRENT_JSON_FILE" to "$BACKUP_JSON_FILE"
  mv "$CURRENT_JSON_FILE" "$BACKUP_JSON_FILE"
fi

echo Moving "$LATEST_JSON_FILE" to "$CURRENT_JSON_FILE"
mv $LATEST_JSON_FILE $CURRENT_JSON_FILE

echo

echo === Calling staff-list-excel.sh ===
./staff-list-excel.sh
EXCEL_RESULT=$?
if [[ "$EXCEL_RESULT" -ne "0" ]]; then
  echo "ERROR: staff-list-excel.sh failed."
fi

echo

echo === Calling staff-list-drupal.sh ===
./staff-list-drupal.sh
DRUPAL_RESULT=$?
if [[ "$DRUPAL_RESULT" -ne "0" ]]; then
  echo "ERROR: staff-list-drupal.sh failed."
fi
