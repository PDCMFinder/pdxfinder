package org.pdxfinder.preload;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

public class OmicHarmonizerTests {

    private ArrayList<ArrayList<String>> testData;
 
    
    private static final OmicHarmonizer.OMIC expectedMut = OmicHarmonizer.OMIC.MUT;
    private static final OmicHarmonizer.OMIC expectedCNA = OmicHarmonizer.OMIC.CNA;
    private static final String expectedUndefined = "undefined";
    private static final String UNIQUE_HEADER = "chromosome";
    private static final String assemblyHeader = "genome_assembly";
    private static final String chromoHeader = "chromosome";
    private static final String seqStartHeader = "seq_start_position";
    private static final String seqEndHeader = "seq_end_position";

    private static final int ERROR = -1;
    private static final int MUTCOLUMNSIZE = 23;
    private static final int CNACOLUMNSIZE = 20;
    private static final String CHAINFILE = "/home/afollette/IdeaProjects/pdxFinder/indexer/src/main/resources/LiftOverResources/hg19ToHg38.over.chain.gz";
    private OmicHarmonizer harmonizer = new OmicHarmonizer(CHAINFILE);
    
    public OmicHarmonizerTests() throws IOException {
    }


    @Before
    public void init(){
        testData = new ArrayList<>();
    }

    @Test
    public void Given_mutSheet_When_determineDataTypeIsCalled_OmicTypeStringIsMut(){

        //given
        testData.add(fillNewList("TEST",MUTCOLUMNSIZE));

        //when
        harmonizer.setOmicType(OmicHarmonizer.OMIC.MUT);

        //then
        Assert.assertEquals(expectedMut, harmonizer.getOmicType());
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
    public void Given_OmicSheet_When_AssemblyIsBlank_Then_returnOnlyHeader() throws IOException {

        //Given
        createHeadersWithAssemChromoAndStartSeq(17,18,19, MUTCOLUMNSIZE);
        ArrayList<String> firstDataRow = (fillNewList("TEST",MUTCOLUMNSIZE));
        firstDataRow.set(17, "");
        testData.add(firstDataRow);

        //When
        int actualSize = harmonizer.runLiftOver(testData, OmicHarmonizer.OMIC.MUT).size();

        Assert.assertEquals(1, actualSize);
    }

    @Test
    public void Given_OmicSheet_When_AssemblyIs37_Then_returnAddRowToOutputWithCorrectSize() throws IOException {


        int expectedCol = 18;
        int chromoCol = 16;
        int seqStartCol = 17;

        //Given
        createHeadersWithAssemChromoAndStartSeq(expectedCol,chromoCol,seqStartCol, MUTCOLUMNSIZE);

        ArrayList<String> firstDataRow = (fillNewList("1500",MUTCOLUMNSIZE));
        firstDataRow.set(expectedCol, "37");
        testData.add(firstDataRow);

        //When
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(testData, OmicHarmonizer.OMIC.MUT);

        Assert.assertEquals(1, actualList.size());
        Assert.assertEquals(MUTCOLUMNSIZE,actualList.get(0).size());

    }

    @Test
    public void Given_OmicSheet_When_AssemblyIs38_Then_OnlyHeaders() throws IOException {

        int expectedCol = 18;

        //Given
        createHeadersWithAssemChromoAndStartSeq(10,11, 12, MUTCOLUMNSIZE);
        ArrayList<String> firstDataRow = (fillNewList("TEST",MUTCOLUMNSIZE));
        firstDataRow.set(expectedCol, "38");
        testData.add(firstDataRow);

        //When
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(testData, OmicHarmonizer.OMIC.MUT);

        Assert.assertEquals(1, actualList.size());
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

        int assemblyCol = 16;
        int chromoCol = 17;
        int seqStartCol = 18;
        int seqEndCol = 19;

        //Given
        ArrayList<String> headers = fillNewList("HEADER",MUTCOLUMNSIZE);
        headers.set(assemblyCol,assemblyHeader);
        headers.set(chromoCol,chromoHeader);
        headers.set(seqStartCol, seqStartHeader);
        headers.set(seqEndCol, seqEndHeader);
        testData.add(headers);


        ArrayList<String> firstDataRow = (fillNewList(cellTestValue, CNACOLUMNSIZE));
        firstDataRow.set(assemblyCol, "37");
        firstDataRow.set(chromoCol, providedChromo);
        firstDataRow.set(seqStartCol, providedStartSeq);
        firstDataRow.set(seqEndCol, providedSeqEnd);
        testData.add(firstDataRow);

        //When
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(testData, OmicHarmonizer.OMIC.CNA);

        Assert.assertEquals(actualList.get(1).get(chromoCol), expectedChromo);
        Assert.assertEquals(actualList.get(1).get(seqStartCol), expectedStartSeq);
        assertArraysAreCopies(testData.get(1), actualList.get(1), 16);
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
        createHeadersWithAssemChromoAndStartSeq(assemblyCol,chromoCol,seqStartCol,MUTCOLUMNSIZE);

        ArrayList<String> firstDataRow = (fillNewList(cellTestValue, MUTCOLUMNSIZE));
        firstDataRow.set(assemblyCol, "37");
        firstDataRow.set(chromoCol, providedChromo);
        firstDataRow.set(seqStartCol, providedStartSeq);
        testData.add(firstDataRow);

        //When
        harmonizer.setOmicSheet(testData);
        ArrayList<ArrayList<String>> actualList = harmonizer.runLiftOver(testData, OmicHarmonizer.OMIC.MUT);

        Assert.assertEquals(actualList.get(1).get(chromoCol), expectedChromo);
        Assert.assertEquals(actualList.get(1).get(seqStartCol), expectedStartSeq);
        assertArraysAreCopies(testData.get(1), actualList.get(1), 19);
    }

    private void createHeadersWithAssemChromoAndStartSeq(int assemblyCol,int chromoCol,int seqStartCol, int columnSize){

        ArrayList<String> headers = fillNewList("HEADER",columnSize);
        headers.set(assemblyCol,assemblyHeader);
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
