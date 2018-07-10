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

    public DataTransformerService(PdmrPdxInfoRepository pdmrPdxInfoRepository){
        this.pdmrPdxInfoRepository = pdmrPdxInfoRepository;
    }

    //Transformation rule as specified here: https://docs.google.com/spreadsheets/d/1buUu5yj3Xq8tbEtL1l2UILV9kLnouGqF0vIjFlGGbEE
    public JsonNode transformDataAndSave(String url1,String url2,String url3,String url4,String url5,String url6,String url7){

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
        String sourceUrl = "";
        String extractionMethod = "";
        String dateAtCollection = "";
        String accessibility = "";


        String report = "";


        // Read the whole JSOn as a JsonNode type & Retrieve each specimen search record as a Map (key value type) type
        JsonNode rootArray = connectToJSON(url1);

        JsonNode pdmrSpecimenData = connectToJSON(url2);

        JsonNode tissueOrigins = connectToJSON(url3);

        JsonNode tumorGradeStageTypes = connectToJSON(url4);

        JsonNode mouseStrains = connectToJSON(url5);

        JsonNode impantationSites = connectToJSON(url6);

        JsonNode tissueTypes = connectToJSON(url7);


        for (JsonNode node : rootArray)
        {

            Map<String, Object> specimenSearch = mapper.convertValue(node, Map.class);

            modelID = specimenSearch.get("PATIENTID")+"-"+specimenSearch.get("SPECIMENID");
            patientID = specimenSearch.get("PATIENTID")+"";
            gender = specimenSearch.get("GENDER")+"";
            race = specimenSearch.get("RACEDESCRIPTION")+"";
            ethnicity = specimenSearch.get("ETHNICITYDESCRIPTION")+"";
            primarySite = specimenSearch.get("DISEASELOCATIONDESCRIPTION")+"";
            initialDiagnosis = "";
            clinicalDiagnosis = specimenSearch.get("MEDDRADESCRIPTION")+"";
            tumorType = "";
            stageClassification = "";
            stageValue = "";
            gradeClassification = "";
            gradeValue = "";
            sampleType = specimenSearch.get("TISSUETYPEDESCRIPTION")+"";
            strain = "";
            mouseSex = "";
            engraftmentSite = "";
            sourceUrl = DATASOURCE_URL_PREFIX+specimenSearch.get("SPECIMENSEQNBR");
            extractionMethod = "";
            dateAtCollection = "";
            accessibility = "";

            treatmentNaive = "Unknown";
            age = "";
            specimenSite = "";


            // Treatment naive
            try{
                if ( specimenSearch.get("CURRENTREGIMEN").toString().equalsIgnoreCase("Treatment naive") ) {
                    treatmentNaive = "Treatment Naive";
                }
            }catch (Exception e){}


            try{
                if ( specimenSearch.get("PRIORREGIMEN").toString().equalsIgnoreCase("Treatment naive") ) {
                    treatmentNaive = "Treatment Naive";
                }
            }catch (Exception e){}






            for (JsonNode tumorGradeStageType : tumorGradeStageTypes)
            {

                Map<String, Object> dTumorGradeStageType = mapper.convertValue(tumorGradeStageType, Map.class);

                if (specimenSearch.get("TUMORGRADESTAGESEQNBR").equals(dTumorGradeStageType.get("TUMORGRADESTAGESEQNBR"))){

                    String x = specimenSearch.get("TUMORGRADESTAGESEQNBR")+"";

                    if ( x.equals("1") || x.equals("8") || x.equals("9") || x.equals("10") || x.equals("11") ){
                        //1,8,9,10,11
                        stageClassification = dTumorGradeStageType.get("TUMORGRADESTAGESHORTNAME")+"";
                        gradeClassification = "Not Specified";
                    }else{
                        //2,3,4,5,6,7,12,13
                        gradeClassification = dTumorGradeStageType.get("TUMORGRADESTAGESHORTNAME")+"";
                        stageClassification = "Not Specified";
                    }
                }
            }


            for (JsonNode tissueType : tissueTypes) // TISSUETYPESHORTNAME
            {
                Map<String, Object> dTissueType = mapper.convertValue(tissueType, Map.class);

                if (specimenSearch.get("TISSUETYPESHORTNAME").equals(dTissueType.get("TISSUETYPESHORTNAME"))){
                    extractionMethod = dTissueType.get("TISSUETYPEDESCRIPTION")+"";
                }
            }



            for (JsonNode pdmrSpecimen : pdmrSpecimenData)
            {

                Map<String, Object> specimen = mapper.convertValue(pdmrSpecimen, Map.class);

                if (specimenSearch.get("SPECIMENID").equals(specimen.get("SPECIMENID"))){

                    age = specimen.get("AGEATSAMPLING")+"";
                    specimenSite = specimen.get("BIOPSYSITE")+"";

                    dateAtCollection = specimen.get("COLLECTIONDATE")+"";
                    try {
                        dateAtCollection = dateAtCollection.substring(0, 10);
                    }catch (Exception e){}

                    accessibility = specimen.get("PUBLICACCESSYN")+"";
                    if (accessibility.equals("Y")){
                        accessibility = "Public";
                    }


                        for (JsonNode mouseStrain : mouseStrains)
                        {
                            Map<String, Object> dMouseStrains = mapper.convertValue(mouseStrain, Map.class);

                            if (specimen.get("MOUSESTRAINSEQNBR").equals(dMouseStrains.get("MOUSESTRAINSEQNBR"))){
                                    strain = dMouseStrains.get("MOUSESTRAINDESCRIPT")+"";
                            }
                        }



                        for (JsonNode impantationSite : impantationSites)
                        {
                            Map<String, Object> dImpantationSites = mapper.convertValue(impantationSite, Map.class);

                            if (specimen.get("IMPLANTATIONSITESEQNBR").equals(dImpantationSites.get("IMPLANTATIONSITESEQNBR"))){
                                engraftmentSite = dImpantationSites.get("IMPLANTATIONSITEDESCRIPTION")+"";
                            }
                            /* with the number to to table IMPLANTATIONSITES, key is IMPLANTATIONSITESEQNBR, value in finde should be IMPLANTATIONSITEDESCRIPTION */
                        }


                        // Retrieve details of specimen.get("PROVIDEDTISSUEORIGINSEQNBR").
                        for (JsonNode tissueOrigin : tissueOrigins)
                        {
                            Map<String, Object> tissue = mapper.convertValue(tissueOrigin, Map.class);
                            if (specimen.get("PROVIDEDTISSUEORIGINSEQNBR").equals(tissue.get("PROVIDEDTISSUEORIGINSEQNBR"))) {
                                tumorType = tissue.get("PROVIDEDTISSUEORIGINDESCRIPT") + "";
                            }
                        }

                }


            }


            try{
                pdmrPdxInfoRepository.save(
                        new PdmrPdxInfo(modelID,patientID,gender,age,race,ethnicity,specimenSite,primarySite,initialDiagnosis,
                                clinicalDiagnosis,tumorType,stageClassification,stageValue,gradeClassification,gradeValue,
                                sampleType,strain,mouseSex,treatmentNaive,engraftmentSite,sourceUrl,extractionMethod,dateAtCollection,accessibility)
                );
                log.info("Loaded Record for Patient"+specimenSearch.get("PATIENTID"));
            }catch (Exception e){
                log.info("Record for Patient"+specimenSearch.get("PATIENTID")+ "Not Loaded");
            }

            report += "Loaded Record for Patient "+specimenSearch.get("PATIENTID")+"<br>";

        }

        return rootArray;


    }



    public JsonNode connectToJSON(String apiLink)
    {

        JsonNode rootArray = null;
        ObjectMapper mapper = new ObjectMapper();

        try
        {

            URL url = new URL(apiLink);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200)
            {
                throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader( (conn.getInputStream()) ));

            rootArray = mapper.readTree(br);
            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootArray;

    }


    public List<PdmrPdxInfo> getAllPdmr()
    {
        List<PdmrPdxInfo> pdmrPdxInfos = pdmrPdxInfoRepository.findAll();

        return pdmrPdxInfos;
    }

}
