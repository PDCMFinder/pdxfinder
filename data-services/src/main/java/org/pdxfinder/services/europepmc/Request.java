package org.pdxfinder.services.europepmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "queryString",
        "resultType",
        "cursorMark",
        "pageSize",
        "sort",
        "synonym"
})
public class Request {

    private String queryString;
    private String resultType;
    private String cursorMark;
    private Integer pageSize;
    private String sort;
    private Boolean synonym;

    public String getQueryString() {
        return queryString;
    }

    public String getResultType() {
        return resultType;
    }

    public String getCursorMark() {
        return cursorMark;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public String getSort() {
        return sort;
    }

    public Boolean getSynonym() {
        return synonym;
    }
}