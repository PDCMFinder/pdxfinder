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
import org.pdxfinder.services.constants.OmicCSVColumn;
import org.pdxfinder.services.constants.PdmrOmicCol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Service
public class TransformerService {


    private static final Logger log = LoggerFactory.getLogger(TransformerService.class);

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



        List<String> modelIDList = new ArrayList<>();
        List<Map<String, String>> mappingList = new ArrayList<>();


        for (JsonNode node : rootArray) {

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

            }

            clinicalDiagnosis = new StringBuilder(clinicalDiagnosis.toString().replaceAll("[^a-zA-Z,0-9 +_-]", "").trim());
            clinicalDiagnosis = new StringBuilder(clinicalDiagnosis.toString().replaceAll("\\s\\s", " "));


            List<Sample> sampleList = new ArrayList<>();

            for (JsonNode sample : samples) {
                Map dSample = mapper.convertValue(sample, Map.class);

                if (specimenSearch.get("SPECIMENSEQNBR").equals(dSample.get("SPECIMENSEQNBR"))) {

                    // Retrieve sample data
                    sampleId = dSample.get("SAMPLEID")+"";

                    wholeExomeSeqYn = dSample.get("WHOLEEXOMESEQUENCEFTPYN")+"";
                    rnaSeqYn = dSample.get("RNASEQUENCEFTPYN")+"";

                    samplePassage = String.valueOf(dSample.get("PASSAGEOFTHISSAMPLE"));

                    if ( isNumeric(samplePassage)){
                        if (!sampleId.contains("CAF")){
                            sampleTumorType = "engrafted Tumor";
                            sampleList.add(new Sample(sampleId,sampleTumorType,samplePassage,wholeExomeSeqYn,wholeExomeSeqYn,wholeExomeSeqYn,rnaSeqYn,rnaSeqYn));
                        }else {
                            log.warn("This is Strange, CAF Culture that has passage number");
                        }
                    }else{
                        if (sampleId.equals("ORIGINATOR")){
                            sampleTumorType = "patient Tumor";
                            samplePassage = null;
                            sampleList.add(new Sample(sampleId,sampleTumorType,samplePassage,wholeExomeSeqYn,wholeExomeSeqYn,wholeExomeSeqYn,rnaSeqYn,rnaSeqYn));
                        }else {
                            log.warn("This is neither PDX nor Patient Sample ");
                        }
                    }





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




            List<Treatment> treatments = new ArrayList<>();

            for (JsonNode currentTherapy : currentTherapies) {
                Map dCurrentTherapy = mapper.convertValue(currentTherapy, Map.class);


                if (specimenSearch.get("PATIENTSEQNBR").equals(dCurrentTherapy.get("PATIENTSEQNBR"))) {


                    startingDate = dCurrentTherapy.get("DATEREGIMENSTARTED")+"";
                    try {
                        startingDate = startingDate.equals("null") ? unKnown : startingDate.substring(0, 10);
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    }



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

                    drug=""; duration = unKnown; response="";
                }

            }




            for (JsonNode priorTherapy : priorTherapies) {
                Map dPriorTherapy = mapper.convertValue(priorTherapy, Map.class);

                if (specimenSearch.get("PATIENTSEQNBR").equals(dPriorTherapy.get("PATIENTSEQNBR"))) {

                    priorDate = dPriorTherapy.get("DATEREGIMENSTARTED")+"";
                    try {
                        priorDate = priorDate.equals("null") ? unKnown : priorDate.substring(0, 10);
                    } catch (Exception e) { log.error(e.getMessage());}

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

                    drug=""; duration = unKnown; response="";
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

                    dateAtCollection = specimen.get("COLLECTIONDATE").toString().substring(0, 10);

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
                Map<String, String> mappingData = new LinkedHashMap<>();
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

            log.trace("{} has no sample in the database", modelID);
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


    public boolean isNumeric(String val) {
        boolean report = false;

        try {
            Double.parseDouble(val);
            report = true;
        } catch (Exception e) { }

        return report;
    }


    public List transformOncoKB(){


        String oncoKbUrl = String.format("%s%s", this.dataRootDir, "/PDMR/raw/PDMR_ONCOKBGENEPANEL.json");
        String sampleUrl = String.format("%s%s", this.dataRootDir, "/PDMR/raw/PDMR_SAMPLE.json");
        String specimenSearchUrl = String.format("%s%s", this.dataRootDir, "/PDMR/raw/PDMR_SPECIMENSEARCH.json");
        String hgncSymbolUrl = String.format("%s%s", this.dataRootDir, "/PDMR/raw/PDMR_HUGOGENESYMBOL.json");
        String variantClassUrl = String.format("%s%s", this.dataRootDir, "/PDMR/raw/PDMR_VARIANTCLASS.json");



        List<Map<String, String>> oncoKbData = util.serializeDataToMaps(oncoKbUrl);
        List<Map<String, String>> sampleData = util.serializeDataToMaps(sampleUrl);
        List<Map<String, String>> specimenSearchData = util.serializeDataToMaps(specimenSearchUrl);
        List<Map<String, String>> hgncSymbolData = util.serializeDataToMaps(hgncSymbolUrl);
        List<Map<String, String>> variantClassData = util.serializeDataToMaps(variantClassUrl);


        List<Map<OmicCSVColumn, String>> transformedData = new ArrayList<>();

        for (Map<String, String> oncoKb : oncoKbData) {

            AtomicBoolean validData = new AtomicBoolean(false);
            Map<OmicCSVColumn, String> rowMap = new LinkedHashMap<>();

            // Get Model ID Column
            // Get Sample from Sample Data and if SAMPLESEQNBR is found, get SPECIMENSEQNBR
            sampleData.forEach(sample -> {

                if (String.valueOf(oncoKb.get("SAMPLESEQNBR")).equals(String.valueOf(sample.get("SAMPLESEQNBR")))) {

                    rowMap.put(OmicCSVColumn.DATASOURCE, "PDMR");

                    // Search for the specimenSeqNumber inside the sampleSearch Data
                    specimenSearchData.forEach(specimen -> {
                        if (String.valueOf(specimen.get("SPECIMENSEQNBR")).equals(String.valueOf(sample.get("SPECIMENSEQNBR")))) {
                            rowMap.put(OmicCSVColumn.MODEL_ID, specimen.get("PATIENTID") + "-" + specimen.get("SPECIMENID"));
                        }
                    });

                    // Get Sample ID Column
                    rowMap.put(OmicCSVColumn.SAMPLE_ID, sample.get("SAMPLEID"));

                    String samplePassage = sample.get("PASSAGEOFTHISSAMPLE");

                    // Get Sample Origin
                    if ( isNumeric(samplePassage) ){
                        rowMap.put(OmicCSVColumn.SAMPLE_ORIGIN, "engrafted tumor");
                        rowMap.put(OmicCSVColumn.PASSAGE, samplePassage);
                        validData.set(true);
                    }else {
                        if (sample.get("SAMPLEID").equals("ORIGINATOR")){
                            rowMap.put(OmicCSVColumn.SAMPLE_ORIGIN, "patient tumor");
                            rowMap.put(OmicCSVColumn.PASSAGE, "");
                            validData.set(true);
                        }else {
                            validData.set(false);
                        }
                    }

                    // Get Host Strain name Column
                    rowMap.put(OmicCSVColumn.HOST_STRAIN_NAME, "NOD.Cg-Prkdcscid Il2rgtm1Wjl/SzJ");
                }
            });

            // Get Gene Symbol or HGNC Symbol
            hgncSymbolData.forEach(hgncSymbol -> {
                if ( String.valueOf(oncoKb.get("HUGOGENESYMBOLSEQNBR")).equals(String.valueOf(hgncSymbol.get("HUGOGENESYMBOLSEQNBR"))) ){
                    rowMap.put(OmicCSVColumn.HGNC_SYMBOL, hgncSymbol.get("HUGOGENESYMBOLDESCRIPTION"));
                }
            });

            // Get Coding Sequence Change
            rowMap.put(OmicCSVColumn.CODING_SEQUENCE_CHANGE,
                       Optional.ofNullable(oncoKb.get("HGVSCDNACHANGE")).isPresent() ?
                               oncoKb.get("HGVSCDNACHANGE").replace("c.","") : "");

            // Get Amino Acid Change
            rowMap.put(OmicCSVColumn.AMINO_ACID_CHANGE,
                       Optional.ofNullable(oncoKb.get("HGVSPROTEINCHANGE")).isPresent() ?
                               oncoKb.get("HGVSPROTEINCHANGE").replace("p.","") : "");

            // Get Consequence Column
            variantClassData.forEach(variantClass -> {
                if ( String.valueOf(oncoKb.get("VARIANTCLASSSEQNBR")).equals(String.valueOf(variantClass.get("VARIANTCLASSSEQNBR"))) ){
                    rowMap.put(OmicCSVColumn.CONSEQUENCE, variantClass.get("VARIANTCLASSDESCRIPTION"));
                }
            });

            // Get Functional Prediction
            rowMap.put(OmicCSVColumn.FUNCTIONAL_PREDICTION,
                       Optional.ofNullable(oncoKb.get("POLYPHEN")).isPresent() ?
                               String.format("%s|sift",oncoKb.get("POLYPHEN")) : "");

            // Read Depth
            rowMap.put(OmicCSVColumn.READ_DEPTH, oncoKb.get("TOTALREADS"));

            // Get Allele Frequency
            rowMap.put(OmicCSVColumn.ALLELE_FREQUENCY, oncoKb.get("VARIANTADELLEFREQ"));

            // Get Chromoseme Column
            rowMap.put(OmicCSVColumn.CHROMOSOME,
                       Optional.ofNullable(oncoKb.get("CHROMOSOME")).isPresent() ?
                               oncoKb.get("CHROMOSOME").replace("chr","") : "");

            // Seq Start Position
            rowMap.put(OmicCSVColumn.SEQ_START_POSITION, oncoKb.get("STARTPOSITION"));

            // Get Ref Allele
            rowMap.put(OmicCSVColumn.REF_ALLELE, oncoKb.get("REFERENCEALLELE"));

            // Get Alt Allele
            rowMap.put(OmicCSVColumn.ALT_ALLELE, oncoKb.get("ALTALLELE"));

            // Get Gene IDs
            rowMap.put(OmicCSVColumn.UCSC_GENE_ID, "");
            rowMap.put(OmicCSVColumn.NCBI_GENE_ID, "");
            rowMap.put(OmicCSVColumn.ENSEMBL_GENE_ID, "");
            rowMap.put(OmicCSVColumn.ENSEMBL_TRANSCRIPT_ID, "");


            // Get Variation Id
            rowMap.put(OmicCSVColumn.RS_ID_VARIANT,
                       Optional.ofNullable(oncoKb.get("EXISTINGVARIANT")).isPresent() ?
                               oncoKb.get("EXISTINGVARIANT") : "");

            // Get Genome Assembly
            rowMap.put(OmicCSVColumn.GENOME_ASSEMBLY, "hg19");

            // Get Platform Column
            rowMap.put(OmicCSVColumn.PLATFORM, "OncoKB Gene Panel");


            if (validData.get())
                transformedData.add(rowMap);

        }


        return transformedData;

    }



    public List transformPDMRGenomics(List<Map<String, String>> untransformedGenomicData){

        List<Map<OmicCSVColumn, String>> transformedData = new ArrayList<>();

        for (Map<String, String> data : untransformedGenomicData) {

            boolean validData = true;
            Map<OmicCSVColumn, String> rowMap = new LinkedHashMap<>();


            // Get Sample ID Column
            rowMap.put(OmicCSVColumn.DATASOURCE, "PDMR");
            rowMap.put(OmicCSVColumn.MODEL_ID, data.get(PdmrOmicCol.PATIENT_ID.get()).concat("-").concat(data.get(PdmrOmicCol.SPECIMEN_ID.get())));

            // Get Sample ID Column
            if (data.get(PdmrOmicCol.PDM_TYPE.get()).equals("PDX") || data.get(PdmrOmicCol.PDM_TYPE.get()).equals(PdmrOmicCol.PDM_TYPE_PATENT.get())){

                rowMap.put(OmicCSVColumn.SAMPLE_ID, data.get(PdmrOmicCol.SAMPLE_ID.get()));
            }else {
                validData = false;
            }

            String modelID = rowMap.get(OmicCSVColumn.MODEL_ID);
            String sampleID = rowMap.get(OmicCSVColumn.SAMPLE_ID);

            // Get Sample Origin and Passage Column
            if (data.get(PdmrOmicCol.PDM_TYPE.get()).equals("PDX")){

                rowMap.put(OmicCSVColumn.SAMPLE_ORIGIN, "engrafted tumor");

                String passage = getPassageByModelIDAndSampleID(modelID, sampleID);
                rowMap.put(OmicCSVColumn.PASSAGE, passage);

            }else if (data.get(PdmrOmicCol.PDM_TYPE.get()).equals(PdmrOmicCol.PDM_TYPE_PATENT.get())){

                rowMap.put(OmicCSVColumn.SAMPLE_ORIGIN, "patient tumor");
                rowMap.put(OmicCSVColumn.PASSAGE, "");
            }else {
                validData = false;
            }


            // Get Host Strain name Column
            rowMap.put(OmicCSVColumn.HOST_STRAIN_NAME, "");

            // Get Gene Symbol or HGNC Symbol
            if (data.get(PdmrOmicCol.GENE.get()).equals("None Found")){

                validData = false;
            }else {
                rowMap.put(OmicCSVColumn.HGNC_SYMBOL, data.get(PdmrOmicCol.GENE.get()));
            }

            // Get Amino Acid Change
            rowMap.put(OmicCSVColumn.AMINO_ACID_CHANGE, data.get(PdmrOmicCol.AA_CHANGE.get()));

            // Get Nucleotide Change
            rowMap.put(OmicCSVColumn.NUCLEOTIDE_CHANGE, data.get(PdmrOmicCol.CODON_CHANGE.get()));

            // Get Consequence Column
            rowMap.put(OmicCSVColumn.CONSEQUENCE, data.get(PdmrOmicCol.IMPACT.get()));

            // Read Depth
            rowMap.put(OmicCSVColumn.READ_DEPTH, data.get(PdmrOmicCol.READ_DEPTH.get()));

            // Get Allele Frequency
            rowMap.put(OmicCSVColumn.ALLELE_FREQUENCY, data.get(PdmrOmicCol.ALLELE_FREQUENCY.get()));

            // Get Chromoseme Column
            rowMap.put(OmicCSVColumn.CHROMOSOME, data.get(PdmrOmicCol.CHR.get()));

            // Seq Start Position
            rowMap.put(OmicCSVColumn.SEQ_START_POSITION, data.get(PdmrOmicCol.POSITION.get()));

            // Get Ref Allele
            rowMap.put(OmicCSVColumn.REF_ALLELE, data.get(PdmrOmicCol.REF_ALLELE.get()));

            // Get Alt Allele
            rowMap.put(OmicCSVColumn.ALT_ALLELE, data.get(PdmrOmicCol.ALT_ALLELE.get()));

            // Get Gene IDs
            rowMap.put(OmicCSVColumn.UCSC_GENE_ID, "");
            rowMap.put(OmicCSVColumn.NCBI_GENE_ID, "");
            rowMap.put(OmicCSVColumn.ENSEMBL_GENE_ID, "");
            rowMap.put(OmicCSVColumn.ENSEMBL_TRANSCRIPT_ID, "");

            // Get RS ID Variant
            rowMap.put(OmicCSVColumn.RS_ID_VARIANT, data.get(PdmrOmicCol.DB_SNP_ID.get()));

            // Get Genome Assembly
            rowMap.put(OmicCSVColumn.GENOME_ASSEMBLY, "hg19");

            // Get Platform Column
            rowMap.put(OmicCSVColumn.PLATFORM, "NCI cancer gene panel");

            if (validData)
                transformedData.add(rowMap);

        }


        return transformedData;
    }


}
