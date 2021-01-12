package org.pdxfinder.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.services.constants.DataUrl;
import org.pdxfinder.services.dto.europepmc.Publication;
import org.pdxfinder.services.dto.europepmc.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class PublicationServiceTest extends BaseTest {

    private Logger log = LoggerFactory.getLogger(PublicationServiceTest.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PublicationService publicationService;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper;

    private List<String> expectedResults;

    @Before
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mapper = new ObjectMapper();
        expectedResults = Arrays.asList("17606733","29463559","27374081");
    }

    @Test
    public void givenPubMedIdAndApiUrl_whenGetEuropePmcPublicationsIsCalled_thenReturnEuropmcResult() throws Exception {

        // given
        String pubMedId = "29245952";
        String apiUrl = String.format("%s?query=ext_id:%s&resultType=core&format=json", DataUrl.EUROPE_PMC_URL.get(), pubMedId);

        String expectedTitle= "The humanized anti-human AMHRII mAb 3C23K exerts an anti-tumor activity against human ovarian cancer through tumor-associated macrophages.";
        String expectedAuthors= "Bougherara H, Némati F, Nicolas A, Massonnet G, Pugnière M, Ngô C, Le Frère-Belda MA";
        String expectedPubYear = "2017";
        String expectedJournalTitle = "Oncotarget";

        Map<String, List<Result>> publication  = Collections.singletonMap(
                "resultList",
                Collections.singletonList(new Result(expectedTitle, expectedAuthors, expectedJournalTitle, expectedPubYear))
        );
        Publication expectedPublication = new Publication(publication);

        mockServer.expect(ExpectedCount.once(), requestTo(new URI(apiUrl)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .body(mapper.writeValueAsString(expectedPublication))
                );

        //when
        Result publicationResult = publicationService.getEuropePmcPublications(Collections.singletonList(pubMedId))
                .get(0)
                .getResultList().get("resultList").get(0);


        //then
        mockServer.verify();

        assertEquals(expectedTitle, publicationResult.getTitle());
        assertEquals(expectedPubYear, publicationResult.getPubYear());
        assertEquals(expectedJournalTitle, publicationResult.getJournalTitle());

    }


    @Test
    public void given_pubMedIdPrefixedWIthPMID_When_SanitizePubMedIdsInvoked_Then_PrefixRemoved() {

        // given
        List<String> pubmedIdsWithPrefix = Arrays.asList("PMID:17606733", "PMID:29463559", "PMID:27374081");

        // when
        List<String> actualResults = publicationService.sanitizePubMedIds(pubmedIdsWithPrefix);

        // Then
        assertEquals(this.expectedResults, actualResults);
    }


    @Test
    public void given_pubMedIdPrefixedWIthSpaces_When_SanitizePubMedIdsInvoked_Then_SpacesRemoved() {

        // given
        List<String> pubmedIdsWithSpaces = Arrays.asList("PMID: 17606733", " 29 4635 59 ", "PMID: 27 37 4081 ");

        // when
        List<String> actualResults = publicationService.sanitizePubMedIds(pubmedIdsWithSpaces);

        // Then
        assertEquals(this.expectedResults, actualResults);
    }


    @Test
    public void given_pubMedIdContainsSemiColon_When_SanitizePubMedIdsInvoked_Then_ListCreated() {

        // given
        List<String> pubmedIdsWithSemiColon = Arrays.asList("PMID:17606733; PMID:29463559", "PMID:27374081");

        // when
        List<String> actualResults = publicationService.sanitizePubMedIds(pubmedIdsWithSemiColon);

        // Then
        assertEquals(this.expectedResults, actualResults);
    }



}
