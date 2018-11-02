package org.pdxfinder.services.pdf;

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


    //TEXT LABELS
    public static String DATA_PROVIDER = "Data Provider";
    public static String INFO = "PDX FINDER INFO";
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


    private String value;

    private Label(String val) {
        value = val;
    }

    public String val() {
        return value;
    }

}

