package org.pdxfinder.dataexport;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class DataExportTest extends BaseTest {


    @MockBean
    private DataImportService dataImportService;
    @MockBean
    protected UtilityService utilityService;

    Group providerGroup;
    UniversalDataExporter universalDataExporter;

    @Before
    public void setUp() {

        providerGroup = Group.createProviderGroup("TestGroup", "TG", "", "Academia", "Bob", "Bob's page");

        universalDataExporter = new UniversalDataExporter(dataImportService, utilityService);
        //universalDataExporter.init("", providerGroup);
    }

    @Test
    public void Given_PatientAndProvider_When_GetPatientSheetIsCalled_Then_PatientDataIsInRowOne() {

        when(dataImportService.findPatientsByGroup(providerGroup))
          .thenReturn(getPatientListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initPatientData();
        List<List<String>> patientData = universalDataExporter.getPatientSheetDataExport();

        Assert.assertEquals("p123", patientData.get(0).get(0));
    }

    @Test
    public void Given_PatientAndModelAndSample_When_PatientTumorSheetIsCalled_Then_PatientDataIsInRowOne(){

        Patient patient = new Patient("p123", "male", "", "", providerGroup);
        PatientSnapshot patientSnapshot = new PatientSnapshot();
        patientSnapshot.setAgeAtCollection("65");
        Sample sample = new Sample();
        sample.setSourceSampleId("s123");

        TumorType tt = new TumorType("Metastatic");
        Tissue ot = new Tissue("Brain");
        Tissue ss = new Tissue("Brain");

        sample.setType(tt);
        sample.setOriginTissue(ot);
        sample.setSampleSite(ss);

        patientSnapshot.addSample(sample);
        patient.addSnapshot(patientSnapshot);

        List<Patient> patientList = new ArrayList<>();
        patientList.add(patient);

        ModelCreation modelCreation = new ModelCreation();
        modelCreation.setSourcePdxId("m123");

        when(dataImportService.findPatientTumorAtCollectionDataByDS(providerGroup))
          .thenReturn(patientList);
        when(dataImportService.findModelBySample(sample))
          .thenReturn(modelCreation);

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initPatientTumorAtCollection();

        List<List<String>> patientTumorAtCollection = universalDataExporter.getPatientTumorSheetDataExport();
        Assert.assertEquals("p123", patientTumorAtCollection.get(0).get(0));
    }

    @Test
    public void Given_ModelWithDetails_When_GetModelDetailsIsCalled_Then_ModelDataIsInRowOne(){

        when( dataImportService.findModelsWithSpecimensAndQAByDS(providerGroup.getAbbreviation()))
          .thenReturn(getModelListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initPdxModelDetails();
        universalDataExporter.initPdxModelValidations();

        List<List<String>> pdxModelDetails = universalDataExporter.getPdxModelSheetDataExport();
        List<List<String>> pdxModelValidations = universalDataExporter.getPdxModelValidationSheetDataExport();

        Assert.assertEquals("m123", pdxModelDetails.get(0).get(0));
        Assert.assertEquals("hsname", pdxModelDetails.get(0).get(1));

        Assert.assertEquals("m123", pdxModelValidations.get(0).get(0));
        Assert.assertEquals("technology", pdxModelValidations.get(0).get(1));

    }

    @Test
    public void Given_Provider_When_GetSharingAndContactSheetIsCalled_Then_SharingAndContactDataIsInRowOne(){

        when(dataImportService.findModelsWithSharingAndContactByDS(providerGroup.getAbbreviation()))
          .thenReturn(getModelListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initSharingAndContact();

        List<List<String>> sharingAndContact = universalDataExporter.getSharingAndContactSheetDataExport();

        Assert.assertEquals("m123", sharingAndContact.get(0).get(0));
        Assert.assertEquals("Academia", sharingAndContact.get(0).get(1));

    }

    @Test
    public void Given_Provider_When_GetLoaderRelatedSheetIsCalled_Then_DataIsInRowOne(){

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initLoaderRelatedData();

        List<List<String>> loaderRelatedData = universalDataExporter.getLoaderRelatedDataSheetDataExport();

        Assert.assertEquals("TG", loaderRelatedData.get(0).get(1));


    }

    @Test
    public void Given_ModelWithMolecularData_When_GetSamplePlatformSheetIsCalled_Then_SamplePlatformDataIsInRowOne(){

        when(dataImportService.findModelXenograftPlatformSampleByDS(providerGroup.getAbbreviation()))
          .thenReturn(getModelListForTest());

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initSamplePlatformDescription();

        List<List<String>> samplePlatformDescription = universalDataExporter.getSamplePlatformDescriptionSheetDataExport();

        Assert.assertEquals("s123", samplePlatformDescription.get(0).get(1));
        Assert.assertEquals("xs123", samplePlatformDescription.get(1).get(1));

    }

    @Test
    public void Given_ModelsWithOmic_When_GetOmicSheetsAreCalled_Then_DataIsInRowOne(){

        when(dataImportService.findModelsWithSharingAndContactByDS(providerGroup.getAbbreviation()))
          .thenReturn(getModelListForTest());
        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(providerGroup.getAbbreviation(), "m123", "mutation"))
          .thenReturn(getModelListForTest().get(0));
        when(dataImportService.findModelWithMolecularDataByDSAndIdAndMolcharType(providerGroup.getAbbreviation(), "m123", "copy number alteration"))
          .thenReturn(getModelListForTest().get(0));

        universalDataExporter.setDs(providerGroup);
        universalDataExporter.initMutationData();
        universalDataExporter.initCNAData();

        List<List<String>> mutationData = universalDataExporter.getMutationSheetDataExport();
        List<List<String>> cnaData = universalDataExporter.getCnaSheetDataExport();

        Assert.assertEquals("m123", mutationData.get(0).get(1));
        Assert.assertEquals("markersymbol", mutationData.get(0).get(6));

        Assert.assertEquals("m123", cnaData.get(0).get(1));
        Assert.assertEquals("markersymbol", cnaData.get(0).get(6));

    }


    @Test
    public void Given_SheetRowData_When_UpdateSheetIsCalled_Then_SheetIsUpdated(){

        Workbook wb = new XSSFWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");

        List<String> rowData = new ArrayList<>(Arrays.asList("1","2","3","4"));
        List<List<String>> data = new ArrayList<>();
        data.add(rowData);

        universalDataExporter.updateSheetWithData(sheet1, data, 1, 1);

        Assert.assertEquals("2", sheet1.getRow(0).getCell(1).getStringCellValue());
    }

    @Test
    public void Given_dataIsReadyToExport_When_exportIsCalled_Then_createCorrectFileStructure() throws IOException {

        Path metaData = Paths.get( "/tmp/metadata_template.xlsx");
        Path samplePlatform = Paths.get("/tmp/sampleplatform_template.xlsx" );
        Path mutation = Paths.get("/tmp/mutation_template.xlsx");
        Path cnaTemplate = Paths.get("/tmp/cna_template.xlsx");

        Workbook metaDataXlsx = new XSSFWorkbook();
        Workbook samplePlatformXlsx = new XSSFWorkbook();
        Workbook mutationXlsx = new XSSFWorkbook();                                                           
        Workbook cnaTemplateXlsx = new XSSFWorkbook();

        for(int i = 0; i < 7; i++) {
            metaDataXlsx.createSheet(String.format("%s", i));
        }

        samplePlatformXlsx.createSheet("0");
        mutationXlsx.createSheet("0");
        cnaTemplateXlsx.createSheet("0");

        metaDataXlsx.write(new FileOutputStream(metaData.toFile()));
        samplePlatformXlsx.write(new FileOutputStream(samplePlatform.toFile()));
        mutationXlsx.write(new FileOutputStream(mutation.toFile()));
        cnaTemplateXlsx.write(new FileOutputStream(cnaTemplate.toFile()));

        try {
            universalDataExporter.setTemplateDir("/tmp");
            universalDataExporter.setDs(providerGroup);
            universalDataExporter.initPatientData();
            universalDataExporter.export("/tmp");
        } finally {

            Files.delete(metaData);
            Files.delete(samplePlatform);
            Files.delete(mutation);
            Files.delete(cnaTemplate);
        }

        Path expectedProviderDir = Paths.get("/tmp/TG");
        Path expectedCnaDir = Paths.get("/tmp/TG/cna");
        Path expectedMutDir = Paths.get("/tmp/TG/mut");
        Path expectedMetaData = Paths.get("/tmp/TG/metadata.xlsx");

        Assert.assertTrue(expectedProviderDir.toFile().exists());
        Assert.assertTrue(expectedCnaDir.toFile().exists());
        Assert.assertTrue(expectedMutDir.toFile().exists());
        Assert.assertTrue(expectedMetaData.toFile().exists());

    }




    private List<ModelCreation> getModelListForTest(){

        List<ModelCreation> modelCreationList = new ArrayList<>();

        ModelCreation model = new ModelCreation();
        model.setSourcePdxId("m123");

        Group accessGroup = Group.createAccessibilityGroup("Academia", "transnational");
        Group project = new Group("project1", "p1", "Project");

        Group publicationGroup = new Group();
        publicationGroup.setType("Publication");
        publicationGroup.setPubMedId("12345");

        model.addGroup(publicationGroup);
        model.addGroup(providerGroup);
        model.addGroup(accessGroup);
        model.addGroup(project);

        ExternalUrl url = new ExternalUrl(ExternalUrl.Type.CONTACT,"email@address.com");
        List<ExternalUrl> urlList = new ArrayList<>();
        urlList.add(url);
        model.setExternalUrls(urlList);

        QualityAssurance qualityAssurance = new QualityAssurance("technology", "description", "1,2");
        model.addQualityAssurance(qualityAssurance);

        Sample patientSample = new Sample();
        patientSample.setSourceSampleId("s123");

        model.setSample(patientSample);

        MolecularCharacterization molecularCharacterization = new MolecularCharacterization();
        molecularCharacterization.setType("mutation");
        molecularCharacterization.setTechnology("techtest");
        Platform platform = new Platform();
        platform.setName("platform");
        platform.setUrl("platformurl");

        molecularCharacterization.setPlatform(platform);
        patientSample.addMolecularCharacterization(molecularCharacterization);

        //setting up the xeno sample
        Specimen specimen = new Specimen();
        specimen.setPassage("1");
        specimen.setHostStrain(new HostStrain("hssymbol", "hsname"));

        Sample xenoSample = new Sample();
        xenoSample.setSourceSampleId("xs123");
        specimen.setSample(xenoSample);
        xenoSample.addMolecularCharacterization(molecularCharacterization);
        model.addSpecimen(specimen);


        modelCreationList.add(model);


        //setting up mutation data
        MarkerAssociation ma = new MarkerAssociation();
        MolecularData md = new MolecularData();
        md.setAminoAcidChange("aminoacidchange");
        Marker m = new Marker("markersymbol", "markername");
        md.setMarker(m.getHgncSymbol());
        ma.addMolecularData(md);
        molecularCharacterization.addMarkerAssociation(ma);

        //setting up cna data
        MolecularCharacterization molecularCharacterization2 = new MolecularCharacterization();
        molecularCharacterization2.setType("copy number alteration");
        molecularCharacterization2.setPlatform(platform);
        MarkerAssociation ma2 = new MarkerAssociation();
        MolecularData md2 = new MolecularData();
        md2.setCnaCopyNumberStatus("cnaStatus");
        Marker m2 = new Marker("markersymbol", "markername");
        md2.setMarker(m2.getHgncSymbol());
        ma2.addMolecularData(md2);
        molecularCharacterization2.addMarkerAssociation(ma2);
        xenoSample.addMolecularCharacterization(molecularCharacterization2);

        return modelCreationList;
    }

    private List<Patient> getPatientListForTest(){

        Patient patient = new Patient("p123", "male", "", "", providerGroup);
        patient.setCancerRelevantHistory("");
        patient.setFirstDiagnosis("");
        patient.setAgeAtFirstDiagnosis("60");

        List<Patient> patientList = new ArrayList<>();
        patientList.add(patient);



        return patientList;
    }


}
