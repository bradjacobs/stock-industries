package com.github.bradjacobs.stock.classifications.naics;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * NOTE:
 *   these 2 files have code values in different columns:
 *      https://www.census.gov/naics/2017NAICS/2-6%20digit_2017_Codes.xlsx
 *      https://www.census.gov/naics/2017NAICS/2017_NAICS_Descriptions.xlsx
 */
public class NaicsDataConverter extends BaseDataConverter<NaicsRecord>
{
    @Override
    public Classification getClassification()
    {
        return Classification.NAICS;
    }

    @Override
    public List<NaicsRecord> generateDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NEVER).setSkipEmptyRows(true).build();
        String[][] csvData = excelReader.createCsvMatrix(getClassification().getSourceFileLocation());
        return generateRecords(csvData);
    }



    public List<NaicsRecord> generateRecords(String[][] csvData)
    {
        List<NaicsRecord> recordList = new ArrayList<>();

        NaicsRecord currentRecord = null;

        // important note:  code assumes the data input is formatted and sorted
        //   in a very specific way (or it'll blow up)

        // starting at index 1 (skip header row)
        for (int i = 1; i < csvData.length; i++)
        {
            String[] rowData = csvData[i];

            String code = rowData[0];
            String title = rowData[1];
            String description = rowData[2];

            if (code.length() == 5 && StringUtils.isNumeric(code)) {
                continue;  // ignore the 5-digit entries
            }
            if (StringUtils.isEmpty(code) || StringUtils.isEmpty(title)) {
                continue;
            }

            title = cleanValue(title);

            // NOTE: there are some code value 'exceptions' that are actually ranges:
            //   e.g.    31-33  Manufacturing,   44-45  Retail Trade,   etc
            if (code.length() == 2 || code.contains("-")) {
                if (currentRecord != null) {
                    recordList.add(currentRecord);
                }
                currentRecord = new NaicsRecord();
                currentRecord.setSectorId(code);
                currentRecord.setSectorName(title);
            }
            else if (code.length() == 3)
            {
                if (currentRecord.getSubSectorId() != null) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy();
                    currentRecord.setIndustryGroupId(null);
                    currentRecord.setIndustryGroupName(null);
                    currentRecord.setIndustryId(null);
                    currentRecord.setIndustryName(null);
                }

                currentRecord.setSubSectorId(code);
                currentRecord.setSubSectorName(title);
            }
            else if (code.length() == 4)
            {
                if (currentRecord.getIndustryGroupId() != null) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy();
                    currentRecord.setIndustryId(null);
                    currentRecord.setIndustryName(null);
                }

                currentRecord.setIndustryGroupId(code);
                currentRecord.setIndustryGroupName(title);
            }
            else if (code.length() == 6)
            {
                description = cleanValue(description);
                if (currentRecord.getIndustryId() != null) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy();
                }

                currentRecord.setIndustryId(code);
                currentRecord.setIndustryName(title);
                currentRecord.setDescription(description);
            }
            else {
                throw new RuntimeException("Unexpected code value: " + code);
            }
        }

        recordList.add(currentRecord);

        // sanity check
        for (NaicsRecord naicsRecord : recordList)
        {
            if (StringUtils.isEmpty(naicsRecord.getSectorId())) {
                throw new RuntimeException("empty sectorId detected");
            }
            else if (StringUtils.isEmpty(naicsRecord.getSubSectorId())) {
                throw new RuntimeException("empty subsectorId detected");
            }
            else if (StringUtils.isEmpty(naicsRecord.getIndustryGroupId())) {
                throw new RuntimeException("empty industrygroupid detected");
            }
            else if (StringUtils.isEmpty(naicsRecord.getIndustryId())) {
                throw new RuntimeException("empty industryid detected");
            }
        }


        return recordList;
    }
}
