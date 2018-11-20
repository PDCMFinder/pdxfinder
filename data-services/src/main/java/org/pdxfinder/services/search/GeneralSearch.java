package org.pdxfinder.services.search;

/*
 * Created by csaba on 20/11/2018.
 */
public abstract class GeneralSearch {

    private String name;
    private String urlParam;

    public GeneralSearch(String name, String urlParam) {
        this.name = name;
        this.urlParam = urlParam;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(String urlParam) {
        this.urlParam = urlParam;
    }
}
