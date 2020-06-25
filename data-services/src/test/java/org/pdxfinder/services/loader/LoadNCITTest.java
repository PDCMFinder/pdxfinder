package org.pdxfinder.services.loader;

import net.minidev.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Marker;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.loader.envload.LoadNCIT;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LoadNCITTest extends BaseTest {

    @Mock
    private DataImportService dataImportService;

    @Mock
    private UtilityService utilityService;

    @InjectMocks
    private LoadNCIT loadNCIT;

    private Marker marker = new Marker();

    private JSONObject expectedOntologyJson = new JSONObject();

    private int minOntologyTermToSave = 0;

    private String diseaseBranchUrl;

    @Before
    public void setup() {

        // given
        JSONObject oboTerm1 = new JSONObject();
        oboTerm1.put("iri", "http://purl.obolibrary.org/obo/NCIT_C4741");
        oboTerm1.put("label", "Neoplasm by Morphology");
        oboTerm1.put("synonyms",Arrays.asList("Neoplasm by Morphology"));
        oboTerm1.put("ontology_name", "ncit");


        JSONObject oboTerm2 = new JSONObject();
        oboTerm2.put("iri", "http://purl.obolibrary.org/obo/NCIT_C3263");
        oboTerm2.put("label", "Neoplasm by Site");
        oboTerm2.put("synonyms",Arrays.asList("Neoplasm by Site"));
        oboTerm2.put("ontology_name", "ncit");

        List oboTermList = Arrays.asList(oboTerm1, oboTerm2);

        this.minOntologyTermToSave = oboTermList.size();

        JSONObject oboTerms = new JSONObject();
        oboTerms.put("terms", oboTermList);

        expectedOntologyJson.put("_embedded", oboTerms);
    }


    @Test
    public void given_DiseaseBranchURL_When_LoadOntologyInvoked_Then_SaveOntologyTerm() {

        // given
        this.diseaseBranchUrl = DataUrl.DISEASES_BRANCH_URL.get();
        String ontologyLabel = "Cancer";

        OntologyTerm ontologyTerm = new OntologyTerm(this.diseaseBranchUrl, ontologyLabel);
        when(this.dataImportService.getOntologyTerm(any(String.class), any(String.class)))
                .thenReturn(ontologyTerm);

        String expectedJsonString = expectedOntologyJson.toJSONString();
        when(this.utilityService.parseURL(any(String.class)))
                .thenReturn(expectedJsonString);

        // When
        loadNCIT.loadOntology(this.diseaseBranchUrl);

        // Then
        verify(dataImportService, atLeast(minOntologyTermToSave)).saveOntologyTerm(any(OntologyTerm.class));

    }




}





