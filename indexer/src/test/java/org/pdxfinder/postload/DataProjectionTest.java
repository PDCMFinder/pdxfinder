package org.pdxfinder.postload;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.DrugService;

import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;

import java.util.*;


public class DataProjectionTest extends BaseTest {


    CreateDataProjections createDataProjections;

    @MockBean
    DataImportService dataImportService;

    @MockBean
    DrugService drugService;


    @Before
    public void init() {

        createDataProjections = new CreateDataProjections(dataImportService, drugService);


    }

    @Test
    public void given_HashMapDP_when_saveDP_then_Saved(){
        createDataProjections.setCytogeneticsDP(getTwoKeyData());
        createDataProjections.setMutatedPlatformMarkerVariantModelDP(getThreeKeyData());
        createDataProjections.setModelDrugResponseDP(getTwoKeyData());
        createDataProjections.setImmunoHistoChemistryDP(getTwoKeyData());
        createDataProjections.setCopyNumberAlterationDP(getOneKeyData());
        createDataProjections.setExpressionDP(getTwoKeyData());
        createDataProjections.setDrugDosingDP(getOneKeyData());
        createDataProjections.setPatientTreatmentDP(getOneKeyData());
        createDataProjections.setFrequentlyMutatedMarkersDP(new ArrayList<>());
        createDataProjections.setExpressionDP(getTwoKeyData());
        createDataProjections.setDataAvailableDP(new HashedMap<>());
        createDataProjections.setFrequentlyMutatedMarkersDP(new ArrayList<>());

        when(dataImportService.saveDataProjection(any())).thenAnswer(i -> i.getArguments()[0]);
        when(dataImportService.findDataProjectionByLabel(any())).thenReturn(null);

        createDataProjections.saveDataProjections();

        Assert.assertEquals("cytogenetics", createDataProjections.saveDP("cytogenetics",
                createDataProjections.getCytogeneticsDP()).getLabel());
        Assert.assertEquals("copy number alteration", createDataProjections.saveDP("copy number alteration",
                createDataProjections.getCopyNumberAlterationDP()).getLabel());
        Assert.assertEquals("expression", createDataProjections.saveDP("expression",
                createDataProjections.getExpressionDP()).getLabel());
        Assert.assertEquals("PlatformMarkerVariantModel", createDataProjections.saveDP("PlatformMarkerVariantModel",
                createDataProjections.getMutatedPlatformMarkerVariantModelDP()).getLabel());
        Assert.assertEquals("ModelDrugData", createDataProjections.saveDP("ModelDrugData",
                createDataProjections.getModelDrugResponseDP()).getLabel());
        Assert.assertEquals("breast cancer markers", createDataProjections.saveDP("breast cancer markers",
                createDataProjections.getImmunoHistoChemistryDP()).getLabel());
        Assert.assertEquals("drug dosing counter", createDataProjections.saveDP("drug dosing counter",
                createDataProjections.getDrugDosingDP()).getLabel());
        Assert.assertEquals("patient treatment", createDataProjections.saveDP("patient treatment",
                createDataProjections.getPatientTreatmentDP()).getLabel());
        Assert.assertEquals("MarkerVariant", createDataProjections.saveDP("MarkerVariant",
                createDataProjections.getMutatedMarkerVariantDP()).getLabel());
        Assert.assertEquals("data available", createDataProjections.saveDP("data available",
                createDataProjections.getDataAvailableDP()).getLabel());
        Assert.assertEquals("frequently mutated genes", createDataProjections.saveDP("frequently mutated genes",
                createDataProjections.getFrequentlyMutatedMarkersDP()).getLabel());
    }

    @Test
    public void given_Json_when_jsonKeyIsNull_then_ExceptionIsThrown(){
        Map<String, Long> map = new HashedMap<>();
        map.put(null, new Long(1));
        Assert.assertEquals("", createDataProjections.createJsonString(map));

    }

