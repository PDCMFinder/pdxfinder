package org.pdxfinder.dataexport;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.utils.CbpTransformer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.fail;

public class CbpTransformerTests extends BaseTest {

    private UtilityService utilityService = new UtilityService();
    private CbpTransformer cbpTransformer = new CbpTransformer();

    private File jsonDummy;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void init() throws IOException {
        jsonDummy = folder.newFile("UtilityTest.json");
    }

    @Test
    public void Given_seralizeJsonUriToString_WhenBlankCalled_ReturnNull() throws IOException, JSONException {

        JSONObject jsObject = utilityService.seralizeJsonURItoJsonObject(jsonDummy.getAbsolutePath());
        Assert.assertNull(jsObject);
    }

    @Test
    public void Given_givenBasicJsonInFile_WhenSerializeJsonURItoJsonArray_ReturnBasicArray() throws IOException, JSONException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonDummy));
        writer.write("{}");
        writer.close();

        JSONObject jsonObject = utilityService.seralizeJsonURItoJsonObject(jsonDummy.getAbsolutePath());
        Assert.assertEquals(jsonObject.toString(), "{}");

    }


    @Test
    public void Given_BasicJsonContainingFile_When_SerializeJsonUriToJsonArrayIsCalled_ReturnJsonNodeContainingCorrectData() throws IOException, JSONException {

        String expectedValueKeyTest1 = "1";
        String expectedValueKeyTest2 = "2";
        String expectedValueKeyTest3 = "3";

        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonDummy));
        writer.write("{ \"Test1\":\"1\", \"Test2\" : \"2\", \"Test3\" : \"3\" }");
        writer.close();

        JSONObject actuaObject = utilityService.seralizeJsonURItoJsonObject(jsonDummy.getAbsolutePath());

        Assert.assertEquals(expectedValueKeyTest1, actuaObject.getString("Test1"));
        Assert.assertEquals(expectedValueKeyTest2, actuaObject.getString("Test2"));
        Assert.assertEquals(expectedValueKeyTest3, actuaObject.getString("Test3"));
    }


    @Test
    public void Given_serializeJsonToListOfList_WhenBadUriIsCalled_Called_ReturnNull() throws IOException, JSONException {

        List<List<Object>> actualSheet = utilityService.serializeJsonToListOfLists("Bad/URI");
        Assert.assertNull(actualSheet);
    }

    @Test
    public void Given_BasicEmptyFile_When_serializeJsonToListOfLists_ReturnEmptyListOfList() throws IOException, JSONException {

        String expectedValueKeyTest1 = "1";
        String expectedValueKeyTest2 = "2";
        String expectedValueKeyTest3 = "3";

        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonDummy));
        writer.write("{ \"Test1\":\"1\", \"Test2\" : \"2\", \"Test3\" : \"3\" }");
        writer.close();

        List<List<Object>> actualList = utilityService.serializeJsonToListOfLists(jsonDummy.getAbsolutePath());
        Assert.assertEquals(1,actualList.size());
    }

    @Test
    public void Given_CalltoCbpTransformer_Fail() throws IOException {
        cbpTransformer.exportCBP("/tmp","DummyJson" );
    }


}
