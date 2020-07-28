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
    public void given_CytoGenetics_when_saveDP_then_Saved(){
        createDataProjections.setCytogeneticsDP(getCytogeneticsData());

        when(dataImportService.saveDataProjection(any())).thenAnswer(i -> i.getArguments()[0]);
        Assert.assertEquals("cytogenetics", createDataProjections.saveDP("cytogenetics", createDataProjections.getCytogeneticsDP()).getLabel());


    }

    private Map<String, Map<String, Set<Long>>> getCytogeneticsData(){
        //marker=>status=>set of model ids
        Set<Long> modelIds = new HashSet<>();
        modelIds.add(new Long(1));
        Map<String, Set<Long>> status = new HashedMap<>();
        status.put("positive",modelIds);
        Map<String,Map<String, Set<Long>>> markers = new HashedMap<>();
        markers.put("KRAS", status);
        return markers;

    }

}
