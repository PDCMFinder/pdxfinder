package org.pdxfinder.services.mapping;

/*
 * Created by abayomi on 06/08/2019.
 */
public enum Status {

    unmapped,
    created,
    validated,
    unvalidated;

    public String get() {
        return name();
    }
}
