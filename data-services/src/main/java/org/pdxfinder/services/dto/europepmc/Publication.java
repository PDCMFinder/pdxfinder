package org.pdxfinder.services.dto.europepmc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Publication {

    private Map<String, List<Result>> resultList = new HashMap<>();

    public Publication() {
    }

    public Publication(Map<String, List<Result>> resultList) {
        this.resultList = resultList;
    }

    public Map<String, List<Result>> getResultList() {
        return resultList;
    }

}
