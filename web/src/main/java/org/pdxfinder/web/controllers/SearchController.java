package org.pdxfinder.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jmason on 16/03/2017.
 */
@Controller
public class SearchController {

    @RequestMapping("/search")
    String index() {
        return "search";
    }


}
