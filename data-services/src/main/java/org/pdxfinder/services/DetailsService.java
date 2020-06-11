package org.pdxfinder.services;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.*;
import org.pdxfinder.services.dto.*;
import org.pdxfinder.services.dto.pdxgun.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.join;

/*
 * Created by abayomi on 09/05/2018.
 */
@Service
public class DetailsService {


    private SampleRepository sampleRepository;

    private PatientRepository patientRepository;
    private PatientSnapshotRepository patientSnapshotRepository;
    private ModelCreationRepository modelCreationRepository;
    private SpecimenRepository specimenRepository;
    private MolecularCharacterizationRepository molecularCharacterizationRepository;
    private PlatformRepository platformRepository;
    private TreatmentSummaryRepository treatmentSummaryRepository;
    private MarkerAssociationRepository markerAssociationRepository;

    private GraphService graphService;
    private Map<String, List<String>> facets = new HashMap<>();
    private PlatformService platformService;
    private DrugService drugService;
    private PatientService patientService;
    private PublicationService publicationService;

    private final static Logger log = LoggerFactory.getLogger(DetailsService.class);
    private ReferenceDbService referenceDbService;


    public DetailsService(SampleRepository sampleRepository,
                          PatientRepository patientRepository,
                          PatientSnapshotRepository patientSnapshotRepository,
                          ModelCreationRepository modelCreationRepository,
                          SpecimenRepository specimenRepository,
                          MolecularCharacterizationRepository molecularCharacterizationRepository,
                          PlatformRepository platformRepository,
                          TreatmentSummaryRepository treatmentSummaryRepository,
                          GraphService graphService,
                          PlatformService platformService,
                          DrugService drugService,
                          PatientService patientService,
                          MarkerAssociationRepository markerAssociationRepository,
                          PublicationService publicationService,
                          ReferenceDbService referenceDbService) {

        this.sampleRepository = sampleRepository;
        this.patientRepository = patientRepository;
        this.patientSnapshotRepository = patientSnapshotRepository;
        this.modelCreationRepository = modelCreationRepository;
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;
        this.specimenRepository = specimenRepository;
        this.platformRepository = platformRepository;
        this.treatmentSummaryRepository = treatmentSummaryRepository;
        this.graphService = graphService;
        this.platformService = platformService;
        this.drugService = drugService;
        this.patientService = patientService;
        this.markerAssociationRepository = markerAssociationRepository;
        this.publicationService = publicationService;
        this.referenceDbService = referenceDbService;

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

            pdx.getExternalUrls().stream().forEach(extUrl -> {
                if (extUrl.getType().equals(ExternalUrl.Type.SOURCE.getValue())) {
                    dto.setViewDataAtUrl(extUrl.getUrl());
                } else {
                    dto.setContactProviderUrl(extUrl.getUrl());
                }
            });

            dto.setViewDataAtLabel("View Data at " + pdx.getDataSource());
        } else {
            dto.setViewDataAtUrl("#");
            dto.setViewDataAtLabel("Unknown source");
        }


        PatientSnapshot currentPatientSnapshot = null;
        //since there is only one element being returned in the set, this will give the current snapshot for the patient
        for (PatientSnapshot ps : patient.getSnapshots()) {
            currentPatientSnapshot = ps;
        }

        if (currentPatientSnapshot != null && currentPatientSnapshot.getAgeAtCollection() != null) {
            dto.setAgeAtTimeOfCollection(currentPatientSnapshot.getAgeAtCollection());
        } else {
            dto.setAgeAtTimeOfCollection("Not specified");
        }

        if (patient.getRace() != null && !patient.getRace().isEmpty()) {
            dto.setRace(patient.getRace());
        } else {
            dto.setRace("Not specified");
        }

        if (patient.getEthnicity() != null && !patient.getEthnicity().isEmpty()) {

            dto.setEthnicity(patient.getEthnicity());
        } else {

            dto.setEthnicity("Not specified");
        }

