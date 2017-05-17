package org.pdxfinder.services;

import org.pdxfinder.dao.BackgroundStrain;
import org.pdxfinder.services.dto.SearchDTO;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;


public class SearchService {

    public List<SearchDTO> findResultsByTerm(String term) {
        return Arrays.asList(new SearchDTO("result1"), new SearchDTO("result2"));
    }

}
