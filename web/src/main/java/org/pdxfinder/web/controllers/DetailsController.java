package org.pdxfinder.web.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.pdxfinder.services.DetailsService;
import org.pdxfinder.services.PdfService;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.pdf.Label;
import org.pdxfinder.services.pdf.PdfHelper;
import org.pdxfinder.services.pdf.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/*
 * Created by abayomi on 09/05/2018.
 */
@Controller
public class DetailsController {


    private DetailsService detailsService;

    @Autowired
    PdfService pdfService;


    @Autowired
    public DetailsController(DetailsService detailsService) {
        this.detailsService = detailsService;
    }


    @RequestMapping(value = "/pdx/{dataSrc}/{modelId:.+}")
    public String details(Model model,
                          @PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                          @RequestParam(value = "size", defaultValue = "15000") Integer size) {

        model.addAttribute("data", detailsService.getModelDetails(dataSrc, modelId, page, size, "", "", ""));
        return "details";
    }


    @RequestMapping(method = RequestMethod.GET, value = "/pdx/{dataSrc}/{modelId}/export")
    @ResponseBody
    public String download(HttpServletResponse response,
                           @PathVariable String dataSrc,
                           @PathVariable String modelId) {

        Set<String[]> variationDataCSV = detailsService.getVariationDataCSV(dataSrc, modelId);


        CsvMapper mapper = new CsvMapper();

        CsvSchema schema = CsvSchema.builder()
                .addColumn("Sample ID")
                .addColumn("Passage")
                .addColumn("Histology")
                .addColumn("Tumor type")
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
                .addColumn("Platform")
                .build().withHeader();

        String output = "CSV output";
        try {
            output = mapper.writer(schema).writeValueAsString(variationDataCSV);
        } catch (JsonProcessingException e) {}

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=pdxfinder.org_variation" + dataSrc + "_" + modelId + ".csv");
        try{
            response.getOutputStream().flush();
        }catch (Exception e){

        }
        return output;


    }


    @GetMapping("/pdx/{dataSrc}/{modelId:.+}/pdf")
    public String pdfView(Model model, HttpServletRequest request,
                          @PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                          @RequestParam(value = "size", defaultValue = "15000") Integer size) {

        Report report = new Report();
        PdfHelper pdfHelper = new PdfHelper();

        DetailsDTO detailsDTO = detailsService.getModelDetails(dataSrc, modelId, page, size, "", "", "");

        String modelUrl = Label.WEBSITE + request.getRequestURI();

        modelUrl = modelUrl.substring(0, modelUrl.length() - 4);

        report.setContent(pdfService.generatePdf(detailsDTO, modelUrl));
        report.setStyles(pdfHelper.getStyles());

        model.addAttribute("report", report);

        return "pdf-generator";
    }


}

