package org.pdxfinder.services.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/*
 * Created by abayomi on 01/11/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Lists {

    private String style;
    private String type;
    private List<Text> ul;

    public Lists() {
    }

    public Lists(String style, String type, List<Text> ul) {
        this.style = style;
        this.type = type;
        this.ul = ul;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Text> getUl() {
        return ul;
    }

    public void setUl(List<Text> ul) {
        this.ul = ul;
    }
}
