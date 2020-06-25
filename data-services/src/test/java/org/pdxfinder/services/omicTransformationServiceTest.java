package org.pdxfinder.services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Marker;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class omicTransformationServiceTest extends BaseTest {

    @Mock DataImportService dataImportService;

    @InjectMocks OmicTransformationService omicTransformationService;

    private static final String DUMMY_NCBI_ID = "9999999";
    private static final String EXPECTED_SYMBOL = "TEST";
    private Marker marker;


    @Before
    public void init(){
        marker = new Marker();
        marker.setHgncSymbol("TEST");
    }

    @Test
    public void Given_ncbiGene_When_ncbiGeneIdToHgncSymbol_Then_returnHgncSymbol(){
        when(dataImportService.getMarkerbyNcbiGeneId(DUMMY_NCBI_ID)).thenReturn(marker);
        String actualSymbol = omicTransformationService.ncbiGeneIdToHgncSymbol(DUMMY_NCBI_ID);
        //then
        Assert.assertEquals(EXPECTED_SYMBOL, actualSymbol);
    }


    @Test
    public void Given_twoIdenticalNcbiGenes_When_ncbiGeneIdToHgncSymbolIsCalledTwice_Then_onlyCallDbOnce(){
        when(dataImportService.getMarkerbyNcbiGeneId(DUMMY_NCBI_ID)).thenReturn(marker);
        omicTransformationService.ncbiGeneIdToHgncSymbol(DUMMY_NCBI_ID);
        String actualSymbol = omicTransformationService.ncbiGeneIdToHgncSymbol(DUMMY_NCBI_ID);
        //then
        verify(dataImportService, times(1)).getMarkerbyNcbiGeneId(DUMMY_NCBI_ID);
        Assert.assertEquals(EXPECTED_SYMBOL, actualSymbol);
    }

    @Test
    public void Given_listOfUniqNcbiGenes_When_convertListOfNcbiToHgncIsCalled_Then_callNcbiGeneIdToHgncSymbolForEach(){
        when(dataImportService.getMarkerbyNcbiGeneId(anyString())).thenReturn(marker);
        List<String> ncbiGenes = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            ncbiGenes.add(String.valueOf(i));
        }
        omicTransformationService.convertListOfNcbiToHgnc(ncbiGenes);
        //then
        verify(dataImportService, times(5)).getMarkerbyNcbiGeneId(anyString());

    }
    @Test
    public void Given_listOfIdenticaNcbiGenes_When_convertListOfNcbiToHgncIsCalled_Then_callNcbiGeneIdToHgncSymbolOnce(){
        when(dataImportService.getMarkerbyNcbiGeneId(anyString())).thenReturn(marker);
        List<String> ncbiGenes = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            ncbiGenes.add(String.valueOf(1000));
        }
        omicTransformationService.convertListOfNcbiToHgnc(ncbiGenes);
        //then
        verify(dataImportService, times(1)).getMarkerbyNcbiGeneId(anyString());

    }



}
