package org.pdxfinder.irccdatamodel;

/**
 * Created by csaba on 26/07/2017.
 */
public class IRCCMarkerMutation {

    String sampleId;
    String hugoSymbol;
    String entrezId;
    String ncbiBuild;
    String exon;
    String chromosome;
    String start;
    String end;
    String startPosition;
    String endPosition;
    String strand;
    String variantClassification;
    String hgvspShort;
    String proteinPosition;
    String swissprot;
    String xenoPassage;
    String platform;


    public IRCCMarkerMutation(String sampleId, String hugoSymbol, String entrezId, String ncbiBuild, String exon, String chromosome,
                              String start, String end, String startPosition, String endPosition, String strand,
                              String hgvspShort, String proteinPosition, String swissprot, String xenoPassage, String platform, String variantClassification) {

        this.sampleId = sampleId;
        this.hugoSymbol = hugoSymbol;
        this.entrezId = entrezId;
        this.ncbiBuild = ncbiBuild;
        this.exon = exon;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.strand = strand;
        this.hgvspShort = hgvspShort;
        this.proteinPosition = proteinPosition;
        this.swissprot = swissprot;
        this.xenoPassage = xenoPassage;
        this.platform = platform;
        this.variantClassification = variantClassification;
    }

    public String getHugoSymbol() {
        return hugoSymbol;
    }

    public String getEntrezId() {
        return entrezId;
    }

    public String getNcbiBuild() {
        return ncbiBuild;
    }

    public String getExon() {
        return exon;
    }

    public String getChromosome() {
        return chromosome;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getStartPosition() {
        return startPosition;
    }

    public String getEndPosition() {
        return endPosition;
    }

    public String getStrand() {
        return strand;
    }

    public String getHgvspShort() {
        return hgvspShort;
    }

    public String getProteinPosition() {
        return proteinPosition;
    }

    public String getSwissprot() {
        return swissprot;
    }

    public String getXenoPassage() {
        return xenoPassage;
    }

    public String getPlatform() {
        return platform;
    }

    public String getSampleId() {
        return sampleId;
    }

    public String getVariantClassification() {
        return variantClassification;
    }
}
