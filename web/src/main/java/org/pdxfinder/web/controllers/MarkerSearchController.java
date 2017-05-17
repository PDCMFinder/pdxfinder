package org.pdxfinder.web.controllers;

import org.pdxfinder.dao.MarkerAssociation;
import org.pdxfinder.services.MarkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by csaba on 16/05/2017.
 */

@RestController
public class MarkerSearchController{

    MarkerService markerService;

    @Autowired
    public MarkerSearchController(MarkerService markerService){
        this.markerService = markerService;
    }

    @RequestMapping(method= RequestMethod.GET, value = "/getallmarkers")
    public List<String> getAllMarkers() {
        return markerService.getAllMarkers();
    }

}