package org.pdxfinder.preload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class OmicHarmonizer {

    enum OMIC {
        MUT,
        CNA
    }

    private OMIC omicType;
    private ArrayList<ArrayList<String>> omicSheet;
    private ArrayList<ArrayList<String>> outputSheet;
    private int chromosomeColumn;
    private int seqStartPositionCol = -1;
    private int seqEndPositionCol = -1;
    private int genomeAssemblyCol = -1;

    private static final String CHROMOSOME = "chromosome";
    private static final String SEQSTARTPOS = "seq_start_position";
    private static final String SEQENDPOS = "seq_end_position";
    private static final String GENOMEASSEMBLY = "genome_assembly";
    private static final String ERRORSTR = "ERROR LIFTING";

    private PDXLiftOver lifter = new PDXLiftOver();

    Logger log = LoggerFactory.getLogger(OmicHarmonizer.class);

    OmicHarmonizer(String chain) {
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
        seqStartPositionCol = getColumnByHeader(SEQSTARTPOS);

        genomeAssemblyCol = getColumnByHeader(GENOMEASSEMBLY);
        if (omicType.equals(OMIC.CNA)) seqEndPositionCol = getColumnByHeader(SEQENDPOS);
    }

    private void findLastHeaderColumn(){

        int endCol = getHeaders().size();

        for(int i = 0; i < endCol; i++) {
            if (getHeaders().get(i).trim().equals("")) endCol = i;
        }
    }

    private void iterateThruLiftOver() {

        for (ArrayList<String> row : omicSheet)

            if ((!row.isEmpty()) && isHg37(row)) {

                Map<String, long[]> liftedData = lifter.liftOverCoordinates(getRowsGenomicCoor(row));
                if ((liftedData.isEmpty()||liftedData.containsKey(ERRORSTR)||liftedData.containsValue(-1))) {
                    logLiftError(row);

                } else {
                    harmonizeData(liftedData, row);
                }
            }
    }

    private void logLiftError(ArrayList<String> row) {
        String errorMSG = String.format("Genomic coordinates not lifted. Chro %s start %s %n", row.get(chromosomeColumn), row.get(seqStartPositionCol));
        log.warn(errorMSG);
    }

    private void harmonizeData(Map<String,long[]> liftedData, ArrayList<String> row){
        mergeLiftDataWithRowData(liftedData, row);
        updateAssembly(row);
        outputSheet.add(row);
    }

    private Map<String, long[]> getRowsGenomicCoor(ArrayList<String> row){

        String rowChromosome = row.get(chromosomeColumn);
        long rowStartPos = getAndValidateNum(row, seqStartPositionCol);
        long rowEndPos = getSeqEndPosition(row);

         Map<String, long[]> genomCoors = new LinkedHashMap<>();
         genomCoors.put(rowChromosome, new long[] { rowStartPos, rowEndPos});

         return genomCoors;
    }

    private long getAndValidateNum(ArrayList<String> row, int colNum){
        return Long.parseLong(validateNumStr(row.get(colNum)));
    }

    private String validateNumStr(String numStr){
        return numStr.trim().equals("") ? "-1" : numStr;
    }

    private long getSeqEndPosition(ArrayList<String> row) {

        long endPos = -1;
        if(omicType.equals(OMIC.CNA)) endPos = getAndValidateNum(row, seqEndPositionCol);
        else if(omicType.equals(OMIC.MUT)) endPos = getAndValidateNum(row, seqStartPositionCol);
        return endPos;
    }

    private void setSeqEndPos(ArrayList<String> row, String endPos){
        if (omicType.equals(OMIC.CNA)) row.set(seqEndPositionCol, endPos);
    }

    private void mergeLiftDataWithRowData(Map<String,long[]> liftedData, ArrayList<String>row) {

        Set<Map.Entry<String,long[]>> set = liftedData.entrySet();
        ArrayList<Map.Entry<String,long[]>> list = new ArrayList<>(set);

        for(Map.Entry<String,long[]> entry : list ){

            row.set(chromosomeColumn,entry.getKey());
            row.set(seqStartPositionCol,String.valueOf(entry.getValue()[0]));

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
        else
            return errorFlag;

    }

    protected ArrayList<String> getHeaders(){
        return omicSheet.get(0);
    }

    protected boolean headersAreNotMissing(){
        return (chromosomeColumn != -1 && genomeAssemblyCol != -1 &&  seqStartPositionCol != -1);
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
