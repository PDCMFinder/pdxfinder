package org.pdxfinder.web.controllers;




import org.json.JSONObject;
import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.MolCharService;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.dto.VariationDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by abayomi on 05/07/2017.
 */
@RestController
public class RestControllerGeneral {

    GraphService graphService;
    SearchService searchService;
    MolCharService molCharService;

    @Autowired
    public RestControllerGeneral(GraphService graphService, SearchService searchService, MolCharService molCharService) {
        this.graphService = graphService;
        this.searchService = searchService;
        this.molCharService = molCharService;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/DOAutoSuggest")
    public Set<String> mappedDOTerm() {
        Set<String> autoSuggestList = graphService.getMappedDOTerms();
        return autoSuggestList;
    }



    @RequestMapping(value = "/modeldata/{dataSrc}/{modelId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public VariationDataDTO postVariationDataByPlatform(@PathVariable String dataSrc,
                                                        @PathVariable String modelId,
                                                        @RequestParam(value="platform", required = false) String platform,
                                                        @RequestParam(value="passage", required = false) String passage,
                                                        @RequestBody MultiValueMap data) {

        int draw = Integer.parseInt(data.getFirst("draw").toString());
        String searchText = data.getFirst("search[value]").toString();
        String sortColumn = data.getFirst("order[0][column]").toString();
        String sortDir = data.getFirst("order[0][dir]").toString();
        int start = Integer.parseInt(data.getFirst("start").toString());
        int size = Integer.parseInt(data.getFirst("length").toString());

        sortColumn = getSortColumn(sortColumn);

        String dPlatform = (platform == null) ? "" : platform;
        String dPassage = (passage == null) ? "" : passage;
        VariationDataDTO variationDataDTO = searchService.variationDataByPlatform(dataSrc,modelId,dPlatform,dPassage,start,size,searchText,draw,sortColumn,sortDir);

        return variationDataDTO;

    }


    @RequestMapping(value = "/patientdata/{dataSrc}/{modelId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public VariationDataDTO postPatientVariationData(@PathVariable String dataSrc,
                                                     @PathVariable String modelId,
                                                     @RequestParam(value="platform", required = false) String platform,
                                                     @RequestBody MultiValueMap data) {

        int draw = Integer.parseInt(data.getFirst("draw").toString());
        String searchText = data.getFirst("search[value]").toString();
        String sortColumn = data.getFirst("order[0][column]").toString();
        String sortDir = data.getFirst("order[0][dir]").toString();
        int start = Integer.parseInt(data.getFirst("start").toString());
        int size = Integer.parseInt(data.getFirst("length").toString());

        sortColumn = getSortColumn(sortColumn);

        String dPlatform = (platform == null) ? "" : platform;
        VariationDataDTO variationDataDTO = searchService.patientVariationDataByPlatform(dataSrc,modelId,dPlatform,searchText,draw,sortColumn,sortDir,start,size);

        return variationDataDTO;

    }




    @RequestMapping(value = "/getxdata/{dataSrc}/{modelId}")
    public VariationDataDTO getXenoVariationData(@PathVariable String dataSrc,
                                                 @PathVariable String modelId,
                                                 @RequestParam(value="page", required = false) Integer page,
                                                 @RequestParam(value="size", required = false) Integer pageSize,
                                                 @RequestParam(value="passage", required = false) String passage,
                                                 @RequestParam(value="platform", required = false) String platform) {

        int start = (page == null || page < 1) ? 0 : page - 1;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;

        /*
        TM00231?platform=CTP&page=0&size=20
        modeldata/JAX/TM00231?platform=CTP&passage=0
         */

        String dPlatform = (platform == null) ? "" : platform;
        String dPassage = (passage == null) ? "" : passage;
        VariationDataDTO variationDataDTO = searchService.variationDataByPlatform(dataSrc,modelId,dPlatform,dPassage,start,size,"",1,"","");

        return variationDataDTO;

    }


    @RequestMapping(value = "/modeldetails/{dataSrc}/{modelId}")
    public DetailsDTO details(@PathVariable String dataSrc,
                              @PathVariable String modelId,
                              @RequestParam(value="page", required = false) Integer page,
                              @RequestParam(value="size", required = false) Integer size,
                              @RequestParam(value="platform", required = false) String platform) {

        int viewPage = (page == null || page < 1) ? 0 : page - 1;
        int viewSize = (size == null || size < 1) ? 20 : size;
        String viewPlatform = (platform == null) ? "" : platform;

        DetailsDTO dto = searchService.searchForModel(dataSrc, modelId, viewPage,viewSize,viewPlatform,"","");

        return  dto;
    }


    @RequestMapping(value = "/modeltech/{dataSrc}/{modelId}")
    public Map findModelTechnology(@PathVariable String dataSrc, @PathVariable String modelId,
                                   @RequestParam(value="passage", required = false) String passage) {

        String dPassage = (passage == null) ? "" : passage;
        return  searchService.findModelPlatformAndPassages(dataSrc,modelId,dPassage);
    }


    @RequestMapping(value = "/patienttech/{dataSrc}/{modelId}")
    public Map findPatientTechnology(@PathVariable String dataSrc, @PathVariable String modelId) {

        return  searchService.findPatientPlatforms(dataSrc,modelId);
    }


    public String getSortColumn(String sortolumn){

        Map<String, String> tableColumns = new HashMap<>();
        tableColumns.put("0","mAss.technology");
        tableColumns.put("1","mAss.technology");
        tableColumns.put("2","mAss.technology");

        return tableColumns.get(sortolumn);
    }


    @RequestMapping(value = "/getmutatedmarkerswithvariants")
    public ResponseEntity getMutatedMarkersWithVariants(){
        JSONObject j = new JSONObject();

        try {
            j = new JSONObject(molCharService.getMutatedMarkersAndVariants());
        }
        catch(Exception e){
            e.printStackTrace();
        }
        //TODO: Look for possible formatting, it looks bad
        return new ResponseEntity(molCharService.getMutatedMarkersAndVariants(), HttpStatus.OK);
    }



}