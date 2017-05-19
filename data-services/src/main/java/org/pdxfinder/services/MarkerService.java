package org.pdxfinder.services;

import org.pdxfinder.dao.Marker;
import org.pdxfinder.repositories.MarkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    public List<String> getAllMarkers() {
        Collection<Marker> markers = markerRepository.findAllMarkers();
        List<String> result = new ArrayList<>();

        for (Marker marker : markers) {
            result.add(marker.getName());
        }

        return result;
    }

}
