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


    private int numPages;
    private int beginIndex;
    private int endIndex;
    private int currentIndex;
    private int totalResults;
    private Integer page;
    private Integer size;
    private boolean dataAvailableColumnPresent;
    private boolean isMutationSelected;
    private boolean isDrugSelected;


    private String query;

    private List<FacetOption> patientAgeSelected;
    private List<FacetOption> patientGenderSelected;
    private List<FacetOption> datasourceSelected;
    private List<FacetOption> cancerSystemSelected;
    private List<FacetOption> sampleTumorTypeSelected;
    private List<FacetOption> mutationSelected;
    private List<FacetOption> drugSelected;
    private List<FacetOption> projectSelected;

    private String facetString;
    private List<ModelForQuery> searchResults;
    private String textSearchDescription;
    private List<AutoCompleteOption> autoCompleteOptions;
    private Map<String, String> platformsAndUrls;

    private String mutatedMarkersAndVariants;

    private Map<String, List<String>> platformMap;
    private Map<String, List<String>> mutationMap;
    private List<FacetOption> diagnosisSelected;
    private Map<String, List<String>> facetOptions;
    private Map<String, List<String>> markerMap;
    private Map<String, Set<String>> markerMapWithAllVariants;
    private Map<String, List<String>> drugMap;
    private Map<String, Set<String>> drugMapWithAllResponses;

    private List<String> drugNames;
    private List<String> drugResponses;


    public WebSearchDTO() {
    }


    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public int getBeginIndex() {
        return beginIndex;
    }

    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public boolean getIsMutationSelected() {
        return isMutationSelected;
    }

    public void setIsMutationSelected(boolean isMutationSelected) {
        this.isMutationSelected = isMutationSelected;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public List<FacetOption> getProjectSelected() {
        return projectSelected;
    }

    public void setProjectSelected(List<FacetOption> projectSelected) {
        this.projectSelected = projectSelected;
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

    public String getFacetString() {
        return facetString;
    }

    public void setFacetString(String facetString) {
        this.facetString = facetString;
    }

    public List<ModelForQuery> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<ModelForQuery> searchResults) {
        this.searchResults = searchResults;
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

    public String getMutatedMarkersAndVariants() {
        return mutatedMarkersAndVariants;
    }

    public void setMutatedMarkersAndVariants(String mutatedMarkers) {
        this.mutatedMarkersAndVariants = mutatedMarkers;
    }

    public Map<String, List<String>> getPlatformMap() {
        return platformMap;
    }

    public void setPlatformMap(Map<String, List<String>> platformMap) {
        this.platformMap = platformMap;
    }

    public Map<String, List<String>> getMutationMap() {
        return mutationMap;
    }

    public void setMutationMap(Map<String, List<String>> mutationMap) {
        this.mutationMap = mutationMap;
    }

    public void setDiagnosisSelected(List<FacetOption> diagnosisSelected) {
        this.diagnosisSelected = diagnosisSelected;
    }

    public List<FacetOption> getDiagnosisSelected() {
        return diagnosisSelected;
    }

    public void setFacetOptions(Map<String, List<String>> facetOptions) {
        this.facetOptions = facetOptions;
    }

    public Map<String, List<String>> getFacetOptions() {
        return facetOptions;
    }

    public Map<String, List<String>> getMarkerMap() {
        return markerMap;
    }

    public void setMarkerMap(Map<String, List<String>> markerMap) {
        this.markerMap = markerMap;
    }

    public Map<String, Set<String>> getMarkerMapWithAllVariants() {
        return markerMapWithAllVariants;
    }

    public void setMarkerMapWithAllVariants(Map<String, Set<String>> markerMapWithAllVariants) {
        this.markerMapWithAllVariants = markerMapWithAllVariants;
    }

    public Map<String, List<String>> getDrugMap() {
        return drugMap;
    }

    public void setDrugMap(Map<String, List<String>> drugMap) {
        this.drugMap = drugMap;
    }

    public Map<String, Set<String>> getDrugMapWithAllResponses() {
        return drugMapWithAllResponses;
    }

    public void setDrugMapWithAllResponses(Map<String, Set<String>> drugMapWithAllResponses) {
        this.drugMapWithAllResponses = drugMapWithAllResponses;
    }

    public boolean isMutationSelected() {
        return isMutationSelected;
    }

    public void setMutationSelected(boolean mutationSelected) {
        isMutationSelected = mutationSelected;
    }

    public List<String> getDrugNames() {
        return drugNames;
    }

    public void setDrugNames(List<String> drugNames) {
        this.drugNames = drugNames;
    }

    public List<String> getDrugResponses() {
        return drugResponses;
    }

    public void setDrugResponses(List<String> drugResponses) {
        this.drugResponses = drugResponses;
    }

    public List<FacetOption> getDrugSelected() {
        return drugSelected;
    }

    public void setDrugSelected(List<FacetOption> drugSelected) {
        this.drugSelected = drugSelected;
    }

    public boolean getIsDrugSelected() {
        return isDrugSelected;
    }

    public void setIsDrugSelected(boolean drugSelected) {
        isDrugSelected = drugSelected;
    }

    public boolean isDataAvailableColumnPresent() {
        return dataAvailableColumnPresent;
    }

    public void setDataAvailableColumnPresent(boolean dataAvailablePresent) {
        dataAvailableColumnPresent = dataAvailablePresent;
    }
}
