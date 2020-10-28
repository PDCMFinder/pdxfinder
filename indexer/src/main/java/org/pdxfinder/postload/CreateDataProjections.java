package org.pdxfinder.postload;

import com.github.openjson.*;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.queryresults.MutatedMarkerData;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.DrugService;
import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.dto.DataAvailableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Created by csaba on 09/03/2018.
 */
@Service
@Order(value = 90)
public class CreateDataProjections implements ApplicationContextAware{

    private final static Logger log = LoggerFactory.getLogger(CreateDataProjections.class);
    private DataImportService dataImportService;
    private DrugService drugService;

    @Value("${user.home}")
    String homeDir;

    @Value("${data-dir}")
    private String finderRootDir;

    protected ReportManager reportManager;

    //"platform"=>"marker"=>"variant"=>"set of model ids"
    private Map<String, Map<String, Map<String, Set<Long>>>> mutatedPlatformMarkerVariantModelDP = new HashMap<>();

    //"marker"=>"set of variants"
    private Map<String, Set<String>> mutatedMarkerVariantDP = new HashMap<>();

    private List<ModelForQuery> modelForQueryDP = new ArrayList<>();

    //"drugname"=>"response"=>"set of model ids"
    private Map<String, Map<String, Set<Long>>> modelDrugResponseDP = new HashMap<>();

    //"platform"=>"markercombos"=>"set of model ids"
    private Map<String, Map<String, Set<Long>>> immunoHistoChemistryDP = new HashMap<>();

    //marker=>status=>set of model ids
    private Map<String, Map<String, Set<Long>>> cytogeneticsDP = new HashMap<>();

    //"cnamarker"=>set of model ids
    private Map<String, Set<Long>> copyNumberAlterationDP = new HashMap<>();

    //"platform"=>"marker"=>"set of model ids"
    private Map<String, Map<String, Set<Long>>> expressionDP = new HashMap<>();

    private Map<String, List<DataAvailableDTO>> dataAvailableDP = new HashMap<>();

    private TreeMap<String, Set<Long>> frequentlyMutatedMarkers = new TreeMap<>();
    private List<MutatedMarkerData> frequentlyMutatedMarkersDP = new ArrayList<>();

    //name of drugs to model
    private Map<String, Set<Long>> drugDosingDP = new HashMap<>();

    //"treatment name"=>"set of model ids"
    private Map<String, Set<Long>> patientTreatmentDP = new HashMap<>();

    protected static ApplicationContext context;

    @Autowired
    public CreateDataProjections(DataImportService dataImportService, DrugService drugService) {

        this.dataImportService = dataImportService;
        this.drugService = drugService;
    }

