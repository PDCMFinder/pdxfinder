package org.pdxfinder.preload;

import org.junit.Ignore;
import org.junit.Test;
import org.pdxfinder.BaseTest;

import java.io.IOException;

public class PreloadRunnerTest extends BaseTest {

    @Test
    public void areYouAlive() throws IOException {

        PreloadRunner runner = new PreloadRunner();
        runner.setFinderRootDir("/home/afollette/Documents/data/UPDOG");
        runner.runLiftOver();
    }

}
