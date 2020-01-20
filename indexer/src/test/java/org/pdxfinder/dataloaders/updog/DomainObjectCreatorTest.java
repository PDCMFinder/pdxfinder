package org.pdxfinder.dataloaders.updog;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import org.pdxfinder.services.DataImportService;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Matchers.anyObject;
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


    @Before
    public void setUp(){
        Map<String, Table> pdxDataTables = getTestDataTable();
        domainObjectCreator = new DomainObjectCreator(dataImportService, pdxDataTables);

        providerGroup = new Group("TestProvider", "TP", "description", "",
                "", "");
        providerGroup.setType("Provider");

        testPatient = new Patient("patient1", "female", "-", "ethnicity",providerGroup );
    }


    @Test
    public void Given_ProviderTable_When_CreateProviderIsCalled_Then_ProviderNodeIsInDomainMap(){

        when(dataImportService.getProviderGroup(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(providerGroup);


        domainObjectCreator.createProvider();

        Group provider = (Group) domainObjectCreator.getDomainObject("provider_group", null);
        Assert.assertEquals("TP", provider.getAbbreviation());
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

        return tableMap;
    }

}
