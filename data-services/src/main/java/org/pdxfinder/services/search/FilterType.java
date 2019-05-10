package org.pdxfinder.services.search;

public enum FilterType {

    OneParamCheckboxFilter,
    OneParamTextFilter,
    TwoParamLinkedFilter,
    TwoParamUnlinkedFilter;

    public String get() {
        return name();
    }
}
