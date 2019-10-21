package org.pdxfinder.dataloaders;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.SpecimenRepository;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class LoadIRCCTest extends BaseTest {

    private Group transnationalAccess;
    private Group badAccess;



    @Before public void init() {
        MockitoAnnotations.initMocks(this);
        transnationalAccess = new Group();
        transnationalAccess.setName("transnational access");
        badAccess = new Group();
        badAccess.setName("another group");

        loader.providerDS = new Group(
            "Test IRCC Provider",
            "IRCC-CRC",
            "Test provider description",
            "Academia",
            "Joe Bloggs",
            "ircc.example.com");


    }

    @MockBean UtilityService utilityService;
    @MockBean DataImportService dataImportService;
    @MockBean SpecimenRepository specimenRepository;
    @InjectMocks private LoadIRCC loader;

    @Test
    public void Given_ValidSpecimen_WhenLoadSpecimen_Then_SpecimenAddedToModel() throws Exception {
        ModelCreation modelCreation = new ModelCreation("1");
        JSONArray specimens = new JSONArray(
            "[{" +
                "\"Specimen ID\":\"CRCTEST\"," +
                "\"Engraftment Type\":\"tissue fragment\"," +
                "\"Engraftment Site\":\"subcutis right\"," +
                "\"Strain\":\"NOD SCID\"," +
                "\"Passage\":\"1\"," +
                "\"Platforms\":[ " +
                    "{\"Platform\":\"RealTimePCR_GCN\"}" +
                "]" +
            "}]"
        );
        JSONObject json = specimens.getJSONObject(0);
        Specimen specimen = new Specimen();
        specimen.setExternalId(json.getString("Specimen ID"));
        specimen.setPassage(json.getString("Passage"));

        loader.dto.setModelCreation(modelCreation);
        loader.dto.setSpecimens(specimens);


        when(dataImportService.getSpecimen(
            modelCreation,
            json.getString("Specimen ID"),
            loader.providerDS.getAbbreviation(),
            json.getString("Passage"))
        ).thenReturn(specimen);

        loader.step13LoadSpecimens();

        assertEquals( "CRCTEST", loader.dto.getSpecimens().getJSONObject(0).getString("Specimen ID"));
        assertThat(loader.dto.getModelCreation().getSpecimens().contains(specimen), is(true));
    }

    @Test
    public void Given_IRCCCRCModel_When_AddAccessModality_Then_AddToTransnationalAccessGroup() {
        when(dataImportService.getAccessibilityGroup("", "transnational access"))
                .thenReturn(transnationalAccess);

        ModelCreation mc = new ModelCreation();
        loader.dto.setModelCreation(mc);
        loader.step18SetAdditionalGroups();

        assertThat(loader.dto.getModelCreation().getGroups().contains(transnationalAccess), is(true));
        assertThat(loader.dto.getModelCreation().getGroups().contains(badAccess), is(false));
    }

}
