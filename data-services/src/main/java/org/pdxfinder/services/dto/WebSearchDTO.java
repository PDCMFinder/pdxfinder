package org.pdxfinder.services.dto;

import org.pdxfinder.services.ds.AutoCompleteOption;
import org.pdxfinder.services.ds.FacetOption;
import org.pdxfinder.services.ds.ModelForQuery;

import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Created by csaba on 30/04/2018.
 */
public class WebSearchDTO {

    List<FacetOption> patientAgeSelected;
    List<FacetOption> patientGenderSelected;
    List<FacetOption> datasourceSelected;
    List<FacetOption> cancerSystemSelected;
    List<FacetOption> sampleTumorTypeSelected;
    List<FacetOption> mutationSelected;

    private String facetString;
    private List<ModelForQuery> searchResults;
    private String textSearchDescription;
    private List<AutoCompleteOption> autoCompleteOptions;
    Map<String, String> platformsAndUrls;

    public WebSearchDTO() {
    }


    public List<ModelForQuery> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<ModelForQuery> searchResults) {
        this.searchResults = searchResults;
    }

    public String getFacetString() {
        return facetString;
    }

    public void setFacetString(String facetString) {
        this.facetString = facetString;
    }

    public String getTextSearchDescription() {
        return textSearchDescription;
    }

    public void setTextSearchDescription(String textSearchDescription) {
        this.textSearchDescription = textSearchDescription;
    }

    public List<AutoCompleteOption> getAutoCompleteOptions() {
        return autoCompleteOptions;
    }

    public void setAutoCompleteOptions(List<AutoCompleteOption> autoCompleteOptions) {
        this.autoCompleteOptions = autoCompleteOptions;
    }

    public Map<String, String> getPlatformsAndUrls() {
        return platformsAndUrls;
    }

    public void setPlatformsAndUrls(Map<String, String> platformsAndUrls) {
        this.platformsAndUrls = platformsAndUrls;
    }

    public List<FacetOption> getPatientAgeSelected() {
        return patientAgeSelected;
    }

    public void setPatientAgeSelected(List<FacetOption> patientAgeSelected) {
        this.patientAgeSelected = patientAgeSelected;
    }

    public List<FacetOption> getPatientGenderSelected() {
        return patientGenderSelected;
    }

    public void setPatientGenderSelected(List<FacetOption> patientGenderSelected) {
        this.patientGenderSelected = patientGenderSelected;
    }

    public List<FacetOption> getDatasourceSelected() {
        return datasourceSelected;
    }

    public void setDatasourceSelected(List<FacetOption> datasourceSelected) {
        this.datasourceSelected = datasourceSelected;
    }

    public List<FacetOption> getCancerSystemSelected() {
        return cancerSystemSelected;
    }

    public void setCancerSystemSelected(List<FacetOption> cancerSystemSelected) {
        this.cancerSystemSelected = cancerSystemSelected;
    }

    public List<FacetOption> getSampleTumorTypeSelected() {
        return sampleTumorTypeSelected;
    }

    public void setSampleTumorTypeSelected(List<FacetOption> sampleTumorTypeSelected) {
        this.sampleTumorTypeSelected = sampleTumorTypeSelected;
    }

    public List<FacetOption> getMutationSelected() {
        return mutationSelected;
    }

    public void setMutationSelected(List<FacetOption> mutationSelected) {
        this.mutationSelected = mutationSelected;
    }
}
