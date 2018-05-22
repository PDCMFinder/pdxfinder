/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.services.ds;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.TreatmentProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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

     public static TreatmentProtocol getTreatmentProtocol(String drug){

         TreatmentProtocol tp = new TreatmentProtocol();
         boolean updated = false;

         if(drug.toLowerCase().equals("doxorubicin")){

             tp.addDrug("Doxorubicin");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.toLowerCase().equals("doxorubicin+cyclophosphamide")){

             tp.addDrug("Doxorubicin");
             tp.addType("Drug");
             tp.addTarget("DNA");

             tp.addDrug("Cyclophosphamide");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.toLowerCase().equals("paclitaxel")){

             tp.addDrug("Paclitaxel");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.toLowerCase().equals("tamoxifen")){

             tp.addDrug("Tamoxifen");
             tp.addType("Drug");
             tp.addTarget("estrogen receptor");
             updated = true;
         }
         else if(drug.toLowerCase().equals("trastuzumab")){

             tp.addDrug("Trastuzumab");
             tp.addType("Drug");
             tp.addTarget("erbb2");
             updated = true;
         }
         else if(drug.equals("0.9% Solution of Sodium Chloride") || drug.equals("Saline")){

             tp.addDrug("Sodium choride solution");
             tp.addType("Control");
             updated = true;
         }
         else if(drug.equals("Erbitux, Cetuximab")){

             tp.addDrug("Cetuximab");
             tp.addType("Drug");
             tp.addTarget("EGFR");
             updated = true;
         }
         else if(drug.equals("DSW (control)")){

             tp.addDrug("Dextrose solution");
             tp.addType("Control");
             updated = true;
         }
         else if(drug.equals("D5W + CMC)")){

             tp.addDrug("Dextrose solution");
             tp.addType("Control");

             tp.addDrug("Carboxymethyl cellulose");
             tp.addType("Control");
             updated = true;
         }
         else if(drug.equals("DMSO")){

             tp.addDrug("Dimethyl sulfoxide");
             tp.addType("Control");
             updated = true;
         }
         else if(drug.equals("Docetaxel")){

             tp.addDrug("Docetaxel");
             tp.addType("Drug");
             tp.addTarget("tubulin");
             updated = true;
         }
         else if(drug.equals("Docetaxel + Trametinib")){

             tp.addDrug("Docetaxel");
             tp.addType("Drug");
             tp.addTarget("tubulin");

             tp.addDrug("Trametinib");
             tp.addType("Drug");
             tp.addTarget("MEK1 and Mek2");
             updated = true;
         }
         else if(drug.equals("Erlotinib")){

             tp.addDrug("Erlotinib");
             tp.addType("Drug");
             tp.addTarget("EGFR");
             updated = true;
         }
         else if(drug.equals("Avastin + Rapamycin")){

             tp.addDrug("Bevacizumab");
             tp.addType("Drug");
             tp.addTarget("VEGFA");

             tp.addDrug("Rapamycin");
             tp.addType("Drug");
             tp.addTarget("mTOR");
             updated = true;
         }
         else if(drug.equals("Carboplatin")){

             tp.addDrug("Carboplatin");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("CMC")){

             tp.addDrug("Carboxymethyl cellulose");
             tp.addType("Control");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Cisplatin")){

             tp.addDrug("Cisplatin");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Cisplatin + Etoposide")){

             tp.addDrug("Cisplatin");
             tp.addType("Drug");
             tp.addTarget("DNA");

             tp.addDrug("Etoposide");
             tp.addType("Drug");
             tp.addTarget("DNA topoisomerase 2-alpha");
             updated = true;
         }
         else if(drug.equals("Cisplatin + Gemcitabine")){

             tp.addDrug("Cisplatin");
             tp.addType("Drug");
             tp.addTarget("DNA");

             tp.addDrug("Gemcitabine");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Crizotinib")){

             tp.addDrug("Crizotinib");
             tp.addType("Drug");
             tp.addTarget("ALK");
             updated = true;
         }
         else if(drug.equals("Dabrafenib")){

             tp.addDrug("Crizotinib");
             tp.addType("Drug");
             tp.addTarget("BRAF");
             updated = true;
         }
         else if(drug.equals("Topotecan") || drug.equals("Etoposide")){

             tp.addDrug("Etoposide");
             tp.addType("Drug");
             tp.addTarget("DNA topoisomerase 2-alpha");
             updated = true;
         }
         else if(drug.equals("5-FU")){

             tp.addDrug("Fluorouracil");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Gemcitabine") || drug.equals("Cyclophosphamide")){

             tp.addDrug("Gemcitabine");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Gemcitabine + 5-FU")){

             tp.addDrug("Gemcitabine");
             tp.addType("Drug");
             tp.addTarget("DNA");

             tp.addDrug("Fluorouracil");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Oxaliplatin + 5-FU")){

             tp.addDrug("Oxaliplatin");
             tp.addType("Drug");
             tp.addTarget("DNA");

             tp.addDrug("Fluorouracil");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Oxaliplatin")){

             tp.addDrug("Oxaliplatin");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Rapamycin")){

             tp.addDrug("Rapamycin");
             tp.addType("Drug");
             tp.addTarget("mTOR");
             updated = true;
         }
         else if(drug.equals("Temozolomide")){

             tp.addDrug("Temozolomide");
             tp.addType("Drug");
             tp.addTarget("DNA");
             updated = true;
         }
         else if(drug.equals("Trametinib")){

             tp.addDrug("Trametinib");
             tp.addType("Drug");
             tp.addTarget("MEK1 and Mek2");
             updated = true;
         }
         else if(drug.equals("Valproic acid")){

             tp.addDrug("Valproic Acid");
             tp.addType("Drug");
             tp.addTarget("HDAC9");
             updated = true;
         }


        if(!updated){
            tp.addDrug("Unknown drug");
        }


         return tp;
     }

}
