package org.pdxfinder.services;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.*;
import org.pdxfinder.services.constants.MolCharTable;
import org.pdxfinder.services.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DetailsService {

    private SampleRepository sampleRepository;

    private PatientRepository patientRepository;
    private ModelCreationRepository modelCreationRepository;
    private SpecimenRepository specimenRepository;
    private MolecularCharacterizationRepository molecularCharacterizationRepository;
    private PlatformRepository platformRepository;
    private TreatmentSummaryRepository treatmentSummaryRepository;
    private MarkerAssociationRepository markerAssociationRepository;

    private DrugService drugService;
    private PatientService patientService;
    private PublicationService publicationService;

    private static final String NOT_SPECIFIED = "NOT_SPECIFIED";

    public DetailsService(SampleRepository sampleRepository,
                          PatientRepository patientRepository,
                          ModelCreationRepository modelCreationRepository,
                          SpecimenRepository specimenRepository,
                          MolecularCharacterizationRepository molecularCharacterizationRepository,
                          PlatformRepository platformRepository,
                          TreatmentSummaryRepository treatmentSummaryRepository,
                          DrugService drugService,
                          PatientService patientService,
                          MarkerAssociationRepository markerAssociationRepository,
                          PublicationService publicationService) {

        this.sampleRepository = sampleRepository;
        this.patientRepository = patientRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
        this.specimenRepository = specimenRepository;
        this.platformRepository = platformRepository;
        this.treatmentSummaryRepository = treatmentSummaryRepository;
        this.drugService = drugService;
        this.patientService = patientService;
        this.markerAssociationRepository = markerAssociationRepository;
        this.publicationService = publicationService;

    }



    public DetailsDTO getModelDetails(String dataSource, String modelId) {

        DetailsDTO dto = new DetailsDTO();

        Patient patient = patientRepository.findByDataSourceAndModelId(dataSource, modelId);
        Group providerGroup = patient.getProviderGroup();
        ModelCreation pdx = modelCreationRepository.findByDataSourceAndSourcePdxId(dataSource, modelId);
        Sample patientSample = sampleRepository.findPatientSampleWithDetailsByDataSourceAndPdxId(dataSource, modelId);

        dto.setModelId(pdx.getSourcePdxId());
        dto.setDataSource(pdx.getDataSource());
        dto.setPatientSex(patient.getSex());

        if (pdx != null && pdx.getExternalUrls() != null) {

            pdx.getExternalUrls().forEach(extUrl ->{
                if (extUrl.getType().equals(ExternalUrl.Type.SOURCE.getValue())){
                    dto.setViewDataAtUrl(extUrl.getUrl());
                }else{
                    dto.setContactProviderUrl(extUrl.getUrl());
                }
            });

            dto.setViewDataAtLabel("View Data at "+pdx.getDataSource());
        }

        else{
            dto.setViewDataAtUrl("#");
            dto.setViewDataAtLabel("Unknown source");
        }



        PatientSnapshot currentPatientSnapshot = null;
        //since there is only one element being returned in the set, this will give the current snapshot for the patient
        for(PatientSnapshot ps: patient.getSnapshots()){
            currentPatientSnapshot = ps;
        }

        if(currentPatientSnapshot != null && currentPatientSnapshot.getAgeAtCollection() != null){

            dto.setAgeAtTimeOfCollection(currentPatientSnapshot.getAgeAtCollection());
        }
        else{

            dto.setAgeAtTimeOfCollection(NOT_SPECIFIED);
        }

        if(patient.getRace() != null && !patient.getRace().isEmpty()){

            dto.setRace(patient.getRace());
        }
        else{

            dto.setRace(NOT_SPECIFIED);
        }

        if(patient.getEthnicity() != null && !patient.getEthnicity().isEmpty()){

            dto.setEthnicity(patient.getEthnicity());
        }
        else{

            dto.setEthnicity(NOT_SPECIFIED);
        }

        if(patientSample.getSampleToOntologyRelationship() != null && patientSample.getSampleToOntologyRelationship().getOntologyTerm()!=null){
            dto.setMappedOntologyTermLabel(patientSample.getSampleToOntologyRelationship().getOntologyTerm().getLabel());
        }
        else{
            dto.setMappedOntologyTermLabel("");
        }

        dto.setProviderName(providerGroup.getName());
        dto.setContactProviderLabel(providerGroup.getAbbreviation());
        dto.setContactProviderUrl(providerGroup.getContact());


        if (pdx != null && pdx.getExternalUrls() != null) {

            pdx.getExternalUrls().stream().forEach(extUrl ->{
                if (extUrl.getType().equals(ExternalUrl.Type.SOURCE.getValue())){
                    dto.setViewDataAtUrl(extUrl.getUrl());
                }else{
                    dto.setContactProviderUrl(extUrl.getUrl());
                }
            });

            dto.setViewDataAtLabel("View Data at "+pdx.getDataSource());
        }

        else{
            dto.setViewDataAtUrl("#");
            dto.setViewDataAtLabel("Unknown source");
        }


        dto.setPatientSex(patient.getSex());

        dto.setRelatedModels(getModelsOriginatedFromSamePatient(dataSource, modelId));

        if(patientSample.getOriginTissue() != null){
            dto.setPrimaryTissue(patientSample.getOriginTissue().getName());
        }

        if(patientSample.getSampleSite() != null){
            dto.setCollectionSite(patientSample.getSampleSite().getName());
        }

        if(patientSample.getType() != null){
            dto.setTumorType(patientSample.getType().getName());
        }

        if(patientSample.getStage() != null){
            dto.setStage(patientSample.getStage());
        }

        if(patientSample.getStageClassification() != null){
            dto.setStageClassification(patientSample.getStageClassification());
        }

        if(patientSample.getGrade() != null){
            dto.setGrade(patientSample.getGrade());
        }

        if(patientSample.getGradeClassification() != null){
            dto.setGradeClassification(patientSample.getGradeClassification());
        }


        //Assembling PDX MODEL ENGRAFTMENT DATA
        Set<EngraftmentDataDTO> engraftmentData = new HashSet<>();
        //engraftments >> passages[]
        Map<EngraftmentDataDTO, Set<String>> engraftmentDataMap = new HashMap<>();

        if(pdx.getSpecimens() != null){

            for(Specimen sp: pdx.getSpecimens()){

                if(sp.getHostStrain() != null){

                    EngraftmentDataDTO edto = new EngraftmentDataDTO();

                    edto.setStrainName(
                            (sp.getHostStrain() != null) ? notEmpty(sp.getHostStrain().getName()) : NOT_SPECIFIED
                    );

                    edto.setEngraftmentSite(
                            (sp.getEngraftmentSite() != null) ? notEmpty(sp.getEngraftmentSite().getName()) : NOT_SPECIFIED
                    );

                    edto.setEngraftmentType(
                            (sp.getEngraftmentType() != null) ? notEmpty(sp.getEngraftmentType().getName()) : NOT_SPECIFIED
                    );

                    edto.setEngraftmentMaterial(
                            (sp.getEngraftmentMaterial() != null) ? notEmpty(sp.getEngraftmentMaterial().getName()) : NOT_SPECIFIED
                    );

                    edto.setEngraftmentMaterialState(
                            (sp.getEngraftmentMaterial() != null) ? notEmpty(sp.getEngraftmentMaterial().getState()) : NOT_SPECIFIED
                    );

                    String passage = (sp.getPassage() != null) ? notEmpty(sp.getPassage()) : NOT_SPECIFIED;

                    //if the datakey combination is found, then don't add new Engraftment data, but rather uodate the passage accordingly
                    if (engraftmentDataMap.containsKey(edto)) {

                        engraftmentDataMap.get(edto).add(passage);

                    } else {

                        // If new, save in the Map
                        engraftmentDataMap.put(edto, new HashSet<>(Collections.singletonList(passage)));
                    }
                }
            }
        }

        for(Map.Entry<EngraftmentDataDTO, Set<String>> entry: engraftmentDataMap.entrySet()){

            EngraftmentDataDTO edto = entry.getKey();
            Set<String> passages = entry.getValue();
            List<String> passageList = new ArrayList<>();
            passageList.addAll(passages);

            //order the passages:
            Collections.sort(passageList);
            edto.setPassage(StringUtils.join(passageList, ", "));
            engraftmentData.add(edto);

        }

        dto.setPdxModelList(engraftmentData);


        List<QualityAssurance> qaList = pdx.getQualityAssurance();
        if(qaList == null) qaList = new ArrayList<>();

        List<QualityControlDTO> qcontrolList = new ArrayList<>();
        for(QualityAssurance qa: qaList){

            QualityControlDTO qdto = new QualityControlDTO(qa.getTechnology(), qa.getDescription(), qa.getPassages());
            qcontrolList.add(qdto);
        }

        dto.setModelQualityControl(qcontrolList);

        PatientDTO patientDTO = patientService.getPatientDetails(dataSource, modelId);
        dto.setPatient(patientDTO);

        List<DrugSummaryDTO> dosingStudies = getDrugSummary(dataSource, modelId);
        dto.setDosingStudy(dosingStudies);
        dto.setDosingStudyProtocolUrl(drugService.getPlatformUrlByDataSource(dataSource));
        dto.setDosingStudyNumbers(dosingStudies.size());


        //MOLECULAR DATA TAB
        List<MolecularDataEntryDTO> mdeDTO = new ArrayList<>();
        Set<String> dataTypes = new HashSet<>();
        //first add molchars linked to the patient sample
        Collection<MolecularCharacterization> patientMCs = molecularCharacterizationRepository.findAllBySample(patientSample);
        for(MolecularCharacterization mc : patientMCs){

            MolecularDataEntryDTO mde = new MolecularDataEntryDTO();
            mde.setSampleId(patientSample.getSourceSampleId());
            mde.setSampleType("Patient Tumor");
            mde.setEngraftedTumorPassage("NA");
            mde.setMolcharType(mc.getType());
            mde.setDataAvailableLabel(mc.getPlatform().getName());
            mde.setDataAvailableUrl("");
            mde.setPlatformUsedLabel(mc.getPlatform().getName());


            if(mc.getPlatform().getName() == null || mc.getPlatform().getName().isEmpty() || mc.getPlatform().getName().equalsIgnoreCase("not specified")
                    || mc.getPlatform().getUrl() == null || mc.getPlatform().getUrl().isEmpty()){

                mde.setPlatformUsedUrl(null);
            }
            else{

                mde.setPlatformUsedUrl(mc.getPlatform().getUrl());
            }

            int assocData = molecularCharacterizationRepository.findAssociationsNumberById(mc);

            if(assocData == 0){
                mde.setDataAssociated("NO");
            }
            else{
                mde.setDataAssociated("YES");
                dataTypes.add(mc.getType());
            }

            if(mc.isVisible()){
                mde.setIsVisible("YES");
            }
            else{
                mde.setIsVisible("NO");
            }
            mde.setRawDataLabel("Not available");
            mde.setMolcharId(mc.getId().toString());

            if (patientSample.getSourceSampleId() != null)
                mdeDTO.add(mde);
        }

        //then add molchars linked to the xenograft sample

        List<Specimen> specimens = specimenRepository.findAllWithMolcharDataByModel(pdx);

        for(Specimen sp: specimens){

            if(sp.getSample() != null){

                Sample xenoSample = sp.getSample();

                for(MolecularCharacterization mc: xenoSample.getMolecularCharacterizations()){

                    MolecularDataEntryDTO mde = new MolecularDataEntryDTO();

                    mde.setSampleId(xenoSample.getSourceSampleId()== null ? NOT_SPECIFIED :xenoSample.getSourceSampleId());
                    mde.setSampleType("Engrafted Tumor");
                    mde.setEngraftedTumorPassage(sp.getPassage());
                    mde.setMolcharType(mc.getType());
                    mde.setDataAvailableLabel(mc.getPlatform().getName());
                    mde.setDataAvailableUrl("");
                    mde.setPlatformUsedLabel(mc.getPlatform().getName());
                    mde.setPlatformUsedUrl(mc.getPlatform().getUrl());
                    mde.setRawDataLabel("Not available");
                    mde.setMolcharId(mc.getId().toString());

                    int assocData = molecularCharacterizationRepository.findAssociationsNumberById(mc);

                    if(assocData == 0){
                        mde.setDataAssociated("NO");
                    }
                    else{
                        mde.setDataAssociated("YES");
                        dataTypes.add(mc.getType());
                    }

                    if(mc.isVisible()){
                        mde.setIsVisible("YES");
                    }
                    else{
                        mde.setIsVisible("NO");
                    }

                    mdeDTO.add(mde);

                }


            }

            // Get PDX Publication Data
            List<String> pubMedIds = new ArrayList<>();
            Optional<Set<Group>> optionalGroups = Optional.ofNullable(pdx.getGroups());

            optionalGroups.ifPresent(groups -> {

                for (Group group : groups){
                    if (group.getType().equals("Publication")){
                        pubMedIds.add(group.getPubMedId());
                    }
                }
            });

            dto.setPublications(publicationService.getEuropePmcPublications(pubMedIds));

        }

        dto.setMolecularDataRows(mdeDTO);
        dto.setMolecularDataEntrySize(mdeDTO.size());
        dto.setDataTypes(dataTypes);
        return dto;
    }


    public MolecularDataTableDTO getMolecularDataTable(String id){

        Set<String> tableHeadersSet = new HashSet<>();
        ArrayList<String> tableHeaders = new ArrayList<>();
        List<List<String>> tableRows = new ArrayList<>();
        MolecularDataTableDTO dto = new MolecularDataTableDTO();

        int batchSize = 500;

        MolecularCharacterization mc = molecularCharacterizationRepository.getMolecularDataById(Long.valueOf(id));

        int numberOfAssociations = markerAssociationRepository.getMarkerAssociationCountByMolCharId(Long.valueOf(id));
        List<MarkerAssociation> associationList = new ArrayList<>();

        for(int i=0; i<numberOfAssociations; i += batchSize){

            List<MarkerAssociation> subList = markerAssociationRepository.findAssociationsByMolCharIdFromTo(Long.valueOf(id), i, batchSize);
            associationList.addAll(subList);
        }


        //check if molchar exists and if not, display an error message
        if(mc == null){

            tableHeaders.add("");

            List<String> notVisibleDataRow = new ArrayList<>();
            notVisibleDataRow.add("ERROR: This molecular characterization does not exist.");

            tableRows.add(notVisibleDataRow);

            dto.setTableHeaders(tableHeaders);
            dto.setTableRows(tableRows);

            return dto;
        }


        //check if data is visible and can be displayed
        if(!mc.isVisible()){

            tableHeaders.add("");

            List<String> notVisibleDataRow = new ArrayList<>();
            notVisibleDataRow.add("This data is only accessible through the provider website - please click on 'Contact Provider' button above to request access.");

            tableRows.add(notVisibleDataRow);

            dto.setTableHeaders(tableHeaders);
            dto.setTableRows(tableRows);
            dto.setVisible(false);

            return dto;
        }



        Sample sample = sampleRepository.findSampleByMolcharId(Long.valueOf(id));
        String sampleId = sample.getSourceSampleId() == null ? "" : sample.getSourceSampleId();


        //STEP 0: Add sampleid to table, we always display this
        tableHeadersSet.add(MolCharTable.SAMPLE_ID.key());


        //STEP 1: dynamically determine the headers of the table
        for(MarkerAssociation ma: associationList){


            if(ma.getChromosome() != null && !ma.getChromosome().isEmpty()){
                tableHeadersSet.add(MolCharTable.CHROMOSOME.key());
            }

            if(ma.getSeqPosition() != null && !ma.getSeqPosition().isEmpty()){
                tableHeadersSet.add("seqposition");
            }

            if(ma.getRefAllele() != null && !ma.getRefAllele().isEmpty()){
                tableHeadersSet.add(MolCharTable.REFALLELE.key());
            }

            if(ma.getAltAllele() != null && !ma.getAltAllele().isEmpty()){
                tableHeadersSet.add(MolCharTable.ALTALLELE.key());
            }

            if(ma.getConsequence() != null && !ma.getConsequence().isEmpty()){
                tableHeadersSet.add(MolCharTable.CONSEQUENCE.key());
            }

            if(ma.getMarker() != null){
                tableHeadersSet.add(MolCharTable.HGNC_SYMBOL.key());
            }

            if(ma.getZscore() != null && !ma.getZscore().isEmpty()){
                tableHeadersSet.add(MolCharTable.ZSCORE.key());
            }

            if(ma.getAminoAcidChange() != null && !ma.getAminoAcidChange().isEmpty()){
                tableHeadersSet.add(MolCharTable.AMINOACID_CHANGE.key());
            }

            if(ma.getReadDepth() != null && !ma.getReadDepth().isEmpty()){
                tableHeadersSet.add(MolCharTable.READ_DEPTH.key());
            }

            if(ma.getAlleleFrequency() != null && !ma.getAlleleFrequency().isEmpty()){
                tableHeadersSet.add(MolCharTable.ALLELE_FREQUENCY.key());
            }


            if(ma.getRsIdVariants() != null && !ma.getRsIdVariants().isEmpty()){
                tableHeadersSet.add(MolCharTable.RS_IDVARIANTS.key());
            }

            if(ma.getNucleotideChange() != null && !ma.getNucleotideChange().isEmpty()){
                tableHeadersSet.add(MolCharTable.NUCLEOTIDE_CHANGE.key());
            }

            if(ma.getGenomeAssembly() != null && !ma.getGenomeAssembly().isEmpty()){
                tableHeadersSet.add(MolCharTable.GENOME_ASSEMBLY.key());
            }

            if(ma.getSeqStartPosition() != null && !ma.getSeqStartPosition().isEmpty()){
                tableHeadersSet.add(MolCharTable.SEQ_STARTPOSITION.key());
            }

            if(ma.getSeqEndPosition() != null && !ma.getSeqEndPosition().isEmpty()){
                tableHeadersSet.add(MolCharTable.SEQ_ENDPOSITION.key());
            }

            if(ma.getStrand() != null && !ma.getStrand().isEmpty()){
                tableHeadersSet.add("strand");
            }

            if(ma.getEnsemblTranscriptId() != null && !ma.getEnsemblTranscriptId().isEmpty()){
                tableHeadersSet.add(MolCharTable.ENSEMBL_TRANSCRIPTID.key());
            }

            if(ma.getUcscTranscriptId() != null && !ma.getUcscTranscriptId().isEmpty()){
                tableHeadersSet.add("ucsctranscriptid");
            }

            if(ma.getNcbiTranscriptId() != null && !ma.getNcbiTranscriptId().isEmpty()){
                tableHeadersSet.add("ncbitranscriptid");
            }

            if(ma.getCdsChange() != null && !ma.getCdsChange().isEmpty()){
                tableHeadersSet.add("cdschange");
            }

            if(ma.getType() != null && !ma.getType().isEmpty()){
                tableHeadersSet.add("type");
            }

            if(ma.getAnnotation() != null && !ma.getAnnotation().isEmpty()){
                tableHeadersSet.add("annotation");
            }

            if(ma.getCytogeneticsResult() != null && !ma.getCytogeneticsResult().isEmpty()){
                tableHeadersSet.add(MolCharTable.CYTOGENETICS_RESULT.key());
            }

            if(ma.getMicrosatelliteResult() != null && !ma.getMicrosatelliteResult().isEmpty()){
                tableHeadersSet.add("microsateliteresult");
            }

            if(ma.getProbeIDAffymetrix() != null && !ma.getProbeIDAffymetrix().isEmpty()){
                tableHeadersSet.add(MolCharTable.PROBEID_AFFYMETRIX.key());
            }

            if(ma.getCnaLog2RCNA() != null && !ma.getCnaLog2RCNA().isEmpty()){
                tableHeadersSet.add(MolCharTable.CNALOG2_RCNA.key());
            }

            if(ma.getCnaLog10RCNA() != null && !ma.getCnaLog10RCNA().isEmpty()){
                tableHeadersSet.add(MolCharTable.CNALOG10_RCNA.key());
            }

            if(ma.getCnaCopyNumberStatus() != null && !ma.getCnaCopyNumberStatus().isEmpty()){
                tableHeadersSet.add(MolCharTable.CNA_COPYNUMBER_STATUS.key());
            }

            if(ma.getCnaGisticValue() != null && !ma.getCnaGisticValue().isEmpty()){
                tableHeadersSet.add(MolCharTable.CNA_GISTICVALUES.key());
            }

            if(ma.getCnaPicnicValue() != null && !ma.getCnaPicnicValue().isEmpty()){
                tableHeadersSet.add("cnapicnicvalue");
            }

        }


        //STEP 2: Determine table headers order
        // DON'T CHANGE THE ORDER OF THESE CONDITIONS OR THE WORLD WILL TREMBLE!
        // (But if you REALLY need to change the order, don't forget to change it at step 3, too!!!)



        if(tableHeadersSet.contains(MolCharTable.SAMPLE_ID.key())){
            tableHeaders.add(MolCharTable.SAMPLE_ID.col());
        }

        if(tableHeadersSet.contains(MolCharTable.HGNC_SYMBOL.key())){
            tableHeaders.add(MolCharTable.HGNC_SYMBOL.col());
        }

        if(tableHeadersSet.contains(MolCharTable.AMINOACID_CHANGE.key())){
            tableHeaders.add(MolCharTable.AMINOACID_CHANGE.col());
        }

        if(tableHeadersSet.contains(MolCharTable.CONSEQUENCE.key())){
            tableHeaders.add(MolCharTable.CONSEQUENCE.col());
        }

        if(tableHeadersSet.contains(MolCharTable.NUCLEOTIDE_CHANGE.key())){
            tableHeaders.add(MolCharTable.NUCLEOTIDE_CHANGE.col());
        }

        if(tableHeadersSet.contains(MolCharTable.READ_DEPTH.key())){
            tableHeaders.add(MolCharTable.READ_DEPTH.col());
        }

        if(tableHeadersSet.contains(MolCharTable.ALLELE_FREQUENCY.key())){
            tableHeaders.add(MolCharTable.ALLELE_FREQUENCY.col());
        }

        if(tableHeadersSet.contains(MolCharTable.PROBEID_AFFYMETRIX.key())){
            tableHeaders.add(MolCharTable.PROBEID_AFFYMETRIX.col());
        }

        if(tableHeadersSet.contains(MolCharTable.CNALOG10_RCNA.key())){
            tableHeaders.add(MolCharTable.CNALOG10_RCNA.col());
        }

        if(tableHeadersSet.contains(MolCharTable.CNALOG2_RCNA.key())){
            tableHeaders.add(MolCharTable.CNALOG2_RCNA.col());
        }

        if(tableHeadersSet.contains(MolCharTable.CNA_COPYNUMBER_STATUS.key())){
            tableHeaders.add(MolCharTable.CNA_COPYNUMBER_STATUS.col());
        }

        if(tableHeadersSet.contains(MolCharTable.CNA_GISTICVALUES.key())){
            tableHeaders.add(MolCharTable.CNA_GISTICVALUES.col());
        }

        if(tableHeadersSet.contains(MolCharTable.CHROMOSOME.key())){
            tableHeaders.add(MolCharTable.CHROMOSOME.col());
        }

        if(tableHeadersSet.contains(MolCharTable.SEQ_STARTPOSITION.key())){
            tableHeaders.add(MolCharTable.SEQ_STARTPOSITION.col());
        }

        if(tableHeadersSet.contains(MolCharTable.SEQ_ENDPOSITION.key())){
            tableHeaders.add(MolCharTable.SEQ_ENDPOSITION.col());
        }

        if(tableHeadersSet.contains(MolCharTable.REFALLELE.key())){
            tableHeaders.add(MolCharTable.REFALLELE.col());
        }

        if(tableHeadersSet.contains(MolCharTable.ALTALLELE.key())){
            tableHeaders.add(MolCharTable.ALTALLELE.col());
        }

        if(tableHeadersSet.contains(MolCharTable.RS_IDVARIANTS.key())){
            tableHeaders.add(MolCharTable.RS_IDVARIANTS.col());
        }

        if(tableHeadersSet.contains(MolCharTable.ENSEMBL_TRANSCRIPTID.key())){
            tableHeaders.add(MolCharTable.ENSEMBL_TRANSCRIPTID.col());
        }

        if(tableHeadersSet.contains(MolCharTable.ENSEMBL_GENEID.key())){
            tableHeaders.add(MolCharTable.ENSEMBL_GENEID.col());
        }

        if(tableHeadersSet.contains(MolCharTable.UCSC_GENEID.key())){
            tableHeaders.add(MolCharTable.UCSC_GENEID.col());
        }

        if(tableHeadersSet.contains(MolCharTable.NCBI_GENEID.key())){
            tableHeaders.add(MolCharTable.NCBI_GENEID.col());
        }


        if(tableHeadersSet.contains(MolCharTable.ZSCORE.key())){
            tableHeaders.add(MolCharTable.ZSCORE.col());
        }

        if(tableHeadersSet.contains(MolCharTable.GENOME_ASSEMBLY.key())){
            tableHeaders.add(MolCharTable.GENOME_ASSEMBLY.col());
        }

        if(tableHeadersSet.contains(MolCharTable.CYTOGENETICS_RESULT.key())){
            tableHeaders.add(MolCharTable.CYTOGENETICS_RESULT.col());
        }



        //STEP 3: Insert the rows of the table
        // DON'T CHANGE THE ORDER OF THESE CONDITIONS OR THE WORLD WILL TREMBLE!
        // (But if you REALLY need to change the order, don't forget to change it at step 2, too!!!)
        for(MarkerAssociation ma: associationList){

            List<String> row = new ArrayList<>();


            if(tableHeadersSet.contains(MolCharTable.SAMPLE_ID.key())){
                row.add(sampleId);
            }

            if(tableHeadersSet.contains(MolCharTable.HGNC_SYMBOL.key())){
                row.add(ma.getMarker().getHgncSymbol());
            }

            if(tableHeadersSet.contains(MolCharTable.AMINOACID_CHANGE.key())){
                row.add(ma.getAminoAcidChange() == null ? "" : ma.getAminoAcidChange());
            }

            if(tableHeadersSet.contains(MolCharTable.CONSEQUENCE.key())){
                row.add((ma.getConsequence() == null ? "": ma.getConsequence()));
            }

            if(tableHeadersSet.contains(MolCharTable.NUCLEOTIDE_CHANGE.key())){
                row.add((ma.getNucleotideChange() == null ? "":ma.getNucleotideChange()));
            }

            if(tableHeadersSet.contains(MolCharTable.READ_DEPTH.key())){
                row.add((ma.getReadDepth() == null ? "":ma.getReadDepth()));
            }

            if(tableHeadersSet.contains(MolCharTable.ALLELE_FREQUENCY.key())){
                row.add((ma.getAlleleFrequency() == null? "":ma.getAlleleFrequency()));
            }

            if(tableHeadersSet.contains(MolCharTable.PROBEID_AFFYMETRIX.key())){
                row.add((ma.getProbeIDAffymetrix() == null ? "":ma.getProbeIDAffymetrix()));
            }

            if(tableHeadersSet.contains(MolCharTable.CNALOG10_RCNA.key())){
                row.add((ma.getCnaLog10RCNA() == null ? "":ma.getCnaLog10RCNA()));
            }

            if(tableHeadersSet.contains(MolCharTable.CNALOG2_RCNA.key())){
                row.add((ma.getCnaLog2RCNA() == null ? "":ma.getCnaLog2RCNA()));
            }

            if(tableHeadersSet.contains(MolCharTable.CNA_COPYNUMBER_STATUS.key())){
                row.add((ma.getCnaCopyNumberStatus() == null ? "":ma.getCnaCopyNumberStatus()));
            }

            if(tableHeadersSet.contains(MolCharTable.CNA_GISTICVALUES.key())){
                row.add((ma.getCnaGisticValue() == null ? "" : ma.getCnaGisticValue()));
            }

            if(tableHeadersSet.contains(MolCharTable.CHROMOSOME.key())){
                row.add((ma.getChromosome() == null ? "" : ma.getChromosome()));
            }

            if(tableHeadersSet.contains(MolCharTable.SEQ_STARTPOSITION.key())){
                row.add((ma.getSeqStartPosition() == null ? "" : ma.getSeqStartPosition()));
            }

            if(tableHeadersSet.contains(MolCharTable.SEQ_ENDPOSITION.key())){
                row.add((ma.getSeqEndPosition() == null ? "" : ma.getSeqEndPosition()));
            }

            if(tableHeadersSet.contains(MolCharTable.REFALLELE.key())){
                row.add((ma.getRefAllele() == null ? "" : ma.getRefAllele()));
            }

            if(tableHeadersSet.contains(MolCharTable.ALTALLELE.key())){
                row.add((ma.getAltAllele() == null ? "" : ma.getAltAllele()));
            }

            if(tableHeadersSet.contains(MolCharTable.RS_IDVARIANTS.key())){
                row.add((ma.getRsIdVariants() == null ? "" : ma.getRsIdVariants()));
            }

            if(tableHeadersSet.contains(MolCharTable.ENSEMBL_TRANSCRIPTID.key())){
                row.add((ma.getEnsemblTranscriptId() == null ? "" : ma.getEnsemblTranscriptId()));
            }

            if(tableHeadersSet.contains(MolCharTable.ENSEMBL_GENEID.key())){
                row.add((ma.getMarker().getEnsemblGeneId() == null ? "": ma.getMarker().getEnsemblGeneId() ));
            }

            if(tableHeadersSet.contains(MolCharTable.UCSC_GENEID.key())){
                row.add((ma.getMarker().getUcscGeneId() == null ? "" : ma.getMarker().getUcscGeneId()));
            }

            if(tableHeadersSet.contains(MolCharTable.NCBI_GENEID.key())){
                row.add((ma.getMarker().getNcbiGeneId() == null ? "" : ma.getMarker().getNcbiGeneId()));
            }

            if(tableHeadersSet.contains(MolCharTable.ZSCORE.key())){
                row.add((ma.getZscore() == null ? "" : ma.getZscore()));
            }

            if(tableHeadersSet.contains(MolCharTable.GENOME_ASSEMBLY.key())){
                row.add((ma.getGenomeAssembly() == null ? "" : ma.getGenomeAssembly()));
            }

            if(tableHeadersSet.contains(MolCharTable.CYTOGENETICS_RESULT.key())){
                row.add(ma.getCytogeneticsResult() == null ? "" : ma.getCytogeneticsResult());
            }


            tableRows.add(row);

        }


        dto.setTableHeaders(tableHeaders);
        dto.setTableRows(tableRows);


        return dto;
    }



    /**
     * Return a formatted string representing the host and passage
     * @param hostStrain    the key to the map of host strains
     * @param hostStrainMap the map containing all the host strains associated to the model
     * @return a formatted string representing the host strains
     */
    private String getHostStrainString(String hostStrain, Map<String, String> hostStrainMap) {
        String passage = hostStrainMap.get(hostStrain).equals(NOT_SPECIFIED) ? "" : "(" + hostStrainMap.get(hostStrain) + ")";
        String formatted = String.format("%s%s", hostStrain, passage);
        return formatted;
    }


    public Map findPatientPlatforms(String dataSource, String modelId){

        Map<String, String> platformMap = new HashMap<>();

        List<MolecularCharacterization> molecularCharacterizations = molecularCharacterizationRepository.findPatientPlatformByModelId(dataSource,modelId);

        for (MolecularCharacterization mc : molecularCharacterizations) {

            if(mc.getPlatform() != null){
                platformMap.put(mc.getPlatform().getName(), mc.getPlatform().getName());
            }

        }

        return platformMap;
    }

    public Map<String, Set<String>> findModelPlatformAndPassages(String dataSource, String modelId,String passage){

        /**
         * Used to store a technology String with their respective List of PDX Passages
         */
        Map<String, Set<String>> platformMap = new HashMap<>();

        /**
         * Retrieve all the technologies for that mouse model
         */
        List<Platform> platforms = platformRepository.findModelPlatformByModelId(dataSource,modelId);

        /**
         * For each of the technologies retrieve the list of PDX passages using the specimen repository
         */
        for (Platform platform : platforms) {

            List<Specimen> specimens = specimenRepository.findSpecimenBySourcePdxIdAndPlatform2(dataSource,modelId,platform.getName());

            Set<String> passagesList = new HashSet<>();
            for (Specimen specimen : specimens)
            {
                passagesList.add(specimen.getPassage()+"");
            }

            platformMap.put(platform.getName(), passagesList);
        }

        return platformMap;
    }


    public List<String> getModelsOriginatedFromSamePatient(String dataSource, String modelId) {

        return patientRepository.getModelsOriginatedFromSamePatientByDataSourceAndModelId(dataSource, modelId);
    }


    public List<DrugSummaryDTO> getDrugSummary(String dataSource, String modelId) {

        TreatmentSummary ts = treatmentSummaryRepository.findModelTreatmentByDataSourceAndModelId(dataSource, modelId);

        List<DrugSummaryDTO> results = new ArrayList<>();

        if (ts != null && ts.getTreatmentProtocols() != null) {

            for (TreatmentProtocol tp : ts.getTreatmentProtocols()) {


                DrugSummaryDTO dto = new DrugSummaryDTO();
                dto.setDrugName(tp.getTreatmentString(true));
                List<TreatmentComponent> components = tp.getComponents();
                String dose = "";

                if(components.size()>0){
                    for(TreatmentComponent tc : components){
                        if(!dose.equals("")){
                            dose += " / ";
                        }
                        dose += tc.getDose();
                    }
                }
                dto.setDose(dose);

                if (tp.getResponse() != null && tp.getResponse().getDescription() != null) {
                    dto.setResponse(tp.getResponse().getDescription());
                } else {
                    dto.setResponse("");
                }

                results.add(dto);
            }
        }

        return results;
    }


    public List<String> getCsvHead(String molcharType){


        List<String> commonHead = Arrays.asList("Sample ID","Sample Origin","Passage","Histology","Data Type","Platform","HGNC Symbol");

        List<String> mutHead = Arrays.asList("Nucleotide Change","Amino Acid Change","Read Depth","Allele Freq","RS ID Variant","Chromosome","Seq Start Position","Ref Allele","Alt Allele","Consequence","Genome Assembly");

        List<String> cnaHead = Arrays.asList("Log10R CNA","Log2R CNA","CNA Status","Gistic Value CNA","Picnic Value CNA","Chromosome","Seq Start Position","Seq End Position","Genome Assembly");

        List<String> cytogeneticsHead = Arrays.asList("Cytogenetics Result");

        List<String> csvHead;

        if (molcharType.equals("mutation")) {

            csvHead = ListUtils.union(commonHead,mutHead);
        }else if (molcharType.equals("copy-number-alteration")){

            csvHead = ListUtils.union(commonHead,cnaHead);
        }else{

            csvHead = ListUtils.union(commonHead,cytogeneticsHead);
        }

        return csvHead;
    }


    public List<List<String>> getVariationDataByMolcharTypeCSV(String dataSource, String modelId, String molcharType) {

        molcharType = molcharType.replace("-"," ");

        /**
         *  Retreive Diagnosis Information and get Specimens
         */
        Sample patientSample = sampleRepository.findPatientSampleWithDetailsByDataSourceAndPdxId(dataSource, modelId);

        String mappedOntologyTermLabel = patientSample.getSampleToOntologyRelationship().getOntologyTerm().getLabel();

        List<Specimen> specimens = specimenRepository.findSpecimenBySourcePdxId(dataSource, modelId, molcharType);

        patientSample = sampleRepository.findHumanSampleBySourcePdxIdAndMolcharType(dataSource, modelId, molcharType);


        List<List<String>> variationData = new ArrayList();

        if (specimens != null) {
            for (Specimen specimen : specimens) {
                variationData.addAll(buildUpDTO(specimen.getSample(), specimen.getPassage(), mappedOntologyTermLabel, molcharType));
            }
        }

        if(patientSample != null){

            variationData.addAll(buildUpDTO(patientSample, "", mappedOntologyTermLabel, molcharType));
        }


        return variationData;

    }



    public List<List<String>> buildUpDTO(Sample sample,String passage,String mappedOntologyTermLabel, String molcharType){

        List<List<String>> variationData = new LinkedList<>();

        try {

            int count = 1;
            for (MolecularCharacterization dMolChar : sample.getMolecularCharacterizations()) {

                List<MarkerAssociation> markerAssociations = new ArrayList();
                markerAssociations.addAll(dMolChar.getMarkerAssociations());


                for (MarkerAssociation markerAss : markerAssociations) {

                    List<String> dData = new ArrayList<>();

                    dData.add(sample.getSourceSampleId());
                    dData.add( (passage.equals("")) ? "Patient Tumor" : "Xenograft" );
                    dData.add(passage);
                    dData.add(mappedOntologyTermLabel);
                    dData.add(WordUtils.capitalizeFully(molcharType));
                    dData.add(dMolChar.getPlatform().getName());
                    dData.add(markerAss.getMarker().getHgncSymbol());

                    if (molcharType.equals("mutation")){
                        dData.add(markerAss.getNucleotideChange());
                        dData.add(markerAss.getAminoAcidChange());
                        dData.add(markerAss.getReadDepth());
                        dData.add(markerAss.getAlleleFrequency());
                        dData.add(markerAss.getRsIdVariants());
                        dData.add(markerAss.getChromosome());
                        dData.add(markerAss.getSeqStartPosition());
                        dData.add(markerAss.getRefAllele());
                        dData.add(markerAss.getAltAllele());
                        dData.add(markerAss.getConsequence());
                        dData.add(markerAss.getGenomeAssembly());
                    }

                    if (molcharType.equals("copy number alteration")){

                        dData.add(markerAss.getCnaLog10RCNA());
                        dData.add(markerAss.getCnaLog2RCNA());
                        dData.add(markerAss.getCnaCopyNumberStatus());
                        dData.add(markerAss.getCnaGisticValue());
                        dData.add(markerAss.getCnaPicnicValue());
                        dData.add(markerAss.getChromosome());
                        dData.add(markerAss.getSeqStartPosition());
                        dData.add(markerAss.getSeqEndPosition());
                        dData.add(markerAss.getGenomeAssembly());
                    }

                    if (molcharType.equals("cytogenetics")){

                        dData.add(markerAss.getCytogeneticsResult());
                    }

                    /*
                        markerAssocArray[13] = sample.getDiagnosis();
                        markerAssocArray[14] = sample.getType().getName();

                     */
                    variationData.add(dData);

                }
            }


        }catch (Exception e) { }


        return variationData;
    }


    public Sort.Direction getSortDirection(String sortDir){

        Sort.Direction direction = Sort.Direction.ASC;

        if (sortDir.equals("desc")){
            direction = Sort.Direction.DESC;
        }

        return direction;
    }



    public String notEmpty(String incoming){

        String result = (incoming == null) ? NOT_SPECIFIED : incoming;

        result = result.equals("null") ? NOT_SPECIFIED : result;

        result = result.length() == 0 ? NOT_SPECIFIED : result;

        result = StringUtils.isEmpty(incoming) ? NOT_SPECIFIED : result;

        result = result.equals("Unknown") ? NOT_SPECIFIED : result;

        return result;
    }


}
