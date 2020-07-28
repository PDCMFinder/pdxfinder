package org.pdxfinder.postload;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.DrugService;

import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


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


        when(dataImportService.saveDataProjection(any())).thenAnswer(i -> i.getArguments()[0]);
        when(dataImportService.findDataProjectionByLabel(any())).thenReturn(null);
        Assert.assertEquals("cytogenetics", createDataProjections.saveDP("cytogenetics",
                createDataProjections.getCytogeneticsDP()).getLabel());
        Assert.assertEquals("copy number alteration", createDataProjections.saveDP("copy number alteration",
                createDataProjections.getCopyNumberAlterationDP()).getLabel());
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

    }

    @Test
    public void given_Json_when_jsonKeyIsNull_then_ExceptionIsThrown(){
        Map<String, Long> map = new HashedMap<>();
        map.put(null, new Long(1));
        Assert.assertEquals("", createDataProjections.createJsonString(map));

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

}
