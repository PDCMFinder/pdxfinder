package org.pdxfinder.transcommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.controller.TransController;
import org.pdxfinder.transdatamodel.PdmrPdxInfo;
import org.pdxfinder.transrepository.PdmrPdxInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Mosaku Abayomi on 12/12/2017.
 */

@Service
public class DataTransformerService {

    ObjectMapper mapper = new ObjectMapper();
    private PdmrPdxInfoRepository pdmrPdxInfoRepository;
    private String DATASOURCE_URL_PREFIX = "https://pdmdb.cancer.gov/pls/apex/f?p=101:4:0::NO:4:P4_SPECIMENSEQNBR:";

    private final static Logger log = LoggerFactory.getLogger(TransController.class);

    public DataTransformerService(PdmrPdxInfoRepository pdmrPdxInfoRepository) {
        this.pdmrPdxInfoRepository = pdmrPdxInfoRepository;
    }

    //Transformation rule as specified here: https://docs.google.com/spreadsheets/d/1buUu5yj3Xq8tbEtL1l2UILV9kLnouGqF0vIjFlGGbEE
    public JsonNode transformDataAndSave(String specimenSearchUrl,
                                         String specimenUrl,
                                         String tissueOriginsUrl,
                                         String tumoGradeStateTypesUrl,
                                         String mouseStrainsUrl,
                                         String implantationSitesUrl,
                                         String tissueTypeUrl,
                                         String histologyUrl,
                                         String tumorGradeUrl,
                                         String samplesUrl) {

        String modelID = "";
        String patientID = "";
        String gender = "";
        String age = "";
        String race = "";
        String ethnicity = "";
        String specimenSite = "";
        String primarySite = "";
        String initialDiagnosis = "";
        String clinicalDiagnosis = "";
        String tumorType = "";
        String stageClassification = "";
        String stageValue = "";
        String gradeClassification = "";
        String gradeValue = "";
        String sampleType = "";
        String strain = "";
        String mouseSex = "";
        String treatmentNaive = "";
        String engraftmentSite = "";
        String engraftmentType = "";
        String sourceUrl = "";
        String extractionMethod = "";
        String dateAtCollection = "";
        String accessibility = "";


        String report = "";


        //If seqnumber is ) input in finder "Heterotopic" else if (1,2,3,4,5,6) put "Orthotopic" else(99) pit not specified

        // Read the whole JSOn as a JsonNode type & Retrieve each specimen search record as a Map (key value type) type
        JsonNode rootArray = connectToJSON(specimenSearchUrl);

        JsonNode pdmrSpecimenData = connectToJSON(specimenUrl);

        JsonNode tissueOrigins = connectToJSON(tissueOriginsUrl);

        JsonNode tumorGradeStageTypes = connectToJSON(tumoGradeStateTypesUrl);

        JsonNode mouseStrains = connectToJSON(mouseStrainsUrl);

        JsonNode impantationSites = connectToJSON(implantationSitesUrl);

        JsonNode tissueTypes = connectToJSON(tissueTypeUrl);

        JsonNode samples = connectToJSON(samplesUrl);

        JsonNode histologies = connectToJSON(histologyUrl);

        JsonNode tumorGrades = connectToJSON(tumorGradeUrl);

        //engraftmentType

        for (JsonNode node : rootArray) {

            Map<String, Object> specimenSearch = mapper.convertValue(node, Map.class);

            modelID = specimenSearch.get("PATIENTID") + "-" + specimenSearch.get("SPECIMENID");
            patientID = specimenSearch.get("PATIENTID") + "";
            gender = specimenSearch.get("GENDER").toString().equals("M") ? "Male" : "Female";

            race = specimenSearch.get("RACEDESCRIPTION") + "";
            race = race.equals("Not Provided") ? "Not Specified" : race;

            ethnicity = specimenSearch.get("ETHNICITYDESCRIPTION") + "";
            ethnicity = ethnicity.equals("Not Provided") ? "Not Specified" : ethnicity;

            primarySite = specimenSearch.get("DISEASELOCATIONDESCRIPTION") + "";
            initialDiagnosis = "";
            clinicalDiagnosis = specimenSearch.get("MEDDRADESCRIPTION") + "";
            tumorType = "";
            stageClassification = "";
            stageValue = "";
            gradeClassification = "";
            gradeValue = "";
            sampleType = specimenSearch.get("TISSUETYPEDESCRIPTION") + "";
            strain = "";
            mouseSex = "";
            engraftmentSite = "";
            sourceUrl = DATASOURCE_URL_PREFIX + specimenSearch.get("SPECIMENSEQNBR");
            extractionMethod = "";
            dateAtCollection = "";
            accessibility = "";

            treatmentNaive = "Not Specified";
            age = "";
            specimenSite = "";


            // Treatment naive
            try {
                if (specimenSearch.get("CURRENTREGIMEN").toString().equalsIgnoreCase("Treatment naive")) {
                    treatmentNaive = "Treatment Naive";
                }
            } catch (Exception e) {
            }


            // From specimensearch table - pick SPECIMENSEQNBR column
            // Look SAMPLE table for key SPECIMENSEQNBR and retrieve the SAMPLESEQNBR column
            // Look HISTOLOGY table for key SAMPLESEQNBR and retrieve TUMORGRADESEQNBR
            // Look TumorGrade  table for key TUMORGRADESEQNBR and set Grade as retrieved TUMORGRADESHORTNAME
            for (JsonNode sample : samples) {
                Map<String, Object> dSample = mapper.convertValue(sample, Map.class);

                if (specimenSearch.get("SPECIMENSEQNBR").equals(dSample.get("SPECIMENSEQNBR"))) {


                    for (JsonNode histology : histologies) {
                        Map<String, Object> dHistology = mapper.convertValue(histology, Map.class);

                        if (dSample.get("SAMPLESEQNBR").equals(dHistology.get("SAMPLESEQNBR"))) {


                            for (JsonNode tumorGrade : tumorGrades) {
                                Map<String, Object> dTumorGrade = mapper.convertValue(tumorGrade, Map.class);

                                if (dHistology.get("TUMORGRADESEQNBR").equals(dTumorGrade.get("TUMORGRADESEQNBR"))) {

                                    gradeValue = dTumorGrade.get("TUMORGRADESHORTNAME")+"";
                                    gradeValue = gradeValue.equals("---") ? "Not Specified" : gradeValue;
                                }
                            }
                        }
                    }

                }
            }



            for (JsonNode tumorGradeStageType : tumorGradeStageTypes) {

                Map<String, Object> dTumorGradeStageType = mapper.convertValue(tumorGradeStageType, Map.class);

                if (specimenSearch.get("TUMORGRADESTAGESEQNBR").equals(dTumorGradeStageType.get("TUMORGRADESTAGESEQNBR"))) {

                    String x = specimenSearch.get("TUMORGRADESTAGESEQNBR") + "";

                    if (x.equals("1") || x.equals("8") || x.equals("9") || x.equals("10") || x.equals("11")) {
                        //1,8,9,10,11
                        stageClassification = dTumorGradeStageType.get("TUMORGRADESTAGESHORTNAME") + "";
                        gradeClassification = "Not Specified";
                    } else {
                        //2,3,4,5,6,7,12,13
                        gradeClassification = dTumorGradeStageType.get("TUMORGRADESTAGESHORTNAME") + "";
                        gradeClassification = gradeClassification.equals("NA") ? "Not Specified" : gradeClassification;
                        stageClassification = "Not Specified";
                    }
                }
            }



            for (JsonNode tissueType : tissueTypes)
            {
                Map<String, Object> dTissueType = mapper.convertValue(tissueType, Map.class);

                if (specimenSearch.get("TISSUETYPESHORTNAME").equals(dTissueType.get("TISSUETYPESHORTNAME"))) {
                    extractionMethod = dTissueType.get("TISSUETYPEDESCRIPTION") + "";
                }
            }


            for (JsonNode pdmrSpecimen : pdmrSpecimenData) {

                Map<String, Object> specimen = mapper.convertValue(pdmrSpecimen, Map.class);

                if (specimenSearch.get("SPECIMENID").equals(specimen.get("SPECIMENID"))) {

                    age = specimen.get("AGEATSAMPLING") + "";
                    specimenSite = specimen.get("BIOPSYSITE") + "";

                    dateAtCollection = specimen.get("COLLECTIONDATE") + "";
                    try {
                        dateAtCollection = dateAtCollection.substring(0, 10);
                    } catch (Exception e) {
                    }

                    accessibility = specimen.get("PUBLICACCESSYN") + "";
                    if (accessibility.equals("Y")) {
                        accessibility = "Public";
                    }


                    for (JsonNode mouseStrain : mouseStrains) {
                        Map<String, Object> dMouseStrains = mapper.convertValue(mouseStrain, Map.class);

                        if (specimen.get("MOUSESTRAINSEQNBR").equals(dMouseStrains.get("MOUSESTRAINSEQNBR"))) {
                            strain = dMouseStrains.get("MOUSESTRAINDESCRIPT") + "";
                        }
                    }


                    for (JsonNode impantationSite : impantationSites) {
                        Map<String, Object> dImpantationSites = mapper.convertValue(impantationSite, Map.class);

                        if (specimen.get("IMPLANTATIONSITESEQNBR").equals(dImpantationSites.get("IMPLANTATIONSITESEQNBR"))) {
                            engraftmentSite = dImpantationSites.get("IMPLANTATIONSITEDESCRIPTION") + "";
                        }
                            /* with the number to to table IMPLANTATIONSITES, key is IMPLANTATIONSITESEQNBR, value in finde should be IMPLANTATIONSITEDESCRIPTION */
                    }

                    if (specimen.get("IMPLANTATIONSITESEQNBR").equals("0")) {
                        engraftmentType = "Heterotopic";
                    }else if(specimen.get("IMPLANTATIONSITESEQNBR").equals("99")){
                        engraftmentSite = "Not Specified";
                        engraftmentType = "Not Specified";
                    }else{
                        engraftmentType = "Orthotopic";
                    }


                    // Retrieve details of specimen.get("PROVIDEDTISSUEORIGINSEQNBR").
                    for (JsonNode tissueOrigin : tissueOrigins) {
                        Map<String, Object> tissue = mapper.convertValue(tissueOrigin, Map.class);
                        if (specimen.get("PROVIDEDTISSUEORIGINSEQNBR").equals(tissue.get("PROVIDEDTISSUEORIGINSEQNBR"))) {
                            tumorType = tissue.get("PROVIDEDTISSUEORIGINDESCRIPT") + "";
                            tumorType = tumorType.equals("Metastatic Site") ? "Metastatic" : tumorType;
                        }
                    }

                }


            }


            try {
                pdmrPdxInfoRepository.save(
                        new PdmrPdxInfo(modelID, patientID, gender, age, race, ethnicity, specimenSite, primarySite, initialDiagnosis,
                                clinicalDiagnosis, tumorType, stageClassification, stageValue, gradeClassification, gradeValue,
                                sampleType, strain, mouseSex, treatmentNaive, engraftmentSite, engraftmentType, sourceUrl, extractionMethod, dateAtCollection, accessibility)
                );
                log.info("Loaded Record for Patient" + specimenSearch.get("PATIENTID"));
            } catch (Exception e) {
                log.info("Record for Patient" + specimenSearch.get("PATIENTID") + "Not Loaded");
            }

            report += "Loaded Record for Patient " + specimenSearch.get("PATIENTID") + "<br>";

        }

        return rootArray;


    }


    public JsonNode connectToJSON(String apiLink) {

        JsonNode rootArray = null;
        ObjectMapper mapper = new ObjectMapper();

        try {

            URL url = new URL(apiLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            rootArray = mapper.readTree(br);
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootArray;

    }


    public List<PdmrPdxInfo> getAllPdmr() {
        List<PdmrPdxInfo> pdmrPdxInfos = pdmrPdxInfoRepository.findAll();

        return pdmrPdxInfos;
    }

}
