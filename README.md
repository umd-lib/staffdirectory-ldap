# staffdirectory-ldap

Java application for generating an Excel spreadsheet from LDAP.

**Warning**: This is PROOF-OF-CONCEPT code only.

## Application configuration

The following file is used to configure the application.

### ldap.properties

A properties file for specifying the LDAP connection information.

A sample "ldap.properties.example" file has been included in this repository.

Copy the "ldap.properties.example" to "ldap.properties" and fill in the
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
> java -jar target/staffdirectory-ldap-<VERSION>.jar --config <LDAP CONFIG FILE> --out <EXCEL OUTPUT FILE>
```
where:
 
* \<VERSION> is the Maven version number
* \<LDAP CONFIG FILE> is the path to the LDAP configuration properties file
* \<EXCEL OUTPUT FILE> the path location to create the Excel spreadsheet
 
For example, using
 
* \<VERSION> - "1.0.0-SNAPSHOT"
* \<LDAP CONFIG FILE> - "ldap.properties"
* \<EXCEL OUTPUT FILE> - "test.xlsx"

the command would be:

```
> java -jar target/staffdirectory-ldap-1.0.0-SNAPSHOT.jar --config ldap.properties --out test.xlsx
```

## License

See the [LICENSE](LICENSE.txt) file for license rights and limitations.