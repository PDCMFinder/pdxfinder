package org.pdxfinder.preload;


import java.util.*;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.Assert;

public class LiftOverDataDrivenTest extends AbstractTestNGSpringContextTests {

    @DataProvider(name = "hg19_0Base_EndExcluded")
    public static Object[][] hg19Tohg38_basicSet(){
        return new Object[][] //Taken from MUT data in PDX Finder - ran through UCSC Liftover tool and
                //validated manually. No base 1 to base 0 conversion performed.
                {
                        {"chr6",32188823,32188823,"6",32221046,32221046},
                        {"chr22",39448275,39448275,"22",39052270,39052270},
                        {"chr2",179441932,179441932,"2",178577205,178577205},
                        {"chr2",202131459,202131459,"2",201266736,201266736},
                        {"chr2",179446381,179446381,"2",178581654,178581654},
                        {"chr12",431586,431586,"12",322420,322420},
                        {"chr7",100678616,100678616,"7",101035335,101035335},
                        {"chr9",5126343,5126343,"9",5126343,5126343},
                        {"chr7",100678820,100678820,"7",101035539,101035539},
                        {"chr7",100680256,100680256,"7",101036975,101036975},
                        {"chr3",142269075,142269075,"3",142550233,142550233},
                        {"chr2",202139648,202139648,"2",201274925,201274925},
                        {"chr7",100680248,100680248,"7",101036967,101036967},
                        {"chr7",100677455,100677455,"7",101034174,101034174},
                        {"chr12",57865321,57865321,"12",57471538,57471538},
                        {"chr7",100677642,100677642,"7",101034361,101034361},
                        {"chr5",149515202,149515202,"5",150135639,150135639},
                        {"chr1",154562788,154562788,"1",154590312,154590312},
                        {"chr7",100678173,100678173,"7",101034892,101034892},
                        {"chr10",47000004,47000004,"10",46549613,46549613},
                        {"chr7",100677405,100677405,"7",101034124,101034124},
                        {"chr6",31864547,31864547,"6",31896770,31896770},
                        {"chr4",106196834,106196834,"4",105275677,105275677},
                        {"chr7",100677645,100677645,"7",101034364,101034364},
                        {"chr12",7045891,7045897,"12",6936728,6936734},
                        {"chr12",112888273,112888273,"12",112450469,112450469},
                        {"chr2",212576777,212576777,"2",211712052,211712052},
                        {"chr7",100678610,100678610,"7",101035329,101035329},
                        {"chr7",100681359,100681359,"7",101038078,101038078},
                        {"chr2",179584914,179584914,"2",178720187,178720187},
                        {"chr7",100678568,100678568,"7",101035287,101035287},
                        {"chr7",100681172,100681172,"7",101037891,101037891},
                        {"chr7",100677378,100677378,"7",101034097,101034097},
                        {"chr5",31515657,31515657,"5",31515550,31515550},
                        {"chr7",100679754,100679754,"7",101036473,101036473},
                        {"chr1",216219781,216219781,"1",216046439,216046439},
                        {"chr4",106197000,106197000,"4",105275843,105275843},
                        {"chr1",246670481,246670481,"1",246507179,246507179},
                        {"chr7",100678740,100678740,"7",101035459,101035459},
                        {"chr9",8341851,8341851,"9",8341851,8341851},
                        {"chr7",151859683,151859683,"7",152162598,152162598},
                        {"chr7",100678481,100678481,"7",101035200,101035200},
                        {"chr6",32188383,32188383,"6",32220606,32220606},
                        {"chr1",154573967,154573967,"1",154601491,154601491},
                        {"chr7",142562051,142562051,"7",142864293,142864293},
                        {"chr4",126412154,126412154,"4",125490999,125490999},
                        {"chr1",226924875,226924875,"1",226737174,226737174},
                        {"chr7",100675376,100675376,"7",101032095,101032095},
                        {"chr1",120458270,120458270,"1",119915647,119915647},
                        {"chr7",100678464,100678464,"7",101035183,101035183},
                        {"chr7",100677501,100677501,"7",101034220,101034220},
                        {"chr7",100686777,100686777,"7",101043496,101043496},
                        {"chr12",121416650,121416650,"12",120978847,120978847},
                        {"chr7",100683050,100683050,"7",101039769,101039769},
                        {"chr2",179582853,179582853,"2",178718126,178718126},
                        {"chr7",100680983,100680983,"7",101037702,101037702},
                        {"chr12",121435427,121435427,"12",120997624,120997624},
                        {"chr2",86683642,86683642,"2",86456519,86456519},
                        {"chr12",7045891,7045894,"12",6936728,6936731},
                        {"chr6",32188640,32188640,"6",32220863,32220863},
                        {"chr2",121747406,121747406,"2",120989830,120989830},
                        {"chr1",44133641,44133641,"1",43667970,43667970},
                        {"chr14",20770036,20770036,"14",20301877,20301877},
                        {"chr7",100677837,100677837,"7",101034556,101034556},
                        {"chr7",100677630,100677630,"7",101034349,101034349},
                        {"chr7",100677714,100677714,"7",101034433,101034433},
                        {"chr2",202131426,202131426,"2",201266703,201266703},
                        {"chr4",106157698,106157698,"4",105236541,105236541},
                        {"chr6",41021108,41021108,"6",41053369,41053369}
        };
    }

