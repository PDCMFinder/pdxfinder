package org.pdxfinder.commands.dataloaders;

import org.pdxfinder.graph.dao.*;
import org.pdxfinder.reportmanager.ReportManager;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.dto.NodeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

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


    public void loadOmicData(ModelCreation modelCreation, Group providerGroup, String dataType) {  // csv or xlsx or json

        reportManager = (ReportManager) context.getBean("ReportManager");

        List<Map<String, String>> dataList;

        String omicDir = (dataType.equals("mutation")) ? "mut" : "cna";

        if (omicDataFilesType.equals("ONE_FILE_PER_MODEL")){

            // THIS HANDLES SITUATIONS WHERE OMIC DATA IS PROVIDED AS 100s OF CSV/JSON WITH ONE_FILE_PER_MODEL
            String modelID = modelCreation.getSourcePdxId();
            dataList = utilityService.serializeDataToMaps(dataRootDirectory+dataSourceAbbreviation+"/"+omicDir+"/"+modelID+"."+omicFileExtension);

        }else {

            // THIS HANDLES SITUATIONS WHERE OMIC DATA IS PROVIDED AS A SINGLE CSV/JSON WITH ALL_MODELS_IN_ONE_FILE
            String variationURLStr = dataRootDirectory+dataSourceAbbreviation+"/"+omicDir+"/data."+omicFileExtension;
            Map<String, List<Map<String, String>> > fullData = utilityService.serializeAndGroupFileContent(variationURLStr,omicModelID);

            dataList = fullData.get(modelCreation.getSourcePdxId());
        }


        String modelID = modelCreation.getSourcePdxId();
        Map<String, Platform> platformMap = new HashMap<>();
        Map<String, MolecularCharacterization> molcharMap = new HashMap<>();

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
            String technology = data.get(omicPlatform);

            //Skip loading fish!
            if(technology.equals("Other:_FISH")){
                count++;
                continue;
            }

            Platform platform;
            if(platformMap.containsKey(technology)){

                platform = platformMap.get(technology);
            }
            else{

                String platformURLKey = technology.replace("\\s","_");

                platform = dataImportService.getPlatform(technology, providerGroup, platformURL.get(platformURLKey));
                platformMap.put(technology, platform);
            }


            // STEP 2: GET THE CACHED MOLCHAR OBJECT OR CREATE ONE IF IT DOESN'T EXIST IN THE MAP, KEY is sampleid__passage__technology
            MolecularCharacterization molecularCharacterization;
            String passage = (data.get(omicPassage) == null) ? findMyPassage(modelCreation, data.get(omicSampleID), data.get(omicSampleOrigin)) : data.get(omicPassage);


            String molcharKey = data.get(omicSampleID) + "__" + passage + "__" + data.get(omicPlatform)+ "__" + data.get(omicSampleOrigin);

            if(molcharMap.containsKey(molcharKey)){

                molecularCharacterization = molcharMap.get(molcharKey);
            }
            else{

                molecularCharacterization = new MolecularCharacterization();
                molecularCharacterization.setType(dataType);
                molecularCharacterization.setPlatform(platform);
                molcharMap.put(molcharKey, molecularCharacterization);
            }


            //step 3: get the marker suggestion from the service
            NodeSuggestionDTO nsdto = dataImportService.getSuggestedMarker(this.getClass().getSimpleName(), dataSourceAbbreviation, modelCreation.getSourcePdxId(), data.get(omicHgncSymbol), dataType, technology);

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

                if (dataType.equals("mutation")){

                    ma = setVariationProperties(data, marker);
                }else {

                    ma = setCNAProperties(data, marker);
                }

                molecularCharacterization.addMarkerAssociation(ma);

            }

            count++;
            if (count % 100 == 0) {
                log.info("loaded {} {} ", count, dataType);
            }
        }
        log.info("loaded " + totalData + " markers for " + modelID);


        //PHASE 2: get objects from cache and persist them
        for(Map.Entry<String, MolecularCharacterization> mcEntry : molcharMap.entrySet()){

            String mcKey = mcEntry.getKey();
            MolecularCharacterization mc = mcEntry.getValue();

            String[] mcKeyArr = mcKey.split("__");
            String sampleId = mcKeyArr[0];
            String pass = getPassage(mcKeyArr[1]);
            String sampleOrigin = mcKeyArr[3];

            boolean foundSpecimen = false;

            if(sampleOrigin.toLowerCase().equals("patient tumor")){

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



    private MarkerAssociation setVariationProperties(Map<String,String> data, Marker marker){

        MarkerAssociation ma = new MarkerAssociation();
        ma.setAminoAcidChange(data.get(omicAminoAcidChange));
        ma.setConsequence(data.get(omicConsequence));
        ma.setAlleleFrequency(data.get(omicAlleleFrequency));
        ma.setChromosome(data.get(omicChromosome));
        ma.setReadDepth(data.get(omicReadDepth));
        ma.setRefAllele(data.get(omicRefAllele));
        ma.setAltAllele(data.get(omicAltAllele));
        ma.setGenomeAssembly(data.get(omicGenomeAssembly));
        ma.setRsIdVariants(data.get(omicRsIdVariants));
        ma.setSeqStartPosition(data.get(omicSeqStartPosition));

        ma.setEnsemblTranscriptId(data.get(omicEnsemblTranscriptId));
        ma.setNucleotideChange(data.get(omicNucleotideChange));
        ma.setMarker(marker);

        return  ma;
    }



    private MarkerAssociation setCNAProperties(Map<String,String> data, Marker marker){

        MarkerAssociation ma = new MarkerAssociation();


        //setHostStrain Name
        ma.setChromosome(data.get(omicChromosome));
        ma.setSeqStartPosition(data.get(omicSeqStartPosition));
        ma.setSeqEndPosition(data.get(omicSeqStartPosition));
        ma.setCnaLog10RCNA(data.get(omicCnaLog10RCNA));
        ma.setCnaLog2RCNA(data.get(omicCnaLog2RCNA));
        ma.setCnaCopyNumberStatus(data.get(omicCnaCopyNumberStatus));
        ma.setCnaGisticValue(data.get(omicCnaGisticvalue));
        ma.setCnaPicnicValue(data.get(omicCnaPicnicValue));
        ma.setGenomeAssembly(data.get(omicGenomeAssembly));

        marker.setHgncSymbol(data.get(omicHgncSymbol));
        marker.setUcscGeneId(data.get(omicUcscGeneId));
        marker.setNcbiGeneId(data.get(omicNcbiGeneId));
        marker.setEnsemblGeneId(data.get(omicEnsemblGeneId));

        ma.setMarker(marker);
        return  ma;
    }




    public String getPassage(String passageString) {

        if(!passageString.isEmpty() && passageString.toUpperCase().contains("P")){

            passageString = passageString.toUpperCase().replace("P", "");
        }
        //does this string have digits only now?
        if(passageString.matches("\\d+")) return passageString;

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

            if (passage.equals(null)){
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
