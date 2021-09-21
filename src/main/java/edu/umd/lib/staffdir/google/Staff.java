package edu.umd.lib.staffdir.google;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the "Staff" sheet from the Google Sheets document.
 */
public class Staff {
  private Map<String, Map<String, String>> staffMap;

  /**
   * Converts the raw List of Maps from SheetsRetriever into a Map of Maps,
   * indexed by uid.
   *
   * @param rawOrganizationsList
   *          the raw List of Map<String, String> from SheetsRetriever
   * @param uidField
   *          the key in the map designating the uid.
   */
  public Staff(List<Map<String, String>> rawStaffMap, String uidField) {
    this.staffMap = createStaffMap(rawStaffMap, uidField);
  }

  /**
   * Converts the given List of Maps into a Map of Maps, keyed by the uid.
   *
   * @param rawStaffMap
   *          the List of Maps to convert
   * @param uidField
   *          the key in the Map to use to retrieve the uid.
   * @return a Map of Maps, keyed by the uid.
   *
   */
  private static Map<String, Map<String, String>> createStaffMap(
      List<Map<String, String>> rawStaffMap, String uidField) {
    Map<String, Map<String, String>> results = new HashMap<>();

    for (Map<String, String> entry : rawStaffMap) {
      String uid = entry.get(uidField).trim();
      Map<String, String> trimmedEntries = trimMap(entry);
      results.put(uid, trimmedEntries);
    }
    return results;
  }

  /**
   * Returns a Map where every entry in the given Map has been "trimmed" by the
   * "String.trim()" method.
   *
   * This is done because there is occasionally extra whitespace when users
   * copy-and-paste entries into the source spreadsheet.
   *
   * @param rawMap
   *          the Map to trim
   * @return the trimmed map
   */
  private static Map<String, String> trimMap(Map<String, String> rawMap) {
    Map<String, String> trimmedMap = new HashMap<>();
    for (String key : rawMap.keySet()) {
      trimmedMap.put(key, rawMap.get(key).trim());
    }
    return trimmedMap;
  }

  /**
   * @return a Set of all the uids
   */
  public Set<String> getUids() {
    return staffMap.keySet();
  }

  /**
   * Returns a Map of the staff information for the given uid.
   *
   * @param uid
   *          the uid of the staff information to return
   * @return a Map of the staff information for the given uid.
   */
  public Map<String, String> get(String uid) {
    return staffMap.get(uid);
  }
}
