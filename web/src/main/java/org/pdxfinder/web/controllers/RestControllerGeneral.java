package org.pdxfinder.web.controllers;

import org.pdxfinder.services.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Created by abayomi on 05/07/2017.
 */
@RestController
public class RestControllerGeneral
{

        GraphService graphService;

        @Autowired
        public RestControllerGeneral( GraphService graphService)
        {
                 this.graphService = graphService;
        }



        @RequestMapping(method = RequestMethod.GET, value = "/DOAutoSuggest")
        public Set<String> mappedDOTerm()
        {
                Set<String> autoSuggestList = graphService.getMappedDOTerms();
                return autoSuggestList;
        }
}
