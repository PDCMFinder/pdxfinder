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

        if(drug.toLowerCase().equals("doxorubicin")){

            return "Doxorubicin";
        }
        else if(drug.toLowerCase().equals("cyclophosphamide")){


            return "Cyclophosphamide";

        }
        else if(drug.toLowerCase().equals("paclitaxel")){

            return "Paclitaxel";
        }
        else if(drug.toLowerCase().equals("tamoxifen")){

            return "Tamoxifen";
        }
        else if(drug.toLowerCase().equals("trastuzumab")){

            return "Trastuzumab";
        }
        else if(drug.equals("0.9% Solution of Sodium Chloride") || drug.equals("Saline")){

            return "Sodium chloride solution";
        }
        else if(drug.equals("Erbitux, Cetuximab")){

            return "Cetuximab";
        }
        else if(drug.equals("DSW (control)")){

            return "Dextrose solution";
        }
        else if(drug.equals("D5W")){

            return "Dextrose solution";
        }
        else if(drug.equals("CMC")){

            return "Carboxymethyl cellulose";
        }
        else if(drug.equals("DMSO")){

            return "Dimethyl sulfoxide";
        }
        else if(drug.equals("Docetaxel")){

            return "Docetaxel";
        }
        else if(drug.equals("Trametinib")){

            return "Trametinib";
        }
        else if(drug.equals("Erlotinib")){

            return "Erlotinib";
        }
        else if(drug.toLowerCase().equals("avastin")){

            return "Bevacizumab";
        }
        else if(drug.equals("Carboplatin")){

            return "Carboplatin";
        }
        else if(drug.equals("CMC")){

            return "Carboxymethyl cellulose";
        }
        else if(drug.equals("Cisplatin")){

            return "Cisplatin";
        }
        else if(drug.equals("Etoposide")){

            return "Etoposide";
        }
        else if(drug.equals("Gemcitabine")){

            return "Gemcitabine";
        }
        else if(drug.equals("Crizotinib")){

            return "Crizotinib";
        }
        else if(drug.equals("Dabrafenib")){

            return "Dabrafenib";
        }
        else if(drug.equals("Etoposide")){

            return "Etoposide";
        }
        else if(drug.equals("Topotecan")){
            return drug;
        }
        else if(drug.equals("5-FU") || drug.equals("5-Fluorouracil")){

            return "Fluorouracil";
        }

        else if(drug.equals("Oxaliplatin")){

            return "Oxaliplatin";
        }
        else if(drug.equals("Rapamycin")){

            return "Rapamycin";
        }
        else if(drug.equals("Temozolomide")){

            return "Temozolomide";
        }
        else if(drug.equals("Trametinib")){

            return "Trametinib";
        }
        else if(drug.equals("Valproic acid")){

            return "Valproic Acid";
        }
        else if(drug.equals("Epirubicin")){

            return "Epirubicin";
        }
        else if(drug.equals("Radiotherapy")){

            return drug;
        }
        else if(drug.equals("Goserelin")){

            return drug;
        }
        else if(drug.equals("Letrozole")){

            return drug;
        }
        else if(drug.equals("Capecitabine")){

            return drug;
        }
        else if(drug.equals("Denosumab")){

            return drug;
        }

        else if(drug.equals("AZD1775")){
            return "Adavosertib";
        }
        else if(drug.equals("MK1775")){
            return "Adavosertib";
        }
        else if(drug.equals("MK-2206")){
            return "Akt Inhibitor MK2206";
        }
        else if(drug.equals("Autologous Hematopoietic Stem Cell Transplantation")){
            return "Autologous Hematopoietic Stem Cell Transplantation";
        }
        else if(drug.equals("Avastin")){
            return "Avastin";
        }
        else if(drug.equals("5-Azacytidine")){
            return "Azacitidine";
        }
        else if(drug.equals("BCG Vaccine")){
            return "BCG Vaccine";
        }
        else if(drug.equals("BCG")){
            return "BCG Vaccine";
        }
        else if(drug.equals("Bendamustine")){
            return "Bendamustine";
        }
        else if(drug.equals("Bevacizumab")){
            return "Bevacizumab";
        }
        else if(drug.equals("Bicalutamide")){
            return "Bicalutamide";
        }
        else if(drug.equals("Biochemotherapy")){
            return "Biochemotherapy";
        }
        else if(drug.equals("Cabazitaxel")){
            return "Cabazitaxel";
        }
        else if(drug.equals("Cabozantinib")){
            return "Cabozantinib";
        }
        else if(drug.equals("capecitabine")){
            return "capecitabine";
        }
        else if(drug.equals("carboplatin")){
            return "carboplatin";
        }
        else if(drug.equals("Celecoxib")){
            return "Celecoxib";
        }
        else if(drug.equals("Cetuximab")){
            return "Cetuximab";
        }
        else if(drug.equals("Undefined Chemoradiation")){
            return "Chemoradiotherapy";
        }
        else if(drug.equals("Undefined Chemotherapy")){
            return "Chemotherapy";
        }
        else if(drug.equals("chemotherapy ")){
            return "chemotherapy ";
        }
        else if(drug.equals("chlorambucil")){
            return "chlorambucil";
        }
        else if(drug.equals("Choline Kinase Alpha Inhibitor")){
            return "Choline Kinase Alpha Inhibitor TCD-717";
        }
        else if(drug.equals("cisplatin")){
            return "cisplatin";
        }
        else if(drug.equals("Cisplatin ")){
            return "Cisplatin ";
        }
        else if(drug.equals("Undefined Clinical Trial")){
            return "Clinical Trial";
        }
        else if(drug.equals("CAVATAK")){
            return "Coxsackievirus A21";
        }
        else if(drug.equals("Cyclophosphamid ")){
            return "Cyclophosphamid ";
        }
        else if(drug.equals("Cyclophosphamide")){
            return "Cyclophosphamide";
        }
        else if(drug.equals("cytarabine")){
            return "cytarabine";
        }
        else if(drug.equals("cytarabine ")){
            return "cytarabine ";
        }
        else if(drug.equals("Dacarbazine")){
            return "Dacarbazine";
        }
        else if(drug.equals("dacarbazine ")){
            return "dacarbazine ";
        }
        else if(drug.equals("Dasatinib")){
            return "Dasatinib";
        }
        else if(drug.equals("Decitabine")){
            return "Decitabine";
        }
        else if(drug.equals("dexamethasone ")){
            return "dexamethasone ";
        }
        else if(drug.equals("Docetaxel")){
            return "Docetaxel";
        }
        else if(drug.equals("Doxorubicin Hcl Liposome Injection")){
            return "Docetaxel/Doxorubicin HCl Liposome";
        }
        else if(drug.equals("doxorubicin")){
            return "doxorubicin";
        }
        else if(drug.equals("doxorubicin ")){
            return "doxorubicin ";
        }
        else if(drug.equals("elmustine")){
        return "elmustine";
    }
