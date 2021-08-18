# staff-retriever Input Document

## Introduction

The "staff-retriever" script uses a Google Sheets document to provide the list
of people to retrieve from LDAP, as well as additional information required
for the output documents that LDAP does not provide.

The script is reliant on the following sheets in the document:

* Staff
* Organization

## Staff Sheet

The "Staff" sheet has the following columns:

* Directory ID
* Name - the name of the person.
* Cost Center
* Appt Fte
* Faculty Perm Status
* Functional Title

Only persons listed on this sheet (and found in LDAP) will be included in
the JSON output file.

### Directory ID

The "uid" of the person in LDAP.

### Name

The full name of the person.

**Note:** This field is intended only to make it simpler to identify individuals
for updates. The "Name" field should not be used to create values in the
output documents (use "sn" and "givenName" from LDAP instead).

### Cost Center

The Library cost center for the person. The value in this field must be listed
in the "Organization" sheet.

### Appt Fte

The "full-time equivalent" of the person's appointment.

### Faculty Perm Status

A "P" indicates the person has faculty permanent status.

### Functional Title

Typically used by output documents to optionally override a title retrieved
from LDAP.

## Organization Sheet

The "Organization" sheet has the following columns:

* Cost Center
* Division Code
* Division
* Department
* Unit
* Location

Note that the data in the sheet is "sparse", and follows a hierarchical
structure. See "Cost Center" for an explanation for this structure.

### Cost Center

The cost center, as a key to associate a person with a particular
division/department/unit and location.

Note that cost centers consist of 6 digits, exhibiting a hierarchical structure:

* the first two digits represent the division
* the middle two digits represent the department
* the last two digits represent the unit

When adding a cost center, the "parent" cost center must also be added (if it
does not already exist in the table).

For example, if a cost center of "123456" was added for the "Medieval Studies"
unit in the "Classics" department of the "Humanities" division, the following
cost centers would also need to be added:

* 120000 - for the "Humanities" division
* 123400 - for the "Classics" department

This structure enables the application, when given the cost center for a unit,
to "look up" the cost center for the department and division by simply
substituting zeros with the appropriate digits.

### Division Code

The abbreviation for the library division.

### Division

The full name for the library division.

### Department

The full name for the library department.

### Unit

The full name for the library unit.

### Location

The location of the cost center.
