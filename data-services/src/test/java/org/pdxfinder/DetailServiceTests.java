package org.pdxfinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DetailsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

public class DetailServiceTests extends BaseTest {

    @Autowired
    private DetailsService detailsService;
    private Sample testSample;
    private MolecularCharacterization testMc;
    private MarkerAssociation testMa;
    private List<MarkerAssociation> MaList;
    private Set<MolecularCharacterization> McSet;
    private Platform testPlatform;
    private Marker testMarker;

    @Before
    public void init(){

        testSample = new Sample();
        testMc = new MolecularCharacterization();
        testMa = new MarkerAssociation();
        testMarker = new Marker();
        testPlatform = new Platform();
        McSet = new HashSet<>();

        testMarker.setHgncSymbol("TEST");
        testPlatform.setName("TEST");
        testSample.setSourceSampleId("TEST");

        testMc.setPlatform(testPlatform);
        testMa.setMarker(testMarker);

    }

    @Test
    public void Given_NodesExistButNoMolCharIspassed_When_Called_ReturnReturnBasicData() {

        long expectedReturnSize = 5;

        McSet.add(testMc);
        MaList = Collections.singletonList(testMa);
        testMc.setMarkerAssociations(MaList);
        testSample.setMolecularCharacterizations(McSet);

        String expectedSampleName = "TEST";
        String expectedPassage = "TEST";
        String expectedMappedOntologyTermLabel = "TEST";
        String expectedMolCharType = "test";

        String expectedPatientTumor = "Xenograft";
        String expectedCapatalizedMolCharType = "Test";
        String expectedPlatformName = "TEST";
        String expectedMarkerHGNCSymbol = "TEST";

        List<String> expectedList = Arrays.asList(expectedSampleName, expectedPatientTumor, expectedPassage, expectedMappedOntologyTermLabel,
                expectedCapatalizedMolCharType, expectedPlatformName, expectedMarkerHGNCSymbol);

        List<List<String>> actualReturnData = detailsService.buildUpDTO(testSample,expectedPassage,expectedMappedOntologyTermLabel,expectedMolCharType);
        Assert.assertNotNull(actualReturnData);
        Assert.assertEquals(expectedList, actualReturnData.get(0));
    }

    @Test
    public void Given_MutationDataExistsWithNoData_When_buildUpDtoIsCalled_doNotReturnNull(){

        List<String> expectedList = new LinkedList<>();

        testMa.setNucleotideChange("TEST");
        testMa.setAminoAcidChange("TEST");
        testMa.setReadDepth("TEST");
        testMa.setAlleleFrequency("TEST");
        testMa.setExistingVariations("TEST");
        testMa.setChromosome("TEST");
        testMa.setSeqStartPosition("TEST");
        testMa.setRefAllele("TEST");
        testMa.setAltAllele("TEST");
        testMa.setConsequence("TEST");
        testMa.setGenomeAssembly("TEST");

        MaList = Collections.singletonList(testMa);
        testMc.setMarkerAssociations(MaList);

        McSet.add(testMc);
        testSample.setMolecularCharacterizations(McSet);

        String testPassage = "TEST";
        String testMappedOntologyTermLabel = "TEST";
        String testMolCharType = "mutation";



        for(int i = 0; i < 18; i++){
            expectedList.add("TEST");
        }


        List<List<String>> actualList = detailsService.buildUpDTO(testSample,testPassage,testMappedOntologyTermLabel,testMolCharType);

        for(int i = 0; i < 18; i++){
            if(i == 1) Assert.assertEquals("Xenograft", actualList.get(0).get(1));
            else if(i == 4)Assert.assertEquals("Mutation", actualList.get(0).get(4));
            else Assert.assertEquals(expectedList.get(i), actualList.get(0).get(i));
        }

    }

    @Test
    public void Given_MutationDataExistsWithData_When_buildUpDtoISCalled_returnData(){

        McSet.add(testMc);
        MaList = Collections.singletonList(testMa);
        testMc.setMarkerAssociations(MaList);
        testSample.setMolecularCharacterizations(McSet);

        String molCharType = "mutation";
        String testMappedOntologyTermLabel = "TEST";
        String testPassage = "";

        List<List<String>> actualList = detailsService.buildUpDTO(testSample,testPassage,testMappedOntologyTermLabel,molCharType);
    }

}
