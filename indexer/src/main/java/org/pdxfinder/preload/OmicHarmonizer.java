package org.pdxfinder.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OmicHarmonizer {

    private String omicType = "undefined";
    private ArrayList<ArrayList<String>> omicSheet;
    private ArrayList<ArrayList<String>> outputSheet;
    private int chromosomeColumn;
    private int seq_start_position = -1;
    private int seq_end_position = -1;
    private int genomeAssemblyCol = -1;

    private static final int MUT_COLUMN_SIZE = 23;
    private static final int CNA_COLUMN_SIZE = 20;

    private PDXLiftOver lifter = new PDXLiftOver();

    Logger log = LoggerFactory.getLogger(OmicHarmonizer.class);

    protected ArrayList<ArrayList<String>> runLiftOver(String chainFile){

        outputSheet = new ArrayList<>();

        lifter.setChainFileURI(chainFile);

        determineDataType();
        chromosomeColumn = getColumnByHeader("chromosome");
        seq_start_position = getColumnByHeader("seq_start_position");
        genomeAssemblyCol = getColumnByHeader("genome_assembly");
        if (omicType.equals("CNA")) seq_end_position = getColumnByHeader("seq_end_position");

        iterateThruLiftOver();
        return outputSheet;
    }

    private void iterateThruLiftOver() {

        omicSheet.stream()
                .skip(1)
                .forEach(s -> {

                    if(isHg37(s)){
                        Map<String,int[]> liftedData = lifter.liftOverCoordinates(getRowsGenomicCoor(s));
                        if(liftedData.isEmpty()) log.warn("Genomic coordinates not lifted. Chro" + s.get(0) + "start" + s.get(1) + "end" + s.get(2));
                        mergeLiftDataWithRowData(liftedData,s);
                        outputSheet.add(s);
                    }
                });
    }

     private Map<String, int[]> getRowsGenomicCoor(ArrayList<String> row){

        String rowChromosome = row.get(chromosomeColumn);
        int rowStartPos = Integer.parseInt(row.get(seq_start_position));
        int rowEndPos = getSeqEndPosition(row);

        return new LinkedHashMap<String, int[]>() {{
            put(rowChromosome,
                new int[] { rowStartPos, rowEndPos});
        }};
    }

    private int getSeqEndPosition(ArrayList<String> row) {
        return ((Integer.parseInt(row.get(seq_start_position))) + 1);
    }

    private void mergeLiftDataWithRowData(Map<String,int[]> liftedData, ArrayList<String>row) {

        Set<Map.Entry<String,int[]>> set = liftedData.entrySet();
        ArrayList<Map.Entry<String,int[]>> list = new ArrayList<>(set);

        for(Map.Entry<String,int[]> entry : list ){

            row.set(chromosomeColumn,entry.getKey());
            row.set
                    (seq_start_position,String.valueOf(entry.getValue()[0]));
            row.set
                    (seq_end_position,String.valueOf(entry.getValue()[1]));
        }
    }

    protected boolean isHg37(ArrayList<String> row){

        if(genomeAssemblyCol == -1)  log.warn("Genome assembly column was not found");

        return row.get(genomeAssemblyCol).trim().matches("(?i)(37|19|Hg19|GRC37)");
    }

    protected void determineDataType() {

        int size = omicSheet.get(0).size();

        if (size == MUT_COLUMN_SIZE) omicType = "MUT";
        else if (size == CNA_COLUMN_SIZE) omicType = "CNA";
    }

    protected int getColumnByHeader(String header) {

        ArrayList<String> headers = getHeaders();
        Iterator<String> iterator = headers.iterator();
        int errorFlag = -1;

        boolean foundMatch = false;
        int index = -1;

        do {
            index++;
            foundMatch = iterator.next().equalsIgnoreCase(header);

        }while(iterator.hasNext() && ! foundMatch);

        if(foundMatch)
        return index;
        else return errorFlag;

    }

    protected ArrayList<String> getHeaders(){
        return omicSheet.get(0);
    }

    public void setOmicSheet(ArrayList<ArrayList<String>> omicSheet) {
        this.omicSheet = omicSheet;
    }

    public String getOmicType() {
        return omicType;
    }

}
