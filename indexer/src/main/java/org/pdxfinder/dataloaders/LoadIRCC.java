package org.pdxfinder.dataloaders;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.pdxfinder.graph.dao.*;
import org.pdxfinder.services.DataImportService;
import org.pdxfinder.services.UtilityService;
import org.pdxfinder.services.ds.Standardizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@PropertySource("classpath:loader.properties")
@ConfigurationProperties(prefix = "ircc")
public class LoadIRCC extends LoaderBase {
    private static final Logger log = LoggerFactory.getLogger(LoadIRCC.class);

    // samples -> markerAsssociations
    private HashMap<String, HashSet<MarkerAssociation>> markerAssociations = new HashMap();
    private HashMap<String, HashMap<String, String>> specimenSamples = new HashMap();
    private HashSet<Integer> loadedModelHashes = new HashSet<>();

    @Value("${data-dir}")
    private String finderRootDir;

    @Value("${irccpdx.variation.max}")
    private int variationMax;

    public LoadIRCC(UtilityService utilityService, DataImportService dataImportService) {
        super(utilityService, dataImportService);
    }

    public void run() throws Exception {

            initMethod();
            irccAlgorithm();
    }


    public void irccAlgorithm() throws Exception {

        step00StartReportManager();
        step02GetMetaDataJSON();
        if (skipThis) return;
        step03CreateProviderGroup();
        step04CreateNSGammaHostStrain();
        step06SetProjectGroup();
        step07GetPDXModels();

        for (int i = 0; i < jsonArray.length(); i++) {
            this.jsonData = jsonArray.getJSONObject(i);

            if (loadedModelHashes.contains(jsonData.toString().hashCode())) continue;
            loadedModelHashes.add(jsonData.toString().hashCode());

            step08GetMetaData();
            step09LoadPatientData();
            step10LoadExternalURLs();
            step12CreateModels();
            step13LoadSpecimens();
            step17LoadModelDosingStudies();
            step16LoadVariationData();
            step18SetAdditionalGroups();
        }
    }

    @Override
    protected void initMethod() {
        log.info("Loading IRCC PDX data.");
        finderRootDir = UniversalLoader.stripTrailingSlash(finderRootDir);
        jsonFile = finderRootDir + "/data/" + dataSourceAbbreviation+"/pdx/models.json";

        dataSource = dataSourceAbbreviation;
        filesDirectory = "";

        platformURL = new HashMap<>();
        platformURL.put("whole exome sequencing_mutation","/platform/whole-exome-sequencing/");
        platformURL.put("TargetedNGS_MUT_mutation","/platform/ircc-gene-panel/");
        platformURL.put("Targeted Next Generation Sequencing_copy number alteration","/platform/ircc-gene-panel/");

    }

    @Override
    protected void step01GetMetaDataFolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void step05CreateNSHostStrain() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void step10LoadExternalURLs() {
        loadExternalURLs(dataSourceContact,Standardizer.NOT_SPECIFIED);
        dataImportService.saveSample(dto.getPatientSample());
        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
    }

    @Override
    protected void step11LoadBreastMarkers() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void step13LoadSpecimens()throws Exception {

        dto.getModelCreation().addGroup(providerDS);

        JSONArray specimens = dto.getSpecimens();

        for (int i = 0; i < specimens.length(); i++) {
            JSONObject specimenJSON = specimens.getJSONObject(i);

            String specimenId = specimenJSON.getString("Specimen ID");
            Specimen specimen = dataImportService.getSpecimen(
                dto.getModelCreation(),
                specimenId,
                providerDS.getAbbreviation(),
                specimenJSON.getString("Passage"));
            specimen.setHostStrain(nsgBS);

            EngraftmentSite is = dataImportService.getImplantationSite(specimenJSON.getString("Engraftment Site"));
            specimen.setEngraftmentSite(is);
            EngraftmentType it = dataImportService.getImplantationType("Heterotopic");
            specimen.setEngraftmentType(it);

            EngraftmentMaterial em = dataImportService.getEngraftmentMaterial(specimenJSON.getString("Engraftment Type"));
            specimen.setEngraftmentMaterial(em);

            Sample specSample = new Sample();
            specSample.setSourceSampleId(specimenId);
            specSample.setDataSource(providerDS.getAbbreviation());

            specimen.setSample(specSample);

            dto.getModelCreation().addSpecimen(specimen);
            dto.getModelCreation().addRelatedSample(specSample);

        }
    }

