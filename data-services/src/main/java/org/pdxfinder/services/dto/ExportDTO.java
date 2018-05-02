package org.pdxfinder.services.dto;

import org.pdxfinder.services.ds.ModelForQuery;

import java.util.Set;

/*
 * Created by csaba on 02/05/2018.
 */
public class ExportDTO {

    Set<ModelForQuery> results;
    String facetsString;

    public ExportDTO() {
    }


    public Set<ModelForQuery> getResults() {
        return results;
    }

    public void setResults(Set<ModelForQuery> results) {
        this.results = results;
    }

    public String getFacetsString() {
        return facetsString;
    }

    public void setFacetsString(String facetsString) {
        this.facetsString = facetsString;
    }
}
