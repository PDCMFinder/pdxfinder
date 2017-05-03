/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import org.pdxfinder.dao.BackgroundStrain;
import org.pdxfinder.dao.ExternalDataSource;
import org.pdxfinder.dao.ImplantationSite;
import org.pdxfinder.dao.ImplantationType;
import org.pdxfinder.dao.Marker;
import org.pdxfinder.dao.MarkerAssociation;
import org.pdxfinder.dao.MolecularCharacterization;
import org.pdxfinder.dao.Patient;
import org.pdxfinder.dao.PatientSnapshot;
import org.pdxfinder.dao.PdxStrain;
import org.pdxfinder.dao.Sample;
import org.pdxfinder.dao.Tissue;
import org.pdxfinder.dao.TumorType;
import org.pdxfinder.repositories.ExternalDataSourceRepository;
import org.pdxfinder.repositories.TumorTypeRepository;
import org.pdxfinder.repositories.BackgroundStrainRepository;
import org.pdxfinder.repositories.ImplantationSiteRepository;
import org.pdxfinder.repositories.ImplantationTypeRepository;
import org.pdxfinder.repositories.MarkerAssociationRepository;
import org.pdxfinder.repositories.MarkerRepository;
import org.pdxfinder.repositories.MolecularCharacterizationRepository;
import org.pdxfinder.repositories.PatientRepository;
import org.pdxfinder.repositories.PatientSnapshotRepository;
import org.pdxfinder.repositories.PdxStrainRepository;
import org.pdxfinder.repositories.SampleRepository;
import org.pdxfinder.repositories.TissueRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * The hope was to put a lot of reused repository actions into one place ie find
 * or create a node or create a node with that requires a number of 'child'
 * nodes that are terms
 *
 * @author sbn
 */
@Component
public class LoaderUtils {

    private TumorTypeRepository tumorTypeRepository;
    private BackgroundStrainRepository backgroundStrainRepository;
    private ImplantationTypeRepository implantationTypeRepository;
    private ImplantationSiteRepository implantationSiteRepository;
    private ExternalDataSourceRepository externalDataSourceRepository;
    private PatientRepository patientRepository;
    private PdxStrainRepository pdxStrainRepository;
    private TissueRepository tissueRepository;
    private PatientSnapshotRepository patientSnapshotRepository;
    private SampleRepository sampleRepository;
    private MarkerRepository markerRepository;
    private MarkerAssociationRepository markerAssociationRepository;
    private MolecularCharacterizationRepository molecularCharacterizationRepository;

    private final static Logger log = LoggerFactory.getLogger(LoaderUtils.class);

    public LoaderUtils(TumorTypeRepository tumorTypeRepository,
            BackgroundStrainRepository backgroundStrainRepository,
            ImplantationTypeRepository implantationTypeRepository,
            ImplantationSiteRepository implantationSiteRepository,
            ExternalDataSourceRepository externalDataSourceRepository,
            PatientRepository patientRepository,
            PdxStrainRepository pdxStrainRepository,
            TissueRepository tissueRepository,
            PatientSnapshotRepository patientSnapshotRepository,
            SampleRepository sampleRepository,
            MarkerRepository markerRepository,
            MarkerAssociationRepository markerAssociationRepository,
            MolecularCharacterizationRepository molecularCharacterizationRepository) {

        Assert.notNull(tumorTypeRepository);
        Assert.notNull(backgroundStrainRepository);
        Assert.notNull(implantationTypeRepository);
        Assert.notNull(implantationSiteRepository);
        Assert.notNull(externalDataSourceRepository);
        Assert.notNull(patientRepository);
        Assert.notNull(pdxStrainRepository);
        Assert.notNull(tissueRepository);
        Assert.notNull(patientSnapshotRepository);
        Assert.notNull(sampleRepository);
        Assert.notNull(markerRepository);
        Assert.notNull(markerAssociationRepository);
        Assert.notNull(molecularCharacterizationRepository);

        this.tumorTypeRepository = tumorTypeRepository;
        this.backgroundStrainRepository = backgroundStrainRepository;
        this.implantationTypeRepository = implantationTypeRepository;
        this.implantationSiteRepository = implantationSiteRepository;
        this.externalDataSourceRepository = externalDataSourceRepository;
        this.patientRepository = patientRepository;
        this.pdxStrainRepository = pdxStrainRepository;
        this.tissueRepository = tissueRepository;
        this.patientSnapshotRepository = patientSnapshotRepository;
        this.sampleRepository = sampleRepository;
        this.markerRepository = markerRepository;
        this.markerAssociationRepository = markerAssociationRepository;
        this.molecularCharacterizationRepository = molecularCharacterizationRepository;

    }

