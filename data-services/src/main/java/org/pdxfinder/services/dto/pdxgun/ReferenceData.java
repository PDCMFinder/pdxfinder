package org.pdxfinder.services.dto.pdxgun;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReferenceData {

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private List<ApiData> gene;

        private List<ApiData> variant;

        public List<ApiData> getGene() {
            return gene;
        }

        public void setGene(List<ApiData> gene) {
            this.gene = gene;
        }

        public List<ApiData> getVariant() {
            return variant;
        }

        public void setVariant(List<ApiData> variant) {
            this.variant = variant;
        }
    }
}
