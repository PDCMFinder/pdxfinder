package org.pdxfinder.commands;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.neo4j.ogm.session.Session;
import org.pdxfinder.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.ds.Standardizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Load data from HCI PDXNet.
 */
@Component
@Order(value = 0)
public class LoadHCI implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(LoadHCI.class);

    private final static String HCI_DATASOURCE_ABBREVIATION = "PDXNet-HCI-BCM";
    private final static String HCI_DATASOURCE_NAME = "HCI-Baylor College of Medicine";
    private final static String HCI_DATASOURCE_DESCRIPTION = "HCI BCM PDX mouse models for PDXNet.";
    private final static String DATASOURCE_CONTACT = "Alana.Welm@hci.utah.edu";

    private final static String NSG_BS_NAME = "NOD scid gamma";
    private final static String NSG_BS_SYMBOL = "NOD.Cg-Prkdc<sup>scid</sup> Il2rg<sup>tm1Wjl</sup>/SzJ"; //yay HTML in name
    private final static String NSG_BS_URL = "http://jax.org/strain/005557";

    private final static String NS_BS_NAME = "NOD scid";
    private final static String NS_BS_SYMBOL = "NOD.CB17-Prkd<sup>cscid</sup>/J"; //yay HTML in name
    private final static String NS_BS_URL = "https://www.jax.org/strain/001303";

    private final static String SOURCE_URL = null;

    // for now all samples are of tumor tissue
    private final static Boolean NORMAL_TISSUE_FALSE = false;

    private final static String NOT_SPECIFIED = Standardizer.NOT_SPECIFIED;

    private HostStrain nsgBS, nsBS;
    private ExternalDataSource hciDS;

    private Options options;
    private CommandLineParser parser;
    private CommandLine cmd;
    private HelpFormatter formatter;

    private DataImportService dataImportService;
    private Session session;

    @Value("${hcipdx.url}")
    private String urlStr;

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

            log.info("Loading Huntsman PDX data.");

            if (urlStr != null) {
                log.info("Loading from URL " + urlStr);
                parseJSON(parseURL(urlStr));
            } else {
                log.error("No hcipdx.url provided in properties");
            }
        }
    }

    private void parseJSON(String json) {

        hciDS = dataImportService.getExternalDataSource(HCI_DATASOURCE_ABBREVIATION, HCI_DATASOURCE_NAME, HCI_DATASOURCE_DESCRIPTION,DATASOURCE_CONTACT, SOURCE_URL);
        nsgBS = dataImportService.getHostStrain(NSG_BS_NAME, NSG_BS_SYMBOL, NSG_BS_URL, NSG_BS_NAME);
        nsBS = dataImportService.getHostStrain(NS_BS_NAME, NS_BS_SYMBOL, NS_BS_URL, NS_BS_NAME);

        try {
            JSONObject job = new JSONObject(json);
            JSONArray jarray = job.getJSONArray("HCI");

            for (int i = 0; i < jarray.length(); i++) {

                JSONObject j = jarray.getJSONObject(i);

                createGraphObjects(j);
            }

        } catch (Exception e) {
            log.error("Error getting HCI PDX models", e);

        }
    }

    @Transactional
    void createGraphObjects(JSONObject j) throws Exception {
        String modelID = j.getString("Model ID");
        String sampleID = j.getString("Sample ID");
        String diagnosis = j.getString("Clinical Diagnosis");
       

        String classification = j.getString("Stage") + "/" + j.getString("Grades");

        String age = Standardizer.getAge(j.getString("Age"));
        String gender = Standardizer.getGender(j.getString("Gender"));
        
        PatientSnapshot pSnap = dataImportService.getPatientSnapshot(j.getString("Patient ID"),
                gender, "", j.getString("Ethnicity"), age, hciDS);

        String tumorType = Standardizer.getTumorType(j.getString("Tumor Type"));
        
        String sampleSite = Standardizer.getValue("Sample Site",j);

        Sample sample = dataImportService.getSample(sampleID, tumorType, diagnosis,
                j.getString("Primary Site"), sampleSite,
                j.getString("Sample Type"), classification, NORMAL_TISSUE_FALSE, hciDS.getAbbreviation());

        List<ExternalUrl> externalUrls = new ArrayList<>();
        externalUrls.add(dataImportService.getExternalUrl(ExternalUrl.Type.CONTACT, DATASOURCE_CONTACT));

        pSnap.addSample(sample);
        
        /*
        I guess this is considered IHC and not markers
        if(j.has("Markers") ){
            Set<MolecularCharacterization> mcs = new HashSet<>();
            JSONArray markers = j.getJSONArray("Markers");
            
            MolecularCharacterization molC = new MolecularCharacterization();
            molC.setTechnology("IHC");
            Set<MarkerAssociation> markerAssocs = new HashSet();
            for(int i =0; i< markers.length(); i++){
                JSONObject job = markers.getJSONObject(i);
                Platform pl = loaderUtils.getPlatform(job.getString("Platform"), hciDS);
                
                //all are IHC so this is ok to do over and over again
                molC.setPlatform(pl);
                

                Marker m = loaderUtils.getMarker(job.getString("Marker"), job.getString("Marker"));
                MarkerAssociation ma = new MarkerAssociation();
                ma.setMarker(m);
                ma.setImmunoHistoChemistryResult(job.getString("Association"));
                markerAssocs.add(ma);
                
               
                }
            molC.setMarkerAssociations(markerAssocs);
            mcs.add(molC);
            sample.setMolecularCharacterizations(mcs);
        
        }
        */
        
        
        // This multiple QA approach only works because Note and Passage are the same for all QAs
        QualityAssurance qa = new QualityAssurance(Standardizer.NOT_SPECIFIED,Standardizer.NOT_SPECIFIED,Standardizer.NOT_SPECIFIED);
        
        StringBuilder technology = new StringBuilder();
        if(j.has("QA")){
            JSONArray qas = j.getJSONArray("QA");
            for (int i = 0; i < qas.length(); i++) {
                if (qas.getJSONObject(i).getString("Technology").equalsIgnoreCase("histology")) {
                    qa.setTechnology(qas.getJSONObject(i).getString("Technology"));
                    qa.setDescription(qas.getJSONObject(i).getString("Note"));
                    qa.setPassages(qas.getJSONObject(i).getString("Passage"));
                }
            }
        }
        

        ModelCreation modelCreation = dataImportService.createModelCreation(modelID, this.hciDS.getAbbreviation(), sample, qa, externalUrls);
        modelCreation.addRelatedSample(sample);

        

        dataImportService.saveSample(sample);
        dataImportService.savePatientSnapshot(pSnap);

        String implantationTypeStr = Standardizer.getValue("Implantation Type", j);
        String implantationSiteStr = Standardizer.getValue("Engraftment Site", j);
        EngraftmentSite engraftmentSite = dataImportService.getImplantationSite(implantationSiteStr);
        EngraftmentType engraftmentType = dataImportService.getImplantationType(implantationTypeStr);
        
        // uggh parse strains
        ArrayList<HostStrain> strainList= new ArrayList();
        String strains = j.getString("Strain");
        if(strains.contains(" and ")){
            strainList.add(nsgBS);
            strainList.add(nsBS);
        }else if(strains.contains("gamma")){
            strainList.add(nsgBS);
        }else{
            strainList.add(nsBS);
        }
        
        int count = 0;
        for(HostStrain strain : strainList){
            count++;
            Specimen specimen = new Specimen();
            specimen.setExternalId(modelID+"-"+count);
            specimen.setEngraftmentSite(engraftmentSite);
            specimen.setEngraftmentType(engraftmentType);
            specimen.setHostStrain(strain);
            
             Sample specSample = new Sample();
             specSample.setSourceSampleId(modelID+"-"+count);
             specimen.setSample(specSample);
            
            modelCreation.addSpecimen(specimen);
            modelCreation.addRelatedSample(specSample);
            dataImportService.saveSpecimen(specimen);
        }
        dataImportService.saveModelCreation(modelCreation);
        
        
        TreatmentSummary ts;


        try{
            if(j.has("Treatments")){
                JSONObject treatment = j.optJSONObject("Treatments");
                //if the treatment attribute is not an object = it is an array
                if(treatment == null && j.optJSONArray("Treatments") != null){

                    JSONArray treatments = j.getJSONArray("Treatments");

                    if(treatments.length() > 0){
                        //log.info("Treatments found for model "+mc.getSourcePdxId());
                        ts = new TreatmentSummary();

                        for(int t = 0; t<treatments.length(); t++){
                            JSONObject treatmentObject = treatments.getJSONObject(t);

                            TreatmentProtocol tp = Standardizer.getTreatmentProtocol(treatmentObject.getString("Drug"));
                            tp.setDose(treatmentObject.getString("Dose") );

                            Response r = new Response();
                            r.setDescription(treatmentObject.getString("Response"));

                            tp.setResponse(r);
                            ts.addTreatmentProtocol(tp);
                        }

                        ts.setModelCreation(modelCreation);
                        modelCreation.setTreatmentSummary(ts);
                    }
                }

            }

            dataImportService.saveModelCreation(modelCreation);

        }
        catch(Exception e){

            e.printStackTrace();
        }
        
        
    }
    


    private String parseURL(String urlStr) {
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            log.error("Unable to read from MD Anderson JSON URL " + urlStr, e);
        }
        return sb.toString();
    }

}
