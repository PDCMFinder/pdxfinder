package org.pdxfinder.preloader;

import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.preload.PDXLiftOver;

import java.util.LinkedHashMap;
import java.util.Map;

public class LiftOverTests extends BaseTest {

    @Test(expected = NullPointerException.class)
    public void Given_noChainFileGiven_When_liftOverIsCalled_throwRuntimeException(){

        PDXLiftOver pdxliftover = new PDXLiftOver();

        Map<String, int[]> genomeCoordinates = new LinkedHashMap<String, int[]>()
            {{
                put("chr1", new int[]{10000, 10000});
            }};
        pdxliftover.liftOverGenomeCoordinates(genomeCoordinates);
    }
}
