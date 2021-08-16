package edu.umd.lib.staffdir.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  /**
   * Generates an Excel spreadsheet from the provided information
   *
   * @param filename
   *          the filename of the Excel spreadsheet
   * @param persons
   *          the List of persons to include in the spreadsheet
   * @param password
   *          the password to set on the Excel spreadsheet to protect it.
   */
  public static void generate(String filename, List<Map<String, String>> fieldMappings,
      List<Person> persons, String password) {
    try (Workbook wb = new XSSFWorkbook()) {

      Sheet sheet = wb.createSheet("All Staff List");

      int rowIndex = 0;

      // Header row
      String[] columnTitles = {
          "LastName", "FirstName", "PhoneNumber", "E-mail", "Title", "RoomNo",
          "Bldg", "Division", "Department", "Unit", "Location", "Appt Fte",
          "Category Status", "Faculty Perm Status", "Descriptive Title",
          "Expr1", "CostCenter"
      };

      // Map columns in destination spreadsheet to fields in the field mappings
      Map<String, Map<String, String>> columnsToFields = new HashMap<>();
      for (String columnTitle : columnTitles) {
        for (Map<String, String> fieldMapping : fieldMappings) {
          if (columnTitle.equals(fieldMapping.get("Spreadsheet Column"))) {
            columnsToFields.put(columnTitle, fieldMapping);
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
          Map<String, String> fieldMapping = columnsToFields.get(columnTitle);
          if (fieldMapping != null) {
            String source = fieldMapping.get("Source");
            String sourceField = fieldMapping.get("Source Field");
            String value = p.getAllowNull(source, sourceField);
            if (value != null) {
              rowValues.put(columnTitle, value);
            }
          }
        }

        // Derived Values
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

          // Special handling for FTE -- shown as a percentage
          if ("Appt Fte".equals(columnTitle)) {
            String fte = (rowValues.get(columnTitle) == null) ? "100" : rowValues.get(columnTitle);
            double fteAsDouble = Double.parseDouble(fte);
            double fteAsPercent = fteAsDouble / 100.0;
            cell.setCellValue(fteAsPercent);
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

      try (OutputStream fileOut = new FileOutputStream(filename)) {
        wb.write(fileOut);
      } catch (IOException ioe) {
        log.error("I/O error writing out spreadsheet", ioe);
      }
    } catch (IOException ioe) {
      log.error("I/O error closing workbook", ioe);
    }
  }
}
