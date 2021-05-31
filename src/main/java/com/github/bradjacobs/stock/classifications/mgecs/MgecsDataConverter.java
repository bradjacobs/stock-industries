package com.github.bradjacobs.stock.classifications.mgecs;

import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;
import com.github.bradjacobs.stock.util.PdfUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MgecsDataConverter extends BaseDataConverter<MgecsRecord>
{
    private static final String SOURCE_FILE = "https://advisor.morningstar.com/Enterprise/VTC/MorningstarGlobalEquityClassStructure2019v3.pdf";

    private static final int SECTOR_ID_LENGTH = 3;
    private static final int GROUP_ID_LENGTH = 5;
    private static final int INDUSTRY_ID_LENGTH = 8;

    private static final String START_LINE_INDICATOR = "1 Cyclical";

    @Override
    public String getFilePrefix()
    {
        return "morningstar";
    }

    @Override
    public List<MgecsRecord> generateDataRecords() throws IOException
    {
        URL url = new URL(SOURCE_FILE);
        String[] pdfFileLines = PdfUtil.getPdfFileLines(url.openStream());


        List<MgecsRecord> recordList = new ArrayList<>();

        MgecsRecord currentRecord = null;

        boolean parsingStarted = false;
        for (int i = 0; i < pdfFileLines.length; i++)
        {
            // todo - clean up start detectiion.
            String line = pdfFileLines[i].trim();
            if (line.equalsIgnoreCase(START_LINE_INDICATOR)) {
                parsingStarted = true;
            }

            if (parsingStarted)
            {
                if (StringUtils.isEmpty(line)) {
                    continue;
                }
                else if (line.toLowerCase().contains("morningstar")) {
                    continue;
                }

                if (StringUtils.isNumeric(line))
                {
                    String id = line;
                    String name = "";

                    // note:  page numbers are example of case that would pass 'isNumeric' check, but not pass if-check below
                    if (id.length() == SECTOR_ID_LENGTH || id.length() == GROUP_ID_LENGTH || id.length() == INDUSTRY_ID_LENGTH)
                    {
                        name = pdfFileLines[++i];  // don't trim (..yet)

                        // note: the name affiliated w/ the group category can be split across multiple lines
                        if (id.length() == GROUP_ID_LENGTH)
                        {
                            // temporarily add back new line and don't trim
                            //  b/c spacing can get messed up.
                            String futureLine = pdfFileLines[i+1];
                            while (! StringUtils.isNumeric(futureLine))
                            {
                                if (StringUtils.isNotEmpty(futureLine.trim())) {
                                    name = name + futureLine;
                                }
                                i++;
                                futureLine = pdfFileLines[i+1];
                            }
                            name = cleanWhitespace(name);
                        }

                        // now finally can trim + clean value
                        name = cleanValue(name);


                        if (id.length() == SECTOR_ID_LENGTH)
                        {
                            if (currentRecord != null) {
                                recordList.add(currentRecord);
                            }
                            currentRecord = new MgecsRecord();
                            currentRecord.setSectorId(id);
                            currentRecord.setSectorName(name);
                        }
                        else if (id.length() == GROUP_ID_LENGTH)
                        {
                            if (currentRecord.getIndustryGroupId() != null) {
                                recordList.add(currentRecord);
                                currentRecord = currentRecord.copy();
                                currentRecord.setIndustryId(null);
                                currentRecord.setIndustryName(null);
                            }
                            currentRecord.setIndustryGroupId(id);
                            currentRecord.setIndustryGroupName(name);
                        }
                        else if (id.length() == INDUSTRY_ID_LENGTH)
                        {
                            if (currentRecord.getIndustryId() != null) {
                                recordList.add(currentRecord);
                                currentRecord = currentRecord.copy();
                            }
                            currentRecord.setIndustryId(id);
                            currentRecord.setIndustryName(name);
                        }
                    }
                }
            }
        }

        recordList.add(currentRecord);

        // sanity check
        for (MgecsRecord naicsRecord : recordList)
        {
            if (StringUtils.isEmpty(naicsRecord.getSectorId())) {
                throw new RuntimeException("empty sectorId detected");
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

    @Override
    protected String cleanValue(String input)
    {
        String result = super.cleanValue(input);

        // todo:   TBD
//        // replace special 8212 dash w/ "normal" dash (if necessary)
//        if (result.contains("—")) {
//            result = result.replace('—', '-');
//        }

        return result;

    }

}
