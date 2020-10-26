package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.pdxfinder.TSV;
import org.pdxfinder.graph.dao.Group;
import org.pdxfinder.graph.dao.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class PatientCreator {

    private GroupCreator groupCreator;
    private static final Logger log = LoggerFactory.getLogger(PatientCreator.class);

    public PatientCreator(
        GroupCreator groupCreator
    ) {
        this.groupCreator = groupCreator;
    }

    Group createDependencies(Map<String, Table> tableSet) {
        Set<Group> providerGroups = groupCreator.createProviderGroup(tableSet);
        return new ArrayList<>(providerGroups).get(0);
    }

    Set<Patient> create(Map<String, Table> tableSet, Group providerGroup) {
        Set<Patient> patients = new HashSet<>();
        Table patientTable = tableSet.get("metadata-patient.tsv");

        for (Row row : patientTable) {
            String patientId = row.getText(TSV.Metadata.patient_id.name());
            Patient p = new Patient(patientId, providerGroup);
            p.setSex(row.getText(TSV.Metadata.sex.name()));
            p.setEthnicity(row.getText(TSV.Metadata.ethnicity.name()));
            p.setCancerRelevantHistory(row.getText(TSV.Metadata.history.name()));
            p.setFirstDiagnosis(row.getText(TSV.Metadata.initial_diagnosis.name()));
            p.setAgeAtFirstDiagnosis(row.getText(TSV.Metadata.age_at_initial_diagnosis.name()));
            patients.add(p);
        }

        return patients;
    }
}
