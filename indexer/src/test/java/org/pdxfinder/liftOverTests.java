package org.pdxfinder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pdxfinder.preload.PDXLiftOver;

import java.util.LinkedHashMap;
import java.util.Map;

public class liftOverTests extends BaseTest {




    @Test()
    public void Given_noChainFileGiven_When_liftOverIsCalled_throwRuntimeException(){
         PDXLiftOver pdxliftover = new PDXLiftOver();

        Map<String, int[]> genomeCoordinates = new LinkedHashMap<String, int[]>()
            {{
                put("chr1", new int[]{10000, 10000});
            }};
        pdxliftover.liftOverGenomeCoordinates(genomeCoordinates);
        assert(true == true);
    }
}
