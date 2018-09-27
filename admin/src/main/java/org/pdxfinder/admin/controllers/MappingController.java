package org.pdxfinder.admin.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/*
 * Created by csaba on 01/08/2018.
 */
@Controller
public class MappingController {


    @RequestMapping("/mapping")
    String mapping() {

        return "mapping";
    }
}
