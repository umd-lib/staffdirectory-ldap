package edu.umd.lib.staffdir.excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;

public class ExcelGeneratorTest {
  private ExcelGenerator excelGenerator;

  @Before
  public void setUp() throws Exception {
    List<Map<String, String>> fieldMappings = fromCsvFile("src/test/resources/excel/allStaffFieldMappings.csv");
    List<Map<String, String>> categoryStatusAbbreviations = fromCsvFile(
        "src/test/resources/excel/categoryStatusMap.csv");
    excelGenerator = new ExcelGenerator(fieldMappings, categoryStatusAbbreviations);
  }

  @Test
  public void testGetDisplayValue_nullValues() {
    assertNull(excelGenerator.getDisplayValue(null, null));
  }

  @Test
  public void testGetDisplayValue_unknownDisplayType() {
    String displayType = "UNKNOWN_DISPLAY_TYPE";

    // Values for unknown display types are returned unchanged
    assertNull(excelGenerator.getDisplayValue(displayType, null));
    assertEquals("", excelGenerator.getDisplayValue(displayType, ""));
    assertEquals("Value", excelGenerator.getDisplayValue(displayType, "Value"));
  }

  @Test
  public void testGetDisplayValue_textDisplayType() {
    String displayType = "Text";

    // All values get returned unchanged
    assertNull(excelGenerator.getDisplayValue(displayType, null));
    assertEquals("", excelGenerator.getDisplayValue(displayType, ""));
    assertEquals("Value", excelGenerator.getDisplayValue(displayType, "Value"));
  }

  @Test
  public void testGetDisplayValue_categoryStatusDisplayType() {
    String displayType = "CategoryStatus";

    // Null/Unknown values are returned unchanged
    assertNull(excelGenerator.getDisplayValue(displayType, null));
    assertEquals("", excelGenerator.getDisplayValue(displayType, ""));
    assertEquals("NOT_A_CATEGORY", excelGenerator.getDisplayValue(displayType, "NOT_A_CATEGORY"));

    // Abbreviations get expanded
    assertEquals("Abbreviation 1", excelGenerator.getDisplayValue(displayType, "Abbr1"));
    assertEquals("Abbreviation 2", excelGenerator.getDisplayValue(displayType, "Abbr2"));
  }

  @Test
  public void testGetDisplayValue_percentageDisplayType() {
    String displayType = "Percentage";

    // Null values get returned as 100
    assertEquals("100", excelGenerator.getDisplayValue(displayType, null));

    // Everything else just passes through
    assertEquals("1.23", excelGenerator.getDisplayValue(displayType, "1.23"));
    assertEquals("ABC", excelGenerator.getDisplayValue(displayType, "ABC"));
  }

  @Test
  public void testGetDisplayValue_phoneNumberDisplayType() {
    String displayType = "PhoneNumber";

    // Null values are returned unchanged
    assertNull(excelGenerator.getDisplayValue(displayType, null));

    // Values not matching excepted patterns are returned unchanged
    assertEquals("", excelGenerator.getDisplayValue(displayType, ""));
    assertEquals("Belmont 1234", excelGenerator.getDisplayValue(displayType, "Belmont 1234"));

    // Phone numbers of the form
    // "+<country code> <area code> <exchange code> <line number>"
    // are converted to "<area code><exchange code><line number>"
    assertEquals("3014059195", excelGenerator.getDisplayValue(displayType, "+1 301 405 9195"));
  }

  private List<Map<String, String>> fromCsvFile(String filename) throws Exception {
    List<Map<String, String>> records;
    try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
      String[] headers = br.readLine().split(",");
      records = br.lines().map(s -> s.split(","))
          .map(t -> IntStream.range(0, t.length)
              .boxed()
              .collect(Collectors.toMap(i -> headers[i], i -> t[i])))
          .collect(Collectors.toList());
    }
    return records;
  }
}
