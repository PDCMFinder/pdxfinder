package org.pdxfinder.services.dto;

import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.search.WebFacetContainer;

import java.util.List;

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

    private String facetString;
    private String textSearchDescription;

    private String query;

    //the facets menu with their possible options, including what options are selected
    private WebFacetContainer webFacetsContainer;

    private List<String> mainSearchFieldOptions;

    private List<ModelForQuery> results;


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

    public boolean isDataAvailableColumnPresent() {
        return dataAvailableColumnPresent;
    }

    public void setDataAvailableColumnPresent(boolean dataAvailableColumnPresent) {
        this.dataAvailableColumnPresent = dataAvailableColumnPresent;
    }

    public boolean isMutationSelected() {
        return isMutationSelected;
    }

    public void setMutationSelected(boolean mutationSelected) {
        isMutationSelected = mutationSelected;
    }

    public boolean isDrugSelected() {
        return isDrugSelected;
    }

    public void setDrugSelected(boolean drugSelected) {
        isDrugSelected = drugSelected;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public WebFacetContainer getWebFacetsContainer() {
        return webFacetsContainer;
    }

    public void setWebFacetsContainer(WebFacetContainer webFacetsContainer) {
        this.webFacetsContainer = webFacetsContainer;
    }

    public List<ModelForQuery> getResults() {
        return results;
    }

    public void setResults(List<ModelForQuery> results) {
        this.results = results;
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

    public List<String> getMainSearchFieldOptions() {
        return mainSearchFieldOptions;
    }

    public void setMainSearchFieldOptions(List<String> mainSearchFieldOptions) {
        this.mainSearchFieldOptions = mainSearchFieldOptions;
    }
}
