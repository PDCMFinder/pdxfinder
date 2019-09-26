package org.pdxfinder;

import org.junit.*;

import org.pdxfinder.preload.omicDataCrawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;



public class omicDataCrawlerTests extends BaseTest{

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

        Path tmpRootDir = Files.createTempDirectory("TEST");

        Path provider1 = Files.createDirectory(Paths.get(tmpRootDir.toString() + "/provider1"));
        Path provider2 = Files.createDirectory(Paths.get(tmpRootDir.toString() + "/provider2"));

        Path mut = Files.createFile(Paths.get(provider1.toString() + "/mut"));
        Path metadata = Files.createFile(Paths.get(provider1.toString() + "/metadata"));
        Path cyto = Files.createFile(Paths.get(provider1.toString() + "/cyto"));

        Path mut2 = Files.createFile(Paths.get(provider2.toString() + "/mut"));
        Path metadata2 = Files.createFile(Paths.get(provider2.toString() + "/metadata"));
        Path cyto2 = Files.createFile(Paths.get(provider2.toString() + "/cyto"));


        tmpRootDir.toFile().deleteOnExit();
    }

    @Test
    public void Given_nonExistantRootFolder_When_crawlerIsCalled_returnNullFolder(){

        omicDataCrawler crawler = new omicDataCrawler();
        crawler.searchFileTreeForOmicData(new File(""));
    }

    @Test
    public void Given_MultipleProvidersWithMutCytoAndMeta_When_crawlerIsCalled_returnListOfOnlyMut(){

        //given
        //init()

        //when
        List<File> actualFiles = initCrawlersAndPassRootFile();

        //then
        Assert.assertTrue(actualFiles.size() == 2);
        Assert.assertEquals(actualFiles.get(0),mut.toFile());
        Assert.assertEquals(actualFiles.get(1),mut.toFile());
    }

    private List<File> initCrawlersAndPassRootFile(){

        omicDataCrawler crawler = new omicDataCrawler();
        return crawler.searchFileTreeForOmicData(tmpRootDir.toFile());
    }
}
