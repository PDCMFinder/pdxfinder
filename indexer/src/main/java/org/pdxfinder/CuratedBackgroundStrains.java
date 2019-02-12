package org.pdxfinder;

import org.pdxfinder.graph.dao.HostStrain;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Collection of background strains added by curation
 */
@ConfigurationProperties(prefix = "curated-background-strains")
@Configuration
public class CuratedBackgroundStrains {

    private List<HostStrain> hostStrains;


}
