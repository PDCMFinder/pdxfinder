package org.pdxfinder.services.mapping;

/*
 * Created by csaba on 04/07/2019.
 */
public enum MappingEntityType {

    diagnosis,
    treatment;

    public String get() {
        return name();
    }
}