    public ExternalDataSource getExternalDataSource(String abbr, String name, String description) {
        ExternalDataSource eDS = externalDataSourceRepository.findByAbbreviation(abbr);
        if (eDS == null) {
            log.info("External data source '{}' not found. Creating", abbr);
            eDS = new ExternalDataSource(
                    name,
                    abbr,
                    description,
                    Date.from(Instant.now()));
            externalDataSourceRepository.save(eDS);
        }

        return eDS;

    }

    public PdxStrain createPDXStrain(String pdxId, ImplantationSite implantationSite, ImplantationType implantationType, Sample sample, BackgroundStrain backgroundStrain) {

        PdxStrain pdxStrain = pdxStrainRepository.findBySourcePdxId(pdxId);
        if (pdxStrain != null) {
            log.info("Deleting existing PdxStrain " + pdxId);
            pdxStrainRepository.delete(pdxStrain);
        }

        pdxStrain = new PdxStrain(pdxId, implantationSite, implantationType, sample, backgroundStrain);
        pdxStrainRepository.save(pdxStrain);
        return pdxStrain;
    }

    public PdxStrain createPDXStrain(String pdxId, String implantationSiteStr, String implantationTypeStr, Sample sample, BackgroundStrain backgroundStrain) {

        ImplantationSite implantationSite = this.getImplantationSite(implantationSiteStr);
        ImplantationType implantationType = this.getImplantationType(implantationTypeStr);
        PdxStrain pdxStrain = pdxStrainRepository.findBySourcePdxId(pdxId);
        if (pdxStrain != null) {
            log.info("Deleting existing PdxStrain " + pdxId);
            pdxStrainRepository.delete(pdxStrain);
        }
        pdxStrain = new PdxStrain(pdxId, implantationSite, implantationType, sample, backgroundStrain);
        pdxStrainRepository.save(pdxStrain);
        return pdxStrain;
    }

    public PatientSnapshot getPatientSnapshot(String externalId, String sex, String race, String ethnicity, String age, ExternalDataSource externalDataSource) {

        Patient patient = patientRepository.findByExternalId(externalId);
        PatientSnapshot patientSnapshot = null;

        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);

            patient = this.getPatient(externalId, sex, race, ethnicity, externalDataSource);

