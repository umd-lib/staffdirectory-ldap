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

A "config.properties" file is used for specifying application configuration,
including:

* the LDAP connection information
* the Google service account credentials file
* the Google Sheets document to retrieve

A sample "config.properties.template" file has been included in this repository.

Copy the "config.properties.template" to "config.properties" and fill in the
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
> target/appassembler/bin/all-staff-list-builder --config <CONFIG FILE> [--user <USER>] [--password <PASSWORD>] --input <JSON INPUT FILE> --output <EXCEL OUTPUT FILE>
```

where:

* \<CONFIG FILE> is the path to the configuration properties file
* \<USER> an (optional) user used to protect the Excel spreadsheet.
    Will use a default if not provided, and "password" is provided.
* \<PASSWORD> an (optional) password required to modify the Excel spreadsheet
* \<JSON INPUT FILE> the path location to the JSON file created by "staff-retriever"
* \<EXCEL OUTPUT FILE> the path location to create the Excel spreadsheet

For example, using

* \<CONFIG FILE> - "config.properties"
* \<USER> - "Test User"
* \<PASSWORD> - "abcd"
* \<JSON INPUT FILE> - "persons.json"
* \<EXCEL OUTPUT FILE> - "All Staff List New.xlsx"

the command would be:

```
> target/appassembler/bin/all-staff-list-builder --config config.properties --user "Test User" --password abcd --input persons.json --output "All Staff List New.xlsx"
```

### Excel User and Password

The "--user" and "--password" arguments are used to set the
"ReadOnly-Recommended" flag on the Excel spreadsheet. When set, any user opening
the Excel spreadsheet will be shown a dialog indicating that the file is
"reserved by" the name given in "--user", and given an option to either type
in the password, or open the file read-only. Only users entering the password
can modify the Excel spreadsheet.

The "user" setting is essentially cosmetic -- it is shown in the dialog, and
has no other effect.

The Excel spreadsheet is protected in this way because when opened normally,
Excel "locks" the file, preventing it from being updated. This is a problem for
the cron job in "k8s-staffdirectory-ldap", which cannot replace the file if it
open for modification. Therefore the Excel spreadsheet used on the LAN should
always have the user and password set.

## Document Mappings

See [docs/OutputDocumentMapping.md](docs/OutputDocumentMapping.md) for
information on using the Google Sheets document for specifying the display of
fields in the output document.

## Dockerfile-staffdir-cron and Kubernetes

The "Dockerfile-staffdir-cron" creates a Docker image for use with the
[umd-lib/k8s-staffdirectory-ldap][k8s-staffdirectory-ldap] Kubernetes
configuration.

The scripts used by CronJob are in the "docker_config/staffdirectory-ldap"
directory.

## License

See the [LICENSE](LICENSE.txt) file for license rights and limitations.

[k8s-staffdirectory-ldap]: https://github.com/umd-lib/k8s-staffdirectory-ldap
