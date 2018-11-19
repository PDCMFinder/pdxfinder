package org.pdxfinder.services.search;

/*
 * Created by csaba on 19/11/2018.
 */
public abstract class GeneralFilter {



    public String name;

    public GeneralFilter(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
