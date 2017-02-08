package org.pdxfinder.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jmason on 05/02/2017.
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    String index() {
        return "index";
    }

}
