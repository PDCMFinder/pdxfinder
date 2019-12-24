package org.pdxfinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.pdxfinder.services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UtilityServiceTest{

    private UtilityService utilityService = new UtilityService();

    Path csvFilePath;
    Path tsvFilePath;

    @Before
    public void init() throws IOException {

        //final TemporaryFolder testFolder = new TemporaryFolder();
        //testFolder.create();

        Path tmpPath = Paths.get("/tmp/");
        csvFilePath = Files.createTempFile(tmpPath,"test",".csv");
        tsvFilePath = Files.createTempFile(tmpPath, "test2",".tsv");

        String csvContentKey = "Key1,Key2,Key3,Key4,Key5\n";
        String csvContentValue = "Value1,Value2,Value3,Value4,Value5\n";
        String tsvContentKey = "Key1\tKey2\tKey3\tKey4\tKey5\n";
        String tsvContentValue = "Value1\tValue2\tValue3\tValue4\tValue5\n";

        FileWriter csvWriter = new FileWriter(csvFilePath.toString());
        FileWriter tsvWriter = new FileWriter(tsvFilePath.toString());

        csvWriter.write(csvContentKey);
        csvWriter.write(csvContentValue);
        tsvWriter.write(tsvContentKey);
        tsvWriter.write(tsvContentValue);

        tsvWriter.flush();
        csvWriter.flush();
        tsvWriter.close();
        csvWriter.close();
    }

    @Test
    public void Given_csvFileExistsWithHeaders_When_SerializeDataToMapsIsCalled_ReturnCorrectMap(){

        Map<String,String> expectedMap = new LinkedHashMap<>();

        expectedMap.put("Key1","Value1");
        expectedMap.put("Key2","Value2");
        expectedMap.put("Key3","Value3");
        expectedMap.put("Key4","Value4");
        expectedMap.put("Key5","Value5");

        List<Map<String,String>> actualMap = utilityService.serializeDataToMaps(csvFilePath.toString());

        Assert.assertEquals(expectedMap, actualMap);
    }

    @Test
    public void Given_tsvFileExistsWithHeaders_When_SerializeDataToMapsIsCalled_ReturnCorrectMap(){

        Map<String,String> expectedMap = new LinkedHashMap<>();

        expectedMap.put("Key1","Value1");
        expectedMap.put("Key2","Value2");
        expectedMap.put("Key3","Value3");
        expectedMap.put("Key4","Value4");
        expectedMap.put("Key5","Value5");

        List<Map<String,String>> actualMap = utilityService.serializeDataToMaps(tsvFilePath.toString());

        Assert.assertEquals(expectedMap, actualMap);
    }
}
