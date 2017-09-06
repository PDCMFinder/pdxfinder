package org.pdxfinder.web.controllers;

import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.dto.DetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Created by abayomi on 05/07/2017.
 */
@RestController
public class RestControllerGeneral {

        GraphService graphService;
        SearchService searchService;

        @Autowired
        public RestControllerGeneral(GraphService graphService, SearchService searchService) {
                this.graphService = graphService;
                this.searchService = searchService;
        }


        @RequestMapping(method = RequestMethod.GET, value = "/DOAutoSuggest")
        public Set<String> mappedDOTerm() {
                Set<String> autoSuggestList = graphService.getMappedDOTerms();
                return autoSuggestList;
        }


        @RequestMapping(value = "/modeldetails/{dataSrc}/{modelId}")
        public DetailsDTO detail(@PathVariable String dataSrc, @PathVariable String modelId) {
                DetailsDTO dto = searchService.searchForModel(dataSrc, modelId);
                return dto;
        }

}