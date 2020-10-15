package org.pdxfinder.web.controllers;

import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.SearchService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


@Controller
public class IndexController {



    private GraphService graphService;
    private SearchService searchService;

    public IndexController(GraphService graphService, SearchService searchService) {
        this.graphService = graphService;
        this.searchService = searchService;
    }

    @RequestMapping("/")
    String contextRootRedirect(){
        return "redirect:/search";
    }


    @Cacheable
    private Integer getNumModels()
    {

        Integer numModels;
        int pdxCount = searchService.modelCount();
        pdxCount -= (pdxCount % 100);
        numModels = pdxCount;
        return numModels;
    }

}
