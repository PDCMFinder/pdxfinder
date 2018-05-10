/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.services.ds;
import org.neo4j.ogm.json.JSONObject;
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
        try{
            value = j.getString(name);
            if(value.trim().length()==0){
                value = NOT_SPECIFIED;
            }
        }catch(Exception e){}
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

         if(r.toLowerCase().equals("pd")) return "Progressive Disease";
         if(r.toLowerCase().equals("sd")) return "Stable Disease";
         if(r.toLowerCase().equals("cr")) return "Complete Response";
         if(r.toLowerCase().equals("pr")) return "Partial Response";

         return r;
     }


     public static String getDrugName(String d){

         if(d == null || d.isEmpty()) return "Not Specified";

         if(d.equals("Erbitux, Cetuximab")) return "Cetuximab (Erbitux®)";
         if(d.equals("0.9% Solution of Sodium Chloride")) return "0.9% Solution of Sodium Chloride (control)";

         return d;
     }

}
