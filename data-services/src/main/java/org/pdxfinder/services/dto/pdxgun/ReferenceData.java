package org.pdxfinder.services.dto.pdxgun;

import java.util.List;
public class ReferenceData {

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private List<Gene> gene;

        public List<Gene> getGene() {
            return gene;
        }

        public void setGene(List<Gene> gene) {
            this.gene = gene;
        }
    }
}
