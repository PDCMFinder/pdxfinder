package org.pdxfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdxfinder.rdbms.dao.MappingEntity;
import org.pdxfinder.rdbms.repositories.MappingEntityRepository;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.transcommands.DataTransformerService;
import org.pdxfinder.transdatamodel.PdmrPdxInfo;
import org.pdxfinder.transdatamodel.PdxInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@RestController
@RequestMapping("/transformer")
public class TransController {

    private final static Logger log = LoggerFactory.getLogger(TransController.class);

    private RestTemplate restTemplate = new RestTemplate();
    private ObjectMapper mapper = new ObjectMapper();
    private DataTransformerService dataTransformerService;
    @Autowired
    private UtilityService utilityService;
    @Autowired
    private MappingEntityRepository mappingEntityRepository;



    public TransController(DataTransformerService dataTransformerService, RestTemplateBuilder restTemplateBuilder){
        this.dataTransformerService = dataTransformerService;
        this.restTemplate = restTemplateBuilder.build();
    }


    @GetMapping("/view-data")
    public PdxInfo getAllPdmr()
    {
        List<PdmrPdxInfo> pdmrPdxInfos = dataTransformerService.getAllPdmr();

        PdxInfo pdxInfo = new PdxInfo();
        pdxInfo.setPdxInfo(pdmrPdxInfos);

        return pdxInfo;
    }


    @GetMapping("/drugs")
    public String getAllPdmrDrugs()
    {
        List<PdmrPdxInfo> pdmrPdxInfos = dataTransformerService.getAllPdmr();

        String drugList = "";

        for (PdmrPdxInfo pdmrPdxInfo : pdmrPdxInfos){

            drugList += dataTransformerService.getDrugs(pdmrPdxInfo);
        }

        return drugList;
    }



    @GetMapping("/transform-pdmr-data")
    public List<Map> connectPdmr(){

        List<Map> mappingList = dataTransformerService.transformDataAndSave();
        return mappingList;

    }

    /*
    Test Saving Mapping Entity in H2 Database */

    @GetMapping("/mapping")
    public List<MappingEntity> saveMapping(){

        ArrayList<String> mappingLabels = new ArrayList<>();
        mappingLabels.add("DataSource");
        mappingLabels.add("SampleDiagnosis");
        mappingLabels.add("TumorType");
        mappingLabels.add("OriginTissue");

        Map mappingValues = new HashMap();
        mappingValues.put("OriginTissue","blood");
        mappingValues.put("TumorType","primary");
        mappingValues.put("SampleDiagnosis","acute myeloid leukemia");
        mappingValues.put("DataSource","jax");

        MappingEntity mappingEntity = new MappingEntity("DIAGNOSIS", mappingLabels, mappingValues);

        mappingEntity.setMappedTermLabel("Acute Myeloid Leukemia");
        mappingEntity.setStatus("Created");
        mappingEntity.setDateCreated(new Date());
        mappingEntity.setMappedTermUrl("http://purl.obolibrary.org/obo/NCIT_C3171");
        mappingEntity.setMapType("direct");
        mappingEntity.setJustification("0");

        //mappingEntityRepository.save(mappingEntity);

        return mappingEntityRepository.findAll();

    }


    @GetMapping("/get-mapping")
    public List<MappingEntity> getMapping(){

        return mappingEntityRepository.findAll();

    }


    private String omicFile= System.getProperty("user.home") + "/Downloads/NGS-LOADER/ncicancergenepaneldata_MARCH2019.csv";

    @RequestMapping("/rewrite-pdmr-omic-data")
    public Object downloads() {

        List<Map<String, String>> dataList = utilityService.serializeCSVToMaps(omicFile);


        List<String> csvHead = Arrays.asList("datasource","Model_ID","Sample_ID","sample_origin","Passage","host_strain_name","hgnc_symbol","amino_acid_change","nucleotide_change",
                "consequence","read_depth","Allele_frequency","chromosome","seq_start_position","ref_allele","alt_allele","ucsc_gene_id",
                "ncbi_gene_id","ensembl_gene_id","ensembl_transcript_id","rs_id_Variant","genome_assembly","Platform");



        List removedList = new ArrayList();
        for (Map<String, String> data : dataList) {

            if ( !data.get("Sample_ID").equals("ORIGINATOR") ){

                String modelID = data.get("Model_ID");
                String sampleID = data.get("Sample_ID");

                String passage = dataTransformerService.getPassageByModelIDAndSampleID(modelID, sampleID);

                data.put("Passage", passage);

                /*if (passage.equals("XXXX")){
                    removedList.add(data);
                }*/
            }


            if ( data.get("hgnc_symbol").equals("None Found") ){

                removedList.add(data);
            }

        }

        dataList.removeAll(removedList);


        utilityService.writeCsvFile(dataList,csvHead, "data.csv");

        return dataList;
    }






    @RequestMapping("/rewrite-curie-lc-cna")
    public Object curieTransform() {

        String cnaFile= System.getProperty("user.home") + "/Downloads/curie-cna.xlsx";

        List<Map<String, String>> dataList = utilityService.serializeExcelDataNoIterator(cnaFile,0,1);


        List<String> xlsHead = Arrays.asList("datasource","Model_ID","Sample_ID","sample_origin","Passage","host_strain_name","chromosome","seq_start_position","seq_end_position",
                "Probe_ID_affymetrix","hgnc_symbol","ucsc_gene_id","ncbi_gene_id","ensembl_gene_id","log10R_cna","log2R_cna","copy_number_status",
                "gistic_value_cna","genome_assembly","Platform");


        for (Map<String, String> data : dataList) {

            String modelID = data.get("Model_ID");

            if (modelID.equals("LCF4")){

                data.put("Sample_ID","LCF4p09/09/2009");
                data.put("sample_origin","engrafted tumor");
                data.put("Passage","4");
                data.put("Platform","CGH array");

            }

            if (modelID.equals("LCF9")){

                data.put("Sample_ID","LCF9p16:19/05/2015");
                data.put("sample_origin","engrafted tumor");
                data.put("Passage","16");
                data.put("Platform","CGH array");

            }

            if (modelID.equals("LCF16")){

                data.put("Sample_ID","LCF16p27:26/08/2016");
                data.put("sample_origin","engrafted tumor");
                data.put("Passage","27");
                data.put("Platform","CGH array");

            }

            if (modelID.equals("LCF26")){

                data.put("Sample_ID","LCF26p3:23/09/2011");
                data.put("sample_origin","engrafted tumor");
                data.put("Passage","3");
                data.put("Platform","CGH array");

            }

        }

        utilityService.writeCsvFile(dataList,xlsHead, "curie-cna.csv");

        return dataList;
    }






}





