package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.TSV;
import org.pdxfinder.services.OmicTransformationService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.utils.CbpTransformer;
import org.pdxfinder.utils.CbpTransformer.cbioType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CbpTransformerTests extends BaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private UtilityService utilityService;
    @Mock
    private OmicTransformationService omicTransformationService;
    @Mock
    private UniversalDataWriterServices universalDataWriterUtilities;
    @Captor
    private ArgumentCaptor<ArrayList<List<String>>> captor;

    @InjectMocks
    private CbpTransformer cbpTransformer;

    private File jsonDummy;
    private File exportFolder;
    private cbioType mutDataType;
    private cbioType gisticDataType;
    private String templatesFolder;

    @Before
    public void init() throws IOException {
        jsonDummy = folder.newFile("UtilityTest.json");
        mutDataType = cbioType.MUT;
        gisticDataType = cbioType.GISTIC;

        TemporaryFolder rootFolder = new TemporaryFolder();
        rootFolder.create();
        exportFolder = rootFolder.newFolder();

        TemporaryFolder templateRoot = new TemporaryFolder();
        templateRoot.create();
        templatesFolder = templateRoot.getRoot().getAbsolutePath();
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.metadata_template.fileName, 7);
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.sampleplatform_template.fileName, 2);
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.mutation_template.fileName, 2);
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.cna_template.fileName, 2);
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.cytogenetics_template.fileName, 2);
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.expression_template.fileName, 2);
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.drugdosing_template.fileName, 2);
        writeWorkbook(templatesFolder + "/" + TSV.templateNames.patienttreatment_template.fileName, 2);
    }

    private void writeWorkbook(String directory, int sheetCount) throws IOException {
        OutputStream out = new FileOutputStream(directory);
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (int i = 0; i < sheetCount; i++) {
            workbook.createSheet();
        }
        workbook.write(out);
    }


    @Test(expected = IOException.class)
    public void Given_nonExistentJsonFilesArePassed_WhenExportCBPisCalled_Then_throwIOexception() throws IOException {
        cbpTransformer.exportCBP(exportFolder, new File(templatesFolder), new File("/tmp/not/existing"), mutDataType);
    }

    @Test(expected = IOException.class)
    public void Given_nonExistentTemplateDirectoryisPassed_WhenExportCBPisCalled_Then_throwIOexception() throws IOException {
        cbpTransformer.exportCBP(exportFolder, new File("/Fake/Path/"), jsonDummy, mutDataType);
    }

    @Test
    public void Give_JsonArrayAndValidImportDirectory_When_exportsIsCalled__ThenNewMutDirExists() throws IOException {
        String mut = TSV.molecular_characterisation_type.mut.mcType;
        String mutFileId = TSV.molecular_characterisation_type.mut.name();
        String ns = "Not Specified";

        List<Map<String, Object>> dummyListMap = new ArrayList<>();
        Map<String, Object> dummyMap= new HashMap<>();
        dummyMap.put("patientId","1");
        dummyMap.put("sampleId","2");
        dummyMap.put("entrezGeneId","00001");
        dummyMap.put("chr","3");
        dummyMap.put("startPosition","4");
        dummyMap.put("referenceAllele","5");
        dummyMap.put("variantAllele","6");
        dummyMap.put("ncbiBuild","7");
        dummyListMap.add(dummyMap);

        when(utilityService.serializeJSONToMaps(jsonDummy.getAbsolutePath()))
                .thenReturn(dummyListMap);

        cbpTransformer.exportCBP(exportFolder, new File(templatesFolder), jsonDummy, mutDataType);

        String filename = Paths.get(jsonDummy.getAbsolutePath()).getFileName().toString();
        String expectedExportURI = String.format("%s/%s/%s/%s_%s",exportFolder, filename,"mut",filename, mutFileId);
        verify(universalDataWriterUtilities).writeSingleOmicFileToTsv(eq(expectedExportURI), any(Sheet.class), anyList());
    }

    @Test
    public void Give_nestedGeneInformationInJason_When_cbioportalExporterIsCalled_ThenFileContainsGeneInformation() throws IOException {
        String cnaFileId = TSV.molecular_characterisation_type.cna.name();
        String geneSymbol = "CELSR1";

        Map<String, Object> geneMap = new LinkedHashMap<>();
        geneMap.put("entrezGeneId",9620);
        geneMap.put("hugoGeneSymbol", geneSymbol);
        geneMap.put("type", "protien_coding");

        List<Map<String, Object>> dummyListMap = new ArrayList<>();
        Map<String, Object> dummyMap= new HashMap<>();
        dummyMap.put("patientId","1");
        dummyMap.put("sampleId","2");
        dummyMap.put("entrezGeneId","00001");
        dummyMap.put("chr","3");
        dummyMap.put("gene", geneMap);
        dummyMap.put("startPosition","4");
        dummyMap.put("referenceAllele","5");
        dummyMap.put("alteration","2");
        dummyMap.put("ncbiBuild","7");
        dummyListMap.add(dummyMap);

        when(utilityService.serializeJSONToMaps(jsonDummy.getAbsolutePath()))
                .thenReturn(dummyListMap);

        cbpTransformer.exportCBP(exportFolder, new File(templatesFolder), jsonDummy, gisticDataType);

        String filename = Paths.get(jsonDummy.getAbsolutePath()).getFileName().toString();
        String expectedExportURI = String.format("%s/%s/%s/%s_%s",exportFolder, filename,"cna",filename, cnaFileId);
        verify(universalDataWriterUtilities).writeSingleOmicFileToTsv(eq(expectedExportURI), any(Sheet.class), captor.capture());

        String geneName = captor.getValue().get(0).get(8);
        Assert.assertEquals(geneName, geneSymbol);
    }
}


