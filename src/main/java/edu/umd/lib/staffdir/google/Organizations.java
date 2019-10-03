package edu.umd.lib.staffdir.google;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes a "raw" organization List returned from the Google Sheets document
 * by populating any missing values from parent cost centers.
 * <p>
 * This class is needed because the spreadsheet information is "sparse", i.e.
 * some information (such as the Division for a department cost center) is not
 * repeated on every row.
 */
public class Organizations {
  /**
   * The list of organization types
   */
  enum Type {
  DIVISION, DEPARTMENT, UNIT
  }

  /**
   * Returns Map of organizations, using the cost center as the key.
   *
   * @param rawOrganizationsList
   *          a List of Maps containing the spreadsheet information for
   *          organizations
   * @param costCenterField
   *          the String containing the map key to use for the cost center.
   * @return
   */
  public static Map<String, Map<String, String>> getCostCentersMap(
      List<Map<String, String>> rawOrganizationsList, String costCenterField) {
    Map<String, Map<String, String>> costCentersMap = new HashMap<>();

    if (rawOrganizationsList == null) {
      return costCentersMap;
    }

    for (Map<String, String> entry : rawOrganizationsList) {
      String costCenter = entry.get(costCenterField);
      costCentersMap.put(costCenter, entry);
    }

    for (String costCenter : costCentersMap.keySet()) {
      Map<String, String> m = populateEntry(costCenter, costCentersMap);
      costCentersMap.put(costCenter, m);
    }

    return costCentersMap;
  }

  /**
   * Populates entries with information from their parent cost centers.
   * <p>
   * For example, a department entry is populated with the division code and
   * division name. A unit is populated with the division code, division name,
   * and department name.
   * <p>
   * This is needed because the spreadsheet is "sparse", it does not include all
   * values for all entries.
   *
   * @param costCenter
   *          the cost center to populate
   * @param costCentersMap
   *          a Map of cost centers
   * @return a Map for the cost center, populated with parent cost center
   *         information.
   */
  protected static Map<String, String> populateEntry(String costCenter,
      Map<String, Map<String, String>> costCentersMap) {
    Map<String, String> entry = new HashMap<>(costCentersMap.get(costCenter));

    Type costCenterType = getType(costCenter);
    if (costCenterType == Type.DIVISION) {
      return entry;
    }

    if ((costCenterType == Type.DEPARTMENT) || (costCenterType == Type.UNIT)) {
      String divisionCostCenter = getDivisionCostCenter(costCenter);
      Map<String, String> divisionEntry = costCentersMap.get(divisionCostCenter);
      entry.put("Division Code", divisionEntry.get("Division Code"));
      entry.put("Division", divisionEntry.get("Division"));

    }

    if (costCenterType == Type.UNIT) {
      String departmentCostCenter = getDepartmentCostCenter(costCenter);
      Map<String, String> departmentEntry = costCentersMap.get(departmentCostCenter);
      entry.put("Department", departmentEntry.get("Department"));
    }

    return entry;
  }

  /**
   * Returns the Organization.TYPE for the given 6-digit cost center code.
   *
   * @param costCenter
   *          the cost center code to return the type of.
   * @return the Organization.TYPE for the given 6-digit cost center code.
   */
  protected static Organizations.Type getType(String costCenter) {
    String departmentCode = costCenter.substring(2, 4);
    String unitCode = costCenter.substring(4, 6);

    if (!"00".equals(unitCode)) {
      return Type.UNIT;
    } else if (!"00".equals(departmentCode)) {
      return Type.DEPARTMENT;
    }

    return Type.DIVISION;
  }

  /**
   * Returns the division cost center associated with the given cost center;
   *
   * @param costCenter
   *          the cost center to retrieve the division cost center of
   * @return the division cost center associated with the given cost center
   */
  protected static String getDivisionCostCenter(String costCenter) {
    String divisionCode = costCenter.substring(0, 2);
    return divisionCode + "0000";
  }

  /**
   * Returns the department cost center associated with the given cost center;
   *
   * @param costCenter
   *          the cost center to retrieve the division cost center of
   * @return the department cost center associated with the given cost center
   */
  protected static String getDepartmentCostCenter(String costCenter) {
    String departmentCode = costCenter.substring(0, 4);
    return departmentCode + "00";
  }

}
