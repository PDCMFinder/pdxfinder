package org.pdxfinder.preload;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class OmicHarmonizerTests {

    private ArrayList<ArrayList<String>> testData;
    private OmicHarmonizer harmonizer = new OmicHarmonizer();
    private static final String expectedMut = "MUT";
    private static final String expectedCNA = "CNA";
    private static final String expectedUndefined = "undefined";
    private static final String UNIQUE_HEADER = "chromosome";
    private static final String assemblyHeader = "genome_assembly";
    private static final String chromoHeader = "chromosome";
    private static final String seqStartHeader = "seq_start_position";
    private static final String CNAseqEndHeader = "seq_end_position";

    private static final int ERROR = -1;
    private static final int MUTCOLUMNSIZE = 23;
    private static final int CNACOLUMNSIZE = 20;
    private static final String CHAINFILE = "/home/afollette/IdeaProjects/pdxFinder/indexer/src/main/resources/LiftOverResources/hg19ToHg38.over.chain.gz";


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
    public void Given_UndefinedColumnSize_When_determineDataTypeIsCalled_Then_OmicTypeIsUndefined(){

        //given
        testData.add(fillNewList("TEST",15));

        //when
        harmonizer.setOmicSheet(testData);
        harmonizer.determineDataType();

        //then
        Assert.assertEquals(expectedUndefined, harmonizer.getOmicType());
    }

    @Test
    public void Given_Headers_When_getHeadersIsCalled_Then_returnHeadersRow(){

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

    @Test
    public void Given_Headers_When_valueCannotBeFound_Then_returnBlanks(){

        //Given
        testData.add(fillNewList("HEADER",MUTCOLUMNSIZE));
        testData.add(fillNewList("TEST",MUTCOLUMNSIZE));

        //when
        harmonizer.setOmicSheet(testData);
        int actualCol = harmonizer.getColumnByHeader(UNIQUE_HEADER);

        Assert.assertEquals(ERROR,actualCol);
    }

    @Test
    public void Given_OmicSheet_When_AssemblyIsBlank_Then_returnBlankSheet() throws IOException {

        String assembly = "genome_assembly";
        int expectedCol = 18;

        //Given
        ArrayList<String> headers = fillNewList("HEADER",MUTCOLUMNSIZE);
        headers.set(expectedCol,assembly);
        testData.add(headers);
        ArrayList<String> firstDataRow = (fillNewList("TEST",MUTCOLUMNSIZE));
        firstDataRow.set(expectedCol, "");
        testData.add(firstDataRow);

        //When
        harmonizer.setOmicSheet(testData);
        int actualSize = harmonizer.runLiftOver(CHAINFILE).size();

        Assert.assertEquals(0, actualSize);
    }

    @Test
    public void Given_OmicSheet_When_AssemblyIs37_Then_returnAddRowToOutputWithCorrectSize() throws IOException {

        String assembly = "genome_assembly";
        String chromo = "chromosome";
        String seqStart = "seq_start_position";
        int expectedCol = 18;
        int chromoCol = 16;
        int seqStartCol = 17;

        //Given
        createHeadersWithAssemChromoAndStartSeq(expectedCol,chromoCol,seqStartCol, MUTCOLUMNSIZE);

        ArrayList<String> firstDataRow = (fillNewList("10",MUTCOLUMNSIZE));
        firstDataRow.set(expectedCol, "37");
        testData.add(firstDataRow);

        //When
        harmonizer.setOmicSheet(testData);
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(CHAINFILE);

        Assert.assertEquals(1, actualList.size());
        Assert.assertEquals(MUTCOLUMNSIZE,actualList.get(0).size());
    }

    @Test
    public void Given_OmicSheet_When_AssemblyIs38_Then_blankList() throws IOException {

        int expectedCol = 18;

        //Given
        ArrayList<String> headers = fillNewList("HEADER",MUTCOLUMNSIZE);
        headers.set(expectedCol,assemblyHeader);
        testData.add(headers);
        ArrayList<String> firstDataRow = (fillNewList("TEST",MUTCOLUMNSIZE));
        firstDataRow.set(expectedCol, "38");
        testData.add(firstDataRow);

        //When
        harmonizer.setOmicSheet(testData);
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(CHAINFILE);

        Assert.assertEquals(0, actualList.size());
    }

    @Test
    public void Given_realHgCNAdataIsUsed_When_runLiftOverIsCalled_Then_pointsAreLiftedAndMerged() throws IOException {

        // {"chr6",32188823,32188823,"6",32221046,32221046},

        String cellTestValue = "10";

        String providedChromo = "chr6";
        String providedStartSeq = "32188823";
        String providedSeqEnd = "32188823";

        String expectedChromo = "6";
        String expectedStartSeq = "32221046";
        String expectedSeqEnd = "32221046";

        int assemblyCol = 16;
        int chromoCol = 17;
        int seqStartCol = 18;
        int seqEndCol = 19;

        //Given
        ArrayList<String> headers = fillNewList("HEADER", CNACOLUMNSIZE);
        headers.set(assemblyCol, assemblyHeader);
        headers.set(chromoCol, chromoHeader);
        headers.set(seqStartCol, seqStartHeader);
        headers.set(seqEndCol, CNAseqEndHeader);
        testData.add(headers);

        ArrayList<String> firstDataRow = (fillNewList(cellTestValue, CNACOLUMNSIZE));
        firstDataRow.set(assemblyCol, "37");
        firstDataRow.set(chromoCol, providedChromo);
        firstDataRow.set(seqStartCol, providedStartSeq);
        firstDataRow.set(seqEndCol, providedSeqEnd);
        testData.add(firstDataRow);

        //When
        harmonizer.setOmicSheet(testData);
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(CHAINFILE);

        Assert.assertEquals(actualList.get(0).get(chromoCol), expectedChromo);
        Assert.assertEquals(actualList.get(0).get(seqStartCol), expectedStartSeq);
        assertArraysAreCopies(testData.get(1), actualList.get(0), 16);
    }

    @Test
    public void Given_realMUTnoENDSEQ_When_runLiftOverIsCalled_Then_EndSeqIsHasNoPOS() throws IOException {

        // {"chr6",32188823,32188823,"6",32221046,32221046},

        String cellTestValue = "10";

        String providedChromo = "chr6";
        String providedStartSeq = "32188823";

        String expectedChromo = "6";
        String expectedStartSeq = "32221046";


        int assemblyCol = 19;
        int chromoCol = 20;
        int seqStartCol = 21;
        int seqEndCol = 22;

        //Given
        ArrayList<String> headers = fillNewList("HEADER", MUTCOLUMNSIZE);
        headers.set(assemblyCol, assemblyHeader);
        headers.set(chromoCol, chromoHeader);
        headers.set(seqStartCol, seqStartHeader);
        testData.add(headers);

        ArrayList<String> firstDataRow = (fillNewList(cellTestValue, MUTCOLUMNSIZE));
        firstDataRow.set(assemblyCol, "37");
        firstDataRow.set(chromoCol, providedChromo);
        firstDataRow.set(seqStartCol, providedStartSeq);
        testData.add(firstDataRow);

        //When
        harmonizer.setOmicSheet(testData);
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(CHAINFILE);

        Assert.assertEquals(actualList.get(0).get(chromoCol), expectedChromo);
        Assert.assertEquals(actualList.get(0).get(seqStartCol), expectedStartSeq);
        assertArraysAreCopies(testData.get(1), actualList.get(0), 19);
    }

    private void createHeadersWithAssemChromoAndStartSeq(int expectedCol,int chromoCol,int seqStartCol,int columnSize){

        ArrayList<String> headers = fillNewList("HEADER",columnSize);
        headers.set(expectedCol,assemblyHeader);
        headers.set(chromoCol,chromoHeader);
        headers.set(seqStartCol, seqStartHeader);
        testData.add(headers);
    }

    private void assertArraysAreCopies(ArrayList<String> expected, ArrayList<String> actual, int size){

        for(int i = 0; i < size; i++){
            Assert.assertEquals
                    (actual.get(i),expected.get(i));
        }
    }

    private ArrayList<String> fillNewList(String filler, int len){

        ArrayList<String> newList = new ArrayList<>();
        for(int i = 0; i < len; i++){
            newList.add(filler);
        }
        return newList;
    }
}
