package org.pdxfinder.services.ds;

import java.util.Objects;

public class FacetOption implements Comparable {

    String name;
    Integer count;
    Boolean selected;
    SearchFacetName facetType;

    public FacetOption(String name, Integer count) {
        this.name = name;
        this.count = count;
    }

    public FacetOption(String name, Integer count, Boolean selected) {
        this.name = name;
        this.count = count;
        this.selected = selected;
    }

    public FacetOption(String name, Integer count, Boolean selected, SearchFacetName facetType) {
        this.name = name;
        this.count = count;
        this.selected = selected;
        this.facetType = facetType;
    }


    public void increment() {
        this.count += 1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }

    public SearchFacetName getFacetType() {
        return facetType;
    }

    public void setFacetType(SearchFacetName facetType) {
        this.facetType = facetType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacetOption that = (FacetOption) o;
        return Objects.equals(name, that.name) &&
                facetType == that.facetType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, facetType);
    }

    @Override
    public int compareTo(Object o) {
        return this.name.compareTo(((FacetOption) o).getName());
    }
}
