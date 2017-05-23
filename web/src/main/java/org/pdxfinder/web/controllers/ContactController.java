package org.pdxfinder.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jmason on 23/05/2017.
 */
@Controller
public class ContactController {

    @RequestMapping("/contact")
    String contact() {
        return "contact";
    }


}
