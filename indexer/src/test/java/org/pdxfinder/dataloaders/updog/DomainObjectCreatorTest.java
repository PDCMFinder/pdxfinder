package org.pdxfinder.dataloaders.updog;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.HashMap;
import java.util.Map;

public class DomainObjectCreatorTest extends BaseTest {


    @MockBean
    private DataImportService dataImportService;

    private DomainObjectCreator domainObjectCreator;

    private Group providerGroup;
    private Patient testPatient;
    private ModelCreation testModel;


    @Before
    public void setUp(){
        Map<String, Table> pdxDataTables = getTestDataTable();
        domainObjectCreator = new DomainObjectCreator(dataImportService, pdxDataTables);

        providerGroup = new Group("TestProvider", "TP", "description", "",
                "", "");
        providerGroup.setType("Provider");

        testPatient = new Patient("patient1", "female", "-", "ethnicity",providerGroup );
        testModel = new ModelCreation("model1");
    }


    @Test
    public void Given_ProviderTable_When_CreateProviderIsCalled_Then_ProviderNodeIsInDomainMap(){

        when(dataImportService.getProviderGroup(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(providerGroup);


        domainObjectCreator.createProvider();

        Group provider = (Group) domainObjectCreator.getDomainObject("provider_group", null);
        Assert.assertEquals("TP", provider.getAbbreviation());
    }


    @Test
    public void Given_PatientTable_When_CreatePatient_Then_PatientNodeIsInMap(){

        domainObjectCreator.addDomainObject("provider_group", null, providerGroup);
        when(dataImportService.createPatient("patient1", providerGroup, "female", "", "ethnicity")).thenReturn(testPatient);
        when(dataImportService.savePatient(testPatient)).thenReturn(testPatient);

        domainObjectCreator.createPatientData();

        Patient patient = (Patient) domainObjectCreator.getDomainObject("patient", "patient1");

        Assert.assertEquals("patient1", patient.getExternalId());
        Assert.assertEquals("female", patient.getSex());

    }

    @Test
    public void Given_ModelTable_When_CreateModel_Then_ModelNodeIsInMap(){

        domainObjectCreator.addDomainObject("provider_group", null, providerGroup);

        domainObjectCreator.createModelData();

        ModelCreation model = (ModelCreation) domainObjectCreator.getDomainObject("model", "model1");

        Assert.assertEquals("model1", model.getSourcePdxId());

    }

    @Test
    public void Given_SampleTable_When_CreateSample_Then_SampleIsInMap(){

        domainObjectCreator.addDomainObject("provider_group", null, providerGroup);
        domainObjectCreator.addDomainObject("patient", "patient1", testPatient);
        domainObjectCreator.addDomainObject("model", "model1", testModel);

        when(dataImportService.getTumorType("Primary")).thenReturn(new TumorType("Primary"));

        domainObjectCreator.createSampleData();

        ModelCreation model = (ModelCreation) domainObjectCreator.getDomainObject("model", "model1");
        Patient patient = (Patient) domainObjectCreator.getDomainObject("patient", "patient1");

        Sample sample = model.getSample();

        Assert.assertEquals("sample1", sample.getSourceSampleId());
        Assert.assertEquals("70",patient.getSnapshotByDate("01/01/1900").getAgeAtCollection());
        Assert.assertEquals("Primary", sample.getType().getName());

    }







    private Map<String, Table> getTestDataTable(){

        Map<String, Table> tableMap = new HashMap<>();

        String[] loaderCol1 = {"v1", "v2", "v3", "v4", "Test Provider"};
        String[] loaderCol2 = {"v1", "v2", "v3", "v4", "TP"};
        String[] loaderCol3 = {"v1", "v2", "v3", "v4", "/source/test"};
        String[] loaderCol4 = {"v1", "v2", "v3", "v4", ""};

        Table loaderTable = Table.create("metadata-loader.tsv").addColumns(
                StringColumn.create("name", loaderCol1),
                StringColumn.create("abbreviation", loaderCol2),
                StringColumn.create("internal_url", loaderCol3),
                StringColumn.create("internal_dosing_url", loaderCol4)
        );

        tableMap.put("metadata-loader.tsv", loaderTable);

        String[] patientCol1 = {"v1", "v2", "v3", "v4","patient1"};
        String[] patientCol2 = {"v1", "v2", "v3", "v4","female"};
        String[] patientCol3 = {"v1", "v2", "v3", "v4","history"};
        String[] patientCol4 = {"v1", "v2", "v3", "v4","ethnicity"};
        String[] patientCol5 = {"v1", "v2", "v3", "v4","initial diagnosis"};
        String[] patientCol6 = {"v1", "v2", "v3", "v4","age at initial diagnosis"};

        Table patientTable = Table.create("metadata-patient.tsv").addColumns(
                StringColumn.create("patient_id", patientCol1),
                StringColumn.create("sex", patientCol2),
                StringColumn.create("history", patientCol3),
                StringColumn.create("ethnicity", patientCol4),
                StringColumn.create("initial_diagnosis", patientCol5),
                StringColumn.create("age_at_initial_diagnosis", patientCol6)
        );

        tableMap.put("metadata-patient.tsv", patientTable);


        String[] modelCol1 = {"v1", "v2", "v3", "v4","model1"};
        String[] modelCol2 = {"v1", "v2", "v3", "v4","hoststrainname1"};
        String[] modelCol3 = {"v1", "v2", "v3", "v4","hoststrainnomenclature1"};
        String[] modelCol4 = {"v1", "v2", "v3", "v4","engraftmentsite1"};
        String[] modelCol5 = {"v1", "v2", "v3", "v4","engraftmenttype1"};
        String[] modelCol6 = {"v1", "v2", "v3", "v4","sampletype1"};
        String[] modelCol7 = {"v1", "v2", "v3", "v4","samplestate1"};
        String[] modelCol8 = {"v1", "v2", "v3", "v4","1"};
        String[] modelCol9 = {"v1", "v2", "v3", "v4","publications1"};

        Table modelTable = Table.create("metadata-model.tsv").addColumns(
                StringColumn.create("model_id", modelCol1),
                StringColumn.create("host_strain", modelCol2),
                StringColumn.create("host_strain_full", modelCol3),
                StringColumn.create("engraftment_site", modelCol4),
                StringColumn.create("engraftment_type", modelCol5),
                StringColumn.create("sample_type", modelCol6),
                StringColumn.create("sample_state", modelCol7),
                StringColumn.create("passage_number", modelCol8),
                StringColumn.create("publications", modelCol9)

        );

        tableMap.put("metadata-model.tsv", modelTable);


        String[] modelValCol1 = {"v1", "v2", "v3", "v4","model1"};
        String[] modelValCol2 = {"v1", "v2", "v3", "v4","techique1"};
        String[] modelValCol3 = {"v1", "v2", "v3", "v4","description1"};
        String[] modelValCol4 = {"v1", "v2", "v3", "v4","passage1"};
        String[] modelValCol5 = {"v1", "v2", "v3", "v4","hoststrain1"};

        Table modelValidationTable = Table.create("metadata-model_validation.tsv").addColumns(
                StringColumn.create("model_id", modelValCol1),
                StringColumn.create("validation_technique", modelValCol2),
                StringColumn.create("description", modelValCol3),
                StringColumn.create("passages_tested", modelValCol4),
                StringColumn.create("validation_host_strain_full", modelValCol5)
        );

        tableMap.put("metadata-model_validation.tsv", modelValidationTable);



        String[] sampleCol1 = {"v1", "v2", "v3", "v4","patient1"};
        String[] sampleCol2 = {"v1", "v2", "v3", "v4","sample1"};
        String[] sampleCol3 = {"v1", "v2", "v3", "v4","01/01/1900"};
        String[] sampleColEmpty = {"v1", "v2", "v3", "v4",""};
        String[] sampleCol6 = {"v1", "v2", "v3", "v4","70"};
        String[] sampleCol7 = {"v1", "v2", "v3", "v4","Cancer"};
        String[] sampleCol8 = {"v1", "v2", "v3", "v4","Primary"};
        String[] sampleCol9 = {"v1", "v2", "v3", "v4","Breast"};
        String[] sampleCol10 = {"v1", "v2", "v3", "v4","Breast"};
        String[] sampleCol11 = {"v1", "v2", "v3", "v4","model1"};

        Table sampleTable = Table.create("metadata-sample.tsv").addColumns(
                StringColumn.create("patient_id", sampleCol1),
                StringColumn.create("sample_id", sampleCol2),
                StringColumn.create("collection_date", sampleCol3),
                StringColumn.create("age_in_years_at_collection", sampleCol6),
                StringColumn.create("diagnosis", sampleCol7),
                StringColumn.create("tumour_type", sampleCol8),
                StringColumn.create("primary_site", sampleCol9),
                StringColumn.create("collection_site", sampleCol10),
                StringColumn.create("model_id", sampleCol11),
                StringColumn.create("collection_event", sampleColEmpty),
                StringColumn.create("months_since_collection_1", sampleColEmpty),
                StringColumn.create("virology_status", sampleColEmpty),
                StringColumn.create("stage", sampleColEmpty),
                StringColumn.create("staging_system", sampleColEmpty),
                StringColumn.create("grade", sampleColEmpty),
                StringColumn.create("grading_system", sampleColEmpty),
                StringColumn.create("treatment_naive_at_collection", sampleColEmpty)
        );

        tableMap.put("metadata-sample.tsv", sampleTable);


        return tableMap;
    }

}
