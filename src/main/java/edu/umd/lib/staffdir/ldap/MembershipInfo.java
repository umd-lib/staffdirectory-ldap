package edu.umd.lib.staffdir.ldap;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts "memberOf" values into specific attributes
 */
public class MembershipInfo {
  public static final Logger log = LoggerFactory.getLogger(MembershipInfo.class);

  // The prefix for the Staff Directory Grouper group
  private static final String STAFF_DIR_PREFIX = "Departmental_Groups:Libraries:Staff_Directory:";

  // The Grouper group for faculty permanent status
  private static final String FACULTY_PERMANENT_STATUS = "Departmental_Groups:Libraries:Staff_Directory:Permanent_Status";

  // The prefix for the FTE Grouper group
  private static final String FTE_PREFIX = "Departmental_Groups:Libraries:Staff_Directory:FTE_";

  private List<String> memberships;

  private String division;
  private String department;
  private String unit;
  private String costCenter;
  private boolean facultyPermanentStatus;
  private String fte = null;

  /**
   * Constructs a MembershipInfo from the given List of "memberOf" values.
   *
   * @param memberships
   *          the "memberOf" values to use in constructing this object.
   */
  public MembershipInfo(List<String> memberships) {
    this.memberships = memberships;
    init();
  }

  private void init() {
    if (memberships == null) {
      return;
    }

    String libraryMembership = null;
    for (String membership : memberships) {
      if (membership == null) {
        return;
      }
      if (membership.equals(FACULTY_PERMANENT_STATUS)) {
        this.facultyPermanentStatus = true;
        continue;
      }
      if (membership.startsWith(FTE_PREFIX)) {
        String chopped = membership.replaceFirst(FTE_PREFIX, "");
        String fte = chopped + ".00";
        this.fte = fte;
        continue;
      }
      if (membership.startsWith(STAFF_DIR_PREFIX)) {
        libraryMembership = membership;
      }
    }

    if (libraryMembership == null) {
      return;
    }

    String chopped = libraryMembership.replaceFirst(STAFF_DIR_PREFIX, "");
    String[] elements = chopped.split(":");

    if ((elements.length >= 2) && (elements.length <= 3)) {
      if (elements.length == 3) {
        // Unit
        String unitElement = elements[2];
        String[] unitElements = unitElement.split("_", 2);
        String underscoreUnit = unitElements[1];
        this.unit = underscoreUnit.replaceAll("_", " ");
        this.costCenter = unitElements[0];
      }
      // Division and department
      String divisionElement = elements[0];
      String[] divisionElements = divisionElement.split("_", 2);
      String underscoreDivision = divisionElements[1];

      String departmentElement = elements[1];
      String[] departmentElements = departmentElement.split("_", 2);
      String underscoreDepartment = departmentElements[1];
      this.division = underscoreDivision.replaceAll("_", " ");
      this.department = underscoreDepartment.replaceAll("_", " ");
      if (this.costCenter == null) {
        this.costCenter = departmentElements[0];
      }
    } else {
      log.error("Can't parse '" + libraryMembership + "'");
    }
  }

  /**
   * @return the organizational division, or null if the division could not be
   *         determined.
   */
  public String getDivision() {
    return division;

  }

  /**
   * @return the organizational department, or null if the division could not be
   *         determined.
   */
  public String getDepartment() {
    return department;
  }

  /**
   * @return the organizational unit, or null no unit information was provided,
   *         or the unit could not be determined.
   */
  public String getUnit() {
    return unit;
  }

  /**
   * @return the cost center, or null if the cost center could not be
   *         determined.
   */
  public String getCostCenter() {
    return costCenter;
  }

  /**
   * @return true if faculty permanent status was found, false otherwise.
   */
  public boolean isFacultyPermanentStatus() {
    return facultyPermanentStatus;
  }

  /**
   * @return a String representing the FTE percentage. A value of null indicates
   *         a full-time FTE.
   */
  public String getFte() {
    return fte;
  }
}
