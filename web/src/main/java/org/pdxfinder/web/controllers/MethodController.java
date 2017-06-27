package org.pdxfinder.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by abayomi on 27/06/2017.
 */


@Controller
public class MethodController {

    @RequestMapping("/methods")
    String index() {
        return "methods";
    }

}
