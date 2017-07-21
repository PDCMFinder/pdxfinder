package org.pdxfinder.web.controllers;

import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.SampleService;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.dto.SearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Created by csaba on 09/05/2017.
 */
@RestController
public class SampleSearchController
{

        SampleService sampleService;
        SearchService searchService;
        GraphService graphService;

        @Autowired
        public SampleSearchController(SampleService sampleService, SearchService searchService, GraphService graphService){
            this.sampleService = sampleService;
            this.searchService = searchService;
            this.graphService = graphService;
        }



        @RequestMapping(method = RequestMethod.GET, value = "/searchmodels")
        public List<SearchDTO> search2(@RequestParam(value="diag", required = false) String diag,
                                       @RequestParam(value="markers[]", required = false) String[] markers,
                                       @RequestParam(value="datasources[]", required = false) String[] datasources,
                                       @RequestParam(value="originttumortypes[]", required = false) String[] origintumortypes ) {

            if(diag == null) diag = "";
            if(markers == null) markers = new String[]{};
            if(datasources == null) datasources = new String[]{};
            if(origintumortypes == null) origintumortypes = new String[]{};


            System.out.println("Diag:"+diag);
            System.out.println("Markers:"+Arrays.toString(markers));
            System.out.println("Sources:"+Arrays.toString(datasources));
            System.out.println("Types:"+Arrays.toString(origintumortypes));

            return searchService.search(diag, markers, datasources, origintumortypes);
        }

}
