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
    private Path cna;
    private Path cna1;
    private Path mutFolder;
    private Path cnaFolder;
    private Path cytoFolder;

    private Path mutFolder2;
    private Path cnaFolder2;
    private Path cytoFolder2;


    @Before
    public void buildTestStructure() throws IOException {

        tmpRootDir = Files.createTempDirectory("TEST");

        provider1 = Files.createDirectory(Paths.get(tmpRootDir.toString() + "/provider1"));
        provider2 = Files.createDirectory(Paths.get(tmpRootDir.toString() + "/provider2"));

        mutFolder = Files.createDirectory(Paths.get(provider1.toString() + "/mut"));
        cnaFolder = Files.createDirectory(Paths.get(provider1.toString() + "/cna"));
        cytoFolder = Files.createDirectory(Paths.get(provider1.toString() + "/cyto"));

        mutFolder2 = Files.createDirectory(Paths.get(provider2.toString() + "/mut"));
        cnaFolder2 = Files.createDirectory(Paths.get(provider2.toString() + "/cna"));
        cytoFolder2 = Files.createDirectory(Paths.get(provider2.toString() + "/cyto"));

        mut = Files.createFile(Paths.get(mutFolder.toString() + "/mut"));
        metadata = Files.createFile(Paths.get(provider1.toString() + "/metadata"));
        cyto = Files.createFile(Paths.get(cytoFolder.toString() + "/cyto"));
        cna = Files.createFile(Paths.get(cnaFolder.toString() + "/cna"));

        mut2 = Files.createFile(Paths.get(mutFolder2.toString() + "/mut"));
        metadata2 = Files.createFile(Paths.get(provider2.toString() + "/metadata"));
        cyto2 = Files.createFile(Paths.get(cytoFolder2.toString() + "/cyto"));
        cna1 = Files.createFile(Paths.get(cnaFolder2.toString() + "/cna"));
    }

    @After
    public void deleteFiles(){
        tmpRootDir.toFile().delete();
    }

    @Test
    public void Given_nonExistantRootFolder_When_crawlerIsCalled_returnDoNothingAndLogMessage(){

        //when
        initCrawlersAndPassRootFile(new File(""));
    }


    @Test
    public void Given_productionFileSchema_When_crawlerIsCalled_returnListOfOnlyMutAndCNA(){

        //given
        //init()

        //when
        List<File> actualFiles = initCrawlersAndPassRootFile(tmpRootDir.toFile());

        //then
        Assert.assertEquals(4, actualFiles.size());
        Assert.assertTrue(actualFiles.contains(mut.toFile()));
        Assert.assertTrue(actualFiles.contains(mut2.toFile()));
        Assert.assertTrue(actualFiles.contains(cna.toFile()));
        Assert.assertTrue(actualFiles.contains(cna1.toFile()));
    }

    private List<File> initCrawlersAndPassRootFile(File rootDir){

        OmicDataCrawler crawler = new OmicDataCrawler();
        return crawler.searchFileTreeForOmicData(rootDir);
    }
}
