package edu.umd.lib.staffdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import edu.umd.lib.staffdir.excel.ExcelGenerator;
import edu.umd.lib.staffdir.ldap.Ldap;

/**
 * Application entrypoint for generating an Excel spreadsheet from LDAP
 */
public class Main {
  public static final Logger log = LoggerFactory.getLogger(Main.class);

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {
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

    String propFilename = cmdLine.getOptionValue("config");
    String locationsFilename = cmdLine.getOptionValue("locations");
    String outputFilename = cmdLine.getOptionValue("output");

    File propFile = new File(propFilename);
    File locationsFile = new File(locationsFilename);

    if (!propFile.exists()) {
      System.err.println("ERROR: Properties file '" + propFilename + "' cannot be found or accessed.");
      System.exit(1);
    }

    if (!locationsFile.exists()) {
      System.err.println("ERROR: Locations mapping file '" + locationsFilename + "' cannot be found or accessed.");
      System.exit(1);
    }

    Properties props = null;
    try {
      FileInputStream propFileIn = new FileInputStream(propFile);
      props = new Properties(System.getProperties());
      props.load(propFileIn);
    } catch (IOException ioe) {
      System.out.println(ioe);
      System.exit(1);
    }

    Map<String, String> locations = null;
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    try {
      locations = mapper.readValue(locationsFile, Map.class);
    } catch (IOException ioe) {
      log.error("Could not parse locations map", ioe);
    }

    String ldapUrl = props.getProperty("ldap.url");
    String authentication = props.getProperty("ldap.authentication");
    String bindDn = props.getProperty("ldap.bindDn");
    String credentials = props.getProperty("ldap.credentials");
    String searchBaseDn = props.getProperty("ldap.searchBaseDn");

    List<Person> persons = Ldap.ldapSearch(ldapUrl, authentication, bindDn, credentials, searchBaseDn);

    // Sort the persons by last name
    Collections.sort(persons,
        (Person p1, Person p2) -> p1.getLastName().toLowerCase().compareTo(p2.getLastName().toLowerCase()));

    ExcelGenerator.generate(outputFilename, persons, "abcd", locations);
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
    Option locationsOption = Option.builder("l")
        .longOpt("locations")
        .hasArg()
        .argName("locations YAML file")
        .required()
        .desc("The YAML file containing the cost center to locations mapping")
        .build();
    Option helpOption = Option.builder("h")
        .longOpt("help")
        .desc("Print this message")
        .build();

    Options options = new Options();
    options.addOption(outputOption);
    options.addOption(configOption);
    options.addOption(locationsOption);
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
