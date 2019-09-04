package org.pdxfinder.graph.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreatmentProtocolTest extends BaseTest {

    private static final Boolean READ_CONTROLS_COMPONENTS = true;
    private static final Boolean DO_NOT_READ_CONTROLS = false;

    private static final String FIRST_TREATMENT_NAME = "TEST_TREATMENT_ONE";
    private static final String SECOND_TREATMENT_NAME = "TEST_TREATMENT_TWO";
    private static final String THIRD_TREATMENT_NAME = "TEST_TREATMENT_THREE";

    private static final String ONTOLOGY_URL = "TEST_URL";

    private static final String CONTROL_ONTOLOGY_LABEL = "TEST_ONTOLOGY_TERM";
    private static final String DRUG_ONTOLOGY_LABEL = "TEST_ONTOLOGY_TERM2";
    private static final String DRUG_ONTOLOGY_LABEL_2 = "TEST_ONTOLOGY_TERM3";

    private static final String RESPONSE_STRING = "TEST_RESPONSE";

    private static final String CONTROL_DURATION_1 = "TEST_DURATION_1";
    private static final String DRUG_DURATION_2 = "TEST_DURATION_2";
    private static final String DRUG_DURATION_3 = "TEST_DURATION_3";

    private static final String CONTROL_DOSE_1 = "TEST_DOSE_1";
    private static final String DRUG_DOSE_2 = "TEST_DOSE_2";
    private static final String DRUG_DOSE_3 = "TEST_DOSE_3";

    private List<TreatmentProtocol> treatmentProtocolList = new ArrayList<>();

    @Before
    public void InitModels() {

        createMockDatasets1();
        createMockDatasets2();
    }

    private void createMockDatasets1() {

        List<Treatment> treatments;
        treatments = createTreatmentsAndOntologies1();
        treatmentProtocolList.add(createTreatmentComponentsAndProtocols1(treatments));

    }

    private List<Treatment> createTreatmentsAndOntologies1(){

        Treatment treatment1 = new Treatment(FIRST_TREATMENT_NAME);
        Treatment treatment2 = new Treatment(SECOND_TREATMENT_NAME);
        Treatment treatment3 = new Treatment(THIRD_TREATMENT_NAME);

        OntologyTerm ontologyTerm1 = new OntologyTerm(ONTOLOGY_URL, CONTROL_ONTOLOGY_LABEL);
        OntologyTerm ontologyTerm2 = new OntologyTerm(ONTOLOGY_URL, DRUG_ONTOLOGY_LABEL);
        OntologyTerm ontologyTerm3 = new OntologyTerm(ONTOLOGY_URL, DRUG_ONTOLOGY_LABEL_2);

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

    private TreatmentProtocol createTreatmentComponentsAndProtocols1(List<Treatment> treatments) {

        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();
        List<TreatmentComponent> componentsList= new ArrayList<>();

        TreatmentComponent control = new TreatmentComponent();
        TreatmentComponent drug1 = new TreatmentComponent();
        TreatmentComponent drug2 = new TreatmentComponent();

        control.setDose(CONTROL_DOSE_1);
        drug1.setDose(DRUG_DOSE_2);
        drug2.setDose(DRUG_DOSE_3);

        control.setDuration(CONTROL_DURATION_1);
        drug1.setDuration(DRUG_DURATION_2);
        drug2.setDuration(DRUG_DURATION_3);

        control.setType("Control");
        drug1.setType("Drug");
        drug2.setType("Drug");

        control.setTreatment(treatments.get(0));
        drug1.setTreatment(treatments.get(1));
        drug2.setTreatment(treatments.get(2));

        componentsList.add(control);
        componentsList.add(drug1);
        componentsList.add(drug2);

        Response testResponse = new Response(RESPONSE_STRING, treatmentProtocol);

        treatmentProtocol.setComponents(componentsList);
        treatmentProtocol.setResponse(testResponse);

        return treatmentProtocol;
    }

    private void createMockDatasets2() {

        Treatment treatment;
        treatment = createTreatmentsAndOntologyRelationship();
        treatmentProtocolList.addAll(createTreatmentComponentsAndProtocols2(treatment));
    }

    private Treatment createTreatmentsAndOntologyRelationship() {

        Treatment treatment1 = new Treatment(FIRST_TREATMENT_NAME);
        TreatmentToOntologyRelationship ontologyRelationship1 = new TreatmentToOntologyRelationship();
        treatment1.setTreatmentToOntologyRelationship(ontologyRelationship1);

        return treatment1;
    }

    private List<TreatmentProtocol> createTreatmentComponentsAndProtocols2(Treatment treatments){

        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();
        TreatmentProtocol treatmentProtocol2 = new TreatmentProtocol();

        List<TreatmentComponent> componentsList1 = new ArrayList<>();
        List<TreatmentComponent> componentsList2 = new ArrayList<>();

        TreatmentComponent control = new TreatmentComponent();
        TreatmentComponent drug1 = new TreatmentComponent();

        control.setType("Control");
        control.setTreatment(treatments);

        control.setDose(CONTROL_DOSE_1);
        control.setDuration(CONTROL_DURATION_1);

        componentsList1.add(control);
        componentsList2.add(drug1);

        Response testResponse = new Response(RESPONSE_STRING, treatmentProtocol);
        Response testResponse2 = new Response(RESPONSE_STRING, treatmentProtocol2);

        treatmentProtocol.setComponents(componentsList1);
        treatmentProtocol2.setComponents(componentsList2);

        treatmentProtocol.setResponse(testResponse);
        treatmentProtocol2.setResponse(testResponse2);

        return Arrays.asList(treatmentProtocol,treatmentProtocol2);
    }

    @Test
    public void Given_getTreatmentString_When_MultipleTCandControlsOn_Then_ReturnNotNullConcatenatedString(){

        String treatmentString;

        treatmentString = treatmentProtocolList
                .get(0)
                .getTreatmentString(READ_CONTROLS_COMPONENTS);

        Assert.assertNotNull(treatmentString);
        Assert.assertEquals(
                String.format("%s and %s and %s", CONTROL_ONTOLOGY_LABEL, DRUG_ONTOLOGY_LABEL, DRUG_ONTOLOGY_LABEL_2),
                treatmentString);
    }

    @Test
    public void Given_getTreatmentString_WhenTreatmentComponentsWithNoControl_Then_returnConcatenatedStrings(){

        String treatmentString;

        treatmentString = treatmentProtocolList
                .get(0)
                .getTreatmentString(DO_NOT_READ_CONTROLS);

        Assert.assertNotNull(treatmentString);
        Assert.assertEquals(
                String.format("%s and %s", DRUG_ONTOLOGY_LABEL, DRUG_ONTOLOGY_LABEL_2),
                treatmentString);
    }

    @Test
    public void Given_getTreatmentString_When_TreatmentsAreMissingOntologies_Then_returnBlankStrings() {

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
    public void Given_getDoseString_When_ControlFlagIsToggled_Then_returnAppropriateDoseString() {

        String doseStringWithControls;
        String doseStringWithoutControls;

        doseStringWithControls = treatmentProtocolList
                .get(0)
                .getDoseString(READ_CONTROLS_COMPONENTS);

        doseStringWithoutControls = treatmentProtocolList
                .get(0)
                .getDoseString(DO_NOT_READ_CONTROLS);

        Assert.assertEquals(
                String.format("%s / %s / %s", CONTROL_DOSE_1, DRUG_DOSE_2, DRUG_DOSE_3),
                doseStringWithControls);
        Assert.assertEquals(
                String.format("%s / %s", DRUG_DOSE_2, DRUG_DOSE_3),
                doseStringWithoutControls);
    }

    @Test
    public void  Given_getDurationString_When_ControlFlagIsToggled_Then_returnStringWithAndWithoutControls() {

        String durationStringWithControls;
        String durationStringWithoutControls;

        durationStringWithControls = treatmentProtocolList
                .get(0)
                .getDurationString(READ_CONTROLS_COMPONENTS);

        durationStringWithoutControls = treatmentProtocolList
                .get(0)
                .getDurationString(DO_NOT_READ_CONTROLS);

        Assert.assertEquals(
                String.format("%s / %s / %s", CONTROL_DURATION_1, DRUG_DURATION_2, DRUG_DURATION_3),
                durationStringWithControls);
        Assert.assertEquals(
                String.format("%s / %s", DRUG_DURATION_2, DRUG_DURATION_3),
                durationStringWithoutControls);
    }

    @Test
    public void Given_getDurationAndGetDose_When_TreatmentIsMissingAnOntology_Then_returnDoseAndDurationUnaffected() {

        String durationStringWithControls;
        String doseStringWithControls;

        durationStringWithControls = treatmentProtocolList
                .get(1)
                .getDurationString(READ_CONTROLS_COMPONENTS);

        doseStringWithControls = treatmentProtocolList
                .get(1)
                .getDoseString(READ_CONTROLS_COMPONENTS);

        Assert.assertEquals(CONTROL_DURATION_1,durationStringWithControls);
        Assert.assertEquals(CONTROL_DOSE_1,doseStringWithControls);
    }

    @Test
    public void Given_getDurationAndGetDose_When_NodeStructureIsIncomplete_Then_DoNotReturnNull(){

        TreatmentComponent treatmentComponent = new TreatmentComponent();
        TreatmentProtocol treatmentProtocol = new TreatmentProtocol();

        treatmentProtocol.addTreatmentComponent(treatmentComponent);

        Assert.assertNotNull(treatmentProtocol.getDoseString(READ_CONTROLS_COMPONENTS));
        Assert.assertNotNull(treatmentProtocol.getDurationString(READ_CONTROLS_COMPONENTS));
    }

}
