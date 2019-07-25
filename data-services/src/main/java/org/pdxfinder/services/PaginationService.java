package org.pdxfinder.services;

import org.pdxfinder.services.dto.PaginationDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Created by abayomi on 25/07/2019.
 */
@Service
public class PaginationService {




    public PaginationDTO initializeDTO(Page pages){

        int page = pages.getNumber() + 1; // offset for ZERO with +1
        int size = pages.getSize();

        int begin = Math.max(1, page - 4);
        int numPages = pages.getTotalPages();
        int current = page;
        long totalResult = pages.getTotalElements();
        int end = Math.min(begin + 7, numPages);
        size = (size == 0) ? (int) totalResult : size;

        PaginationDTO paginationDto = new PaginationDTO();
        paginationDto.setNumPages(numPages);
        paginationDto.setBeginIndex(begin);
        paginationDto.setEndIndex(end);
        paginationDto.setCurrentIndex(current);
        paginationDto.setTotalResults(totalResult);
        paginationDto.setPage(page);
        paginationDto.setSize(size);

        return paginationDto;
    }
}
