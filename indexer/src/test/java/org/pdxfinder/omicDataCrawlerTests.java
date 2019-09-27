package org.pdxfinder;

import org.junit.*;

import org.pdxfinder.preload.OmicDataCrawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;



public class omicDataCrawlerTests {

    private String finderRootDir;

    private Path tmpRootDir;
    private Path provider1;
    private Path provider2;
    private Path mut;
    private Path metadata;
    private Path cyto;
    private Path mut2;
    private Path metadata2;
    private Path cyto2;

    @Before
    public void buildTestStructure() throws IOException {

        tmpRootDir = Files.createTempDirectory("TEST");

        provider1 = Files.createDirectory(Paths.get(tmpRootDir.toString() + "/provider1"));
        provider2 = Files.createDirectory(Paths.get(tmpRootDir.toString() + "/provider2"));

        mut = Files.createFile(Paths.get(provider1.toString() + "/mut"));
        metadata = Files.createFile(Paths.get(provider1.toString() + "/metadata"));
        cyto = Files.createFile(Paths.get(provider1.toString() + "/cyto"));

        mut2 = Files.createFile(Paths.get(provider2.toString() + "/mut"));
        metadata2 = Files.createFile(Paths.get(provider2.toString() + "/metadata"));
        cyto2 = Files.createFile(Paths.get(provider2.toString() + "/cyto"));


        tmpRootDir.toFile().deleteOnExit();
    }

    @Test
    public void Given_nonExistantRootFolder_When_crawlerIsCalled_returnDoNothingAndLogMessage(){

        //when
        initCrawlersAndPassRootFile(new File(""));
    }


    @Test
    public void Given_MultipleProvidersWithMutCytoAndMeta_When_crawlerIsCalled_returnListOfOnlyMut(){

        //given
        //init()

        //when
        List<File> actualFiles = initCrawlersAndPassRootFile(tmpRootDir.toFile());

        //then
        Assert.assertTrue(actualFiles.size() == 2);
        Assert.assertEquals(actualFiles.get(0).getName(),mut.toFile().getName());
        Assert.assertEquals(actualFiles.get(1).getName(),mut2.toFile().getName());

    }

    private List<File> initCrawlersAndPassRootFile(File rootDir){

        OmicDataCrawler crawler = new OmicDataCrawler();
        return crawler.searchFileTreeForOmicData(rootDir);
    }
}
