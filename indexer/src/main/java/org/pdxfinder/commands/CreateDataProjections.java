package org.pdxfinder.commands;

import com.google.gson.Gson;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.DrugService;
import org.pdxfinder.services.ds.ModelForQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Created by csaba on 09/03/2018.
 */
@Component
@Order(value = 90)
public class CreateDataProjections implements CommandLineRunner{

    private final static Logger log = LoggerFactory.getLogger(CreateDataProjections.class);
    private DataImportService dataImportService;
    private DrugService drugService;

    @Value("${user.home}")
    String homeDir;

    //"platform"=>"marker"=>"variant"=>"set of model ids"
    private Map<String, Map<String, Map<String, Set<Long>>>> mutatedPlatformMarkerVariantModelDP = new HashMap<>();

    //"marker"=>"set of variants"
    private Map<String, Set<String>> mutatedMarkerVariantDP = new HashMap<>();

    private List<ModelForQuery> modelForQueryDP = new ArrayList<>();

    //"platform"=>"marker"=>"set of model ids"
    private Map<String, Map<String, Set<Long>>> wtMarkersDataProjection = new HashMap<>();

    //"drugname"=>"response"=>"set of model ids"
    private Map<String, Map<String, Set<Long>>> modelDrugResponseDP = new HashMap<>();

    //"platform"=>"markercombos"=>"set of model ids"
    private Map<String, Map<String, Set<Long>>> immunoHistoChemistryDP = new HashMap<>();

