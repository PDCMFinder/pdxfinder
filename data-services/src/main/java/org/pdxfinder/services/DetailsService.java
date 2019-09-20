package org.pdxfinder.services;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.graph.repositories.*;
import org.pdxfinder.services.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

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

    private final String JAX_URL = "http://tumor.informatics.jax.org/mtbwi/pdxDetails.do?modelID=";
    private final String JAX_URL_TEXT = "View data at JAX";
    private final String IRCC_URL = "mailto:andrea.bertotti@unito.it?subject=";
    private final String IRCC_URL_TEXT = "Contact IRCC here";
    private final String PDMR_URL = "https://pdmdb.cancer.gov/pls/apex/f?p=101:41";
    private final String PDMR_URL_TEXT = "View data at PDMR";

    //private final String HCI_URL = "https://www.pdxnetwork.org/hcibcm/";
    private final String HCI_URL = "";
    private final String HCI_DS = "PDXNet-HCI-BCM";
    //    private final String WISTAR_URL = "https://www.pdxnetwork.org/the-wistarmd-andersonpenn/";
    private final String WISTAR_URL = "";

    private final String WISTAR_DS = "PDXNet-Wistar-MDAnderson-Penn";
    private final String MDA_URL = "";
    //private final String MDA_URL = "https://www.pdxnetwork.org/md-anderson/";
    private final String MDA_DS = "PDXNet-MDAnderson";
    //private final String WUSTL_URL = "https://www.pdxnetwork.org/wustl/";
    private final String WUSTL_URL = "";
    private final String WUSTL_DS = "PDXNet-WUSTL";

    private final static Logger log = LoggerFactory.getLogger(DetailsService.class);



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
                          MarkerAssociationRepository markerAssociationRepository) {

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



        PatientSnapshot currentPatientSnapshot = null;
        //since there is only one element being returned in the set, this will give the current snapshot for the patient
        for(PatientSnapshot ps: patient.getSnapshots()){
            currentPatientSnapshot = ps;
        }

        if(currentPatientSnapshot != null && currentPatientSnapshot.getAgeAtCollection() != null){

            dto.setAgeAtTimeOfCollection(currentPatientSnapshot.getAgeAtCollection());
        }
        else{

            dto.setAgeAtTimeOfCollection("Not specified");
        }

        if(patient.getRace() != null && !patient.getRace().isEmpty()){

            dto.setRace(patient.getRace());
        }
        else{

            dto.setRace("Not specified");
        }

        if(patient.getEthnicity() != null && !patient.getEthnicity().isEmpty()){

            dto.setEthnicity(patient.getEthnicity());
        }
        else{

            dto.setEthnicity("Not specified");
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


            if(mc.getPlatform().getName() == null || mc.getPlatform().getName().isEmpty() || mc.getPlatform().getName().toLowerCase().equals("not specified")
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

                    mde.setSampleId(xenoSample.getSourceSampleId()== null ? "Not Specified":xenoSample.getSourceSampleId());
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
                    //if (xenoSample.getSourceSampleId() != null)
                    mdeDTO.add(mde);

                }


            }



        }

        dto.setMolecularDataRows(mdeDTO);
        dto.setMolecularDataEntrySize(mdeDTO.size());
        dto.setDataTypes(dataTypes);
        return dto;
        //getModelDetails(dataSource, modelId, 0, 15000, "", "", "");
    }


    /**
     * Creates a table from a selected (molchar)--(massoc)--(marker) object
     *
     * @param id, that is the node id of the selected molchar object
     * @return
     */
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
        tableHeadersSet.add("sampleid");


        //STEP 1: dynamically determine the headers of the table
        for(MarkerAssociation ma: associationList){


            if(ma.getChromosome() != null && !ma.getChromosome().isEmpty()){
                tableHeadersSet.add("chromosome");
            }

            if(ma.getSeqPosition() != null && !ma.getSeqPosition().isEmpty()){
                tableHeadersSet.add("seqposition");
            }

            if(ma.getRefAllele() != null && !ma.getRefAllele().isEmpty()){
                tableHeadersSet.add("refallele");
            }

            if(ma.getAltAllele() != null && !ma.getAltAllele().isEmpty()){
                tableHeadersSet.add("altallele");
            }

            if(ma.getConsequence() != null && !ma.getConsequence().isEmpty()){
                tableHeadersSet.add("consequence");
            }

            if(ma.getMarker() != null){
                tableHeadersSet.add("hgncsymbol");
            }

            if(ma.getAminoAcidChange() != null && !ma.getAminoAcidChange().isEmpty()){
                tableHeadersSet.add("aminoacidchange");
            }

            if(ma.getReadDepth() != null && !ma.getReadDepth().isEmpty()){
                tableHeadersSet.add("readdepth");
            }

            if(ma.getAlleleFrequency() != null && !ma.getAlleleFrequency().isEmpty()){
                tableHeadersSet.add("allelefrequency");
            }


            if(ma.getRsIdVariants() != null && !ma.getRsIdVariants().isEmpty()){
                tableHeadersSet.add("rsidvariants");
            }

            if(ma.getNucleotideChange() != null && !ma.getNucleotideChange().isEmpty()){
                tableHeadersSet.add("nucleotidechange");
            }

            if(ma.getGenomeAssembly() != null && !ma.getGenomeAssembly().isEmpty()){
                tableHeadersSet.add("genomeassembly");
            }

            if(ma.getSeqStartPosition() != null && !ma.getSeqStartPosition().isEmpty()){
                tableHeadersSet.add("seqstartposition");
            }

            if(ma.getSeqEndPosition() != null && !ma.getSeqEndPosition().isEmpty()){
                tableHeadersSet.add("seqendposition");
            }

            if(ma.getStrand() != null && !ma.getStrand().isEmpty()){
                tableHeadersSet.add("strand");
            }

            if(ma.getEnsemblTranscriptId() != null && !ma.getEnsemblTranscriptId().isEmpty()){
                tableHeadersSet.add("ensembltranscriptid");
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
                tableHeadersSet.add("cytogeneticsresult");
            }

            if(ma.getMicrosatelliteResult() != null && !ma.getMicrosatelliteResult().isEmpty()){
                tableHeadersSet.add("microsateliteresult");
            }

            if(ma.getProbeIDAffymetrix() != null && !ma.getProbeIDAffymetrix().isEmpty()){
                tableHeadersSet.add("probeidaffymetrix");
            }

            if(ma.getCnaLog2RCNA() != null && !ma.getCnaLog2RCNA().isEmpty()){
                tableHeadersSet.add("cnalog2rcna");
            }

            if(ma.getCnaLog10RCNA() != null && !ma.getCnaLog10RCNA().isEmpty()){
                tableHeadersSet.add("cnalog10rcna");
            }

            if(ma.getCnaCopyNumberStatus() != null && !ma.getCnaCopyNumberStatus().isEmpty()){
                tableHeadersSet.add("cnacopynumberstatus");
            }

            if(ma.getCnaGisticValue() != null && !ma.getCnaGisticValue().isEmpty()){
                tableHeadersSet.add("cnagisticvalue");
            }

            if(ma.getCnaPicnicValue() != null && !ma.getCnaPicnicValue().isEmpty()){
                tableHeadersSet.add("cnapicnicvalue");
            }

        }


        //STEP 2: Determine table headers order
        // DON'T CHANGE THE ORDER OF THESE CONDITIONS OR THE WORLD WILL TREMBLE!
        // (But if you REALLY need to change the order, don't forget to change it at step 3, too!!!)



        if(tableHeadersSet.contains("sampleid")){
            tableHeaders.add("Sample Id");
        }

        if(tableHeadersSet.contains("hgncsymbol")){
            tableHeaders.add("HGNC Symbol");
        }

        if(tableHeadersSet.contains("aminoacidchange")){
            tableHeaders.add("Amino Acid Change");
        }

        if(tableHeadersSet.contains("consequence")){
            tableHeaders.add("Consequence");
        }

        if(tableHeadersSet.contains("nucleotidechange")){
            tableHeaders.add("Nucleotide Change");
        }

        if(tableHeadersSet.contains("readdepth")){
            tableHeaders.add("Read Depth");
        }

        if(tableHeadersSet.contains("allelefrequency")){
            tableHeaders.add("Allele Frequency");
        }

        if(tableHeadersSet.contains("probeidaffymetrix")){
            tableHeaders.add("Probe Id Affymetrix");
        }

        if(tableHeadersSet.contains("cnalog10rcna")){
            tableHeaders.add("Log10 Rcna");
        }

        if(tableHeadersSet.contains("cnalog2rcna")){
            tableHeaders.add("Log2 Rcna");
        }

        if(tableHeadersSet.contains("cnacopynumberstatus")){
            tableHeaders.add("Copy Number Status");
        }

        if(tableHeadersSet.contains("cnagisticvalue")){
            tableHeaders.add("Gistic Value");
        }

        if(tableHeadersSet.contains("chromosome")){
            tableHeaders.add("Chromosome");
        }

        if(tableHeadersSet.contains("seqstartposition")){
            tableHeaders.add("Seq. Start Position");
        }

        if(tableHeadersSet.contains("seqendposition")){
            tableHeaders.add("Seq. End Position");
        }

        if(tableHeadersSet.contains("refallele")){
            tableHeaders.add("Ref. Allele");
        }

        if(tableHeadersSet.contains("altallele")){
            tableHeaders.add("Alt Allele");
        }

        if(tableHeadersSet.contains("rsidvariants")){
            tableHeaders.add("Rs Id Variant");
        }

        if(tableHeadersSet.contains("ensembltranscriptid")){
            tableHeaders.add("Ensembl Transcript Id");
        }

        if(tableHeadersSet.contains("ensemblgeneid")){
            tableHeaders.add("Ensembl Gene Id");
        }

        if(tableHeadersSet.contains("ucscgeneid")){
            tableHeaders.add("Ucsc Gene Id");
        }

        if(tableHeadersSet.contains("ncbigeneid")){
            tableHeaders.add("Ncbi Gene Id");
        }

        if(tableHeadersSet.contains("genomeassembly")){
            tableHeaders.add("Genome Assembly");
        }

        if(tableHeadersSet.contains("cytogeneticsresult")){
            tableHeaders.add("Result");
        }



        //STEP 3: Insert the rows of the table
        // DON'T CHANGE THE ORDER OF THESE CONDITIONS OR THE WORLD WILL TREMBLE!
        // (But if you REALLY need to change the order, don't forget to change it at step 2, too!!!)
        for(MarkerAssociation ma: associationList){

            List<String> row = new ArrayList<>();


            if(tableHeadersSet.contains("sampleid")){
                row.add(sampleId);
            }

            if(tableHeadersSet.contains("hgncsymbol")){
                row.add(ma.getMarker().getHgncSymbol());
            }

            if(tableHeadersSet.contains("aminoacidchange")){
                row.add(ma.getAminoAcidChange() == null ? "" : ma.getAminoAcidChange());
            }

            if(tableHeadersSet.contains("consequence")){
                row.add((ma.getConsequence() == null ? "": ma.getConsequence()));
            }

            if(tableHeadersSet.contains("nucleotidechange")){
                row.add((ma.getNucleotideChange() == null ? "":ma.getNucleotideChange()));
            }

            if(tableHeadersSet.contains("readdepth")){
                row.add((ma.getReadDepth() == null ? "":ma.getReadDepth()));
            }

            if(tableHeadersSet.contains("allelefrequency")){
                row.add((ma.getAlleleFrequency() == null? "":ma.getAlleleFrequency()));
            }

            if(tableHeadersSet.contains("probeidaffymetrix")){
                row.add((ma.getProbeIDAffymetrix() == null ? "":ma.getProbeIDAffymetrix()));
            }

            if(tableHeadersSet.contains("cnalog10rcna")){
                row.add((ma.getCnaLog10RCNA() == null ? "":ma.getCnaLog10RCNA()));
            }

            if(tableHeadersSet.contains("cnalog2rcna")){
                row.add((ma.getCnaLog2RCNA() == null ? "":ma.getCnaLog2RCNA()));
            }

            if(tableHeadersSet.contains("cnacopynumberstatus")){
                row.add((ma.getCnaCopyNumberStatus() == null ? "":ma.getCnaCopyNumberStatus()));
            }

            if(tableHeadersSet.contains("cnagisticvalue")){
                row.add((ma.getCnaGisticValue() == null ? "" : ma.getCnaGisticValue()));
            }

            if(tableHeadersSet.contains("chromosome")){
                row.add((ma.getChromosome() == null ? "" : ma.getChromosome()));
            }

            if(tableHeadersSet.contains("seqstartposition")){
                row.add((ma.getSeqStartPosition() == null ? "" : ma.getSeqStartPosition()));
            }

            if(tableHeadersSet.contains("seqendposition")){
                row.add((ma.getSeqEndPosition() == null ? "" : ma.getSeqEndPosition()));
            }

            if(tableHeadersSet.contains("refallele")){
                row.add((ma.getRefAllele() == null ? "" : ma.getRefAllele()));
            }

            if(tableHeadersSet.contains("altallele")){
                row.add((ma.getAltAllele() == null ? "" : ma.getAltAllele()));
            }

            if(tableHeadersSet.contains("rsidvariants")){
                row.add((ma.getRsIdVariants() == null ? "" : ma.getRsIdVariants()));
            }

            if(tableHeadersSet.contains("ensembltranscriptid")){
                row.add((ma.getEnsemblTranscriptId() == null ? "" : ma.getEnsemblTranscriptId()));
            }

            if(tableHeadersSet.contains("ensemblgeneid")){
                row.add((ma.getMarker().getEnsemblGeneId() == null ? "": ma.getMarker().getEnsemblGeneId() ));
            }

            if(tableHeadersSet.contains("ucscgeneid")){
                row.add((ma.getMarker().getUcscGeneId() == null ? "" : ma.getMarker().getUcscGeneId()));
            }

            if(tableHeadersSet.contains("ncbigeneid")){
                row.add((ma.getMarker().getNcbiGeneId() == null ? "" : ma.getMarker().getNcbiGeneId()));
            }

            if(tableHeadersSet.contains("genomeassembly")){
                row.add((ma.getGenomeAssembly() == null ? "" : ma.getGenomeAssembly()));
            }

            if(tableHeadersSet.contains("cytogeneticsresult")){
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
        String passage = hostStrainMap.get(hostStrain).equals("Not Specified") ? "" : "(" + hostStrainMap.get(hostStrain) + ")";
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

        String result = (incoming == null) ? "Not Specified" : incoming;

        result = result.equals("null") ? "Not Specified" : result;

        result = result.length() == 0 ? "Not Specified" : result;

        result = StringUtils.isEmpty(incoming) ? "Not Specified" : result;

        result = result.equals("Unknown") ? "Not Specified" : result;

        return result;
    }


}
