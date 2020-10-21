package org.pdxfinder.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.graph.queryresults.MutatedMarkerData;
import org.pdxfinder.services.*;
import org.pdxfinder.services.ds.AutoCompleteOption;
import org.pdxfinder.services.dto.*;
import org.pdxfinder.services.highchart.HexColors;
import org.pdxfinder.services.pdf.Label;
import org.pdxfinder.services.pdf.PdfHelper;
import org.pdxfinder.services.pdf.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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
    private Statistics statistics;

    @Autowired
    PdfService pdfService;

    @Autowired
    private SearchService searchService;

    @Autowired
    public AjaxController(AutoCompleteService autoCompleteService,
                          PlatformService platformService,
                          MolCharService molCharService,
                          DetailsService detailsService,
                          DrugService drugService,
                          GraphService graphService,
                          Statistics statistics) {

        this.autoCompleteService = autoCompleteService;
        this.platformService = platformService;
        this.molCharService = molCharService;
        this.detailsService = detailsService;
        this.drugService = drugService;
        this.graphService = graphService;
        this.statistics = statistics;
    }


    @GetMapping("/drugnames")
    List<String> getDrugnames(){

        return drugService.getDrugNames();
    }

    @GetMapping("/modelcountperdrug")
    public List<CountDTO> getModelCountByDrug() {

        return  drugService.getModelCountByDrugs();
    }

    @GetMapping("/modelcountpergene")
    public List<MutatedMarkerData> getModelCountByMarker() {

        return  graphService.getModelCountByGene();
    }

    @GetMapping("/autosuggests")
    List<AutoCompleteOption> getAutoSuggestList(){

        List<String> autoSuggestions = autoCompleteService.getAutoSuggestions();
        List<AutoCompleteOption> suggestions = new ArrayList<>();

        for(String s : autoSuggestions){
            suggestions.add(new AutoCompleteOption(s, "OntologyTerm"));
        }

        return suggestions;
    }


    @GetMapping("/platform/{dataSrc}")
    public Map findPlatformBySource(@PathVariable String dataSrc) {

        return  platformService.getPlatformCountBySource(dataSrc);
    }


    @GetMapping("/platformdata/{dataSrc}")
    public List<DataAvailableDTO> findPlatformDataBySource(@PathVariable String dataSrc) {

        //populate list with data [{dataType:"mutation",platform:"CTP", models:20},{},{}]
        return  platformService.getAvailableDataBySource(dataSrc);
    }


    @GetMapping("/modeldetails/{dataSrc}/{modelId}")
    public DetailsDTO details(@PathVariable String dataSrc,
                              @PathVariable String modelId,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "20") Integer size,
                              @RequestParam(value="platform", required = false) String platform) {

        String viewPlatform = (platform == null) ? "" : platform;

        DetailsDTO dto = detailsService.getModelDetails(dataSrc, modelId);

        return  dto;
    }



    @GetMapping("/modeltech/{dataSrc}/{modelId}")
    public Map findModelTechnology(@PathVariable String dataSrc, @PathVariable String modelId,
                                   @RequestParam(value="passage", required = false) String passage) {

        String dPassage = (passage == null) ? "" : passage;
        return  detailsService.findModelPlatformAndPassages(dataSrc,modelId,dPassage);
    }


    @GetMapping("/patienttech/{dataSrc}/{modelId}")
    public Map findPatientTechnology(@PathVariable String dataSrc, @PathVariable String modelId) {

        return  detailsService.findPatientPlatforms(dataSrc,modelId);
    }


    @GetMapping("/getmutatedmarkerswithvariants")
    public Object getMutatedMarkersWithVariants(){

        ObjectMapper mapper = new ObjectMapper();
        Object object = Object.class;

        try{
            object = mapper.readValue(molCharService.getMutatedMarkersAndVariants(), Object.class);
        }catch (Exception e){
        }

        return object;

    }


    @GetMapping("/pdx/{dataSrc}/{modelId:.+}/pdf-data")
    public Report pdfView(HttpServletRequest request,
                          @PathVariable String dataSrc,
                          @PathVariable String modelId,
                          @RequestParam(value = "page", defaultValue = "0") Integer page,
                          @RequestParam(value = "size", defaultValue = "15000") Integer size) {

        Report report = new Report();
        PdfHelper pdfHelper = new PdfHelper();

        DetailsDTO detailsDTO = detailsService.getModelDetails(dataSrc, modelId);

        String modelUrl = Label.WEBSITE + request.getRequestURI();
        modelUrl = modelUrl.substring(0, modelUrl.length() - 9);

        report.setFooter(pdfService.generateFooter());
        report.setContent(pdfService.generatePdf(detailsDTO, modelUrl));
        report.setStyles(pdfHelper.getStyles());

        return report;
    }

    @GetMapping("/getmoleculardata/{molcharId}")
    public MolecularDataTableDTO getMolecularTableData(@PathVariable String molcharId){

        return detailsService.getMolecularDataTable(molcharId, false);

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



    /**********   STATISTICS CONTROLLERS   ************/


    @GetMapping("/statistics/test")
    public Object doTest(){

        List dataLabels = Arrays.asList("IRC-CRC", "JAX", "CRL", "TRACE-PDTX", "CURIE-LC", "CURIE-BC");
        List dataValues = Arrays.asList(1300, 375, 500, 450, 313, 1000);
        String chartTitle  = "PDX Finder Data Deposit Statistics";
        String subtitle   = "As per statistics data 2019";

        return statistics.threeDdoughNutPie(dataLabels,dataValues,chartTitle,subtitle,"JAX");
    }


    @GetMapping("/statistics/molecular-data")
    public Object getChart(){

        List<StatisticsDTO> stats = statistics.mockRepository();

        return statistics.combinedColumnLineAndPieChart(stats);
    }


    @GetMapping("/statistics/drug-dosing/{param}")
    public Object getDosingStat(@PathVariable String param){


        List<StatisticsDTO> data = new ArrayList<>();
        String chartTitle  = "";
        String subtitle   = "";

        if (param.equals("models") ){
            chartTitle = "PDX Models Drug Dosing Data";
            subtitle   = "PDX Models Having Drug Dosing Data Per Data Source";
            data = statistics.pdxCountHavingDrugDataPerDataSource();
        }else if (param.equals("drugs")) {

            chartTitle = "Drug Data Count";
            subtitle   = "Drug Data Count Per Data Source";
            data = statistics.drugCountPerDataSource();
        }

        return statistics.clusteredColumnChart(data, chartTitle, subtitle);

    }

    @GetMapping("/statistics/patient-treatment/{param}")
    public Object basicTreatmentStat(@PathVariable String param){

        List<StatisticsDTO> data = new ArrayList<>();
        String chartTitle  = "";
        String subtitle   = "";

        if (param.equals("patients") ){
            chartTitle = "PDX Models Treatment Data";
            subtitle   = "PDX Models Having Treatment Data Per Data Source";
            data = statistics.pdxCountHavingTreatmentDataPerDataSource();
        }else if (param.equals("treatments")) {

            chartTitle = "Treatment Data Count";
            subtitle   = "Treatment Data Count Per Data Source";
            data = statistics.treatmentsCountPerDataSource();
        }

        return statistics.clusteredColumnChart(data, chartTitle, subtitle);
    }



    @GetMapping("/statistics/model")
    public Object getModelStat(){

        List<CountDTO> data = statistics.modelCount();

        String chartTitle  = "Model Count Data";
        String subtitle   = "Model Count Per Data Release";

        return statistics.basicColumnChart(data, chartTitle, subtitle, HexColors.CUSTOMBLUE);
    }


    @GetMapping("/statistics/providers")
    public Object getProvidersStat(){

        List<CountDTO> data = statistics.providersCount();

        String chartTitle  = "Data Providers";
        String subtitle   = "Providers Count Per Data Release";

        return statistics.basicColumnChart(data, chartTitle, subtitle, HexColors.BLACK);
    }



}
