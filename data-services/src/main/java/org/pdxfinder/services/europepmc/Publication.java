package org.pdxfinder.services.europepmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version",
        "hitCount",
        "nextCursorMark",
        "request",
        "resultList"
})
public class Publication {

    private String version;
    private Integer hitCount;
    private String nextCursorMark;
    private Request request;
    private Map<String, List<Result>> resultList;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getHitCount() {
        return hitCount;
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = hitCount;
    }

    public String getNextCursorMark() {
        return nextCursorMark;
    }

    public void setNextCursorMark(String nextCursorMark) {
        this.nextCursorMark = nextCursorMark;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Map<String, List<Result>> getResultList() {
        return resultList;
    }

    public void setResultList(Map<String, List<Result>> resultList) {
        this.resultList = resultList;
    }
}