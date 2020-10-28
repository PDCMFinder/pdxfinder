package org.pdxfinder.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONArray;
import com.github.openjson.JSONException;
import com.github.openjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.pdxfinder.graph.dao.OntologyTerm;
import org.pdxfinder.graph.repositories.OntologyTermRepository;
import org.pdxfinder.graph.repositories.SampleRepository;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.rdbms.repositories.MappingEntityRepository;
import org.pdxfinder.services.dto.PaginationDTO;
import org.pdxfinder.services.mapping.CSV;
import org.pdxfinder.services.mapping.MappingContainer;
import org.pdxfinder.services.mapping.MappingEntityType;
import org.pdxfinder.services.mapping.Status;
import org.pdxfinder.services.zooma.*;
import org.pdxfinder.utils.DamerauLevenshteinAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*
 * Created by csaba on 09/07/2018.
 */
@Service
public class MappingService {

    private final static Logger log = LoggerFactory.getLogger(MappingService.class);
    private ObjectMapper mapper = new ObjectMapper();

    public static final String URI = "http://www.pdxfinder.org/";
    private static final String NAME = "pdx-finder";
    private static final List<String> TOPIC = Arrays.asList("PDXFinder");
    private static final String TYPE = "DATABASE";
    private static final String EVIDENCE = "SUBMITTER_PROVIDED";
    private static final String ACCURACY = "PRECISE";
    private static final String ANNOTATOR = "Nathalie Conte";

    @Value("${data-dir}")
    private String rootDir;

    @Value("${mappings.mappedTermUrl}")
    private String knowledgBaseURL;

    private SampleRepository sampleRepository;
    private MappingEntityRepository mappingEntityRepository;


    private MappingContainer container;

    private boolean INITIALIZED = false;

    private UtilityService utilityService;

    private PaginationService paginationService;
    private OntologyTermRepository ontologyTermRepository;


    @Autowired
    public MappingService(SampleRepository sampleRepository,
                          MappingEntityRepository mappingEntityRepository,
                          OntologyTermRepository ontologyTermRepository,
                          UtilityService utilityService,
                          PaginationService paginationService) {

        this.sampleRepository = sampleRepository;
        this.mappingEntityRepository = mappingEntityRepository;
        this.ontologyTermRepository = ontologyTermRepository;
        this.utilityService = utilityService;
        this.paginationService = paginationService;
        container = new MappingContainer();
    }

    public String getDiagnosisMappingKey(String dataSource, String diagnosis, String originTissue, String tumorType){

        String mapKey = MappingEntityType.diagnosis.get() + "__" + dataSource + "__" + diagnosis + "__" + originTissue + "__" + tumorType;
        mapKey = mapKey.replaceAll("[^a-zA-Z0-9 _-]", "").toLowerCase();
        return mapKey;
    }

    public String getTreatmentMappingKey(String dataSource, String treatmentName){

        String mapKey = MappingEntityType.treatment.get() + "__" + dataSource + "__" + treatmentName;
        mapKey = mapKey.replaceAll("[^a-zA-Z0-9 _-]", "").toLowerCase();
        return  mapKey;
    }

    public MappingEntity getDiagnosisMapping(String dataSource, String diagnosis, String originTissue, String tumorType) {

        if (!INITIALIZED) loadRules("json");
        String mapKey = getDiagnosisMappingKey(dataSource, diagnosis, originTissue, tumorType);
        return container.getEntityById(mapKey);
    }

    public MappingEntity getTreatmentMapping(String dataSource, String treatmentName) {

        if (!INITIALIZED) loadRules("json");
        String mapKey = getTreatmentMappingKey(dataSource, treatmentName);
        return container.getEntityById(mapKey);
    }


