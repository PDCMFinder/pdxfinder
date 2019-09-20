package org.pdxfinder.services.highchart;


/*
 * Created by abayomi on 28/06/2019.
 */
public enum ChartStrings {

    HTML_HEAD_FORMAT("<span style='font-size:10px'>{point.key}</span><table>"),
    HTML_POINT_FORMAT("<tr><td style='color:{series.color};padding:0'>{series.name}: </td> <td style='padding:0'><b>{point.y:.1f}</b></td></tr>"),
    HTML_FOOTER_FORMAT("</table>");

    private String value;

    private ChartStrings(String val) {
        value = val;
    }

    public String get() {
        return value;
    }


}

