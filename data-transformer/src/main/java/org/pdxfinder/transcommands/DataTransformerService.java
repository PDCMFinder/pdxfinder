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

    private final static Logger log = LoggerFactory.getLogger(TransController.class);

    public DataTransformerService(PdmrPdxInfoRepository pdmrPdxInfoRepository){
        this.pdmrPdxInfoRepository = pdmrPdxInfoRepository;
    }


    //Transformation rule as specified here: https://docs.google.com/spreadsheets/d/1buUu5yj3Xq8tbEtL1l2UILV9kLnouGqF0vIjFlGGbEE
    public JsonNode transformDataAndSave(String url1,String url2){

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
        String grades = "";
        String tumorStage = "";
        String sampleType = "";
        String strain = "";
        String mouseSex = "";
        String treatmentNaive = "";
        String engraftmentSite = "";

        String report = "";


        // Read the whole JSOn as a JsonNode type & Retrieve each specimen search record as a Map (key value type) type
        JsonNode rootArray = connectToJSON(url1);

        JsonNode pdmrSpecimenData = connectToJSON(url2);


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
            grades = specimenSearch.get("TUMORGRADESTAGEDESCRIPTION")+"";
            tumorStage = specimenSearch.get("TUMORGRADESTAGEDESCRIPTION")+"";
            sampleType = specimenSearch.get("TISSUETYPEDESCRIPTION")+"";
            strain = "NSG";
            mouseSex = "";
            engraftmentSite = "Subcutaneous";

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


            for (JsonNode pdmrSpecimen : pdmrSpecimenData)
            {

                Map<String, Object> specimen = mapper.convertValue(pdmrSpecimen, Map.class);

                if (specimenSearch.get("SPECIMENID").equals(specimen.get("SPECIMENID"))){

                    age = specimen.get("AGEATSAMPLING")+"";
                    specimenSite = specimen.get("BIOPSYSITE")+"";
                }


            }


            try{
                pdmrPdxInfoRepository.save(
                        new PdmrPdxInfo(modelID,patientID,gender,age,race,ethnicity,specimenSite,primarySite,initialDiagnosis,
                                clinicalDiagnosis,tumorType,grades,tumorStage,sampleType,strain,mouseSex,treatmentNaive,engraftmentSite)
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
