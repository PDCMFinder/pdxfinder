package org.pdxfinder.services.highchart;

/*
 * Created by abayomi on 19/06/2019.
 */
public enum SeriesType {

    SPLINE("spline"),
    COLUMN("column"),
    PIE("pie"),
    BAR("bar");

    private String value;

    private SeriesType(String val) {
        value = val;
    }

    public String get() {
        return value;
    }


}



