package org.pdxfinder.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.pdxfinder.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;


public class UtilityServiceTest extends BaseTest {

    private Logger log = LoggerFactory.getLogger(UtilityServiceTest.class);

    private ObjectMapper mapper;

    private static final String JSON_IRI_KEY = "iri";
    private static final String JSON_IRI_VALUE = "http://purl.obolibrary.org/obo/NCIT_C4741";
    private static final String JSON_LABEL_KEY = "label";
    private static final String JSON_LABEL_VALUE = "Neoplasm by Morphology";

    @InjectMocks
    private UtilityService utilityService;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {

        mapper = new ObjectMapper();
    }

    @Test
    public void given_JsonObject_When_JsonStringToNodeInvoked_Then_JsonNodeCreated() {

        // given
        ObjectNode testJsonObject = mapper.createObjectNode();
        testJsonObject.put(JSON_IRI_KEY, JSON_IRI_VALUE);
        testJsonObject.put(JSON_LABEL_KEY, JSON_LABEL_VALUE);
        String testJsonString = testJsonObject.toString();

        // when
        JsonNode jsonNode = utilityService.jsonStringToNode(testJsonString);


        // Then
        String expectedString = testJsonObject.get(JSON_IRI_KEY).asText();
        String actualString = jsonNode.get(JSON_IRI_KEY).asText();

        int expectedLabelNodeLength = 22;
        int actualLabelNodeLength = jsonNode.get(JSON_LABEL_KEY).asText().length();

        assertEquals(expectedString, actualString);
        assertSame(expectedLabelNodeLength, actualLabelNodeLength);
        assertSame(testJsonObject.getClass(), jsonNode.getClass());
    }


    @Test(expected = NullPointerException.class)
    public void given_WrongFormatJsonObject_When_JsonStringToNodeInvoked_Then_ReturnNullObject() {

        // given
        String wrongFormatJson = String.format("{ %s : %s }", JSON_IRI_KEY, JSON_IRI_VALUE);

        // when
        JsonNode expectedNullResult = utilityService.jsonStringToNode(wrongFormatJson);

        expectedNullResult.get(JSON_IRI_KEY);
    }


    @Test // testConvertLocalFileToByteException
    public void given_NonExistingFilePath_When_ConvertLocalFileToByteInvoked_Then_ReturnNullByteArray() throws IOException {

        // given : that we build a path to a non-existing file
        File nonExistingFilepath = temporaryFolder.newFolder("reports")
                .toPath()
                .resolve("tempFile.txt")
                .toFile();

        // when
        byte[] actual = utilityService.convertLocalFileToByte(nonExistingFilepath.getPath());

        // Then
        Assert.assertNull(actual);
    }


    @Test
    public void given_FilePath_When_ConvertLocalFileToByteInvoked_Then_ReturnFileByteArray() throws IOException {

        // given
        File tempFile = temporaryFolder.newFile("output.txt");

        byte[] expected = Files.readAllBytes(tempFile.toPath());

        // when
        byte[] actual = utilityService.convertLocalFileToByte(tempFile.getPath());

        // Then
        assertThat(tempFile).hasBinaryContent(actual);
        assertArrayEquals(expected, actual);
    }


    @Test(expected = StringIndexOutOfBoundsException.class)
    public void given_WrongFilePath_When_MkDirectoryFromFilePathNameInvoked_Then_ThrowException() throws IOException {

        // given
        String wrongFilePath = "fileDirectory";

        // when
        utilityService.mkDirectoryFromFilePathName(wrongFilePath);
    }


    @Test
    public void given_FilePath_When_MkDirectoryFromFilePathNameInvoked_Then_CreateDirectory() throws IOException {

        // given temporary test folder
        File volatileTestFolder = temporaryFolder.newFolder("subfolder");

        String directoryNameToCreate = volatileTestFolder.getAbsolutePath() + "/testDirectory/";

        // when ... call the utility service to create a directory "testDirectory" inside the "volatileTestFolder"
        utilityService.mkDirectoryFromFilePathName(directoryNameToCreate);

        File directory = new File(directoryNameToCreate);

        // Then assert that the test directory was created
        assertTrue(directory.exists());
        assertThat(directory).hasName("testDirectory").isDirectory().hasParent(resolvePath("subfolder"));
    }


    private String resolvePath(String folder) {
        return temporaryFolder
                .getRoot().toPath()
                .resolve(folder)
                .toString();
    }

}