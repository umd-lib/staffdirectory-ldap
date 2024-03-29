package edu.umd.lib.staffdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import edu.umd.lib.staffdir.drupal.DrupalGenerator;
import edu.umd.lib.staffdir.google.SheetsRetriever;

/**
 * Command-line application for generating the JSON file to be sent to Drupal to
 * populate the Staff Directory.
 */
public class DrupalBuilder {
  public static final Logger log = LoggerFactory.getLogger(DrupalBuilder.class);

  public static void main(String[] args) {
    try {
      CommandLine cmdLine = parseCommandLine(args);

      String propFilename = cmdLine.getOptionValue("config");
      String inputFilename = cmdLine.getOptionValue("input");
      String outputFilename = cmdLine.getOptionValue("output");

      Properties props = getProperties(propFilename);

      // Google configuration Settings
      String appName = props.getProperty("appName");
      String serviceAccountCredentialsFile = props.getProperty("serviceAccountCredentialsFile");
      String spreadsheetDocId = props.getProperty("spreadsheetDocId");
      SheetsRetriever sr = new SheetsRetriever(appName, serviceAccountCredentialsFile);

      List<Person> jsonPersons = JsonUtils.readFromJson(inputFilename);

      List<Map<String, String>> drupalMappings = sr.toMap(spreadsheetDocId, "Drupal Mapping");

      DrupalGenerator drupalGenerator = new DrupalGenerator(drupalMappings);
      drupalGenerator.generate(outputFilename, jsonPersons);
    } catch (Exception e) {
      log.error("ERROR - An exception occurred.", e);
      // Exit with system status 1 to indicate that an error occurred.
      System.exit(1);
    }
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
    Option inputOption = Option.builder("i")
        .longOpt("input")
        .hasArg()
        .argName("input file")
        .required()
        .desc("The JSON input filename")
        .build();
    Option outputOption = Option.builder("o")
        .longOpt("output")
        .hasArg()
        .argName("output file")
        .required()
        .desc("The Drupal output filename")
        .build();
    Option configOption = Option.builder("c")
        .longOpt("config")
        .hasArg()
        .argName("properties file")
        .required()
        .desc("The properties file for containing Google credentials")
        .build();
    Option helpOption = Option.builder("h")
        .longOpt("help")
        .desc("Print this message")
        .build();

    Options options = new Options();
    options.addOption(inputOption);
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
    String header = "Generates the JSON file to send to Drupal.";
    formatter.printHelp(120, "drupal-builder", header, options, "", true);
  }
}
