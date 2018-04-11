/*******************************************************************************
 * Copyright 2015 EMBL - European Bioinformatics Institute
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *******************************************************************************/

package org.pdxfinder.web.controllers;

import org.pdxfinder.services.ds.ModelForQuery;
import org.pdxfinder.services.ds.SearchDS;
import org.pdxfinder.web.controllers.sitemap.XmlUrl;
import org.pdxfinder.web.controllers.sitemap.XmlUrlSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Set;

/**
 * This controller creates sitemap files conforming to http://www.sitemap.org
 */
@Controller
public class SiteMapController {

    private SearchDS searchDS;

    public SiteMapController(SearchDS searchDS) {
        this.searchDS = searchDS;
    }


    /**
     * Generate sitemap XML file
     *
     * @return an XML object containing a gene sitemap
     */
    @RequestMapping(value = "/sitemap.xml", method = RequestMethod.GET, produces = "application/xml; charset=utf-8")
    @ResponseBody
    public String createSitemap() {

        Set<ModelForQuery> results = searchDS.getModels();

        String base = "https://www.pdxfinder.org";
        XmlUrlSet xmlUrlSet = new XmlUrlSet();

        // Add search page to the sitemap
        create(xmlUrlSet, base + "/data/search", XmlUrl.Priority.MEDIUM);

        // Generate the links.
        for (ModelForQuery result : results) {
            String target = base + String.format("/data/pdx/%s/%s", result.getDatasource(), result.getExternalId());
            create(xmlUrlSet, target, XmlUrl.Priority.MEDIUM);
        }

        return getSitemapFile(xmlUrlSet);
    }


    // PRIVATE METHODS

    private void create(XmlUrlSet xmlUrlSet, String link, XmlUrl.Priority priority) {
        xmlUrlSet.addUrl(new XmlUrl(link, priority));
    }

    private String getSitemapFile(Object xmlObject) {

        StringWriter writer = new StringWriter(4096);

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(xmlObject.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            jaxbMarshaller.marshal(xmlObject, writer);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + writer.toString();
    }

}
