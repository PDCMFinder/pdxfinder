package org.pdxfinder.preload;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomXlsxReader {

    Logger log = LoggerFactory.getLogger(CustomXlsxReader.class);

    Optional<Workbook> xslxSheet;
    List<List<String>> sheetData = null;

    public List<List<String>> readFirstSheet(File xlsx) throws IOException {

        xslxSheet = getWorkbook(xlsx);

        if( xslxSheet.isPresent() ){

            sheetData = iterateThroughSheet(getSheet(xslxSheet));
        }

        return sheetData;
    }

    public List<List<String>> iterateThroughSheet(Sheet xslxSheet) {

        List<List<String>> listOfCellLists = new ArrayList<>();

        Iterator<Row> iterator = xslxSheet.iterator();
        int rowCounter = 0;

        iterator.hasNext();

        while (iterator.hasNext()) {

            Row currentRow = iterator.next();

            ArrayList<String> cellValues = getCellValues(currentRow);

            listOfCellLists.add(cellValues);

            rowCounter++;
        }

        return listOfCellLists;
    }

    private ArrayList<String> getCellValues(Row currentRow){

        Iterator<Cell> cellIterator = currentRow.cellIterator();
        ArrayList<String> cellValues = new ArrayList<>();

        cellIterator.forEachRemaining(c -> {

            cellValues.add(
                    getCellValueAsString(c)
            );

        });

        return cellValues;
    }

    private String getCellValueAsString(Cell currentCell) {

        String value = "";

        value = getString(currentCell, value);

        return value;
    }

    public static String getString(Cell currentCell, String value) {

        switch (currentCell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                value = cleanSpaces(
                            currentCell.getStringCellValue(
                            )
                );

                break;

            case Cell.CELL_TYPE_NUMERIC:
                value = cleanFloat(
                            cleanSpaces(
                                (String.valueOf
                                    (currentCell.getNumericCellValue()
                                    )
                                )
                            )
                );
                break;
        }
        return value;
    }

    private static String cleanSpaces(String stringToClean) {
        return stringToClean.trim();
    }

    private static String cleanFloat(String floatValue) {
        String regex = "(\\d.+)\\.\\d";
        return floatValue.replaceAll(regex, "$1");
    }

    public Optional<Workbook> getWorkbook(File file) {

        if (!file.exists()) return Optional.empty();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            return Optional.of(workbook);

        } catch (IOException e) {
            log.error("There was a problem accessing the file: {}", e);
        }
        return Optional.empty();
    }

    private Sheet getSheet(Optional<Workbook> workbook) {
        return xslxSheet.get().getSheetAt(1);
    }
}
