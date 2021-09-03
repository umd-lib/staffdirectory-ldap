package edu.umd.lib.staffdir.drupal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.umd.lib.staffdir.Person;

/**
 * Creates a JSON file for input to Drupal
 */
public class DrupalGenerator {
  public static final Logger log = LoggerFactory.getLogger(DrupalGenerator.class);

  private Map<String, Map<String, String>> drupalFieldsToSourceFields;

  public DrupalGenerator(List<Map<String, String>> fieldMappings) {
    // Drupal fields
    String[] drupalFields = new String[fieldMappings.size()];
    for (int i = 0; i < fieldMappings.size(); i++) {
      drupalFields[i] = fieldMappings.get(i).get("Destination Field");
    }

    // Map Drupal output fields to fields in the field mappings
    drupalFieldsToSourceFields = new HashMap<>();
    for (String drupalField : drupalFields) {
      for (Map<String, String> fieldMapping : fieldMappings) {
        if (drupalField.equals(fieldMapping.get("Destination Field"))) {
          drupalFieldsToSourceFields.put(drupalField, fieldMapping);
        }
      }
    }
  }

  /**
   * Generates an JSON file from the provided information
   *
   * @param filename
   *          the filename of the Excel spreadsheet
   * @param persons
   *          the List of persons to include in the spreadsheet
   */
  public void generate(String filename, List<Person> persons)
      throws JsonMappingException, JsonGenerationException, IOException {
    try (FileOutputStream out = new FileOutputStream(filename)) {
      generate(new FileOutputStream(filename), persons);
    }
  }

  /**
   * Generates the JSON to output from the given List of Persons to the provided
   * output stream.
   *
   * @param out
   *          the OutputStream to write the output to
   * @param persons
   *          the List of Persons to output
   * @throws JsonMappingException
   * @throws JsonGenerationException
   * @throws IOException
   */
  protected void generate(OutputStream out, List<Person> persons)
      throws JsonMappingException, JsonGenerationException, IOException {
    Map<String, Map<String, String>> outputMap = new HashMap<>();

    // Data rows
    for (Person p : persons) {
      String uid = p.uid;
      Map<String, String> personMap = personToMap(p);
      outputMap.put(uid, personMap);
    }

    ObjectMapper mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter().writeValue(out, outputMap);
  }

  /**
   * Returns the String to display in the spreadsheet, based on the given value
   * and display type
   *
   * @param displayType
   *          the display type to use in formatting the value
   * @param value
   *          the value to format
   * @return the String to display in the spreadsheet
   */
  protected String getDisplayValue(String displayType, String value) {
    if (displayType == null) {
      log.warn("WARNING: Received null DisplayType. Returning value '{}' unchanged.", value);
      return value;
    }

    switch (displayType) {
    case "Text":
      return value;
    default:
      log.warn("WARNING: Unhandled DisplayType '{}'. Returning value '{}' unchanged.", displayType, value);
      return value;
    }
  }

  /**
   * Converts the given Person into the Map<String, String> to provide to
   * Drupal.
   *
   * @param p
   *          the Person to convert
   * @return a Map<String, String> representing to provide to Drupal
   */
  protected Map<String, String> personToMap(Person p) {
    Map<String, String> result = new HashMap<>();

    for (String drupalField : drupalFieldsToSourceFields.keySet()) {
      Map<String, String> fieldMapping = drupalFieldsToSourceFields.get(drupalField);
      if (fieldMapping != null) {
        String source = fieldMapping.get("Source");
        String sourceField = fieldMapping.get("Source Field");

        // Skip "Derived" source fields
        if ("Derived".equals(source)) {
          continue;
        }
        String value = p.getAllowNull(source, sourceField);
        if (value != null) {
          String displayValue = getDisplayValue(fieldMapping.get("Display Type"), value);
          result.put(drupalField, displayValue);
        }
      }
    }

    // Derived Values

    // Title
    result.put("title", getTitle(p));

    // Display Name
    result.put("display_name", String.format("%s %s", p.get("LDAP", "givenName"), p.get("LDAP", "sn")).trim());

    // Location
    result.put("location", getLocation(p));

    return result;
  }

  /**
   * Returns the "derived" title for the given Person.
   *
   * From the "Drupal Mapping" sheet of the "Online Staff Directory Mapping"
   * Google sheets document.
   *
   * @param p
   *          the Person to return the title of
   * @return the "derived" title for the given Person.
   */
  // Staff::Functional Title if not empty, otherwise umDisplayTitle;
  // if umOfficalTitle is Librarian I,II,III,IV then append ' (' +
  // umOfficialTitle + ')'
  //
  // Note: In this implementation, added the caveat the umOfficialTitle is
  // not used if it matches the "umDisplayTitle". This is so we don't get
  // titles of the form "Librarian II (Librarian II)"
  protected String getTitle(Person p) {
    String title = "";

    // Staff::Functional Title might not be in the sources, so need to check
    // for value first.
    if (p.hasValue("Staff", "Functional Title")) {
      title = p.get("Staff", "Functional Title");
    } else {
      title = p.get("LDAP", "umDisplayTitle");
    }

    String umOfficialTitle = p.get("LDAP", "umOfficialTitle");
    if (!umOfficialTitle.equals(title) && umOfficialTitle.startsWith("Librarian")) {
      title = String.format("%s (%s)", title, umOfficialTitle);
    }

    return title;
  }

  /**
   * Returns the "derived" location for the given Person.
   *
   * From the "Drupal Mapping" sheet of the "Online Staff Directory Mapping"
   * Google sheets document.
   *
   * @param p
   *          the Person to return the location of
   * @return the "derived" location for the given Person.
   */
  // umPrimaryCampusRoom + ' ' + umPrimaryCampusBuilding
  protected String getLocation(Person p) {
    String room = "";
    String building = "";

    if (p.hasValue("LDAP", "umPrimaryCampusRoom")) {
      room = p.get("LDAP", "umPrimaryCampusRoom");
    }

    if (p.hasValue("LDAP", "umPrimaryCampusBuilding")) {
      building = p.get("LDAP", "umPrimaryCampusBuilding");
    }

    // Location
    return String.format("%s %s", room, building).trim();
  }
}
