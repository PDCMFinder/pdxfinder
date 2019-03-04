package org.pdxfinder.commands.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.HelpFormatter;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
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
public class LoadHCI extends LoaderBase implements CommandLineRunner {


    private final static Logger log = LoggerFactory.getLogger(LoadHCI.class);

    private final static String DATASOURCE_ABBREVIATION = "PDXNet-HCI-BCM";
    private final static String DATASOURCE_NAME = "HCI-Baylor College of Medicine";
    private final static String DATASOURCE_DESCRIPTION = "HCI BCM PDX mouse models for PDXNet.";
    private final static String DATASOURCE_CONTACT = "Alana.Welm@hci.utah.edu";
    private final static String PROVIDER_TYPE = "";
    private final static String ACCESSIBILITY = "";

    private final static String NSG_BS_NAME = "NOD scid gamma";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    private final static String NS_BS_NAME = "NOD scid";
    private final static String NS_BS_SYMBOL = "NOD.CB17-Prkd<sup>cscid</sup>/J"; //yay HTML in name
    private final static String NS_BS_URL = "https://www.jax.org/strain/001303";

    private final static String DOSING_STUDY_URL = "/platform/hci-drug-dosing/";

    private final static String SOURCE_URL = null;

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    private HelpFormatter formatter;

    private DataImportService dataImportService;

    @Value("${pdxfinder.data.root.dir}")
    private String dataRootDir;

    @PostConstruct
    public void init() {
        formatter = new HelpFormatter();
    }

    public LoadHCI(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
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

            loaderTemplate();

        }
    }



    @Override
    protected void initMethod() {

        log.info("Loading Huntsman PDX data.");

        dto = new LoaderDTO();

        jsonFile = dataRootDir + DATASOURCE_ABBREVIATION + "/pdx/models.json";
        dataSource = DATASOURCE_ABBREVIATION;
        filesDirectory = "";
        dataSourceAbbreviation = DATASOURCE_ABBREVIATION;
        dataSourceContact = DATASOURCE_CONTACT;
        dosingStudyURL = DOSING_STUDY_URL;
    }

    @Override
    void step00StartReportManager() { }

    @Override
    protected void step01GetMetaDataFolder() { }


    @Override
    protected void step03CreateProviderGroup() {

        loadProviderGroup(DATASOURCE_NAME, DATASOURCE_ABBREVIATION, DATASOURCE_DESCRIPTION, PROVIDER_TYPE, ACCESSIBILITY, null, DATASOURCE_CONTACT, SOURCE_URL);
    }


    @Override
    protected void step04CreateNSGammaHostStrain() {

        loadNSGammaHostStrain(NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME, NSG_BS_NAME);
    }


    @Override
    protected void step05CreateNSHostStrain() {

        loadNSHostStrain(NS_BS_SYMBOL, NS_BS_URL, NS_BS_NAME);
    }


    @Override
    protected void step06CreateProjectGroup() {

        loadProjectGroup("PDXNet");
    }


    @Override
    protected void step07GetPDXModels() {

        loadPDXModels(metaDataJSON,"HCI");
    }


    // HCI uses common implementation Steps s step08GetMetaData,step09LoadPatientData default


    @Override
    protected void step10LoadExternalURLs() {

        loadExternalURLs(DATASOURCE_CONTACT,Standardizer.NOT_SPECIFIED);
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
    protected void step14LoadCurrentTreatment() {

        loadCurrentTreatment();

    }




    @Override
    protected void step15LoadImmunoHistoChemistry() {

        String ihcFileStr = dataRootDir + DATASOURCE_ABBREVIATION + "/ihc/ihc.txt";

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
                            String samleId = row[1];
                            String marker = row[2];
                            String result = row[3];
                            //System.out.println(modelId);

                            if (modelId.isEmpty() || samleId.isEmpty() || marker.isEmpty() || result.isEmpty())
                                continue;

                            if (molCharMap.containsKey(modelId + "---" + samleId)) {

                                MolecularCharacterization mc = molCharMap.get(modelId + "---" + samleId);
                                Marker m = dataImportService.getMarker(marker);

                                MarkerAssociation ma = new MarkerAssociation();
                                ma.setImmunoHistoChemistryResult(result);
                                ma.setMarker(m);
                                mc.addMarkerAssociation(ma);
                            } else {

                                MolecularCharacterization mc = new MolecularCharacterization();
                                mc.setType("IHC");
                                mc.setPlatform(pl);


                                Marker m = dataImportService.getMarker(marker);
                                MarkerAssociation ma = new MarkerAssociation();
                                ma.setImmunoHistoChemistryResult(result);
                                ma.setMarker(m);
                                mc.addMarkerAssociation(ma);

                                molCharMap.put(modelId + "---" + samleId, mc);
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








}
