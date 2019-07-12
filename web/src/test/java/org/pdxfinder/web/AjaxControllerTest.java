package org.pdxfinder.web;

import static org.hamcrest.Matchers.*;

import org.apache.http.entity.ContentType;
import org.hamcrest.Matchers;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.mockito.BDDMockito.given;

/*
 * Created by abayomi on 03/07/2019.
 */
public class AjaxControllerTest extends BaseTests {

    Logger log = LoggerFactory.getLogger(AjaxControllerTest.class);

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // run once before any of the tests
    }

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void getMolecularDataStatAPIOK() throws Exception {
        String urlTemplate = "/statistics/molecular-data";
        this.mockMvc.perform(get(urlTemplate)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title.text", is("")))
                .andExpect(jsonPath("$.xAxis").exists())
                .andExpect(jsonPath("$.xAxis.categories[*]").isNotEmpty())
                .andExpect(jsonPath("$.xAxis.categories[*]", hasItem("JAN 2019")))
                .andExpect(jsonPath("$.series").exists())
                .andExpect(jsonPath("$.series", hasSize(5)))
                .andExpect(jsonPath("$.series[*].type", hasItem("spline")))
                .andExpect(jsonPath("$.series[*].name", hasItem("Total Data Per Release"))).andDo(print());

    }


    @Test
    public void getMolecularDataStatNotFound404() throws Exception {
        mockMvc.perform(get("/statistics/molecular-data/5")).andExpect(status().isNotFound());
    }


    @Test
    public void getTreatmentStatAPIOK() throws Exception {
        String urlTemplate = "/statistics/patient-treatment/patients";
        this.mockMvc.perform(get(urlTemplate)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.title.text").value("Patient Data"))
                .andExpect(jsonPath("$.xAxis").exists())
                .andExpect(jsonPath("$.xAxis.categories[*]").isNotEmpty())
                .andExpect(jsonPath("$.yAxis").exists())
                .andExpect(jsonPath("$.series").exists())
                //.andExpect(jsonPath("$.series", hasSize(5)))
                .andExpect(jsonPath("$.series[*].data[*]").isNotEmpty())
                .andExpect(jsonPath("$.subtitle.text", is("Patient Count Per Data Release")));
    }


    @After
    public void tearDown() throws Exception {
        // run after each test
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        // run once after all tests
    }


}
