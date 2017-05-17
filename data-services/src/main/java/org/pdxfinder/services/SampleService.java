package org.pdxfinder.services;

import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.SampleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by csaba on 09/05/2017.
 */
@Service
public class SampleService {

    @Autowired
    SampleRepository sampleRepository;

    public Map<String, Object> searchForSamples(String diag){

        Collection<Sample> result = sampleRepository.findByDiagnosisContains(diag);
        return toFinalFormat(result);

    }

    public Map<String, Object> searchForSamplesWithFilters(String diag, String[] markers, String[] datasources, String[] origintumortypes){

        Collection<Sample> result = sampleRepository.findByDiagnosisContains(diag);
        return toFinalFormat(result);

    }

    private Map<String,Object> toFinalFormat(Collection<Sample> samples){

        List<Map<String, Object>> nodes = new ArrayList<>();
        //List<Map<String, Object>> rels = new ArrayList<>();

        Map<String, Object> result = new HashMap<String, Object>(2);
        int i = 0;
        Iterator<Sample> r = samples.iterator();

        while (r.hasNext()) {
            Sample sample = r.next();
            result.put(sample.getSourceSampleId(),sample);

        }
        return result;





    }

    private Map<String, Object> map(String key1, Object value1, String key2, Object value2, String key3, Object value3, String key4, Object value4 ) {
        Map<String, Object> result = new HashMap<String, Object>(2);
        result.put(key1, value1);
        result.put(key2, value2);
        result.put(key3, value3);
        result.put(key4, value4);

        return result;
    }




}
