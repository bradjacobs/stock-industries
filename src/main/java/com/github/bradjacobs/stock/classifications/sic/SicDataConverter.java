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

        String prevDivisionId = "";

        String divisionId = "";
        String divisionName = "";
        String majorGroupId = "";
        String majorGroupName = "";
        String industryGroupId = "";
        String industryGroupName = "";
        String industryId = "";
        String industryName = "";


        for (String line : lines)
        {
            String[] rowData = line.split(" ");

            divisionId = rowData[0];

            if (divisionId.length() != 1) {
                // all data entries have a 1-letter first value
                continue;
            }

            if (!divisionId.equals(prevDivisionId))
            {
                String subStringToFind = "Division " + divisionId + " ";
                int index = line.indexOf(subStringToFind);
                if (index > 0) {
                    divisionName = line.substring(index + subStringToFind.length());
                    divisionName = cleanValue(divisionName);
                }
                prevDivisionId = divisionId;
            }
            else
            {
                String twoDigitValue = rowData[1];
                String threeDigitValue = rowData[2];
                String fourDigitValue = rowData[3];

                // use the 'rest of the array' to reconstruct the name value
                List<String> dataList = Arrays.asList(rowData);
                List<String> subList = dataList.subList(4, dataList.size());
                String[] subArray = subList.toArray(new String[0]);
                String name = String.join(" ", subArray);

                name = cleanValue(name);

                if (!fourDigitValue.endsWith("0")) {
                    // industries have non-zero 4th digit
                    industryId = fourDigitValue;
                    industryName = name;

                    SicRecord record = new SicRecord(divisionId, divisionName, majorGroupId, majorGroupName, industryGroupId, industryGroupName, industryId, industryName);
                    recordList.add(record);
                }
                else if (!threeDigitValue.endsWith("0")) {
                    // industryGroups have non-zero 3rd digit
                    industryGroupId = fourDigitValue;
                    industryGroupName = name;
                }
                else {
                    majorGroupId = fourDigitValue;
                    majorGroupName = name;
                }
            }
        }

        return recordList;
    }


}
