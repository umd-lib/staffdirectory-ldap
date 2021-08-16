package edu.umd.lib.staffdir;

import java.util.Map;

/**
 * Encapsulates information for a single person.
 */
public class Person {
  public final String uid;
  public final String lastName;
  public final String firstName;
  public final String phoneNumber;
  public final String email;
  public final String officialTitle;
  public final String roomNumber;
  public final String building;
  public final String division;
  public final String department;
  public final String unit;
  public final String fte;
  public final String categoryStatus;
  public final boolean isFacultyPermanentStatus;
  public final String descriptiveTitle;
  public final String costCenter;
  public final String location;
  public final String functionalTitle;

  /**
   * Constructs a new Person from the given parameters
   *
   * @param uid
   *          the unique identifier for the person, typically the directory id
   * @param staffMap
   *          a Map<String, Map<String, String>> of entries from the "Staff"
   *          sheet of the "Online Staff Directory Mapping" Google Sheets
   *          document
   * @param ldapResults
   *          a Map<String, Map<String, String>> of the LDAP search results
   * @param organizationsMap
   *          a Map<String, Map<String, String>> derived from the
   *          "Organizations" sheet of the "Online Staff Directory Mapping"
   *          Google Sheets document
   */
  public Person(String uid,
      Map<String, Map<String, String>> staffMap,
      Map<String, Map<String, String>> ldapResults,
      Map<String, Map<String, String>> organizationsMap) {
    Map<String, String> staffEntry = staffMap.get(uid);
    Map<String, String> ldapResult = ldapResults.get(uid);
    String costCenter = staffEntry.get("Cost Center");
    Map<String, String> organization = organizationsMap.get(costCenter);

    boolean facultyPermStatus = false;
    String facultyPermStatusStr = staffEntry.get("Faculty Perm Status");
    if ((facultyPermStatusStr != null) && ("p".equals(facultyPermStatusStr.toLowerCase()))) {
      facultyPermStatus = true;
    }

    this.uid = uid;
    this.lastName = ldapResult.get("sn");
    this.firstName = ldapResult.get("givenName");
    this.phoneNumber = ldapResult.get("telephoneNumber");
    this.email = ldapResult.get("mail");
    this.officialTitle = ldapResult.get("umOfficialTitle");
    this.roomNumber = ldapResult.get("umPrimaryCampusRoom");
    this.building = ldapResult.get("umPrimaryCampusBuilding");
    this.division = organization.get("Division Code");
    this.department = organization.get("Department");
    this.unit = organization.get("Unit");
    this.fte = staffEntry.get("Appt Fte");
    this.categoryStatus = ldapResult.get("umCatStatus");
    this.costCenter = costCenter;
    this.isFacultyPermanentStatus = facultyPermStatus;
    this.descriptiveTitle = ldapResult.get("umDisplayTitle");
    this.location = organization.get("Location");
    this.functionalTitle = staffEntry.get("Functional Title");
  }

  @Override
  public String toString() {
    String str = String.format("Person@%s[uid: %s - %s, %s]]ß",
        Integer.toHexString(System.identityHashCode(this)),
        uid, lastName, firstName);
    return str;
  }
}
