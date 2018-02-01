package org.pdxfinder.web.controllers;

import org.json.JSONArray;
import org.json.JSONObject;
import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.ds.SearchDS;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class ChartDataController {

    private SearchDS searchDS;

    public ChartDataController(SearchDS searchDS) {
        this.searchDS = searchDS;
    }

    @RequestMapping("/graphdata")
    @ResponseBody
    public String graphdata(Model model) {

        JSONObject data = getData();
        return data.toString();
    }


    @Cacheable
    public JSONObject getData() {
        final Set<ModelForQuery> models = searchDS.getModels();

        JSONObject data = new JSONObject();
        JSONArray cancerbyType = new JSONArray();
        JSONArray providersCollection = new JSONArray();

        int ids = 1;

        Map<String, DataHolder> cancers = new HashMap<>();

        for (ModelForQuery m : models) {

            if (!cancers.containsKey(m.getMappedOntologyTerm())) {
                cancers.put(m.getMappedOntologyTerm(), new DataHolder(m.getMappedOntologyTerm()));
                cancers.get(m.getMappedOntologyTerm()).id = ids++;
            }

            DataHolder dh = cancers.get(m.getMappedOntologyTerm());
            dh.increment();

        }

        for (DataHolder h : cancers.values()) {
            cancerbyType.put(h.toJson());
        }

        data.put("cancer_by_type", cancerbyType);

        Map<String, DataHolder> providers = new HashMap<>();

        for (ModelForQuery m : models) {

            if (!providers.containsKey(m.getDatasource())) {
                providers.put(m.getDatasource(), new DataHolder(m.getDatasource()));
                providers.get(m.getDatasource()).id = ids++;
            }

            DataHolder dh = providers.get(m.getDatasource());
            dh.increment();

        }

        for (DataHolder h : providers.values()) {
            providersCollection.put(h.toJson());
        }


        data.put("providers", providersCollection);

        return data;
    }


    private class DataHolder {

        private Integer id;
        private String name;
        private Integer number = 0;
        private String description;

        public DataHolder(String name) {
            this.name = name;
        }

        public void increment() {
            number += 1;
        }

        public JSONObject toJson() {
            JSONObject n = new JSONObject();
            n.put("id", id);
            n.put("name", name);
            n.put("number", number);
            n.put("description", description);
            return n;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
