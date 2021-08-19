package edu.umd.lib.staffdir;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class to read/write JSON
 */
public class JsonUtils {
  public static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

  /**
   * Converts the given List of Persons to a JSON file
   *
   * @param persons
   *          the List of Persons to output as JSON
   * @param jsonFilename
   *          the filename to write to
   */
  public static void writeToJson(List<Person> persons, String jsonFilename) {
    try (PrintWriter out = new PrintWriter(new FileWriter(jsonFilename))) {
      writeToJson(persons, out);
    } catch (IOException ioe) {
      log.error("ERROR: Writing JSON to '{}'", jsonFilename, ioe);
    }
  }

  /**
   * Converts the given List of Persons to JSON and outputs to the given
   * PrintWriter.
   *
   * @param persons
   *          the List of Persons to output as JSON
   * @param out
   *          the PrintWriter to use for output
   * @throws IOException
   *           if an I/O error occurs
   */
  public static void writeToJson(List<Person> persons, PrintWriter out) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(persons);
    out.println(json);
  }

  /**
   * Returns a List of Persons parsed from the given JSON file, or null if an
   * error occurs.
   *
   * @param jsonFilename
   *          the filename containing the JSON to parse
   * @return a List of Persons parsed from the given JSON file, or null if an
   *         error occurs.
   */
  public static List<Person> readFromJson(String jsonFilename) {
    try (InputStream in = new FileInputStream(jsonFilename)) {
      return readFromJson(in);
    } catch (IOException ioe) {
      log.error("ERROR: Reading JSON from '{}'", jsonFilename, ioe);
    }
    return null;
  }

  /**
   * Returns a List of Persons parsed from JSON on the given InputStream
   *
   * @param in
   *          the InputStream containing the JSON input
   * @return a List of Persons parsed from JSON
   * @throws IOException
   *           is an I/O error occurs.
   */
  public static List<Person> readFromJson(InputStream in) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    List<Person> persons = objectMapper.readValue(in,
        new TypeReference<List<Person>>() {
        });
    return persons;
  }
}
