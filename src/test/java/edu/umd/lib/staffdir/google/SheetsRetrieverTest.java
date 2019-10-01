package edu.umd.lib.staffdir.google;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.api.services.sheets.v4.model.ValueRange;

public class SheetsRetrieverTest {
  @Test
  public void testMembershipProcessor() {
    SheetsRetriever sr = new SheetsRetriever("staffdirectory-ldap-test", "not_needed.json");

    // null ValueRange
    List<Map<String, String>> results = sr.toMap(null);
    assertTrue(results.isEmpty());

    // empty ValueRange
    ValueRange emptyVr = new ValueRange();
    results = sr.toMap(emptyVr);
    assertTrue(results.isEmpty());

    // populated ValueRange
    ValueRange vr = new ValueRange();
    List<List<Object>> values = new ArrayList<>();
    List<Object> headerRow = new ArrayList<>(Arrays.asList("Header 1", "Header 2", "Header 3"));
    List<Object> row1 = new ArrayList<>(Arrays.asList("Row 1 Field 1", "Row 1 Field 2", "Row 1 Field 3"));
    List<Object> row2 = new ArrayList<>(Arrays.asList("Row 2 Field 1", "Row 2 Field 2", "Row 2 Field 3"));
    values.add(headerRow);
    values.add(row1);
    values.add(row2);
    vr.setValues(values);

    results = sr.toMap(vr);
    assertFalse(results.isEmpty());
    int expectedSize = values.size() - 1; // Do not count header row
    assertEquals(expectedSize, results.size());
    assertEquals("Row 1 Field 1", results.get(0).get("Header 1"));
    assertEquals("Row 1 Field 2", results.get(0).get("Header 2"));
    assertEquals("Row 1 Field 3", results.get(0).get("Header 3"));
    assertEquals("Row 2 Field 1", results.get(1).get("Header 1"));
    assertEquals("Row 2 Field 2", results.get(1).get("Header 2"));
    assertEquals("Row 2 Field 3", results.get(1).get("Header 3"));

  }
}
