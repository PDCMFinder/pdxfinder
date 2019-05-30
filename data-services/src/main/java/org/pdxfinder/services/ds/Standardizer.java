/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.services.ds;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.Drug;
import org.pdxfinder.graph.dao.Response;
import org.pdxfinder.graph.dao.TreatmentComponent;
import org.pdxfinder.graph.dao.TreatmentProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.spec.ECField;
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


    public static String getDrugName(String drug){

        drug = drug.toLowerCase();

        if(drug.toLowerCase().equals("doxorubicin".toLowerCase())){

            return "Doxorubicin";
        }
        else if(drug.toLowerCase().equals("cyclophosphamide".toLowerCase())){


            return "Cyclophosphamide";

        }
        else if(drug.toLowerCase().equals("paclitaxel".toLowerCase())){

            return "Paclitaxel";
        }
        else if(drug.toLowerCase().equals("tamoxifen".toLowerCase())){

            return "Tamoxifen";
        }
        else if(drug.toLowerCase().equals("trastuzumab".toLowerCase())){

            return "Trastuzumab";
        }
        else if(drug.equals("0.9% Solution of Sodium Chloride".toLowerCase()) || drug.equals("Saline".toLowerCase())){

            return "Sodium chloride solution";
        }
        else if(drug.equals("Erbitux, Cetuximab".toLowerCase())){

            return "Cetuximab";
        }
        else if(drug.equals("DSW (control)".toLowerCase())){

            return "Dextrose solution";
        }
        else if(drug.equals("D5W".toLowerCase())){

            return "Dextrose solution";
        }
        else if(drug.equals("CMC".toLowerCase())){

            return "Carboxymethyl cellulose";
        }
        else if(drug.equals("DMSO".toLowerCase())){

            return "Dimethyl sulfoxide";
        }
        else if(drug.equals("Docetaxel".toLowerCase())){

            return "Docetaxel";
        }
        else if(drug.equals("Trametinib".toLowerCase())){

            return "Trametinib";
        }
        else if(drug.equals("Erlotinib".toLowerCase())){

            return "Erlotinib";
        }
        else if(drug.toLowerCase().equals("avastin".toLowerCase())){

            return "Bevacizumab";
        }
        else if(drug.equals("Carboplatin".toLowerCase())){

            return "Carboplatin";
        }
        else if(drug.equals("CMC".toLowerCase())){

            return "Carboxymethyl cellulose";
        }
        else if(drug.equals("Cisplatin".toLowerCase())){

            return "Cisplatin";
        }
        else if(drug.equals("Etoposide".toLowerCase())){

            return "Etoposide";
        }
        else if(drug.equals("Gemcitabine".toLowerCase())){

            return "Gemcitabine";
        }
        else if(drug.equals("Crizotinib".toLowerCase())){

            return "Crizotinib";
        }
        else if(drug.equals("Dabrafenib".toLowerCase())){

            return "Dabrafenib";
        }
        else if(drug.equals("Etoposide".toLowerCase())){

            return "Etoposide";
        }
        else if(drug.equals("Topotecan".toLowerCase())){
            return drug;
        }
        else if(drug.equals("5-FU".toLowerCase()) || drug.equals("5-Fluorouracil".toLowerCase())){

            return "Fluorouracil";
        }

        else if(drug.equals("Oxaliplatin".toLowerCase())){

            return "Oxaliplatin";
        }
        else if(drug.equals("Rapamycin".toLowerCase())){

            return "Rapamycin";
        }
        else if(drug.equals("Temozolomide".toLowerCase())){

            return "Temozolomide";
        }
        else if(drug.equals("Trametinib".toLowerCase())){

            return "Trametinib";
        }
        else if(drug.equals("Valproic acid".toLowerCase())){

            return "Valproic Acid";
        }
        else if(drug.equals("Epirubicin".toLowerCase())){

            return "Epirubicin";
        }
        else if(drug.equals("Radiotherapy".toLowerCase())){

            return drug;
        }
        else if(drug.equals("Goserelin".toLowerCase())){

            return drug;
        }
        else if(drug.equals("Letrozole".toLowerCase())){

            return drug;
        }
        else if(drug.equals("Capecitabine".toLowerCase())){

            return drug;
        }
        else if(drug.equals("Denosumab".toLowerCase())){

            return drug;
        }

        else if(drug.equals("AZD1775".toLowerCase())){
            return "Adavosertib";
        }
        else if(drug.equals("MK1775".toLowerCase())){
            return "Adavosertib";
        }
        else if(drug.equals("MK-2206".toLowerCase())){
            return "Akt Inhibitor MK2206";
        }
        else if(drug.equals("Autologous Hematopoietic Stem Cell Transplantation".toLowerCase())){
            return "Autologous Hematopoietic Stem Cell Transplantation";
        }
        else if(drug.equals("Avastin".toLowerCase())){
            return "Avastin";
        }
        else if(drug.equals("5-Azacytidine".toLowerCase())){
            return "Azacitidine";
        }
        else if(drug.equals("BCG Vaccine".toLowerCase())){
            return "BCG Vaccine";
        }
        else if(drug.equals("BCG".toLowerCase())){
            return "BCG Vaccine";
        }
        else if(drug.equals("Bendamustine".toLowerCase())){
            return "Bendamustine";
        }
        else if(drug.equals("Bevacizumab".toLowerCase())){
            return "Bevacizumab";
        }
        else if(drug.equals("Bicalutamide".toLowerCase())){
            return "Bicalutamide";
        }
        else if(drug.equals("Biochemotherapy".toLowerCase())){
            return "Biochemotherapy";
        }
        else if(drug.equals("Cabazitaxel".toLowerCase())){
            return "Cabazitaxel";
        }
        else if(drug.equals("Cabozantinib".toLowerCase())){
            return "Cabozantinib";
        }
        else if(drug.equals("capecitabine".toLowerCase())){
            return "capecitabine";
        }
        else if(drug.equals("carboplatin".toLowerCase())){
            return "carboplatin";
        }
        else if(drug.equals("Celecoxib".toLowerCase())){
            return "Celecoxib";
        }
        else if(drug.equals("Cetuximab".toLowerCase())){
            return "Cetuximab";
        }
        else if(drug.equals("Undefined Chemoradiation".toLowerCase())){
            return "Chemoradiotherapy";
        }
        else if(drug.equals("Undefined Chemotherapy".toLowerCase())){
            return "Chemotherapy";
        }
        else if(drug.equals("chemotherapy ".toLowerCase())){
            return "chemotherapy ";
        }
        else if(drug.equals("chlorambucil".toLowerCase())){
            return "chlorambucil";
        }
        else if(drug.equals("Choline Kinase Alpha Inhibitor".toLowerCase())){
            return "Choline Kinase Alpha Inhibitor TCD-717";
        }
        else if(drug.equals("cisplatin".toLowerCase())){
            return "cisplatin";
        }
        else if(drug.equals("Cisplatin ".toLowerCase())){
            return "Cisplatin ";
        }
        else if(drug.equals("Undefined Clinical Trial".toLowerCase())){
            return "Clinical Trial";
        }
        else if(drug.equals("CAVATAK".toLowerCase())){
            return "Coxsackievirus A21";
        }
        else if(drug.equals("Cyclophosphamid ".toLowerCase())){
            return "Cyclophosphamid ";
        }
        else if(drug.equals("Cyclophosphamide".toLowerCase())){
            return "Cyclophosphamide";
        }
        else if(drug.equals("cytarabine".toLowerCase())){
            return "cytarabine";
        }
        else if(drug.equals("cytarabine ".toLowerCase())){
            return "cytarabine ";
        }
        else if(drug.equals("Dacarbazine".toLowerCase())){
            return "Dacarbazine";
        }
        else if(drug.equals("dacarbazine ".toLowerCase())){
            return "dacarbazine ";
        }
        else if(drug.equals("Dasatinib".toLowerCase())){
            return "Dasatinib";
        }
        else if(drug.equals("Decitabine".toLowerCase())){
            return "Decitabine";
        }
        else if(drug.equals("dexamethasone ".toLowerCase())){
            return "dexamethasone ";
        }
        else if(drug.equals("Docetaxel".toLowerCase())){
            return "Docetaxel";
        } else if (drug.equals("Doxorubicin Hcl Liposome Injection".toLowerCase())) {
            return "Docetaxel/Doxorubicin HCl Liposome";
        } else if (drug.equals("doxorubicin".toLowerCase())) {
            return "doxorubicin";
        } else if (drug.equals("doxorubicin ".toLowerCase())) {
            return "doxorubicin ";
        } else if (drug.equals("elmustine".toLowerCase())) {
            return "elmustine";
        } else if (drug.equals("epirubicin".toLowerCase())) {
            return "epirubicin";
        } else if (drug.equals("eribulin".toLowerCase())) {
            return "eribulin";
        }
        else if(drug.equals("erlotinib".toLowerCase())){
            return "erlotinib";
        }
        else if(drug.equals("etoposide".toLowerCase())){
            return "etoposide";
        }
        else if(drug.equals("everolimus".toLowerCase())){
            return "everolimus";
        }
        else if(drug.equals("exemestane".toLowerCase())){
            return "exemestane";
        }
        else if(drug.equals("external Beam Radiation Therapy".toLowerCase())){
            return "external Beam Radiation Therapy ";
        }
        else if(drug.equals("Fluorouracil".toLowerCase())){
            return "Fluorouracil";
        }
        else if(drug.equals("5-Fluorouracil".toLowerCase())){
            return "Fluorouracil";
        }
        else if(drug.equals("Flutamide".toLowerCase())){
            return "Flutamide";
        }
        else if(drug.equals("Folic acid ".toLowerCase())){
            return "Folic acid ";
        }
        else if(drug.equals("Folinic acid".toLowerCase())){
            return "Folinic acid";
        }
        else if(drug.equals("Leucovorin".toLowerCase()) || drug.equals("Leucovorin Calcium") ){
            return "Folinic acid";
        }
        else if(drug.equals("folinic acid".toLowerCase())){
            return "folinic acid";
        }
        else if(drug.equals("FU-LV regimen".toLowerCase())){
            return "FU-LV regimen";
        }
        else if(drug.equals("Fulvestrant".toLowerCase())){
            return "Fulvestrant";
        }
        else if(drug.equals("Gemcitabine".toLowerCase())){
            return "Gemcitabine";
        }
        else if(drug.equals("ifosfamide".toLowerCase())){
            return "ifosfamide";
        }
        else if(drug.equals("Imatinib".toLowerCase())){
            return "Imatinib";
        }
        else if(drug.equals("Indimitecan".toLowerCase())){
            return "Indimitecan";
        }
        else if(drug.equals("Indotecan".toLowerCase())){
            return "Indotecan";
        }
        else if(drug.equals("Interferon".toLowerCase())){
            return "Interferon";
        }
        else if(drug.equals("Interferon Alpha".toLowerCase())){
            return "Interferon Alpha";
        }
        else if(drug.equals("Interleukin-2".toLowerCase())){
            return "Interleukin-2";
        }
        else if(drug.equals("Brachytherapy".toLowerCase())){
            return "Internal Radiation Therapy";
        }
        else if(drug.equals("Ipilimumab".toLowerCase())){
            return "Ipilimumab";
        }
        else if(drug.equals("irinotecan".toLowerCase())){
            return "irinotecan";
        }
        else if(drug.equals("Irinotecan".toLowerCase())){
            return "Irinotecan";
        }
        else if(drug.equals("Lenalidomide".toLowerCase())){
            return "Lenalidomide";
        }
        else if(drug.equals("Letrozole".toLowerCase())){
            return "Letrozole";
        }
        else if(drug.equals("Leuprolide".toLowerCase())){
            return "Leuprolide";
        }
        else if(drug.equals("leuvocorin".toLowerCase())){
            return "leuvocorin";
        }
        else if(drug.equals("Mesna".toLowerCase())){
            return "Mesna";
        }
        else if(drug.equals("methotrexate".toLowerCase())){
            return "methotrexate";
        }
        else if(drug.equalsIgnoreCase("TRC102".toLowerCase())){
            return "Methoxyamine";
        }
        else if(drug.equalsIgnoreCase("Vinblastine".toLowerCase())){
            return "Vinblastine";
        }
        else if(drug.equals("Mitomycin C".toLowerCase())){
            return "Mitomycin";
        }
        else if(drug.equals("mitoxantrone".toLowerCase())){
            return "mitoxantrone";
        }
        else if(drug.equals("nab-Paclitaxel".toLowerCase())){
            return "Nab-paclitaxel";
        }
        else if(drug.equals("Nivolumab".toLowerCase())){
            return "Nivolumab";
        }
        else if(drug.equals("Obinutuzumab".toLowerCase())){
            return "Obinutuzumab";
        }
        else if(drug.equals("Onalespib".toLowerCase())){
            return "Onalespib";
        }
        else if(drug.equals("oxaliplatin".toLowerCase())){
            return "oxaliplatin";
        }
        else if(drug.equals("Oxaliplatin ".toLowerCase())){
            return "Oxaliplatin ";
        }
        else if(drug.equals("Paclitaxel".toLowerCase())){
            return "Paclitaxel";
        }
        else if(drug.equals("Panitumumab".toLowerCase())){
            return "Panitumumab";
        }
        else if(drug.equals("pazopanib".toLowerCase())){
            return "pazopanib";
        }
        else if(drug.equals("Pazopanib hydrochloride".toLowerCase())){
            return "Pazopanib Hydrochloride";
        }
        else if(drug.equals("Pemetrexed".toLowerCase())){
            return "Pemetrexed";
        }
        else if(drug.equals("pimasertib".toLowerCase())){
            return "pimasertib";
        }
        else if(drug.equals("Pixantrone".toLowerCase())){
            return "Pixantrone";
        }
        else if(drug.equals("Prednisolone".toLowerCase())){
            return "Prednisolone";
        }
        else if(drug.equals("prednisolone ".toLowerCase())){
            return "prednisolone ";
        }
        else if(drug.equals("prednisone".toLowerCase())){
            return "prednisone";
        }
        else if(drug.equals("Procarbazine".toLowerCase())){
            return "Procarbazine";
        }
        else if(drug.equals("Radiation".toLowerCase())){
            return "Radiation Therapy";
        }
        else if(drug.equals("Radiotherapy".toLowerCase())){
            return "Radiotherapy";
        }
        else if(drug.equals("Regorafenib".toLowerCase())){
            return "Regorafenib";
        }
        else if(drug.equals("Rituximab".toLowerCase())){
            return "Rituximab";
        }
        else if(drug.equals("rituximab ".toLowerCase())){
            return "rituximab ";
        }
        else if(drug.equals("Romidepsin".toLowerCase())){
            return "Romidepsin";
        }
        else if(drug.equals("Selumetinib".toLowerCase())){
            return "Selumetinib";
        }
        else if(drug.equals("PX-866".toLowerCase())){
            return "Sonolisib";
        }
        else if(drug.equals("sorafenib".toLowerCase())){
            return "sorafenib";
        }
        else if(drug.equals("SUNITINIB MALATE".toLowerCase())){
            return "Sunitinib Malate";
        }
        else if(drug.equals("BMN-673".toLowerCase())){
            return "Talazoparib";
        }
        else if(drug.equals("TVEC".toLowerCase())){
            return "Talimogene Laherparepvec";
        }
        else if(drug.equals("Taxol".toLowerCase())){
            return "Taxol";
        }
        else if(drug.equals("Temozolomide".toLowerCase())){
            return "Temozolomide";
        }
        else if(drug.equals("Thiotepa".toLowerCase())){
            return "Thiotepa";
        }
        else if(drug.equals("Tipiracil hydrochloride".toLowerCase())){
            return "Tipiracil Hydrochloride";
        }
        else if(drug.equals("Titanium silicate".toLowerCase())){
            return "Titanium silicate";
        }
        else if(drug.equals("Tivantinib".toLowerCase())){
            return "Tivantinib";
        }
        else if(drug.equals("Topotecan".toLowerCase())){
            return "Topotecan";
        }
        else if(drug.equals("Trametinib".toLowerCase())){
            return "Trametinib";
        }
        else if(drug.equals("Trastuzumab".toLowerCase())){
            return "Trastuzumab";
        }
        else if(drug.equals("Trifluridine".toLowerCase())){
            return "Trifluridine";
        }
        else if(drug.equals("Vemurafenib".toLowerCase())){
            return "Vemurafenib";
        }
        else if(drug.equals("Vincristin".toLowerCase())){
            return "Vincristin";
        }
        else if(drug.equals("vincristine".toLowerCase())){
            return "vincristine";
        }
        else if(drug.equals("vincristine".toLowerCase())){
            return "vincristine";
        }
        else if(drug.equals("Vindesin ".toLowerCase())){
            return "Vindesin ";
        }
        else if(drug.equals("Vinorelbine".toLowerCase())){
            return "Vinorelbine";
        }
        else if(drug.equals("Ziv-Aflibercept".toLowerCase())){
            return "Ziv-Aflibercept";
        }




        return "Not Specified";
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
