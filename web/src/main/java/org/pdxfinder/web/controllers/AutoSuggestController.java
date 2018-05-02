package org.pdxfinder.web.controllers;

import org.pdxfinder.services.AutoCompleteService;
import org.pdxfinder.services.ds.AutoCompleteOption;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    List<AutoCompleteOption> getAutoSuggestList(){


        List<AutoCompleteOption> autoSuggestions = autoCompleteService.getAutoSuggestions();
        return autoSuggestions;

    }



}
