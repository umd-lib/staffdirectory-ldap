# Staff Directory Mapping

## Introduction

This page describes the mapping of fields from the source
"Online Staff Directory Mapping" document and LDAP to the fields in the
"Person" Java object.

## Mapping

| Mapping Document           | LDAP Attribute          | Person              |
| -------------------------- | ----------------------- | ------------------- |
| Staff::Directory ID        |                         | uid                 |
| Staff::Appt Fte            |                         | fte                 |
| Staff::Cost Center         |                         | costCenter          |
| Staff::Faculty Perm Status |                         | isFacultyPermStatus |
| Staff::Functional Title    |                         | functionalTitle     |
| Organization::Division     |                         | division            |
| Organization::Department   |                         | department          |
| Organization::Unit         |                         | unit                |
| Organization::Location     |                         | location            |
|                            | sn                      | lastName            |
|                            | givenName               | firstName           |
|                            | telephoneNumber         | phoneNumber         |
|                            | mail                    | email               |
|                            | umOfficialTitle         | officialTitle       |
|                            | umDisplayTitle          | descriptiveTitle    |
|                            | umPrimaryCampusRoom     | roomNumber          |
|                            | umPrimaryCampusBuilding | building            |
|                            | umCatStatus             | categoryStatus      |

**Note:** "Organization" fields are determined from using the
"Staff::CostCenter" field as key.