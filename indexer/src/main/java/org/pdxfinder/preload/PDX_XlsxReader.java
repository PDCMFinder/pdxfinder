package org.pdxfinder.preload;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.neo4j.cypher.internal.compiler.v2_3.planner.logical.cardinality.DelegatingSelectivityEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

public class PDX_XlsxReader {

    Logger log = LoggerFactory.getLogger(PDX_XlsxReader.class);

    Optional<Workbook> xslxWorkbook;
    ArrayList<ArrayList<String>> sheetData = null;

    public ArrayList<ArrayList<String>> readFirstSheet(File xlsx) throws IOException {

        xslxWorkbook = getWorkbook(xlsx);

        if( xslxWorkbook.isPresent() ){
            sheetData = iterateThroughSheet(getSheet(xslxWorkbook));
        }
        return sheetData;
    }

    private Optional<Workbook> getWorkbook(File file) {

        if (!file.exists()) return Optional.empty();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
            return Optional.of(workbook);

        } catch (IOException e) {
            log.error("There was a problem accessing the file: {}", e);
        }
        return Optional.empty();
    }

    protected ArrayList<ArrayList<String>> iterateThroughSheet(Sheet xslxSheet) {

        ArrayList<ArrayList<String>> listOfCellLists = new ArrayList<>();

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

        for(int i = 0; i < currentRow.getLastCellNum(); i++) {

            Cell cell = (currentRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK));
            if(cellIsNotNullOrEmpty(cell))
                cellValues.add("");
            else
                cellValues.add(getString(cell));
        }
        return cellValues;
    }

    private boolean cellIsNotNullOrEmpty(Cell cell){
        if (cell.getCellType() == Cell.CELL_TYPE_BLANK) return true;
        else return false;
    }

    private String getCellValueAsString(Cell currentCell) {
        return getString(currentCell);
    }

    public static String getString(Cell currentCell) {

        String value;

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
                                    ((int) currentCell.getNumericCellValue()
                                    )
                                )
                            )
                );
                break;

            default:
                value = "";
                break;
        }
        return value;
    }

    private static String cleanFloat(String floatValue) {
        String regex = "(\\d.+)\\.[0]+";
        return floatValue.replaceAll(regex, "$1");
    }

        private static String cleanSpaces(String stringToClean) {
        return stringToClean.trim();
    }

    private Sheet getSheet(Optional<Workbook> workbook) {
        return xslxWorkbook.get().getSheetAt(0);
    }
}