    @DataProvider(name = "hg19UnliftableBase0")
    public Object[][] hg19UnliftableBase0(){
        return new Object[][] //pulled from pdx Finder -- formated into base 0 half open, ran through liftover, errors collected
                //and converted back to base 1 fully closed.
                        {
                                {"chr10",49132259,49132259},
                                {"chr20",63694757,63694757},
                                {"chr11",96383280,96383280},
                                {"chr16",88427552,88427552},
                                {"chr21",14152600,14152600},
                                {"chr21",44637505,44637505},
                                {"chr8",142786489,142786489},
                                {"chr21",44637695,44637710},
                                {"chr21",10649583,10649583},
                                {"chr13",112399156,112399156},
                                {"chr21",44666571,44666571},
                                {"chr17",82178542,82178542},
                                {"chr22",16784234,16784234},
                                {"chr10",49131917,49131917},
                                {"chr20",63305944,63305944},
                                {"chr12",132730222,132730222},
                                {"chr17",81647906,81647906},
                                {"chr18",79713127,79713127},
                                {"chr19",8698550,8698550},
                                {"chr12",132730003,132730003},
                                {"chr19",8698023,8698023},
                                {"chr18",78993768,78993768},
                                {"chr5",181059785,181059785},
                                {"chr18",79486406,79486406},
                                {"chr17",82938057,82938057},
                                {"chr20",63563157,63563157},
                                {"chr9",70535957,70535957},
                                {"chr20",63694757,63694757},
                                {"chr1",145872788,145872788},
                                {"chr12",138310,138310},
                                {"chr9",70536002,70536002},
                                {"chr16",88434626,88434626},
                                {"chr21",44666840,44666841},
                                {"chr12",139036,139036}
                };
    }

    @DataProvider(name = "invalidChromosome")
    public Object[][] invalidChromosome(){
        return new Object[][]
                {
                        {"",100677630,100677630},
                        {"GARBAGE",41021108,41021108},
                        {"ch6",32188823,32188823 }

                };
    }


    @DataProvider(name = "alternativeChromosomeFormat")
    public Object[][] alternativeChromosomeFormat(){
        return new Object[][]
                {
                        {"1",100677630,100677630, "1"},
                        {"2",41021108,41021108 ,"2"},
                        {"x", 106157698,106157698, "X"},
                        {"y",15000,15000, "Y"},
                        {"M", 1500,1500, "M"},
                        {"MT", 15000,15000, "M"},
                        {"X", 106157698,106157698, "X"},
                        {"m", 1500,1500, "M"},
                        {"mt", 15000,15000, "M"},

                };
    }



    @DataProvider(name = "invalidLength")
    public Object[][] invalidLength(){
        return new Object[][]
                {
                        {"chr22",99999998, 999999999},
                        {"chr10", 400000, 399999},
                        {"chr2", -1, 1},
                        {"chr3", 0, 1}
                };
    }


    PDXLiftOver pdxliftover = new PDXLiftOver();
    private final static String blank = "";
    private final static int zero = 0;