            patientSnapshot = new PatientSnapshot(patient, age);
            patientSnapshotRepository.save(patientSnapshot);

        } else {
            patientSnapshot = this.getPatientSnapshot(patient, age);
        }
        return patientSnapshot;
    }

    public PatientSnapshot getPatientSnapshot(Patient patient, String age) {

        PatientSnapshot patientSnapshot = null;

        Set<PatientSnapshot> pSnaps = patientSnapshotRepository.findByPatient(patient);
        loop:
        for (PatientSnapshot ps : pSnaps) {
            if (ps.getAge().equals(age)) {
                patientSnapshot = ps;
                break loop;
            }
        }
        if (patientSnapshot == null) {
            log.info("PatientSnapshot for patient '{}' at aget '{}' not found. Creating", patient.getExternalId(), age);
            patientSnapshot = new PatientSnapshot(patient, age);
            patientSnapshotRepository.save(patientSnapshot);
        }

        return patientSnapshot;
    }

    public Patient getPatient(String externalId, String sex, String race, String ethnicity, ExternalDataSource externalDataSource) {

        Patient patient = patientRepository.findByExternalId(externalId);

        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);

            patient = new Patient(externalId, sex, race, ethnicity, externalDataSource);

            patientRepository.save(patient);
        }

        return patient;
    }

    public Sample getSample(String sourceSampleId, String typeStr, String diagnosis, String originStr, String sampleSiteStr, String classification, Boolean normalTissue, ExternalDataSource externalDataSource) {

        TumorType type = this.getTumorType(typeStr);
        Tissue origin = this.getTissue(originStr);
        Tissue sampleSite = this.getTissue(sampleSiteStr);
        Sample sample = sampleRepository.findBySourceSampleId(sourceSampleId);
        if (sample == null) {

            sample = new Sample(sourceSampleId, type, diagnosis, origin, sampleSite, classification, normalTissue, externalDataSource);
            sampleRepository.save(sample);
        }

        return sample;
    }

    public ImplantationSite getImplantationSite(String iSite) {
        ImplantationSite site = implantationSiteRepository.findByName(iSite);
        if (site == null) {
            log.info("Implantation Site '{}' not found. Creating.", iSite);
            site = new ImplantationSite(iSite);
            implantationSiteRepository.save(site);
        }

        return site;
    }

    public ImplantationType getImplantationType(String iType) {
        ImplantationType type = implantationTypeRepository.findByName(iType);
        if (type == null) {
            log.info("Implantation Site '{}' not found. Creating.", iType);
            type = new ImplantationType(iType);
            implantationTypeRepository.save(type);
        }

        return type;
    }

    public Tissue getTissue(String t) {
        Tissue tissue = tissueRepository.findByName(t);
        if (tissue == null) {
            log.info("Tissue '{}' not found. Creating.", t);
            tissue = new Tissue(t);
            tissueRepository.save(tissue);
        }

        return tissue;
    }

    public TumorType getTumorType(String name) {
        TumorType tumorType = tumorTypeRepository.findByName(name);
        if (tumorType == null) {
            log.info("TumorType '{}' not found. Creating.", name);
            tumorType = new TumorType(name);
            tumorTypeRepository.save(tumorType);
        }

        return tumorType;
    }

    public BackgroundStrain getBackgroundStrain(String symbol, String name, String description, String url) {
        BackgroundStrain bgStrain = backgroundStrainRepository.findByName(name);
        if (bgStrain == null) {
            log.info("Background Strain '{}' not found. Creating", name);
            bgStrain = new BackgroundStrain(symbol, name, description, url);
            backgroundStrainRepository.save(bgStrain);
        }
        return bgStrain;
    }
    
    // is this bad? ... probably..
    public Marker getMarker(String symbol) {
        return this.getMarker(symbol, symbol);
    }

    public Marker getMarker(String symbol, String name) {

        Marker marker = markerRepository.findByName(name);
        if (marker == null && symbol != null) {
            marker = markerRepository.findBySymbol(symbol);
        }
        if (marker == null) {
            log.info("Marker '{}' not found. Creating", name);
            marker = new Marker(symbol, name);
            marker = markerRepository.save(marker);
        }
        return marker;
    }

    public MarkerAssociation getMarkerAssociation(String type, String markerSymbol, String markerName) {
        Marker m = this.getMarker(markerSymbol, markerName);
        MarkerAssociation ma = markerAssociationRepository.findByTypeAndMarkerName(type, m.getName());
        
        if(ma == null && m.getSymbol() != null){
            ma = markerAssociationRepository.findByTypeAndMarkerSymbol(type, m.getSymbol());
        }
        
        if (ma == null) {
            ma = new MarkerAssociation(type, m);
            markerAssociationRepository.save(ma);
        }
        
        return ma;
    }

    // wow this is misleading it doesn't do anyting!
    public void deleteAllByEDSName(String edsName) throws Exception {
        throw new Exception("Nothing deleted. Method not implemented!");
    }

    public void savePatientSnapshot(PatientSnapshot ps) {
        patientSnapshotRepository.save(ps);
    }

    public void saveMolecularCharacterization(MolecularCharacterization mc) {
        molecularCharacterizationRepository.save(mc);
    }
}
