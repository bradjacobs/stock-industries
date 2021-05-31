package com.github.bradjacobs.stock.classifications.refinitiv;

import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.util.PdfUtil;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RefinitivDataConverter extends BaseDataConverter<RefinitivRecord>
{
    private static final String SOURCE_FILE = "https://www.refinitiv.com/content/dam/marketing/en_us/documents/quick-reference-guides/trbc-business-classification-quick-guide.pdf";

    private static final int ECONOMIC_SECTOR_ID_LENGTH = 2;
    private static final int BUSINESS_SECTOR_ID_LENGTH = 4;
    private static final int INDUSTRY_GROUP_ID_LENGTH = 6;
    private static final int INDUSTRY_ID_LENGTH = 8;
    private static final int ACTIVITY_ID_LENGTH = 10;


    @Override
    public String getFilePrefix()
    {
        return "trbc";
    }

    @Override
    public List<RefinitivRecord> generateDataRecords() throws IOException
    {
        URL url = new URL(SOURCE_FILE);
        String[] pdfFileLines = PdfUtil.getPdfFileLines(url.openStream());

        return parseLines(pdfFileLines);
    }


    private boolean isTableColumnHeaderRow(String line)
    {
        return line.startsWith("Economic");
    }

    private boolean isSkipableLine(String line)
    {
        if (isTableColumnHeaderRow(line)) {
            return true;
        }
        if (line.startsWith("Thomson Reuters")) {
            return true;
        }
        if (line.toLowerCase().contains("refinitiv")) {
            return true;
        }
        return false;
    }


    private List<RefinitivRecord> parseLines(String[] lines)
    {
        List<RefinitivRecord> recordList = new ArrayList<>();

        String economicSectorName = "";
        String businessSectorName = "";
        String industryGroupName = "";
        String industryName = "";
        String activityName = "";

        String economicSectorId = "";
        String businessSectorId = "";
        String industryGroupId = "";
        String industryId = "";
        String activityId = "";

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();

            if (isSkipableLine(line)) {
                continue;
            }

            String[] lineElements = line.split(" ");

            if (lineElements.length <= 2) {
                continue;
            }

            String trbcId = lineElements[lineElements.length-1];
            if (! isNumber(trbcId)) {
                continue;
            }

            String permId = lineElements[lineElements.length-2];  //  note: can be "{New}" (i.e. can't assume it's a number)
            int permIdIndex = line.indexOf(permId);

            // name is immediately before the permId
            String name = line.substring(0, permIdIndex-1);

            name = cleanValue(name);

            // todo - fix couple rare exceptions where name is on more than one line.

            if (trbcId.length() == ECONOMIC_SECTOR_ID_LENGTH) {
                economicSectorId = trbcId;
                economicSectorName = name;
            }
            else if (trbcId.length() == BUSINESS_SECTOR_ID_LENGTH) {
                businessSectorId = trbcId;
                businessSectorName = name;
            }
            else if (trbcId.length() == INDUSTRY_GROUP_ID_LENGTH) {
                industryGroupId = trbcId;
                industryGroupName = name;
            }
            else if (trbcId.length() == INDUSTRY_ID_LENGTH) {
                industryId = trbcId;
                industryName = name;
            }
            else if (trbcId.length() == ACTIVITY_ID_LENGTH) {
                activityId = trbcId;
                activityName = name;

                RefinitivRecord record = new RefinitivRecord(
                    economicSectorId, economicSectorName,
                    businessSectorId, businessSectorName,
                    industryGroupId, industryGroupName,
                    industryId, industryName,
                    activityId, activityName);

                recordList.add(record);
            }
        }

        return recordList;
    }

    @Override
    protected String cleanValue(String input)
    {
        // todo - some minor upper/lower case fixes.

        return super.cleanValue(input);
    }

    private static boolean isNumber(String s)
    {
        if (s == null) {
            return false;
        }
        s = s.trim();
        if (s.length() == 0) {
            return false;
        }

        try {
            Long.valueOf(s);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

}
