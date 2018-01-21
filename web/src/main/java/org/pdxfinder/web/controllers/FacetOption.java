package org.pdxfinder.web.controllers;

import java.util.Objects;

public class FacetOption implements Comparable {

    String name;
    Integer count;

    public FacetOption(String name, Integer count) {
        this.name = name;
        this.count = count;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacetOption that = (FacetOption) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }

    @Override
    public int compareTo(Object o) {
        // TODO: Sort "Child" to top of list
        return this.name.compareTo(((FacetOption) o).getName());
    }
}
