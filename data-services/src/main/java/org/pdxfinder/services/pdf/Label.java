package org.pdxfinder.services.pdf;

import java.util.Arrays;
import java.util.List;

/*
 * Created by abayomi on 31/10/2018.
 */
public enum Label {

    PDX_MODEL("PDX MODEL"),
    PDF("Pdf");


    //STYLES
    public static String STYLE_BODY_TEXT3 = "bodyText3";
    public static String STYLE_TABLE_H3 = "tableH3";
    public static String STYLE_TD = "tdStyle";
    public static String STYLE_TABLE2 = "tableExample2";

    //COLORS
    public static String COLOR_PDX_SECONDARY = "#00b2d5";


    //TEXT LABELS
    public static String DATA_PROVIDER = "Data Provider";
    public static String INFO = "PDX MODEL INFO";
    public static String JAX = "Jackson Laboratory";
    public static String JAX_URL = "http://www.jax.org";
    public static String SUBMIT = "Submit Models";
    public static String HELP_DESK = "Submit Models";
    public static String HELP_DESK_MAIL = "helpdesk@pdxfnder.org";
    public static String SUBMISSION_MAIL = "submissions@pdxfnder.org";
    public static String LINKS = "Important Links";
    public static String PDX_LABEL = "View on PDX Finder";
    public static String JAX_LABEL = "View on JAX";
    public static String CONTACT_PROVIDER = "Contact Provider for this PDX Model";
    public static String PDX_DEV_INFO = "EMBL-EBI and The Jackson Laboratory are co-developers of PDX Finder. This work is supported by the National Institutes of Health/National Cancer Institute U24 CA204781 01 and R01 CA089713.";
    public static String PDX_FINDER_URL = "http://www.pdxfinder.org";
    public static String WEBSITE = "www.pdxfinder.org";

    public static String TYPE_SQUARE = "square";

    public static String NULL = null;
    public static Boolean TRUE = true;
    public static Boolean FALSE = false;


    public static String TXT_PATIENT = "PATIENT";
    public static String TXT_PATIENT_TUMOR = "PATIENT TUMOR";
    public static String TXT_ENGRAFTMENT = "PDX MODEL ENGRAFTMENT ";
    public static List<String> TXT_ENGRAFTMENT_TABLE_HEAD = Arrays.asList("HOST STRAIN NAME", "SITE", "TYPE", "MATERIAL", "MATERIAL STATUS", "PASSAGE");
    public static String TXT_QC = "MODEL QUALITY CONTROL";
    public static String TXT_PATIENT_COLLECTION = "PATIENT TUMOR COLLECTION FOR PDX MODEL";
    public static List<String> TXT_QC_TABLE_HEAD = Arrays.asList("TECHNIQUE", "DESCRIPTION", "PASSAGE");


    // TEXT DESCRIPTIONS
    public static String TXT_SEX = "Sex";
    public static String TXT_COLLECTION_AGE = "Age at Collection Time";
    public static String TXT_RACE = "Race / Ethnicity";


    public static String TXT_HISTOLOGY = "Histology";
    public static String TXT_TISSUE = "Primary Tissue";
    public static String TXT_SITE = "Collection Site";
    public static String TXT_TUMOR = "Tumor Type";
    public static String TXT_GRADE = "Grade";
    public static String TXT_STAGE = "Stage";

    public static String TXT_AGE = "Age";
    public static String TXT_DIAGNOSIS = "Diagnosis";
    public static String TXT_MOUSE = "Pdx Mouse";
    public static String TXT_TUMOR_TYPE = "Type";




    private String value;

    private Label(String val) {
        value = val;
    }

    public String val() {
        return value;
    }

}

