package com.github.bradjacobs.stock.classifications.gics;


import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;
import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GicsDataConverter extends BaseDataConverter<GicsRecord>
{
    private static final String SOURCE_FILE = "https://www.msci.com/documents/1296102/11185224/GICS_map+2018.xlsx";

    private static final boolean SKIP_DISCONTINUED_RECORDS = true;
    private static final String DISCONTINUED_IDENTIFIER = "discontinued";


    @Override
    public String getFilePrefix()
    {
        return "gics";
    }

    @Override
    public List<GicsRecord> generateDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NEVER).setSkipEmptyRows(true).build();
        String[][] csvData = excelReader.createCsvMatrix(SOURCE_FILE);
        return generateRecords(csvData);
    }



    public List<GicsRecord> generateRecords(String[][] csvData)
    {
        List<GicsRecord> recordList = new ArrayList<>();
        GicsRecord prevRecord = createBlankRecord();

        boolean discoveredFirstDataRow = false;

        int rowCount = csvData.length;
        for (int i = 0; i < rowCount; i++)
        {
            String[] dataRow = csvData[i];

            if (!discoveredFirstDataRow) {
                String firstColumnValue = dataRow[Column.COL_SECTOR_ID.ordinal()];
                if (StringUtils.isNumeric(firstColumnValue)) {
                    discoveredFirstDataRow = true;
                }
                else {
                    continue;
                }
            }

            // the description is always on the following line.
            String[] descriptionRow = csvData[++i];

            GicsRecord rowRecord = generateRecord(dataRow, descriptionRow, prevRecord);

            if (! shouldSkip(rowRecord))
            {
                recordList.add(rowRecord);
                prevRecord = rowRecord;
            }
        }

        return recordList;
    }


    private boolean shouldSkip(GicsRecord record)
    {
        if (SKIP_DISCONTINUED_RECORDS && isDiscontinued(record)) {
            return true;
        }
        return false;
    }

    private boolean isDiscontinued(GicsRecord record) {
        if (record.getSectorName().contains(DISCONTINUED_IDENTIFIER) ||
            record.getGroupName().contains(DISCONTINUED_IDENTIFIER) ||
            record.getIndustryName().contains(DISCONTINUED_IDENTIFIER) ||
            record.getSubIndustryName().contains(DISCONTINUED_IDENTIFIER)) {
            return true;
        }
        return false;
    }

    private GicsRecord generateRecord(String[] rowData, String[] rowDescription, GicsRecord previousRecord)
    {
        GicsRecord record = new GicsRecord();

        record.setSectorId(getCellValueOrDefault(rowData, Column.COL_SECTOR_ID, previousRecord.getSectorId()));
        record.setSectorName(getCellValueOrDefault(rowData, Column.COL_SECTOR_NAME, previousRecord.getSectorName()));
        record.setGroupId(getCellValueOrDefault(rowData, Column.COL_GROUP_ID, previousRecord.getGroupId()));
        record.setGroupName(getCellValueOrDefault(rowData, Column.COL_GROUP_NAME, previousRecord.getGroupName()));
        record.setIndustryId(getCellValueOrDefault(rowData, Column.COL_INDUSTRY_ID, previousRecord.getIndustryId()));
        record.setIndustryName(getCellValueOrDefault(rowData, Column.COL_INDUSTRY_NAME, previousRecord.getIndustryName()));
        record.setSubIndustryId(getCellValueOrDefault(rowData, Column.COL_SUB_INDUSTRY_ID, previousRecord.getSubIndustryId()));
        record.setSubIndustryName(getCellValueOrDefault(rowData, Column.COL_SUB_INDUSTRY_NAME, previousRecord.getSubIndustryName()));

        record.setDescription(getCellValue(rowDescription, Column.COL_DESCRIPTION));

        return record;
    }

    private String getCellValueOrDefault(String[] rowData, Column column, String defaultValue)
    {
        String cellValue = getCellValue(rowData, column);
        return cellValue.length() > 0 ? cellValue : defaultValue;
    }


    private String getCellValue(String[] rowData, Column column)
    {
        return cleanValue(rowData[column.getColIndex()]);
    }



    private GicsRecord createBlankRecord() {
        return new GicsRecord("","","","","","","","","");
    }


    // NOTE: the header row from the original source data is incomplete,
    //   therefore have a simple enum to represent the column location for each value.
    //   The order of these enums _DOES_ matter.
    private enum Column
    {
        COL_SECTOR_ID, COL_SECTOR_NAME, COL_GROUP_ID, COL_GROUP_NAME, COL_INDUSTRY_ID, COL_INDUSTRY_NAME, COL_SUB_INDUSTRY_ID, COL_SUB_INDUSTRY_NAME, COL_DESCRIPTION;

        private int getColIndex() {
            // description in the original data file is in the same column as the subindustry name
            //   (but happens to be on the next row)
            if (this.equals(COL_DESCRIPTION)) {
                return COL_SUB_INDUSTRY_NAME.getColIndex();
            }
            return this.ordinal();
        }
    }


}
