package dev.springtools.util.excel;

import org.apache.poi.ss.usermodel.*;

import java.io.InputStream;
import java.util.*;

public class ExcelReader {

    private final InputStream excel;

    public ExcelReader(InputStream excel) {
        this.excel = excel;
    }

    public List<Map<String, Object>> read() throws Exception {

        List<Map<String, Object>> rows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(excel)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();

            if (!iterator.hasNext()) {
                return rows;
            }

            // Header
            Row headerRow = iterator.next();
            List<String> headers = new ArrayList<>();

            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue().trim());
            }

            // Data rows
            while (iterator.hasNext()) {
                Row row = iterator.next();
                Map<String, Object> record = new HashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    record.put(headers.get(i), readCell(cell));
                }

                rows.add(record);
            }
        }

        return rows;
    }

    private Object readCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
