package org.pdxfinder.dataloaders;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.preload.pdxLiftOver;

import java.util.LinkedHashMap;
import java.util.Map;

public class liftOverTests extends BaseTest {

    pdxLiftOver pdxliftover = new pdxLiftOver();

    @Before
    public void init() {
    }


    @Test(expected = NullPointerException.class)
    public void Given_noChainFileGiven_When_liftOverIsCalled_throwRuntimeException(){

        Map<String, int[]> genomeCoordinates = new LinkedHashMap<String, int[]>()
            {{
                put("chr1", new int[]{10000, 10000});
            }};
        pdxliftover.liftOverGenomeCoordinates(genomeCoordinates);
    }
}
