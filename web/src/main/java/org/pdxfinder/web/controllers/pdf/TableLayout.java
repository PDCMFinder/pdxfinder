package org.pdxfinder.web.controllers.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/*
 * Created by abayomi on 29/10/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableLayout {

    private Boolean defaultBorder;
    private String vLineColor;
    private String hLineColor;

    public TableLayout() {
    }

    public TableLayout(Boolean defaultBorder, String vLineColor, String hLineColor) {
        this.defaultBorder = defaultBorder;
        this.vLineColor = vLineColor;
        this.hLineColor = hLineColor;
    }

    public Boolean getDefaultBorder() {
        return defaultBorder;
    }

    public void setDefaultBorder(Boolean defaultBorder) {
        this.defaultBorder = defaultBorder;
    }

    public String getvLineColor() {
        return vLineColor;
    }

    public void setvLineColor(String vLineColor) {
        this.vLineColor = vLineColor;
    }

    public String gethLineColor() {
        return hLineColor;
    }

    public void sethLineColor(String hLineColor) {
        this.hLineColor = hLineColor;
    }
}

