package org.pdxfinder.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOError;
import java.io.IOException;
import java.util.*;

public class OmicHarmonizer {

    private String omicType = "undefined";
    private ArrayList<ArrayList<String>> omicSheet;
    private ArrayList<ArrayList<String>> outputSheet;
    private int chromosomeColumn;
    private int seq_start_positionCol = -1;
    private int seq_end_positionCol = -1;
    private int genomeAssemblyCol = -1;

    private static final int MUT_COLUMN_SIZE = 22;
    private static final int CNA_COLUMN_SIZE = 20;
    private static final String CHROMOSOME = "chromosome";
    private static final String SEQ_START_POS = "seq_start_position";
    private static final String SEQ_END_POS = "seq_end_position";
    private static final String GENOME_ASSEMBLY = "genome_assembly";
    private final static String errorStr = "ERROR LIFTING";

    static int lastEmptyRow;

    private PDXLiftOver lifter = new PDXLiftOver();

    Logger log = LoggerFactory.getLogger(OmicHarmonizer.class);

    protected ArrayList<ArrayList<String>> runLiftOver(String chainFile) throws IOException {

        outputSheet = new ArrayList<>();
        lifter.setChainFileURI(chainFile);
        initHeaders();

        if(headersAreNotMissing()) {
            iterateThruLiftOver();
        }
        else throw new IOException("Headers are not found on file" );
        return outputSheet;
    }

    private void initHeaders() {
        determineDataType();
        chromosomeColumn = getColumnByHeader(CHROMOSOME);
        seq_start_positionCol = getColumnByHeader(SEQ_START_POS);
        genomeAssemblyCol = getColumnByHeader(GENOME_ASSEMBLY);
        if (omicType.equals("CNA")) seq_end_positionCol = getColumnByHeader(SEQ_END_POS);
    }

    private void iterateThruLiftOver() {

        for (ArrayList<String> row : omicSheet)

            if ((!row.isEmpty()) && isHg37(row)) {

                System.out.println("Lifting over row index: " + omicSheet.indexOf(row));
                Map<String, long[]> liftedData = lifter.liftOverCoordinates(getRowsGenomicCoor(row));
                if ((liftedData.isEmpty()||liftedData.containsKey(errorStr))) {
                    logLiftError(row);

                } else {
                    harmonizeData(liftedData, row);
                }
            }
    }

    private void logLiftError(ArrayList<String> row) {
        log.warn("Genomic coordinates not lifted. Chro " + row.get(0) + " start " + row.get(1) + " end " + row.get(2) + "\n");
    }

    private void harmonizeData(Map<String,long[]> liftedData, ArrayList<String> row){
        mergeLiftDataWithRowData(liftedData, row);
        updateAssembly(row);
        outputSheet.add(row);
    }

    private Map<String, long[]> getRowsGenomicCoor(ArrayList<String> row){

        String rowChromosome = row.get(chromosomeColumn);
        long rowStartPos = getAndValidateNum(row, seq_start_positionCol);
        long rowEndPos = getSeqEndPosition(row);

        return new LinkedHashMap<String, long[]>() {{
            put(rowChromosome,
                new long[] { rowStartPos, rowEndPos});
        }};
    }

    private long getAndValidateNum(ArrayList<String> row, int colNum){
        return Long.parseLong(validateNumStr(row.get(colNum)));
    }

    private String validateNumStr(String NumStr){
        return NumStr.trim().matches("") ? "-1" : NumStr;
    }

    private long getSeqEndPosition(ArrayList<String> row) {

        long endPos = -1;
        if(omicType.equals("CNA")) endPos = getAndValidateNum(row, seq_end_positionCol);
        else if(omicType.equals("MUT")) endPos = getAndValidateNum(row, seq_start_positionCol);
        return endPos;
    }

    private void setSeqEndPos(ArrayList<String> row, String endPos){
        if (omicType.equals("CNA")) row.set(seq_end_positionCol, endPos);
    }

    private void mergeLiftDataWithRowData(Map<String,long[]> liftedData, ArrayList<String>row) {

        Set<Map.Entry<String,long[]>> set = liftedData.entrySet();
        ArrayList<Map.Entry<String,long[]>> list = new ArrayList<>(set);

        for(Map.Entry<String,long[]> entry : list ){

            row.set(chromosomeColumn,entry.getKey());
            row.set
                    (seq_start_positionCol,String.valueOf(entry.getValue()[0]));
            setSeqEndPos(row, String.valueOf(entry.getValue()[1]));
        }
    }

    protected boolean isHg37(ArrayList<String> row){
        return row.get(genomeAssemblyCol).trim().matches("(?i)(37|19|Hg19|GRC37)");
    }

    protected void updateAssembly(ArrayList<String> row){
        row.set(genomeAssemblyCol, "Hg38");
    }

    protected void determineDataType() {

        long size = omicSheet.get(0).size();

        if (size == MUT_COLUMN_SIZE) omicType = "MUT";
        else if (size == CNA_COLUMN_SIZE) omicType = "CNA";
        else log.error("Could not determine Omic type for column size :" + size);
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

    protected boolean headersAreNotMissing(){
        return (chromosomeColumn != -1 && genomeAssemblyCol != -1 &&  seq_start_positionCol != -1);
    }

    public void setOmicSheet(ArrayList<ArrayList<String>> omicSheet) {
        this.omicSheet = omicSheet;
    }
    public String getOmicType() {
        return omicType;
    }

}