    @Override
    protected void step14LoadPatientTreatments() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void step15LoadImmunoHistoChemistry() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void step16LoadVariationData() {
        log.info(String.format("Loading WGS for model %s", dto.getModelCreation().getSourcePdxId()));
        loadOmicData(dto.getModelCreation(), providerDS,"mutation", finderRootDir+"/data/"+dataSource);
        loadOmicData(dto.getModelCreation(), providerDS,"copy number alteration", finderRootDir+"/data/"+dataSource);
    }

    @Override
    void step17LoadModelDosingStudies() {
        TreatmentSummary ts;
        if (dto.getModelDosingStudies().length() > 0) {
            ts = new TreatmentSummary();
            ts.setUrl(dosingStudyURL);

            for (int t = 0; t < dto.getModelDosingStudies().length(); t++) {
                try {
                    JSONObject treatmentObject = dto.getModelDosingStudies().getJSONObject(t);
                    TreatmentProtocol treatmentProtocol = dataImportService.getTreatmentProtocol(treatmentObject.getString("Drug"),
                            treatmentObject.getString("Dose"),
                            treatmentObject.getString("Response Class"), "");

                    if (treatmentProtocol != null) {
                        ts.addTreatmentProtocol(treatmentProtocol);
                    }
                } catch (JSONException e) {
                    log.error(e.toString());
                }
                ts.setModelCreation(dto.getModelCreation());
                dto.getModelCreation().setTreatmentSummary(ts);
            }
        }

        dataImportService.saveModelCreation(dto.getModelCreation());

        dataImportService.savePatient(dto.getPatient());
        dataImportService.savePatientSnapshot(dto.getPatientSnapshot());
        dataImportService.saveModelCreation(dto.getModelCreation());

    }

    @Override
    protected void step18SetAdditionalGroups() {
        Group access = dataImportService.getAccessibilityGroup(
            validateAccessibility(""),
            validateModality("transnational access")
        );

        Group project = dataImportService.getProjectGroup("EurOPDX");

        dto.getModelCreation().addGroup(access);
        dto.getModelCreation().addGroup(project);
        dataImportService.saveModelCreation(dto.getModelCreation());
    }

    @Transactional
    public void loadVariantsBySpecimen() {
        try {
            String variationURLStr = finderRootDir +dataSourceAbbreviation+"/mut/data.json";
            JSONObject job = new JSONObject(utilityService.parseFile(variationURLStr));
            JSONArray jarray = job.getJSONArray("IRCCVariation");

            Platform platform = dataImportService.getPlatform(tech, "mutation", providerDS, platformURL.get("targetedNgsPlatformURL"));
            platform.setGroup(providerDS);
            dataImportService.savePlatform(platform);

            for (int i = 0; i < jarray.length(); i++) {
                if (i == variationMax) {
                    log.error(String.format("Quitting after loading %s maximum variants", i));
                    break;
                }

                JSONObject variation = jarray.getJSONObject(i);
                String sample = variation.getString("Sample ID");
                String specimen = variation.getString("Specimen ID");

                if(specimenSamples.containsKey(specimen)){
                    specimenSamples.get(specimen).put(sample, sample);
                }else{
                    HashMap<String,String> samples = new HashMap();
                    samples.put(sample,sample);
                    specimenSamples.put(specimen,samples);
                }

                String gene = variation.getString("Gene");
                String type = variation.getString("Type");

                Marker marker = dataImportService.getMarker(gene,gene);
                MarkerAssociation ma = new MarkerAssociation();
                ma.setMarker(marker);
                ma.setType(type);
                ma.setCdsChange(variation.getString("CDS"));
                ma.setChromosome(variation.getString("Chrom"));
                ma.setConsequence(variation.getString("Effect"));
                ma.setSeqPosition(variation.getString("Pos"));
                ma.setRefAllele(variation.getString("Ref"));
                ma.setAltAllele(variation.getString("Alt"));
                ma.setAminoAcidChange(variation.getString("Protein"));
                ma.setAlleleFrequency(variation.getString("VAF"));
                ma.setRsIdVariants(variation.getString("avsnp147"));

                PlatformAssociation pa = dataImportService.createPlatformAssociation(platform, marker);
                dataImportService.savePlatformAssociation(pa);

                if (markerAssociations.containsKey(sample)) {
                    markerAssociations.get(sample).add(ma);
                } else {
                    HashSet<MarkerAssociation> mas = new HashSet();
                    mas.add(ma);
                    markerAssociations.put(sample, mas);
                }

            }

        } catch (Exception e) {
            log.error("Unable to load variants");
        }

    }

}
