package edu.umd.lib.staffdir.ldap;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MembershipInfo {
  public static final Logger log = LoggerFactory.getLogger(MembershipInfo.class);
  
  private static final String STAFF_DIR_PREFIX = "Departmental_Groups:Libraries:Staff_Directory:";
  private static final String FACULTY_PERMANENT_STATUS = "Departmental_Groups:Libraries:Staff_Directory:Permanent_Status";
  
  private List<String> memberships;
  
  private String division;
  private String department;
  private String unit;
  private String costCenter;
  private boolean facultyPermanentStatus;
  
  public MembershipInfo(List<String> memberships) {
    this.memberships = memberships;
    init();
  }
  
  private void init() {
    if  (memberships == null) {
      return;
    }
    
    String libraryMembership = null;
    for(String membership : memberships) {
      if (membership == null) {
        return;
      }
      if (membership.equals(FACULTY_PERMANENT_STATUS)) {
        this.facultyPermanentStatus = true;
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
    
    if ((elements.length >= 2) && (elements.length <=3)) {
      if (elements.length == 3) {
        // Unit
        String unitElement = elements[2];
        String[] unitElements = unitElement.split("_", 2);
        String underscoreUnit = unitElements[1];
        this.unit = underscoreUnit.replaceAll("_", " ");
        this.costCenter =  unitElements[0];
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
        this.costCenter =  departmentElements[0];
      }
    } else {
      log.error("Can't parse '"+libraryMembership+"'");
    }
  }
  
  public String getDivision() {
    return division;
    
  }
  
  public String getDepartment() {
    return department;
  }
  
  public String getUnit() {
    return unit;
  }
  
  public String getCostCenter() {
    return costCenter;
  }
  
  public boolean isFacultyPermanentStatus() {
    return facultyPermanentStatus;
  }
}
