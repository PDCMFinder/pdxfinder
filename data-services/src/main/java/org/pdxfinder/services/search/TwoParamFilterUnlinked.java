package org.pdxfinder.services.search;

import java.util.List;
import java.util.Map;

/*
 * Created by csaba on 19/11/2018.
 */
public class TwoParamFilterUnlinked extends GeneralFilter{

    private List<String> param1;
    private List<String> param2;

    private Map<String, List<String>> selected;


    public TwoParamFilterUnlinked(String name) {
        super(name);
    }
}
