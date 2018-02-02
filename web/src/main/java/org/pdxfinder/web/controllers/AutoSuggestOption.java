package org.pdxfinder.web.controllers;

/*
 * Created by csaba on 02/02/2018.
 */
public class AutoSuggestOption {

    private String value;
    private String context;

    public AutoSuggestOption(String value, String context) {
        this.value = value;
        this.context = context;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
