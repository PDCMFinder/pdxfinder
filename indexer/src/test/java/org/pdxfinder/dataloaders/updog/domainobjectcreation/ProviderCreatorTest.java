package org.pdxfinder.dataloaders.updog.domainobjectcreation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.pdxfinder.graph.dao.Group;
import tech.tablesaw.api.Table;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ProviderCreatorTest {

    private Map<String, Table> testTableSet = DomainObjectCreatorTest.getTestPdxDataTables();
    private String PROVIDER_NAME = testTableSet.get("metadata-loader.tsv").row(0).getText("name");
    private String ABBREVIATION = testTableSet.get("metadata-loader.tsv").row(0).getText("abbreviation");
    private String URL = testTableSet.get("metadata-loader.tsv").row(0).getText("internal_url");

    private String CONTACT = testTableSet.get("metadata-sharing.tsv").row(0).getText("email");
    private String PROVIDER_TYPE = testTableSet.get("metadata-sharing.tsv").row(0).getText("provider_type");

    @InjectMocks private ProviderCreator providerCreator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void create_givenValidTable_createProviderGroup() {
        Map<String, Table> testTableSet = DomainObjectCreatorTest.getTestPdxDataTables();
        Group expected = Group.createProviderGroup(PROVIDER_NAME, ABBREVIATION, "", PROVIDER_TYPE, CONTACT, URL);
        assertThat(providerCreator.create(testTableSet).contains(expected), is(true));
    }

    @Test public void create_givenNoProviderName_createProviderGroupWithEmptyName() {
        Group expected = Group.createProviderGroup("", ABBREVIATION, "", PROVIDER_TYPE, CONTACT, URL);
        Map<String, Table> providerMissingProviderName = DomainObjectCreatorTest.getTestPdxDataTables();
        providerMissingProviderName.get("metadata-loader.tsv").row(0).setText("name", null);
        assertThat(providerCreator.create(providerMissingProviderName).contains(expected), is(true));
    }

}