        if (patientSample.getSampleToOntologyRelationship() != null && patientSample.getSampleToOntologyRelationship().getOntologyTerm() != null) {
            dto.setMappedOntologyTermLabel(patientSample.getSampleToOntologyRelationship().getOntologyTerm().getLabel());
        } else {
            dto.setMappedOntologyTermLabel("");
        }

        dto.setProviderName(providerGroup.getName());
        dto.setContactProviderLabel(providerGroup.getAbbreviation());
        dto.setContactProviderUrl(providerGroup.getContact());


        if (pdx != null && pdx.getExternalUrls() != null) {

            pdx.getExternalUrls().stream().forEach(extUrl -> {
                if (extUrl.getType().equals(ExternalUrl.Type.SOURCE.getValue())) {
                    dto.setViewDataAtUrl(extUrl.getUrl());
                } else {
                    dto.setContactProviderUrl(extUrl.getUrl());
                }
            });

            dto.setViewDataAtLabel("View Data at " + pdx.getDataSource());
        } else {
            dto.setViewDataAtUrl("#");
            dto.setViewDataAtLabel("Unknown source");
        }


        dto.setPatientSex(patient.getSex());

        dto.setRelatedModels(getModelsOriginatedFromSamePatient(dataSource, modelId));

        if (patientSample.getOriginTissue() != null) {
            dto.setPrimaryTissue(patientSample.getOriginTissue().getName());
        }

        if (patientSample.getSampleSite() != null) {
            dto.setCollectionSite(patientSample.getSampleSite().getName());
        }

        if (patientSample.getType() != null) {
            dto.setTumorType(patientSample.getType().getName());
        }

        if (patientSample.getStage() != null) {
            dto.setStage(patientSample.getStage());
        }

        if (patientSample.getStageClassification() != null) {
            dto.setStageClassification(patientSample.getStageClassification());
        }

        if (patientSample.getGrade() != null) {
            dto.setGrade(patientSample.getGrade());
        }

        if (patientSample.getGradeClassification() != null) {
            dto.setGradeClassification(patientSample.getGradeClassification());
        }


        //Assembling PDX MODEL ENGRAFTMENT DATA
        Set<EngraftmentDataDTO> engraftmentData = new HashSet<>();
        //engraftments >> passages[]
        Map<EngraftmentDataDTO, Set<String>> engraftmentDataMap = new HashMap<>();

        if (pdx.getSpecimens() != null) {

            for (Specimen sp : pdx.getSpecimens()) {

                if (sp.getHostStrain() != null) {

                    EngraftmentDataDTO edto = new EngraftmentDataDTO();

                    edto.setStrainName(
                            (sp.getHostStrain() != null) ? notEmpty(sp.getHostStrain().getName()) : "Not Specified"
                    );

                    edto.setEngraftmentSite(
                            (sp.getEngraftmentSite() != null) ? notEmpty(sp.getEngraftmentSite().getName()) : "Not Specified"
                    );

                    edto.setEngraftmentType(
                            (sp.getEngraftmentType() != null) ? notEmpty(sp.getEngraftmentType().getName()) : "Not Specified"
                    );

                    edto.setEngraftmentMaterial(
                            (sp.getEngraftmentMaterial() != null) ? notEmpty(sp.getEngraftmentMaterial().getName()) : "Not Specified"
                    );

                    edto.setEngraftmentMaterialState(
                            (sp.getEngraftmentMaterial() != null) ? notEmpty(sp.getEngraftmentMaterial().getState()) : "Not Specified"
                    );

                    String passage = (sp.getPassage() != null) ? notEmpty(sp.getPassage()) : "Not Specified";

                    //if the datakey combination is found, then don't add new Engraftment data, but rather uodate the passage accordingly
                    if (engraftmentDataMap.containsKey(edto)) {

                        engraftmentDataMap.get(edto).add(passage);

                    } else {

                        // If new, save in the Map
                        engraftmentDataMap.put(edto, new HashSet<>(Arrays.asList(passage)));
                    }
                }
            }
        }

