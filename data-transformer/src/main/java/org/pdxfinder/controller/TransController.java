package org.pdxfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;


@RestController
@RequestMapping("/transformer")
public class TransController {

    private final static Logger log = LoggerFactory.getLogger(TransController.class);
    private String homeDir = System.getProperty("user.home");

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

/*
        List<String> csvHead = Arrays.asList("datasource","Model_ID","Sample_ID","sample_origin","Passage","host_strain_name","hgnc_symbol","amino_acid_change","nucleotide_change",
                "consequence","read_depth","Allele_frequency","chromosome","seq_start_position","ref_allele","alt_allele","ucsc_gene_id",
                "ncbi_gene_id","ensembl_gene_id","ensembl_transcript_id","rs_id_Variant","genome_assembly","Platform");*/



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


        String destination = homeDir+"/Downloads/data.csv";
        utilityService.writeCsvFile(dataList, destination);

        return dataList;
    }



    @RequestMapping("/rewrite-ircc-cna-to-json")
    public Object irccCNA() {

        // Get CNA xls Template
        String cnaFile = System.getProperty("user.home") + "/PDXFinder/data/IRCC-CRC/cna/CNA- IRCC-CRC template.xlsx";

        List<Map<String, String>> dataList = utilityService.serializeExcelDataNoIterator(cnaFile,0,1);

        return dataList;
    }



    @RequestMapping("/rewrite-ircc-json")
    public Object irccTransformJson() {

        String jsonFile= System.getProperty("user.home") + "/PDXFinder/data/IRCC-CRC/mut/data.json";

        List<Map<String, String>> dataList = utilityService.serializeJSONToMaps(jsonFile, "IRCCVariation");
        List<Map<String, String>> newDataList = new ArrayList<>();

        for (Map<String, String> data : dataList) {

            String modelId = data.get("Model ID");
            String specimenId = data.get("Specimen ID");


            Map<String, String> newData = new LinkedHashMap<>();

            newData.put("datasource", "IRCC-CRC");
            newData.put("Model_ID", data.get("Model ID"));
            newData.put("Sample_ID", specimenId);

            if ( specimenId.startsWith(modelId+"H") ){
                newData.put("sample_origin", "patient tumor");
            } else if (specimenId.startsWith(modelId+"X")){
                newData.put("sample_origin", "Xenograft");
            }

            newData.put("Passage", null);
            newData.put("host_strain_name", "");
            newData.put("hgnc_symbol", data.get("Gene"));
            newData.put("amino_acid_change", data.get("Protein"));
            newData.put("nucleotide_change", "");
            newData.put("consequence", data.get("Effect"));
            newData.put("read_depth", "");
            newData.put("Allele_frequency", data.get("VAF"));
            newData.put("chromosome", data.get("Chrom"));
            newData.put("seq_start_position", data.get("Pos"));
            newData.put("ref_allele", data.get("Ref"));
            newData.put("alt_allele", data.get("Alt"));
            newData.put("ucsc_gene_id", "");
            newData.put("ncbi_gene_id", "");
            newData.put("ensembl_gene_id", "");
            newData.put("ensembl_transcript_id", "");
            newData.put("rs_id_Variant", data.get("avsnp147"));
            newData.put("genome_assembly", "");
            newData.put("Platform", "TargetedNGS_MUT");


            newDataList.add(newData);
        }


        /*

            "datasource": "IRCC-CRC",
            "Model_ID": "CRC0112LM",                                Model ID
            "Sample_ID": "CRC0112LMH0000000000D01000",              Sample ID
            "sample_origin": "patient tumor",                       modelId+"H" [patient Tumor] , modelId+"X" [Xenograft]
            "Passage": "",                                          null
            "host_strain_name": "",                                 "" *****
            "hgnc_symbol": "A2M",                                   Gene
            "amino_acid_change": "R823W",                           Protein
            "nucleotide_change": "",                                "" *****
            "consequence": "Missense_Mutation",                     Effect
            "read_depth": "",                                       "" *****
            "Allele_frequency": "",                                 VAF
            "chromosome": "12",                                     Chrom
            "seq_start_position": "9243799",                        Pos *****
            "ref_allele": "",                                       Ref
            "alt_allele": "",                                       Alt
            "ucsc_gene_id": "",                                     "" *****
            "ncbi_gene_id": "2",                                    "" *****
            "ensembl_gene_id": "",                                  "" *****
            "ensembl_transcript_id": "",                            "" *****
            "rs_id_Variant": "",                                    avsnp147
            "genome_assembly": "37",
            "Platform": "whole exome sequencing                     TargetedNGS_MUT   ... platformURL.get("targetedNgsPlatformURL")



            ma.setCdsChange(variation.getString("CDS"));
            ma.setType(type);

        */


        // Get Exome Template
        String exomeFile = System.getProperty("user.home") + "/PDXFinder/data/IRCC-CRC/exome_IRCC-CRC template.xlsx";

        List<Map<String, String>> dataList2 = utilityService.serializeExcelDataNoIterator(exomeFile,0,1);

        return ListUtils.union(dataList2, newDataList);
    }






    @RequestMapping("/rewrite-curie-lc-cna")
    public Object curieTransform() {

        String cnaFile= System.getProperty("user.home") + "/Downloads/curie-cna.xlsx";

        List<Map<String, String>> dataList = utilityService.serializeExcelDataNoIterator(cnaFile,0,1);

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


        String destination = homeDir+"/Downloads/curie-cna.csv";
        utilityService.writeCsvFile(dataList, destination);

        return dataList;
    }








    // FIX DERIVED DATA SHEET / PDX DETAILS IN CRL DATA
    @RequestMapping("/rewrite-crl")
    public Object crlTransform() {


        // UPDATE DERIVED DATA SHEET
        String templateFile= System.getProperty("user.home") + "/Downloads/DATA/crl.xlsx";
        List<Map<String, String>> derivedDataSheet = utilityService.serializeExcelDataNoIterator(templateFile,6,4);

        List removedList = new ArrayList();


        for (Map<String, String> data : derivedDataSheet) {

            String modelID = data.get("Model ID");

            // Replace Model Ids with prefix CRL-
            data.put("Model ID","CRL-"+modelID);

            // Deduct 1 from all passages
            String passage = data.get("Passage");
            try {

                String cleanPassage = Integer.toString(Integer.parseInt(passage) - 1);
                data.put("Passage", cleanPassage);
            }catch (Exception e){

                log.error("{}'s passage {} is not a meaningful passage data ", modelID, passage);

                // Trash it now, but treat later
                removedList.add(data);
            }

            // Change Sample Origin to Xenograft
            data.put("Sample origin","xenograft");

            // remove Gene Expression Data
            if ( data.get("Molecular Characterization type").contains("Gene expression") ){

                removedList.add(data);
            }
        }
        derivedDataSheet.removeAll(removedList);

        // Capture Map Keys to use as excel head
        List<String> xlsHead = new ArrayList<>();
        for (Map.Entry<String, String> entry : derivedDataSheet.get(0).entrySet() ) {
            xlsHead.add(entry.getKey());
        }


        String destination = homeDir+"/Downloads/CRL-Derived_dataset_from_tumor.csv";
        utilityService.writeCsvFile(derivedDataSheet, destination);
        utilityService.writeXLSXFile(derivedDataSheet,"CRL-Derived_dataset_from_tumor.xlsx", "Derived_dataset_from_tumor");


        // UPDATE PDX DETAILS SHEET

        List<Map<String, String>> pdxModelDetailSheet = utilityService.serializeExcelDataNoIterator(templateFile,4,4);
        //pdxModelDetailSheet.remove(pdxModelDetailSheet.get(0));

        // Group DerivedDataSheet By ModelID
        Map<String, List<Map<String, String>> > groupedDerivedDataSheet = utilityService.groupDataByColumn(derivedDataSheet,"Model ID");


        // Pull Model from ModelDetailSheet and look Up in DerivedDataSheet using the grouped version, pick the Passages and add to ModelDetailSheet
        for (Map<String, String> modelDetail : pdxModelDetailSheet) {

            String modelID = modelDetail.get("Model ID");
            String modelDetailPassage = modelDetail.get("Passage");

            // Look up ModelID in groupedDerivedDataSheet
            List<Map<String, String>> derivedDatas = groupedDerivedDataSheet.get(modelID);

            // Pick the New Passages from derivedData and add to ModelDetailSheet
            Set<String> derivedDataPassages = new HashSet<>();
            derivedDataPassages.add(modelDetailPassage);

            try{

                for (Map<String, String> derivedData : derivedDatas ){

                    derivedDataPassages.add(derivedData.get("Passage"));
                }
                modelDetail.put("Passage", StringUtils.join(derivedDataPassages, ','));

            }catch (Exception e){

                log.error(modelID+" Found in Model Detail, but not in derived data");
            }
        }



        // Capture Map Keys to use as excel head
        xlsHead = new ArrayList<>();
        for (Map.Entry<String, String> entry : pdxModelDetailSheet.get(0).entrySet() ) {
            xlsHead.add(entry.getKey());
        }


        destination = homeDir+"/Downloads/CRL-PDX_Model_detail.csv";
        utilityService.writeCsvFile(pdxModelDetailSheet, destination);
        utilityService.writeXLSXFile(pdxModelDetailSheet,"CRL-PDX_Model_detail.xlsx", "PDX_Model_detail");


        return pdxModelDetailSheet;
    }





    @RequestMapping("/move-crl-omic-file")
    public Object moveFile() {



        String templateFile= System.getProperty("user.home") + "/Downloads/template.xlsx";
        List<Map<String, String>> data = utilityService.serializeExcelDataNoIterator(templateFile,6,4);
        data.remove(data.get(0));

        for (Map<String, String> dData : data){

            String sampleID = dData.get("sample ID");
            String dataType = dData.get("Molecular Characterization type");

            String sourceDir = (dataType.equals("Mutation")) ? "Mutation" : "Copy_Numbers";
            String destinationDir = sourceDir+"2";
            String fileSuffix = (dataType.equals("Mutation")) ? "_mutation_list_pdxfinder.csv" : "_cna_list_pdxfinder.csv";

            String source = System.getProperty("user.home") + "/Downloads/TEMP/"+sourceDir+"/"+sampleID+fileSuffix;

            String destination = System.getProperty("user.home") + "/Downloads/TEMP/"+destinationDir+"/"+sampleID+fileSuffix;

            utilityService.moveFile(source,destination);

            log.info("Moving {} File ...  ",dataType);

        }

        return data;
    }





    @RequestMapping("/clean-crl-omic-files")
    public Object cleanCRLOMICFiles() {


        List<String> csvHead = Arrays.asList("datasource","Model_ID","Sample_ID","sample_origin","Passage","host_strain_name","hgnc_symbol","amino_acid_change","nucleotide_change",
                "consequence","read_depth","Allele_frequency","chromosome","seq_start_position","ref_allele","alt_allele","ucsc_gene_id",
                "ncbi_gene_id","ensembl_gene_id","ensembl_transcript_id","rs_id_Variant","genome_assembly","Platform");


        String templateFile= homeDir + "/Downloads/template.xlsx";
        List<Map<String, String>> data = utilityService.serializeExcelDataNoIterator(templateFile,6,4);
        data.remove(data.get(0));


        for (Map<String, String> dData : data){

            String sampleID = dData.get("sample ID");
            String dataType = dData.get("Molecular Characterization type");

            String sourceDir = (dataType.equals("Mutation")) ? "Mutation2" : "Copy_Numbers2";


            String fileSuffix = (dataType.equals("Mutation")) ? "_mutation_list_pdxfinder.csv" : "_cna_list_pdxfinder.csv";

            String destinationDir = sourceDir+"-Final";


            String source = homeDir+"/Downloads/TEMP/"+sourceDir+"/"+sampleID+fileSuffix;

            log.info(source);

            List<Map<String, String>> dataList = utilityService.serializeCSVToMaps(source);


            String modelID = "";
            for (Map<String, String> dataRow : dataList) {

                modelID = "CRL-"+dataRow.get("Model_ID");
                dataRow.put("Model_ID", modelID);
            }

            String destination = homeDir+"/Downloads/TEMP/"+destinationDir+"/"+modelID+".csv";

            utilityService.writeCsvFile(dataList, destination);

            log.info("Created {} File ...  in {} ", dataType, destination);

        }

        return data;
    }



    @RequestMapping("/get-files")
    public Object getAllFiles() {

        String source = homeDir+"/Downloads/TEMP/Mutation/";

        utilityService.listAllFilesInADirectory(source);

        return "success";
    }


    @RequestMapping("/get-files-size")
    public long whenGetReadableSize_thenCorrect() {

        long directorySize = directorySize(homeDir+"/Downloads/TEMP/Mutation2-Final/");

        long oneFileSize = oneFileSize(homeDir+"/Downloads/TEMP/Mutation2-Final/CRL-97.csv");


        // Time taken divided by file size times everything:

        return oneFileSize;
    }


    public long oneFileSize(String fileLink) {

        File file =new File(fileLink);

        long fileSize = file.length();

        return fileSize;
    }


    public long directorySize(String dirLink){

        long size = 0;

        Path folder = Paths.get(dirLink);
        try {

            size = Files.walk(folder)
                    .filter(p -> p.toFile().isFile())
                    .mapToLong(p -> p.toFile().length())
                    .sum();

        }catch (Exception e){ }

        return size;
    }





}









