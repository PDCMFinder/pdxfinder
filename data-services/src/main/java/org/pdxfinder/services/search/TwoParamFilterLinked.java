package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class TwoParamFilterLinked extends GeneralFilter{

    private Map<String, List<String>> options;

    private Map<String, List<String>> selected;


    public TwoParamFilterLinked(String name) {
        super(name);
    }
}
