package org.pdxfinder.dataloaders;

import org.pdxfinder.graph.dao.*;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UniversalLoaderOmic extends LoaderProperties implements ApplicationContextAware {

    Logger log = LoggerFactory.getLogger(UniversalLoaderOmic.class);


    protected UtilityService utilityService;

    protected DataImportService dataImportService;

    protected static ApplicationContext context;

    protected ReportManager reportManager;

    public UniversalLoaderOmic(UtilityService utilityService, DataImportService dataImportService) {
        this.utilityService = utilityService;
        this.dataImportService = dataImportService;
    }


    public void loadOmicData(ModelCreation modelCreation, Group providerGroup, String dataType, String providerRootDirectory) {  // csv or xlsx or json

        reportManager = (ReportManager) context.getBean("ReportManager");

        log.trace("Loading {} data for model {} ", dataType, modelCreation.getSourcePdxId());

        List<Map<String, String>> dataList = new ArrayList<>();

        String omicDir = null;

        if(dataType.equals("mutation")){
            omicDir = "mut";

        }
        else if(dataType.equals("copy number alteration")){
            omicDir = "cna";

        }
        else if(dataType.equals("transcriptomics")){
            omicDir = "trans";
        }

        if(omicDir == null) {
            log.error("Cannot determine directory for datatype: "+dataType);
            return;
        }


        String omicDataRootDirUrl = providerRootDirectory + "/"+omicDir;
        File dataRootDir = new File(omicDataRootDirUrl);

        if(dataRootDir.exists()){

            String[] filesInDir = dataRootDir.list();

            if(filesInDir.length > 0){

                //look for the data.xlsx first
                String singleDataXls = omicDataRootDirUrl + "/data.xlsx";
                File singleDataXlsFile = new File(singleDataXls);

                //look for data.json
                String singleDataJson = omicDataRootDirUrl + "/data.json";
                File singleDataJsonFile = new File(singleDataJson);

                //look for data.csv
                String singleDataCsv = omicDataRootDirUrl + "/data.csv";
                File singleDataCsvFile = new File(singleDataCsv);

                //look for modelid.json
                String modelDataJson = omicDataRootDirUrl + "/" + modelCreation.getSourcePdxId() + ".json";
                File modelDataJsonFile = new File(modelDataJson);

                //look for modelid.csv
                String modelDataCsv = omicDataRootDirUrl + "/" + modelCreation.getSourcePdxId() + ".csv";
                File modelDataCsvFile = new File(modelDataCsv);

                if(singleDataXlsFile.exists()){

                    Map<String, List<Map<String, String>> > fullData = utilityService.serializeAndGroupFileContent(singleDataXls,omicModelID);
                    dataList = fullData.get(modelCreation.getSourcePdxId());
                }
                else if(singleDataCsvFile.exists()){

                    Map<String, List<Map<String, String>> > fullData = utilityService.serializeAndGroupFileContent(singleDataCsv,omicModelID);
                    dataList = fullData.get(modelCreation.getSourcePdxId());
                }
                else if(singleDataJsonFile.exists()){

                    Map<String, List<Map<String, String>> > fullData = utilityService.serializeAndGroupFileContent(singleDataJson,omicModelID);
                    dataList = fullData.get(modelCreation.getSourcePdxId());
                }
                else if(modelDataJsonFile.exists()){

                    dataList = utilityService.serializeDataToMaps(modelDataJson);
                }
                else if(modelDataCsvFile.exists()){

                    dataList = utilityService.serializeDataToMaps(modelDataCsv);
                }




            }

        }
        else{

            log.error("Directory doesn't exist: "+omicDataRootDirUrl);
        }




        String modelID = modelCreation.getSourcePdxId();
        Map<String, Platform> platformMap = new HashMap<>();
        Map<String, MolecularCharacterization> existingMolcharNodes = new HashMap<>();
        Map<String, MolecularCharacterization> toBeCreatedMolcharNodes = new HashMap<>();

        //get existing molchar objects and put them in a map
        //first the molchars of the patient sample
        String passage = "";
        if(modelCreation.getSample().getMolecularCharacterizations() != null){

            for(MolecularCharacterization mc : modelCreation.getSample().getMolecularCharacterizations()){

                if (mc != null && mc.getPlatform() != null){

                    String molcharKey = modelCreation.getSample().getSourceSampleId() + "__" + passage + "__" + mc.getPlatform().getName()+ "__patient__"+mc.getType();
                    existingMolcharNodes.put(molcharKey, mc);
                }
            }
        }

        //then all molchars related to xenograft samples

        if(modelCreation.getSpecimens()!= null){

            for(Specimen sp: modelCreation.getSpecimens()){

                Sample sample = sp.getSample();

                if(sample != null && sample.getMolecularCharacterizations() != null){

                    for(MolecularCharacterization mc: sample.getMolecularCharacterizations()){

                        if(sample.getSourceSampleId() == null ) log.error("Missing sampleid for "+modelID);
                        if(sp.getPassage() == null) log.error("Missing passage for "+modelID);
                        if(mc.getPlatform() == null) log.error("Missing platform for "+modelID);

                        if(mc.getPlatform().getName() == null) log.error("Missing platform name for "+modelID);

                        String molcharKey = sample.getSourceSampleId() + "__" + sp.getPassage() + "__" + mc.getPlatform().getName()+ "__xenograft__"+mc.getType();
                        existingMolcharNodes.put(molcharKey, mc);
                    }
                }
            }
        }












        int totalData = 0;
        try {

            totalData = dataList.size();
        }catch (Exception e){

            log.info(" ********* Model ID : {} has no {} data, so skip ********* ", modelID, dataType );
            return;
        }

        log.info(totalData +" "+dataType+" gene variants for model " + modelID);

        int count = 0;

        //PHASE 1: ASSEMBLE OBJECTS IN MEMORY, REDUCING DB INTERACTIONS AS MUCH AS POSSIBLE
        for (Map<String, String> data : dataList ) {

            //STEP 1: GET THE PLATFORM AND CACHE IT
            String platformName = data.get(omicPlatform);
            String platformNameKey = dataSourceAbbreviation+"__" + platformName +"__"+dataType;

            //Skip loading fish!
            if(platformName.equals("Other:_FISH")){
                count++;
                continue;
            }

            Platform platform;

            if(platformMap.containsKey(platformNameKey)){

                platform = platformMap.get(platformNameKey);
            }
            else{

                String platformURLKey = platformName+"_"+dataType;

                platform = dataImportService.getPlatform(platformName, dataType, providerGroup);

                if(platform.getUrl()== null || platform.getUrl().isEmpty()){

                    platform.setUrl(platformURL.get(platformURLKey));
                }

                platformMap.put(platformNameKey, platform);
            }


            // STEP 2: GET THE CACHED MOLCHAR OBJECT OR CREATE ONE IF IT DOESN'T EXIST IN THE MAP, KEY is sampleid__passage__technology
            MolecularCharacterization molecularCharacterization = null;
            passage = (data.get(omicPassage) == null) ? findMyPassage(modelCreation, data.get(omicSampleID), data.get(omicSampleOrigin)) : data.get(omicPassage);

            String origin = (data.get(omicSampleOrigin) == null)? "":data.get(omicSampleOrigin).toLowerCase().trim();

            String molcharKey = data.get(omicSampleID) + "__" + passage + "__" + data.get(omicPlatform)+ "__" + origin+"__"+dataType;




            if(existingMolcharNodes.containsKey(molcharKey)){
                molecularCharacterization = existingMolcharNodes.get(molcharKey);
            }
            else if(toBeCreatedMolcharNodes.containsKey(molcharKey)){
                molecularCharacterization = toBeCreatedMolcharNodes.get(molcharKey);
            }
            else{
                log.info("Looking at molchar "+molcharKey);
                //log.info("Existing keys: ");
                //log.info(existingMolcharNodes.keySet().toString());
                molecularCharacterization = new MolecularCharacterization();
                molecularCharacterization.setType(dataType);
                molecularCharacterization.setPlatform(platform);
                MarkerAssociation markerAssociation = new MarkerAssociation();
                molecularCharacterization.addMarkerAssociation(markerAssociation);
                toBeCreatedMolcharNodes.put(molcharKey, molecularCharacterization);
            }


            //step 3: get the marker suggestion from the service
            NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, modelCreation.getSourcePdxId(), data.get(omicHgncSymbol), dataType, platformNameKey);

            Marker marker;

            if(nsdto.getNode() == null){

                //log.info("Found an unrecognised Marker Symbol {} in Model: {}, Skipping This!!!! ", data.get(omicHgncSymbol), modelID);
                //log.info(data.toString());

                reportManager.addMessage(nsdto.getLogEntity());
                count++;
                continue;
            }
            else{


                // step 4: assemble the MarkerAssoc object and add it to molchar
                marker = (Marker) nsdto.getNode();

                //if we have any message regarding the suggested marker, ie: prev symbol, synonym, etc, add it to the report
                if(nsdto.getLogEntity() != null){
                    reportManager.addMessage(nsdto.getLogEntity());
                }

                MarkerAssociation ma;
                MolecularData md;
                if (dataType.equals("mutation")){

                    md = setVariationProperties(data, marker);
                }else if(dataType.equals("copy number alteration")) {

                    md = setCNAProperties(data, marker);
                }
                else if (dataType.equals("transcriptomics")){

                    md = setTranscriptomicProperties(data, marker);
                }
                else{
                    log.error("Unknown datatype: {}",dataType);
                    md = new MolecularData();
                }


                molecularCharacterization.getMarkerAssociations().get(0).getMolecularDataList().add(md);

            }

            count++;
            if (count % 100 == 0) {
                log.info("loaded {} {} ", count, dataType);
            }
        }
        log.info("loaded " + totalData + " markers for " + modelID);


        //PHASE 2: save existing molchars with new data
        log.info("Saving existing molchars for model "+modelID);
        for(Map.Entry<String, MolecularCharacterization> mcEntry : existingMolcharNodes.entrySet()){
            MolecularCharacterization mc = mcEntry.getValue();
            mc.getFirstMarkerAssociation().encodeMolecularData();
            dataImportService.saveMolecularCharacterization(mc);

        }


        log.info("Saving new molchars for model "+modelID);
        //PHASE 3: get objects from cache and persist them
        for(Map.Entry<String, MolecularCharacterization> mcEntry : toBeCreatedMolcharNodes.entrySet()){

            String mcKey = mcEntry.getKey();
            MolecularCharacterization mc = mcEntry.getValue();

            mc.getFirstMarkerAssociation().encodeMolecularData();

            String[] mcKeyArr = mcKey.split("__");
            String sampleId = mcKeyArr[0];
            String pass = getPassage(mcKeyArr[1]);
            String sampleOrigin = mcKeyArr[3];

            boolean foundSpecimen = false;

            if(sampleOrigin.equalsIgnoreCase("patient tumor") || sampleOrigin.equalsIgnoreCase("patient")){

                Sample patientSample = modelCreation.getSample();
                patientSample.setSourceSampleId(sampleId);
                patientSample.addMolecularCharacterization(mc);
                continue;

            }

            if(modelCreation.getSpecimens() != null){

                for(Specimen specimen : modelCreation.getSpecimens()){

                    if(specimen.getPassage().equals(pass)){

                        if(specimen.getSample() != null && specimen.getSample().getSourceSampleId().equals(sampleId)){

                            Sample xenograftSample = specimen.getSample();
                            xenograftSample.addMolecularCharacterization(mc);

                            foundSpecimen = true;

                        }
                    }

                }

            }
            //this passage is either not present yet or the linked sample has a different ID, create a specimen with sample and link mc
            if(!foundSpecimen){
                log.info("Creating new specimen for "+mcKey);

                Sample xenograftSample = new Sample();
                xenograftSample.setSourceSampleId(sampleId);
                xenograftSample.addMolecularCharacterization(mc);

                Specimen specimen = new Specimen();
                specimen.setPassage(pass);
                specimen.setSample(xenograftSample);


                modelCreation.addRelatedSample(xenograftSample);
                modelCreation.addSpecimen(specimen);
            }
        }

        dataImportService.saveModelCreation(modelCreation);

    }



    private MolecularData setVariationProperties(Map<String,String> data, Marker marker){

        MolecularData ma = new MolecularData();
        ma.setAminoAcidChange(data.get(omicAminoAcidChange));
        ma.setConsequence(data.get(omicConsequence));
        ma.setAlleleFrequency(data.get(omicAlleleFrequency));
        ma.setChromosome(data.get(omicChromosome));
        ma.setReadDepth(data.get(omicReadDepth));
        ma.setRefAllele(data.get(omicRefAllele));
        ma.setAltAllele(data.get(omicAltAllele));
        ma.setGenomeAssembly(data.get(omicGenomeAssembly));
        ma.setExistingVariations(data.get(omicRsIdVariants));
        ma.setSeqStartPosition(data.get(omicSeqStartPosition));

        ma.setEnsemblTranscriptId(data.get(omicEnsemblTranscriptId));
        ma.setNucleotideChange(data.get(omicNucleotideChange));
        ma.setMarker(marker.getHgncSymbol());

        return  ma;
    }



    private MolecularData setCNAProperties(Map<String,String> data, Marker marker){

        MolecularData md = new MolecularData();
        //setHostStrain Name
        md.setChromosome(data.get(omicChromosome));
        md.setSeqStartPosition(data.get(omicSeqStartPosition));
        md.setSeqEndPosition(data.get(omicSeqEndPosition));
        md.setCnaLog10RCNA(data.get(omicCnaLog10RCNA));
        md.setCnaLog2RCNA(data.get(omicCnaLog2RCNA));
        md.setCnaCopyNumberStatus(data.get(omicCnaCopyNumberStatus));
        md.setCnaGisticValue(data.get(omicCnaGisticvalue));
        md.setCnaPicnicValue(data.get(omicCnaPicnicValue));
        md.setGenomeAssembly(data.get(omicGenomeAssembly));

        marker.setHgncSymbol(data.get(omicHgncSymbol));
        marker.setUcscGeneId(data.get(omicUcscGeneId));
        marker.setNcbiGeneId(data.get(omicNcbiGeneId));
        marker.setEnsemblGeneId(data.get(omicEnsemblGeneId));

        md.setMarker(marker.getHgncSymbol());
        return  md;
    }


    private MolecularData setTranscriptomicProperties(Map<String,String> data, Marker marker){

        MolecularData ma = new MolecularData();
        ma.setChromosome(data.get(omicChromosome));
        ma.setSeqStartPosition(data.get(omicSeqStartPosition));
        ma.setSeqEndPosition(data.get(omicSeqEndPosition));
        ma.setRnaSeqCoverage(data.get(rnaSeqCoverage));
        ma.setRnaSeqFPKM(data.get(rnaSeqFPKM));
        ma.setRnaSeqTPM(data.get(rnaSeqTPM));
        ma.setRnaSeqCount(data.get(rnaSeqCount));
        ma.setAffyHGEAProbeId(data.get(affyHGEAProbeId));
        ma.setAffyHGEAExpressionValue(data.get(affyHGEAExpressionValue));
        ma.setIlluminaHGEAProbeId(data.get(illuminaHGEAProbeId));
        ma.setIlluminaHGEAExpressionValue(data.get(illuminaHGEAExpressionValue));
        ma.setGenomeAssembly(data.get(omicGenomeAssembly));
        ma.setZscore(data.get(omicZscore));

        ma.setMarker(marker.getHgncSymbol());

        return  ma;
    }


    public String getPassage(String passageString) {

        if(!passageString.isEmpty() && passageString.toUpperCase().contains("P")){

            passageString = passageString.toUpperCase().replace("P", "");
        }
        //does this string have digits only now?
        if(passageString.matches("\\d+")) return passageString;

        //is this a double? ie: 1.0
        if(passageString.matches("(\\d)\\.(\\d)")){

            try {
                double p = Double.parseDouble(passageString);
                int i = (int) p;
                return String.valueOf(i);
            }
            catch (Exception e){

                log.warn("Unable to determine passage from sample name " + passageString + ". Assuming 0");
                return "0";
            }

        }

        log.warn("Unable to determine passage from sample name " + passageString + ". Assuming 0");
        return "0";

    }

    public String findMyPassage(ModelCreation modelCreation, String sampleId, String sampleOrigin){

        String passage = null;

        if(!sampleOrigin.toLowerCase().equals("xenograft")){
            passage = "";
        }else {
            for(Specimen specimen : modelCreation.getSpecimens()){

                if(specimen.getSample() != null && specimen.getSample().getSourceSampleId().equals(sampleId)){
                    passage = specimen.getPassage();
                    log.info("Passage: {} found for {} in the database, Good Data",passage, sampleId); //TODO: remove This
                    break;
                }
            }

            if (passage == null){
                log.error("Passage not found for Xenograft Sample {} both in the data File and database, Wrong Data", sampleId);
            }
        }
        return passage;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


}
