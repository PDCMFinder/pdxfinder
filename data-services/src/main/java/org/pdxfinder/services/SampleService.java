package org.pdxfinder.services;

import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.MarkerRepository;
import org.pdxfinder.repositories.SampleRepository;
import org.pdxfinder.services.dto.SearchDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by csaba on 09/05/2017.
 */
@Service
public class SampleService {


    private SampleRepository sampleRepository;

    @Autowired
    public SampleService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }


}
