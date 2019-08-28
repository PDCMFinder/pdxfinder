package org.pdxfinder.graph.dao;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.pdxfinder.BaseTestWithPersistence;
import org.pdxfinder.graph.repositories.DataProjectionRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DataProjectionsTimeTest extends BaseTestWithPersistence {

    private static final String LABEL = "TEST";

    @Autowired
    private DataProjectionRepository dataProjectionRepository;

    @Ignore
    @Test
    public void Given_DataProjection_When_CascadeCreateSaveAndFinds_Then_PrintTimes(){

        String hundredBytes = generateRandomString(2);
        String tenKiB = generateRandomString(4);
        String hundredKib = generateRandomString(5);
        String oneMib = generateRandomString(6);
        String tenMiB = generateRandomString(7);

        //to init db. Else the first time test is off by a second.
        timeCreateSaveAndFindAndReturnElapsedTime(hundredBytes);

        List<Long> elapsedTime = timeCreateSaveAndFindAndReturnElapsedTime(tenKiB);
        List<Long> elapsedTime1 = timeCreateSaveAndFindAndReturnElapsedTime(hundredKib);
        List<Long> elapsedTime2 = timeCreateSaveAndFindAndReturnElapsedTime(oneMib);
        List<Long> elapsedTime3 = timeCreateSaveAndFindAndReturnElapsedTime(tenMiB);


        String timeTable = String.format(" Data Size: %s Time %s %s %s Average %s \n", tenKiB.length(), elapsedTime.get(0), elapsedTime.get(1), elapsedTime.get(2), elapsedTime.get(3)) +
                String.format(" Data Size: %s Time %s %s %s Average %s \n", hundredKib.length(), elapsedTime1.get(0), elapsedTime1.get(1), elapsedTime1.get(2), elapsedTime1.get(3)) +
                String.format(" Data Size: %s Time %s %s %s  Average %s \n", oneMib.length(), elapsedTime2.get(0), elapsedTime2.get(1), elapsedTime2.get(2), elapsedTime2.get(3)) +
                String.format(" Data Size: %s Time %s %s %s Average %s \n", tenMiB.length(), elapsedTime3.get(0), elapsedTime3.get(1), elapsedTime3.get(2), elapsedTime3.get(3));
        System.out.println(timeTable);
    }


    private String generateRandomString(int exponent){

        int byteSize = (int) Math.pow(10, exponent);
        byte[] generatedByte = new byte[byteSize];
        new Random().nextBytes(generatedByte);
        return Arrays.toString(generatedByte);
    }

    private List<Long> timeCreateSaveAndFindAndReturnElapsedTime(String randomString){

        List<Long> time = new ArrayList<>();
        long average;
        long start;
        long end;

        DataProjection returnProjection;

        for(int i = 0; i < 3; i++) {

            start = System.currentTimeMillis();

            DataProjection dataProjection = new DataProjection();
            dataProjection.setLabel(LABEL);
            dataProjection.setValue(randomString);

            dataProjectionRepository.save(dataProjection);
            returnProjection = dataProjectionRepository.findByLabel(LABEL);
            Assert.assertEquals(LABEL, returnProjection.getLabel());

            end = System.currentTimeMillis();

            dataProjectionRepository.deleteAll();
            time.add(end - start);
        }
        average = ((time.get(0)+time.get(1)+time.get(2))/3);
        time.add(average);
        return time;
    }
}