    @Test
    public void given_TreatmentSummary_when_Process_then_MapIsPopulated(){
        TreatmentSummary ts = getTreatmentSummary();
        createDataProjections.processModelDrugs(new Long(1), ts);
        Assert.assertEquals(true,createDataProjections.getModelDrugResponseDP().containsKey("label5regimen"));
        Assert.assertEquals(true,createDataProjections.getModelDrugResponseDP().containsKey("label1 and label2"));
    }

    private Map<String, Set<Long>> getOneKeyData(){

        Set<Long> modelIds = new HashSet<>();
        modelIds.add(new Long(1));
        Map<String, Set<Long>> map1 = new HashMap<>();
        map1.put("key1", modelIds);
        return map1;
    }

    private Map<String, Map<String, Set<Long>>> getTwoKeyData(){

        Set<Long> modelIds = new HashSet<>();
        modelIds.add(new Long(1));
        Map<String, Set<Long>> map2 = new HashMap<>();
        Map<String, Map<String, Set<Long>>> map1 = new HashMap<>();
        map2.put("key2", modelIds);
        map1.put("key1", map2);
        return map1;
    }

    private Map<String, Map<String, Map<String, Set<Long>>>> getThreeKeyData(){

        Set<Long> modelIds = new HashSet<>();
        modelIds.add(new Long(1));
        Map<String, Set<Long>> map3 = new HashMap<>();
        Map<String, Map<String, Set<Long>>> map2 = new HashMap<>();
        Map<String, Map<String, Map<String, Set<Long>>>> map1 = new HashMap<>();
        map3.put("key3",modelIds);
        map2.put("key2", map3);
        map1.put("key1", map2);
        return map1;
    }


    private TreatmentSummary getTreatmentSummary(){
        TreatmentSummary ts = new TreatmentSummary();
        TreatmentProtocol tp1 = new TreatmentProtocol();
        TreatmentProtocol tp2 = new TreatmentProtocol();
        Response response = new Response();
        response.setDescription("description");
        tp1.setResponse(response);
        tp2.setResponse(response);
        TreatmentComponent tc1 = new TreatmentComponent();
        TreatmentComponent tc2 = new TreatmentComponent();
        TreatmentComponent tc3 = new TreatmentComponent();
        Treatment t1 = new Treatment("regimendrug1");
        Treatment t2 = new Treatment("drug2");
        Treatment t3 = new Treatment("drug3");
        TreatmentToOntologyRelationship ttor1 = new TreatmentToOntologyRelationship();
        TreatmentToOntologyRelationship ttor2 = new TreatmentToOntologyRelationship();
        TreatmentToOntologyRelationship ttor3 = new TreatmentToOntologyRelationship();
        OntologyTerm ot1 = new OntologyTerm("url1", "label1");
        OntologyTerm ot2 = new OntologyTerm("url2", "label2");
        OntologyTerm ot3 = new OntologyTerm("url3", "label3");
        OntologyTerm ot4 = new OntologyTerm("url4", "label4");
        ot1.setType("Drug");
        ot2.setType("Drug");
        ot3.setType("Drug");
        ot4.setType("Drug");
        OntologyTerm regimen = new OntologyTerm("url5", "label5regimen");
        regimen.setType("treatment regimen");
        regimen.addSubclass(ot3);
        regimen.addSubclass(ot4);
        t1.setTreatmentToOntologyRelationship(ttor1);
        ttor1.setOntologyTerm(regimen);
        tc1.setTreatment(t1);
        tp1.addTreatmentComponent(tc1);
        ts.addTreatmentProtocol(tp1);
        tc2.setTreatment(t2);
        tc3.setTreatment(t3);
        t2.setTreatmentToOntologyRelationship(ttor2);
        t3.setTreatmentToOntologyRelationship(ttor3);
        ttor2.setOntologyTerm(ot1);
        ttor3.setOntologyTerm(ot2);
        tp2.addTreatmentComponent(tc2);
        tp2.addTreatmentComponent(tc3);
        ts.addTreatmentProtocol(tp2);
        return ts;
    }

}
