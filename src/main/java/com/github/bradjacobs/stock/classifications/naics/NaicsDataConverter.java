package com.github.bradjacobs.stock.classifications.naics;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * NOTE:
 *   these 2 files have code values in different columns:
 *      https://www.census.gov/naics/2017NAICS/2-6%20digit_2017_Codes.xlsx
 *      https://www.census.gov/naics/2017NAICS/2017_NAICS_Descriptions.xlsx
 */
public class NaicsDataConverter implements DataConverter<NaicsRecord>
{
    private static final int SECTOR_ID_LENGTH = 2;
    private static final int SUB_SECTOR_ID_LENGTH = 3;
    private static final int INDUSTRY_GROUP_ID_LENGTH = 4;
    private static final int INDUSTRY_ID_LENGTH = 6;

    private static final int IGNORABLE_ID_LENGTH = 5; // ignore all codes of this length

    private static final int CODE_COLUMN_INDEX = 0;
    private static final int TITLE_COLUMN_INDEX = 1;
    private static final int DESCRIPTION_COLUMN_INDEX = 2;

    // map to determine the depth level based on the length of the id.
    private static final Map<Integer,Integer> LENGTH_TO_LEVEL_MAP =  new HashMap<Integer, Integer>() {{
        put(SECTOR_ID_LENGTH, 1);
        put(SUB_SECTOR_ID_LENGTH, 2);
        put(INDUSTRY_GROUP_ID_LENGTH, 3);
        put(INDUSTRY_ID_LENGTH, 4);
    }};


    @Override
    public Classification getClassification()
    {
        return Classification.NAICS;
    }

    @Override
    public List<NaicsRecord> createDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NEVER).setSkipEmptyRows(true).build();
        String[][] csvData = excelReader.createCsvMatrix(getClassification().getSourceFileLocation());
        return generateRecords(csvData);
    }


    public List<NaicsRecord> generateRecords(String[][] csvData)
    {
        List<NaicsRecord> recordList = new ArrayList<>();

        NaicsRecord currentRecord = new NaicsRecord();

        // important note:  code assumes the data input is formatted and sorted
        //   in a very specific way (or it'll blow up)

        // NOTE: starting at index 1 (skip header row)
        for (int i = 1; i < csvData.length; i++)
        {
            String[] rowData = csvData[i];

            String code = rowData[CODE_COLUMN_INDEX];
            String title = rowData[TITLE_COLUMN_INDEX];
            String description = rowData[DESCRIPTION_COLUMN_INDEX];

            if (StringUtils.isEmpty(code) || StringUtils.isEmpty(title)) {
                continue;
            }

            int codeLength = code.length();

            if (codeLength == IGNORABLE_ID_LENGTH && StringUtils.isNumeric(code)) {
                continue;
            }

            Integer level = LENGTH_TO_LEVEL_MAP.get(codeLength);

            // NOTE: there are some code value 'exceptions' that are actually ranges for sectors:
            //   e.g.    31-33  Manufacturing,   44-45  Retail Trade,   etc
            if (code.contains("-")) {
                level = LENGTH_TO_LEVEL_MAP.get(SECTOR_ID_LENGTH);
            }

            if (level == null) {
                throw new RuntimeException("Unexpected code id: " + code);
            }

            title = cleanValue(title);

            if (level == 1) {
                if (! currentRecord.getSectorId().isEmpty()) {
                    recordList.add(currentRecord);
                }
                currentRecord = new NaicsRecord();
                currentRecord.setSectorId(code);
                currentRecord.setSectorName(title);
            }
            else if (level == 2)
            {
                if (! currentRecord.getSubSectorId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setSubSectorId(code);
                currentRecord.setSubSectorName(title);
            }
            else if (level == 3)
            {
                if (! currentRecord.getIndustryGroupId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setIndustryGroupId(code);
                currentRecord.setIndustryGroupName(title);
            }
            else if (level == 4)
            {
                description = cleanDescriptionValue(description);  // desc gets special cleanup logic.

                if (! currentRecord.getIndustryId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
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


    protected String cleanValue(String input)
    {
        String cleanedValue = StringUtil.cleanWhitespace(input);

        // remove any trailing capital 'T' (if exists)
        if (cleanedValue.endsWith("T")) {
            cleanedValue = cleanedValue.substring(0, cleanedValue.length() - 1);
        }
        return cleanedValue;
    }

    protected String cleanDescriptionValue(String description) {
        if (StringUtils.isEmpty(description)) {
            return "";
        }

        // the "illustrative examples" are usually more clutter than they are worth,
        //   BUT, may change decision on this at a later time.
        int examplesIndex = description.indexOf("Illustrative Examples");
        if (examplesIndex > 0) {
            description = description.substring(0, examplesIndex);
        }

        // the downloaded file doesn't actually have any information after "Cross-References"
        //  so just remove that key word, if exists
        int crossReferencesIndex = description.indexOf("Cross-References");
        if (crossReferencesIndex > 0) {
            description = description.substring(0, crossReferencesIndex);
        }

        return cleanValue(description);
    }

}