    @Autowired
    public CreateDataProjections(DataImportService dataImportService, DrugService drugService) {

        this.dataImportService = dataImportService;
        this.drugService = drugService;
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

            createModelDrugResponseDataProjection();

            createImmunoHistoChemistryDataProjection();

            saveDataProjections();

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

            Set<MarkerAssociation> mas = dataImportService.findMutationByMolChar(mc);

            if(mas != null){

                for(MarkerAssociation ma: mas){

                    Marker m = ma.getMarker();

                    if(m != null){

                        String variantName = ma.getAminoAcidChange();
                        String markerName = m.getName();

                        if(variantName != null && !variantName.isEmpty()  && markerName != null && !markerName.isEmpty()){

                            //this was needed to avoid issues with variants where the value was a single space " "
                            if(variantName.length()<3) variantName = "Not applicable";

                            addToMutatedMarkerVariantDP(markerName, variantName);

                            addToThreeParamDP(mutatedPlatformMarkerVariantModelDP, platformName, markerName, variantName, modelId);

                        }

                    }
                    count++;
                    if(count%10000 == 0) {log.info("Processed "+count+" MA objects");}
                    //if (count > 40000) break;
                }

            }

        }
    }


    private void createImmunoHistoChemistryDataProjection(){

        Collection<MolecularCharacterization> ihcMolchars = dataImportService.findMolCharsByType("IHC");
        log.info("Looking at "+ihcMolchars.size()+" IHC MolChar objects. This may take a while folks...");
        int count = 0;

        for(MolecularCharacterization mc:ihcMolchars){

            ModelCreation model = dataImportService.findModelByMolChar(mc);

            Long modelId = model.getId();

            String platformName = "Not Specified";

            if(mc.getPlatform() != null && mc.getPlatform().getName() != null && !mc.getPlatform().getName().isEmpty()){

                platformName = mc.getPlatform().getName();
            }

            Set<MarkerAssociation> mas = dataImportService.findMarkerAssocsByMolChar(mc);

            Set<String> markerSet = new HashSet<>();

            if(mas != null){

                for(MarkerAssociation ma: mas){

                    Marker m = ma.getMarker();

                    if(m != null){

                        String ihcResult = ma.getImmunoHistoChemistryResult();
                        String markerName = m.getName();
                        //log.info(ihcResult + markerName);
                        if(ihcResult != null && !ihcResult.isEmpty()  && markerName != null && !markerName.isEmpty()){

                            //this was needed to avoid issues with variants where the value was a single space " "
                            if(ihcResult.length()<3) ihcResult = "Not applicable";
                            if(ihcResult.toLowerCase().contains("pos")) ihcResult = "pos";
                            if(ihcResult.toLowerCase().contains("neg")) ihcResult = "neg";

                            if(ihcResult.equals("pos") || ihcResult.equals("neg") || ihcResult.equals("Not applicable")){

                                //discard markers that are not ER, HER2 or PR
                                if(markerName.toLowerCase().equals("er") || markerName.toLowerCase().equals("her2") || markerName.toLowerCase().equals("pr")) {
                                    markerSet.add(markerName + ihcResult);
                                }
                            }
                            else{
                                log.error("Found invalid ihcResult, skipping: "+ihcResult);
                            }


                        }
                    }
                    count++;
                    if(count%10000 == 0) {log.info("Processed "+count+" MA objects");}
                    //if (count > 40000) break;
                }
                List<String> markerList = new ArrayList<>(markerSet);
                Collections.sort(markerList);
                String markerResultCombo = markerList.stream().collect(Collectors.joining("_"));

                //log.info("Entry: "+platformName + markerResultCombo + modelId);
                if(markerResultCombo.split("_").length == 3){
                    addToTwoParamDP(immunoHistoChemistryDP, platformName, markerResultCombo, modelId);
                }
                else{
                    log.warn("Skipping "+markerResultCombo +". This is not a valid combination for the DP.");
                }


            }


        }

        //log.info(immunoHistoChemistryDP.toString());
    }


    private void addToTwoParamDP(Map<String, Map<String, Set<Long>>> collection, String key1, String key2, Long modelId){

        if(collection.containsKey(key1)){

            if(collection.get(key1).containsKey(key2)){
             collection.get(key1).get(key2).add(modelId);

            }
            else{

                Set<Long> set = new HashSet<>();
                set.add(modelId);
                collection.get(key1).put(key2, set);
            }
        }
        else{

            Set<Long> set = new HashSet<>();
            set.add(modelId);

            Map<String, Set<Long>> map = new HashMap<>();
            map.put(key2, set);

            collection.put(key1, map);
        }

    }


    private void addToThreeParamDP(Map<String, Map<String, Map<String, Set<Long>>>> collection, String key1, String key2, String key3, Long modelId){

        if(collection.containsKey(key1)){

            if(collection.get(key1).containsKey(key2)){

                if(collection.get(key1).get(key2).containsKey(key3)){

                    collection.get(key1).get(key2).get(key3).add(modelId);
                }
                else{

                    Set<Long> models = new HashSet<>(Arrays.asList(modelId));
                    collection.get(key1).get(key2).put(key3,models);
                }

            }
            else{

                Set<Long> models = new HashSet<>(Arrays.asList(modelId));

                Map<String, Set<Long>> map3 = new HashMap();
                map3.put(key3,models);

                collection.get(key1).put(key2, map3);

            }



        }
        else{

            Set<Long> models = new HashSet<>(Arrays.asList(modelId));

            Map<String, Set<Long>> map3 = new HashMap();
            map3.put(key3,models);

            Map map2 = new HashMap();
            map2.put(key2, map3);

            collection.put(key1, map2);
        }


    }

    private void addToFourParamDP(Map<String, Map<String, Map<String, Map<String, Set<Long>>>>> collection, String key1, String key2, String key3, String key4, Long modelId){

        if(collection.containsKey(key1)){

            if(collection.get(key1).containsKey(key2)){

                if(collection.get(key1).get(key2).containsKey(key3)){

                    if(collection.get(key1).get(key2).get(key3).containsKey(key4)){

                        collection.get(key1).get(key2).get(key3).get(key4).add(modelId);
                    }
                    else{

                        Set<Long> models = new HashSet<>(Arrays.asList(modelId));

                        collection.get(key1).get(key2).get(key3).put(key4, models);
                    }

                }
                else{

                    Set<Long> models = new HashSet<>(Arrays.asList(modelId));

                    Map<String, Set<Long>> map4 = new HashMap();
                    map4.put(key4, models);

                    collection.get(key1).get(key2).put(key3, map4);
                }

            }
            else{

                Set<Long> models = new HashSet<>(Arrays.asList(modelId));

                Map<String, Set<Long>> map4 = new HashMap();
                map4.put(key4, models);

                Map map3 = new HashMap();
                map3.put(key3, map4);

                collection.get(key1).put(key2, map3);
            }


        }
        else{

            Set<Long> models = new HashSet<>(Arrays.asList(modelId));

            Map<String, Set<Long>> map4 = new HashMap();
            map4.put(key4, models);

            Map map3 = new HashMap();
            map3.put(key3, map4);

            Map map2 = new HashMap();
            map2.put(key2, map3);

            collection.put(key1, map2);
        }


    }


    /**
     * Inserts a variant for a marker
     *
     * @param markerName
     * @param variantName
     */
    private void addToMutatedMarkerVariantDP(String markerName, String variantName){

        if(variantName == null || variantName.length()<3) variantName = "Not applicable";

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
        Map<Long, List<String>> mutationPlatformsByModel = new HashMap<>();
        Map<Long, List<String>> cnaPlatformsByModel = new HashMap<>();


        Collection<ModelCreation> allModelsWithPlatforms = dataImportService.findAllModelsPlatforms();

        for (ModelCreation mc : allModelsWithPlatforms) {


            if(mc.getRelatedSamples() != null){

                for(Sample s : mc.getRelatedSamples()){

                    if(s.getMolecularCharacterizations() != null){

                        for(MolecularCharacterization molc : s.getMolecularCharacterizations()){

                                if (molc.getPlatform() != null && !molc.getType().isEmpty()) {
                                    String platformName = molc.getPlatform().getName();

                                    if (dataImportService.countMarkerAssociationBySourcePdxId(mc.getSourcePdxId(), mc.getDataSource(), platformName) > 0) {

                                        if (molc.getType().toLowerCase().equals("mutation")) {

                                            if (!mutationPlatformsByModel.containsKey(mc.getId())) {
                                                mutationPlatformsByModel.put(mc.getId(), new ArrayList<>());
                                            }

                                            mutationPlatformsByModel.get(mc.getId()).add(platformName);
                                        } else if (molc.getType().toLowerCase().equals("copy number alteration")) {

                                            if (!cnaPlatformsByModel.containsKey(mc.getId())) {

                                                cnaPlatformsByModel.put(mc.getId(), new ArrayList<>());
                                            }

                                            cnaPlatformsByModel.get(mc.getId()).add(platformName);
                                        }

                                    }

                                }

                        }

                    }

                }

            }


            /*
            if (!mutationPlatformsByModel.containsKey(mc.getId())) {
                mutationPlatformsByModel.put(mc.getId(), new ArrayList<>());
            }

            // Are there any molecular characterizations associated to this model?
            if (mc.getRelatedSamples().stream().map(Sample::getMolecularCharacterizations).mapToLong(Collection::size).sum() > 0) {

                // Get all molecular characterizations platforms into a list
                mutationPlatformsByModel.get(mc.getId()).addAll(
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
            */

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


            Set<String> dataAvailable = new HashSet<>();

            if(mutationPlatformsByModel.containsKey(mc.getId())){

                dataAvailable.add("Gene Mutation");

            }

            if(cnaPlatformsByModel.containsKey(mc.getId())){
                dataAvailable.add("Copy Number Alteration");
            }

            if(dataImportService.isTreatmentSummaryAvailableOnModel(mc.getDataSource(), mc.getSourcePdxId())){
                dataAvailable.add("Dosing Studies");
            }
            try {
                if (dataImportService.isTreatmentSummaryAvailableOnPatient(mc.getDataSource(), mc.getSourcePdxId())) {
                    dataAvailable.add("Patient Treatment");
                }
            }
            catch(Exception e){
                e.printStackTrace();
                log.error(mc.getSourcePdxId());
            }

            mfq.setDataAvailable(new ArrayList<>(dataAvailable));



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
            mfq.setPatientEthnicity(mc.getSample().getPatientSnapshot().getPatient().getEthnicity());
            mfq.setDiagnosis(mc.getSample().getDiagnosis());
            mfq.setMappedOntologyTerm(mc.getSample().getSampleToOntologyRelationShip().getOntologyTerm().getLabel());

            if (mc.getSample().getPatientSnapshot().getTreatmentNaive() != null) {
                String treatmentNaive = mc.getSample().getPatientSnapshot().getTreatmentNaive();

                if(treatmentNaive.isEmpty()){

                    mfq.setPatientTreatmentStatus("Not Specified");
                }
                else if(treatmentNaive.toLowerCase().contains("not")){
                    mfq.setPatientTreatmentStatus("Not Treatment Naive");
                }
                else{
                    mfq.setPatientTreatmentStatus("Treatment Naive");
                }

            } else {
                mfq.setPatientTreatmentStatus("Not Specified");
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
            if(mc.getSample().getSampleToOntologyRelationShip().getOntologyTerm().getSubclassOf() != null){
                for (OntologyTerm t : mc.getSample().getSampleToOntologyRelationShip().getOntologyTerm().getSubclassOf()) {
                    allOntologyTerms.addAll(getAllAncestors(t));
                }
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

            //ADD PROJECTS TO MFQ
            if(mc.getGroups() != null){

                for(Group g : mc.getGroups()){

                    if(g.getType().equals("Project")){

                        mfq.addProject(g.getName());
                    }

                }
            }



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


    private void createModelDrugResponseDataProjection(){

        log.info("Creating Model Drug Response DP");

        List<TreatmentSummary> treatmentSummaries = drugService.getModelTreatmentSummariesWithDrugAndResponse();

        for(TreatmentSummary ts : treatmentSummaries){

            ModelCreation model = dataImportService.findModelByTreatmentSummary(ts);

            //check if treatment is linked to a model
            if(model != null){

                Long modelId = model.getId();

                for(TreatmentProtocol tp : ts.getTreatmentProtocols()){


                    String drugName = tp.getDrugString(true);
                    String response = tp.getResponse().getDescription();

                    addToModelDrugResponseDP(modelId, drugName, response);

                }
            }

        }
        System.out.println();

    }

    private void addToModelDrugResponseDP(Long modelId, String drugName, String responseVal){

        if(modelId != null && drugName != null && !drugName.isEmpty() && responseVal != null && !responseVal.isEmpty()){

            //TODO: Remove regex after drug harmonization is done
            String drug = drugName.replaceAll("[^a-zA-Z0-9 _-]","");
            String response = responseVal.replaceAll("[^a-zA-Z0-9 _-]","");

            if(modelDrugResponseDP.containsKey(drug)){

                if(modelDrugResponseDP.get(drug).containsKey(response)){

                    modelDrugResponseDP.get(drug).get(response).add(modelId);
                }
                //new response
                else{
                    Set s = new HashSet();
                    s.add(modelId);

                    modelDrugResponseDP.get(drug).put(response,s);
                }
            }
            //new drug, create response and add model
            else{

                Set s = new HashSet();
                s.add(modelId);

                Map respMap = new HashMap();
                respMap.put(response, s);

                modelDrugResponseDP.put(drug, respMap);
            }

        }


    }



    private void saveDataProjections(){

        log.info("Saving DataProjections");

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

        DataProjection drugDP = dataImportService.findDataProjectionByLabel("ModelDrugData");

        if(drugDP == null){

            drugDP = new DataProjection();
            drugDP.setLabel("ModelDrugData");
        }

        DataProjection ihcDP = dataImportService.findDataProjectionByLabel("IHC");

        if(ihcDP == null){
            ihcDP = new DataProjection();
            ihcDP.setLabel("IHC");
        }


        JSONObject j1 ,j2, j3, j4;

        try{
            j1 = new JSONObject(mutatedPlatformMarkerVariantModelDP.toString());
            pmvmDP.setValue(j1.toString());
        }
        catch(Exception e){

            e.printStackTrace();
            log.error(mutatedPlatformMarkerVariantModelDP.toString());
        }

        try{
            j2 = new JSONObject(mutatedMarkerVariantDP.toString());
            mvDP.setValue(j2.toString());
        }
        catch(Exception e){
            e.printStackTrace();
            log.error(mutatedMarkerVariantDP.toString());
        }
        try{
            j3 = new JSONObject(modelDrugResponseDP.toString());
            drugDP.setValue(j3.toString());
        }
        catch(Exception e){
            e.printStackTrace();
            log.error(modelDrugResponseDP.toString());
        }

        try{
            j4 = new JSONObject(immunoHistoChemistryDP.toString());
            ihcDP.setValue(j4.toString());
        }
        catch(Exception e){
            e.printStackTrace();
            log.error(immunoHistoChemistryDP.toString());
        }


        dataImportService.saveDataProjection(pmvmDP);
        dataImportService.saveDataProjection(mvDP);
        dataImportService.saveDataProjection(drugDP);
        dataImportService.saveDataProjection(ihcDP);

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

    private void dumpDataToFile(){
        log.info("Dumping data to file");
        String fileName = "/Users/csaba/Documents/pdxFinderDump.txt";
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));

            writer.append(immunoHistoChemistryDP.toString());
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}