    @BeforeSuite
    public void init(){
        pdxliftover.setChainFileURI("/home/afollette/IdeaProjects/pdxFinder/indexer/src/main/resources/LiftOverResources/hg19ToHg38.over.chain.gz");
    }

    @Test(dataProvider = "hg19_0Base_EndExcluded", enabled = true)
    public void Given_hg19set_When_liftOverGenomeCoordinatesIsCalled_returnHg38coord(String hg19Chromosome, int hg19start, int hg19end, String hg38Chromosome, int hg38start, int hg38end) {

        //given
        Map<String,int[]> expectedCoordinates = createMapFromCoordinates(hg19Chromosome,hg19start,hg19end);

        //When
        Map<String,int[]> actualCoordinates = pdxliftover.liftOverCoordinates(expectedCoordinates);

        //then
        assertCoordinatesMatch(hg38Chromosome, hg38start,hg38end, actualCoordinates);
    }


    @Test(dataProvider = "hg19UnliftableBase0", enabled = true)
    public void Given_unliftableSet_When_liftOverGenomeCoordinatesIsCalled_returnBlanksAndZeros(String expectedChromosome, int expStart, int expEnd) {

        //given
        Map<String, int[]> expectedCoordinates = createMapFromCoordinates(expectedChromosome, expStart, expEnd);
        //When
        Map<String, int[]> actualCoordinates = pdxliftover.liftOverCoordinates(expectedCoordinates);

        //then
        assertCoordinatesMatch(blank,zero,zero, actualCoordinates);

    }

    @Test(dataProvider = "invalidChromosome", enabled = true)
    public void Given_invalidChromosomeFormat_When_liftOverGenomeCoordinates_returnBlankStringAndZeros(String invalidChromsome, int start, int end) {

        //given
        Map<String, int[]> genomeCoordinates = createMapFromCoordinates(invalidChromsome,start,end);

        //When
        Map<String, int[]> actualCoordinates = pdxliftover.liftOverCoordinates(genomeCoordinates);


        //then
        assertCoordinatesMatch(blank,zero,zero, actualCoordinates);
    }

    @Test(dataProvider = "invalidLength", enabled = true)
    public void Given_invalidLength_When_liftOverGenomeCoordinates_returnBlankStringAndZeros(String chromsome, int invalidStart, int invalidEnd) {

        //given
        Map<String, int[]> genomeCoordinates = createMapFromCoordinates(chromsome,invalidStart,invalidEnd);

        //When
        Map<String, int[]> actualCoordinates = pdxliftover.liftOverCoordinates(genomeCoordinates);

        Map.Entry<String,int[]> actEntry = actualCoordinates.entrySet().iterator().next();

        //then
        assertCoordinatesMatch(blank,zero,zero, actualCoordinates);
    }

    @Test(dataProvider = "alternativeChromosomeFormat", enabled = true)
    public void Given_alternativeChromosomeFormat_When_Lifted_ConvertedToChainFormat(String chromosome, int start, int end, String expectedFormat){

        //given
        Map<String, int[]> genomeCoordinates = createMapFromCoordinates(chromosome,start,end);

        //When
        Map<String, int[]> actualCoordinates = pdxliftover.liftOverCoordinates(genomeCoordinates);

        Map.Entry<String,int[]> actEntry = actualCoordinates.entrySet().iterator().next();

        Assert.assertEquals(actEntry.getKey(),expectedFormat);
    }


    private Map<String,int[]> createMapFromCoordinates(String chromosome, int start, int end) {

        return new LinkedHashMap<String,int[]>()
        {{
            put(chromosome, new int[]{start, end});
        }};
    }

    private void assertCoordinatesMatch(String expectedChromosome, int expectedStart, int expectedEnd, Map<String,int[]> actualCoordinates) {

        Map.Entry<String,int[]> actEntry = actualCoordinates.entrySet().iterator().next();

        Assert.assertEquals(actEntry.getKey(),expectedChromosome);
        Assert.assertEquals(expectedStart, actEntry.getValue()[0]);
        Assert.assertEquals(expectedEnd, actEntry.getValue()[1]);
    }


}


