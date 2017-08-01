package org.pdxfinder.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by abayomi on 01/08/2017.
 */

@Controller
public class ErrorController {

    @RequestMapping("/error")
    String index() {
        return "error";
    }

}

