package org.pdxfinder.web.controllers;

import org.pdxfinder.services.AutoCompleteService;
import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.ds.AutoSuggestOption;
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

    private AutoCompleteService autoCompleteService;

    public AutoSuggestController(AutoCompleteService autoCompleteService) {
        this.autoCompleteService = autoCompleteService;
    }

    @RequestMapping(value = "/autosuggests")
    List<AutoSuggestOption> getAutoSuggestList(){


        return autoCompleteService.getAutoSuggestions();

    }



}
