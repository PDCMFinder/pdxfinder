package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.pdxfinder.dataloaders.updog.TSV;
import org.pdxfinder.graph.dao.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class GroupCreator {

    private static final Logger log = LoggerFactory.getLogger(GroupCreator.class);

    Set<Group> createProviderGroup(Map<String, Table> tableSet) {
        Row loaderData = tableSet.get("metadata-loader.tsv").row(0);
        String providerName = loaderData.getText(TSV.Metadata.name.name());
        String abbreviation = loaderData.getText(TSV.Metadata.abbreviation.name());
        String internalUrl = loaderData.getText(TSV.Metadata.internal_url.name());

        Row sharingData = tableSet.get("metadata-sharing.tsv").row(0);
        String contact = sharingData.getText(TSV.Metadata.email.name());
        String providerType = sharingData.getText(TSV.Metadata.provider_type.name());
        log.debug("Setting [{}] attributes using the first row from the metadata-sharing table: " +
            "contact: {}, provider type: {}",
            providerName, contact, providerType);

        Group providerGroup = Group.createProviderGroup(
            providerName,
            abbreviation,
            "",
            providerType,
            contact,
            internalUrl);
        providerGroup.setType("Provider");

        return new HashSet<>(Collections.singletonList(providerGroup));
    }

}
