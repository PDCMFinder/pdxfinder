package org.pdxfinder.services;

import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.DataProjectionRepository;
import org.pdxfinder.graph.repositories.ModelCreationRepository;
import org.pdxfinder.graph.repositories.PlatformRepository;
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
    private DataProjectionRepository dataProjectionRepository;


    public PlatformService(PlatformRepository platformRepository, ModelCreationRepository modelCreationRepository, DrugService drugService, DataProjectionRepository dataProjectionRepository) {
        this.platformRepository = platformRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.drugService = drugService;
        this.dataProjectionRepository = dataProjectionRepository;
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


        List<DataAvailableDTO> results = new ArrayList<>();

        String daJson;
        if(dataProjectionRepository.findByLabel("data available") != null){

            daJson = dataProjectionRepository.findByLabel("data available").getValue();
        }
        else{
            return results;
        }

        try{

            JSONObject json = new JSONObject(daJson);

            for(int i=0; i<json.names().length(); i++){

                if(json.names().getString(i).toLowerCase().equals(dataSource.toLowerCase())){

                    JSONArray jarray = json.getJSONArray(json.names().getString(i));

                    for(int j = 0; j < jarray.length(); j++){
                        JSONObject obj = jarray.getJSONObject(j);

                        results.add(new DataAvailableDTO(obj.getString("dataType"), obj.getString("platformName"), obj.getString("modelNumbers"), obj.getString("platformUrl")));
                    }

                    return results;
                }
            }

        }
        catch (Exception e){

            e.printStackTrace();
        }

        return results;



    }


    public Map<String, String> getPlatformsWithUrls() {

        Map<String, String> result = new HashMap<>();
        Collection<Platform> platforms = platformRepository.findAllWithUrl();


        for (Platform p : platforms) {

            if (p.getName() != null && !p.getName().isEmpty() && !p.getUrl().isEmpty()) {

                result.put(p.getName(), p.getUrl());
            }
        }


        return result;
    }



}
