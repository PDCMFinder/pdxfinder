package org.pdxfinder.services.ds;

/*
 * Created by csaba on 05/02/2018.
 */
public class AutoSuggestOption implements Comparable<AutoSuggestOption> {


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

    @Override
    public int compareTo(AutoSuggestOption o) {
        return this.value.compareTo(o.value);
    }
}
