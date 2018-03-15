package org.pdxfinder.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.dao.Specimen;
import org.pdxfinder.services.GraphService;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.dto.VariationDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Created by csaba on 12/05/2017.
 */
@Controller
public class DetailsPageController {

    private SearchService searchService;
    private GraphService graphService;


    @Autowired
    public DetailsPageController(SearchService searchService, GraphService graphService) {
        this.searchService = searchService;
        this.graphService = graphService;
    }

    @RequestMapping(value = "/pdx/{dataSrc}/{modelId}")
    public String details(@PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value="page", required = false) Integer page,
                          @RequestParam(value="size", required = false) Integer size,Model model){

        int viewPage = (page == null || page < 1) ? 0 : page-1;
        int viewSize = (size == null || size < 1) ? 15000 : size;

        Map<String, String> patientTech = searchService.findPatientPlatforms(dataSrc,modelId);
        Map<String, Set<String>> modelTechAndPassages = searchService.findModelPlatformAndPassages(dataSrc,modelId,"");

        DetailsDTO dto = searchService.searchForModel(dataSrc,modelId,viewPage,viewSize,"","","");

        List<String> relatedModels = searchService.getModelsOriginatedFromSamePatient(dataSrc, modelId);



        List<VariationDataDTO> variationDataDTOList = new ArrayList<>();

        Map<String, String> techNPassToSampleId = new HashMap<>();

        for (String tech : modelTechAndPassages.keySet()) {

            //Retrieve the passages:
            Set<String> passages = modelTechAndPassages.get(tech);

            // Retrieve variation data by technology and passage
            for (String passage : passages){
                VariationDataDTO variationDataDTO = searchService.variationDataByPlatform(dataSrc,modelId,tech,passage,viewPage,viewSize,"",1,"","");
                variationDataDTOList.add(variationDataDTO);

                // Aggregate sampleIds for this Technology and passage in a Set<String>, to remove duplicates
                Set<String> sampleIDSet = new HashSet<>();
                for (String[] data : variationDataDTO.getData()){
                    sampleIDSet.add(data[0]);
                }

                // Turn the Set<String> to a comma seperated String
                String sampleIDs = "";
                for (String sampleID : sampleIDSet){
                    sampleIDs += sampleID+",";
                }

                // Create a Key Value map of (Technology+Passage , sampleIDs) and Pass to Thymeleaf front end
                techNPassToSampleId.put(tech+passage,StringUtils.stripEnd(sampleIDs, ","));
            }





        }

        //dto.setTotalPages((int) Math.ceil(totalRecords/dSize) );

        //auto suggestions for the search field
        Set<String> autoSuggestList = graphService.getMappedNCITTerms();
        model.addAttribute("mappedTerm", autoSuggestList);


        model.addAttribute("nonjsVariationdata", variationDataDTOList);

        model.addAttribute("fullData",dto);

        model.addAttribute("modelId",modelId);
        model.addAttribute("dataSrc",dataSrc);

        model.addAttribute("externalId", dto.getExternalId());
        model.addAttribute("dataSource", dto.getDataSource());
        model.addAttribute("patientId", dto.getPatientId());
        model.addAttribute("gender", dto.getGender());
        model.addAttribute("age", dto.getAge());
        model.addAttribute("race", dto.getRace());
        model.addAttribute("ethnicity", dto.getEthnicity());
        model.addAttribute("diagnosis", dto.getDiagnosis());
        model.addAttribute("tumorType", dto.getTumorType());
        model.addAttribute("class", dto.getClassification());
        model.addAttribute("originTissue", dto.getOriginTissue());
        model.addAttribute("sampleSite", dto.getSampleSite());

        model.addAttribute("sampleType", notEmpty(dto.getSampleType()));
        model.addAttribute("strain", notEmpty(dto.getStrain()));
        model.addAttribute("mouseSex", dto.getMouseSex());
        model.addAttribute("engraftmentSite", notEmpty(dto.getEngraftmentSite()));
        model.addAttribute("markers", dto.getCancerGenomics());
        model.addAttribute("url", dto.getExternalUrl());
        model.addAttribute("urlText", dto.getExternalUrlText());
        model.addAttribute("mappedOntology", dto.getMappedOntology());

        //model.addAttribute("specimenId", dto.getSpecimenId());
        for (Specimen specimen : dto.getSpecimens()) {
            model.addAttribute("specimenId",specimen.getExternalId() );
        }

        model.addAttribute("totalPages", dto.getTotalPages());
        model.addAttribute("presentPage", viewPage+1);
        model.addAttribute("totalRecords", dto.getVariationDataCount());

        model.addAttribute("variationData", dto.getMarkerAssociations());

        model.addAttribute("modelInfo", modelTechAndPassages);
        model.addAttribute("patientInfo", patientTech);

        model.addAttribute("relatedModels", relatedModels);
        model.addAttribute("technology", notEmpty(dto.getTechnology()));
        model.addAttribute("description", notEmpty(dto.getDescription()));
        model.addAttribute("passages", notEmpty(dto.getPassages()));
        model.addAttribute("sampleIdMap",techNPassToSampleId);


        Map<String, String> sorceDesc = new HashMap<>();
        sorceDesc.put("JAX","The Jackson Laboratory");
        sorceDesc.put("PDXNet-HCI-BCM","HCI-Baylor College of Medicine");
        sorceDesc.put("PDXNet-Wistar-MDAnderson-Penn","Melanoma PDX established by the Wistar/MD Anderson/Penn");
        sorceDesc.put("PDXNet-WUSTL","Washington University in St. Louis");
        sorceDesc.put("PDXNet-MDAnderson","University of Texas MD Anderson Cancer Center");
        sorceDesc.put("PDMR","NCI Patient-Derived Models Repository");
        sorceDesc.put("IRCC","Candiolo Cancer Institute");


        model.addAttribute("sourceDescription", sorceDesc.get(dto.getDataSource()));
        model.addAttribute("contacts",dto.getContacts());

        /*
        if(relatedModels.size()>0){
            String rm = "";
            for (String mod:relatedModels){
                rm+="<a href=\"/data/pdx/"+dto.getDataSource()+"/"+mod+"\">"+mod+"</a>";
            }
            model.addAttribute("relatedModels", rm);
        }
        else{
            model.addAttribute("relatedModels", "-");
        }
        */

        return "details";
    }





    @RequestMapping(method = RequestMethod.GET, value = "/pdx/{dataSrc}/{modelId}/export")
    @ResponseBody
    public String download(HttpServletResponse response,
                                   @PathVariable String dataSrc,
                                   @PathVariable String modelId){

        Map<String, Set<String>> modelTechAndPassages = searchService.findModelPlatformAndPassages(dataSrc,modelId,"");

        List<List<String[]>> variationDataDTOList = new ArrayList<>();

        for (String tech : modelTechAndPassages.keySet()) {
            VariationDataDTO variationDataDTO = searchService.variationDataByPlatform(dataSrc,modelId,tech,"",0,50000,"",1,"","");
            variationDataDTOList.add(variationDataDTO.getData());
        }

        List<String[]> variationData = variationDataDTOList.get(0);

        CsvMapper mapper = new CsvMapper();

        CsvSchema schema = CsvSchema.builder()
                .addColumn("Sample ID")
                .addColumn("Chromosome")
                .addColumn("Seq. Position")
                .addColumn("Ref Allele")
                .addColumn("Alt Allele")
                .addColumn("Consequence")
                .addColumn("Gene")
                .addColumn("Amino Acid Change")
                .addColumn("Read Depth")
                .addColumn("Allele Freq")
                .addColumn("RS Variant")
                .build().withHeader();


        String output = "CSV output";
        try {
            output = mapper.writer(schema).writeValueAsString(variationData);
        } catch (JsonProcessingException e) {}

        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=pdxfinder.org_variation"+dataSrc+"_"+modelId+".csv");

        return output;

    }



    public String notEmpty(String incoming){

        String result = (incoming == null) ? "Not Available" : incoming;
        result = (result.length() == 0 ? "Not Available" : result );

        return result;
    }

}