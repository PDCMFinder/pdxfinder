package org.pdxfinder.web.controllers;

import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.services.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by csaba on 09/05/2017.
 */
@RestController
public class SampleSearchController {

    SampleService sampleService;

    @Autowired
    public SampleSearchController(SampleService sampleService){
        this.sampleService = sampleService;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/searchsamples")
    public Map<String, Object> search(@RequestParam(value="diag", required = false) String diag,
                                      @RequestParam(value="markers[]", required = false) String[] markers,
                                      @RequestParam(value="datasources[]", required = false) String[] datasources,
                                      @RequestParam(value="origintumortypes[]", required = false) String[] origintumortypes ) {

        return sampleService.searchForSamples(diag);
    }



}
