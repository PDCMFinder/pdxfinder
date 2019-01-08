package org.pdxfinder.services.search;

public enum FilterType {

    OneParamFilter,
    TwoParamLinkedFilter,
    TwoParamUnlinkedFilter;

    public String get() {
        return name();
    }
}
