package edu.umd.lib.staffdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import edu.umd.lib.staffdir.google.Organizations;
import edu.umd.lib.staffdir.google.SheetsRetriever;
import edu.umd.lib.staffdir.ldap.Ldap;

/**
 * Application entrypoint for generating an Excel spreadsheet from LDAP
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
    List<Map<String, String>> rawStaffMap = sr.toMap(spreadsheetDocId, "Staff");

    String costCenterField = "Cost Center";
    String uidField = "Directory ID";
    Map<String, Map<String, String>> organizationsMap = Organizations.getCostCentersMap(rawOrganizationsList,
        costCenterField);
    Map<String, Map<String, String>> staffMap = createStaffMap(rawStaffMap, uidField);

    Set<String> uids = staffMap.keySet();

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
      Map<String, String> staffEntry = staffMap.get(uid);
      Map<String, String> ldapEntry = ldapResults.get(uid);
      String costCenter = staffEntry.get("Cost Center");
      Map<String, String> organization = organizationsMap.get(costCenter);
      Map<String, Map<String, String>> sources = new HashMap<>();
      sources.put("Staff", staffEntry);
      sources.put("Organization", organization);
      sources.put("LDAP", ldapEntry);

      if (ldapEntry != null) {
        persons.add(new Person(uid, sources));
      } else {
        log.warn("WARNING: Could not find '{}' in LDAP. Skipping.", uid);
      }
    }

    // Sort the persons by last name and first name
    Collections.sort(persons, new PersonSorter());

    JsonUtils.writeToJson(persons, outputFilename);
  }

  /**
   * Sorts Person objects by Last Name and First Name, based on LDAP attributes
   */
  static class PersonSorter implements Comparator<Person> {
    @Override
    public int compare(Person person1, Person person2) {
      if (person1 == person2) {
        return 0;
      }

      if (person1 == null) {
        return -1;
      }

      if (person2 == null) {
        return 1;
      }

      String person1SortKey = (person1.get("LDAP", "sn") + person1.get("LDAP", "givenName")).toLowerCase();
      String person2SortKey = (person2.get("LDAP", "sn") + person2.get("LDAP", "givenName")).toLowerCase();

      return person1SortKey.compareTo(person2SortKey);
    }
  }

  public static Map<String, Map<String, String>> createStaffMap(List<Map<String, String>> rawStaffMap,
      String uidField) {
    Map<String, Map<String, String>> results = new HashMap<>();
    for (Map<String, String> entry : rawStaffMap) {
      String uid = entry.get(uidField);
      results.put(uid, entry);
    }
    return results;
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
