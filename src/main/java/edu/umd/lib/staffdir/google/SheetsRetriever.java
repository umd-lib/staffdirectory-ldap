package edu.umd.lib.staffdir.google;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.OAuth2Credentials;

/**
 * Converts a single sheet in a Google Sheets document into a List of Maps.
 * <p>
 * This class assumes the first row of the sheet is the list of headers to use
 * as the keys for the map.
 */
public class SheetsRetriever {
  public static final Logger log = LoggerFactory.getLogger(SheetsRetriever.class);

  private final String appName;
  private final String serviceAccountCredentialsFile;

  /**
   * Constructs a SheetsRetriever object.
   *
   * @param appName
   *          the application name to provide to Google
   * @param serviceAccountCredentialsFile
   *          the fully-qualified path to the file containing the service
   *          account authentication credentials.
   */
  public SheetsRetriever(String appName, String serviceAccountCredentialsFile) {
    this.appName = appName;
    this.serviceAccountCredentialsFile = serviceAccountCredentialsFile;
  }

  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT
   *          The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException
   *           If the credentials.json file cannot be found.
   */
  private OAuth2Credentials getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    GoogleCredentials credential = GoogleCredentials.fromStream(
        new FileInputStream(this.serviceAccountCredentialsFile))
        .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
    return credential;
  }

  /**
   * Returns a ValueRange comprising all the cells in the given sheet of the
   * spreadsheet document.
   *
   * @param spreadsheetDocId
   *          the document id (from the URL) of the document
   * @param sheetName
   *          the name of the sheet within the document
   * @return a ValueRange comprising all the cells in the given sheet of the
   *         spreadsheet document.
   */
  private ValueRange getSpreadsheetCells(String spreadsheetDocId, String sheetName) {
    try {
      // Build a new authorized API client service.
      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      final String spreadsheetId = spreadsheetDocId;
      JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
      OAuth2Credentials credentials = getCredentials(HTTP_TRANSPORT);
      HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);
      Sheets service = new Sheets.Builder(HTTP_TRANSPORT, jsonFactory, requestInitializer)
          .setApplicationName(this.appName)
          .build();

      final String range = sheetName;
      ValueRange response = service.spreadsheets().values()
          .get(spreadsheetId, range)
          .execute();

      return response;
    } catch (GeneralSecurityException | IOException e) {
      log.error("Could not retrieve spreadsheetDocId='{}'", spreadsheetDocId, e);
    }
    return null;
  }

  /**
   * Returns a List containing a Map for each row in the given sheet of the
   * spreadsheet document.
   * <p>
   * This method assumes that the headers, used as the keys for the Map, are in
   * the first row of the sheet.
   *
   * @param spreadsheetDocId
   *          the document id (from the URL) of the document
   * @param sheetName
   *          the name of the sheet within the document
   * @return a List containing a Map for each row in the given sheet of the
   *         spreadsheet document. The map uses the first row as the keys for
   *         the map.
   */
  public List<Map<String, String>> toMap(String spreadsheetDocId, String sheetName) {
    ValueRange valueRange = getSpreadsheetCells(spreadsheetDocId, sheetName);
    return toMap(valueRange);
  }

  /**
   * Returns a List containing a Map for each row in the given ValueRange
   * <p>
   * This method assumes that the headers, used as the keys for the Map, are in
   * the first row of the ValueRange.
   *
   * @param valueRange
   *          the ValueRange containing the spreadsheet cells.
   * @return a List containing a Map for each row in the ValueRange. The map
   *         uses the first row as the keys for the map.
   */
  public List<Map<String, String>> toMap(ValueRange valueRange) {
    List<Map<String, String>> results = new ArrayList<>();

    if (valueRange == null) {
      return results;
    }

    List<List<Object>> values = valueRange.getValues();
    if (values == null || values.isEmpty()) {
      return results;
    }

    List<String> headerRow = new ArrayList<>();
    for (List<Object> row : values) {
      List<String> rowValues = new ArrayList<>();
      for (Object col : row) {
        rowValues.add((String) col);
        log.debug((String) col);
      }
      if (headerRow.isEmpty()) {
        headerRow.addAll(rowValues);
        log.debug("\n");
        continue;
      }
      Map<String, String> rowMap = new HashMap<>();
      for (int i = 0; i < rowValues.size(); i++) {
        rowMap.put(headerRow.get(i), rowValues.get(i));
      }
      results.add(rowMap);
      log.debug("\n");
    }
    return results;
  }
}