        for (Map.Entry<EngraftmentDataDTO, Set<String>> entry : engraftmentDataMap.entrySet()) {

            EngraftmentDataDTO edto = entry.getKey();
            Set<String> passages = entry.getValue();
            List<String> passageList = new ArrayList<>();
            passageList.addAll(passages);

            //order the passages:
            Collections.sort(passageList);
            edto.setPassage(join(passageList, ", "));
            engraftmentData.add(edto);

        }

        dto.setPdxModelList(engraftmentData);


        List<QualityAssurance> qaList = pdx.getQualityAssurance();
        if (qaList == null) qaList = new ArrayList<>();

        List<QualityControlDTO> qcontrolList = new ArrayList<>();
        for (QualityAssurance qa : qaList) {

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
        for (MolecularCharacterization mc : patientMCs) {

            MolecularDataEntryDTO mde = new MolecularDataEntryDTO();
            mde.setSampleId(patientSample.getSourceSampleId());
            mde.setSampleType("Patient Tumor");
            mde.setEngraftedTumorPassage("NA");
            mde.setMolcharType(mc.getType());
            mde.setDataAvailableLabel(mc.getPlatform().getName());
            mde.setDataAvailableUrl("");
            mde.setPlatformUsedLabel(mc.getPlatform().getName());
            mde.setMolcharId(mc.getId().toString());
            setRawDataLabelAndLink(mde, patientSample);

            if (mc.getPlatform().getName() == null || mc.getPlatform().getName().isEmpty() || mc.getPlatform().getName().toLowerCase().equals("not specified")
                    || mc.getPlatform().getUrl() == null || mc.getPlatform().getUrl().isEmpty()) {

                mde.setPlatformUsedUrl(null);
            } else {

                mde.setPlatformUsedUrl(mc.getPlatform().getUrl());
            }

            int assocData = molecularCharacterizationRepository.findAssociationsNumberById(mc);

            if (assocData == 0) {
                mde.setDataAssociated("NO");
            } else {
                mde.setDataAssociated("YES");
                dataTypes.add(mc.getType());
            }

            if (mc.isVisible()) {
                mde.setIsVisible("YES");
            } else {
                mde.setIsVisible("NO");
            }

            if (patientSample.getSourceSampleId() != null)
                mdeDTO.add(mde);
        }

        //then add molchars linked to the xenograft sample

        List<Specimen> specimens = specimenRepository.findAllWithMolcharDataByModel(pdx);

        for (Specimen sp : specimens) {

            if (sp.getSample() != null) {

                Sample xenoSample = sp.getSample();

                for (MolecularCharacterization mc : xenoSample.getMolecularCharacterizations()) {

                    MolecularDataEntryDTO mde = new MolecularDataEntryDTO();

                    mde.setSampleId(xenoSample.getSourceSampleId() == null ? "Not Specified" : xenoSample.getSourceSampleId());
                    mde.setSampleType("Engrafted Tumor");
                    mde.setEngraftedTumorPassage(sp.getPassage());
                    mde.setMolcharType(mc.getType());
                    mde.setDataAvailableLabel(mc.getPlatform().getName());
                    mde.setDataAvailableUrl("");
                    mde.setPlatformUsedLabel(mc.getPlatform().getName());
                    mde.setPlatformUsedUrl(mc.getPlatform().getUrl());
                    mde.setMolcharId(mc.getId().toString());
                    setRawDataLabelAndLink(mde, xenoSample);

                    int assocData = molecularCharacterizationRepository.findAssociationsNumberById(mc);

                    if (assocData == 0) {
                        mde.setDataAssociated("NO");
                    } else {
                        mde.setDataAssociated("YES");
                        dataTypes.add(mc.getType());
                    }

                    if (mc.isVisible()) {
                        mde.setIsVisible("YES");
                    } else {
                        mde.setIsVisible("NO");
                    }
                    //if (xenoSample.getSourceSampleId() != null)
                    mdeDTO.add(mde);
                }
            }
        }
        dto.setMolecularDataRows(mdeDTO);
        dto.setMolecularDataEntrySize(mdeDTO.size());
        dto.setDataTypes(dataTypes);

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

        return dto;
    }

