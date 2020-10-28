package org.pdxfinder.graph.dao;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

public class MarkerAssociationTest {



    @Test
    public void Given_MarkerAssociation_When_EncodeIsCalled_Then_DataInRightFormat(){

        MarkerAssociation ma = new MarkerAssociation();
        MolecularData md = new MolecularData();
        md.setMarker("KRAS");

        ma.setMolecularDataList(Collections.singletonList(md));

        ma.encodeMolecularData();

        Assert.assertEquals(1, ma.getDataPoints());

    }

}
