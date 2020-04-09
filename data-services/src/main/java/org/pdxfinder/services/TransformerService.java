package org.pdxfinder.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.rdbms.dao.PdmrPdxInfo;
import org.pdxfinder.rdbms.dao.Sample;
import org.pdxfinder.rdbms.dao.Treatment;
import org.pdxfinder.rdbms.dao.Validation;
import org.pdxfinder.rdbms.repositories.TransPdxInfoRepository;
import org.pdxfinder.rdbms.repositories.TransSampleRepository;
import org.pdxfinder.rdbms.repositories.TransTreatmentRepository;
import org.pdxfinder.rdbms.repositories.TransValidationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Mosaku Abayomi on 12/12/2017.
 */

@Service
public class TransformerService {


    private final static Logger log = LoggerFactory.getLogger(TransformerService.class);

    private ObjectMapper mapper = new ObjectMapper();
    private TransPdxInfoRepository transPdxInfoRepository;
    private TransTreatmentRepository transTreatmentRepository;
    private TransValidationRepository transValidationRepository;
    private TransSampleRepository transSampleRepository;
    private String homeDir = System.getProperty("user.home");

    private UtilityService util;

    private String dataRootDir;


    public TransformerService(TransPdxInfoRepository transPdxInfoRepository,
                              TransTreatmentRepository transTreatmentRepository,
                              TransValidationRepository transValidationRepository,
                              TransSampleRepository transSampleRepository,
                              UtilityService util) {
        this.transPdxInfoRepository = transPdxInfoRepository;
        this.transTreatmentRepository = transTreatmentRepository;
        this.transValidationRepository = transValidationRepository;
        this.transSampleRepository = transSampleRepository;
        this.util = util;
    }

    public void setDataRootDir(String dataRootDir) {
        this.dataRootDir = dataRootDir;
    }

