package org.pdxfinder.services;

import org.pdxfinder.dao.Platform;
import org.pdxfinder.repositories.PlatformRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Created by abayomi on 16/02/2018.
 */
@Service
public class PlatformService
{


    private PlatformRepository platformRepository;


    public PlatformService(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }


    public Map<String, Integer> getPlatformCountBySource(String dataSource){

            Map<String, Integer> platformMap = new HashMap<>();

            List<Platform> platforms = platformRepository.findPlatformByExternalDataSource(dataSource);

            for (Platform platform : platforms) {
                platformMap.put(platform.getName(), platformRepository.countModelsByPlatformAndExternalDataSource(platform.getName(),dataSource));
            }

            return platformMap;

    }




}
