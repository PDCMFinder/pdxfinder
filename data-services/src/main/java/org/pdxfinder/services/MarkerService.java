package org.pdxfinder.services;

import org.pdxfinder.dao.Marker;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.repositories.MarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by csaba on 16/05/2017.
 */
@Service
public class MarkerService {


    private MarkerRepository markerRepository;

    @Autowired
    public MarkerService(MarkerRepository markerRepository) {
        this.markerRepository = markerRepository;
    }

    public List<String> getAllMarkers(){
        Collection markers = markerRepository.findAllMarkers();
        List<String> result = new ArrayList<>();
        Iterator<Marker> r = markers.iterator();

        while (r.hasNext()) {
            Marker marker = r.next();
            result.add(marker.getName());

        }

        return result;
    }

    public List<String> getAllMarkerNamesBySampleId(String sampleId){
        Collection<Marker> markers = markerRepository.findAllBySampleId(sampleId);

        return markers.stream().map(Marker::getName).collect(Collectors.toList());


    }

}
