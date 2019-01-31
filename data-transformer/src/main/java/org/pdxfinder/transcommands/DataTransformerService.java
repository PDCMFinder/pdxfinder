package org.pdxfinder.transcommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.controller.TransController;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.transdatamodel.PdmrPdxInfo;
import org.pdxfinder.transdatamodel.Sample;
import org.pdxfinder.transdatamodel.Treatment;
import org.pdxfinder.transdatamodel.Validation;
import org.pdxfinder.transrepository.TransPdxInfoRepository;
import org.pdxfinder.transrepository.TransSampleRepository;
import org.pdxfinder.transrepository.TransTreatmentRepository;
import org.pdxfinder.transrepository.TransValidationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Created by Mosaku Abayomi on 12/12/2017.
 */

@Service
public class DataTransformerService {


    private final static Logger log = LoggerFactory.getLogger(TransController.class);
    ObjectMapper mapper = new ObjectMapper();
    private TransPdxInfoRepository transPdxInfoRepository;
    private TransTreatmentRepository transTreatmentRepository;
    private TransValidationRepository transValidationRepository;
    private TransSampleRepository transSampleRepository;

    @Autowired
    private UtilityService util;

    private String DATASOURCE_URL_PREFIX = "https://pdmdb.cancer.gov/pls/apex/f?p=101:4:0::NO:4:P4_SPECIMENSEQNBR:";

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir = System.getProperty("user.home")+"/PDXFinder/data";


    private final static String DATASOURCE_ABBREVIATION = "/PDMR";

    private String RAW_DATA_URL = dataRootDir+DATASOURCE_ABBREVIATION+"/raw";

    private String specimenSearchUrl = RAW_DATA_URL+"/PDMR_SPECIMENSEARCH.json";

    private String specimenUrl = RAW_DATA_URL+"/PDMR_SPECIMEN.json";

    private String tissueOriginsUrl = RAW_DATA_URL+"/PDMR_PROVIDEDTISSUEORIGINS.json";

    private String tumoGradeStateTypesUrl = RAW_DATA_URL+"/PDMR_TUMORGRADESTAGETYPES.json";

    private String mouseStrainsUrl = RAW_DATA_URL+"/PDMR_MOUSESTRAINS.json";

    private String implantationSitesUrl = RAW_DATA_URL+"/PDMR_IMPLANTATIONSITES.json";

    private String tissueTypeUrl = RAW_DATA_URL+"/PDMR_TISSUETYPES.json";

    private String histologyUrl = RAW_DATA_URL+"/PDMR_HISTOLOGY.json";

    private String tumorGradeUrl = RAW_DATA_URL+"/PDMR_TUMORGRADES.json";

    private String samplesUrl = RAW_DATA_URL+"/PDMR_SAMPLE.json";

    private String currentTherapyUrl = RAW_DATA_URL+"/PDMR_CURRENTTHERAPY.json";

    private String standardRegimensUrl = RAW_DATA_URL+"/PDMR_STANDARDIZEDREGIMENS.json";

    private String clinicalResponseUrl = RAW_DATA_URL+"/PDMR_CLINICALRESPONSES.json";

    private String priorTherapyUrl = RAW_DATA_URL+"/PDMR_PRIORTHERAPIES.json";

    private String patientInfoUrl = RAW_DATA_URL+"/PDMR_PATIENTINFO.json";


    public DataTransformerService(TransPdxInfoRepository transPdxInfoRepository,
                                  TransTreatmentRepository transTreatmentRepository,
                                  TransValidationRepository transValidationRepository,
                                  TransSampleRepository transSampleRepository) {
        this.transPdxInfoRepository = transPdxInfoRepository;
        this.transTreatmentRepository = transTreatmentRepository;
        this.transValidationRepository = transValidationRepository;
        this.transSampleRepository = transSampleRepository;
    }

