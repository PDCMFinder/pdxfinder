package org.pdxfinder.preload;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class OmicHarmonizerTests {

    private ArrayList<ArrayList<String>> testData;
    private OmicHarmonizer harmonizer = new OmicHarmonizer();
    private static final String expectedMut = "MUT";
    private static final String expectedCNA = "CNA";
    private static final String expectedUndefined = "undefined";
    private static final String UNIQUE_HEADER = "chromosome";
    private static int MUTCOLUMNSIZE = 23;
    private static int CNACOLUMNSIZE = 20;

    @Before
    public void init(){
        testData = new ArrayList<>();
    }

    @Test
    public void Given_mutSheet_When_determineDataTypeIsCalled_OmicTypeStringIsMut(){

        //given
        testData.add(fillNewList("TEST",MUTCOLUMNSIZE));

        //when
        harmonizer.setOmicSheet(testData);
        harmonizer.determineDataType();

        //then
        Assert.assertEquals(expectedMut, harmonizer.getOmicType());
    }

    @Test
    public void Given_CNASheet_When_determineDataTypeIsCalled_Then_OmicTypeStringIsCNA(){

        //given
        testData.add(fillNewList("TEST",CNACOLUMNSIZE));

        //when
        harmonizer.setOmicSheet(testData);
        harmonizer.determineDataType();

        //then
        Assert.assertEquals(expectedCNA, harmonizer.getOmicType());
    }

    @Test
    public void Given_UndefinedColumnSize_When_determineDataTypeIsCalled_Then_OmicTypeStringIsCNA(){

        //given
        testData.add(fillNewList("TEST",15));

        //when
        harmonizer.setOmicSheet(testData);
        harmonizer.determineDataType();

        //then
        Assert.assertEquals(expectedUndefined, harmonizer.getOmicType());
    }

    @Test
    public void Given_Headers_When_getHeadersIsCalled_Then_returnFirstRow(){

        //Given
        testData.add(fillNewList("HEADER",MUTCOLUMNSIZE));
        testData.add(fillNewList("TEST",MUTCOLUMNSIZE));

        //when
        harmonizer.setOmicSheet(testData);
        ArrayList<String> actualList = harmonizer.getHeaders();

        //Then
        Assert.assertEquals(actualList, testData.get(0));
    }

    @Test
    public void Given_Headers_When_getHeadersIsCalled_Then_returnCorrectColumnNumber(){

        int expectedCol = 7;

        //given
        ArrayList<String> headers = fillNewList("HEADER",MUTCOLUMNSIZE);
        headers.set(expectedCol, UNIQUE_HEADER);
        testData.add(headers);
        testData.add(fillNewList("TEST",MUTCOLUMNSIZE));

        //when
        harmonizer.setOmicSheet(testData);
        int actualCol = harmonizer.getColumnByHeader(UNIQUE_HEADER);

        Assert.assertEquals(expectedCol,actualCol);
    }

    private ArrayList<String> fillNewList(String filler, int len){

        ArrayList<String> newList = new ArrayList<>();
        for(int i = 0; i < len; i++){
            newList.add("TEST");
        }
        return newList;
    }


}
