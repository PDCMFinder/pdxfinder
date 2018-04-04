package org.pdxfinder.services;

import org.pdxfinder.dao.ModelCreation;
import org.pdxfinder.dao.MolecularCharacterization;
import org.pdxfinder.dao.Platform;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.ModelCreationRepository;
import org.pdxfinder.repositories.PlatformRepository;
import org.pdxfinder.services.dto.DataAvailableDTO;
import org.springframework.stereotype.Service;

import java.util.*;

/*
 * Created by abayomi on 16/02/2018.
 */
@Service
public class PlatformService
{


    private PlatformRepository platformRepository;
    private ModelCreationRepository modelCreationRepository;
    private DrugService drugService;


    public PlatformService(PlatformRepository platformRepository, ModelCreationRepository modelCreationRepository, DrugService drugService) {
        this.platformRepository = platformRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.drugService = drugService;
    }


    public Map<String, Integer> getPlatformCountBySource(String dataSource){

            Map<String, Integer> platformMap = new HashMap<>();

            List<Platform> platforms = platformRepository.findPlatformByExternalDataSource(dataSource);

            for (Platform platform : platforms) {
                platformMap.put(platform.getName(), platformRepository.countModelsByPlatformAndExternalDataSource(platform.getName(),dataSource));
            }

            return platformMap;

    }


    public List<DataAvailableDTO> getPlatformDataCountBySource(String dataSource){

        //datatype => platform => modelNumber
        Map<String, Map<String, Integer>> platformDataMap = new HashMap<>();

        Collection<ModelCreation> models = modelCreationRepository.getModelsWithMolCharBySource(dataSource);

        for(ModelCreation m : models){

            String platformName;
            String mcType;

            for(Sample s : m.getRelatedSamples()){

                for(MolecularCharacterization mc : s.getMolecularCharacterizations()){

                    platformName = mc.getPlatform().getName();
                    mcType = mc.getType();

                    if(platformDataMap.containsKey(mcType)){

                        if(platformDataMap.get(mcType).containsKey(platformName)){

                            int oldNum = platformDataMap.get(mcType).get(platformName).intValue();
                            oldNum++;
                            platformDataMap.get(mcType).put(platformName, oldNum);
                        }
                        //new platform
                        else{

                            platformDataMap.get(mcType).put(platformName, new Integer(1));
                        }
                    }
                    //new mcType, new platform
                    else{
                        Map<String, Integer> plat = new HashMap<>();
                        plat.put(platformName, new Integer(1));

                        platformDataMap.put(mcType, plat);

                    }
                }
            }

        }

        List<DataAvailableDTO> resultList = new ArrayList<>();

        for(Map.Entry<String, Map<String, Integer>> mcTypeEntry : platformDataMap.entrySet()){

            String mcType = mcTypeEntry.getKey();

            for(Map.Entry<String, Integer> platformNameEntry : mcTypeEntry.getValue().entrySet()){

                String platformName = platformNameEntry.getKey();
                Integer modelNumbers = platformNameEntry.getValue();

                DataAvailableDTO dto = new DataAvailableDTO(mcType, platformName, modelNumbers.toString());
                resultList.add(dto);
            }
        }

        //Add dosing studies number
        int dosingStudiesNumber = drugService.getDosingStudiesNumberByDataSource(dataSource);

        if(dosingStudiesNumber > 0) {

            DataAvailableDTO dto = new DataAvailableDTO("dosing studies", "Dosing Protocol", Integer.toString(dosingStudiesNumber));
            String platformUrl = drugService.getPlatformUrlByDataSource(dataSource);

            if(platformUrl == null || platformUrl.isEmpty()){
                dto.setPlatformUrl("");
            }
            else{
                dto.setPlatformUrl(platformUrl);
            }
            resultList.add(dto);
        }

        return resultList;
    }




    public List<DataAvailableDTO> getAvailableDataBySource(String dataSource){

        Map<String, DataAvailableDTO> daMap = new HashMap<>();

        Collection<ModelCreation> models = modelCreationRepository.getModelsWithMolCharBySource(dataSource);

        for(ModelCreation m : models){

            String platformName;
            String mcType;
            String platformUrl;

            for(Sample s : m.getRelatedSamples()){

                for(MolecularCharacterization mc : s.getMolecularCharacterizations()){

                    platformName = mc.getPlatform().getName();
                    platformUrl = mc.getPlatform().getUrl();

                    mcType = mc.getType();

                    DataAvailableDTO dto;

                    if(daMap.containsKey(mcType+platformName+platformUrl)){

                        dto = daMap.get(mcType+platformName+platformUrl);
                        int oldNum = Integer.parseInt(dto.getModelNumbers());
                        oldNum++;
                        dto.setModelNumbers(Integer.toString(oldNum));
                        daMap.put(mcType+platformName+platformUrl, dto);

                    }
                    else{

                        daMap.put(mcType+platformName+platformUrl, new DataAvailableDTO(mcType, platformName, "1", platformUrl));
                    }


                }
            }

        }

        List<DataAvailableDTO> results = new ArrayList<>();

        for(DataAvailableDTO dto : daMap.values()){

            results.add(dto);
        }


        //Add dosing studies number
        int dosingStudiesNumber = drugService.getDosingStudiesNumberByDataSource(dataSource);

        if(dosingStudiesNumber > 0) {

            DataAvailableDTO dto = new DataAvailableDTO("dosing studies", "Dosing Protocol", Integer.toString(dosingStudiesNumber));
            String platformUrl = drugService.getPlatformUrlByDataSource(dataSource);

            if(platformUrl == null || platformUrl.isEmpty()){
                dto.setPlatformUrl("");
            }
            else{
                dto.setPlatformUrl(platformUrl);
            }
            results.add(dto);
        }

        return results;
    }


}
