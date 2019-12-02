package org.pdxfinder.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.pdxfinder.services.reporting.MarkerLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Created by csaba on 14/05/2019.
 */
@Component
@Order(value = 15)
public class LoadAdditionalDatasets implements CommandLineRunner, ApplicationContextAware{

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    Logger log = LoggerFactory.getLogger(LoadAdditionalDatasets.class);

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    static ApplicationContext context;
    ReportManager reportManager;


    @Autowired
    private DataImportService dataImportService;

    @Autowired
    private UtilityService utilityService;

    private Session session;




    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadAdditionalDatasets", "Loading additional datasets");
        parser.accepts("loadALL", "Load all, loading additional datasets");
        OptionSet options = parser.parse(args);

        if (options.has("loadAdditionalDatasets") || options.has("loadALL")) {

            reportManager = (ReportManager) context.getBean("ReportManager");

            loadAdditionalDatasetForCRL();

        }
    }


    private void loadAdditionalDatasetForCRL() throws Exception{

        log.info("Loading additional datasets for CRL.");

        String templateFileStr = finderRootDir+"/data/UPDOG/CRL/template.xlsx";
        String markerTemplateFileStr = finderRootDir + "/data/UPDOG/CRL/cna_tested_markers/list.csv";

        File markerListFile = new File(markerTemplateFileStr);
        File templateFile = new File(templateFileStr);

        Set<Marker> markerSet = new HashSet<>();

        if(markerListFile.exists() && templateFile.exists()){

            List<List<String>> markerList = utilityService.serializeCSVToArrayList(markerTemplateFileStr);

            //STEP1: get the marker symbols from the csv file then find the corresponding marker node and add it to a set
            log.info("Found "+markerList.size()+ " markers in the marker file.");
            for(int i = 1; i<markerList.size(); i++){

                if(i%500 == 0){
                    log.info("Collected "+i+" markers from the repository.");
                }

                String markerSymbol = markerList.get(i).get(0);

                //skip all symbols with dot in them
                if(markerSymbol.contains(".")){

                    MarkerLogEntity le = new MarkerLogEntity(this.getClass().getSimpleName(),"CRL", "-", "copy number alteration", "Not Specified", markerSymbol, "","");
                    le.setMessage(markerSymbol +" is an unrecognised symbol");
                    le.setType("ERROR");
                    continue;

                }


                //log.info("Looking up: "+markerSymbol);
                NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), "CRL", "-", markerSymbol, "copy number alteration", "Not Specified");

                if(nsdto.getNode() == null){

                    //uh oh, we found an unrecognised marker symbol, abort, abort!!!!
                    reportManager.addMessage(nsdto.getLogEntity());

                    continue;
                }
                else{

                    Marker marker = (Marker) nsdto.getNode();
                    markerSet.add(marker);

                }

            }


            //STEP2:


            UniversalLoader updog = new UniversalLoader(reportManager, utilityService, dataImportService);
            updog.setFinderRootDir(finderRootDir);

            updog.initTemplates(templateFileStr);

            List<List<String>> datasetDerived = updog.getSamplePlatformDescriptionSheetData();
            int row = 6;

            for (List<String> derivedDatasetRow : datasetDerived) {

                String sampleId = derivedDatasetRow.get(0);

                String origin = derivedDatasetRow.get(1);
                String passage = derivedDatasetRow.get(2);
                String nomenclature = derivedDatasetRow.get(3);
                String modelId = derivedDatasetRow.get(4);
                String molCharType = derivedDatasetRow.get(5);
                String platformName = derivedDatasetRow.get(6);
                String platformTechnology = derivedDatasetRow.get(7);
                String platformDescription = derivedDatasetRow.get(8);
                String analysisProtocol = derivedDatasetRow.get(9);

                //SKIP everything that is not cna
                if(!molCharType.toLowerCase().equals("copy number alteration")){

                    row++;
                    continue;
                }

                platformName = platformName.replaceAll("[^A-Za-z0-9 _-]", "");

                ModelCreation model;
                Sample sample = null;
                //patient sample
                if (origin.toLowerCase().equals("patient")) {

                    sample = dataImportService.findHumanSample(modelId, "CRL");

                    if (sample != null) {

                    }
                    else {

                        log.error("Unknown human sample with id: " + sampleId);
                        row++;
                        continue;
                    }
                }
                //xenograft sample
                else if (origin.toLowerCase().equals("engrafted tumor") || origin.toLowerCase().equals("engrafted tumour") || origin.toLowerCase().equals("xenograft") ) {

                    if (passage == null || passage.isEmpty() || passage.toLowerCase().equals("not specified")) {

                        log.error("Missing essential value Xenograft Passage in row " + row);
                        row++;
                        continue;
                    }

                    if (nomenclature == null || nomenclature.isEmpty()) {

                        log.error("Missing essential value nomenclature in row " + row);
                        row++;
                        continue;
                    }

                    //need this trick to get rid of 0.0 if there is any
                    //if(passage.equals("0.0")) passage = "0";
                    int passageInt = (int) Float.parseFloat(passage);
                    passage = String.valueOf(passageInt);

                    model = dataImportService.findModelByIdAndDataSourceWithSpecimensAndHostStrain(modelId, "CRL");

                    if(model == null){
                        log.error("Model "+modelId + " not found, skipping");
                        row++;
                        continue;
                    }

                    //this specimen should have the appropriate hoststrain, too!
                    Specimen specimen = dataImportService.findSpecimenByModelAndPassageAndNomenclature(model, passage, nomenclature);

                    if(specimen != null) {

                        sample = specimen.getSample();

                    }
                    else{
                        log.error("Specimen not found in row "+row);
                        row++;
                        continue;
                    }

                }

                //STEP3: find the proper molecular characterization node
                MolecularCharacterization molChar = null;

                if(sample != null){

                    for(MolecularCharacterization mc : sample.getMolecularCharacterizations()){

                        if(mc.getType().equalsIgnoreCase(molCharType)){

                            if(mc.getPlatform() != null && mc.getPlatform().getName().equalsIgnoreCase(platformName)){

                                molChar = mc;
                                break;
                            }
                        }

                    }
                }
                else{

                    log.error("Proper sample not found for data in row: "+row);
                    row++;
                    continue;
                }


                if(molChar != null){
                //STEP4: link molchar to the markers with a fake MA
                    int maCounter = 0;
                    MarkerAssociation ma = new MarkerAssociation();
                    for(Marker m: markerSet){


                        //ma.setMarker(m);
                        molChar.addMarkerAssociation(ma);
                        maCounter++;
                        if(maCounter !=0 && maCounter%500==0){
                            dataImportService.saveMolecularCharacterization(molChar);
                            log.info("Saved "+maCounter+" associations for "+modelId);
                        }

                    }

                    //STEP5: save the modified molchar object
                    dataImportService.saveMolecularCharacterization(molChar);
                    log.info("Saved cna data for model: "+modelId);

                }
                else{
                    log.error("Proper MolChar object not found for data in row: "+row);
                    row++;
                    continue;

                }



            }



        }


    }




    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

}
