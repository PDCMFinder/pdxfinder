package org.pdxfinder.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class OmicHarmonizer {

    enum OMIC {
        MUT,
        CNA
    };

    private OMIC omicType;
    private ArrayList<ArrayList<String>> omicSheet;
    private ArrayList<ArrayList<String>> outputSheet;
    private int chromosomeColumn;
    private int seq_start_positionCol = -1;
    private int seq_end_positionCol = -1;
    private int genomeAssemblyCol = -1;
    private int endCol = -1;

    private static final String CHROMOSOME = "chromosome";
    private static final String SEQ_START_POS = "seq_start_position";
    private static final String SEQ_END_POS = "seq_end_position";
    private static final String GENOME_ASSEMBLY = "genome_assembly";
    private final static String errorStr = "ERROR LIFTING";

    private PDXLiftOver lifter = new PDXLiftOver();

    Logger log = LoggerFactory.getLogger(OmicHarmonizer.class);

    OmicHarmonizer(String chain) throws IOException {
        lifter.setChainFileURI(chain);
    }

    protected ArrayList<ArrayList<String>> runLiftOver(ArrayList<ArrayList<String>> sheet, OMIC dataType) throws IOException {

        setOmicType(dataType);
        omicSheet = sheet;

        outputSheet = new ArrayList<>();
        initHeaders();

        if(headersAreNotMissing()) {
            outputSheet.add(getHeaders());
            iterateThruLiftOver();
        }
        else throw new IOException("Headers are not found on file" );
        return outputSheet;
    }

    private void initHeaders() {
        findLastHeaderColumn();
        chromosomeColumn = getColumnByHeader(CHROMOSOME);
        seq_start_positionCol = getColumnByHeader(SEQ_START_POS);

        genomeAssemblyCol = getColumnByHeader(GENOME_ASSEMBLY);
        if (omicType.equals(OMIC.CNA)) seq_end_positionCol = getColumnByHeader(SEQ_END_POS);
    }

    private void findLastHeaderColumn(){

        endCol = getHeaders().size();

        for(int i = 0; i < endCol; i++) {
            if (getHeaders().get(i).trim().equals("")) endCol = i;
        }
    }

    private void iterateThruLiftOver() {

        for (ArrayList<String> row : omicSheet)

            if ((!row.isEmpty()) && isHg37(row)) {

                //System.out.println("Lifting over row index: " + omicSheet.indexOf(row));
                Map<String, long[]> liftedData = lifter.liftOverCoordinates(getRowsGenomicCoor(row));
                if ((liftedData.isEmpty()||liftedData.containsKey(errorStr)||liftedData.containsValue(-1))) {
                    logLiftError(row);

                } else {
                    harmonizeData(liftedData, row);
                }
            }
    }

    private void logLiftError(ArrayList<String> row) {
        log.warn("Genomic coordinates not lifted. Chro " + row.get(chromosomeColumn) + " start " + row.get(seq_start_positionCol) + "\n");
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
        return NumStr.trim().equals("") ? "-1" : NumStr;
    }

    private long getSeqEndPosition(ArrayList<String> row) {

        long endPos = -1;
        if(omicType.equals(OMIC.CNA)) endPos = getAndValidateNum(row, seq_end_positionCol);
        else if(omicType.equals(OMIC.MUT)) endPos = getAndValidateNum(row, seq_start_positionCol);
        return endPos;
    }

    private void setSeqEndPos(ArrayList<String> row, String endPos){
        if (omicType.equals(OMIC.CNA)) row.set(seq_end_positionCol, endPos);
    }

    private void mergeLiftDataWithRowData(Map<String,long[]> liftedData, ArrayList<String>row) {

        Set<Map.Entry<String,long[]>> set = liftedData.entrySet();
        ArrayList<Map.Entry<String,long[]>> list = new ArrayList<>(set);

        for(Map.Entry<String,long[]> entry : list ){

            row.set(chromosomeColumn,entry.getKey());
            row.set(seq_start_positionCol,String.valueOf(entry.getValue()[0]));

            setSeqEndPos(row, String.valueOf(entry.getValue()[1]));
        }
    }

    protected boolean isHg37(ArrayList<String> row){
        return row.get(genomeAssemblyCol).trim().matches("(?i)(37|19|Hg19|GRC37)");
    }

    protected void updateAssembly(ArrayList<String> row){
        row.set(genomeAssemblyCol, "Hg38");
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

    public void setOmicType(OMIC omicType){
        this.omicType = omicType;
    }

    public OMIC getOmicType() {
        return omicType;
    }

}
