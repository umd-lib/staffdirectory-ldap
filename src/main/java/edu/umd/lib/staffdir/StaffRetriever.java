package edu.umd.lib.staffdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.staffdir.google.Organization;
import edu.umd.lib.staffdir.google.SheetsRetriever;
import edu.umd.lib.staffdir.google.Staff;
import edu.umd.lib.staffdir.ldap.Ldap;

/**
 * Command-line application for retrieving Staff Directory information from LDAP
 * and the Google Sheets document. This application outputs JSON, which can be
 * used as input to other applications, such as "AllStaffListBuilder".
 */
public class StaffRetriever {
  public static final Logger log = LoggerFactory.getLogger(StaffRetriever.class);

  public static void main(String[] args) {
    CommandLine cmdLine = parseCommandLine(args);

    String propFilename = cmdLine.getOptionValue("config");
    String outputFilename = cmdLine.getOptionValue("output");

    Properties props = getProperties(propFilename);

    // Google configuration Settings
    String appName = props.getProperty("appName");
    String serviceAccountCredentialsFile = props.getProperty("serviceAccountCredentialsFile");
    String spreadsheetDocId = props.getProperty("spreadsheetDocId");
    SheetsRetriever sr = new SheetsRetriever(appName, serviceAccountCredentialsFile);

    List<Map<String, String>> rawOrganizationsList = sr.toMap(spreadsheetDocId, "Organization");
    String costCenterField = "Cost Center";
    Organization organization = new Organization(rawOrganizationsList, costCenterField);

    List<Map<String, String>> rawStaffMap = sr.toMap(spreadsheetDocId, "Staff");
    String uidField = "Directory ID";
    Staff staff = new Staff(rawStaffMap, uidField);

    Set<String> uids = staff.getUids();

    // LDAP configuration settings
    String ldapUrl = props.getProperty("ldap.url");
    String authentication = props.getProperty("ldap.authentication");
    String bindDn = props.getProperty("ldap.bindDn");
    String credentials = props.getProperty("ldap.credentials");
    String searchBaseDn = props.getProperty("ldap.searchBaseDn");

    Ldap ldap = new Ldap(ldapUrl, authentication, bindDn, credentials, searchBaseDn);
    Map<String, Map<String, String>> ldapResults = ldap.getUsers(uids);

    List<Person> persons = new ArrayList<>();
    for (String uid : uids) {
      Map<String, String> staffEntry = staff.get(uid);
      Map<String, String> ldapEntry = ldapResults.get(uid);
      String costCenter = staffEntry.get("Cost Center");
      Map<String, String> organizationEntry = organization.getOrganization(costCenter);
      Map<String, Map<String, String>> sources = new HashMap<>();
      sources.put("Staff", staffEntry);
      sources.put("Organization", organizationEntry);
      sources.put("LDAP", ldapEntry);

      if (ldapEntry != null) {
        persons.add(new Person(uid, sources));
      } else {
        log.warn("WARNING: Could not find '{}' in LDAP. Skipping.", uid);
      }
    }

    // Sort the persons by last name and first name
    Collections.sort(persons, new Person.LastNameFirstNameComparator());

    JsonUtils.writeToJson(persons, outputFilename);
  }

  /**
   * Returns a Properties object derived from the specified file.
   *
   * Note: This method will terminate the application if the given file cannot
   * be parsed, or if the file is not found.
   *
   * @param propFilename
   *          the name of the file containing the properties
   * @return a Properties object derived from the specified file.
   */
  public static Properties getProperties(String propFilename) {
    File propFile = new File(propFilename);

    if (!propFile.exists()) {
      log.error("ERROR: Properties file '{}' cannot be found or accessed.", propFilename);
      System.exit(1);
    }

    Properties props = null;
    try {
      FileInputStream propFileIn = new FileInputStream(propFile);
      props = new Properties(System.getProperties());
      props.load(propFileIn);
    } catch (IOException ioe) {
      log.error("ERROR: Reading properties file '{}'", propFilename, ioe);
      System.exit(1);
    }
    return props;
  }

  /**
   * Returns a CommandLine object parsed from the given arguments
   *
   * Note: This method will terminate the application if the given arguments
   * cannot be parsed, or if the "--help" option is invoked.
   *
   * @param args
   *          the arguments passed to the "main" method
   * @return a CommandLine object containing the parsed arguments
   */
  public static CommandLine parseCommandLine(String[] args) {
    Options options = getOptions();
    CommandLineParser cmdLineParser = new DefaultParser();

    CommandLine cmdLine = null;
    try {
      cmdLine = cmdLineParser.parse(options, args);
    } catch (ParseException pe) {
      printHelp(options);
      System.exit(1);
    }

    if (cmdLine == null) {
      printHelp(options);
      System.exit(1);
    }

    if (cmdLine.hasOption("help")) {
      printHelp(options);
      System.exit(0);
    }

    return cmdLine;
  }

  /**
   * Create the Options used for the command-line.
   *
   * @return the Options used for the command-line.
   */
  private static Options getOptions() {
    Option outputOption = Option.builder("o")
        .longOpt("output")
        .hasArg()
        .argName("output file")
        .required()
        .desc("The Excel output filename")
        .build();
    Option configOption = Option.builder("c")
        .longOpt("config")
        .hasArg()
        .argName("properties file")
        .required()
        .desc("The properties file for configuring LDAP")
        .build();
    Option helpOption = Option.builder("h")
        .longOpt("help")
        .desc("Print this message")
        .build();

    Options options = new Options();
    options.addOption(outputOption);
    options.addOption(configOption);
    options.addOption(helpOption);

    return options;
  }

  /**
   * Prints a help message describing the command-line options
   *
   * @param options
   *          the Options for the command-line
   */
  private static void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    String header = "Generates an Excel spreadsheet from LDAP.";
    formatter.printHelp(120, "staffdirectory-ldap", header, options, "", true);
  }
}