else if(drug.equals("epirubicin")){
            return "epirubicin";
}
else if(drug.equals("eribulin")){
        return "eribulin";
        }
        else if(drug.equals("erlotinib")){
        return "erlotinib";
        }
        else if(drug.equals("etoposide")){
        return "etoposide";
        }
        else if(drug.equals("everolimus")){
        return "everolimus";
        }
        else if(drug.equals("exemestane")){
        return "exemestane";
        }
        else if(drug.equals("external Beam Radiation Therapy")){
        return "external Beam Radiation Therapy ";
        }
        else if(drug.equals("Fluorouracil")){
        return "Fluorouracil";
        }
        else if(drug.equals("5-Fluorouracil")){
        return "Fluorouracil";
        }
        else if(drug.equals("Flutamide")){
        return "Flutamide";
        }
        else if(drug.equals("Folic acid ")){
        return "Folic acid ";
        }
        else if(drug.equals("Folinic acid")){
        return "Folinic acid";
        }
        else if(drug.equals("Leucovorin")){
        return "Folinic acid";
        }
        else if(drug.equals("folinic acid")){
        return "folinic acid";
        }
        else if(drug.equals("FU-LV regimen")){
        return "FU-LV regimen";
        }
        else if(drug.equals("Fulvestrant")){
        return "Fulvestrant";
        }
        else if(drug.equals("Gemcitabine")){
        return "Gemcitabine";
        }
        else if(drug.equals("ifosfamide")){
        return "ifosfamide";
        }
        else if(drug.equals("Imatinib")){
        return "Imatinib";
        }
        else if(drug.equals("Indimitecan")){
        return "Indimitecan";
        }
        else if(drug.equals("Indotecan")){
        return "Indotecan";
        }
        else if(drug.equals("Interferon")){
        return "Interferon";
        }
        else if(drug.equals("Interferon Alpha")){
        return "Interferon Alpha";
        }
        else if(drug.equals("Interleukin-2")){
        return "Interleukin-2";
        }
        else if(drug.equals("Brachytherapy")){
        return "Internal Radiation Therapy";
        }
        else if(drug.equals("Ipilimumab")){
        return "Ipilimumab";
        }
        else if(drug.equals("irinotecan")){
        return "irinotecan";
        }
        else if(drug.equals("Irinotecan")){
        return "Irinotecan";
        }
        else if(drug.equals("Lenalidomide")){
        return "Lenalidomide";
        }
        else if(drug.equals("Letrozole")){
        return "Letrozole";
        }
        else if(drug.equals("Leuprolide")){
        return "Leuprolide";
        }
        else if(drug.equals("leuvocorin")){
        return "leuvocorin";
        }
        else if(drug.equals("Mesna")){
        return "Mesna";
        }
        else if(drug.equals("methotrexate")){
        return "methotrexate";
        }
        else if(drug.equals("TRC102")){
        return "Methoxyamine";
        }
        else if(drug.equals("Mitomycin C")){
        return "Mitomycin";
        }
        else if(drug.equals("mitoxantrone")){
        return "mitoxantrone";
        }
        else if(drug.equals("nab-Paclitaxel")){
        return "Nab-paclitaxel";
        }
        else if(drug.equals("Nivolumab")){
        return "Nivolumab";
        }
        else if(drug.equals("Obinutuzumab")){
        return "Obinutuzumab";
        }
        else if(drug.equals("Onalespib")){
        return "Onalespib";
        }
        else if(drug.equals("oxaliplatin")){
        return "oxaliplatin";
        }
        else if(drug.equals("Oxaliplatin ")){
        return "Oxaliplatin ";
        }
        else if(drug.equals("Paclitaxel")){
        return "Paclitaxel";
        }
        else if(drug.equals("Panitumumab")){
        return "Panitumumab";
        }
        else if(drug.equals("pazopanib")){
        return "pazopanib";
        }
        else if(drug.equals("Pazopanib hydrochloride")){
        return "Pazopanib Hydrochloride";
        }
        else if(drug.equals("Pemetrexed")){
        return "Pemetrexed";
        }
        else if(drug.equals("pimasertib")){
        return "pimasertib";
        }
        else if(drug.equals("Pixantrone")){
        return "Pixantrone";
        }
        else if(drug.equals("Prednisolone")){
        return "Prednisolone";
        }
        else if(drug.equals("prednisolone ")){
        return "prednisolone ";
        }
        else if(drug.equals("prednisone")){
        return "prednisone";
        }
        else if(drug.equals("Procarbazine")){
        return "Procarbazine";
        }
        else if(drug.equals("Radiation")){
        return "Radiation Therapy";
        }
        else if(drug.equals("Radiotherapy")){
        return "Radiotherapy";
        }
        else if(drug.equals("Regorafenib")){
        return "Regorafenib";
        }
        else if(drug.equals("Rituximab")){
        return "Rituximab";
        }
        else if(drug.equals("rituximab ")){
        return "rituximab ";
        }
        else if(drug.equals("Romidepsin")){
        return "Romidepsin";
        }
        else if(drug.equals("Selumetinib")){
        return "Selumetinib";
        }
        else if(drug.equals("PX-866")){
        return "Sonolisib";
        }
        else if(drug.equals("sorafenib")){
        return "sorafenib";
        }
        else if(drug.equals("SUNITINIB MALATE")){
        return "Sunitinib Malate";
        }
        else if(drug.equals("BMN-673")){
        return "Talazoparib";
        }
        else if(drug.equals("TVEC")){
        return "Talimogene Laherparepvec";
        }
        else if(drug.equals("Taxol")){
        return "Taxol";
        }
        else if(drug.equals("Temozolomide")){
        return "Temozolomide";
        }
        else if(drug.equals("Thiotepa")){
        return "Thiotepa";
        }
        else if(drug.equals("Tipiracil hydrochloride")){
        return "Tipiracil Hydrochloride";
        }
        else if(drug.equals("Titanium silicate")){
        return "Titanium silicate";
        }
        else if(drug.equals("Tivantinib")){
        return "Tivantinib";
        }
        else if(drug.equals("Topotecan")){
        return "Topotecan";
        }
        else if(drug.equals("Trametinib")){
        return "Trametinib";
        }
        else if(drug.equals("Trastuzumab")){
        return "Trastuzumab";
        }
        else if(drug.equals("Trifluridine")){
        return "Trifluridine";
        }
        else if(drug.equals("Vemurafenib")){
        return "Vemurafenib";
        }
        else if(drug.equals("Vincristin")){
        return "Vincristin";
        }
        else if(drug.equals("vincristine")){
        return "vincristine";
        }
        else if(drug.equals("vincristine")){
        return "vincristine";
        }
        else if(drug.equals("Vindesin ")){
        return "Vindesin ";
        }
        else if(drug.equals("Vinorelbine")){
        return "Vinorelbine";
        }
        else if(drug.equals("Ziv-Aflibercept")){
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
