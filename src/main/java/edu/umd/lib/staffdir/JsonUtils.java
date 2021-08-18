package edu.umd.lib.staffdir;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
  public static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

  public static void writeToJson(List<Person> persons, String jsonFilename) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(persons);
      try (PrintWriter out = new PrintWriter(new FileWriter(jsonFilename))) {
        out.println(json);
      }
    } catch (IOException ioe) {
      log.error("ERROR: Writing JSON to '{}'", jsonFilename, ioe);
    }
  }

  public static List<Person> readFromJson(String jsonFilename) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      List<Person> persons = objectMapper.readValue(
          Paths.get(jsonFilename).toFile(),
          new TypeReference<List<Person>>() {
          });
      return persons;
    } catch (IOException ioe) {
      log.error("ERROR: Reading JSON from '{}'", jsonFilename, ioe);
    }
    return null;
  }
}
