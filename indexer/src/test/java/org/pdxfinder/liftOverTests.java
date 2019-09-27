package org.pdxfinder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pdxfinder.preload.PDXLiftOver;

import java.util.LinkedHashMap;
import java.util.Map;

public class liftOverTests extends BaseTest {

    private PDXLiftOver pdxliftover = new PDXLiftOver();

    @Test(expected = NullPointerException.class)
    public void Given_noChainFileGiven_When_liftOverIsCalled_throwRuntimeException(){

        Map<String, int[]> genomeCoordinates = new LinkedHashMap<String, int[]>()
            {{
                put("chr1", new int[]{10000, 10000});
            }};
        pdxliftover.liftOverGenomeCoordinates(genomeCoordinates);
    }
}
