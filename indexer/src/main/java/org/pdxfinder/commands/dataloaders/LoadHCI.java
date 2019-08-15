package org.pdxfinder.commands.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.HelpFormatter;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;

/**
 * Load data from HCI PDXNet.
 */
@Component
@Order(value = -20)
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "hci")
public class LoadHCI extends LoaderBase implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(LoadHCI.class);

    private HelpFormatter formatter;

    @Value("${pdxfinder.root.dir}")
    private String finderRootDir;

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadHCI(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("loadHCI", "Load HCI PDX data");
        parser.accepts("loadALL", "Load all, including HCI PDX data");
        OptionSet options = parser.parse(args);

        if (options.has("loadHCI") || options.has("loadALL")) {

            initMethod();

            globalLoadingOrder();

        }
    }



    @Override
    protected void initMethod() {

        log.info("Loading Huntsman PDX data. ");

        dto = new LoaderDTO();

        jsonFile = finderRootDir + "/data/" + dataSourceAbbreviation + "/pdx/models.json";
        dataSource = dataSourceAbbreviation;
        filesDirectory = "";
    }



    @Override
    protected void step01GetMetaDataFolder() { }



    // HCI uses common implementation Steps step08GetMetaData,step09LoadPatientData default


    @Override
    protected void step10LoadExternalURLs() {

        loadExternalURLs(dataSourceContact,Standardizer.NOT_SPECIFIED);
    }


    @Override
    protected void step11LoadBreastMarkers() {

    }

    // HCI uses common implementation Steps  step12CreateModels default


    @Override
    protected void step13LoadSpecimens() {

        dto.getModelCreation().addRelatedSample(dto.getPatientSample());
        dto.getModelCreation().addGroup(dto.getProjectGroup());

        dataImportService.saveSample(dto.getPatientSample());
        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());

        EngraftmentSite engraftmentSite = dataImportService.getImplantationSite(dto.getImplantationSiteStr());
        EngraftmentType engraftmentType = dataImportService.getImplantationType(dto.getImplantationtypeStr());

        // uggh parse strains
        ArrayList<HostStrain> strainList= new ArrayList();
        String strains = dto.getStrain();
        if(strains.contains(" and ")){
            strainList.add(dto.getNodScidGamma());
            strainList.add(dto.getNodScid());
        }else if(strains.contains("gamma")){
            strainList.add(dto.getNodScid());
        }else{
            strainList.add(dto.getNodScid());
        }

        int count = 0;
        for(HostStrain strain : strainList){
            count++;
            Specimen specimen = new Specimen();
            specimen.setExternalId(dto.getModelID()+"-"+count);
            specimen.setEngraftmentSite(engraftmentSite);
            specimen.setEngraftmentType(engraftmentType);
            specimen.setHostStrain(strain);

            Sample specSample = new Sample();
            specSample.setSourceSampleId(dto.getModelID()+"-"+count);
            specimen.setSample(specSample);

            dto.getModelCreation().addSpecimen(specimen);
            dto.getModelCreation().addRelatedSample(specSample);
            dataImportService.saveSpecimen(specimen);
        }
        dataImportService.saveModelCreation(dto.getModelCreation());
    }




    @Override
    protected void step14LoadPatientTreatments() {


    }




    @Override
    protected void step15LoadImmunoHistoChemistry() {

        String ihcFileStr = finderRootDir + dataSourceAbbreviation + "/ihc/ihc.txt";

        File file = new File(ihcFileStr);

        if (file.exists()) {

            Platform pl = dataImportService.getPlatform("ImmunoHistoChemistry", dto.getProviderGroup());

            String currentLine = "";
            int currentLineCounter = 1;
            String[] row;

            Map<String, MolecularCharacterization> molCharMap = new HashMap<>();

            try {
                BufferedReader buf = new BufferedReader(new FileReader(ihcFileStr));

                while (true) {
                    currentLine = buf.readLine();
                    if (currentLine == null) {
                        break;
                        //skip the first two rows
                    } else if (currentLineCounter < 3) {
                        currentLineCounter++;
                        continue;

                    } else {
                        row = currentLine.split("\t");

                        if (row.length > 0) {

                            String modelId = row[0];
                            String sampleId = row[1];
                            String markerSymbol = row[2];
                            String result = row[3];
                            //System.out.println(modelId);

                            if (modelId.isEmpty() || sampleId.isEmpty() || markerSymbol.isEmpty() || result.isEmpty())
                                continue;

                            NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSource, modelId, markerSymbol, "cytogenetics","ImmunoHistoChemistry");
                            Marker marker = null;


                            if(nsdto.getNode() == null){

                                //uh oh, we found an unrecognised marker symbol, abort, abort!!!!
                                reportManager.addMessage(nsdto.getLogEntity());
                                continue;
                            }
                            else{

                                //we have a marker node, check message

                                marker = (Marker)nsdto.getNode();

                                if(nsdto.getLogEntity() != null){
                                    reportManager.addMessage(nsdto.getLogEntity());
                                }


                                if (molCharMap.containsKey(modelId + "---" + sampleId)) {

                                    MolecularCharacterization mc = molCharMap.get(modelId + "---" + sampleId);


                                    MarkerAssociation ma = new MarkerAssociation();
                                    ma.setCytogeneticsResult(result);
                                    ma.setMarker(marker);
                                    mc.addMarkerAssociation(ma);
                                }
                                else {

                                    MolecularCharacterization mc = new MolecularCharacterization();
                                    mc.setType("cytogenetics");
                                    mc.setPlatform(pl);

                                    MarkerAssociation ma = new MarkerAssociation();
                                    ma.setCytogeneticsResult(result);
                                    ma.setMarker(marker);
                                    mc.addMarkerAssociation(ma);

                                    molCharMap.put(modelId + "---" + sampleId, mc);
                                }



                            }



                        }


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(currentLineCounter + " " + currentLine.toString());
            }

            //System.out.println(molCharMap.toString());

            for (Map.Entry<String, MolecularCharacterization> entry : molCharMap.entrySet()) {
                String key = entry.getKey();
                MolecularCharacterization mc = entry.getValue();

                String[] modAndSamp = key.split("---");
                String modelId = modAndSamp[0];
                String sampleId = modAndSamp[1];

                //Sample sample = dataImportService.findMouseSampleWithMolcharByModelIdAndDataSourceAndSampleId(modelId, hciDS.getAbbreviation(), sampleId);
                Sample sample = dataImportService.findHumanSampleWithMolcharByModelIdAndDataSource(modelId, dto.getProviderGroup().getAbbreviation());

                if (sample == null) {
                    log.warn("Missing model or sample: " + modelId + " " + sampleId);
                    continue;
                }

                sample.addMolecularCharacterization(mc);
                dataImportService.saveSample(sample);

            }

        } else {

            log.warn("Skipping loading IHC for HCI");
        }

    }



    @Override
    protected void step16LoadVariationData() { }


    @Override
    void step17LoadModelDosingStudies() throws Exception {

        loadModelDosingStudies();
    }

    @Override
    void addAccessModality() {}




}
