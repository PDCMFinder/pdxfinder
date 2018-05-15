package org.pdxfinder.commands;

import com.google.gson.Gson;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.ds.ModelForQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/*
 * Created by csaba on 09/03/2018.
 */
@Component
@Order(value = 99)
public class CreateDataProjections implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(CreateDataProjections.class);
    private DataImportService dataImportService;

    @Value("${user.home}")
    String homeDir;

    //"platform"=>"marker"=>"variant"=>"set of model ids"
    private Map<String, Map<String, Map<String, Set<Long>>>> mutatedPlatformMarkerVariantModelDP = new HashMap<>();

    //"marker"=>"set of variants"
    private Map<String, Set<String>> mutatedMarkerVariantDP = new HashMap<>();

    private List<ModelForQuery> modelForQueryDP = new ArrayList<>();

    //"platform"=>"marker"=>"set of model ids"
    private Map<String, Map<String, Set<Long>>> wtMarkersDataProjection = new HashMap<>();


    @Autowired
    public CreateDataProjections(DataImportService dataImportService) {
        this.dataImportService = dataImportService;
    }

    @Override
    public void run(String... args) throws Exception {

        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();
        parser.accepts("createDataProjections", "Creating data projections");
        parser.accepts("loadALL", "Load all, including linking samples to NCIT terms");
        parser.accepts("loadSlim", "Load slim, then link samples to NCIT terms");

        OptionSet options = parser.parse(args);
        long startTime = System.currentTimeMillis();

        if (options.has("createDataProjections") || options.has("loadALL")  || options.has("loadSlim")) {

            log.info("Creating data projections");

            createMutationDataProjection();

            createModelForQueryDataProjection();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int seconds = (int) (totalTime / 1000) % 60;
        int minutes = (int) ((totalTime / (1000 * 60)) % 60);

        log.info(this.getClass().getSimpleName() + " finished after " + minutes + " minute(s) and " + seconds + " second(s)");

    }




    private void createMutationDataProjection(){

        Collection<MolecularCharacterization> mutatedMolchars = dataImportService.findMolCharsByType("mutation");

        log.info("Looking at "+mutatedMolchars.size()+" MolChar objects. This may take a while folks...");

        int count = 0;
        for(MolecularCharacterization mc:mutatedMolchars){



            ModelCreation model = dataImportService.findModelByMolChar(mc);

            Long modelId = model.getId();

            String platformName = "Not Specified";

            if(mc.getPlatform() != null && mc.getPlatform().getName() != null && !mc.getPlatform().getName().isEmpty()){

                platformName = mc.getPlatform().getName();
            }

            Set<MarkerAssociation> mas = dataImportService.findMarkerAssocsByMolChar(mc);

            if(mas != null){


                for(MarkerAssociation ma: mas){


                    Marker m = ma.getMarker();

                    if(m != null){

                        String variantName = ma.getAminoAcidChange();
                        String markerName = m.getName();

                        if(variantName != null && !variantName.isEmpty() && markerName != null && !markerName.isEmpty()){


                            addToMutatedPlatformMarkerVariantModelDP(platformName, markerName, variantName, modelId);
                            addToMutatedMarkerVariantDP(markerName, variantName);

                        }

                    }
                    count++;
                    if(count%10000 == 0) {log.info("Processed "+count+" MA objects");}
                    //if (count > 40000) break;
                }

            }

        }



        log.info("Saving DataProjection");

        DataProjection pmvmDP = dataImportService.findDataProjectionByLabel("PlatformMarkerVariantModel");

        if (pmvmDP == null){

            pmvmDP = new DataProjection();
            pmvmDP.setLabel("PlatformMarkerVariantModel");
        }

        DataProjection mvDP = dataImportService.findDataProjectionByLabel("MarkerVariant");

        if(mvDP == null){

            mvDP = new DataProjection();
            mvDP.setLabel("MarkerVariant");
        }

        JSONObject j1 ,j2;

        try{

            j1 = new JSONObject(this.mutatedPlatformMarkerVariantModelDP.toString());
            j2 = new JSONObject(this.mutatedMarkerVariantDP.toString());

            pmvmDP.setValue(j1.toString());
            mvDP.setValue(j2.toString());

        }
        catch(Exception e){

            e.printStackTrace();
        }


        dataImportService.saveDataProjection(pmvmDP);
        dataImportService.saveDataProjection(mvDP);

        /*
        try {
            Files.write(Paths.get(homeDir + "/PDX/mutated.json"), createJsonString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

    }

    /**
     *
     * Adds platforms, markers, variants and models to a nested structure
     * "platform"=>"marker"=>"variation"=>"set of model ids"
     * Map<String, Map<String, Map<String, Set<String>>>>
     *
     * @param platformName
     * @param markerName
     * @param variantName
     * @param modelId
     */
    private void addToMutatedPlatformMarkerVariantModelDP(String platformName, String markerName, String variantName, Long modelId){

        if(this.mutatedPlatformMarkerVariantModelDP.containsKey(platformName)){

            if(this.mutatedPlatformMarkerVariantModelDP.get(platformName).containsKey(markerName)){

                if(this.mutatedPlatformMarkerVariantModelDP.get(platformName).get(markerName).containsKey(variantName)){

                    this.mutatedPlatformMarkerVariantModelDP.get(platformName).get(markerName).get(variantName).add(modelId);
                }
                //platform and marker is there, variant is missing
                else{

                    Set<Long> models = new HashSet<>(Arrays.asList(modelId));

                    this.mutatedPlatformMarkerVariantModelDP.get(platformName).get(markerName).put(variantName, models);
                }
            }
            //platform is there, marker is missing
            else{

                Set<Long> models = new HashSet<>(Arrays.asList(modelId));

                Map<String, Set<Long>> variants = new HashMap<>();
                variants.put(variantName, models);

                this.mutatedPlatformMarkerVariantModelDP.get(platformName).put(markerName, variants);
            }
        }
        //if the platform is missing, combine all keys
        else{

            Set<Long> models = new HashSet<>(Arrays.asList(modelId));

            Map<String, Set<Long>> variants = new HashMap<>();
            variants.put(variantName, models);

            Map<String, Map<String, Set<Long>>> markers = new HashMap<>();
            markers.put(markerName, variants);

            this.mutatedPlatformMarkerVariantModelDP.put(platformName, markers);
        }

    }


    /**
     * Inserts a variant for a marker
     *
     * @param markerName
     * @param variantName
     */
    private void addToMutatedMarkerVariantDP(String markerName, String variantName){

        if(this.mutatedMarkerVariantDP.containsKey(markerName)){

            this.mutatedMarkerVariantDP.get(markerName).add(variantName);
        }
        else{

            Set s = new HashSet();
            s.add(variantName);

            this.mutatedMarkerVariantDP.put(markerName, s);
        }

    }



    private void createModelForQueryDataProjection(){


        // Get out all platforms for all models and populate a map with the results
        Map<Long, List<String>> platformsByModel = new HashMap<>();
        Collection<ModelCreation> allModelsPlatforms = dataImportService.findAllModelsPlatforms();
        for (ModelCreation mc : allModelsPlatforms) {

            if (!platformsByModel.containsKey(mc.getId())) {
                platformsByModel.put(mc.getId(), new ArrayList<>());
            }

            // Are there any molecular characterizations associated to this model?
            if (mc.getRelatedSamples().stream().map(Sample::getMolecularCharacterizations).mapToLong(Collection::size).sum() > 0) {

                // Get all molecular characterizations platforms into a list
                platformsByModel.get(mc.getId()).addAll(
                        mc.getRelatedSamples().stream()
                                .map(Sample::getMolecularCharacterizations)
                                .flatMap(Collection::stream)
                                .map(x ->  {
                                    if (dataImportService.countMarkerAssociationBySourcePdxId(mc.getSourcePdxId(), x.getPlatform().getName()) != 0) {
                                        return x.getPlatform().getName();
                                    } else {
                                        return " ";
                                    }
                                })
                                .distinct()
                                .collect(Collectors.toList()));
            }
        }

        Map<String, String> cancerSystemMap = new HashMap<>();
        // Mapping NCIT ontology term labels to display labels
        cancerSystemMap.put("Breast Cancer", "Breast Cancer");
        cancerSystemMap.put("Cardiovascular Cancer", "Cardiovascular Cancer");
        cancerSystemMap.put("Connective and Soft Tissue Neoplasm", "Connective and Soft Tissue Cancer");
        cancerSystemMap.put("Digestive System Cancer", "Digestive System Cancer");
        cancerSystemMap.put("Endocrine Cancer", "Endocrine Cancer");
        cancerSystemMap.put("Eye Cancer", "Eye Cancer");
        cancerSystemMap.put("Head and Neck Cancer", "Head and Neck Cancer");
        cancerSystemMap.put("Hematopoietic and Lymphoid System Neoplasm", "Hematopoietic and Lymphoid System Cancer");
        cancerSystemMap.put("Nervous System Cancer", "Nervous System Cancer");
        cancerSystemMap.put("Peritoneal and Retroperitoneal Neoplasms", "Peritoneal and Retroperitoneal Cancer");
        cancerSystemMap.put("Reproductive System Neoplasm", "Reproductive System Cancer");
        cancerSystemMap.put("Respiratory Tract Cancer", "Respiratory Tract Cancer");
        cancerSystemMap.put("Thoracic Neoplasm", "Thoracic Cancer");
        cancerSystemMap.put("Skin Cancer", "Skin Cancer");
        cancerSystemMap.put("Urinary System Cancer", "Urinary System Cancer");
        cancerSystemMap.put("Unclassified", "Unclassified");

        log.info("Creating ModelForQuery DataProjection");

        for (ModelCreation mc : dataImportService.findModelsWithPatientData()) {

            ModelForQuery mfq = new ModelForQuery();
            mfq.setModelId(mc.getId());
            mfq.setExternalId(mc.getSourcePdxId());
            mfq.setDatasource(mc.getDataSource());


            List<String> dataAvailable = new ArrayList<>();

            if(platformsByModel.containsKey(mc.getId())){

                for(String available : platformsByModel.get(mc.getId())){
                    dataAvailable.add("Mutation_"+available);
                }
            }

            if(dataImportService.isTreatmentSummaryAvailable(mc.getDataSource(), mc.getSourcePdxId())){
                dataAvailable.add("Dosing Studies");
            }

            mfq.setDataAvailable(dataAvailable);

            if (mc.getSample().getPatientSnapshot().getTreatmentNaive() != null) {
                mfq.setTreatmentHistory(mc.getSample().getPatientSnapshot().getTreatmentNaive().toString());
            } else {
                mfq.setTreatmentHistory("Not Specified");
            }

            if (mc.getSample().getSampleSite() != null) {
                mfq.setSampleSampleSite(mc.getSample().getSampleSite().getName());
            } else {
                mfq.setSampleSampleSite("Not Specified");
            }

            if (mc.getSample().getType() != null) {
                mfq.setSampleTumorType(mc.getSample().getType().getName());
            } else {
                mfq.setSampleTumorType("Not Specified");
            }

            if (mc.getSample().getSampleSite() != null) {
                mfq.setSampleSampleSite(mc.getSample().getSampleSite().getName());
            } else {
                mfq.setSampleSampleSite("Not Specified");
            }

            // Patient information
            mfq.setPatientAge(mc.getSample().getPatientSnapshot().getAgeBin());
            mfq.setPatientGender(mc.getSample().getPatientSnapshot().getPatient().getSex());
            mfq.setDiagnosis(mc.getSample().getDiagnosis());
            mfq.setMappedOntologyTerm(mc.getSample().getSampleToOntologyRelationShip().getOntologyTerm().getLabel());

            if (mc.getSample().getPatientSnapshot().getTreatmentNaive() != null) {
                mfq.setPatientTreatmentStatus(mc.getSample().getPatientSnapshot().getTreatmentNaive().toString());
            }

            // Sample information
            mfq.setSampleExtractionMethod(mc.getSample().getExtractionMethod());
            mfq.setSampleOriginTissue(mc.getSample().getOriginTissue().getName());
            mfq.setSampleClassification(mc.getSample().getClassification());

            if (mc.getSample().getType() != null) {
                mfq.setSampleTumorType(mc.getSample().getType().getName());
            }
            // Model information
            Set<Specimen> specimens = mc.getSpecimens();
            Set<String> hoststrains = new HashSet<>();
            if (specimens != null && specimens.size() > 0) {

                for (Specimen s: specimens){
                    hoststrains.add(s.getHostStrain().getName());

                    mfq.setModelImplantationSite(s.getEngraftmentSite().getName());
                    mfq.setModelImplantationType(s.getEngraftmentType().getName());
                }
                //Specimen s = specimens.iterator().next();
                mfq.setModelHostStrain(hoststrains);
            }

            // Get all ancestor ontology terms (including self) into a set specific for this model
            Set<OntologyTerm> allOntologyTerms = new HashSet<>();

            // Add direct mapped term
            allOntologyTerms.add(mc.getSample().getSampleToOntologyRelationShip().getOntologyTerm());

            // Add all ancestors of direct mapped term
            for (OntologyTerm t : mc.getSample().getSampleToOntologyRelationShip().getOntologyTerm().getSubclassOf()) {
                allOntologyTerms.addAll(getAllAncestors(t));
            }

            mfq.setAllOntologyTermAncestors(allOntologyTerms.stream().map(OntologyTerm::getLabel).collect(Collectors.toSet()));

            // Add all top level systems (translated) to the Model
            for (String s : allOntologyTerms.stream().map(OntologyTerm::getLabel).collect(Collectors.toSet())) {

                if (cancerSystemMap.keySet().contains(s)) {

                    if (mfq.getCancerSystem() == null) {
                        mfq.setCancerSystem(new ArrayList<>());
                    }

                    mfq.getCancerSystem().add(cancerSystemMap.get(s));

                }
            }

            // Ensure that ALL models have a system -- even if it's not in the ontology nodes specified
            if (mfq.getCancerSystem() == null || mfq.getCancerSystem().size() == 0) {
                if (mfq.getCancerSystem() == null) {
                    mfq.setCancerSystem(new ArrayList<>());
                }

                mfq.getCancerSystem().add(cancerSystemMap.get("Unclassified"));

            }

            // TODO: Complete the organ options
            // TODO: Complete the cell type options
            // TODO: Complete the patient treatment options


            this.modelForQueryDP.add(mfq);
        }



        log.info("Saving ModelForQuery DataProjection");

        DataProjection mfqDP = dataImportService.findDataProjectionByLabel("ModelForQuery");

        if (mfqDP == null){

            mfqDP = new DataProjection();
            mfqDP.setLabel("ModelForQuery");
        }


        Gson gson = new Gson();
        String jsonMfqDP = gson.toJson(this.modelForQueryDP);
        mfqDP.setValue(jsonMfqDP);
        dataImportService.saveDataProjection(mfqDP);


    }


    /**
     * Recursively get all ancestors starting from the supplied ontology term
     *
     * @param t the starting term in the ontology
     * @return a set of ontology terms corresponding to the ancestors of the term supplied
     */
    public Set<OntologyTerm> getAllAncestors(OntologyTerm t) {

        Set<OntologyTerm> retSet = new HashSet<>();

        // Store this ontology term in the set
        retSet.add(t);

        // If this term has parent terms
        if (t.getSubclassOf() != null && t.getSubclassOf().size() > 0) {

            // For each parent term
            for (OntologyTerm st : t.getSubclassOf()) {

                // Recurse and add all ancestor terms to the set
                retSet.addAll(getAllAncestors(st));
            }
        }

        // Return the full set
        return retSet;
    }


    private String createJsonString(Object jstring){

        try{

            JSONObject json = new JSONObject(jstring);
            return json.toString();
        }
        catch(Exception e){

            e.printStackTrace();
        }

        return "";
    }


}
