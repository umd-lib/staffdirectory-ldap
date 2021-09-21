package edu.umd.lib.staffdir;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

/**
 * Utility methods to support tests
 */
public class TestUtils {
  /**
   * Converts a CSV file into a List of Map<String, String>.
   *
   * The first line is assumed to be a header line, used to generate the keys in
   * the Maps.
   *
   * @param filename
   *          the filename of the CSV file.
   * @return a List of Map<String, String> representing the records in the given
   *         CSV file.
   * @throws Exception
   *           if an Exception occurs
   */
  public static List<Map<String, String>> fromCsvFile(String filename) throws Exception {
    List<Map<String, String>> records = new ArrayList<>();
    try (Reader in = new FileReader(filename)) {
      Iterable<CSVRecord> csvRecords = CSVFormat.Builder.create(CSVFormat.EXCEL).setHeader().build().parse(in);

      for (CSVRecord csvRecord : csvRecords) {
        records.add(csvRecord.toMap());
      }
    }

    return records;
  }
}
