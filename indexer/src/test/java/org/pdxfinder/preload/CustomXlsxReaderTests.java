package org.pdxfinder.preload;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

public class CustomXlsxReaderTests {

    private Workbook workbook;
    private Sheet sheet;

    private Row secondDataRow;
    private Row thirdDataRow;

    private String expectedString = "9999";
    private String multipleSpaces = "    ";
    private String blank = "";

    int expectedSize = 13;
    private int expectedInteger = 9999;

    PDX_XlsxReader xlsxReader = new PDX_XlsxReader();

    @Before
    public void init(){

        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("TEST");
        sheet.createRow(0);
        secondDataRow = sheet.createRow(1);
        thirdDataRow = sheet.createRow(2);
    }

    @Test
    public void Given_StringsAndInt_When_IterateThroughSheetIsCalled_Then_returnAllSheetsOfData(){

        //given init()
        fillRowWithCharacters(secondDataRow, expectedString);
        fillRowWithIntegers(thirdDataRow, expectedInteger);

        //When
        ArrayList<ArrayList<String>> actualList = xlsxReader.iterateThroughSheet(sheet);

        //Then
        Assert.assertEquals(3,actualList.size());

        List<String> actualSecondRow = actualList.get(1);
        List<String> actualThirdRow = actualList.get(2);

        AssertEachElementEqualsString(actualSecondRow, expectedString);

        actualThirdRow.forEach(c ->
                Assert.assertEquals(expectedInteger, Integer.parseInt(c)));
    }

    @Test
    public void Given_SheetWithMultipleSpacesInCell_When_IterateThroughSheetIsCalled_Then_returnCleanToBlankStrings(){

        //given init()
        fillRowWithCharacters(secondDataRow, multipleSpaces);
        //When
        ArrayList<ArrayList<String>> actualList = xlsxReader.iterateThroughSheet(sheet);

        //Then
        AssertSizeAndEachStringIsBlank(actualList);
    }

    @Test
    public void Given_nullCells_When_IterateThroughSheetIsCalled_Then_returnCorrectCellCount(){

        //given init()
        fillRowWithCharacters(secondDataRow, null);

        //When
        ArrayList<ArrayList<String>> actualList = xlsxReader.iterateThroughSheet(sheet);

        //Then
        Assert.assertEquals(expectedSize, actualList.get(1).size());
    }

    private void fillRowWithCharacters(Row row, String fill){

        for(int i = 0; i < expectedSize; i++){
            Cell newCell = row.createCell(i);
            newCell.setCellValue(fill);
        }
    }

    private void fillRowWithIntegers(Row row, int fill){

        for(int i = 0; i < expectedSize; i++){
            Cell newCell = row.createCell(i);
            newCell.setCellValue(fill);
        }
    }

    private void AssertSizeAndEachStringIsBlank(ArrayList<ArrayList<String>> actualList){
        Assert.assertEquals(3,actualList.size());
        List<String> actualSecondRow = actualList.get(1);
        AssertEachElementEqualsString(actualSecondRow, blank);
    }

    private void AssertEachElementEqualsString(List<String> elements, String expected){
        elements.forEach(c ->
                Assert.assertEquals(expected,c));
    }

}

