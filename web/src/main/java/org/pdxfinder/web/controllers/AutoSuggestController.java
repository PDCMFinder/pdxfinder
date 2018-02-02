package org.pdxfinder.web.controllers;

import org.pdxfinder.services.GraphService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * Created by csaba on 02/02/2018.
 */
@RestController
public class AutoSuggestController {

    private GraphService graphService;

    public AutoSuggestController(GraphService graphService) {
        this.graphService = graphService;
    }



    @RequestMapping(value = "/autosuggests")
    List<AutoSuggestOption> getAutoSuggestList(){
        List<AutoSuggestOption> autoSuggestList = new ArrayList<>();

        Set<String> ncitTerms = graphService.getMappedNCITTerms();

        for(String term:ncitTerms){
            autoSuggestList.add(new AutoSuggestOption(term, "OntologyTerm"));
        }

        return autoSuggestList;

    }



}
