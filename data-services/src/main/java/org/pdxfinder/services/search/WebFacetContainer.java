package org.pdxfinder.services.search;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by csaba on 23/11/2018.
 */
public class WebFacetContainer {


    private List<WebFacetSection> webFacetSections;


    public WebFacetContainer() {
    }


    public List<WebFacetSection> getWebFacetSections() {
        return webFacetSections;
    }

    public void setWebFacetSections(List<WebFacetSection> webFacetSections) {
        this.webFacetSections = webFacetSections;
    }

    public void addSection(WebFacetSection dto){

        if(this.webFacetSections == null){
            this.webFacetSections = new ArrayList<>();
        }
        this.webFacetSections.add(dto);

    }
}
