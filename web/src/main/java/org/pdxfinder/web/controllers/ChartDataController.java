package org.pdxfinder.web.controllers;

import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.ds.SearchDS;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ChartDataController {

    private SearchDS searchDS;
    private DataImportService dataImportService;

    public ChartDataController(SearchDS searchDS, DataImportService dataImportService) {
        this.searchDS = searchDS;
        this.dataImportService = dataImportService;

    }

    @RequestMapping("/graphdata")
    public Map<String, Collection<DataHolder>> graphdata(Model model) {
        return getData();
    }


    @Cacheable("graph_data")
    public Map<String, Collection<DataHolder>> getData() {

        final Set<ModelForQuery> models = searchDS.getModels();
        final Map<String, Integer> diagnosisCounts = searchDS.getDiagnosisCounts();

        Map<String, Collection<DataHolder>> data = new HashMap<>();
        Map<String, DataHolder> cancers = new HashMap<>();

        int ids = 1;

        for (String key : diagnosisCounts.keySet()) {

            if (!cancers.containsKey(key)) {
                DataHolder dataHolder = new DataHolder(key);
                dataHolder.setNumber(diagnosisCounts.get(key));
                dataHolder.setId(ids++);
                cancers.put(key, dataHolder);
            }

        }

        data.put("cancer_by_type", cancers.values());

        Map<String, DataHolder> dataHolderMap = new HashMap<>();

        Map<String, String> dataHolderDetails = new HashMap<>();

        List<Group> providers = dataImportService.getAllProviderGroups();

        for(Group g : providers){
            dataHolderDetails.put(g.getAbbreviation(), g.getName());
        }

        for (ModelForQuery m : models) {

            if (!dataHolderMap.containsKey(m.getDatasource())) {
                dataHolderMap.put(m.getDatasource(), new DataHolder(m.getDatasource()));

                CenterSpecificDataHolder c = new CenterSpecificDataHolder(m.getDatasource());
                c.setDescription(dataHolderDetails.get(m.getDatasource()));
                c.setDatasource(m.getDatasource());
                dataHolderMap.put(m.getDatasource(), c);

                dataHolderMap.get(m.getDatasource()).id = ids++;
            }

            DataHolder dh = dataHolderMap.get(m.getDatasource());
            dh.increment();

        }


        data.put("providers", dataHolderMap.values());



        /*
        Return counts of models by top level by center
         */
        cancers = new HashMap<>();
        for (ModelForQuery m : models) {

            for (String system : m.getCancerSystem()) {

                String key = system + m.getDatasource();

                if (!cancers.containsKey(key)) {

                    CenterSpecificDataHolder c = new CenterSpecificDataHolder(system);
                    c.setDatasource(m.getDatasource());
                    c.setId(ids++);

                    cancers.put(key, c);
                }

                DataHolder dh = cancers.get(key);
                dh.increment();
            }

        }

        data.put("cancer_by_top_level", cancers.values());
        return data;
    }


    private class DataHolder {

        private Integer id;
        private String name;
        private Integer number = 0;

        public DataHolder() {
        }

        public DataHolder(String name) {
            this.name = name;
        }

        public void increment() {
            number += 1;
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

    }

    private class CenterSpecificDataHolder extends DataHolder {

        private String description;
        private String datasource;

        public CenterSpecificDataHolder(String name) {
            setName(name);
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDatasource() {
            return datasource;
        }

        public void setDatasource(String datasource) {
            this.datasource = datasource;
        }
    }
}
