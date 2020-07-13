package org.pdxfinder.services;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;


public class UtilityServiceTest extends BaseTest {

    private Logger log = LoggerFactory.getLogger(UtilityServiceTest.class);

    private ObjectMapper mapper;

    private static final String JSON_IRI_KEY = "iri";
    private static final String JSON_IRI_VALUE = "http://purl.obolibrary.org/obo/NCIT_C4741";
    private static final String JSON_LABEL_KEY = "label";
    private static final String JSON_LABEL_VALUE = "Neoplasm by Morphology";


    private String camelCaseString = "TheMethodWillConvertTheCamelCaseCsvHeaderIntoUserReadableSentence";
    private String sentenceString = "The Method Will Convert The Camel Case Csv Header Into User Readable Sentence";

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
        String newDirectory = "testDirectory";
        File volatileTestFolder = temporaryFolder.newFolder("subfolder");

        String directoryNameToCreate = String.format("%s/%s/", volatileTestFolder.getAbsolutePath(), newDirectory);

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

    @Test
    public void given_SourceAndDestination_When_MoveFileInvoked_Then_FileIsMoved() throws IOException {

        // given
        String fileName = "output.txt";
        String fileContent = "file content string";
        String newFolderName = "subfolder";

        // create source file in a temp directory
        File sourceFile = temporaryFolder.newFile(fileName);

        // Write data into the temp file.
        FileUtils.writeStringToFile(sourceFile, fileContent);
        String source = sourceFile.getPath();

        // build a destination String without creating the file
        File expectedDestination = temporaryFolder.newFolder(newFolderName)
                .toPath()
                .resolve(fileName)
                .toFile();

        // when
        utilityService.moveFile(source, expectedDestination.getPath());

        // Read the moved file content
        String actual = FileUtils.readFileToString(expectedDestination);

        // Then
        assertTrue(expectedDestination.exists());
        assertThat(expectedDestination).hasName(fileName).hasContent(actual).hasParent(resolvePath(newFolderName));
    }

    @Test
    public void given_FileDir_When_ListAllFilesInADirectoryInvoked_Then_FileIsMoved() throws IOException {

        // given
        String fileNamePrefix = "tempFile";

        // create five files in a temp directory
        File tempFile1 = temporaryFolder.newFile(String.format("%s1.txt", fileNamePrefix));
        File tempFile2 = temporaryFolder.newFile(String.format("%s2.txt", fileNamePrefix));
        File tempFile3 = temporaryFolder.newFile(String.format("%s3.txt", fileNamePrefix));
        File tempFile4 = temporaryFolder.newFile(String.format("%s4.txt", fileNamePrefix));
        File tempFile5 = temporaryFolder.newFile(String.format("%s5.txt", fileNamePrefix));

        List<String> expectedFiles = Arrays.asList(tempFile1.getName(),
                                      tempFile2.getName(),
                                      tempFile3.getName(),
                                      tempFile4.getName(),
                                      tempFile5.getName()
        );
        Set<String> expected = new HashSet<>(expectedFiles);

        // Retrieve the temp directory where the files are stored
        String tempDir = tempFile1.getParent();

        // when
        Set<String> actual = new HashSet<>(
                utilityService.listAllFilesInADirectory(tempDir)
        );

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void given_CamelCaseString_When_CamelCaseToSentenceInvoked_Then_ReturnReadableSentence(){

        // given
        String camelCaseString = this.camelCaseString;
        String expected = this.sentenceString;

        // when
        String actual = utilityService.camelCaseToSentence(camelCaseString);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void given_SentenceString_When_SentenceToCamelCaseInvoked_Then_ReturnCamelCase(){

        // given
        String sentenceString = this.sentenceString;
        String expected = this.camelCaseString;

        // when
        String actual = utilityService.sentenceToCamelCase(sentenceString);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    public void given_DelimitedString_When_SplitTextInvoked_Then_ReturnExpected() throws IOException {

        // given
        String data = "Sample string,will,be,tokenized,on,pipe,and,replaced,with,space";
        String delimiter = ",";
        String separator = " ";
        String expected = "Sample string will be tokenized on pipe and replaced with space";

        // when
        String actual = utilityService.splitText(data, delimiter, separator);

        // Then
        assertEquals(expected, actual);
    }
}