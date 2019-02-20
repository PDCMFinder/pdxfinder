package org.pdxfinder.services.ds;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.QualityAssurance;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Hamonizer {


    static final String hci = "PDXNet-HCI-BCM";
    static final String mdAnderson = "PDXNet-MDAnderson";
    static final String irccCrc = "IRCC-CRC";
    static final String wustl = "PDXNet-WUSTL";

    @Autowired
    private static DataImportService dataImportService;


    public static JSONArray getTreament(JSONObject data, String ds) throws Exception {

        JSONArray treatments = new JSONArray();

        if (ds.equals(hci)){

            try {
                if (data.has("Treatments")) {
                    JSONObject treatmentObj = data.optJSONObject("Treatments");
                    //if the treatment attribute is not an object = it is an array
                    if (treatmentObj == null && data.optJSONArray("Treatments") != null) {
                        treatments = data.getJSONArray("Treatments");
                    }
                }
            }catch (Exception e){}
        }
        return treatments;
    }





    public static JSONArray getSpecimens(JSONObject data,String ds) throws Exception {

        JSONArray specimens = new JSONArray();

        if (ds.equals(irccCrc)){
            specimens = data.getJSONArray("Specimens");
        }
        return specimens;
    }


    public static String getFingerprinting(JSONObject data,String ds) throws Exception {
        String fingerprinting = Standardizer.NOT_SPECIFIED;

        if (ds.equals(irccCrc)){
            fingerprinting = data.getString("Fingerprinting");
        }
        return fingerprinting;
    }


    public static String getSampleID(JSONObject data,String ds) throws Exception {
        String sampleID = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)){
            sampleID = data.getString("Sample ID");
        }

        if (ds.equals(irccCrc) || ds.equals(mdAnderson)){
            sampleID = data.getString("Model ID");
        }

        return sampleID;
    }

    public static String getDiagnosis(JSONObject data,String ds) throws Exception {
        String diagnosis = data.getString("Clinical Diagnosis");

        if (ds.equals(mdAnderson)){
            // mdAnderson preference is for histology
            String histology = data.getString("Histology");
            if (histology.trim().length() > 0) {
                if ("ACA".equals(histology)) {
                    diagnosis = "Adenocarcinoma";
                } else {
                    diagnosis = histology;
                }
            }
        }

        if (ds.equals(wustl)){
            // Preference is for Histology
            String histology = data.getString("Histology");
            if (histology.trim().length() > 0) {
                diagnosis = histology;
            }
        }

        return diagnosis;
    }

    public static String getEthnicity(JSONObject data,String ds) throws Exception {
        String ethnicity = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)) {
            ethnicity = data.getString("Ethnicity");
        }

        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            ethnicity = Standardizer.getValue("Race",data);
            try {
                if (data.getString("Ethnicity").trim().length() > 0) {
                    ethnicity = data.getString("Ethnicity");
                }
            } catch (Exception e) {}
        }
        return ethnicity;
    }


    public static String getClassification(JSONObject data,String ds) throws Exception {
        String classification = Standardizer.NOT_SPECIFIED;

        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            classification = data.getString("Stage") + "/" + data.getString("Grades");
        }

        if (ds.equals(irccCrc) || ds.equals(wustl)){
            classification = data.getString("Stage");
        }

        return classification;
    }




    public static String getImplantationType(JSONObject data,String ds) throws Exception {
        String implantationTypeStr = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)){
            implantationTypeStr = Standardizer.getValue("Implantation Type", data);
        }

        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            implantationTypeStr =  Standardizer.getValue("Tumor Prep",data);
        }

        return implantationTypeStr;
    }


    public static String getEngraftmentSite(JSONObject data,String ds) throws Exception {
        String implantationSite = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci) || ds.equals(mdAnderson) || ds.equals(wustl)){
            implantationSite = Standardizer.getValue("Engraftment Site", data);
        }
        return implantationSite;
    }


    public static String getMarkerPlatform(JSONObject data,String ds) throws Exception {
        String markerPlatform = Standardizer.NOT_SPECIFIED;
        if (ds.equals(mdAnderson) || ds.equals(wustl)){
            markerPlatform = data.getString("Marker Platform");
        }
        return markerPlatform;
    }


    public static String getMarkerStr(JSONObject data,String ds) throws Exception {
        String markerStr = Standardizer.NOT_SPECIFIED;

        if (ds.equals(mdAnderson)){
            markerStr = data.getString("Markers");
        }

        return markerStr;
    }


    public static String getQAPassage(JSONObject data,String ds) throws Exception {
        String passage = Standardizer.NOT_SPECIFIED;
        if (ds.equals(mdAnderson)){
            passage = data.getString("QA Passage").replaceAll("P", "");
        }
        return passage;
    }









    public static QualityAssurance getQualityAssurance(JSONObject data, String ds)  throws Exception{

        QualityAssurance qa = new QualityAssurance();
        String qaType = Standardizer.NOT_SPECIFIED;

        if (ds.equals(hci)){

            // This multiple QA approach only works because Note and Passage are the same for all QAs
            qa = new QualityAssurance(Standardizer.NOT_SPECIFIED,Standardizer.NOT_SPECIFIED,Standardizer.NOT_SPECIFIED);

            StringBuilder technology = new StringBuilder();
            if(data.has("QA")){
                JSONArray qas = data.getJSONArray("QA");
                for (int i = 0; i < qas.length(); i++) {
                    if (qas.getJSONObject(i).getString("Technology").equalsIgnoreCase("histology")) {
                        qa.setTechnology(qas.getJSONObject(i).getString("Technology"));
                        qa.setDescription(qas.getJSONObject(i).getString("Note"));
                        qa.setPassages(qas.getJSONObject(i).getString("Passage"));
                    }
                }
            }

        }



        if (ds.equals(mdAnderson) || ds.equals(wustl)) {

            try {
                qaType = data.getString("QA") + " on passage " + data.getString("QA Passage");
            } catch (Exception e) {
                // not all groups supplied QA
            }

            String qaPassage = data.has("QA Passage") ? data.getString("QA Passage") : null;
            qa = new QualityAssurance(qaType, Standardizer.NOT_SPECIFIED, qaPassage);
            dataImportService.saveQualityAssurance(qa);
        }


        if (ds.equals(irccCrc)) {

            String FINGERPRINT_DESCRIPTION = "Model validated against patient germline.";

            if ("TRUE".equals(data.getString("Fingerprinting").toUpperCase())) {
                qa.setTechnology("Fingerprint");
                qa.setDescription(FINGERPRINT_DESCRIPTION);

                // If the model includes which passages have had QA performed, set the passages on the QA node
                if (data.has("QA Passage") && !data.getString("QA Passage").isEmpty()) {

                    List<String> passages = Stream.of(data.getString("QA Passage").split(","))
                            .map(String::trim)
                            .distinct()
                            .collect(Collectors.toList());
                    List<Integer> passageInts = new ArrayList<>();

                    // NOTE:  IRCC uses passage 0 to mean Patient Tumor, so we need to harmonize according to the other
                    // sources.  Subtract 1 from every passage.
                    for (String p : passages) {
                        Integer intPassage = Integer.parseInt(p);
                        passageInts.add(intPassage - 1);
                    }

                    qa.setPassages(StringUtils.join(passageInts, ", "));
                }
            }

        }


        return qa;
    }
}