    //Transformation rule as specified here: https://docs.google.com/spreadsheets/d/1buUu5yj3Xq8tbEtL1l2UILV9kLnouGqF0vIjFlGGbEE
    public List<Map<String, String>> transformDataAndSave() {
        
        String unKnown = "Not Specified";
        String modelID;
        String patientID;
        String gender;
        String age;
        String race;
        String ethnicity;
        String specimenSite;
        String primarySite;
        String initialDiagnosis;
        StringBuilder clinicalDiagnosis;
        String tumorType;
        String stageClassification;
        String stageValue ;
        String gradeClassification;
        String gradeValue;
        String sampleType;
        String strain;
        String mouseSex;
        String treatmentNaive;
        String engraftmentSite;
        String engraftmentType = "";
        String sourceUrl;
        String extractionMethod;
        String dateAtCollection;
        String accessibility;

        String drug = "";
        String startingDate;
        String priorDate;
        String response = "";
        String duration = unKnown;


        String sampleId;
        String sampleTumorType;
        String samplePassage;
        String wholeExomeSeqYn;
        String rnaSeqYn;


        StringBuilder report = new StringBuilder();

        String specimenSearchUrl = "/PDMR/raw/PDMR_SPECIMENSEARCH.json";
        log.info(specimenSearchUrl);

        //If seqnumber is ) input in finder "Heterotopic" else if (1,2,3,4,5,6) put "Orthotopic" else(99) put not specified

        // Read the whole JSON as a JsonNode type & Retrieve each specimen search record as a Map (key value type) type
        JsonNode rootArray = util.readJsonLocal(this.dataRootDir+ specimenSearchUrl);

        String specimenUrl = "/PDMR/raw/PDMR_SPECIMEN.json";
        JsonNode pdmrSpecimenData = util.readJsonLocal(this.dataRootDir+ specimenUrl);

        String tissueOriginsUrl = "/PDMR/raw/PDMR_PROVIDEDTISSUEORIGINS.json";
        JsonNode tissueOrigins = util.readJsonLocal(this.dataRootDir+ tissueOriginsUrl);

        String tumoGradeStateTypesUrl = "/PDMR/raw/PDMR_TUMORGRADESTAGETYPES.json";
        JsonNode tumorGradeStageTypes = util.readJsonLocal(this.dataRootDir+ tumoGradeStateTypesUrl);

        String mouseStrainsUrl = "/PDMR/raw/PDMR_MOUSESTRAINS.json";
        JsonNode mouseStrains = util.readJsonLocal(this.dataRootDir+ mouseStrainsUrl);

        String implantationSitesUrl = "/PDMR/raw/PDMR_IMPLANTATIONSITES.json";
        JsonNode impantationSites = util.readJsonLocal(this.dataRootDir+ implantationSitesUrl);

        String tissueTypeUrl = "/PDMR/raw/PDMR_TISSUETYPES.json";
        JsonNode tissueTypes = util.readJsonLocal(this.dataRootDir+ tissueTypeUrl);

        String samplesUrl = "/PDMR/raw/PDMR_SAMPLE.json";
        JsonNode samples = util.readJsonLocal(this.dataRootDir+ samplesUrl);

        String histologyUrl = "/PDMR/raw/PDMR_HISTOLOGY.json";
        JsonNode histologies = util.readJsonLocal(this.dataRootDir+ histologyUrl);

        String tumorGradeUrl = "/PDMR/raw/PDMR_TUMORGRADES.json";
        JsonNode tumorGrades = util.readJsonLocal(this.dataRootDir+ tumorGradeUrl);

        String currentTherapyUrl = "/PDMR/raw/PDMR_CURRENTTHERAPY.json";
        JsonNode currentTherapies = util.readJsonLocal(this.dataRootDir+ currentTherapyUrl);

        String standardRegimensUrl = "/PDMR/raw/PDMR_STANDARDIZEDREGIMENS.json";
        JsonNode standardRegimens = util.readJsonLocal(this.dataRootDir+ standardRegimensUrl);

        String clinicalResponseUrl = "/PDMR/raw/PDMR_CLINICALRESPONSES.json";
        JsonNode clinicalResponses = util.readJsonLocal(this.dataRootDir+ clinicalResponseUrl);

        String priorTherapyUrl = "/PDMR/raw/PDMR_PRIORTHERAPIES.json";
        JsonNode priorTherapies = util.readJsonLocal(this.dataRootDir+ priorTherapyUrl);

        String patientInfoUrl = "/PDMR/raw/PDMR_PATIENTINFO.json";
        JsonNode patientInfo = util.readJsonLocal(this.dataRootDir+ patientInfoUrl);
        List patientList = mapper.convertValue(patientInfo, List.class);


        //engraftmentType
        int count = 0;
        Set<PdmrPdxInfo> pdmrPdxInfoList = new HashSet<>();

        List<String> modelIDList = new ArrayList<>();
        List<Map<String, String>> mappingList = new ArrayList<Map<String, String>>();

        for (JsonNode node : rootArray) {

            count++;

            Map specimenSearch = mapper.convertValue(node, Map.class);

            Object pdmTypeDesc = specimenSearch.get("PDMTYPEDESCRIPTION");
            Object tissueTypeDesc = specimenSearch.get("TISSUETYPEDESCRIPTION");

            if ( (pdmTypeDesc.equals("PDX") || pdmTypeDesc.equals("Patient/Originator Specimen")) && (tissueTypeDesc.equals("Resection") || tissueTypeDesc.equals("Tumor Biopsy")) ){

                modelID = specimenSearch.get("PATIENTID") + "-" + specimenSearch.get("SPECIMENID");
            }else {
                continue;
            }

            // CHECK IF THIS MODEL HAS BEEN TREATED :
            String checkIfExist = modelID;
            boolean done = modelIDList.stream().anyMatch(str -> str.equals(checkIfExist));

            if (done) {
                continue;
            }else{
                modelIDList.add(modelID);
            }

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
            String DATASOURCE_URL_PREFIX = "https://pdmdb.cancer.gov/pls/apex/f?p=101:4:0::NO:4:P4_SPECIMENSEQNBR:";
            sourceUrl = DATASOURCE_URL_PREFIX + specimenSearch.get("SPECIMENSEQNBR");
            extractionMethod = "";
            dateAtCollection = "";
            accessibility = "";

            treatmentNaive = null;
            age = "";
            specimenSite = "";


            clinicalDiagnosis = new StringBuilder(specimenSearch.get("MEDDRADESCRIPTION") + "");

            for (Object patientObject : patientList) {

                Map patient = mapper.convertValue(patientObject, Map.class);

                if (specimenSearch.get("PATIENTSEQNBR").equals(patient.get("PATIENTSEQNBR"))) {

                    // Retrieve Diagnosis Subtype data
                    if (patient.get("DIAGNOSISSUBTYPE") != null){
                        clinicalDiagnosis.append(" | ").append(patient.get("DIAGNOSISSUBTYPE"));
                    }

                    // Retrieve Additional Medical History data
                    if (patient.get("ADDITIONALMEDICALHISTORY") != null){
                        clinicalDiagnosis.append(" | ").append(patient.get("ADDITIONALMEDICALHISTORY"));
                    }

                    // Retrieve Notes data
                    if (patient.get("NOTES") != null){
                        clinicalDiagnosis.append(" | ").append(patient.get("NOTES"));
                    }
                }

               // index++; \r\n\r\n \"
            }
            //clinicalDiagnosis = clinicalDiagnosis.replaceAll("\\r|\\\"|\\n", "");

            clinicalDiagnosis = new StringBuilder(clinicalDiagnosis.toString().replaceAll("[^a-zA-Z,0-9 +_-]", "").trim());
            clinicalDiagnosis = new StringBuilder(clinicalDiagnosis.toString().replaceAll("\\s\\s", " "));

            log.info(clinicalDiagnosis.toString());

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
                Map dSample = mapper.convertValue(sample, Map.class);

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
                        Map dHistology = mapper.convertValue(histology, Map.class);

                        if (dSample.get("SAMPLESEQNBR").equals(dHistology.get("SAMPLESEQNBR"))) {


                            for (JsonNode tumorGrade : tumorGrades) {
                                Map dTumorGrade = mapper.convertValue(tumorGrade, Map.class);

                                if (dHistology.get("TUMORGRADESEQNBR").equals(dTumorGrade.get("TUMORGRADESEQNBR"))) {

                                    gradeValue = dTumorGrade.get("TUMORGRADESHORTNAME")+"";
                                    gradeValue = gradeValue.equals("---") ? unKnown : gradeValue;
                                }
                            }
                        }
                    }

                }
            }



                    /*
                        model ID - mpdelID
                        diagnosis - clinicalDiagnosis
                        primary tissue - primarySite
                        tumour type - tumorType
                     */

            // From specimensearch table - pick PATIENTSEQNBR column
            //Look CURRENTTHERAPY table for key PATIENTSEQNBR and retrieve the STANDARDIZEDREGIMENSEQNBR column
            // Look STANDARDIZEDREGIMENS table for key STANDARDIZEDREGIMENSEQNBR and retrieve DISPLAYEDREGIMEN

            //From CURRENTTHERAPY table also retrieve the BESTRESPONSESEQNBR column
            // Look CLINICALRESPONSES table for key CLINICALRESPONSESEQNBR (->BESTRESPONSESEQNBR) and retrieve CLINICALRESPONSEDESCRIPTION

            List<Treatment> treatments = new ArrayList<>();

            for (JsonNode currentTherapy : currentTherapies) {
                Map dCurrentTherapy = mapper.convertValue(currentTherapy, Map.class);


                if (specimenSearch.get("PATIENTSEQNBR").equals(dCurrentTherapy.get("PATIENTSEQNBR"))) {


                    startingDate = dCurrentTherapy.get("DATEREGIMENSTARTED")+"";
                    try {
                        startingDate = startingDate.equals("null") ? unKnown : startingDate.substring(0, 10);
                    } catch (Exception e) {}



                    for (JsonNode standardregimen : standardRegimens) {
                        Map dStandardregimen = mapper.convertValue(standardregimen, Map.class);

                        if (dCurrentTherapy.get("STANDARDIZEDREGIMENSEQNBR").equals(dStandardregimen.get("REGIMENSEQNBR"))) {
                            drug = dStandardregimen.get("DISPLAYEDREGIMEN").toString().replace(","," +");
                        }
                    }


                    for (JsonNode clinicalResponse : clinicalResponses) {
                        Map dClinicalResponse = mapper.convertValue(clinicalResponse, Map.class);

                        if (dCurrentTherapy.get("BESTRESPONSESEQNBR").equals(dClinicalResponse.get("CLINICALRESPONSESEQNBR"))) {
                            response = dClinicalResponse.get("CLINICALRESPONSEDESCRIPTION")+"";
                            response = response.equals("<Unknown>") ? unKnown : response;
                        }
                    }

                    treatments.add(new Treatment(cleanDrugs(drug),null,null,null,duration,null,
                            null,response,null,startingDate,null));

                    drug=""; duration = unKnown; response=""; startingDate = "";
                }

            }




            for (JsonNode priorTherapy : priorTherapies) {
                Map dPriorTherapy = mapper.convertValue(priorTherapy, Map.class);

                if (specimenSearch.get("PATIENTSEQNBR").equals(dPriorTherapy.get("PATIENTSEQNBR"))) {

                    priorDate = dPriorTherapy.get("DATEREGIMENSTARTED")+"";
                    try {
                        priorDate = priorDate.equals("null") ? unKnown : priorDate.substring(0, 10);
                    } catch (Exception e) {}

                    duration = dPriorTherapy.get("DURATIONMONTHS")+" Months";

                    for (JsonNode standardregimen : standardRegimens) {
                        Map dStandardregimen = mapper.convertValue(standardregimen, Map.class);

                        if (dPriorTherapy.get("STANDARDIZEDREGIMENSEQNBR").equals(dStandardregimen.get("REGIMENSEQNBR"))) {
                            drug = dStandardregimen.get("DISPLAYEDREGIMEN").toString().replace(","," +");
                        }
                    }


                    for (JsonNode clinicalResponse : clinicalResponses) {
                        Map dClinicalResponse = mapper.convertValue(clinicalResponse, Map.class);

                        if (dClinicalResponse.get("CLINICALRESPONSESEQNBR").equals(dPriorTherapy.get("BESTRESPONSESEQNBR"))) {
                            response = dClinicalResponse.get("CLINICALRESPONSEDESCRIPTION")+"";
                            response = response.equals("<Unknown>") ? unKnown : response;
                        }
                    }




                    treatments.add(new Treatment(null,cleanDrugs(drug),null,null,duration,null,
                            null,response,null,null,priorDate));

                    drug=""; duration = unKnown; response=""; priorDate = "";
                }
            }



            for (JsonNode tumorGradeStageType : tumorGradeStageTypes) {

                Map dTumorGradeStageType = mapper.convertValue(tumorGradeStageType, Map.class);

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
                Map dTissueType = mapper.convertValue(tissueType, Map.class);

                if (specimenSearch.get("TISSUETYPESHORTNAME").equals(dTissueType.get("TISSUETYPESHORTNAME"))) {
                    extractionMethod = dTissueType.get("TISSUETYPEDESCRIPTION") + "";
                }
            }



            for (JsonNode pdmrSpecimen : pdmrSpecimenData) {

                Map specimen = mapper.convertValue(pdmrSpecimen, Map.class);

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
                        Map dMouseStrains = mapper.convertValue(mouseStrain, Map.class);

                        if (specimen.get("MOUSESTRAINSEQNBR").equals(dMouseStrains.get("MOUSESTRAINSEQNBR"))) {
                            strain = dMouseStrains.get("MOUSESTRAINDESCRIPT") + "";
                        }
                    }


                    for (JsonNode impantationSite : impantationSites) {
                        Map dImpantationSites = mapper.convertValue(impantationSite, Map.class);

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
                        Map tissue = mapper.convertValue(tissueOrigin, Map.class);
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
                                                          clinicalDiagnosis.toString(), tumorType, stageClassification, stageValue, gradeClassification, gradeValue, sampleType, strain, mouseSex,
                                                          treatmentNaive, engraftmentSite, engraftmentType, sourceUrl, extractionMethod, dateAtCollection, accessibility, treatments, validations, sampleList);

                // GENERATE DATA FOR MAPPING
                Map<String, String> mappingData = new LinkedHashMap<String, String>();
                mappingData.put("MODEL ID",modelID);
                mappingData.put("DIAGNOSIS", clinicalDiagnosis.toString());
                mappingData.put("PRIMARY TISSUE", primarySite);
                mappingData.put("TUMOR TYPE", tumorType);
                mappingList.add(mappingData);

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

            report.append("Loaded Record for Patient ").append(specimenSearch.get("PATIENTID")).append("<br>");

            // if (count == 40){ break; }
        }

        return mappingList;


    }


    private String cleanDrugs(String drug){

        String drugString = drug.contains("FOLFOX") ? drug.replace("FOLFOX","Fluorouracil + Leucovorin Calcium + Oxaliplatin") : drug;

        // Always replace FOLFIRINOX before FOLFIRI
        drugString = drugString.contains("FOLFIRINOX") ? drugString.replace("FOLFIRINOX","Fluorouracil + irinotecan + Leucovorin calcium + Oxaliplatin") : drugString;

        drugString = drugString.contains("FOLFIRI") ? drugString.replace("FOLFIRI","Folinic acid + Fluorouracil + Irinotecan") : drugString;

        drugString = drugString.contains("MVAC") ? drugString.replace("MVAC","Cisplatin + Doxorubicin + Methotrexate + Vinblastine") : drugString;

        drugString = drugString.contains("XELOX") ? drugString.replace("XELOX","Capecitabine + Oxaliplatin") : drugString;

        return drugString;
    }



    public List<PdmrPdxInfo> getAllPdmr() {

        return transPdxInfoRepository.findAll();
    }


    public String getPassageByModelIDAndSampleID(String modelID, String sampleID) {

        PdmrPdxInfo pdmrPdxInfos = transPdxInfoRepository.findByModelID(modelID);

        String passage = "XXXX";

        try{

            List<Sample> samples = pdmrPdxInfos.getSamples();
            for (Sample sample : samples){

                if (sampleID.equals(sample.getSampleID())){
                    passage = sample.getPassage();
                }
            }
        }catch (Exception e){

            log.info("{} has no sample in the database", modelID);
        }

        return passage;
    }








    public String getDrugs(PdmrPdxInfo pdmrPdxInfo){

        List<Treatment> treatments = pdmrPdxInfo.getTreatments();

        StringBuilder drugLista = new StringBuilder();

        for (Treatment treatment : treatments){

            try {
                if (!(treatment.getCurrentDrug() == null)) {
                    drugLista.append(util.splitText(treatment.getCurrentDrug(), "\\+", "\n"));
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            try {
                if (!(treatment.getPriorDrug() == null)) {
                    drugLista.append(util.splitText(treatment.getPriorDrug(), "\\+", "\n"));
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        String drugList = homeDir+"/Documents/"+(new Date())+"_pdmrDrug.csv";

        util.writeToFile(drugLista.toString(), drugList, false);

        return drugLista.toString().replace("\n", "<br>");
    }


    public String transformJAXCNV(String cnaStringData) {

        Map<String, List<Map>> dataMap;
        dataMap = mapper.convertValue(util.jsonStringToNode(cnaStringData), Map.class);
        String mapToString = "";

        List<Map> newList = new ArrayList<>();

        dataMap.get("data").forEach(data -> {

            data.put("model id", data.remove("model"));
            data.put("passage num", data.remove("passage"));
            data.put("gene symbol", data.remove("gene"));
            data.put("log2R_cna", data.remove("logratio_ploidy"));
            data.put("genome assembly", "GRCh38");

            newList.add(data);
        });

        dataMap.put("data", newList);

        try {
            mapToString = mapper.writeValueAsString(dataMap);
        } catch (Exception e) {
        }

        return mapToString;
    }


    public String transformJaxRNASeq(String transStringData) {

        Map<String, List<Map>> dataMap;
        dataMap = mapper.convertValue(util.jsonStringToNode(transStringData), Map.class);
        String mapToString = "";

        List<Map> newList = new ArrayList<>();

        dataMap.get("data").forEach(data -> {

            data.put("model id", data.remove("model"));
            data.put("passage num", data.remove("passage"));
            data.put("gene symbol", data.remove("gene"));
            data.put("z_score", data.remove("z_score_percentile_rank"));
            data.put("genome assembly", "GRCh38");

            newList.add(data);
        });

        dataMap.put("data", newList);

        try {
            mapToString = mapper.writeValueAsString(dataMap);
        } catch (Exception e) {
        }

        return mapToString;
    }


}
