package org.pdxfinder.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.admin.pojos.MappingContainer;
import org.pdxfinder.admin.pojos.MappingEntity;
import org.pdxfinder.admin.zooma.*;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.SampleRepository;
import org.pdxfinder.utils.DamerauLevenshteinAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Value("${mappings.diagnosis.file}")
    private String savedDiagnosisMappingsFile;

    @Value("${mappings.mappedTermUrl}")
    private String knowledgBaseURL;

    private SampleRepository sampleRepository;

    private MappingContainer existingDiagnosisMappings;

    @Autowired
    private UtilityService utilityService;

    @Autowired
    public MappingService(SampleRepository sampleRepository) {

        this.sampleRepository = sampleRepository;


    }

    public String getSavedDiagnosisMappingsFile() {
        return savedDiagnosisMappingsFile;
    }

    public void setSavedDiagnosisMappingsFile(String savedDiagnosisMappingsFile) {
        this.savedDiagnosisMappingsFile = savedDiagnosisMappingsFile;
    }


    private void loadSavedDiagnosisMappings(){

        String json = parseFile(savedDiagnosisMappingsFile);
        existingDiagnosisMappings = new MappingContainer();


        try {
            JSONObject job = new JSONObject(json);
            if (job.has("rows")) {
                JSONArray rows = job.getJSONArray("rows");


                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);

                    String dataSource = row.getString("datasource");
                    String sampleDiagnosis = row.getString("samplediagnosis").toLowerCase();
                    String originTissue = row.getString("origintissue");
                    String tumorType = row.getString("tumortype");
                    String ontologyTerm = row.getString("ontologyterm");
                    String mapType = row.getString("maptype");
                    String justification = row.getString("justification");

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

                    MappingEntity me = new MappingEntity(new Long(i+1), "DIAGNOSIS", getDiagnosisMappingLabels(), mappingValues);
                    me.setMappedTermLabel(ontologyTerm);
                    me.setMapType(mapType);
                    me.setJustification(justification);

                    existingDiagnosisMappings.add(me);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }



    public MappingContainer getSavedDiagnosisMappings(String ds){

        if(existingDiagnosisMappings == null){

            loadSavedDiagnosisMappings();
        }

        //no filter, return everything
        if(ds == null) return existingDiagnosisMappings;

        MappingContainer mc = new MappingContainer();

        List<MappingEntity> results = existingDiagnosisMappings.getMappings().values().stream().filter(
                x -> x.getEntityType().equals("DIAGNOSIS") &&
                        x.getMappingValues().get("DataSource").equals(ds)).collect(Collectors.toList());

        results.forEach(x -> {
            mc.add(x);
        });


    return mc;
    }


    public void saveMappingsToFile(String fileName, Collection<MappingEntity> maprules){

        Map<String, Collection<MappingEntity>> mappings = new HashMap<>();
        mappings.put("row", maprules);

        Gson gson = new Gson();
        String json = gson.toJson(mappings);



        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));

            writer.append(json);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public MappingContainer getMissingDiagnosisMappings(String ds){

        MappingContainer mc = new MappingContainer();

        Collection<Sample> samplesWithoutMappedTerm;
        Set<String> existingCombinations = new HashSet<>();

        if(ds == null || ds.isEmpty()){

            samplesWithoutMappedTerm = sampleRepository.findSamplesWithoutOntologyMapping();
        }
        else{

            samplesWithoutMappedTerm = sampleRepository.findSamplesWithoutOntologyMappingByDataSource(ds);
        }

        int mappingCounter = 1;
        log.info("Size: "+samplesWithoutMappedTerm.size());

        for(Sample s : samplesWithoutMappedTerm){

            String dataSource = s.getDataSource();
            String sampleDiagnosis = s.getDiagnosis();
            String originTissue = "";
            String tumorType = "";

            if(s.getOriginTissue() != null){

                originTissue = s.getOriginTissue().getName();
            }

            if(s.getType() != null){

                tumorType = s.getType().getName();
            }

            if(!existingCombinations.contains(dataSource+";"+sampleDiagnosis+";"+originTissue+";"+tumorType)){

                Map<String, String> mappingValues = new HashMap<>();
                mappingValues.put("DataSource", dataSource);
                mappingValues.put("SampleDiagnosis", sampleDiagnosis);
                mappingValues.put("OriginTissue", originTissue);
                mappingValues.put("TumorType", tumorType);

                MappingEntity me = new MappingEntity(mc.getNextAvailableId(), "DIAGNOSIS", getDiagnosisMappingLabels(), mappingValues);

                //get suggestions for missing mapping
                me.setSuggestedMappings(getSuggestionsForUnmappedEntity(me, getSavedDiagnosisMappings(null)));

                mc.add(me);
                existingCombinations.add(dataSource+";"+sampleDiagnosis+";"+originTissue+";"+tumorType);
            }


        }


        return mc;
    }




    private List<MappingEntity> getSuggestionsForUnmappedEntity(MappingEntity me, MappingContainer mc){


        String entityType = me.getEntityType();
        TreeMap<Integer, List<MappingEntity>> unorderedSuggestions = new TreeMap<>();

        //APPLY MAPPING SUGGESTION LOGIC HERE

        List<MappingEntity> mapSuggList =  mc.getMappings().values().stream().filter(x -> x.getEntityType().equals(entityType)).collect(Collectors.toList());

        //Use the Damerau Levenshtein algorithm to determine string similarity

        DamerauLevenshteinAlgorithm dla = new DamerauLevenshteinAlgorithm(1,1,2,2);
        String typeKeyValues1 = getTypeKeyValues(me);

        mapSuggList.forEach(x -> {

            //get similarity index components
            int simIndex = 0;

            for(String label : x.getMappingLabels()){

                simIndex += getSimilarityIndexComponent(dla, me.getEntityType(), label, me.getMappingValues().get(label), x.getMappingValues().get(label));
            }

            //x.setSimilarityIndex(getStringSimilarity(dla, typeKeyValues1, getTypeKeyValues(x)));

            Integer index = new Integer(simIndex);

            if(unorderedSuggestions.containsKey(index)){

                unorderedSuggestions.get(index).add(x);
            }
            else{
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

        log.info("UNMAPPED: "+me.getMappingValues().get("SampleDiagnosis")+" "+me.getMappingValues().get("OriginTissue"));

        int entityCounter = 0;
        for(Map.Entry<Integer, List<MappingEntity>> entry : orderedSuggestions.entrySet()){

            Integer ix = entry.getKey();
            List<MappingEntity> list = entry.getValue();
            for(MappingEntity ment : list){

                log.info("SUGG: "+ment.getMappingValues().get("SampleDiagnosis")+" "+ment.getMappingValues().get("OriginTissue") + "INDEX:"+ix);
                resultList.add(ment);
                entityCounter++;

                if(entityCounter>=10) break;
            }

            if(entityCounter>=10) break;
        }

        //return the first 10 suggestions

        return resultList;
    }



    private String parseFile(String path) {

        StringBuilder sb = new StringBuilder();

        try {
            Stream<String> stream = Files.lines(Paths.get(path));

            Iterator itr = stream.iterator();
            while (itr.hasNext()) {
                sb.append(itr.next());
            }
        } catch (Exception e) {
            log.error("Failed to load file " + path, e);
        }
        return sb.toString();
    }



    private int getSimilarityIndexComponent(DamerauLevenshteinAlgorithm dla, String entityType, String entityAttribute, String attribute1, String attribute2){


        if(entityType.equals("DIAGNOSIS")){

            if(entityAttribute.equals("SampleDiagnosis")){

                return dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase()) * 5;
            }
            else if(entityAttribute.equals("OriginTissue")){

                int diff = dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase());
                //the origin tissue is very different, less likely will be a good suggestion
                if(diff > 4) return 50;
                return diff;
            }
            else{

                int diff = dla.execute(attribute1.toLowerCase(), attribute2.toLowerCase());

                if(diff > 4) return 1;
                return diff;
            }

        }

        return 10000;

    }


    String getTypeKeyValues(MappingEntity me){


        String key = "";

        if(me == null) return key;

        switch (me.getEntityType()){

            case "DIAGNOSIS":
                for(String label : getDiagnosisMappingLabels()){
                    key += me.getMappingValues().get(label).toLowerCase();
                }

                break;

            default: key = "";
        }


        return key;
    }



    List<String> getDiagnosisMappingLabels(){

        List<String> mapLabels = new ArrayList<>();
        mapLabels.add("DataSource");
        mapLabels.add("SampleDiagnosis");
        mapLabels.add("OriginTissue");
        mapLabels.add("TumorType");

        return mapLabels;
    }




    private int getStringSimilarity(DamerauLevenshteinAlgorithm dla, String key1, String key2){

        return dla.execute(key1, key2);
    }



    public List<ZoomaEntity> transformMappingsForZooma(){

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
            BiologicalEntities biologicalEntities = new BiologicalEntities(bioEntity.toUpperCase(),studies,null);

            /* ZOOMA SEMANTIC-TAG DATA */
            List<String> semanticTag = Arrays.asList(mappedTermUrl);

            /* ZOOMA PROVENANCE DATA */
            Source source  = new Source(URI,NAME,TOPIC,TYPE);
            Provenance provenance = new Provenance(
                    source,
                    EVIDENCE,
                    ACCURACY,
                    ANNOTATOR,
                    "2018-11-30 10:48"
            );

            for (String mappingLabel : mappingLabels){

                /* ZOOMA PROPERTY DATA */
                Property property = new Property(mappingLabel,StringUtils.upperCase(mappingValues.get(mappingLabel)) );

                List<String> annotations = new ArrayList<>();

                if (mappingLabel.equals("SampleDiagnosis")){
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




}
