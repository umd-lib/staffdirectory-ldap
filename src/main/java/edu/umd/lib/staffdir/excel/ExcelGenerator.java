package edu.umd.lib.staffdir.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

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

public class ExcelGenerator {
  public static final Logger log = LoggerFactory.getLogger(ExcelGenerator.class);

  public static void generate(String filename, List<Person> persons, String password) {
    try (Workbook wb = new XSSFWorkbook()) {

      Sheet sheet = wb.createSheet("All Staff List");

      int rowIndex = 0;

      // Header row
      String[] columnTitles = {
          "LastName", "FirstName", "PhoneNumber", "E-mail", "Title", "RoomNo",
          "Bldg", "Division", "Department", "Unit", "Location", "Appy Fte",
          "Category Status", "Faculty Perm Status", "Descriptive Title",
          "Expr1", "CostCenter"
      };

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

      // Data rows
      for (Person p : persons) {
        row = sheet.createRow(rowIndex);

        String fte = (p.getFte() == null) ? "100.00%" : (p.getFte() + "%");
        String categoryStatuses = null;
        List<String> categoryStatusesList = p.getCategoryStatuses();
        if (categoryStatusesList != null) {
          categoryStatuses = categoryStatusesList.toString();
        }

        String facultyPermStatus = p.isFacultyPermanentStatus() ? "P" : "";
        String expr1 = String.format("%s %s <%s>", p.getFirstName(), p.getLastName(), p.getEmail());

        String[] rowValues = {
            p.getLastName(),
            p.getFirstName(),
            p.getPhoneNumber(),
            p.getEmail(),
            p.getOfficialTitle(),
            p.getRoomNumber(),
            p.getBuilding(),
            p.getDivision(),
            p.getDepartment(),
            p.getUnit(),
            p.getLocation(),
            fte,
            categoryStatuses,
            facultyPermStatus,
            p.getDescriptiveTitle(),
            expr1,
            p.getCostCenter()
        };

        for (int colIndex = 0; colIndex < rowValues.length; colIndex++) {
          String value = rowValues[colIndex];

          Cell cell = row.createCell(colIndex);
          cell.setCellValue(value);
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
