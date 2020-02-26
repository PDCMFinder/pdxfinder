package org.pdxfinder.dataloaders;


import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Standardizer;
import org.pdxfinder.services.dto.LoaderDTO;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "jax")
public class LoadJAXData extends LoaderBase {

    private final static Logger log = LoggerFactory.getLogger(LoadJAXData.class);

    @Value("${jaxpdx.variation.max}")
    private int maxVariations;

    @Value("${jaxpdx.ref.assembly}")
    private String refAssembly;

    @Value("${data-dir}")
    private String finderRootDir;

    HashMap<String, String> passageMap = null;

    Map<String, Platform> platformMap = new HashMap<>();

    public LoadJAXData(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    public void run() throws Exception {

        initMethod();
        globalLoadingOrder();
    }



    @Override
    protected void initMethod() {

        log.info("Loading JAX PDX data.");

        dto = new LoaderDTO();

        jsonFile = finderRootDir + "/data/" + dataSourceAbbreviation+"/pdx/models.json";
        dataSource = dataSourceAbbreviation;

        platformURL = new HashMap<>();
        platformURL.put("CTP_mutation","/platform/jax-ctp/");
        platformURL.put("Truseq_JAX_mutation","/platform/jax-truseq/");
        platformURL.put("Whole_Exome_mutation","/platform/jax-whole-exome/");
    }



    @Override
    protected void step01GetMetaDataFolder() {

    }



    // JAX uses default implementation Steps step02GetMetaDataJSON


    @Override
    protected void step05CreateNSHostStrain() {

    }


    @Override
    protected void step06SetProjectGroup() {

    }


    // JAX uses default implementation Steps step08GetMetaData



    @Override
    protected void step09LoadPatientData() {
        if(dto.isSkipModel()) return ;
        dto.setHistologyMap(getHistologyImageMap(dto.getModelID()));

        //Check if model exists in DB, if yes, do not load duplicates
        ModelCreation existingModel = dataImportService.findModelByIdAndDataSource(dto.getModelID(), dataSourceAbbreviation);
        if(existingModel != null) {
            log.error("Skipping existing model "+dto.getModelID());
            return;
        }
        // if the diagnosis is still unknown don't load it
        if(dto.getDiagnosis().toLowerCase().contains("unknown") ||
                dto.getDiagnosis().toLowerCase().contains("not specified")){
            log.info("Skipping model "+dto.getModelID()+" with diagnosis:"+dto.getDiagnosis());
            dto.setSkipModel(true);
            return;
        }

        super.step09LoadPatientData();
    }




    @Override
    protected void step10LoadExternalURLs() {
        if(dto.isSkipModel()) return ;
        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());

        loadExternalURLs(dataSourceContact+dto.getModelID(), dataSourceURL+dto.getModelID());
    }




    @Override
    protected void step11LoadBreastMarkers() {

        if(dto.isSkipModel()) return ;
        //create breast cancer markers manually if they are present
        if(!dto.getModelTag().equals(Standardizer.NOT_SPECIFIED)){

            if(dto.getModelTag().equals("Triple Negative Breast Cancer (TNBC)")){
                NodeSuggestionDTO nsdto;

                MolecularCharacterization mc = new MolecularCharacterization();
                mc.setPlatform(dataImportService.getPlatform("Not Specified", "cytogenetics", providerDS));
                mc.setType("cytogenetics");

                //we know these markers exist so no need to check for null
                nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, dto.getModelID(), "ERBB2", "cytogenetics", "ImmunoHistoChemistry");
                Marker her2 = (Marker) nsdto.getNode(); // ERBB2  dataImportService.getMarker("HER2", "HER2");

                nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, dto.getModelID(), "ESR1", "cytogenetics", "ImmunoHistoChemistry");
                Marker er = (Marker) nsdto.getNode(); //dataImportService.getMarker("ER", "ER");

                nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, dto.getModelID(), "PGR", "cytogenetics", "ImmunoHistoChemistry");
                Marker pr = (Marker) nsdto.getNode(); //dataImportService.getMarker("PR", "PR");

                MarkerAssociation her2a = new MarkerAssociation();
                her2a.setMarker(her2);
                her2a.setCytogeneticsResult("negative");

                MarkerAssociation era = new MarkerAssociation();
                era.setMarker(er);
                era.setCytogeneticsResult("negative");

