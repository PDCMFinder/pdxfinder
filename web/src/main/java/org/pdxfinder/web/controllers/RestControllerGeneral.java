package org.pdxfinder.web.controllers;

import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.dto.VariationDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

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

        @Autowired
        public RestControllerGeneral(GraphService graphService, SearchService searchService) {
                this.graphService = graphService;
                this.searchService = searchService;
        }


        @RequestMapping(method = RequestMethod.GET, value = "/DOAutoSuggest")
        public Set<String> mappedDOTerm() {
                Set<String> autoSuggestList = graphService.getMappedDOTerms();
                return autoSuggestList;
        }


        @RequestMapping(value = "/datatable/{dataSrc}/{modelId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        public VariationDataDTO getVariationTable(@PathVariable String dataSrc,
                                                  @PathVariable String modelId,
                                                  @RequestBody MultiValueMap data) {

                int draw = Integer.parseInt(data.getFirst("draw").toString());
                String searchText = data.getFirst("search[value]").toString();
                String sortColumn = data.getFirst("order[0][column]").toString();
                String sortDir = data.getFirst("order[0][dir]").toString();
                int start = Integer.parseInt(data.getFirst("start").toString());
                int size = Integer.parseInt(data.getFirst("length").toString());

                sortColumn = getSortColumn(sortColumn);
                VariationDataDTO variationDataDTO = searchService.getTable(dataSrc,modelId,searchText,draw,sortColumn,sortDir,start,size);

                return variationDataDTO;

        }


        @RequestMapping(value = "/modeldetails/{dataSrc}/{modelId}")
        public DetailsDTO details(@PathVariable String dataSrc,
                                  @PathVariable String modelId,
                                  @RequestParam(value="page", required = false) Integer page) {

                int viewPage = (page == null || page < 1) ? 0 : page - 1;

                DetailsDTO dto = searchService.searchForModel(dataSrc, modelId, viewPage);

                return  dto;
        }


        public String getSortColumn(String sortolumn){

                Map<String, String> tableColumns = new HashMap<>();
                tableColumns.put("0","mAss.technology");
                tableColumns.put("1","mAss.technology");
                tableColumns.put("2","mAss.technology");

                return tableColumns.get(sortolumn);
        }

}