    private void setRawDataLabelAndLink(MolecularDataEntryDTO mde, Sample sample) {
        if (sample.getRawDataUrl() != null) {
            String[]rawDataArray = sample.getRawDataUrl().split(",");
            mde.setRawDataLabel(rawDataArray[0]);
            if (rawDataArray.length == 2) {
                mde.setRawDataLink(rawDataArray[1]);
            } else {
                mde.setRawDataLink("");
            }
        } else {
            mde.setRawDataLink("");
        }
    }


    public MolecularDataTableDTO getMolecularDataTable(String id){

        MolecularDataTableDTO dto = new MolecularDataTableDTO();
        MolecularCharacterization mc = molecularCharacterizationRepository.getMolecularDataById(Long.valueOf(id));

        if(mc == null){

            List<String> notVisibleDataRow = new ArrayList<>();
            notVisibleDataRow.add("ERROR: This molecular characterization does not exist.");
            dto.setReports(notVisibleDataRow);
        }
        else if(!mc.isVisible()){

            List<String> notVisibleDataRow = new ArrayList<>();
            notVisibleDataRow.add("This data is only accessible through the provider website - please click on 'CONTACT PROVIDER' button above to request access.");
            dto.setVisible(false);
            dto.setReports(notVisibleDataRow);
            dto.setMolecularDataRows(new ArrayList<>());
            return dto;
        }
        else {

            Sample sample = sampleRepository.findSampleByMolcharId(Long.valueOf(id));
            String sampleId = sample.getSourceSampleId() == null ? "" : sample.getSourceSampleId();

            MarkerAssociation markerAssociation = mc.getFirstMarkerAssociation();
            try {
                List<MolecularData> molecularDataList = markerAssociation.decodeMolecularData();
                List<MolecularDataRowDTO> tableData = this.getMolecularDataRow(sampleId, molecularDataList);
                dto.setMolecularDataRows(tableData);
            } catch (Exception e) {
                log.error("Error getting molecular data");
            }
        }
        return dto;
    }


    private List<MolecularDataRowDTO> getMolecularDataRow(String sampleId, List<MolecularData> molecularDataList){

        List<String> markerList = MolecularData.getMarkersFromMolecularDataList(molecularDataList);
        List<String> aminoAcidChangeList = MolecularData.getAminoAcidChangesFromMolecularDataList(molecularDataList);

        Map<String, Reference> variantsData = referenceDbService.getReferenceData(aminoAcidChangeList, "variant");
        Map<String, Reference> referenceData = referenceDbService.getReferenceData(markerList, "gene");

        List<MolecularDataRowDTO> tableData = new ArrayList<>();
        molecularDataList.forEach(md -> {

            Reference markerData = referenceDbService.getReference(md.getMarker(), referenceData);
            Reference aminoAcid = referenceDbService.getAminoAcidChangeReference(md.getAminoAcidChange(), variantsData,
                                                                                       md.getExistingVariations(),
                                                                                       md.getChromosome(),
                                                                                       md.getSeqStartPosition(),
                                                                                       md.getRefAllele(),
                                                                                       md.getAltAllele());

            MolecularDataRowDTO dataRow = new MolecularDataRowDTO();
            dataRow.setSampleId(sampleId)
                    .setHgncSymbol(markerData)
                    .setAminoAcidChange(aminoAcid)
                    .setConsequence(md.getConsequence())
                    .setNucleotideChange(md.getNucleotideChange())
                    .setReadDepth(md.getReadDepth() == null ? "" : md.getReadDepth())
                    .setAlleleFrequency(md.getAlleleFrequency())
                    .setProbeIdAffymetrix(md.getProbeIDAffymetrix())
                    .setCnaLog10rCna(md.getCnaLog10RCNA())
                    .setCnaLog2rCna(md.getCnaLog2RCNA())
                    .setCnaCopyNumberStatus(md.getCnaCopyNumberStatus())
                    .setCnaGisticValue(md.getCnaGisticValue())
                    .setChromosome(md.getChromosome())
                    .setSeqStartPosition(md.getSeqStartPosition())
                    .setSeqEndPosition(md.getSeqEndPosition())
                    .setRefAllele(md.getRefAllele())
                    .setAltAllele(md.getAltAllele())
                    .setExistingVariation(md.getExistingVariations())
                    .setVariantClass(md.getVariantClass())
                    .setEnsemblTranscriptId( md.getEnsemblTranscriptId())
                    .setEnsemblTranscriptId(md.getEnsemblGeneId())
                    .setUcscTranscriptId(md.getUcscGeneId())
                    .setNcbiTranscriptId( md.getNcbiGeneId())
                    .setRnaSeqCount( md.getRnaSeqCount())
                    .setZscore(md.getZscore())
                    .setGenomeAssembly(md.getGenomeAssembly())
                    .setCytogeneticsResult(md.getCytogeneticsResult())
                    .setIlluminaHGEAExp(md.getIlluminaHGEAExpressionValue())
                    .build();

            tableData.add(dataRow);
        });

        return tableData;
    }

