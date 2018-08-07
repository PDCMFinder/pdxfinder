package org.pdxfinder.admin.controllers;

import org.neo4j.ogm.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by abayomi on 27/06/2017.
 */

@RestController
public class IndexController {

    private final static Logger log = LoggerFactory.getLogger(IndexController.class);


    @RequestMapping("/api")
    String index() throws JSONException {
        log.info("In the index method");

        return "index";
    }
}
