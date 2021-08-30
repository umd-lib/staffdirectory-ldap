package edu.umd.lib.staffdir.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IgnoredErrorType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.lib.staffdir.Person;

/**
 * Creates an Excel spreadsheet from a List of Persons
 */
public class ExcelGenerator {
  public static final Logger log = LoggerFactory.getLogger(ExcelGenerator.class);

  private List<Map<String, String>> fieldMappings;
  private Map<String, String> categoryStatusMap;

  public ExcelGenerator(
      List<Map<String, String>> fieldMappings,
      List<Map<String, String>> categoryStatusAbbreviations) {
    this.fieldMappings = fieldMappings;

    categoryStatusMap = new HashMap<>();
    for (Map<String, String> categoryStatus : categoryStatusAbbreviations) {
      categoryStatusMap.put(categoryStatus.get("Abbreviation"), categoryStatus.get("Full Text"));
    }
  }

  /**
   * Generates an Excel spreadsheet from the provided information
   *
   * @param filename
   *          the filename of the Excel spreadsheet
   * @param persons
   *          the List of persons to include in the spreadsheet
   * @param password
   *          the password to set on the Excel spreadsheet to protect it, or
   *          null for no password
   */
  public void generate(String filename, List<Person> persons, String password) {
    try (Workbook wb = new XSSFWorkbook()) {

      Sheet sheet = wb.createSheet("All Staff List");

      int rowIndex = 0;

      // Header row
      String[] columnTitles = new String[fieldMappings.size()];
      for (int i = 0; i < fieldMappings.size(); i++) {
        columnTitles[i] = fieldMappings.get(i).get("Destination Field");
      }

      // Map columns in destination spreadsheet to fields in the field mappings
      Map<String, Map<String, String>> columnTitlesToSourceFields = new HashMap<>();
      for (String columnTitle : columnTitles) {
        for (Map<String, String> fieldMapping : fieldMappings) {
          if (columnTitle.equals(fieldMapping.get("Destination Field"))) {
            columnTitlesToSourceFields.put(columnTitle, fieldMapping);
          }
        }
      }

      // Gray background
      CellStyle style = wb.createCellStyle();
      style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
      style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

      Font font = wb.createFont();
      font.setBold(true);
      style.setFont(font);
      Row row = sheet.createRow(rowIndex);

      for (int colIndex = 0; colIndex < columnTitles.length; colIndex++) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(columnTitles[colIndex]);
        style.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(style);
      }

      // Freeze the first row, so that it is always displayed
      sheet.createFreezePane(0, 1);

      rowIndex++;

      // Percentage style
      CellStyle percentageStyle = wb.createCellStyle();
      percentageStyle.setDataFormat(wb.createDataFormat().getFormat("0.00%"));

      // Data rows
      for (Person p : persons) {
        row = sheet.createRow(rowIndex);

        Map<String, String> rowValues = new HashMap<>();

        for (String columnTitle : columnTitles) {
          Map<String, String> fieldMapping = columnTitlesToSourceFields.get(columnTitle);
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
              rowValues.put(columnTitle, displayValue);
            }
          }
        }

        // Derived Values

        // Descriptive Title
        String descriptiveTitle = p.getAllowNull("Staff", "Functional Title");
        if ((descriptiveTitle == null) || descriptiveTitle.isEmpty()) {
          descriptiveTitle = p.get("LDAP", "umDisplayTitle");
        }
        rowValues.put("Descriptive Title", descriptiveTitle);

        // Expr1
        String expr1 = String.format("%s %s <%s>",
            p.get("LDAP", "givenName"),
            p.get("LDAP", "sn"),
            p.get("LDAP", "mail"));

        rowValues.put("Expr1", expr1);

        for (int colIndex = 0; colIndex < columnTitles.length; colIndex++) {
          String columnTitle = columnTitles[colIndex];
          String value = rowValues.get(columnTitle);

          Cell cell = row.createCell(colIndex);
          cell.setCellValue(value);

          Map<String, String> fieldMapping = columnTitlesToSourceFields.get(columnTitle);

          // Special handling for "Percentage" display types
          if ("Percentage".equals(fieldMapping.get("Display Type"))) {
            String percentageValue = getDisplayValue("Percentage", rowValues.get(columnTitle));
            try {
              double percentageValueAsDouble = Double.parseDouble(percentageValue);
              double valueAsPercent = percentageValueAsDouble / 100.0;
              cell.setCellValue(valueAsPercent);
            } catch (NumberFormatException nfe) {
              log.warn("WARNING: Unable to parse '{}' as a percentage for '{}'. Setting field to empty.",
                  percentageValue, p.uid);
              cell.setCellValue("");
            }
            cell.setCellStyle(percentageStyle);
          }
        }

        rowIndex++;
      }

      int numColumns = columnTitles.length;
      int numRows = rowIndex;
      for (int columnIndex = 0; columnIndex < numColumns; columnIndex++) {
        sheet.autoSizeColumn(columnIndex);
      }

      if (password != null) {
        sheet.protectSheet(password);
      }

      // Suppress the "Number Stored as Text" hint
      if (sheet instanceof XSSFSheet) {
        CellRangeAddress allCells = new CellRangeAddress(0, numRows, 0, numColumns);

        ((XSSFSheet) sheet).addIgnoredErrors(allCells, IgnoredErrorType.NUMBER_STORED_AS_TEXT);
      }

      // Turn on Autofiltering in each of the column headers
      sheet.setAutoFilter(new CellRangeAddress(0, 0, 0, numColumns - 1));

      try (OutputStream fileOut = new FileOutputStream(filename)) {
        wb.write(fileOut);
      } catch (IOException ioe) {
        log.error("ERROR: I/O error writing out spreadsheet", ioe);
      }
    } catch (

    IOException ioe) {
      log.error("ERROR: I/O error closing workbook", ioe);
    }
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
    case "CategoryStatus":
      return categoryStatusMap.getOrDefault(value, value);
    case "PhoneNumber":
      return parsePhoneNumber(value);
    case "Percentage":
      // Null values are assumed to be 100%
      if (value == null) {
        return "100";
      }
      // Everything else just passes through
      return value;
    case "Text":
      return value;
    default:
      log.warn("WARNING: Unhandled DisplayType '{}'. Returning value '{}' unchanged.", displayType, value);
      return value;
    }
  }

  /**
   * Parses a value matching the expected phone number pattern to the pattern
   * expected by the spreadsheet and returns. All non-matching values are
   * returned unchanged.
   *
   * @param value
   *          the value to parse
   * @return the parsed phone number, if it matched the expected pattern, or the
   *         unchanged value if it does not match.
   */
  protected String parsePhoneNumber(String value) {
    Pattern phoneNumberPattern = Pattern.compile(
        ".*(?<country>\\+\\w+)\\W+(?<areacode>\\w+)\\W(?<exchange>\\w+)\\W(?<line>\\w+).*");

    if (value == null) {
      return null;
    }

    Matcher m = phoneNumberPattern.matcher(value);
    if (m.matches()) {
      String parsedValue = String.format("%s%s%s", m.group("areacode"), m.group("exchange"), m.group("line"));
      return parsedValue;
    }
    return value;
  }
}
