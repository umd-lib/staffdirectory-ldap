package edu.umd.lib.staffdir;

import java.util.Map;

/**
 * Encapsulates information for a single person.
 */
public class Person {
  public String uid;
  public String lastName;
  public String firstName;
  public String phoneNumber;
  public String email;
  public String officialTitle;
  public String roomNumber;
  public String building;
  public String division;
  public String department;
  public String unit;
  public String fte;
  public String categoryStatus;
  public boolean isFacultyPermanentStatus;
  public String descriptiveTitle;
  public String costCenter;
  public String location;
  public String functionalTitle;

  public static Person createPerson(String uid,
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

    Person person = new Person();
    person.uid = uid;
    person.lastName = ldapResult.get("sn");
    person.firstName = ldapResult.get("givenName");
    person.phoneNumber = ldapResult.get("telephoneNumber");
    person.email = ldapResult.get("mail");
    person.officialTitle = ldapResult.get("umOfficialTitle");
    person.roomNumber = ldapResult.get("umPrimaryCampusRoom");
    person.building = ldapResult.get("umPrimaryCampusBuilding");
    person.division = organization.get("Division Code");
    person.department = organization.get("Department");
    person.unit = organization.get("Unit");
    person.fte = staffEntry.get("Appt Fte");
    person.categoryStatus = ldapResult.get("umCatStatus");
    person.costCenter = costCenter;
    person.isFacultyPermanentStatus = facultyPermStatus;
    person.descriptiveTitle = ldapResult.get("umDisplayTitle");
    person.location = organization.get("Location");
    person.functionalTitle = staffEntry.get("Functional Title");

    return person;
  }

  @Override
  public String toString() {
    String str = String.format("Person@%s[uid: %s - %s, %s]]ß",
        Integer.toHexString(System.identityHashCode(this)),
        uid, lastName, firstName);
    return str;
  }
}
