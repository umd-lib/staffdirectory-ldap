package edu.umd.lib.staffdir.grouper.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.umd.lib.staffdir.grouper.Grouper;

/**
 * Command-line application for deleting members from a Grouper group.
 *
 * This application uses "subjectId" (equivalent to the "employeeNumber"
 * field in LDAP), to delete members from the specified Grouper group.
 */
public class GrouperDeleteMemberCli {
  public static final Logger log = LoggerFactory.getLogger(GrouperDeleteMemberCli.class);

  /**
   * Called by the "grouper-delete" script (configured in th
   * "appassembler-maven-plugin" of the "pom.xml" file), to execute the command.
   *
   * @param args the command-line arguments
   */
  public static void main(String[] args) {
    try {
      CommandLine cmdLine = parseCommandLine(args);

      String propFilename = cmdLine.getOptionValue("config");
      String groupName = cmdLine.getOptionValue("groupName");
      String[] subjectIds = cmdLine.getOptionValues("subjectIds");

      Properties props = getProperties(propFilename);

      // Google configuration Settings
      String wsEndpoint = props.getProperty("grouper.url");
      String wsUser = props.getProperty("grouper.user");
      String wsPassword = props.getProperty("grouper.password");

      Grouper.Config config = new Grouper.Config(wsEndpoint, wsUser, wsPassword);
      WsDeleteMemberResults results = Grouper.deleteMembers(config, groupName, subjectIds);

      for (WsDeleteMemberResult result: results.getResults()) {
        WsResultMeta rm = result.getResultMetadata();
        System.out.println("resultCode: " + rm.getResultCode());
        System.out.println("resultCode2: " + rm.getResultCode2());
        System.out.println("resultMessage: " + rm.getResultMessage());
        System.out.println("success: " + rm.getSuccess());
        System.out.println("-----");
      }
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
    Option configOption = Option.builder("c")
        .longOpt("config")
        .hasArg()
        .argName("properties file")
        .required()
        .desc("The properties file for containing Grouper credentials")
        .build();
    Option groupNameOption = Option.builder("g")
        .longOpt("groupName")
        .hasArg()
        .argName("group name")
        .required()
        .desc("The Grouper group to delete members from")
        .build();
    Option subjectIdsOption = Option.builder("s")
        .longOpt("subjectIds")
        .hasArgs()
        .argName("subject ids")
        .required()
        .valueSeparator(',')
        .desc("A comma-separated list of one or more subject ids to delete from the group")
        .build();
    Option helpOption = Option.builder("h")
        .longOpt("help")
        .desc("Print this message")
        .build();

    Options options = new Options();
    options.addOption(configOption);
    options.addOption(groupNameOption);
    options.addOption(subjectIdsOption);
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
    String header = "Deletes one or more members from a Grouper group";
    formatter.printHelp(120, "grouper", header, options, "", true);
  }
}
