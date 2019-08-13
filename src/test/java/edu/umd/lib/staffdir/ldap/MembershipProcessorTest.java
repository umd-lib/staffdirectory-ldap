package edu.umd.lib.staffdir.ldap;

import static edu.umd.lib.staffdir.ldap.MembershipTestCase.create;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MembershipProcessorTest {

  @Test
  public void testMembershipProcessor() {
    MembershipTestCase[] testCases = new MembershipTestCase[] {
        // Bad values
        create((List<String>) null,
            null, null, null, null, false),

        create("cn=Application_Roles:Libraries:Jenkins:Jenkins-User,ou=grouper, ou=group,dc=umd,dc=edu",
            null, null, null, null, false),

        // Departments
        create("Departmental_Groups:Libraries:Staff_Directory:04_DSS:044100_Software_Systems_Development_and_Research",
            "DSS", "Software Systems Development and Research", null, "044100", false),

        create("Departmental_Groups:Libraries:Staff_Directory:08_AS:083600_Human_Resources",
            "AS", "Human Resources", null, "083600", false),

        create("Departmental_Groups:Libraries:Staff_Directory:08_AS:085000_Budget_&_Business_Services_Office",
            "AS", "Budget & Business Services Office", null, "085000", false),

        // Unit
        create("Departmental_Groups:Libraries:Staff_Directory:08_AS:0836_Human_Resources:083654_Staff_Development",
            "AS", "Human Resources", "Staff Development", "083654", false)
    };

    for (MembershipTestCase test : testCases) {
      MembershipInfo info = new MembershipInfo(test.getMemberships());
      verify(test, info);
    }
  }

  @Test
  public void testFacultyPermanentStatus() {
    List<String> memberWithFacultyPermanentStatus = new ArrayList<>();
    memberWithFacultyPermanentStatus.add("Departmental_Groups:Libraries:Staff_Directory:Permanent_Status");
    memberWithFacultyPermanentStatus
        .add("Departmental_Groups:Libraries:Staff_Directory:04_DSS:044100_Software_Systems_Development_and_Research");

    MembershipTestCase test = create(memberWithFacultyPermanentStatus, "DSS",
        "Software Systems Development and Research", null, "044100", true);

    MembershipInfo info = new MembershipInfo(test.getMemberships());
    verify(test, info);
  }

  @Test
  public void testFte() {
    List<String> memberWithFte = new ArrayList<>();
    memberWithFte.add("Departmental_Groups:Libraries:Staff_Directory:FTE_50");
    memberWithFte
        .add("Departmental_Groups:Libraries:Staff_Directory:04_DSS:044100_Software_Systems_Development_and_Research");

    MembershipTestCase test = create(memberWithFte, "DSS",
        "Software Systems Development and Research", null, "044100", false, "50.00");

    MembershipInfo info = new MembershipInfo(test.getMemberships());
    verify(test, info);
  }

  private void verify(MembershipTestCase expected, MembershipInfo actual) {
    assertEquals("Division failed for " + expected.membersOf, expected.division, actual.getDivision());
    assertEquals("Department failed for " + expected.membersOf, expected.department, actual.getDepartment());
    assertEquals("Unit failed for " + expected.membersOf, expected.unit, actual.getUnit());
    assertEquals("Cost Center failed for " + expected.membersOf, expected.costCenter, actual.getCostCenter());
    assertEquals("Faculty Permanent Status failed for " + expected.membersOf, expected.facultyPermanentStatus,
        actual.isFacultyPermanentStatus());
    assertEquals("FTE failed for " + expected.membersOf, expected.fte,
        actual.getFte());
  }
}

class MembershipTestCase {
  public final List<String> membersOf;
  public final String division;
  public final String department;
  public final String unit;
  public final String costCenter;
  public final boolean facultyPermanentStatus;
  public final String fte;

  public static MembershipTestCase create(String memberOf, String division, String department, String unit,
      String costCenter, boolean facultyPermanentStatus) {
    return new MembershipTestCase(memberOf, division, department, unit, costCenter, facultyPermanentStatus);
  }

  public static MembershipTestCase create(List<String> membersOf, String division, String department, String unit,
      String costCenter, boolean facultyPermanentStatus) {
    return new MembershipTestCase(membersOf, division, department, unit, costCenter, facultyPermanentStatus, "100.00");
  }

  public static MembershipTestCase create(List<String> membersOf, String division, String department, String unit,
      String costCenter, boolean facultyPermanentStatus, String fte) {
    return new MembershipTestCase(membersOf, division, department, unit, costCenter, facultyPermanentStatus, fte);
  }

  public MembershipTestCase(List<String> membersOf, String division, String department, String unit, String costCenter,
      boolean facultyPermanentStatus) {
    this.membersOf = membersOf;
    this.division = division;
    this.department = department;
    this.unit = unit;
    this.costCenter = costCenter;
    this.facultyPermanentStatus = facultyPermanentStatus;
    this.fte = "100.00";
  }

  public MembershipTestCase(List<String> membersOf, String division, String department, String unit, String costCenter,
      boolean facultyPermanentStatus, String fte) {
    this.membersOf = membersOf;
    this.division = division;
    this.department = department;
    this.unit = unit;
    this.costCenter = costCenter;
    this.facultyPermanentStatus = facultyPermanentStatus;
    this.fte = fte;
  }

  public MembershipTestCase(String memberOf, String division, String department, String unit, String costCenter,
      boolean facultyPermanentStatus) {
    this(asList(memberOf), division, department, unit, costCenter, facultyPermanentStatus);
  }

  private static List<String> asList(String memberOf) {
    List<String> list = new ArrayList<>();
    list.add(memberOf);
    return list;

  }

  public List<String> getMemberships() {
    return membersOf;
  }
}