    /**
     * Return a formatted string representing the host and passage
     *
     * @param hostStrain    the key to the map of host strains
     * @param hostStrainMap the map containing all the host strains associated to the model
     * @return a formatted string representing the host strains
     */
    private String getHostStrainString(String hostStrain, Map<String, String> hostStrainMap) {
        String passage = hostStrainMap.get(hostStrain).equals("Not Specified") ? "" : "(" + hostStrainMap.get(hostStrain) + ")";
        String formatted = String.format("%s%s", hostStrain, passage);
        return formatted;
    }


    public Map findPatientPlatforms(String dataSource, String modelId) {

        Map<String, String> platformMap = new HashMap<>();

        List<MolecularCharacterization> molecularCharacterizations = molecularCharacterizationRepository.findPatientPlatformByModelId(dataSource, modelId);

        for (MolecularCharacterization mc : molecularCharacterizations) {

            if (mc.getPlatform() != null) {
                platformMap.put(mc.getPlatform().getName(), mc.getPlatform().getName());
            }

        }

        return platformMap;
    }

    public Map<String, Set<String>> findModelPlatformAndPassages(String dataSource, String modelId, String passage) {

        /**
         * Used to store a technology String with their respective List of PDX Passages
         */
        Map<String, Set<String>> platformMap = new HashMap<>();

        /**
         * Retrieve all the technologies for that mouse model
         */
        List<Platform> platforms = platformRepository.findModelPlatformByModelId(dataSource, modelId);

        /**
         * For each of the technologies retrieve the list of PDX passages using the specimen repository
         */
        for (Platform platform : platforms) {

            List<Specimen> specimens = specimenRepository.findSpecimenBySourcePdxIdAndPlatform2(dataSource, modelId, platform.getName());

            Set<String> passagesList = new HashSet<>();
            for (Specimen specimen : specimens) {
                passagesList.add(specimen.getPassage() + "");
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

                if (components.size() > 0) {
                    for (TreatmentComponent tc : components) {
                        if (!dose.equals("")) {
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


    public List<String> getCsvHead(String molcharType) {


        List<String> commonHead = Arrays.asList("Sample ID", "Sample Origin", "Passage", "Histology", "Data Type", "Platform", "HGNC Symbol");

        List<String> mutHead = Arrays.asList("Nucleotide Change", "Amino Acid Change", "Read Depth", "Allele Freq", "RS ID Variant", "Chromosome", "Seq Start Position", "Ref Allele", "Alt Allele", "Consequence", "Genome Assembly");

        List<String> cnaHead = Arrays.asList("Log10R CNA", "Log2R CNA", "CNA Status", "Gistic Value CNA", "Picnic Value CNA", "Chromosome", "Seq Start Position", "Seq End Position", "Genome Assembly");

        List<String> cytogeneticsHead = Arrays.asList("Cytogenetics Result");

        List<String> csvHead;

        if (molcharType.equals("mutation")) {

            csvHead = ListUtils.union(commonHead, mutHead);
        } else if (molcharType.equals("copy-number-alteration")) {

            csvHead = ListUtils.union(commonHead, cnaHead);
        } else {

            csvHead = ListUtils.union(commonHead, cytogeneticsHead);
        }

        return csvHead;
    }


    public List<List<String>> getVariationDataByMolcharTypeCSV(String dataSource, String modelId, String molcharType) {

        molcharType = molcharType.replace("-", " ");

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

        if (patientSample != null) {

            variationData.addAll(buildUpDTO(patientSample, "", mappedOntologyTermLabel, molcharType));
        }


        return variationData;

    }


    public List<List<String>> buildUpDTO(Sample sample, String passage, String mappedOntologyTermLabel, String molcharType) {

        List<List<String>> variationData = new LinkedList<>();

        try {

            int count = 1;
            for (MolecularCharacterization dMolChar : sample.getMolecularCharacterizations()) {

                List<MarkerAssociation> markerAssociations = new ArrayList();
                markerAssociations.addAll(dMolChar.getMarkerAssociations());


                for (MarkerAssociation markerAss : markerAssociations) {

                    List<String> dData = new ArrayList<>();

                    dData.add(sample.getSourceSampleId());
                    dData.add((passage.equals("")) ? "Patient Tumor" : "Xenograft");
                    dData.add(passage);
                    dData.add(mappedOntologyTermLabel);
                    dData.add(WordUtils.capitalizeFully(molcharType));
                    dData.add(dMolChar.getPlatform().getName());


                    List<MolecularData> molecularData;
                    try {
                        molecularData = markerAss.getMolecularDataList();
                    } catch (Exception e) {
                        log.error("Error getting molecular data");
                        molecularData = new ArrayList<>();
                    }

                    for (MolecularData md : molecularData) {

                        dData.add(md.getMarker());

                        if (molcharType.equals("mutation")) {
                            dData.add(md.getNucleotideChange());
                            dData.add(md.getAminoAcidChange());
                            dData.add(md.getReadDepth());
                            dData.add(md.getAlleleFrequency());
                            dData.add(md.getExistingVariations());
                            dData.add(md.getChromosome());
                            dData.add(md.getSeqStartPosition());
                            dData.add(md.getRefAllele());
                            dData.add(md.getAltAllele());
                            dData.add(md.getConsequence());
                            dData.add(md.getGenomeAssembly());
                        }

                        if (molcharType.equals("copy number alteration")) {

                            dData.add(md.getCnaLog10RCNA());
                            dData.add(md.getCnaLog2RCNA());
                            dData.add(md.getCnaCopyNumberStatus());
                            dData.add(md.getCnaGisticValue());
                            dData.add(md.getCnaPicnicValue());
                            dData.add(md.getChromosome());
                            dData.add(md.getSeqStartPosition());
                            dData.add(md.getSeqEndPosition());
                            dData.add(md.getGenomeAssembly());
                        }

                        if (molcharType.equals("cytogenetics")) {

                            dData.add(md.getCytogeneticsResult());
                        }
                        variationData.add(dData);

                    }


                }
            }


        } catch (Exception e) {
        }


        return variationData;
    }


    public Sort.Direction getSortDirection(String sortDir) {

        Sort.Direction direction = Sort.Direction.ASC;

        if (sortDir.equals("desc")) {
            direction = Sort.Direction.DESC;
        }

        return direction;
    }


    public String notEmpty(String incoming) {

        String result = (incoming == null) ? "Not Specified" : incoming;
        result = result.equals("null") ? "Not Specified" : result;
        result = result.length() == 0 ? "Not Specified" : result;
        result = isEmpty(incoming) ? "Not Specified" : result;
        result = result.equals("Unknown") ? "Not Specified" : result;
        return result;
    }


}