    public void run() {

        long startTime = System.currentTimeMillis();


        log.info("Creating data projections");

        reportManager = (ReportManager) context.getBean("ReportManager");

        createMutationDataProjection();

        createModelForQueryDataProjection();

        createModelDrugResponseDataProjection();

        createPatientTreatmentDataProjection();

        createImmunoHistoChemistryDataProjection();

        createCNADataProjection();

        createExpressionDataProjection();

        createDataAvailableDataProjection();

        createFrequentlyMutatedGenesDataProjection();

        saveDataProjections();

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

            if(model == null){
                log.error("Molchar {} with type {} is not linked to a sample! ",mc.getId(), mc.getType());
                continue;
            }
            Long modelId = model.getId();

            String platformName = "Not Specified";

            if(mc.getPlatform() != null && mc.getPlatform().getName() != null && !mc.getPlatform().getName().isEmpty()){

                platformName = mc.getPlatform().getName();
            }

            Set<MarkerAssociation> mas = dataImportService.findMutationByMolChar(mc);

            if(mas != null){

                for(MarkerAssociation ma: mas){

                    List<MolecularData> molecularData;
                    try{

                        molecularData = ma.decodeMolecularData();
                    }
                    catch (Exception e){
                        log.error("No molecular data");
                        molecularData = new ArrayList<>();
                    }



                    for(MolecularData md: molecularData){

                        String variantName = md.getAminoAcidChange();
                        String markerName = md.getMarker();

                        if(variantName != null && !variantName.isEmpty()  && markerName != null && !markerName.isEmpty()){

                            //this was needed to avoid issues with variants where the value was a single space " "
                            if(variantName.length()<3) variantName = "Not applicable";

                            addToMutatedMarkerVariantDP(markerName, variantName);

                            addToThreeParamDP(mutatedPlatformMarkerVariantModelDP, platformName, markerName, variantName, modelId);
                            addToFrequentlyMutatedMarkers(markerName, modelId);
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

        Collection<MolecularCharacterization> ihcMolchars = dataImportService.findMolCharsByType("cytogenetics");
        log.info("Looking at "+ihcMolchars.size()+" IHC MolChar objects. This may take a while folks...");
        int count = 0;

        //modelid+ "___" + passage => marker => ihcresult
        // 23432___patient => HER2 => {pos, neg}
        Map<String, Map<String, Set<String>>> modelMarkerMap = new HashMap<>();

        for(MolecularCharacterization mc:ihcMolchars){

            ModelCreation model = dataImportService.findModelWithSampleByMolChar(mc);

            if(model == null){
                log.error("Molchar {} with type {} is not linked to a sample! ",mc.getId(), mc.getType());
                continue;
            }

            Long modelId = model.getId();

            //the findModelWithSampleByMolchar should return exactly one sample object
            List<Sample> sampleList = new ArrayList<>(model.getRelatedSamples());
            Sample sample = sampleList.get(0);

            String samplePassage = "patient";
            Specimen specimen = dataImportService.findSpecimenByMolChar(mc);

            if(specimen != null){

                if(!specimen.getPassage().isEmpty()){
                    samplePassage = specimen.getPassage();
                }
                else{
                    samplePassage = "xeno";
                }

            }

            String platformName = "Not Specified";

            if(mc.getPlatform() != null && mc.getPlatform().getName() != null && !mc.getPlatform().getName().isEmpty()){

                platformName = mc.getPlatform().getName();
            }

            Set<MarkerAssociation> mas = dataImportService.findMarkerAssocsByMolChar(mc);

            if(mas != null){

                for(MarkerAssociation ma: mas){

                    List<MolecularData> molecularData;
                    try{

                        molecularData = ma.decodeMolecularData();
                    }
                    catch (Exception e){
                        log.error("No molecular data");
                        molecularData = new ArrayList<>();
                    }


                    for(MolecularData md:molecularData){

                        String ihcResult = md.getCytogeneticsResult();
                        String markerName = md.getMarker();
                        //log.info(ihcResult + markerName);
                        if(ihcResult != null && !ihcResult.isEmpty()  && markerName != null && !markerName.isEmpty()){

                            //this was needed to avoid issues with variants where the value was a single space " "
                            if(ihcResult.length()<3) ihcResult = "Not applicable";

                            addToTwoParamDP(cytogeneticsDP, markerName, ihcResult.toLowerCase(), modelId);

                            if(ihcResult.toLowerCase().contains("pos")) ihcResult = "pos";
                            if(ihcResult.toLowerCase().contains("neg")) ihcResult = "neg";

                            if(ihcResult.equals("pos") || ihcResult.equals("neg") || ihcResult.equals("Not applicable")){

                                //discard markers that are not ER, HER2 or PR
                                if(markerName.equals("ESR1") || markerName.equals("ERBB2") || markerName.equals("PGR")) {


                                    String key = modelId+"___"+samplePassage;

                                    if(!modelMarkerMap.containsKey(key)){

                                        TreeMap markerMap = new TreeMap();
                                        markerMap.put("ERBB2", new HashSet<>());
                                        markerMap.put("ESR1", new HashSet<>());
                                        markerMap.put("PGR", new HashSet<>());
                                        modelMarkerMap.put(key, markerMap);

                                    }

                                    try {
                                        modelMarkerMap.get(key).get(markerName).add(ihcResult);
                                    }
                                    catch(Exception e){
                                        e.printStackTrace();
                                        log.error(key+" "+markerName+" "+ihcResult);
                                    }
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

            }
        }


        for(Map.Entry<String, Map<String, Set<String>>> model: modelMarkerMap.entrySet()){
            //modelid+ "___" + passage => marker => ihcresult
            // 23432___patient => HER2 => {pos, neg}

            String[] idcomp = model.getKey().split("___");
            Long modelId = Long.valueOf(idcomp[0]);
            Map<String, Set<String>> markers = model.getValue();
            String markerResultCombo = null;

            for(Map.Entry<String, Set<String>> marker : markers.entrySet()){

                String markerName = marker.getKey();

                for(String result : marker.getValue()) {

                    markerResultCombo = markerName+result;
                    addToTwoParamDP(immunoHistoChemistryDP, "Not specified", markerResultCombo, modelId);

                }
            }

        }


        //log.info(immunoHistoChemistryDP.toString());
    }

    private void createCNADataProjection(){


        Collection<MolecularCharacterization> cnaMolchars = dataImportService.findMolCharsByType("copy number alteration");
        log.info("Looking at "+cnaMolchars.size()+" CNA MolChar objects. This may take a while folks...");

        int count = 0;

        for(MolecularCharacterization mc:cnaMolchars) {

            ModelCreation model = dataImportService.findModelWithSampleByMolChar(mc);
            if(model == null){
                log.error("Molchar {} with type {} is not linked to a sample! ",mc.getId(), mc.getType());
                continue;
            }
            Long modelId = model.getId();

            Set<String> mas = mc.getMarkers();
            if(mas != null){
                for(String m : mas){


                    if(copyNumberAlterationDP.containsKey(m)){

                        copyNumberAlterationDP.get(m).add(modelId);
                    }
                    else{

                        Set<Long> newSet = new HashSet<>();
                        newSet.add(modelId);
                        copyNumberAlterationDP.put(m, newSet);
                    }
                }

            }


        }

        count++;

    }

    private void createExpressionDataProjection(){


        Collection<MolecularCharacterization> expressionMolchars = dataImportService.findMolCharsByType("expression");
        log.info("Looking at "+expressionMolchars.size()+" Expression MolChar objects. This may take a while folks...");

        for(MolecularCharacterization mc:expressionMolchars) {

            ModelCreation model = dataImportService.findModelWithSampleByMolChar(mc);
            if(model == null){
                log.error("Molchar {} with type {} is not linked to a sample! ",mc.getId(), mc.getType());
                continue;
            }
            Long modelId = model.getId();
            String platform = mc.getPlatform().getName();
            Set<String> mas = mc.getMarkers();

            if(mas != null){
                for(String m : mas){
                    addToTwoParamDP(expressionDP, platform, m, modelId);
                }
            }
        }

    }


    private void createDataAvailableDataProjection(){
        log.info("Creating data available projections");

        List<Group> providerGroups = dataImportService.getAllProviderGroups();

        for(Group group : providerGroups){

            int numberOfModels = dataImportService.getModelCountByDataSource(group.getAbbreviation());

            // molcharType + platformName + platformUrl = > Set of pdx ids
            Map<String, Set<String>> platformMap = new HashMap<>();

            log.info("Creating data available projections for "+group.getAbbreviation()+": "+numberOfModels + " models");

            for(int i =0; i < numberOfModels; i+=100){

                Collection<ModelCreation> models = dataImportService.getModelsWithMolCharBySourceFromTo(group.getAbbreviation(), i, 100);

                for(ModelCreation model: models){

                    for(Sample sample: model.getRelatedSamples()){

                        for(MolecularCharacterization mc: sample.getMolecularCharacterizations()){

                            String platformKey = mc.getType() + "___"+mc.getPlatform().getName()+"___"+mc.getPlatform().getUrl();

                            if(!platformMap.containsKey(platformKey)){
                                platformMap.put(platformKey, new HashSet<>());
                            }

                            platformMap.get(platformKey).add(model.getSourcePdxId());
                        }
                    }
                }


            }

            List<DataAvailableDTO> dataAvailableDTOList = new ArrayList<>();

            for(Map.Entry<String, Set<String>> platform :platformMap.entrySet()){
                try {
                    String[] platformArr = platform.getKey().split("___");
                    String dataType = platformArr[0];
                    String platformName = platformArr[1];
                    String platformUrl = "";
                    if(platformArr.length > 2){
                        platformUrl = platformArr[2];
                    }
                    String numOfModels = Integer.toString(platform.getValue().size());
                    DataAvailableDTO dto = new DataAvailableDTO(dataType, platformName, numOfModels, platformUrl);
                    dataAvailableDTOList.add(dto);
                }
                catch(Exception e){
                    log.error(platform.getKey());
                }
            }

            int drugDosingStudies = dataImportService.findDrugDosingStudyNumberByDataSource(group.getAbbreviation());
            if(drugDosingStudies > 0){

                DataAvailableDTO dto = new DataAvailableDTO("dosing studies", "Dosing Protocol", Integer.toString(drugDosingStudies), dataImportService.getDrugDosingUrlByDataSource(group.getAbbreviation()));
                dataAvailableDTOList.add(dto);
            }

            int patientTreatment = dataImportService.findPatientTreatmentNumberByDataSource(group.getAbbreviation());

            if(patientTreatment > 0){

                DataAvailableDTO dto = new DataAvailableDTO("patient treatment", "Treatment Protocol", Integer.toString(patientTreatment));
                dataAvailableDTOList.add(dto);
            }

            dataAvailableDP.put(group.getAbbreviation(), dataAvailableDTOList);






        }







    }


    private void addToOneParamDP(Map<String, Set<Long>> collection, String key, Long modelId){

        if(key == null || key.isEmpty()) return;

        if(collection.containsKey(key)) {
            collection.get(key).add(modelId);
        }
        else{
            Set<Long> s = new HashSet();
            s.add(modelId);
            collection.put(key, s);
        }
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
        Map<Long, List<String>> cytogeneticsPlatformsByModel = new HashMap<>();
        Map<Long, List<String>> transcriptomicsPlatformsByModel = new HashMap<>();
        Map<String, String> datasourceToDatasourceNameMap = new HashMap<>();


        List<Group> providerGroups = dataImportService.getAllProviderGroups();

        for(Group g: providerGroups){

            datasourceToDatasourceNameMap.put(g.getAbbreviation(), g.getName());
        }

        Collection<ModelCreation> allModelsWithPlatforms = dataImportService.findAllModelsPlatforms();

        for (ModelCreation mc : allModelsWithPlatforms) {


            if(mc.getRelatedSamples() != null){

                for(Sample s : mc.getRelatedSamples()){

                    if(s.getMolecularCharacterizations() != null){

                        for(MolecularCharacterization molc : s.getMolecularCharacterizations()){

                            if (molc.getPlatform() != null && !molc.getType().isEmpty()) {
                                String platformName = molc.getPlatform().getName();


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
                                else if (molc.getType().toLowerCase().equals("cytogenetics")) {

                                    if (!cytogeneticsPlatformsByModel.containsKey(mc.getId())) {

                                        cytogeneticsPlatformsByModel.put(mc.getId(), new ArrayList<>());
                                    }

                                    cytogeneticsPlatformsByModel.get(mc.getId()).add(platformName);
                                }
                                else if (molc.getType().toLowerCase().equals("expression")) {

                                    if (!transcriptomicsPlatformsByModel.containsKey(mc.getId())) {

                                        transcriptomicsPlatformsByModel.put(mc.getId(), new ArrayList<>());
                                    }

                                    transcriptomicsPlatformsByModel.get(mc.getId()).add(platformName);
                                }

                                //}

                            }

                        }

                    }

                }

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
        Collection<ModelCreation> modelsWitPatientData = dataImportService.findModelsWithPatientData();
        for (ModelCreation mc : modelsWitPatientData) {

            ModelForQuery mfq = new ModelForQuery();
            mfq.setModelId(mc.getId());
            mfq.setExternalId(mc.getSourcePdxId());

            mfq.setDatasource(mc.getDataSource());
            mfq.setDatasourceName(datasourceToDatasourceNameMap.get(mc.getDataSource()));

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

            if(cytogeneticsPlatformsByModel.containsKey(mc.getId())){
                dataAvailable.add("Cytogenetics");
            }

            if(transcriptomicsPlatformsByModel.containsKey(mc.getId())){
                dataAvailable.add("Expression");
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
            mfq.setMappedOntologyTerm(mc.getSample().getSampleToOntologyRelationship().getOntologyTerm().getLabel());

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
            allOntologyTerms.add(mc.getSample().getSampleToOntologyRelationship().getOntologyTerm());

            // Add all ancestors of direct mapped term
            if(mc.getSample().getSampleToOntologyRelationship().getOntologyTerm().getSubclassOf() != null){
                for (OntologyTerm t : mc.getSample().getSampleToOntologyRelationship().getOntologyTerm().getSubclassOf()) {
                    allOntologyTerms.addAll(getAllAncestors(t));
                }
            }

            mfq.setAllOntologyTermAncestors(allOntologyTerms.stream().map(OntologyTerm::getLabel).collect(Collectors.toSet()));

            // Add all top level systems (translated) to the Model
            for (String s : allOntologyTerms.stream().map(OntologyTerm::getLabel).collect(Collectors.toSet())) {

                if (cancerSystemMap.containsKey(s)) {

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

            //ADD PROJECTS AND ACCESSIBILITY TO MFQ
            if(mc.getGroups() != null){

                for(Group g : mc.getGroups()){

                    if(g.getType().equals("Project")){

                        mfq.addProject(g.getName());
                    }
                    else if(g.getType().equals("Accessibility")){
                        if(g.getAccessibility() == null){
                            mfq.setModelAccessibility("");
                        }
                        else{
                            mfq.setModelAccessibility(g.getAccessibility());
                        }

                        if(g.getAccessModalities() == null){
                            mfq.setAccessModalities("");
                        }
                        else{
                            mfq.setAccessModalities(g.getAccessModalities());
                        }

                    }
                }
            }



            this.modelForQueryDP.add(mfq);

        }



        log.info("Saving ModelForQuery DataProjection. Have "+modelForQueryDP.size() + " MFQ objects.");

        DataProjection mfqDP = dataImportService.findDataProjectionByLabel("ModelForQuery");

        if (mfqDP == null){

            mfqDP = new DataProjection();
            mfqDP.setLabel("ModelForQuery");
        }

        String jsonMfqDP = JSONObject.wrap(modelForQueryDP).toString();
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

        log.info("Creating Model Drug Response Data Projection");

        List<TreatmentSummary> treatmentSummaries = drugService.getModelTreatmentSummariesWithDrugAndResponse();

        for(TreatmentSummary ts : treatmentSummaries){
            ModelCreation model = dataImportService.findModelByTreatmentSummary(ts);
            //check if treatment is linked to a model
            if(model != null){
                Long modelId = model.getId();
                processModelDrugs(modelId, ts);
            }
        }
    }

    public void processModelDrugs(Long modelId, TreatmentSummary ts){

        for(TreatmentProtocol tp : ts.getTreatmentProtocols()){
            //this bit adds the drugA + drugB + drugC etc to the options
            String drugName = tp.getTreatmentString(true);
            String response = tp.getResponse().getDescription();
            addToModelDrugResponseDP(modelId, drugName, response);

            processModelRegimens(modelId, tp, response);
        }
    }

    public void processModelRegimens(Long modelId, TreatmentProtocol tp, String response){

        List<String> regimenDrugs = new ArrayList<>();
        for(TreatmentComponent tc: tp.getComponents()){
            Treatment t = tc.getTreatment();
            OntologyTerm ot = t.getTreatmentToOntologyRelationship().getOntologyTerm();
            if(ot.getType().equals("treatment regimen") && ot.getSubclassOf() != null && !ot.getSubclassOf().isEmpty()){
                for(OntologyTerm ot2: ot.getSubclassOf()){
                    regimenDrugs.add(ot2.getLabel());
                }
            }
        }

        //sort them alphabetically
        if(regimenDrugs.size() != 0){
            Collections.sort(regimenDrugs);
            String drugName = String.join(" and ", regimenDrugs);
            addToModelDrugResponseDP(modelId, drugName, response);
        }
    }

    private void createPatientTreatmentDataProjection(){

        log.info("Creating patient treatment data projection");

        List<TreatmentSummary> treatmentSummaries = drugService.getPatientTreatmentSummariesWithDrug();

        for(TreatmentSummary ts : treatmentSummaries){

            Collection<ModelCreation> models = null;

            try{
                models = dataImportService.findModelByPatientTreatmentSummary(ts);
            }
            catch (Exception e){

                e.printStackTrace();
                log.error("TS exception: "+ ts.getId().toString());
            }

            //check if treatment is linked to a model
            if(models != null && models.size() > 0 ){

                for(ModelCreation model : models){

                    Long modelId = model.getId();

                    for(TreatmentProtocol tp : ts.getTreatmentProtocols()){

                        //this bit adds the drugA + drugB + drugC etc to the options
                        String drugName = tp.getTreatmentString(true);
                        //String response = tp.getResponse().getDescription();
                        drugName = drugName.replaceAll("/", "");
                        addToOneParamDP(patientTreatmentDP, drugName, modelId);


                        //we also need to deal with regimens
                        List<String> regimenDrugs = new ArrayList<>();

                        for(TreatmentComponent tc: tp.getComponents()){

                            Treatment t = tc.getTreatment();
                            OntologyTerm ot = t.getTreatmentToOntologyRelationship().getOntologyTerm();

                            if(ot.getType().equals("treatment regimen") && ot.getSubclassOf() != null && !ot.getSubclassOf().isEmpty()){

                                for(OntologyTerm ot2: ot.getSubclassOf()){

                                    regimenDrugs.add(ot2.getLabel());

                                }
                            }
                        }

                        //sort them alphabetically
                        if(regimenDrugs.size() != 0){
                            Collections.sort(regimenDrugs);
                            drugName = String.join(" and ", regimenDrugs);
                            drugName = drugName.replaceAll("/", "");
                            addToOneParamDP(patientTreatmentDP, drugName, modelId);
                        }

                    }

                }



            }
            else{
                log.error("Cannot find model corresponding for patient treatmentSummary, TS id= "+ts.getId());
            }
        }


    }



    private void createFrequentlyMutatedGenesDataProjection(){

        log.info("Creating Frequently Mutated Genes data projection");

        for(Map.Entry<String, Set<Long>> entry : frequentlyMutatedMarkers.entrySet() ){

            MutatedMarkerData mmd = new MutatedMarkerData();
            mmd.setGene_name(entry.getKey());
            mmd.setNumber_of_models(entry.getValue().size());

            frequentlyMutatedMarkersDP.add(mmd);
        }

        frequentlyMutatedMarkersDP.sort(Comparator.comparing(MutatedMarkerData::getNumber_of_models).reversed());
    }



    private void addToModelDrugResponseDP(Long modelId, String drugName, String responseVal){

        if(modelId != null && drugName != null && !drugName.isEmpty() && responseVal != null && !responseVal.isEmpty()){

            //TODO: Remove regex after drug harmonization is done
            String drug = drugName.replaceAll("[^a-zA-Z0-9 _-]","");
            String response = responseVal.replaceAll("[^a-zA-Z0-9 _-]","");

            addToDrugDosingDp(drug, modelId);

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

    private void addToFrequentlyMutatedMarkers(String marker, Long modelId){

        if(frequentlyMutatedMarkers.containsKey(marker)){
            frequentlyMutatedMarkers.get(marker).add(modelId);
        }
        else{
            Set<Long> set = new HashSet<>();
            set.add(modelId);
            frequentlyMutatedMarkers.put(marker, set);
        }
    }

    private void addToDrugDosingDp(String drug, Long modelId){

        if(!drugDosingDP.containsKey(drug)){
            drugDosingDP.put(drug, new HashSet<>());
        }
        drugDosingDP.get(drug).add(modelId);
    }

    public void saveDataProjections(){

        log.info("Saving DataProjections");

        saveDP("PlatformMarkerVariantModel", mutatedPlatformMarkerVariantModelDP);
        saveDP("MarkerVariant", mutatedMarkerVariantDP);
        saveDP("ModelDrugData", modelDrugResponseDP);
        saveDP("breast cancer markers", immunoHistoChemistryDP);
        saveDP("cytogenetics", cytogeneticsDP);
        saveDP("copy number alteration", copyNumberAlterationDP);
        saveDP("expression", expressionDP);
        saveDP("data available", dataAvailableDP);
        saveDP("frequently mutated genes", frequentlyMutatedMarkersDP);
        saveDP("patient treatment", patientTreatmentDP);
        saveDP("drug dosing counter", drugDosingDP);

    }

    public DataProjection saveDP(String dpName, Object values){

        DataProjection dataProjection = dataImportService.findDataProjectionByLabel(dpName);

        if(dataProjection== null){
            dataProjection = new DataProjection();
            dataProjection.setLabel(dpName);
        }

        dataProjection.setValue(createJsonString(values));
        return dataImportService.saveDataProjection(dataProjection);
    }

    public String createJsonString(Object jstring){
        try {
            return JSONObject.wrap(jstring).toString();
        }
        catch(Exception e){
            log.error("There was an error serializing the map object to JSON");
        }
        return "";
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


    public void setMutatedPlatformMarkerVariantModelDP(Map<String, Map<String, Map<String, Set<Long>>>> mutatedPlatformMarkerVariantModelDP) {
        this.mutatedPlatformMarkerVariantModelDP = mutatedPlatformMarkerVariantModelDP;
    }

    public void setMutatedMarkerVariantDP(Map<String, Set<String>> mutatedMarkerVariantDP) {
        this.mutatedMarkerVariantDP = mutatedMarkerVariantDP;
    }

    public void setModelForQueryDP(List<ModelForQuery> modelForQueryDP) {
        this.modelForQueryDP = modelForQueryDP;
    }

    public void setModelDrugResponseDP(Map<String, Map<String, Set<Long>>> modelDrugResponseDP) {
        this.modelDrugResponseDP = modelDrugResponseDP;
    }

    public void setImmunoHistoChemistryDP(Map<String, Map<String, Set<Long>>> immunoHistoChemistryDP) {
        this.immunoHistoChemistryDP = immunoHistoChemistryDP;
    }

    public void setCytogeneticsDP(Map<String, Map<String, Set<Long>>> cytogeneticsDP) {
        this.cytogeneticsDP = cytogeneticsDP;
    }

    public void setCopyNumberAlterationDP(Map<String, Set<Long>> copyNumberAlterationDP) {
        this.copyNumberAlterationDP = copyNumberAlterationDP;
    }

    public void setExpressionDP(Map<String, Map<String, Set<Long>>> expressionDP) {
        this.expressionDP = expressionDP;
    }

    public void setDataAvailableDP(Map<String, List<DataAvailableDTO>> dataAvailableDP) {
        this.dataAvailableDP = dataAvailableDP;
    }

    public void setFrequentlyMutatedMarkersDP(List<MutatedMarkerData> frequentlyMutatedMarkersDP) {
        this.frequentlyMutatedMarkersDP = frequentlyMutatedMarkersDP;
    }

    public void setDrugDosingDP(Map<String, Set<Long>> drugDosingDP) {
        this.drugDosingDP = drugDosingDP;
    }

    public void setPatientTreatmentDP(Map<String, Set<Long>> patientTreatmentDP) {
        this.patientTreatmentDP = patientTreatmentDP;
    }

    public Map<String, Map<String, Map<String, Set<Long>>>> getMutatedPlatformMarkerVariantModelDP() {
        return mutatedPlatformMarkerVariantModelDP;
    }

    public Map<String, Set<String>> getMutatedMarkerVariantDP() {
        return mutatedMarkerVariantDP;
    }

    public List<ModelForQuery> getModelForQueryDP() {
        return modelForQueryDP;
    }

    public Map<String, Map<String, Set<Long>>> getModelDrugResponseDP() {
        return modelDrugResponseDP;
    }

    public Map<String, Map<String, Set<Long>>> getImmunoHistoChemistryDP() {
        return immunoHistoChemistryDP;
    }

    public Map<String, Map<String, Set<Long>>> getCytogeneticsDP() {
        return cytogeneticsDP;
    }

    public Map<String, Set<Long>> getCopyNumberAlterationDP() {
        return copyNumberAlterationDP;
    }

    public Map<String, Map<String, Set<Long>>> getExpressionDP() {
        return expressionDP;
    }

    public Map<String, List<DataAvailableDTO>> getDataAvailableDP() {
        return dataAvailableDP;
    }

    public List<MutatedMarkerData> getFrequentlyMutatedMarkersDP() {
        return frequentlyMutatedMarkersDP;
    }

    public Map<String, Set<Long>> getDrugDosingDP() {
        return drugDosingDP;
    }

    public Map<String, Set<Long>> getPatientTreatmentDP() {
        return patientTreatmentDP;
    }
}
