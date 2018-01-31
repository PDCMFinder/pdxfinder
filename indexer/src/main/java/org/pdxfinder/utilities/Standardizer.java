/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.utilities;
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
    
}
