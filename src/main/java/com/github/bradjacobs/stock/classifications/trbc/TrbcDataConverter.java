package com.github.bradjacobs.stock.classifications.trbc;

import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.util.DownloadUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrbcDataConverter extends BaseDataConverter<TrbcRecord>
{
    private static final int ECONOMIC_SECTOR_ID_LENGTH = 2;
    private static final int BUSINESS_SECTOR_ID_LENGTH = 4;
    private static final int INDUSTRY_GROUP_ID_LENGTH = 6;
    private static final int INDUSTRY_ID_LENGTH = 8;
    private static final int ACTIVITY_ID_LENGTH = 10;

    @Override
    public Classification getClassification()
    {
        return Classification.TRBC;
    }

    @Override
    public List<TrbcRecord> createDataRecords() throws IOException
    {
        String[] pdfFileLines = DownloadUtil.downloadPdfFile(getClassification().getSourceFileLocation());

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


    private List<TrbcRecord> parseLines(String[] lines)
    {
        List<TrbcRecord> recordList = new ArrayList<>();

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
            if (! StringUtils.isNumeric(trbcId)) {
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

                TrbcRecord record = new TrbcRecord(
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

}
