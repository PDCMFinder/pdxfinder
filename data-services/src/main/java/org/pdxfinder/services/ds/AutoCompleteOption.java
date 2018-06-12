package org.pdxfinder.services.ds;

/*
 * Created by csaba on 05/02/2018.
 */
public class AutoCompleteOption implements Comparable<AutoCompleteOption> {


    private String value;
    private String context;

    public AutoCompleteOption(String value, String context) {
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
    public int compareTo(AutoCompleteOption o) {
        return this.value.compareTo(o.value);
    }
}
