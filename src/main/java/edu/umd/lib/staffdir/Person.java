package edu.umd.lib.staffdir;

import java.util.List;

/**
 * Encapsulates information for a single person.
 */
public interface Person {
  /**
   * @return the unique identifier for this person. 
   */
  public String getUid();
  
  /**
   * @return the last name (surname) for this person.
   */
  public String getLastName();
  
  /**
   * @return the first name (given name) for this person.
   */
  public String getFirstName();
  
  /**
   * @return the phone number for this person.
   */
  public String getPhoneNumber();
  
  /**
   * @return the email address for this person.
   */
  public String getEmail();
  
  /**
   * @return the official title for this person.
   */
  public String getOfficialTitle();
  
  /**
   * @return the job title for this person.
   */
  public String getJobTitle();
  
  /**
   * @return the room number associated with this person.
   */
  public String getRoomNumber();
  
  /**
   * @return the building associated with this person.
   */
  public String getBuilding();
  
  /**
   * @return the division associated with this person.
   */
  public String getDivision();

  /**
   * @return the department associated with this person.
   */
  public String getDepartment();

  /**
   * @return the unit associated with this person.
   */
  public String getUnit();

  /**
   * @return the location associated with the cost center for this person.
   */
  public String getLocation();

  /**
   * @return the appointment full-time-equivalent for this person.
   */
  public String getFte();
  
  /**
   * @return the List of category statuses associated with this person.
   */
  public List<String> getCategoryStatuses();
  
  /**
   * @return true if this person has Faculty Permanent Status, false otherwise.
   */
  public boolean isFacultyPermanentStatus();
  
  /**
   * @return the descriptive title associated with this person.
   */
  public String getDescriptiveTitle();
    
  /**
   * @return the cost center associated with this person
   */
  public String getCostCenter();
}

