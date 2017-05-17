package org.pdxfinder.web.controllers;

import org.pdxfinder.services.MarkerService;
import org.pdxfinder.services.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

/**
 * Created by jmason on 16/03/2017.
 */
@Controller
public class SearchController {

    //@Autowired
    //SearchService searchService;
    @Autowired
    MarkerService markerService;
    @RequestMapping("/search")
    String index() {
        return "search";
    }


}
