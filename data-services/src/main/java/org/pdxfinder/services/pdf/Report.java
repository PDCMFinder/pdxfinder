package org.pdxfinder.services.pdf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/*
 * Created by abayomi on 01/11/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Report {

    private List<Object> content;
    private Map<String, Style> styles;

    public Report() {
    }

    public List<Object> getContent() {
        return content;
    }

    public void setContent(List<Object> content) {
        this.content = content;
    }

    public Map<String, Style> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, Style> styles) {
        this.styles = styles;
    }
}
