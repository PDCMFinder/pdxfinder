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
        MaList = Collections.singletonList(testMa);
        McSet = new HashSet<>();

        testMarker.setHgncSymbol("testSymbol");
        testPlatform.setName("test");
        testSample.setSourceSampleId("testId");

        testMc.setPlatform(testPlatform);
        testMa.setMarker(testMarker);

    }

    @Test
    public void Given_NodesExistButValuesAreEmpty_When_Called_ReturnReturnBasicData() {

        long expectedReturnSize = 5;

        McSet.add(testMc);
        testMc.setMarkerAssociations(MaList);
        testSample.setMolecularCharacterizations(McSet);

        String expectedSampleName = "testId";
        String expectedPatientTumor = "Patient Tumor";
        String testPassage = "";
        String testMappedOntologyTermLabel = "";
        String testMolCharType = "";
        String expectedPlatformName = "test";
        String expectedMarkerHGNCSymbol = "testSymbol";

        List<String> expectedList = Arrays.asList(expectedSampleName, expectedPatientTumor, testPassage, testMappedOntologyTermLabel,
                testMolCharType, expectedPlatformName, expectedMarkerHGNCSymbol);

        List<List<String>> actualReturnData = detailsService.buildUpDTO(testSample,testPassage,testMappedOntologyTermLabel,testMolCharType);
        Assert.assertNotNull(actualReturnData);
        Assert.assertEquals(expectedList, actualReturnData.get(0));
    }

    @Test
    public void Given_MutationDataExistsWithNoData_When_buildUpDtoIsCalled_doNotReturnNull(){

        testMc.setType("mutation");
        McSet.add(testMc);
        testMc.setMarkerAssociations(MaList);
        testSample.setMolecularCharacterizations(McSet);

        String testPassage = "";
        String testMappedOntologyTermLabel = "";
        String testMolCharType = "";

        List<List<String>> actualReturnData = detailsService.buildUpDTO(testSample,testPassage,testMappedOntologyTermLabel,testMolCharType);
        Assert.assertNotNull(actualReturnData);
    }

}
