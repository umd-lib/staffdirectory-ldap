package edu.umd.lib.staffdir;

/**
 * Builder for constructing a Person object, using a fluent interface.
 */
public class PersonBuilder {
  private PersonImpl person = new PersonImpl();

  public PersonBuilder uid(String uid) {
    person.setUid(uid);
    return this;
  }

  public PersonBuilder lastName(String lastName) {
    person.setLastName(lastName);
    return this;
  }

  public PersonBuilder firstName(String firstName) {
    person.setFirstName(firstName);
    return this;
  }

  public PersonBuilder phoneNumber(String phoneNumber) {
    person.setPhoneNumber(phoneNumber);
    return this;
  }

  public PersonBuilder email(String email) {
    person.setEmail(email);
    return this;
  }

  public PersonBuilder officialTitle(String officialTitle) {
    person.setOfficialTitle(officialTitle);
    return this;
  }

  public PersonBuilder roomNumber(String roomNumber) {
    person.setRoomNumber(roomNumber);
    return this;
  }

  public PersonBuilder building(String building) {
    person.setBuilding(building);
    return this;
  }

  public PersonBuilder division(String division) {
    person.setDivision(division);
    return this;
  }

  public PersonBuilder department(String department) {
    person.setDepartment(department);
    return this;
  }

  public PersonBuilder unit(String unit) {
    person.setUnit(unit);
    return this;
  }

  public PersonBuilder fte(String fte) {
    person.setFte(fte);
    return this;
  }

  public PersonBuilder categoryStatus(String categoryStatus) {
    person.setCategoryStatus(categoryStatus);
    return this;
  }

  public PersonBuilder facultyPermanentStatus(boolean facultyPermanentStatus) {
    person.setFacultyPermanentStatus(facultyPermanentStatus);
    return this;
  }

  public PersonBuilder descriptiveTitle(String descriptiveTitle) {
    person.setDescriptiveTitle(descriptiveTitle);
    return this;
  }

  public PersonBuilder costCenter(String costCenter) {
    person.setCostCenter(costCenter);
    return this;
  }

  public PersonBuilder location(String location) {
    person.setLocation(location);
    return this;
  }

  public PersonBuilder functionalTitle(String functionalTitle) {
    person.setFunctionalTitle(functionalTitle);
    return this;
  }

  public Person getPerson() {
    return person;
  }
}

/**
 * Implementation of the Person interface
 */
class PersonImpl implements Person {
  private String uid;
  private String lastName;
  private String firstName;
  private String phoneNumber;
  private String email;
  private String officialTitle;
  private String roomNumber;
  private String building;
  private String division;
  private String department;
  private String unit;
  private String fte;
  private String categoryStatus;
  private boolean facultyPermanentStatus;
  private String descriptiveTitle;
  private String costCenter;
  private String location;
  private String functionalTitle;

  @Override
  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @Override
  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  @Override
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public String getOfficialTitle() {
    return officialTitle;
  }

  public void setOfficialTitle(String officialTitle) {
    this.officialTitle = officialTitle;
  }

  @Override
  public String getRoomNumber() {
    return roomNumber;
  }

  public void setRoomNumber(String roomNumber) {
    this.roomNumber = roomNumber;
  }

  @Override
  public String getBuilding() {
    return building;
  }

  public void setBuilding(String building) {
    this.building = building;
  }

  @Override
  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  @Override
  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  @Override
  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  @Override
  public String getFte() {
    return fte;
  }

  public void setFte(String fte) {
    this.fte = fte;
  }

  @Override
  public String getCategoryStatus() {
    return categoryStatus;
  }

  public void setCategoryStatus(String categoryStatus) {
    this.categoryStatus = categoryStatus;
  }

  @Override
  public boolean isFacultyPermanentStatus() {
    return facultyPermanentStatus;
  }

  public void setFacultyPermanentStatus(boolean facultyPermanentStatus) {
    this.facultyPermanentStatus = facultyPermanentStatus;
  }

  @Override
  public String getDescriptiveTitle() {
    return descriptiveTitle;
  }

  public void setDescriptiveTitle(String descriptiveTitle) {
    this.descriptiveTitle = descriptiveTitle;
  }

  @Override
  public String getCostCenter() {
    return costCenter;
  }

  public void setCostCenter(String costCenter) {
    this.costCenter = costCenter;
  }

  @Override
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Override
  public String getFunctionalTitle() {
    return functionalTitle;
  }

  public void setFunctionalTitle(String functionalTitle) {
    this.functionalTitle = functionalTitle;
  }

  @Override
  public String toString() {
    String str = String.format("uid: %s - %s, %s", getUid(), getLastName(), getFirstName());
    return str;
  }
}
