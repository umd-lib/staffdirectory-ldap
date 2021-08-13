package edu.umd.lib.staffdir.google;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class OrganizationsTest {
  private List<Map<String, String>> rawOrganizationsList;

  @Before
  public void setUp() {
    // Populated raw organizations list
    rawOrganizationsList = new ArrayList<>();

    String[] headers = {
        "Cost Center", "Division Code", "Division", "Department", "Unit", "Location"
    };

    String[][] orgs = {
        { "030000", "DO", "Dean's Office", "", "", "Dean's Office" },
        { "031800", "", "", "Development Office", "", "Development Office" },
        { "032200", "", "", "Communications" },
        { "032211", "", "", "", "Graphics", "Graphics" },
    };

    for (String[] org : orgs) {
      Map<String, String> orgMap = new HashMap<>();
      int i = 0;
      for (String s : org) {
        orgMap.put(headers[i], s);
        i++;
      }
      rawOrganizationsList.add(orgMap);
    }
  }

  @Test
  public void testGetCostCentersMap() {
    Map<String, Map<String, String>> costCentersMap = null;

    // Null list
    costCentersMap = Organizations.getCostCentersMap(null, "Cost Center");
    assertTrue(costCentersMap.isEmpty());

    // Empty list
    costCentersMap = Organizations.getCostCentersMap(new ArrayList<Map<String, String>>(), "Cost Center");
    assertTrue(costCentersMap.isEmpty());

    // Populated list
    costCentersMap = Organizations.getCostCentersMap(rawOrganizationsList, "Cost Center");
    Map<String, String> entry;

    assertTrue(costCentersMap.containsKey("030000"));
    entry = costCentersMap.get("030000");
    assertEquals("030000", entry.get("Cost Center"));
    assertEquals("DO", entry.get("Division Code"));
    assertEquals("Dean's Office", entry.get("Division"));
    assertEquals("", entry.get("Department"));
    assertEquals("", entry.get("Unit"));
    assertEquals("Dean's Office", entry.get("Location"));

    // Department should have division code and division filled in
    assertTrue(costCentersMap.containsKey("031800"));
    entry = costCentersMap.get("031800");
    assertEquals("031800", entry.get("Cost Center"));
    assertEquals("DO", entry.get("Division Code"));
    assertEquals("Dean's Office", entry.get("Division"));
    assertEquals("Development Office", entry.get("Department"));
    assertEquals("", entry.get("Unit"));
    assertEquals("Development Office", entry.get("Location"));
  }

  @Test
  public void testCostCenterType() {
    assertEquals(Organizations.Type.DIVISION, Organizations.getType("030000"));
    assertEquals(Organizations.Type.DEPARTMENT, Organizations.getType("031800"));
    assertEquals(Organizations.Type.UNIT, Organizations.getType("032211"));
  }

  @Test
  public void testPopulateEntry() {
    Map<String, Map<String, String>> costCentersMap = Organizations.getCostCentersMap(rawOrganizationsList,
        "Cost Center");
    Map<String, String> entry;

    entry = Organizations.populateEntry("030000", costCentersMap);
    assertEquals("030000", entry.get("Cost Center"));
    assertEquals("DO", entry.get("Division Code"));
    assertEquals("Dean's Office", entry.get("Division"));
    assertEquals("", entry.get("Department"));
    assertEquals("", entry.get("Unit"));
    assertEquals("Dean's Office", entry.get("Location"));

    entry = Organizations.populateEntry("031800", costCentersMap);
    assertEquals("031800", entry.get("Cost Center"));
    assertEquals("DO", entry.get("Division Code"));
    assertEquals("Dean's Office", entry.get("Division"));
    assertEquals("Development Office", entry.get("Department"));
    assertEquals("", entry.get("Unit"));
    assertEquals("Development Office", entry.get("Location"));

    entry = Organizations.populateEntry("032211", costCentersMap);
    assertEquals("032211", entry.get("Cost Center"));
    assertEquals("DO", entry.get("Division Code"));
    assertEquals("Dean's Office", entry.get("Division"));
    assertEquals("Communications", entry.get("Department"));
    assertEquals("Graphics", entry.get("Unit"));
    assertEquals("Graphics", entry.get("Location"));

  }

  @Test
  public void testGetDivisionCostCenter() {
    assertEquals("030000", Organizations.getDivisionCostCenter("030000"));
    assertEquals("030000", Organizations.getDivisionCostCenter("031800"));
    assertEquals("030000", Organizations.getDivisionCostCenter("032200"));
    assertEquals("030000", Organizations.getDivisionCostCenter("032211"));
  }

  @Test
  public void testGetDepartmentCostCenter() {
    assertEquals("030000", Organizations.getDepartmentCostCenter("030000"));
    assertEquals("031800", Organizations.getDepartmentCostCenter("031800"));
    assertEquals("032200", Organizations.getDepartmentCostCenter("032200"));
    assertEquals("032200", Organizations.getDepartmentCostCenter("032211"));
  }

}