                MarkerAssociation pra = new MarkerAssociation();
                pra.setMarker(pr);
                pra.setCytogeneticsResult("negative");

                mc.addMarkerAssociation(her2a);
                mc.addMarkerAssociation(era);
                mc.addMarkerAssociation(pra);

                dto.getPatientSample().addMolecularCharacterization(mc);
            }
        }

    }




    @Override
    protected void step12CreateModels() throws Exception  {
        if(dto.isSkipModel()) return ;
        // JAX - Updates Patient Sample b4 model Creation
        dto.getPatientSample().setExtractionMethod(dto.getExtractionMethod());

        if (dto.getHistologyMap().containsKey("Patient")) {
            Histology histology = new Histology();
            Image image = dto.getHistologyMap().get("Patient");
            histology.addImage(image);
            dto.getPatientSample().addHistology(histology);
        }

        super.step12CreateModels();

    }





    @Override
    protected void step13LoadSpecimens() {
        if(dto.isSkipModel()) return ;
        Specimen specimen = dataImportService.getSpecimen(dto.getModelCreation(), dto.getModelID(), providerDS.getAbbreviation(), "");
        specimen.setHostStrain(nsgBS);
        EngraftmentSite engraftmentSite = dataImportService.getImplantationSite(dto.getImplantationSiteStr());
        EngraftmentType engraftmentType = dataImportService.getImplantationType(Standardizer.NOT_SPECIFIED);
        specimen.setEngraftmentSite(engraftmentSite);
        specimen.setEngraftmentType(engraftmentType);

        dto.getModelCreation().addSpecimen(specimen);
        dataImportService.saveSpecimen(specimen);

        dto.setEngraftmentSite(engraftmentSite);
        dto.setEngraftmentType(engraftmentType);

    }




    @Override
    protected void step14LoadPatientTreatments() {


    }




    @Override
    protected void step15LoadImmunoHistoChemistry() {

    }


    @Override
    protected void step16LoadVariationData() {
        if(dto.isSkipModel()) return ;
        String dataDirectory = finderRootDir+"/data/"+dataSourceAbbreviation;

        loadOmicData(dto.getModelCreation(), providerDS,"mutation", dataDirectory);

        loadOmicData(dto.getModelCreation(), providerDS,"copy number alteration", dataDirectory);

        loadOmicData(dto.getModelCreation(), providerDS,"transcriptomics", dataDirectory);

    }


    @Override
    void step17LoadModelDosingStudies() throws Exception {
        if(dto.isSkipModel()) return ;
        loadModelDosingStudies();

    }

    @Override
    void step18SetAdditionalGroups() {
        throw new UnsupportedOperationException();
    }


    /*
    For a given model return a map of passage # or "Patient" -> histology image URL
     */
    private HashMap<String, Image> getHistologyImageMap(String id) {
        HashMap<String, Image> map = new HashMap<>();

            String histologyFile = finderRootDir +dataSourceAbbreviation+"/hist/"+id;
            File file = new File(histologyFile);

            if(file.exists()){
                try {
                    JSONObject job = new JSONObject(utilityService.parseFile(histologyFile));
                    JSONArray jarray = job.getJSONObject("pdxHistology").getJSONArray("Graphics");
                    String comment = job.getJSONObject("pdxHistology").getString("Comment");

                    for (int i = 0; i < jarray.length(); i++) {
                        job = jarray.getJSONObject(i);
                        String desc = job.getString("Description");

                        // comments apply to all of a models histology but histologies are passage specific
                        // so I guess attach the comment to all image descriptions
                        if (comment != null && comment.trim().length() > 0) {
                            String sep = "";
                            if (desc != null && desc.trim().length() > 0) {
                                sep = " : ";
                            }
                            desc = comment + sep + desc;
                        }

                        String url = job.getString("URL");
                        Image img = new Image();
                        img.setDescription(desc);
                        img.setUrl(url);
                        if (desc.startsWith("Patient") || desc.startsWith("Primary")) {
                            map.put("Patient", img);
                        } else {
                            String[] parts = desc.split(" ");
                            if (parts[0].startsWith("P")) {
                                try {
                                    String passage = new Integer(parts[0].replace("P", "")).toString();
                                    map.put(passage, img);
                                } catch (Exception e) {
                                    log.info("Can't extract passage from description " + desc);
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("Error getting histology for model " + id, e);
                }

            }




        return map;
    }



}
