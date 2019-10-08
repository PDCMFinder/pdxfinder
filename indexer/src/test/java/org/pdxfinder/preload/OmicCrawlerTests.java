package org.pdxfinder.preload;

import org.junit.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class OmicCrawlerTests {

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

        mut2 = Files.createFile(Paths.get(mutFolder2.toString() + "/data.xlsx"));
        metadata2 = Files.createFile(Paths.get(provider2.toString() + "/metadata.xlsx"));
        cyto2 = Files.createFile(Paths.get(cytoFolder2.toString() + "/data.xlsx"));
        cna1 = Files.createFile(Paths.get(cnaFolder2.toString() + "/data.xlsx"));
    }

    @After
    public void deleteFiles() {

        mut2.toFile().delete();
        metadata2.toFile().delete();
        cyto2.toFile().delete();
        cna1.toFile().delete();
        mutFolder.toFile().delete();
        cnaFolder.toFile().delete();
        cytoFolder.toFile().delete();
        mutFolder2.toFile().delete();
        cnaFolder2.toFile().delete();
        cytoFolder2.toFile().delete();
        tmpRootDir.toFile().delete();
        provider1.toFile().delete();
        provider2.toFile().delete();
    }

    @Test(expected = IOException.class)
    public void Given_nonExistentRootFolder_When_crawlerIsCalled_throwIOError() throws IOException {

        //when
        initCrawlersAndPassRootFile(new File(""));
    }

    @Test
    public void Given_productionFileSchema_When_crawlerIsCalled_returnListOfOnlyMutAndCNA() throws IOException {

        //given
        //init()
        mut = Files.createFile(Paths.get(mutFolder.toString() + "/data.xlsx"));
        metadata = Files.createFile(Paths.get(provider1.toString() + "/metadata.xlsx"));
        cyto = Files.createFile(Paths.get(cytoFolder.toString() + "/data.xlsx"));
        cna = Files.createFile(Paths.get(cnaFolder.toString() + "/data.xlsx"));


        //when
        List<File> actualFiles = initCrawlersAndPassRootFile(tmpRootDir.toFile());

        //then
        Assert.assertEquals(4, actualFiles.size());
        Assert.assertTrue(actualFiles.contains(mut.toFile()));
        Assert.assertTrue(actualFiles.contains(mut2.toFile()));
        Assert.assertTrue(actualFiles.contains(cna.toFile()));
        Assert.assertTrue(actualFiles.contains(cna1.toFile()));
    }

    @Test
    public void Given_invalidfileName_When_crawlerIsCalled_returnOnlyValidNameMutAndCNA() throws IOException {

        //given
        mut = Files.createFile(Paths.get(mutFolder.toString() + "/TESTdata.xlsx"));
        metadata = Files.createFile(Paths.get(provider1.toString() + "/metadata.xlsx"));
        cyto = Files.createFile(Paths.get(cytoFolder.toString() + "/datahello.xlsx"));
        cna = Files.createFile(Paths.get(cnaFolder.toString() + "/'~data.xlsx'"));

        //when
        List<File> actualFiles = initCrawlersAndPassRootFile(tmpRootDir.toFile());

        //then
        Assert.assertEquals(2, actualFiles.size());
        Assert.assertFalse(actualFiles.contains(mut.toFile()));
        Assert.assertTrue(actualFiles.contains(mut2.toFile()));
        Assert.assertFalse(actualFiles.contains(cna.toFile()));
        Assert.assertTrue(actualFiles.contains(cna1.toFile()));
    }

    private List<File> initCrawlersAndPassRootFile(File rootDir) throws IOException {

        OmicCrawler crawler = new OmicCrawler();
        return crawler.run(rootDir);
    }
}
