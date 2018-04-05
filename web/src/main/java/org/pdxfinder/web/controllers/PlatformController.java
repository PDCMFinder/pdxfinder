package org.pdxfinder.web.controllers;

import org.pdxfinder.services.PlatformService;
import org.pdxfinder.services.dto.DataAvailableDTO;
import org.pdxfinder.services.dto.PlatformDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Created by abayomi on 16/02/2018.
 */
@RestController
public class PlatformController {

    private PlatformService platformService;

    @Autowired
    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @RequestMapping(value = "/platform/{dataSrc}")
    public Map findPlatformBySource(@PathVariable String dataSrc) {

        return  platformService.getPlatformCountBySource(dataSrc);
    }

    @RequestMapping(value = "/platformdata/{dataSrc}")
    public List<DataAvailableDTO> findPlatformDataBySource(@PathVariable String dataSrc) {

        //populate list with data [{dataType:"mutation",platform:"CTP", models:20},{},{}]
        return  platformService.getAvailableDataBySource(dataSrc);
    }


}


