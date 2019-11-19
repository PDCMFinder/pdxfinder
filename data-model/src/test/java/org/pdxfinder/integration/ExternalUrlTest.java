package org.pdxfinder.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pdxfinder.BaseTest;
import org.pdxfinder.graph.dao.ExternalUrl;


public class ExternalUrlTest extends BaseTest {

    ExternalUrl url;
    ExternalUrl url2;

    @Before
    public void init(){

        url = new ExternalUrl(ExternalUrl.Type.CONTACT, "abc@def.gh");

        url2 = new ExternalUrl();
        url2.setType(ExternalUrl.Type.SOURCE.getValue());
        url2.setUrl("http://abc.com");


    }


    @Test
    public void Given_ExternalUrlIsConstructed_When_ValuesAreChecked_Then_ReturnsCorrectTypeAndVal(){


        Assert.assertEquals("contact", url.getType());
        Assert.assertEquals("http://abc.com", url2.getUrl());

    }


}
