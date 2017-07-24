package org.pdxfinder.services;

import org.pdxfinder.dao.OntologyTerm;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.OntologyTermRepository;
import org.pdxfinder.repositories.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jmason on 25/05/2017.
 */

@Service
public class GraphService
{


        private final static Logger log = LoggerFactory.getLogger(GraphService.class);

    private SampleRepository sampleRepository;
    private OntologyTermRepository ontologyTermRepositoryRepository;

        public GraphService(SampleRepository sampleRepository,OntologyTermRepository ontologyTermRepository) {
            this.sampleRepository = sampleRepository;
            this.ontologyTermRepositoryRepository = ontologyTermRepository;
        }



        public Set<String> getMappedDOTerms()
        {

                Collection<OntologyTerm> ontologyTerms = ontologyTermRepositoryRepository.findAllWithMappings();
                Set<String> dataReport = new HashSet<>();

                for (OntologyTerm ontologyTerm : ontologyTerms){

                    if(ontologyTerm.getLabel() != null) {
                        dataReport.add(ontologyTerm.getLabel());
                    }
                }
                // Arrange the result alphabetically
                Set<String> sortedData = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
                sortedData.addAll(dataReport);

                return sortedData;

        }




        public Map<String, Integer> getCancerSubtypeCounts()
        {

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









        /**  Returns the counts of samples in the data base by top level tissue type */
        //TODO: Replace with dynamic query to top level when ontology associations are made
        public Map<String, Integer> getModelCountsBySystem()
        {

                log.info("Entering getModelCountsBySystem");
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









        /**  Returns the counts of tumors in the data base by cell type */
        //TODO: Replace with dynamic query to top level when ontology associations are made
        public Map<String, Integer> getModelCountsByCellType()
        {

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









        /**  Returns the counts of tumors in the data base by cell type */
        //TODO: Replace with dynamic query to top level when ontology associations are made
        public Map<String, Integer> getModelCountsByTissue()
        {

                log.info("Entering getModelCountsByCellType");
                Map<String, Integer> cancerByTissue = new HashMap<>();


                cancerByTissue.put("urinary bladder cancer", 28);
                cancerByTissue.put("lung cancer", 77);
                cancerByTissue.put("hematologic cancer", 3);
                cancerByTissue.put("colon cancer", 496);
                cancerByTissue.put("prostate cancer", 3);
                cancerByTissue.put("rectum cancer", 10);
                cancerByTissue.put("cecum carcinoma", 3);
                cancerByTissue.put("ovarian cancer", 13);
                cancerByTissue.put("liver cancer", 2);
                cancerByTissue.put("duodenum cancer", 1);
                cancerByTissue.put("anus cancer", 2);
                cancerByTissue.put("pancreatic cancer", 19);
                cancerByTissue.put("stomach cancer", 2);
                cancerByTissue.put("ampulla of vater cancer", 4);
                cancerByTissue.put("brain cancer", 34);
                cancerByTissue.put("muscle cancer", 17);
                cancerByTissue.put("appendix cancer", 2);
                cancerByTissue.put("bone cancer", 12);
                cancerByTissue.put("breast cancer", 31);
                cancerByTissue.put("bile duct cancer", 4);
                cancerByTissue.put("connective and soft tissue cancer", 13);
                cancerByTissue.put("b cell lymphoma", 1);
                cancerByTissue.put("endometrial cancer", 1);
                cancerByTissue.put("head and neck cancer", 1);
                cancerByTissue.put("Hodgkins lymphoma", 1);
                cancerByTissue.put("kidney cancer", 10);
                cancerByTissue.put("peripheral nervous system cancer", 1);
                cancerByTissue.put("germ cell cancer", 1);
                cancerByTissue.put("skin cancer", 20);
                cancerByTissue.put("salivary gland cancer", 2);
                cancerByTissue.put("retroperitoneal cancer ", 1);
                cancerByTissue.put("adrenal gland cancer", 1);
                cancerByTissue.put("lipomatous cancer", 1);
                cancerByTissue.put("kidney benign neoplasm", 1);
                cancerByTissue.put("uterine cancer", 1);
                cancerByTissue.put("esophageal cancer", 1);
                cancerByTissue.put("neck cancer", 1);
                cancerByTissue.put("head cancer", 4);
                cancerByTissue.put("tonsil cancer", 2);
                cancerByTissue.put("bladder cancer", 1);
                return cancerByTissue;
        }




}
