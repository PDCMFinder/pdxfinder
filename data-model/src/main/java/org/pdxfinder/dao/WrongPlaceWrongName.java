/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pdxfinder.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.Date;
import org.pdxfinder.repositories.ExternalDataSourceRepository;
import org.pdxfinder.repositories.TumorTypeRepository;
import org.pdxfinder.repositories.BackgroundStrainRepository;
import org.pdxfinder.repositories.ImplantationSiteRepository;
import org.pdxfinder.repositories.ImplantationTypeRepository;
import org.pdxfinder.repositories.PatientRepository;
import org.pdxfinder.repositories.PdxStrainRepository;
import org.pdxfinder.repositories.TissueRepository;
import org.pdxfinder.repositories.TumorRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  // these three types of thing happen a lot // there should be a naming
 * convention to indicate what we are doing
 *
 * // find or create a node // find, find a synonym, or create a node
 *
 * // delete and create (update) a node
 *
 * @author sbn
 */
public class WrongPlaceWrongName {

    @Autowired
    private TumorTypeRepository tumorTypeRepository;

    @Autowired
    private BackgroundStrainRepository backgroundStrainRepository;

    @Autowired
    private ImplantationTypeRepository implantationTypeRepository;

    @Autowired
    private ImplantationSiteRepository implantationSiteRepository;

    @Autowired
    private ExternalDataSourceRepository externalDataSourceRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PdxStrainRepository pdxStrainRepository;

    @Autowired
    private TissueRepository tissueRepository;

    @Autowired
    private TumorRepository tumorRepository;

    private final static Logger log = LoggerFactory.getLogger(WrongPlaceWrongName.class);

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

    public PdxStrain createPDXStrain(String pdxId, ImplantationSite implantationSite, ImplantationType implantationType, Tumor tumor, BackgroundStrain backgroundStrain, String passage) {

        PdxStrain pdxStrain = pdxStrainRepository.findBySourcePdxId(pdxId);
        if (pdxStrain != null) {
            log.info("Deleting existing PdxStrain " + pdxId);
            pdxStrainRepository.delete(pdxStrain);
        }
        pdxStrain = new PdxStrain(pdxId, implantationSite, implantationType, tumor, backgroundStrain, passage);
        pdxStrainRepository.save(pdxStrain);
        return pdxStrain;
    }

    public PdxStrain createPDXStrain(String pdxId, String implantationSiteStr, String implantationTypeStr, Tumor tumor, BackgroundStrain backgroundStrain, String passage) {

        ImplantationSite implantationSite = this.getImplantationSite(implantationSiteStr);
        ImplantationType implantationType = this.getImplantationType(implantationTypeStr);
        PdxStrain pdxStrain = pdxStrainRepository.findBySourcePdxId(pdxId);
        if (pdxStrain != null) {
            log.info("Deleting existing PdxStrain " + pdxId);
            pdxStrainRepository.delete(pdxStrain);
        }
        pdxStrain = new PdxStrain(pdxId, implantationSite, implantationType, tumor, backgroundStrain, passage);
        pdxStrainRepository.save(pdxStrain);
        return pdxStrain;
    }

    public Tumor getTumor(String id, TumorType tumorType, String diagnosis, Tissue originSite, Tissue primarySite, String classification, ExternalDataSource externalDataSource) {

        Tumor tumor = tumorRepository.findBySourceTumorId(id);
        if (tumor != null) {
            log.info("Deleting existing tumor " + id);
            tumorRepository.delete(tumor);
        }
        tumor = new Tumor(id, tumorType, diagnosis, originSite, primarySite, classification, externalDataSource);
        tumorRepository.save(tumor);
        return tumor;
    }

    public Tumor getTumor(String id, String tumorTypeStr, String diagnosis, String originSiteStr, String primarySiteStr, String classification, ExternalDataSource externalDataSource) {

        TumorType tumorType = this.getTumorType(tumorTypeStr);
        Tissue originSite = this.getTissue(originSiteStr);
        Tissue primarySite = this.getTissue(primarySiteStr);

        Tumor tumor = tumorRepository.findBySourceTumorId(id);
        if (tumor != null) {
            log.info("Deleting existing tumor " + id);
            tumorRepository.delete(tumor);
        }
        tumor = new Tumor(id, tumorType, diagnosis, originSite, primarySite, classification, externalDataSource);
        tumorRepository.save(tumor);
        return tumor;
    }

    public Patient getPatient(String externalId, String sex, String age, String race, String ethnicity, ExternalDataSource externalDataSource) {
        Patient patient = patientRepository.findByExternalId(externalId);
        if (patient == null) {
            log.info("Patient '{}' not found. Creating", externalId);
            patient = new Patient(externalId, sex, age, race, ethnicity, externalDataSource);
            patientRepository.save(patient);
        }

        return patient;
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
            log.info("NSG Mouse '{}' not found. Creating", name);
            bgStrain = new BackgroundStrain(symbol, name, description, url);
            backgroundStrainRepository.save(bgStrain);
        }
        return bgStrain;
    }
}
