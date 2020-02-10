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

    private Map<String, List<Result>> resultList;

    public Publication() {
    }

    public Publication(Map<String, List<Result>> resultList) {
        this.resultList = resultList;
    }

    public Map<String, List<Result>> getResultList() {
        return resultList;
    }

    public void setResultList(Map<String, List<Result>> resultList) {
        this.resultList = resultList;
    }
}