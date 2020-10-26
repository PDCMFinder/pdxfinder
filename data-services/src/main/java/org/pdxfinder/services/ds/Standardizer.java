package org.pdxfinder.services.ds;

import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sbn
 */
public class Standardizer {

    public static final String MALE = "Male";
    public static final String FEMALE = "Female";
    public static final String NOT_SPECIFIED = "Not Specified";

    // tumor types
    public static final String PRIMARY ="Primary";
    public static final String RECURRENT = "Recurrent";
    public static final String METASTATIC = "Metastatic";
    public static final String REFRACTORY = "Refractory";

    private final static Logger log = LoggerFactory.getLogger(Standardizer.class);


    public static String getGender(String gender){

        if(gender.toUpperCase().startsWith("F")){
            gender = FEMALE;
        }else if(gender.toUpperCase().startsWith("M")){
            gender = MALE;
        }else {
            gender = NOT_SPECIFIED;
        }
        return gender;

    }

    public static String getTumorType(String tumorType){

        if(tumorType == null){
            tumorType = "";
        }
        if(tumorType.toUpperCase().startsWith("PRI")){
            tumorType = PRIMARY;
        }else if(tumorType.toUpperCase().startsWith("MET")){
            tumorType = METASTATIC;
        }else if(tumorType.toUpperCase().startsWith("REF")){
            tumorType = REFRACTORY;
        }else if(tumorType.toUpperCase().startsWith("REC")){
            tumorType = RECURRENT;
        }else{
            tumorType = NOT_SPECIFIED;
        }
        return tumorType;

    }

    public static String getAge(String age){

        if(age.toLowerCase().equals("not specified")) return NOT_SPECIFIED;

        try{
            age = new Integer(age).toString();
        }catch(NumberFormatException nfe){
            log.error("Cant convert "+age+" to a numeric age. Using "+NOT_SPECIFIED);
            age = NOT_SPECIFIED;
        }

        return age;
    }

    public static String getValue(String name, JSONObject j){
        String value = NOT_SPECIFIED;

        if(j.has(name)){
            try {
                value = j.getString(name);
                if(value.trim().length()==0) {
                    value = NOT_SPECIFIED;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        return value;
    }

    public static String fixNotString(String in){
        if(in.toUpperCase().startsWith("Not")){
            in = Standardizer.NOT_SPECIFIED;
        }
        return in;

    }


    public static String getDrugResponse(String r){

        if(r == null || r.isEmpty()) return "Not Specified";

        if(r.toLowerCase().equals("pd") || r.toLowerCase().equals("progressive disease")) return "Progressive Disease";
        if(r.toLowerCase().equals("sd") || r.toLowerCase().equals("stable disease")) return "Stable Disease";
        if(r.toLowerCase().equals("cr") || r.toLowerCase().equals("complete response")) return "Complete Response";
        if(r.toLowerCase().equals("pr") || r.toLowerCase().equals("partial response")) return "Partial Response";
        if(r.equals("stable disease/complete response")) return "Stable Disease And Complete Response";

        return r;
    }




    public static String getTreatmentComponentType(String drugName){

        drugName = drugName.trim();

        if(drugName.toLowerCase().contains("control")) return "Control";

        if(drugName.equals("Saline") || drugName.equals("D5W") || drugName.equals("CMC") ||
                drugName.equals("DMSO") || drugName.equals("0.9% Solution of Sodium Chloride")) return "Control";

        return "Drug";
    }



    public static String getEthnicity(String ethnicity){

        if(ethnicity.toLowerCase().equals("caucasian")) return "Caucasian";

        if(ethnicity.toLowerCase().equals("hispanic") || ethnicity.toLowerCase().equals("hispanic or latino")) return "Hispanic or Latino";

        if(ethnicity.toLowerCase().equals("white")) return "White";

        if(ethnicity.toLowerCase().equals("african american") || ethnicity.toLowerCase().equals("aa")) return "African American";

        if(ethnicity.toLowerCase().equals("unknown") || ethnicity.toLowerCase().equals("not specified") || ethnicity.toLowerCase().equals("not reported")) return "Not Specified";

        if(ethnicity.equals("White; Hispanic or Latino")) return ethnicity;

        if(ethnicity.toLowerCase().equals("black")) return "Black";

        if(ethnicity.toLowerCase().equals("asian")) return "Asian";

        if(ethnicity.equals("Black or African American; Not Hispanic or Latino")) return "Black or African American; Not Hispanic or Latino";

        if(ethnicity.equals("White; Not Hispanic or Latino")) return ethnicity;

        if(ethnicity.equals("Black or African American")) return ethnicity;

        if(ethnicity.equals("Not Hispanic or Latino")) return ethnicity;

        return "Not Specified";

    }


}
