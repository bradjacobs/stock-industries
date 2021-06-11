package com.github.bradjacobs.stock.classifications.napcs;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://www.census.gov/naics/napcs
 * https://www.census.gov/eos/www/napcs/structure.html
 */
public class NapcsDataConverter extends BaseDataConverter<NapcsRecord>
{
    private static final int ID_COL_INDEX = 1;
    private static final int TITLE_COL_INDEX = 2;

    // map to determine the depth level based on the lenght of the id.
    private static final Map<Integer,Integer> LENGTH_TO_LEVEL_MAP =  new HashMap<Integer, Integer>() {{
        put( 2, 1);
        put( 3, 2);
        put( 5, 3);
        put( 7, 4);
        put( 9, 5);
        put(11, 6);
    }};

    @Override
    public Classification getClassification()
    {
        return Classification.NAPCS;
    }

    @Override
    public List<NapcsRecord> createDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NEVER).setSkipEmptyRows(true).build();
        String[][] csvData = excelReader.createCsvMatrix(getClassification().getSourceFileLocation());

        List<NapcsRecord> recordList = new ArrayList<>();
        NapcsRecord currentRecord = new NapcsRecord();

        // Note: skipping first (header) row
        for (int i = 1; i < csvData.length; i++)
        {
            String[] row = csvData[i];
            String id = row[ID_COL_INDEX];
            String name = row[TITLE_COL_INDEX];

            int level = LENGTH_TO_LEVEL_MAP.get(id.length());

            if (level == 1) {
                currentRecord.setSectionId(id);
                currentRecord.setSectionName(name);
            }
            else if (level == 2) {
                currentRecord.setSubSectionid(id);
                currentRecord.setSubSectionName(name);
            }
            else if (level == 3) {
                if (!currentRecord.getDivisionId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setDivisionId(id);
                currentRecord.setDivisionName(name);
            }
            else if (level == 4) {
                if (!currentRecord.getGroupId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setGroupId(id);
                currentRecord.setGroupName(name);
            }
            else if (level == 5) {
                if (!currentRecord.getSubGroupId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setSubGroupId(id);
                currentRecord.setSubGroupdName(name);
            }
            else if (level == 6) {
                if (!currentRecord.getTrilateralProductId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setTrilateralProductId(id);
                currentRecord.setTrilateralProductName(name);
            }
        }

        recordList.add(currentRecord);

        return recordList;
    }

}
