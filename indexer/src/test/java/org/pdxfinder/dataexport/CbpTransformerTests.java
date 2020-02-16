package org.pdxfinder.dataexport;


import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.utils.CbpTransformer;

import java.io.*;


public class CbpTransformerTests extends BaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private CbpTransformer cbpTransformer = new CbpTransformer();
    private File jsonDummy;

    @Before
    public void init() throws IOException {
        jsonDummy = folder.newFile("UtilityTest.json");
    }


    @Test
    public void Give_JsonArrayAndValidImportDirectory_When_exportsIsCalled__ThenNewMutDirExists() throws IOException {

        String ns = "Not Specified";

        TemporaryFolder rootFolder = new TemporaryFolder();
        rootFolder.create();
        File exportFolder = rootFolder.newFolder();
        File templatesFolder = rootFolder.newFolder();
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Test");
        FileOutputStream out = new FileOutputStream(new File(templatesFolder.getAbsoluteFile() + "/mutation_template.xlsx"));
        workbook.write(out);
        out.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(jsonDummy));
        writer.write("[ { \"patientId\":\"1\", \"sampleId\":\"2\", \"chr\":\"3\", \"startPosition\":\"4\", \"referenceAllele\":\"5\", \"variantAllele\":\"6\", \"ncbiBuild\":\"7\"} ] ");
        writer.close();

        cbpTransformer.exportCBP(exportFolder.getAbsolutePath(), templatesFolder.getAbsolutePath(), jsonDummy.getAbsolutePath());

        File actualGroupFile = new File(exportFolder.getAbsoluteFile() + "/UtilityTest.json");
        File actualMutFile = new File(actualGroupFile + "/mut");
        File outputData = new File(actualMutFile + "/data.xlsx");

        Assert.assertTrue(actualGroupFile.exists());
        Assert.assertTrue(actualMutFile.exists());
        Assert.assertTrue(outputData.exists());
    }
}


