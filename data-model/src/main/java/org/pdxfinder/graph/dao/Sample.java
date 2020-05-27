package org.pdxfinder.graph.dao;

import org.apache.commons.collections4.CollectionUtils;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.HashSet;
import java.util.Set;

/**
 * Sample represents a piece of tissue taken from a specimen (human or mouse)
 * A sample could be cancerous or not (tissue used to compare to cancer sampled from a health tissue)
 */
@NodeEntity
public class Sample {

    @Id
    @GeneratedValue
    private Long id;

    private String sourceSampleId;
    private String diagnosis;
    private String diagnosisNotes;
    private Tissue originTissue;
    private Tissue sampleSite;
    private String extractionMethod;
    private String classification;
    private String stage;
    private String stageClassification;
    private String grade;
    private String gradeClassification;
    //TODO: remove classification field and use stage and grade instead.

    public Boolean normalTissue;
    private String dataSource;

    private String rawDataUrl;

    @Relationship(type="MAPPED_TO")
    private SampleToOntologyRelationship sampleToOntologyRelationShip;

    @Relationship(type = "OF_TYPE")
    private TumorType type;

    @Relationship(type = "CHARACTERIZED_BY", direction = Relationship.INCOMING)
    private Set<MolecularCharacterization> molecularCharacterizations;

    //
    @Relationship(type = "SAMPLED_FROM", direction = Relationship.INCOMING)
    private PatientSnapshot patientSnapshot;

    @Relationship(type = "Histology")
    private Set<Histology> histology;
    
    public Sample() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    // Minimal Constructor
    public Sample(String sourceSampleId) {
        this.sourceSampleId = sourceSampleId;
    }

    public Sample(String sourceSampleId, TumorType type, String diagnosis, Tissue originTissue, Tissue sampleSite, String extractionMethod,
                  String classification, Boolean normalTissue, String dataSource) {
        this.sourceSampleId = sourceSampleId;
        this.type = type;
        this.diagnosis = diagnosis;
        this.originTissue = originTissue;
        this.sampleSite = sampleSite;
        this.extractionMethod = extractionMethod;
        this.classification = classification;
        this.normalTissue = normalTissue;
        this.dataSource = dataSource;

    }

    public Sample(String sourceSampleId, TumorType type, String diagnosis, Tissue originTissue, Tissue sampleSite, String extractionMethod,
                  String stage, String stageClassification, String grade, String gradeClassification, Boolean normalTissue, String dataSource) {
        this.sourceSampleId = sourceSampleId;
        this.type = type;
        this.diagnosis = diagnosis;
        this.originTissue = originTissue;
        this.sampleSite = sampleSite;
        this.extractionMethod = extractionMethod;
        this.stage = stage;
        this.stageClassification = stageClassification;
        this.grade = grade;
        this.gradeClassification = gradeClassification;
        this.normalTissue = normalTissue;
        this.dataSource = dataSource;
    }

    public String getSourceSampleId() {
        return sourceSampleId;
    }

    public void setSourceSampleId(String sourceSampleId) {
        this.sourceSampleId = sourceSampleId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Tissue getOriginTissue() {
        return originTissue;
    }

    public void setOriginTissue(Tissue originTissue) {
        this.originTissue = originTissue;
    }

    public Tissue getSampleSite() {
        return sampleSite;
    }

    public void setSampleSite(Tissue sampleSite) {
        this.sampleSite = sampleSite;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public Boolean getNormalTissue() {
        return normalTissue;
    }

    public void setNormalTissue(Boolean normalTissue) {
        this.normalTissue = normalTissue;
    }

    public TumorType getType() {
        return type;
    }

    public void setType(TumorType type) {
        this.type = type;
    }

    public Set<MolecularCharacterization> getMolecularCharacterizations() {
        return molecularCharacterizations;
    }

    public void setMolecularCharacterizations(Set<MolecularCharacterization> molecularCharacterizations) {
        this.molecularCharacterizations = molecularCharacterizations;
    }

    public boolean hasMolecularCharacterizations() {
        return CollectionUtils.isNotEmpty(this.molecularCharacterizations);
    }

    public void addMolecularCharacterization(MolecularCharacterization mc){

        if(this.molecularCharacterizations == null){
            this.molecularCharacterizations = new HashSet<>();
        }
        this.molecularCharacterizations.add(mc);
    }

    public SampleToOntologyRelationship getSampleToOntologyRelationship() {
        return sampleToOntologyRelationShip;
    }

    public void setSampleToOntologyRelationShip(SampleToOntologyRelationship sampleToOntologyRelationShip) {
        this.sampleToOntologyRelationShip = sampleToOntologyRelationShip;
    }

    public void setHistology(Set<Histology> histology){
        this.histology = histology;
    }
    
    public void addHistology(Histology histology){
        if(this.histology == null){
            this.histology = new HashSet<>();
            this.histology.add(histology);
        }else{
            this.histology.add(histology);
        }
    }
    
    public Set<Histology> getHistology(){
        return this.histology;
    }

    public String getExtractionMethod() {
        return extractionMethod;
    }

    public void setExtractionMethod(String extractionMethod) {
        this.extractionMethod = extractionMethod;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public PatientSnapshot getPatientSnapshot() {
        return patientSnapshot;
    }

    public void setPatientSnapshot(PatientSnapshot patientSnapshot) {
        this.patientSnapshot = patientSnapshot;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStageClassification() {
        return stageClassification;
    }

    public void setStageClassification(String stageClassification) {
        this.stageClassification = stageClassification;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getGradeClassification() {
        return gradeClassification;
    }

    public void setGradeClassification(String gradeClassification) {
        this.gradeClassification = gradeClassification;
    }

    public MolecularCharacterization getMolecularCharacterization(String type, String platformName){

        if(molecularCharacterizations == null) return null;

        for(MolecularCharacterization mc : molecularCharacterizations){

            if(mc.getType().equals(type)){

                if(mc.getPlatform() != null && mc.getPlatform().getName().equals(platformName)) {

                    return mc;
                }

            }
        }

        return null;
    }

    public String getDiagnosisNotes() {
        return diagnosisNotes;
    }

    public void setDiagnosisNotes(String diagnosisNotes) {
        this.diagnosisNotes = diagnosisNotes;
    }

    public String getRawDataUrl() {
        return rawDataUrl;
    }

    public void setRawDataUrl(String rawDataUrl) {
        this.rawDataUrl = rawDataUrl;
    }
}