    //Transformation rule as specified here: https://docs.google.com/spreadsheets/d/1buUu5yj3Xq8tbEtL1l2UILV9kLnouGqF0vIjFlGGbEE
    public Set<PdmrPdxInfo> transformDataAndSave() {

        String unKnown = "Not Specified";
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

        String drug = "";
        String startingDate = "";
        String priorDate = "";
        String response = "";
        String duration = unKnown;


        String sampleId = "";
        String sampleTumorType = "";
        String samplePassage = "";
        String wholeExomeSeqYn = "";
        String rnaSeqYn = "";


        String report = "";

        //If seqnumber is ) input in finder "Heterotopic" else if (1,2,3,4,5,6) put "Orthotopic" else(99) put not specified

        // Read the whole JSON as a JsonNode type & Retrieve each specimen search record as a Map (key value type) type
        JsonNode rootArray = util.readJsonLocal(specimenSearchUrl);

        JsonNode pdmrSpecimenData = util.readJsonLocal(specimenUrl);

        JsonNode tissueOrigins = util.readJsonLocal(tissueOriginsUrl);

        JsonNode tumorGradeStageTypes = util.readJsonLocal(tumoGradeStateTypesUrl);

        JsonNode mouseStrains = util.readJsonLocal(mouseStrainsUrl);

        JsonNode impantationSites = util.readJsonLocal(implantationSitesUrl);

        JsonNode tissueTypes = util.readJsonLocal(tissueTypeUrl);

        JsonNode samples = util.readJsonLocal(samplesUrl);

        JsonNode histologies = util.readJsonLocal(histologyUrl);

        JsonNode tumorGrades = util.readJsonLocal(tumorGradeUrl);

        JsonNode currentTherapies = util.readJsonLocal(currentTherapyUrl);

        JsonNode standardRegimens = util.readJsonLocal(standardRegimensUrl);

        JsonNode clinicalResponses = util.readJsonLocal(clinicalResponseUrl);

        JsonNode priorTherapies = util.readJsonLocal(priorTherapyUrl);

        JsonNode patientInfo = util.readJsonLocal(patientInfoUrl);
        List<Map<String, Object>> patientList = mapper.convertValue(patientInfo, List.class);


        //engraftmentType
        int count = 0;
        Set<PdmrPdxInfo> pdmrPdxInfoList = new HashSet<>();

        for (JsonNode node : rootArray) {

            count++;

            Map<String, Object> specimenSearch = mapper.convertValue(node, Map.class);

            modelID = specimenSearch.get("PATIENTID") + "-" + specimenSearch.get("SPECIMENID");
            patientID = specimenSearch.get("PATIENTID") + "";
            gender = specimenSearch.get("GENDER").toString().equals("M") ? "Male" : "Female";

            race = specimenSearch.get("RACEDESCRIPTION") + "";
            race = race.equals("Not Provided") ? unKnown : race;

            ethnicity = specimenSearch.get("ETHNICITYDESCRIPTION") + "";
            ethnicity = ethnicity.equals("Not Provided") ? unKnown : ethnicity;

            primarySite = specimenSearch.get("DISEASELOCATIONDESCRIPTION") + "";
            initialDiagnosis = "";
            tumorType = "";
            stageClassification = "";
            stageValue = unKnown;
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

            treatmentNaive = null;
            age = "";
            specimenSite = "";


            clinicalDiagnosis = specimenSearch.get("MEDDRADESCRIPTION") + "";

            for (Map patient : patientList) {

                if (specimenSearch.get("PATIENTSEQNBR").equals(patient.get("PATIENTSEQNBR"))) {

                    // Retrieve Diagnosis Subtype data
                    if (patient.get("DIAGNOSISSUBTYPE") != null){
                        clinicalDiagnosis += " | "+patient.get("DIAGNOSISSUBTYPE");
                    }

                    // Retrieve Additional Medical History data
                    if (patient.get("ADDITIONALMEDICALHISTORY") != null){
                        clinicalDiagnosis += " | "+patient.get("ADDITIONALMEDICALHISTORY");
                    }

                    // Retrieve Notes data
                    if (patient.get("NOTES") != null){
                        clinicalDiagnosis += " | "+patient.get("NOTES");
                    }

                }

               // index++;
            }

            // Treatment naive
            /*try {
                if (specimenSearch.get("CURRENTREGIMEN").toString().equalsIgnoreCase("Treatment naive")) {
                    treatmentNaive = "Treatment Naive";
                }
            } catch (Exception e) {
            }*/


            // From specimensearch table - pick SPECIMENSEQNBR column
            // Look SAMPLE table for key SPECIMENSEQNBR and retrieve the SAMPLESEQNBR column
            // Look HISTOLOGY table for key SAMPLESEQNBR and retrieve TUMORGRADESEQNBR
            // Look TumorGrade  table for key TUMORGRADESEQNBR and set Grade as retrieved TUMORGRADESHORTNAME


            List<Sample> sampleList = new ArrayList<>();

            for (JsonNode sample : samples) {
                Map<String, Object> dSample = mapper.convertValue(sample, Map.class);

                if (specimenSearch.get("SPECIMENSEQNBR").equals(dSample.get("SPECIMENSEQNBR"))) {

                    // Retrieve sample data
                    sampleId = dSample.get("SAMPLEID")+"";

                    wholeExomeSeqYn = dSample.get("WHOLEEXOMESEQUENCEFTPYN")+"";
                    rnaSeqYn = dSample.get("RNASEQUENCEFTPYN")+"";

                    if (sampleId.equals("ORIGINATOR")){
                        sampleTumorType = "Patient Tumor";
                    }else {
                        sampleTumorType = "Xenograft Tumor";
                    }

                    if (sampleId.equals("ORIGINATOR")){
                        samplePassage = null;
                    }else {
                        samplePassage = dSample.get("PASSAGEOFTHISSAMPLE")+"";
                    }
                    sampleList.add(new Sample(sampleId,sampleTumorType,samplePassage,wholeExomeSeqYn,wholeExomeSeqYn,wholeExomeSeqYn,rnaSeqYn,rnaSeqYn));
                    sampleId = ""; sampleTumorType = ""; samplePassage = ""; wholeExomeSeqYn=""; rnaSeqYn="";


                    // Retrieve Grade Value
                    for (JsonNode histology : histologies) {
                        Map<String, Object> dHistology = mapper.convertValue(histology, Map.class);

                        if (dSample.get("SAMPLESEQNBR").equals(dHistology.get("SAMPLESEQNBR"))) {


                            for (JsonNode tumorGrade : tumorGrades) {
                                Map<String, Object> dTumorGrade = mapper.convertValue(tumorGrade, Map.class);

                                if (dHistology.get("TUMORGRADESEQNBR").equals(dTumorGrade.get("TUMORGRADESEQNBR"))) {

                                    gradeValue = dTumorGrade.get("TUMORGRADESHORTNAME")+"";
                                    gradeValue = gradeValue.equals("---") ? unKnown : gradeValue;
                                }
                            }
                        }
                    }

                }
            }





            // From specimensearch table - pick PATIENTSEQNBR column
            //Look CURRENTTHERAPY table for key PATIENTSEQNBR and retrieve the STANDARDIZEDREGIMENSEQNBR column
            // Look STANDARDIZEDREGIMENS table for key STANDARDIZEDREGIMENSEQNBR and retrieve DISPLAYEDREGIMEN

            //From CURRENTTHERAPY table also retrieve the BESTRESPONSESEQNBR column
            // Look CLINICALRESPONSES table for key CLINICALRESPONSESEQNBR (->BESTRESPONSESEQNBR) and retrieve CLINICALRESPONSEDESCRIPTION

            List<Treatment> treatments = new ArrayList<>();

            for (JsonNode currentTherapy : currentTherapies) {
                Map<String, Object> dCurrentTherapy = mapper.convertValue(currentTherapy, Map.class);


                if (specimenSearch.get("PATIENTSEQNBR").equals(dCurrentTherapy.get("PATIENTSEQNBR"))) {


                    startingDate = dCurrentTherapy.get("DATEREGIMENSTARTED")+"";
                    try {
                        startingDate = startingDate.equals("null") ? unKnown : startingDate.substring(0, 10);
                    } catch (Exception e) {}



                    for (JsonNode standardregimen : standardRegimens) {
                        Map<String, Object> dStandardregimen = mapper.convertValue(standardregimen, Map.class);

                        if (dCurrentTherapy.get("STANDARDIZEDREGIMENSEQNBR").equals(dStandardregimen.get("REGIMENSEQNBR"))) {
                            drug = dStandardregimen.get("DISPLAYEDREGIMEN").toString().replace(","," +");
                        }
                    }


                    for (JsonNode clinicalResponse : clinicalResponses) {
                        Map<String, Object> dClinicalResponse = mapper.convertValue(clinicalResponse, Map.class);

                        if (dCurrentTherapy.get("BESTRESPONSESEQNBR").equals(dClinicalResponse.get("CLINICALRESPONSESEQNBR"))) {
                            response = dClinicalResponse.get("CLINICALRESPONSEDESCRIPTION")+"";
                            response = response.equals("<Unknown>") ? unKnown : response;
                        }
                    }
                    treatments.add(new Treatment(drug,null,null,null,duration,null,
                            null,response,null,startingDate,null));

                    drug=""; duration = unKnown; response=""; startingDate = "";
                }

            }




            for (JsonNode priorTherapy : priorTherapies) {
                Map<String, Object> dPriorTherapy = mapper.convertValue(priorTherapy, Map.class);

                if (specimenSearch.get("PATIENTSEQNBR").equals(dPriorTherapy.get("PATIENTSEQNBR"))) {

                    priorDate = dPriorTherapy.get("DATEREGIMENSTARTED")+"";
                    try {
                        priorDate = priorDate.equals("null") ? unKnown : priorDate.substring(0, 10);
                    } catch (Exception e) {}

                    duration = dPriorTherapy.get("DURATIONMONTHS")+" Months";

                    for (JsonNode standardregimen : standardRegimens) {
                        Map<String, Object> dStandardregimen = mapper.convertValue(standardregimen, Map.class);

                        if (dPriorTherapy.get("STANDARDIZEDREGIMENSEQNBR").equals(dStandardregimen.get("REGIMENSEQNBR"))) {
                            drug = dStandardregimen.get("DISPLAYEDREGIMEN").toString().replace(","," +");
                        }
                    }


                    for (JsonNode clinicalResponse : clinicalResponses) {
                        Map<String, Object> dClinicalResponse = mapper.convertValue(clinicalResponse, Map.class);

                        if (dClinicalResponse.get("CLINICALRESPONSESEQNBR").equals(dPriorTherapy.get("BESTRESPONSESEQNBR"))) {
                            response = dClinicalResponse.get("CLINICALRESPONSEDESCRIPTION")+"";
                            response = response.equals("<Unknown>") ? unKnown : response;
                        }
                    }
                    treatments.add(new Treatment(null,drug,null,null,duration,null,
                            null,response,null,null,priorDate));

                    drug=""; duration = unKnown; response=""; priorDate = "";
                }
            }



            for (JsonNode tumorGradeStageType : tumorGradeStageTypes) {

                Map<String, Object> dTumorGradeStageType = mapper.convertValue(tumorGradeStageType, Map.class);

                if (specimenSearch.get("TUMORGRADESTAGESEQNBR").equals(dTumorGradeStageType.get("TUMORGRADESTAGESEQNBR"))) {

                    String x = specimenSearch.get("TUMORGRADESTAGESEQNBR") + "";

                    if (x.equals("1") || x.equals("8") || x.equals("9") || x.equals("10") || x.equals("11")) {
                        //1,8,9,10,11
                        stageClassification = dTumorGradeStageType.get("TUMORGRADESTAGESHORTNAME") + "";
                        gradeClassification = unKnown;
                    } else {
                        //2,3,4,5,6,7,12,13
                        gradeClassification = dTumorGradeStageType.get("TUMORGRADESTAGESHORTNAME") + "";
                        gradeClassification = gradeClassification.equals("NA") ? "Not Specified" : gradeClassification;
                        stageClassification = unKnown;
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

                    if (specimen.get("IMPLANTATIONSITESEQNBR").toString().equals("0")) {
                        engraftmentType = "Heterotopic";
                    }
                    else if(specimen.get("IMPLANTATIONSITESEQNBR").toString().equals("99")){
                        engraftmentSite = unKnown;
                        engraftmentType = unKnown;
                    }
                    else{
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






            // Hardcode the validation techniques.
            List<Validation> validations = new ArrayList<>();
            validations.add(new Validation("Fingerprinting","Model validated against  patient tumour or P0 xenograft","All"));
            validations.add(new Validation("Human mouse/DNA","Model validated against  patient tumour or P0 xenograft","All"));
            validations.add(new Validation("Histology","Model validated against histological features of same diagnosis","All"));

            try {

                PdmrPdxInfo pdmrPdxInfo = new PdmrPdxInfo(modelID, patientID, gender, age, race, ethnicity, specimenSite, primarySite, initialDiagnosis,
                        clinicalDiagnosis, tumorType, stageClassification, stageValue, gradeClassification, gradeValue, sampleType, strain, mouseSex,
                        treatmentNaive, engraftmentSite, engraftmentType, sourceUrl, extractionMethod, dateAtCollection, accessibility,treatments,validations, sampleList);

                //pdmrPdxInfoList.add(pdmrPdxInfo);
                transPdxInfoRepository.save(pdmrPdxInfo);

                // Update the Foreign Key pdxinfo_id for the corresponding treatments
                for (Treatment treatment: treatments){
                    treatment.setPdmrPdxInfo(pdmrPdxInfo);
                }
                transTreatmentRepository.save(treatments);

                // Update the Foreign Key pdxinfo_id for the corresponding validations
                for (Validation validation : validations){
                    validation.setPdmrPdxInfo(pdmrPdxInfo);
                }
                transValidationRepository.save(validations);

                // Update the Foreign key pdxinfo_id for the corresponding samples
                for (Sample sample : sampleList){
                    sample.setPdmrPdxInfo(pdmrPdxInfo);
                }
                transSampleRepository.save(sampleList);


                log.info("Loaded Record for Patient" + specimenSearch.get("PATIENTID"));
            } catch (Exception e) {
                log.info("Record for Patient" + specimenSearch.get("PATIENTID") + "Not Loaded");
            }

            report += "Loaded Record for Patient " + specimenSearch.get("PATIENTID") + "<br>";

            // if (count == 40){ break; }
        }

        return pdmrPdxInfoList;


    }



    public List<PdmrPdxInfo> getAllPdmr() {
        List<PdmrPdxInfo> pdmrPdxInfos = transPdxInfoRepository.findAll();

        return pdmrPdxInfos;
    }

}
