package org.pdxfinder.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.pdxfinder.services.*;
import org.pdxfinder.services.ds.AutoCompleteOption;
import org.pdxfinder.services.dto.CountDTO;
import org.pdxfinder.services.dto.DataAvailableDTO;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.dto.VariationDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 03/05/2018.
 */
@RestController
public class AjaxController {

    private AutoCompleteService autoCompleteService;
    private PlatformService platformService;
    private MolCharService molCharService;
    private DetailsService detailsService;
    private DrugService drugService;
    private GraphService graphService;


    @Autowired
    public AjaxController(AutoCompleteService autoCompleteService,
                          PlatformService platformService,
                          MolCharService molCharService,
                          DetailsService detailsService,
                          DrugService drugService,
                          GraphService graphService) {

        this.autoCompleteService = autoCompleteService;
        this.platformService = platformService;
        this.molCharService = molCharService;
        this.detailsService = detailsService;
        this.drugService = drugService;
        this.graphService = graphService;
    }

    @RequestMapping(value = "/drugnames")
    List<String> getDrugnames(){

        return drugService.getDrugNames();
    }

    @RequestMapping(value = "/modelcountperdrug")
    public List<CountDTO> getModelCountByDrug() {

        return  drugService.getModelCountByDrugAndComponentType("Drug");
    }

    @RequestMapping(value = "/modelcountpergene")
    public Iterable<Map<String, Object>> getModelCountByMarker() {

        return  graphService.getModelCountByGene();
    }

    @RequestMapping(value = "/autosuggests")
    List<AutoCompleteOption> getAutoSuggestList(){

        List<AutoCompleteOption> autoSuggestions = autoCompleteService.getAutoSuggestions();
        return autoSuggestions;
    }


    @RequestMapping(value = "/platform/{dataSrc}")
    public Map findPlatformBySource(@PathVariable String dataSrc) {

        return  platformService.getPlatformCountBySource(dataSrc);
    }


    @RequestMapping(value = "/platformdata/{dataSrc}")
    public List<DataAvailableDTO> findPlatformDataBySource(@PathVariable String dataSrc) {

        //populate list with data [{dataType:"mutation",platform:"CTP", models:20},{},{}]
        return  platformService.getAvailableDataBySource(dataSrc);
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
        start = (int) Math.ceil(start / 108.0);

        VariationDataDTO variationDataDTO = detailsService.variationDataByPlatform(dataSrc,modelId,dPlatform,dPassage,start,size,searchText,draw,sortColumn,sortDir);


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
        start = (int) Math.ceil(start / 108.0);

        String dPlatform = (platform == null) ? "" : platform;
        VariationDataDTO variationDataDTO = detailsService.patientVariationDataByPlatform(dataSrc,modelId,dPlatform,searchText,draw,sortColumn,sortDir,start,size);

        return variationDataDTO;

    }


    @RequestMapping(value = "/getxdata/{dataSrc}/{modelId}")
    public VariationDataDTO getXenoVariationData(@PathVariable String dataSrc,
                                                 @PathVariable String modelId,
                                                 @RequestParam(value="page", required = false) Integer page,
                                                 @RequestParam(value="size", required = false) Integer pageSize,
                                                 @RequestParam(value="passage", required = false) String passage,
                                                 @RequestParam(value="platform", required = false) String platform,
                                                 @RequestParam(value="sortcolumn", required = false) String sortColumn) {

        int start = (page == null || page < 1) ? 0 : page - 1;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;

        String dPlatform = (platform == null) ? "" : platform;
        String dPassage = (passage == null) ? "" : passage;
        String dSortColumn = (passage == null) ? "1" : sortColumn;

        VariationDataDTO variationDataDTO = detailsService.variationDataByPlatform(dataSrc,modelId,dPlatform,dPassage,start,size,"",1,"mAss.chromosome","asc");

        return variationDataDTO;

    }


    @RequestMapping(value = "/modeldetails/{dataSrc}/{modelId}")
    public DetailsDTO details(@PathVariable String dataSrc,
                              @PathVariable String modelId,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "20") Integer size,
                              @RequestParam(value="platform", required = false) String platform) {

        String viewPlatform = (platform == null) ? "" : platform;

        DetailsDTO dto = detailsService.getModelDetails(dataSrc, modelId, page, size,viewPlatform,"","");

        return  dto;
    }



    @RequestMapping(value = "/modeltech/{dataSrc}/{modelId}")
    public Map findModelTechnology(@PathVariable String dataSrc, @PathVariable String modelId,
                                   @RequestParam(value="passage", required = false) String passage) {

        String dPassage = (passage == null) ? "" : passage;
        return  detailsService.findModelPlatformAndPassages(dataSrc,modelId,dPassage);
    }


    @RequestMapping(value = "/patienttech/{dataSrc}/{modelId}")
    public Map findPatientTechnology(@PathVariable String dataSrc, @PathVariable String modelId) {

        return  detailsService.findPatientPlatforms(dataSrc,modelId);
    }


    @RequestMapping(value = "/getmutatedmarkerswithvariants")
    public Object getMutatedMarkersWithVariants(){

        JSONObject j = new JSONObject();

        try {
            j = new JSONObject(molCharService.getMutatedMarkersAndVariants());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //ResponseEntity rEntity = new ResponseEntity(molCharService.getMutatedMarkersAndVariants(), HttpStatus.OK);

        ObjectMapper mapper = new ObjectMapper();
        Object object = Object.class;

        try{
            object = mapper.readValue(molCharService.getMutatedMarkersAndVariants(), Object.class);
        }catch (Exception e){
        }

        return object;

    }



    //HELPER METHODS
    public String getSortColumn(String sortcolumn){

        Map<String, String> tableColumns = new HashMap<>();
        tableColumns.put("0","msamp.sourceSampleId");
        tableColumns.put("1","mAss.chromosome");
        tableColumns.put("2","mAss.seqPosition");
        tableColumns.put("3","mAss.refAllele");
        tableColumns.put("4","mAss.altAllele");
        tableColumns.put("5","mAss.consequence");
        tableColumns.put("6","m.symbol");
        tableColumns.put("7","mAss.aminoAcidChange");
        tableColumns.put("8","mAss.aminoAcidChange");
        tableColumns.put("9","mAss.alleleFrequency");
        tableColumns.put("10","mAss.rsVariants");

        return tableColumns.get(sortcolumn);
    }
}
