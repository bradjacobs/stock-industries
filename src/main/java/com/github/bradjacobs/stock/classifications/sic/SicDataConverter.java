package com.github.bradjacobs.stock.classifications.sic;


//  todo - document the similarities/difference of data from these links.
//    https://www.bls.gov/oes/special.requests/oessic87.pdf
// alternate found at:
// https://www.osha.gov/data/sic-manual
// https://www.sec.gov/info/edgar/siccodes.htm
// https://www.gov.uk/government/publications/standard-industrial-classification-of-economic-activities-sic
// http://www.ehso.com/siccodes.php
// https://www.state.nj.us/dep/aqm/es/sic.pdf
//
// https://www.dietrich-direct.com/SIC-Code-Reference-Access.htm

import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.util.DownloadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SicDataConverter extends BaseDataConverter<SicRecord>
{
    private static final String DIVISION_NAME_PREFIX = "Division ";

    private static final int DIVISION_COLUMN_INDEX = 0;
    private static final int TWO_DIGIT_COLUMN_INDEX = 1;
    private static final int THREE_DIGIT_COLUMN_INDEX = 2;
    private static final int FOUR_DIGIT_COLUMN_INDEX = 3;

    @Override
    public Classification getClassification()
    {
        return Classification.SIC;
    }

    @Override
    public List<SicRecord> createDataRecords() throws IOException
    {
        String[] lines = DownloadUtil.downloadPdfFile(getClassification().getSourceFileLocation());

        List<SicRecord> recordList = new ArrayList<>();
        SicRecord currentRecord = new SicRecord();

        for (String line : lines)
        {
            String[] rowData = line.split(" ");

            String divisionId = rowData[0];

            if (divisionId.length() != 1) {
                // all data entries have a 1-letter first value
                continue;
            }

            if (line.contains(DIVISION_NAME_PREFIX)) {
                // it's a special case new division entry
                currentRecord.setDivisionId(divisionId);
                currentRecord.setDivisionName(cleanValue(line));
            }
            else {
                String fourDigitValue = rowData[FOUR_DIGIT_COLUMN_INDEX];

                try {
                    Integer intValue = Integer.valueOf(fourDigitValue);
                }
                catch (Exception e) {
                    int kjkj = 33333;
                }


                // use the 'rest of the array' to reconstruct the name value
                List<String> dataList = Arrays.asList(rowData);
                List<String> subList = dataList.subList(FOUR_DIGIT_COLUMN_INDEX+1, dataList.size());
                String[] subArray = subList.toArray(new String[0]);
                String name = String.join(" ", subArray);

                name = cleanValue(name);

                if (isMajorGroupId(fourDigitValue)) {
                    currentRecord.setMajorGroupId(fourDigitValue);
                    currentRecord.setMajorGroupName(name);
                }
                else if (isIndustryGroupId(fourDigitValue)) {
                    currentRecord.setIndustryGroupId(fourDigitValue);
                    currentRecord.setIndustryGroupName(name);
                }
                else if (isIndustryId(fourDigitValue)) {
                    currentRecord.setIndustryId(fourDigitValue);
                    currentRecord.setIndustryName(name);
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy();
                }
                else {
                    throw new InternalError("Illegal id detected: " + fourDigitValue);
                }
            }
        }
        return recordList;
    }


    private boolean isMajorGroupId(String id) {
        return id.endsWith("00");
    }

    private boolean isIndustryGroupId(String id) {
        return (id.endsWith("0") && !isMajorGroupId(id));
    }

    private boolean isIndustryId(String id) {
        return !id.endsWith("0");
    }

    @Override
    protected String cleanValue(String input)
    {
        // if starts with "Division ", then it's a new division,
        //  so remove redundant prefix.   Add an extra "1" to ALSO remove the letter
        //  following the prefix

        int divisionPrefixIndex = input.indexOf(DIVISION_NAME_PREFIX);
        if (divisionPrefixIndex > 0) {
            input = input.substring(divisionPrefixIndex + DIVISION_NAME_PREFIX.length() + 1);
        }

        return super.cleanValue(input);
    }
}
