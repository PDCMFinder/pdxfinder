package org.pdxfinder.web.controllers;

import org.pdxfinder.services.DetailsService;
import org.pdxfinder.services.PdfService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.dto.DetailsDTO;
import org.pdxfinder.services.pdf.Label;
import org.pdxfinder.services.pdf.PdfHelper;
import org.pdxfinder.services.pdf.Report;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Controller
public class DetailsController {


    private DetailsService detailsService;
    private UtilityService utilityService;
    private PdfService pdfService;

    public DetailsController(DetailsService detailsService, UtilityService utilityService, PdfService pdfService) {
        this.detailsService = detailsService;
        this.utilityService = utilityService;
        this.pdfService = pdfService;
    }

    @GetMapping("/pdx/{dataSrc}/{modelId:.+}")
    public String details(Model model,
                          @PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                          @RequestParam(value = "size", defaultValue = "15000") Integer size) {

        model.addAttribute("data", detailsService.getModelDetails(dataSrc, modelId));
        return "details";
    }


    @GetMapping("/pdx/{dataSrc}/{modelId}/{molcharType}/{molcharId}/export")
    @ResponseBody
    public String download(HttpServletResponse response,
                           @PathVariable String dataSrc,
                           @PathVariable String modelId,
                           @PathVariable String molcharType,
                           @PathVariable String molcharId) throws IOException {

        List<Map<String, Object>> molecularDataRowDTOS = detailsService.getMolecularDataTable(molcharId, true).getMolecularDataCsv();
        String output = utilityService.serializeToCsvWithIncludeNonEmpty(molecularDataRowDTOS);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", String.format("attachment; filename=pdxfinder.org_%s_%s_%s.csv",dataSrc, modelId, molcharType));
        response.getOutputStream().flush();
        return output;
    }


    @GetMapping("/pdx/{dataSrc}/{modelId:.+}/pdf")
    public String pdfView(Model model, HttpServletResponse response,HttpServletRequest request,
                          @PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value = "option", defaultValue = "download") String option) {

        Report report = new Report();
        PdfHelper pdfHelper = new PdfHelper();

        DetailsDTO detailsDTO = detailsService.getModelDetails(dataSrc, modelId);

        String modelUrl = Label.WEBSITE + request.getRequestURI();

        modelUrl = modelUrl.substring(0, modelUrl.length() - 4);

        report.setFooter(pdfService.generateFooter());
        report.setContent(pdfService.generatePdf(detailsDTO, modelUrl));
        report.setStyles(pdfHelper.getStyles());

        model.addAttribute("report", report);
        model.addAttribute("option", option);
        model.addAttribute("modelId", detailsDTO.getModelId());

        return "pdf-generator";
    }

}

