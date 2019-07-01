package org.pdxfinder.web.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.*;
import org.pdxfinder.services.ds.AutoCompleteOption;
import org.pdxfinder.services.dto.*;
import org.pdxfinder.services.highchart.HexColors;
import org.pdxfinder.services.pdf.Label;
import org.pdxfinder.services.pdf.PdfHelper;
import org.pdxfinder.services.pdf.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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


    @GetMapping("/cytogenetics")
    public String getCytogeneticsCombination(){


        String modelID = "";
        List<String> results = new ArrayList<>();

        String rowData = "";

        List<String> markerFilter = Arrays.asList("ERBB2","PGR","ESR1");

        List<ModelCreation> models = searchService.getModelsByMolcharType("cytogenetics");

        for(ModelCreation modelCreation : models){

            modelID = modelCreation.getSourcePdxId();

            Set<Sample> samples = modelCreation.getRelatedSamples();

            for (Sample sample : samples){

                Set<MolecularCharacterization> molchars = sample.getMolecularCharacterizations();

                for (MolecularCharacterization molchar : molchars){

                    List<MarkerAssociation> markerAssocs = molchar.getMarkerAssociations();

                    results = new ArrayList<>();

                    for (MarkerAssociation mAssoc : markerAssocs){

                        boolean inTheList = markerFilter.stream().anyMatch(str -> str.equals(mAssoc.getMarker().getHgncSymbol()));

                        if (inTheList) {
                            results.add(mAssoc.getMarker().getHgncSymbol() + " " + mAssoc.getCytogeneticsResult());
                        }
                    }
                }
            }

            rowData += results+"<br>";
        }

        return rowData;
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

        List<String> autoSuggestions = autoCompleteService.getAutoSuggestions();
        List<AutoCompleteOption> suggestions = new ArrayList<>();

        for(String s : autoSuggestions){
            suggestions.add(new AutoCompleteOption(s, "OntologyTerm"));
        }

        return suggestions;
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


   /*  @RequestMapping(value = "/modeldata/{dataSrc}/{modelId}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
    */

    @RequestMapping(value = "/modeldetails/{dataSrc}/{modelId}")
    public DetailsDTO details(@PathVariable String dataSrc,
                              @PathVariable String modelId,
                              @RequestParam(value = "page", defaultValue = "0") Integer page,
                              @RequestParam(value = "size", defaultValue = "20") Integer size,
                              @RequestParam(value="platform", required = false) String platform) {

        String viewPlatform = (platform == null) ? "" : platform;

        DetailsDTO dto = detailsService.getModelDetails(dataSrc, modelId);

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

        return detailsService.getMolecularDataTable(molcharId);

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

    @GetMapping("/statistics/molecular-data")
    public Object getChart(){

        List<StatisticsDTO> stats = statistics.mockRepository();

        return statistics.combinedColumnLineAndPieChart(stats);
    }


    @GetMapping("/statistics/drug-dosing")
    public Object getDosingStat(){

        Map<String, List<StatisticsDTO>> data = statistics.groupedData();

        return statistics.fixedPlacementColumnChart(data, "Patient Treatments Data");
    }

    @GetMapping("/statistics/patient-treatment/{param}")
    public Object basicTreatmentStat(@PathVariable String param){

        List<StatisticsDTO> data = new ArrayList<>();
        String chartTitle  = "";
        String subtitle   = "";

        if (param.equals("patients") ){
            chartTitle = "Patient Data";
            subtitle   = "Patient Count Per Data Release";
            data = statistics.mockDataTreatmentPatients();
        }else if (param.equals("drugs")) {

            chartTitle = "Drug Data";
            subtitle   = "Drug Count Per Data Release";
            data = statistics.mockDataDrugDosing();
        }else{

        }

        return statistics.clusteredBarChart(data, chartTitle, subtitle);
    }



    @GetMapping("/statistics/model")
    public Object getModelStat(){

        List<CountDTO> data = statistics.modelCountData();

        String chartTitle  = "Model Count Data";
        String subtitle   = "Model Count Per Data Release";

        return statistics.barChart(data, chartTitle, subtitle, HexColors.CUSTOMBLUE);
    }


    @GetMapping("/statistics/providers")
    public Object getProvidersStat(){

        List<CountDTO> data = statistics.providersCountData();

        String chartTitle  = "Data Providers";
        String subtitle   = "Providers Count Per Data Release";

        return statistics.barChart(data, chartTitle, subtitle, HexColors.BLACK);
    }




}
