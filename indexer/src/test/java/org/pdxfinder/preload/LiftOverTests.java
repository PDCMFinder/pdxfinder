package org.pdxfinder.preload;

import org.junit.Test;
import org.pdxfinder.BaseTest;

import java.util.LinkedHashMap;
import java.util.Map;

public class LiftOverTests extends BaseTest {

    @Test(expected = NullPointerException.class)
    public void Given_noChainFileGiven_When_liftOverIsCalled_throwRuntimeException(){

        PDXLiftOver pdxliftover = new PDXLiftOver();

        Map<String, long[]> genomeCoordinates = new LinkedHashMap<String, long[]>()
            {{
                put("chr1", new long[]{10000, 10000});
            }};
        pdxliftover.liftOverCoordinates(genomeCoordinates);
    }
}
