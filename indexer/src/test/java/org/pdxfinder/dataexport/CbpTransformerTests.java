package org.pdxfinder.dataexport;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.OmicTransformationService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.utils.CbpTransformer;
import org.pdxfinder.utils.CbpTransformer.cbioType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;


public class CbpTransformerTests extends BaseTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private UtilityService utilityService;
    @Mock
    private OmicTransformationService omicTransformationService;
    @Mock
    private UniversalDataWriterUtilities universalDataWriterUtilities;

    @InjectMocks
    private CbpTransformer cbpTransformer;

    private File jsonDummy;
    private File exportFolder;
    private File templatesFolder;
    private cbioType mutDataType;
    private cbioType gisticDataType;

    @Before
    public void init() throws IOException {
        jsonDummy = folder.newFile("UtilityTest.json");
        mutDataType = cbioType.MUT;
        gisticDataType = cbioType.GISTIC;

        TemporaryFolder rootFolder = new TemporaryFolder();
        rootFolder.create();
        exportFolder = rootFolder.newFolder();
        templatesFolder = rootFolder.newFolder();
        createMockTemplate();
    }

    public void createMockTemplate() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet omic = workbook.createSheet("Test");
        Row headers = omic.createRow(0);
        for(int i = 0; i < 25; i++){
            headers.createCell(i).setCellValue(i );
        }

        FileOutputStream out = new FileOutputStream(new File(templatesFolder.getAbsoluteFile() + "/mutation_template.xlsx"));
        workbook.write(out);
        out.close();

        FileOutputStream cnaOut = new FileOutputStream(new File(templatesFolder.getAbsoluteFile() + "/cna_template.xlsx"));
        workbook.write(cnaOut);
        out.close();
    }

    @Test(expected = IOException.class)
    public void Given_nonExistentJsonFilesArePassed_WhenExportCBPisCalled_Then_throwIOexception() throws IOException {
        cbpTransformer.exportCBP(exportFolder, templatesFolder, new File("/tmp/not/existing"), mutDataType);
    }

    @Test(expected = IOException.class)
    public void Given_nonExistentTemplateDirectoryisPassed_WhenExportCBPisCalled_Then_throwIOexception() throws IOException {
        cbpTransformer.exportCBP(exportFolder, new File("/Fake/Path/"), jsonDummy, mutDataType);
    }

    @Test
    public void Give_JsonArrayAndValidImportDirectory_When_exportsIsCalled__ThenNewMutDirExists() throws IOException {
        String ns = "Not Specified";

        List<Map<String, Object>> dummyListMap = new ArrayList<Map<String,Object >>();
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


        cbpTransformer.exportCBP(exportFolder, templatesFolder, jsonDummy, mutDataType);

        //Mockito.verify(universalDataExporter, times(1)).export
    }
}


