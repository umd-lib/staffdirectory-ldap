# staffdirectory-ldap

Java application for generating output documents using Staff Directory
data retrieved from LDAP.

## Application configuration

### Google Sheets configuration

This application utilizes a Google Sheets document to provide data and
configuration to the application.

Communication with Google requires a Google service account, and the
private key file for the account must be accessible to the application.

The project associated with the service account must have the
"Google Sheets API" enabled.

Also, the service account must have "view" permission to Google Sheets document
being read. To do this, simply share the document with the email address
specified in the the "client_email" field of the private key file.

### config.properties

A "config.proprties" file is used for specifying application configuration,
including:

* the LDAP connection information
* the Google service account credentials file
* the Google Sheets document to retrieve

A sample "config.properties.example" file has been included in this repository.

Copy the "config.properties.example" to "config.properties" and fill in the
appropriate values.

## Running the application

This application uses the Maven "Application Assembler" plugin
(<https://www.mojohaus.org/appassembler/appassembler-maven-plugin/>)
to create executable scripts for running different commands.

To build the application, run:

```
> mvn clean package appassembler:assemble
```

This will create a "target/appassembler" directory containing the application
components.

## Application Scripts

The following application scripts are available:

* staff-retriever
* all-staff-list-builder

### staff-retriever

Retrieves the "Online Staff Directory Mapping" Google Sheets document from
Google Drive, and creates a JSON file consisting of staff directory information
obtained from LDAP, and the Google Sheets document.

This script uses the following sheets in the Google Drive document:

* Staff
* Organization

See [docs/StaffRetrieverInputDocument.md](docs/StaffRetrieverInputDocument.md)
for information on these sheets.

To run the script (from the project base directory):

```
> target/appassembler/bin/staff-retriever --config <CONFIG FILE> --output <JSON OUTPUT FILE>
```

where:

* \<CONFIG FILE> is the path to the configuration properties file
* \<JSON OUTPUT FILE> the path location to create the JSON output

For example, using

* \<CONFIG FILE> - "config.properties"
* \<JSON OUTPUT FILE> - "persons.json"

the command would be:

```
> target/appassembler/bin/staff-retriever --config config.properties --output persons.json
```

### all-staff-list-builder

This script generates the "All Staff List" spreadsheet from the JSON file
created by the "staff-retriever" script.

This script uses the following sheets in the Google Drive document:

* All Staff List Mapping
* CategoryStatus

To run the script (from the project base directory):

```
> target/appassembler/bin/all-staff-list-builder --config <CONFIG FILE> --input <JSON INPUT FILE> --output <EXCEL OUTPUT FILE>
```

where:

* \<CONFIG FILE> is the path to the configuration properties file
* \<JSON INPUT FILE> the path location to the JSON file created by "staff-retriever"
* \<EXCEL OUTPUT FILE> the path location to create the Excel spreadsheet

For example, using

* \<CONFIG FILE> - "config.properties"
* \<JSON INPUT FILE> - "persons.json"
* \<EXCEL OUTPUT FILE> - "All Staff List New.xlsx"

the command would be:

```
> target/appassembler/bin/all-staff-list-builder --config config.properties --input persons.json --output "All Staff List New.xlsx"
```

## Document Mappings

See [docs/OutputDocumentMapping.md](docs/OutputDocumentMapping.md) for
information on using the Google Sheets document for specifying the display of
fields in the output document.

## License

See the [LICENSE](LICENSE.txt) file for license rights and limitations.
