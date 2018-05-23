package org.pdxfinder.web.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.pdxfinder.services.SearchService;
import org.pdxfinder.services.ds.ModelForQueryExport;
import org.pdxfinder.services.dto.ExportDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by jmason on 16/03/2017.
 */
@Controller
public class SearchController {

    private SearchService searchService;

    public SearchController(SearchService searchService) {

        this.searchService = searchService;
    }

    @RequestMapping("/search/export")
    @ResponseBody
    String export(HttpServletResponse response,
                  @RequestParam("query") Optional<String> query,
                  @RequestParam("datasource") Optional<List<String>> datasource,
                  @RequestParam("diagnosis") Optional<List<String>> diagnosis,
                  @RequestParam("patient_age") Optional<List<String>> patient_age,
                  @RequestParam("patient_treatment_status") Optional<List<String>> patient_treatment_status,
                  @RequestParam("patient_gender") Optional<List<String>> patient_gender,
                  @RequestParam("sample_origin_tissue") Optional<List<String>> sample_origin_tissue,
                  @RequestParam("cancer_system") Optional<List<String>> cancer_system,
                  @RequestParam("sample_tumor_type") Optional<List<String>> sample_tumor_type,
                  @RequestParam("mutation") Optional<List<String>> mutation,
                  @RequestParam("drug") Optional<List<String>> drug
    ) {


        ExportDTO eDTO = searchService.export(query, datasource,
                diagnosis, patient_age, patient_treatment_status, patient_gender, sample_origin_tissue, cancer_system,
                sample_tumor_type, mutation, drug);

        Set<ModelForQueryExport> exportResults = eDTO.getResults().stream().map(ModelForQueryExport::new).collect(Collectors.toSet());

        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = mapper.schemaFor(ModelForQueryExport.class).withHeader();

        String output = "CSV output for configured values " + eDTO.getFacetsString();
        try {
            output = mapper.writer(schema).writeValueAsString(exportResults);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=pdxfinder_search_export.csv");
        try{
            response.getOutputStream().flush();
        }catch (Exception e){

        }

        return output;

    }



    @RequestMapping("/search")
    String search2(Model model,
                   @RequestParam("query") Optional<String> query,
                   @RequestParam("datasource") Optional<List<String>> datasource,
                   @RequestParam("diagnosis") Optional<List<String>> diagnosis,
                   @RequestParam("patient_age") Optional<List<String>> patient_age,
                   @RequestParam("patient_treatment_status") Optional<List<String>> patient_treatment_status,
                   @RequestParam("patient_gender") Optional<List<String>> patient_gender,
                   @RequestParam("sample_origin_tissue") Optional<List<String>> sample_origin_tissue,
                   @RequestParam("cancer_system") Optional<List<String>> cancer_system,
                   @RequestParam("sample_tumor_type") Optional<List<String>> sample_tumor_type,
                   @RequestParam("mutation") Optional<List<String>> mutation,
                   @RequestParam("drug") Optional<List<String>> drug,

                   @RequestParam(value = "page", defaultValue = "1") Integer page,
                   @RequestParam(value = "size", defaultValue = "10") Integer size){

        model.addAttribute("websearch", searchService.webSearch(query, datasource,
                diagnosis, patient_age, patient_treatment_status, patient_gender, sample_origin_tissue, cancer_system,
                sample_tumor_type, mutation, drug, page, size));

        return "search";
    }

}
