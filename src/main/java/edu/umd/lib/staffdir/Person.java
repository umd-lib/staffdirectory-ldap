package edu.umd.lib.staffdir;

import java.util.Map;

/**
 * Encapsulates information for a single person.
 */
public class Person {
  public final String uid;
  private Map<String, Map<String, String>> sources;

  /**
   * Constructs a new Person from the given parameters
   *
   * @param uid
   *          the unique identifier for the person, typically the directory id
   * @param staffMap
   *          a Map<String, String> containing the fields from the "Staff" sheet
   *          of the "Online Staff Directory Mapping" Google Sheets document for
   *          the given uid
   * @param ldapResults
   *          a Map<String, String> of the LDAP search result for the given uid
   * @param organizationsMap
   *          a Map<String, String> of the organization for the associated uid
   *          derived from the "Organizations" sheet of the "Online Staff
   *          Directory Mapping" Google Sheets document
   */
  public Person(String uid,
      Map<String, Map<String, String>> sources) {
    this.uid = uid;
    this.sources = sources;
  }

  public String get(String source, String field) {
    Map<String, String> src = sources.get(source);
    if (src == null) {
      // TODO: Emit warning the source is null
      return "";
    }

    return src.getOrDefault(field, "");
  }

  public String getAllowNull(String source, String field) {
    Map<String, String> src = sources.get(source);
    if (src == null) {
      // TODO: Emit warning the source is null
      return null;
    }

    return src.getOrDefault(field, null);
  }

  @Override
  public String toString() {
    String str = String.format("Person@%s[uid: %s]ß",
        Integer.toHexString(System.identityHashCode(this)),
        uid);
    return str;
  }
}
