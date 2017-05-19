package org.pdxfinder.web.controllers;

import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.services.SampleService;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.dto.SearchDTO;
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
    SearchService searchService;

    @Autowired
    public SampleSearchController(SampleService sampleService, SearchService searchService){
        this.sampleService = sampleService;
        this.searchService = searchService;
    }

    //This is broken, don't use it
    @RequestMapping(method = RequestMethod.GET, value = "/searchsampleswrong")
    public Map<String, Object> search(@RequestParam(value="diag", required = false) String diag,
                                      @RequestParam(value="markers[]", required = false) String[] markers,
                                      @RequestParam(value="datasources[]", required = false) String[] datasources,
                                      @RequestParam(value="origintumortypes[]", required = false) String[] origintumortypes ) {

        return sampleService.searchForSamples(diag);
    }



    @RequestMapping(method = RequestMethod.GET, value = "/searchsamples")
    public List<SearchDTO> search2(@RequestParam(value="diag", required = false) String diag,
                                   @RequestParam(value="markers[]", required = false) String[] markers,
                                   @RequestParam(value="datasources[]", required = false) String[] datasources,
                                   @RequestParam(value="origintumortypes[]", required = false) String[] origintumortypes ) {

        if(diag == null) diag = "";
        if(markers == null) markers = new String[]{};
        if(datasources == null) datasources = new String[]{};
        if(origintumortypes == null) origintumortypes = new String[]{};


        System.out.println("Diag:"+diag);
        System.out.println("Markers:"+Arrays.toString(markers));
        System.out.println("Sources:"+Arrays.toString(datasources));
        System.out.println("Types:"+Arrays.toString(origintumortypes));

        return searchService.searchForSamplesWithFilters(diag, markers, datasources, origintumortypes);
    }

}