    public void saveMappingsToFile(String fileName, List<MappingEntity> maprules) {

        Map<String, List<MappingEntity>> mappings = new HashMap<>();
        mappings.put("mappings", maprules);

        String json = JSONObject.wrap(mappings).toString();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));

            writer.append(json);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Loads rules from a source: file or h2
     *
     * @param source
     */
    private void loadRules(String source) {

        if (container == null) container = new MappingContainer();

        log.info("Loading mapping rules");

        if (source.equals("json")) {

            String mappingRulesDir = rootDir + "/mapping";
            File folder = new File(mappingRulesDir);

            if (folder.exists()) {

                String diagnosisMappingsFilePath = mappingRulesDir + "/diagnosis_mappings.json";
                String treatmentMappingsFilePath = mappingRulesDir + "/treatment_mappings.json";

                File diagnosisFile = new File(diagnosisMappingsFilePath);
                File treatmentFile = new File(treatmentMappingsFilePath);

                if (diagnosisFile.exists()) {

                    loadDiagnosisMappings(diagnosisMappingsFilePath);
                } else {
                    log.error("Diagnosis mappings file not found at " + diagnosisMappingsFilePath);
                }


                if (treatmentFile.exists()) {

                    loadTreatmentMappings(treatmentMappingsFilePath);
                } else {
                    log.error("Treatment mappings file not found at " + treatmentMappingsFilePath);
                }


            } else {

                log.error("Mapping rules directory not found at " + mappingRulesDir);

            }


        } else if (source.equals("h2")) {


        } else {

            log.error("Couldn't load mapping rules, no source was specified");


        }


        INITIALIZED = true;

    }


    /**
     * Populates the container with the diagnosis mapping rules
     *
     * @param file
     */
    private void loadDiagnosisMappings(String file) {


        String json = utilityService.parseFile(file);

        try {
            JSONObject job = new JSONObject(json);
            if (job.has("mappings")) {
                JSONArray rows = job.getJSONArray("mappings");


                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);

                    JSONObject mappingVal = row.getJSONObject("mappingValues");


                    String dataSource = mappingVal.getString("DataSource");
                    String sampleDiagnosis = mappingVal.getString("SampleDiagnosis").toLowerCase();
                    String originTissue = mappingVal.getString("OriginTissue");
                    String tumorType = mappingVal.getString("TumorType");
                    String ontologyTerm = row.getString("mappedTermLabel");
                    String mapType = row.getString("mapType");
                    String justification = row.getString("justification");
                    String mappedTermUrl = row.getString("mappedTermUrl");
                    Long entityId = Long.parseLong(row.getString("entityId"));


                    //if(ds!= null && !ds.toLowerCase().equals(dataSource.toLowerCase())) continue;

                    if (ontologyTerm.equals("") || ontologyTerm == null) continue;
                    if (sampleDiagnosis.equals("") || sampleDiagnosis == null) continue;

                    String updatedDiagnosis = sampleDiagnosis;
                    String pattern = "(.*)Malignant(.*)Neoplasm(.*)";

                    if (sampleDiagnosis.matches(pattern)) {
                        updatedDiagnosis = (sampleDiagnosis.replaceAll(pattern, "\t$1$2Cancer$3")).trim();
                        log.info("Updating label from mapping service of diagnosis '{}' with '{}'", sampleDiagnosis, updatedDiagnosis);
                    }

                    // Remove commas from diagnosis
                    sampleDiagnosis = updatedDiagnosis.replaceAll(",", "");

                    //DO not ask, I know it looks horrible...
                    if (originTissue == null || originTissue.equals("null")) originTissue = "";
                    if (tumorType == null || tumorType.equals("null")) tumorType = "";
                    if (justification == null || justification.equals("null")) justification = "";

                    //make everything lowercase
                    if (dataSource != null) dataSource = dataSource.toLowerCase();
                    if (originTissue != null) originTissue = originTissue.toLowerCase();
                    if (tumorType != null) tumorType = tumorType.toLowerCase();
                    sampleDiagnosis = sampleDiagnosis.toLowerCase();


                    Map<String, String> mappingValues = new HashMap<>();
                    mappingValues.put("DataSource", dataSource);
                    mappingValues.put("SampleDiagnosis", sampleDiagnosis);
                    mappingValues.put("OriginTissue", originTissue);
                    mappingValues.put("TumorType", tumorType);

                    MappingEntity me = new MappingEntity(MappingEntityType.diagnosis.get(), getDiagnosisMappingLabels(), mappingValues);
                    me.setMappedTermLabel(ontologyTerm);
                    me.setMapType(mapType);
                    me.setJustification(justification);
                    me.setEntityId(entityId);
                    me.setMappedTermUrl(mappedTermUrl);
                    me.setMappingKey(me.generateMappingKey());

                    container.addEntity(me);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void loadTreatmentMappings(String file) {


        String json = utilityService.parseFile(file);

        try {
            JSONObject job = new JSONObject(json);
            if (job.has("mappings")) {
                JSONArray rows = job.getJSONArray("mappings");


                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);

                    JSONObject mappingVal = row.getJSONObject("mappingValues");


                    String dataSource = mappingVal.getString("DataSource");
                    String treatmentName = mappingVal.getString("TreatmentName").toLowerCase();
                    String ontologyTerm = row.getString("mappedTermLabel");
                    String mapType = row.getString("mapType");
                    String justification = row.getString("justification");
                    String mappedTermUrl = row.getString("mappedTermUrl");
                    Long entityId = Long.parseLong(row.getString("entityId"));


                    if (ontologyTerm.equals("") || ontologyTerm == null) continue;

                    //DO not ask, I know it looks horrible...
                    if (justification == null || justification.equals("null")) justification = "";

                    //make everything lowercase
                    if (dataSource != null) dataSource = dataSource.toLowerCase();


                    Map<String, String> mappingValues = new HashMap<>();
                    mappingValues.put("DataSource", dataSource);
                    mappingValues.put("TreatmentName", treatmentName);

                    MappingEntity me = new MappingEntity(MappingEntityType.treatment.get(), getTreatmentMappingLabels(), mappingValues);
                    me.setMappedTermLabel(ontologyTerm);
                    me.setMapType(mapType);
                    me.setJustification(justification);
                    me.setEntityId(entityId);
                    me.setMappedTermUrl(mappedTermUrl);
                    me.setMappingKey(me.generateMappingKey());

                    container.addEntity(me);

                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }







    public Map<String, List<MappingEntity>> getMissingDiagnosisMappings(String ds) {

        MappingContainer mc = new MappingContainer();

        if (ds == null || ds.isEmpty()) {

        } else {

        }

        Map<String, List<MappingEntity>> entityMap = new HashMap<>();

        List<MappingEntity> mappingEntities = mappingEntityRepository.findByMappedTermLabel(null);  // new ArrayList<>();

        entityMap.put("mappings", mappingEntities);
        return entityMap;

    }

/*
    public MappingContainer getSavedDiagnosisMappings(String ds){


        if(!INITIALIZED){

            loadRules("file");
        }

        //no filter, return everything
        if(ds == null) return existingDiagnosisMappings;

        MappingContainer mc = new MappingContainer();

        List<MappingEntity> results = existingDiagnosisMappings.getMappings().values().stream().filter(
                x -> x.getEntityType().equals("DIAGNOSIS") &&
                        x.getMappingValues().get("DataSource").equals(ds)).collect(Collectors.toList());

        results.forEach(x -> {
            mc.addEntity(x);
        });


    return mc;
    }

*/


    public MappingContainer getMappingsByDSAndType(List<String> ds, String type) {


        if (!INITIALIZED) {

            loadRules("json");
        }


        MappingContainer mc = new MappingContainer();

        for (MappingEntity me : container.getMappings().values()) {

            if (me.getEntityType().toLowerCase().equals(type.toLowerCase())) {

                for (String dataSource : ds) {

                    if (dataSource.toLowerCase().equals(me.getMappingValues().get("DataSource").toLowerCase())) {
                        //clone object but purge keys
                        MappingEntity me2 = new MappingEntity();
                        me2.setEntityId(me.getEntityId());
                        me2.setEntityType(me.getEntityType());
                        me2.setMappingLabels(me.getMappingLabels());
                        me2.setMappingValues(me.getMappingValues());
                        me2.setMappedTermUrl(me.getMappedTermUrl());
                        me2.setMappedTermLabel(me.getMappedTermLabel());
                        me2.setMapType(me.getMapType());
                        me2.setJustification(me.getJustification());
                        me2.setStatus(me.getStatus());
                        me2.setSuggestedMappings(me.getSuggestedMappings());
                        me2.setMappingKey(me.getMappingKey());
                        mc.addEntity(me2);
                    }
                }
            }
        }

        return mc;

    }



    private List<MappingEntity> getSuggestionsForUnmappedEntity(MappingEntity me, MappingContainer mc) {

        String entityType = me.getEntityType();
        TreeMap<Integer, List<MappingEntity>> unorderedSuggestions = new TreeMap<>();

        //APPLY MAPPING SUGGESTION LOGIC HERE

        List<MappingEntity> mapSuggList = mc.getMappings().values().stream().filter(x -> x.getEntityType().equals(entityType)).collect(Collectors.toList());

        //Use the Damerau Levenshtein algorithm to determine string similarity

        DamerauLevenshteinAlgorithm dla = new DamerauLevenshteinAlgorithm(1, 1, 2, 2);
        String typeKeyValues1 = getTypeKeyValues(me);

        mapSuggList.forEach(x -> {

            //get similarity index components
            int simIndex = 0;

            for (String label : x.getMappingLabels()) {

                simIndex += getSimilarityIndexComponent(dla, me.getEntityType(), label, me.getMappingValues().get(label), x.getMappingValues().get(label));
            }

            //x.setSimilarityIndex(getStringSimilarity(dla, typeKeyValues1, getTypeKeyValues(x)));

            Integer index = new Integer(simIndex);

            if (unorderedSuggestions.containsKey(index)) {

                unorderedSuggestions.get(index).add(x);
            } else {
                List<MappingEntity> list = new ArrayList<>();
                list.add(x);
                unorderedSuggestions.put(index, list);
            }

        });


        //take all mapped entities and order them by their stringsimilarity to the unmapped entity
        //mapSuggList.stream().sorted((x1, x2) -> Integer.compare(getStringSimilarity(dla, typeKeyValues1, getTypeKeyValues(x1)),  getStringSimilarity(dla, typeKeyValues1, getTypeKeyValues(x2))) );
        //mapSuggList = mapSuggList.stream().sorted(Comparator.comparing(MappingEntity::getSimilarityIndex)).collect(Collectors.toList());

        TreeMap<Integer, List<MappingEntity>> orderedSuggestions = new TreeMap<>(unorderedSuggestions);
        List<MappingEntity> resultList = new ArrayList<>();

        //log.info("UNMAPPED: "+me.getMappingValues().get("SampleDiagnosis")+" "+me.getMappingValues().get("OriginTissue"));

        int entityCounter = 0;
        for (Map.Entry<Integer, List<MappingEntity>> entry : orderedSuggestions.entrySet()) {

            Integer ix = entry.getKey();
            List<MappingEntity> list = entry.getValue();
            for (MappingEntity ment : list) {

                //log.info("SUGG: " + ment.getMappingValues().get("SampleDiagnosis") + " " + ment.getMappingValues().get("OriginTissue") + "INDEX:" + ix);
                resultList.add(ment);
                entityCounter++;

                if (entityCounter >= 10) break;
            }

            if (entityCounter >= 10) break;
        }

        //return the first 10 suggestions

        return resultList;
    }


    private int getSimilarityIndexComponent(DamerauLevenshteinAlgorithm dla, String entityType, String entityAttribute, String attribute1, String attribute2) {


        if (entityType.toUpperCase().equals("DIAGNOSIS")) {

            if (entityAttribute.equals("SampleDiagnosis")) {

                return dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase()) * 5;
            }

            if (entityAttribute.equals("OriginTissue")) {

                int diff = dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase());
                //the origin tissue is very different, less likely will be a good suggestion
                if (diff > 4) return 50;
                return diff;
            }

            int diff = dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase());

            if (diff > 4) return 1;
            return diff;


        }
        else if(entityType.toUpperCase().equals("TREATMENT")){

            if (entityAttribute.equals("TreatmentName")) {

                return dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase()) * 5;
            }

            int diff = dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase());

            if (diff > 4) return 1;
            return diff;

        }

        return 10000;

    }


    String getTypeKeyValues(MappingEntity me) {


        String key = "";

        if (me == null) return key;

        switch (me.getEntityType()) {

            case "DIAGNOSIS":
                for (String label : getDiagnosisMappingLabels()) {
                    key += me.getMappingValues().get(label).toLowerCase();
                }

                break;

            default:
                key = "";
        }


        return key;
    }


    public List<String> getDiagnosisMappingLabels() {

        List<String> mapLabels = new ArrayList<>();
        mapLabels.add("DataSource");
        mapLabels.add("SampleDiagnosis");
        mapLabels.add("OriginTissue");
        mapLabels.add("TumorType");

        return mapLabels;
    }

    public List<String> getTreatmentMappingLabels() {

        List<String> mapLabels = new ArrayList<>();
        mapLabels.add("DataSource");
        mapLabels.add("TreatmentName");

        return mapLabels;
    }


    private int getStringSimilarity(DamerauLevenshteinAlgorithm dla, String key1, String key2) {

        return dla.execute(key1, key2);
    }


    public List<ZoomaEntity> transformMappingsForZooma() {

        JsonNode mappingRow = utilityService.readJsonURL(knowledgBaseURL);

        Map<String, List<Object>> dMappingRow = mapper.convertValue(mappingRow, Map.class);

        List<ZoomaEntity> zoomaEntities = new ArrayList<>();

        for (Object data : dMappingRow.get("row")) {

            MappingEntity mappingEntity = mapper.convertValue(data, MappingEntity.class);

            /* RETRIEVE DATA FROM MAPPING ENTITY */
            Long entityId = mappingEntity.getEntityId();
            String entityType = mappingEntity.getEntityType();
            List<String> mappingLabels = mappingEntity.getMappingLabels();

            Map<String, String> mappingValues = mappingEntity.getMappingValues();
            String originTissue = mappingValues.get("OriginTissue");
            String tumorType = mappingValues.get("TumorType");
            String sampleDiagnosis = mappingValues.get("SampleDiagnosis");
            String dataSource = mappingValues.get("DataSource");

            String mappedTermLabel = mappingEntity.getMappedTermLabel();
            String mappedTermUrl = mappingEntity.getMappedTermUrl();
            String mapType = mappingEntity.getMapType();
            String justification = mappingEntity.getJustification();
            String status = mappingEntity.getStatus();


            /* ZOOMA BIOLOGICAL-ENTITY DATA */
            Studies studies = new Studies(dataSource.toUpperCase(), null);
            String bioEntity = StringUtils.join(
                    Arrays.asList(dataSource, sampleDiagnosis, originTissue, tumorType), "__"
            );
            BiologicalEntities biologicalEntities = new BiologicalEntities(bioEntity.toUpperCase(), studies, null);

            /* ZOOMA SEMANTIC-TAG DATA */
            List<String> semanticTag = Arrays.asList(mappedTermUrl);

            /* ZOOMA PROVENANCE DATA */
            Source source = new Source(URI, NAME, TOPIC, TYPE);
            Provenance provenance = new Provenance(
                    source,
                    EVIDENCE,
                    ACCURACY,
                    ANNOTATOR,
                    "2018-11-30 10:48"
            );

            for (String mappingLabel : mappingLabels) {

                /* ZOOMA PROPERTY DATA */
                Property property = new org.pdxfinder.services.zooma.Property(
                    mappingLabel,
                    StringUtils.upperCase(mappingValues.get(mappingLabel)));

                List<String> annotations = new ArrayList<>();

                if (mappingLabel.equals("SampleDiagnosis")) {
                    annotations = semanticTag;
                }

                ZoomaEntity zoomaEntity = new ZoomaEntity(
                        biologicalEntities,
                        property,
                        annotations,
                        provenance
                );
                zoomaEntities.add(zoomaEntity);
            }


        }


        return zoomaEntities;

    }


    public void saveUnmappedTreatment(String dataSource, String treatment) {

        List<String> mappingLabels = Arrays.asList("DataSource", "TreatmentName");

        Map mappingValues = new HashMap();
        mappingValues.put("DataSource", dataSource.toLowerCase());
        mappingValues.put("TreatmentName", treatment);

        MappingEntity mappingEntity = new MappingEntity(MappingEntityType.treatment.get(), mappingLabels, mappingValues);

        saveUnmappedTerms(mappingEntity);
    }


    public void saveUnmappedDiagnosis(String dataSource, String diagnosis, String originTissue, String tumorType) {

        ArrayList<String> mappingLabels = new ArrayList<>();
        mappingLabels.add("DataSource");
        mappingLabels.add("SampleDiagnosis");
        mappingLabels.add("OriginTissue");
        mappingLabels.add("TumorType");

        Map mappingValues = new HashMap();
        mappingValues.put("DataSource", dataSource.toLowerCase());
        mappingValues.put("SampleDiagnosis", diagnosis);
        mappingValues.put("OriginTissue", originTissue);
        mappingValues.put("TumorType", tumorType);

        MappingEntity mappingEntity = new MappingEntity(MappingEntityType.diagnosis.get(), mappingLabels, mappingValues);

        saveUnmappedTerms(mappingEntity);
    }


    public void saveUnmappedTerms(MappingEntity mappingEntity) {

        mappingEntity.setStatus(Status.unmapped.get());
        mappingEntity.setMappedTermLabel("-");
        mappingEntity.setDateCreated(new Date());
        mappingEntity.setEntityType(mappingEntity.getEntityType().toLowerCase());

        String mappingKey = mappingEntity.generateMappingKey();
        mappingEntity.setMappingKey(mappingKey);

        MappingEntity entity = mappingEntityRepository.findByMappingKey(mappingKey);

        if (entity == null) {

            mappingEntityRepository.save(mappingEntity);

            log.info("UNMAPPED TERM WAS SAVED: {}", mappingEntity.generateMappingKey());

        }
    }


    public void saveMappedTerms(List<MappingEntity> mappingEntities) {

        for (MappingEntity mappingEntity : mappingEntities) {

            mappingEntity.setEntityId(null);
            mappingEntity.setStatus(Status.validated.get());
            mappingEntity.setEntityType(mappingEntity.getEntityType().toLowerCase());

            String mappingKey = mappingEntity.getMappingKey();

            MappingEntity entity = mappingEntityRepository.findByMappingKey(mappingKey);

            if (entity == null) {

                mappingEntityRepository.save(mappingEntity);

                log.warn("{} was SAVED ", mappingKey);

            } else {
                log.warn("{} was not NOT SAVED: found in the Database ", mappingKey);
            }
        }
    }

    public void purgeMappingDatabase(){
        log.warn("Deleting H2 database and all its {} mapping data", mappingEntityRepository.findAll().size());
        mappingEntityRepository.deleteAll();
    }

    public List<MappingEntity> loadMappingsFromFile(String jsonFile) {

        String jsonKey = "mappings";
        List<MappingEntity> mappingEntities = new ArrayList<>();

        List<Map<String, Object>> mappings = utilityService.serializeJSONToMaps(jsonFile, jsonKey);

        mappings.forEach(mapping -> {

            MappingEntity mappingEntity = mapper.convertValue(mapping, MappingEntity.class);
            mappingEntity.setMappingKey(mappingEntity.generateMappingKey());
            mappingEntity.setDateCreated(new Date());
            mappingEntities.add(mappingEntity);

        });

        return mappingEntities;
    }



    public void readArchive(String entityType) {

        String jsonKey = "mappings";

        String mappingDirectory = rootDir+"/mapping/backup/"+entityType;

        utilityService.listAllFilesInADirectory(mappingDirectory);

    }


    public void writeMappingsToFile(String entityType) {

        if (!containsSpecialCharacters(entityType)) throw new IllegalArgumentException("EntityType contains illegal characters");
        else {
            String jsonKey = "mappings";
            String mappingFile = rootDir + "/mapping/" + entityType + "_mappings.json";

            // Generate Unique name to back up previous mapping file
            String baseDirectory = rootDir + "/mapping/backup/";
            String backupPreviousMappingFile = baseDirectory + entityType + "/" +
                    (new Date()).toString().replaceAll(" ", "-") + "-" + entityType + "_mappings.json";

            // Back up previous mapping file before replacement
            utilityService.moveFile(mappingFile, backupPreviousMappingFile);


            // Get Latest mapped terms from the data base
            List<MappingEntity> mappingEntities = mappingEntityRepository.findByEntityTypeAndStatusIsNot(entityType, "unmapped");

            Map dataMap = new HashMap();
            dataMap.put(jsonKey, mappingEntities);

            // Write latest mapped terms to the file system
            try {

                String newFile = mapper.writeValueAsString(dataMap);
                utilityService.writeToFile(newFile, mappingFile, false);

            } catch (JsonProcessingException e) {
            }
        }

    }

    private boolean containsSpecialCharacters(String input){
        return input.matches("^[A-Za-z0-9-_]*$");
    }


    public PaginationDTO search(int page,
                                int size,
                                List<String> entityType,
                                String mappingLabel,
                                List<String> mappingValue,
                                String mappedTermLabel,
                                String mapType,
                                String mappedTermsOnly,
                                List<String> status) {

        String sortColumn = "id";
        Sort.Direction direction = getSortDirection("asc");

        Pageable pageable = null;

        // requested data size is either greater than zero or forced to 10
        size = (size > 0) ? size : 10;

        // requested page is either +ve or forced to default page
        int start = (page > 0) ? page - 1 : 0;

        Sort sort = Sort.by(direction, sortColumn);

        pageable = PageRequest.of(start, size, sort);

        Page<MappingEntity> mappingEntityPage = mappingEntityRepository.findByMultipleFilters(entityType, mappingLabel, mappingValue, mappedTermLabel, mapType, mappedTermsOnly, status, pageable);

        List<MappingEntity> mappingEntityList = new ArrayList<>();


        mappingEntityPage.forEach(mappingEntity -> {

            //get suggestions for missing mapping
            if (mappingEntity.getMappedTermLabel().equals("-")) {

                mappingEntity
                        .setSuggestedMappings(getSuggestionsForUnmappedEntity(
                                mappingEntity,
                                getMappedEntitiesByType(mappingEntity.getEntityType())));
            }

            mappingEntityList.add(mappingEntity);
        });

        PaginationDTO paginationDto = paginationService.initializeDTO(mappingEntityPage);
        paginationDto.setAdditionalProperty("mappings", mappingEntityList);

        return paginationDto;

    }


    public Sort.Direction getSortDirection(String sortDir) {

        Sort.Direction direction = Sort.Direction.ASC;

        if (sortDir.equals("desc")) {
            direction = Sort.Direction.DESC;
        }
        return direction;
    }


    public MappingContainer getMappedEntitiesByType(String entityType) {

        List<MappingEntity> mappedEntities = mappingEntityRepository.findByEntityTypeAndMapTypeIsNotNull(entityType);

        MappingContainer mc = new MappingContainer();

        mappedEntities.forEach(mappedEntity -> {
            mc.addEntity(mappedEntity);
        });

        return mc;
    }


    public List<Map> getMappingSummary(String entityType) {

        List<Object[]> summary = mappingEntityRepository.findMissingMappingStat(entityType);

        List<String> resultColumns = Arrays.asList("DataSource", "Unmapped", "Mapped", "Validated", "Created", "Orphaned");

        List<Map> mappingSummary = utilityService.objectArrayListToMapList(summary, resultColumns);

        return mappingSummary;
    }



    public MappingEntity getMappingEntityById(Integer entityId) {

        Long id = Long.parseLong(String.valueOf(entityId));

        MappingEntity mappingEntity = mappingEntityRepository.findByEntityId(id).get();

        //Get suggestions only if mapped term is missing
        MappingContainer mappingContainer = getMappedEntitiesByType(mappingEntity.getEntityType());

        // Remove present mappingEntity from mappingContainer to be used for suggestion
        mappingContainer.getMappings().remove(mappingEntity.getMappingKey());

        mappingEntity
                .setSuggestedMappings(getSuggestionsForUnmappedEntity(
                        mappingEntity,
                        mappingContainer));

        return mappingEntity;
    }



    public boolean checkExistence(Long entityId) {

        return mappingEntityRepository.existsByEntityId(entityId);

    }


    public Optional<MappingEntity> getByMappingKeyAndEntityId(String mappingKey, Long entityId) {

        Optional<MappingEntity> mappingEntity = mappingEntityRepository
                .findByMappingKeyAndEntityId(mappingKey, entityId);

        return mappingEntity;
    }


    // Update Bulk List of Mapping Entity Records
    public List<MappingEntity> updateRecords(List<MappingEntity> submittedEntities) {

        List<MappingEntity> savedEntities = new ArrayList<>();

        submittedEntities.forEach(newEntity -> {

            MappingEntity updated = mappingEntityRepository.findByEntityId(newEntity.getEntityId())
                    .map(mappingEntity -> {

                        mappingEntity.setDateUpdated(new Date());
                        mappingEntity.setStatus(newEntity.getStatus());
                        mappingEntity.setMappedTermLabel(newEntity.getMappedTermLabel());
                        mappingEntity.setMappedTermUrl(newEntity.getMappedTermUrl());
                        mappingEntity.setMapType(newEntity.getMapType());
                        mappingEntity.setJustification(newEntity.getJustification());

                        return mappingEntityRepository.save(mappingEntity);
                    })
                    .orElseGet(() -> {
                        return newEntity;
                    });

            savedEntities.add(updated);

        });

        /* WRITE updated mapped terms to the file system and backup old file */
        writeMappingsToFile(submittedEntities.get(0).getEntityType());

        return savedEntities;
    }


    @Cacheable("ontologies")
    public List<OntologyTerm> getOntologyTermsByType(String type) {

        return ontologyTermRepository.findByType(type);

    }




    public List<MappingEntity> processUploadedCSV(List<Map<String, String>> csvData) {


        List<MappingEntity> savedEntities = new ArrayList<>();

        csvData.forEach(eachData -> {

            // Retrieve the entityId from the csv data
            Long entityId = Long.parseLong(eachData.get(CSV.entityId.get()));


            // Pull the data from h2 based on the entityId, update the data from csvData and save
            MappingEntity updated = mappingEntityRepository.findByEntityId(entityId)
                    .map(mappingEntity -> {

                        /*
                         *  Get Decision column of this csvData, if Yes, change Status to Validated
                         *  If Decision column is NO, Pick content of ApprovedTerm column, replace maped term for that entity and set status as validated
                         */
                        if (eachData.get(CSV.decision.get()).equalsIgnoreCase(CSV.no.get())){

                            mappingEntity.setMappedTermLabel(eachData.get(CSV.mappedTerm.get()));
                            mappingEntity.setMappedTermUrl(eachData.get(CSV.mappedTermUrl.get()));
                        }


                        mappingEntity.setDateUpdated(new Date());
                        mappingEntity.setStatus(Status.validated.get());
                        return mappingEntityRepository.save(mappingEntity);
                    })
                    .orElseGet(() -> {
                        return new MappingEntity();
                    });

            savedEntities.add(updated);

        });

        return savedEntities;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

}
