package org.pdxfinder.services.search;

import org.pdxfinder.services.search.GeneralFilter;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by csaba on 23/11/2018.
 */
public class WebFacetSection {


    private String name;

    private List<GeneralFilter> filterComponents;

    public WebFacetSection(String name, List<GeneralFilter> filterComponents) {
        this.name = name;
        this.filterComponents = filterComponents;
    }

    public WebFacetSection() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<GeneralFilter> getFilterComponents() {
        return filterComponents;
    }

    public void setFilterComponents(List<GeneralFilter> filterComponents) {
        this.filterComponents = filterComponents;
    }

    public void addComponent(GeneralFilter filter){

        if(this.filterComponents == null){
            this.filterComponents = new ArrayList<>();
        }
        this.filterComponents.add(filter);
    }
}
