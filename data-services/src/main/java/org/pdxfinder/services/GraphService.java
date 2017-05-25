package org.pdxfinder.services;

import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jmason on 25/05/2017.
 */
@Service
public class GraphService {

    private final static Logger log = LoggerFactory.getLogger(GraphService.class);

    private SampleRepository sampleRepository;

    public GraphService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    public Map<String, Integer> getCancerSubtypeCounts() {

        log.info("Entering method getCancerSubtypeCounts");


        // Map name of subtype to integer representing the count of that subtype in the database
        Map<String, Integer> cancerSubtypes = new HashMap<>();

        Iterable<Sample> allSamples = sampleRepository.findAll();

        for (Sample sample : allSamples) {

            // Increment the count for a specific subtype
            Integer value = cancerSubtypes.get(sample.getType().getName());
            cancerSubtypes.put(sample.getType().getName(), (value == null) ? 1 : value + 1);

        }

        return cancerSubtypes;
    }


    /**
     * Returns the counts of samples in the data base by top level tissue type
     */
    //TODO: Replace with dynamic query to top level when ontology associations are made
    public Map<String, Integer> getModelCountsByTissue() {

        log.info("Entering getModelCountsByTissue");
        Map<String, Integer> typesByTissue = new HashMap<>();

        typesByTissue.put("breast", 31);
        typesByTissue.put("connective and soft tissue", 12);
        typesByTissue.put("endocrine gland", 19);
        typesByTissue.put("gastrointestinal", 593);
        typesByTissue.put("head and neck", 6);
        typesByTissue.put("immune", 5);
        typesByTissue.put("intergumentary", 20);
        typesByTissue.put("musculoskeletal", 29);
        typesByTissue.put("nervous", 37);
        typesByTissue.put("reproductive organ", 18);
        typesByTissue.put("respiratory", 77);
        typesByTissue.put("urinary", 39);

        return typesByTissue;
    }

    /**
     * Returns the counts of tumors in the data base by cell type
     */
    //TODO: Replace with dynamic query to top level when ontology associations are made
    public Map<String, Integer> getModelCountsByCellType() {

        log.info("Entering getModelCountsByCellType");
        Map<String, Integer> modelsByCellType = new HashMap<>();

        modelsByCellType.put("adenocarcinoma", 532);
        modelsByCellType.put("carcinoma", 87);
        modelsByCellType.put("squamous cell carcinoma", 42);
        modelsByCellType.put("non small cell carcinoma", 38);
        modelsByCellType.put("small cell carcinoma", 5);
        modelsByCellType.put("papillary serous adenocarcinoma", 3);
        modelsByCellType.put("carcinosarcoma", 1);
        modelsByCellType.put("large cell carcinoma", 1);
        modelsByCellType.put("papillary carcinoma", 1);
        modelsByCellType.put("transitional cell carcinoma", 1);
        modelsByCellType.put("sarcoma", 34);
        modelsByCellType.put("rhabdomyosarcoma", 6);
        modelsByCellType.put("osteosarcoma", 2);
        modelsByCellType.put("liposarcoma", 1);
        modelsByCellType.put("spindle cell sarcoma", 1);
        modelsByCellType.put("malignant glioma", 34);
        modelsByCellType.put("neuroblastoma", 2);
        modelsByCellType.put("melanoma", 16);
        modelsByCellType.put("teratoma", 1);
        modelsByCellType.put("leukemia", 3);
        modelsByCellType.put("lymphoma", 2);
        modelsByCellType.put("malignant mesothelioma", 1);

        return modelsByCellType;
    }
}
