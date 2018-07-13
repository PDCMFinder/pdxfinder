package org.pdxfinder.services;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.admin.pojos.MappingContainer;
import org.pdxfinder.admin.pojos.MappingEntity;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/*
 * Created by csaba on 09/07/2018.
 */
@Service
public class MappingService {

    private final static Logger log = LoggerFactory.getLogger(MappingService.class);

    private SampleRepository sampleRepository;

    @Autowired
    public MappingService(SampleRepository sampleRepository) {

        this.sampleRepository = sampleRepository;

    }


    public MappingContainer getSavedDiagnosisMappings(String mappingFileLocation){

        //mappingFileLocation = "/Users/csaba/PDX/LoaderData/mappings/diagnosis_to_ncit.json";

        String json = parseFile(mappingFileLocation);
        MappingContainer mc = new MappingContainer();


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
                    me.setMappedTerm(ontologyTerm);
                    me.setMapType(mapType);
                    me.setJustification(justification);

                    mc.add(me);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



    return mc;
    }


    public MappingContainer getMissingMappings(String ds){

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

                mc.add(me);
                existingCombinations.add(dataSource+";"+sampleDiagnosis+";"+originTissue+";"+tumorType);
            }


        }


        return mc;
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



    List<String> getDiagnosisMappingLabels(){

        List<String> mapLabels = new ArrayList<>();
        mapLabels.add("DataSource");
        mapLabels.add("SampleDiagnosis");
        mapLabels.add("OriginTissue");
        mapLabels.add("TumorType");

        return mapLabels;
    }



}
