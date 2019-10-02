# staffdirectory-ldap

Java application for generating an Excel spreadsheet from LDAP.

**Warning**: This is PROOF-OF-CONCEPT code only.

## Application configuration

### Google Sheets configuration

This application communicates with Google via a Google service account, and
the private key file for the account must be accessible to the application.

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
* the Google Sheets document to retrive

A sample "config.properties.example" file has been included in this repository.

Copy the "config.properties.example" to "config.properties" and fill in the
appropriate values.

## Running the application

The application is configured to use Maven to create a single executable
jar that contains all the dependencies. To create the jar, simply run:

```
> mvn package
```

This will create a "staffdirectory-ldap-\<VERSION>.jar" in the target/
subdirectory, where \<VERSION> is the Maven version number.

To run the application, use the following command:

```
> java -jar target/staffdirectory-ldap-<VERSION>.jar --config <CONFIG FILE> --out <EXCEL OUTPUT FILE>
```
where:
 
* \<VERSION> is the Maven version number
* \<CONFIG FILE> is the path to the configuration properties file
* \<EXCEL OUTPUT FILE> the path location to create the Excel spreadsheet
 
For example, using
 
* \<VERSION> - "1.0.0-SNAPSHOT"
* \<CONFIG FILE> - "config.properties"
* \<EXCEL OUTPUT FILE> - "test.xlsx"

the command would be:

```
> java -jar target/staffdirectory-ldap-1.0.0-SNAPSHOT.jar --config config.properties --out test.xlsx
```

## License

See the [LICENSE](LICENSE.txt) file for license rights and limitations.
