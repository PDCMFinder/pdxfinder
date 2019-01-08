package org.pdxfinder.services.search;

/*
 * Created by csaba on 19/11/2018.
 */
public abstract class GeneralFilter {



    public String name;

    public String urlParam;

    public Boolean isActive;

    public String type;

    public GeneralFilter(String name, String urlParam, Boolean isActive, String type) {
        this.name = name;
        this.urlParam = urlParam;
        this.isActive = isActive;
        this.type = type;
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

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
