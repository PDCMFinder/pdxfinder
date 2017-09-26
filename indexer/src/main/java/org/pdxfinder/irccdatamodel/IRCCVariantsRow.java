package org.pdxfinder.irccdatamodel;

/*
 * Created by csaba on 25/09/2017.
 */
public class IRCCVariantsRow {

    private String sampleId;
    private String chrom;
    private String pos;
    private String ref;
    private String alt;
    private String gene;
    private String cds;
    private String protein;
    private String type;
    private String effect;
    private String annotation;


    public IRCCVariantsRow(String sampleId, String chrom, String pos, String ref, String alt, String gene, String cds, String protein, String type, String effect, String annotation) {
        this.sampleId = sampleId;
        this.chrom = chrom;
        this.pos = pos;
        this.ref = ref;
        this.alt = alt;
        this.gene = gene;
        this.cds = cds;
        this.protein = protein;
        this.type = type;
        this.effect = effect;
        this.annotation = annotation;
    }


    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getChrom() {
        return chrom;
    }

    public void setChrom(String chrom) {
        this.chrom = chrom;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getCds() {
        return cds;
    }

    public void setCds(String cds) {
        this.cds = cds;
    }

    public String getProtein() {
        return protein;
    }

    public void setProtein(String protein) {
        this.protein = protein;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}
