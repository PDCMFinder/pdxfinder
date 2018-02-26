package org.pdxfinder.services;

import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.repositories.OntologyTermRepository;
import org.pdxfinder.services.ds.AutoSuggestOption;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * Created by csaba on 05/02/2018.
 */
@Service
public class AutoCompleteService {


    private OntologyTermRepository ontologyTermRepository;


    public AutoCompleteService(OntologyTermRepository ontologyTermRepository) {
        this.ontologyTermRepository = ontologyTermRepository;
    }


    public List<AutoSuggestOption> getAutoSuggestions(){

        Collection<OntologyTerm> ontologyTerms = ontologyTermRepository.findAllWithMappings();
        List<AutoSuggestOption> autoSuggestList = new ArrayList<>();

        for (OntologyTerm ontologyTerm : ontologyTerms) {
            if (ontologyTerm.getLabel() != null) {

                autoSuggestList.add(new AutoSuggestOption(ontologyTerm.getLabel(), "OntologyTerm"));
            }
        }

        return autoSuggestList;
    }


}
