package org.pdxfinder.graph.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.repositories.TreatmentProtocolRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test for getTreamentString() in TreatmentProtocol
 */

public class TreatmentProtocolTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(TreatmentProtocolTest.class);

    final Boolean READ_CONTROLS_COMPONENTS = true;
    final Boolean DO_NOT_READ_CONTROLS = false;

    private final String firstTreatmentName = "TEST_TREATMENT_ONE";
    private final String secondTreatmentName = "TEST_TREATMENT_TWO";
    private final String thirdTreatmentName = "TEST_TREATMENT_THREE";

    private final String ontologyURL = "TEST_URL";

    private final String controlOntologyLabel = "TEST_ONTOLOGY_TERM";
    private final String drugOntologyLabel = "TEST_ONTOLOGY_TERM2";
    private final String drugOntologyLabel2 = "TEST_ONTOLOGY_TERM3";

    private final String responseString = "TEST_RESPONSE";

    private final String controlDuration1 = "TEST_DURATION_1";
    private final String drugDuration2 = "TEST_DURATION_2";
    private final String drugDuration3 = "TEST_DURATION_3";

    private final String controlDose1 = "TEST_DOSE_1";
    private final String drugDose2 = "TEST_DOSE_2";
    private final String drugDose3 = "TEST_DOSE_3";

    @Autowired
    private TreatmentProtocolRepository treatmentProtocolRepository;

    private List<TreatmentProtocol> treatmentProtocolList = new ArrayList<TreatmentProtocol>();

    @Before
    public void InitModels() {

        createMockDatasets1();
        createMockDatasets2();
    }

    private void createMockDatasets1() {

        List<Treatment> treatments = new ArrayList<Treatment>();

        treatments = createTreatmentsAndOntologies1();

        createTreatmentComponentsAndProtocols1(treatments);
    }


    private List<Treatment> createTreatmentsAndOntologies1(){

        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();

        Treatment treatment1 = new Treatment(firstTreatmentName);
        Treatment treatment2 = new Treatment(secondTreatmentName);
        Treatment treatment3 = new Treatment(thirdTreatmentName);

        OntologyTerm ontologyTerm1 = new OntologyTerm(ontologyURL, controlOntologyLabel);
        OntologyTerm ontologyTerm2 = new OntologyTerm(ontologyURL, drugOntologyLabel);
        OntologyTerm ontologyTerm3 = new OntologyTerm(ontologyURL, drugOntologyLabel2);

        TreatmentToOntologyRelationship ontologyRelationship1 = new TreatmentToOntologyRelationship();
        TreatmentToOntologyRelationship ontologyRelationship2 = new TreatmentToOntologyRelationship();
        TreatmentToOntologyRelationship ontologyRelationship3 = new TreatmentToOntologyRelationship();

        ontologyRelationship1.setOntologyTerm(ontologyTerm1);
        ontologyRelationship2.setOntologyTerm(ontologyTerm2);
        ontologyRelationship3.setOntologyTerm(ontologyTerm3);

        ontologyRelationship1.setTreatment(treatment1);
        ontologyRelationship2.setTreatment(treatment2);
        ontologyRelationship3.setTreatment(treatment3);
        treatment1.setTreatmentToOntologyRelationship(ontologyRelationship1);
        treatment2.setTreatmentToOntologyRelationship(ontologyRelationship2);
        treatment3.setTreatmentToOntologyRelationship(ontologyRelationship3);

        return Arrays.asList(treatment1, treatment2, treatment3);
    }

    private void createTreatmentComponentsAndProtocols1(List<Treatment> treatments) {

        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();

        List<TreatmentComponent> componentsList= new ArrayList<TreatmentComponent>();
        TreatmentComponent controlTC = new TreatmentComponent();
        TreatmentComponent drug1 = new TreatmentComponent();
        TreatmentComponent drug2 = new TreatmentComponent();

        controlTC.setDose(controlDose1);
        drug1.setDose(drugDose2);
        drug2.setDose(drugDose3);

        controlTC.setDuration(controlDuration1);
        drug1.setDuration(drugDuration2);
        drug2.setDuration(drugDuration3);

        controlTC.setTreatment(treatments.get(0));
        controlTC.setType("Control");

        drug1.setTreatment(treatments.get(1));
        drug2.setTreatment(treatments.get(2));

        componentsList.add(controlTC);
        componentsList.add(drug1);
        componentsList.add(drug2);

        Response testResponse = new Response(responseString, treatmentProtocol);

        treatmentProtocol.setComponents(componentsList);
        treatmentProtocol.setResponse(testResponse);

        treatmentProtocolList.add(treatmentProtocol);
    }

    private void createMockDatasets2() {

        List<Treatment> treatments;

        treatments = createTreatmentsAndOntologies2();

        createTreatmentComponentsAndProtocols2(treatments);

    }

    private List<Treatment> createTreatmentsAndOntologies2() {

        Treatment treatment1 = new Treatment(firstTreatmentName);

        TreatmentToOntologyRelationship ontologyRelationship1 = new TreatmentToOntologyRelationship();
        treatment1.setTreatmentToOntologyRelationship(ontologyRelationship1);

        return Arrays.asList(treatment1);
    }

    private void createTreatmentComponentsAndProtocols2(List<Treatment> treatments){

        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();
        TreatmentProtocol treatmentProtocol2 = new TreatmentProtocol();

        List<TreatmentComponent> componentsList1 = new ArrayList<TreatmentComponent>();
        List<TreatmentComponent> componentsList2 = new ArrayList<TreatmentComponent>();

        TreatmentComponent controlTC = new TreatmentComponent();
        TreatmentComponent drug1 = new TreatmentComponent();

        controlTC.setTreatment(treatments.get(0));
        controlTC.setType("Control");

        controlTC.setTreatment(treatments.get(0));

        componentsList1.add(controlTC);
        componentsList2.add(drug1);

        Response testResponse = new Response(responseString, treatmentProtocol);
        Response testResponse2 = new Response(responseString, treatmentProtocol2);

        treatmentProtocol.setComponents(componentsList1);
        treatmentProtocol2.setComponents(componentsList2);

        treatmentProtocol.setResponse(testResponse);
        treatmentProtocol2.setResponse(testResponse2);

        treatmentProtocolList.add(treatmentProtocol);
        treatmentProtocolList.add(treatmentProtocol2);
    }

    @Test
    public void testGetTreatmentStringWithControlComponents(){

        final Boolean READ_CONTROLS_COMPONENTS = true;

        String treatmentString = null;

        treatmentString = treatmentProtocolList
                .get(0)
                .getTreatmentString(READ_CONTROLS_COMPONENTS);

        Assert.assertNotNull(treatmentString);
        Assert.assertEquals(controlOntologyLabel + " and " + drugOntologyLabel + " and " +
                drugOntologyLabel2,treatmentString);
    }

    @Test
    public void testGetTreatmentStringWithNoControlComponents(){

        String treatmentString = null;

        treatmentString = treatmentProtocolList
                .get(0)
                .getTreatmentString(DO_NOT_READ_CONTROLS);

        Assert.assertNotNull(treatmentString);
        Assert.assertEquals(drugOntologyLabel + " and " + drugOntologyLabel2, treatmentString);
    }

    @Test
    public void testGetTreatmentStringNoNullOnReturn() {

        String treatmentString1;
        String treatmentString2;
        String treatmentString3;

        treatmentString1  = treatmentProtocolList
            .get(1)
            .getTreatmentString(READ_CONTROLS_COMPONENTS);

        treatmentString2  = treatmentProtocolList
            .get(2)
            .getTreatmentString(READ_CONTROLS_COMPONENTS);

        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();
        treatmentString3 = treatmentProtocol.getTreatmentString(true);

        Assert.assertEquals("", treatmentString1);
        Assert.assertEquals("", treatmentString2);
        Assert.assertEquals("", treatmentString3);
    }

    @Test
    public void testGetDoseString() {

        final Boolean READ_CONTROLS_COMPONENTS = true;

        String doseStringWithControls;
        String doseStringWithoutControls;

        doseStringWithControls = treatmentProtocolList
                .get(0)
                .getDoseString(READ_CONTROLS_COMPONENTS);

        doseStringWithoutControls = treatmentProtocolList
                .get(0)
                .getDoseString(DO_NOT_READ_CONTROLS);


        Assert.assertNotNull(doseStringWithControls);
        Assert.assertNotNull(doseStringWithoutControls);

        Assert.assertEquals(controlDose1 + " / " + drugDose2 + " / " +
                drugDose3, doseStringWithControls);
        Assert.assertEquals(drugDose2 + " / " +
                drugDose3, doseStringWithoutControls);
    }

    @Test
    public void testGetDurationString() {

        final Boolean READ_CONTROLS_COMPONENTS = true;

        String durationStringWithControls;
        String durationStringWithoutControls;

        durationStringWithControls = treatmentProtocolList
                .get(0)
                .getDurationString(READ_CONTROLS_COMPONENTS);

        durationStringWithoutControls = treatmentProtocolList
                .get(0)
                .getDurationString(DO_NOT_READ_CONTROLS);

        Assert.assertNotNull(durationStringWithControls);
        Assert.assertNotNull(durationStringWithoutControls);

        Assert.assertEquals(controlDuration1 + " / " + drugDuration2 + " / " +
                drugDuration3, durationStringWithControls);
        Assert.assertEquals(drugDuration2 + " / " +
                drugDuration3, durationStringWithoutControls);

    }

    public void testDurationAndDoseForNoDataReturn() {

        String durationStringWithControls;
        String doseStringWithControls;

        durationStringWithControls = treatmentProtocolList
                .get(1)
                .getDurationString(READ_CONTROLS_COMPONENTS);

        doseStringWithControls = treatmentProtocolList
                .get(1)
                .getDurationString(READ_CONTROLS_COMPONENTS);

        Assert.assertEquals("",durationStringWithControls);
        Assert.assertEquals("",doseStringWithControls);

    }

    @Test
    public void testDurationAndDoseForNullDataResponse(){

        TreatmentComponent treatmentComponent = new TreatmentComponent();
        TreatmentProtocol tp = new TreatmentProtocol();

        tp.addTreatmentComponent(treatmentComponent);

        Assert.assertNotNull(tp.getDoseString(READ_CONTROLS_COMPONENTS));
        Assert.assertNotNull(tp.getDurationString(READ_CONTROLS_COMPONENTS));

    }

}
