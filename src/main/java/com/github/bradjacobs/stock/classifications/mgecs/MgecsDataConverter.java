package com.github.bradjacobs.stock.classifications.mgecs;

import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MgecsDataConverter implements DataConverter<MgecsRecord>
{
    private static final int SECTOR_ID_LENGTH = 3;
    private static final int GROUP_ID_LENGTH = 5;
    private static final int INDUSTRY_ID_LENGTH = 8;

    private static final String START_LINE_INDICATOR = "1 Cyclical"; // the string to look for to detect the first line with the 'data'

    @Override
    public Classification getClassification()
    {
        return Classification.MGECS;
    }


    @Override
    public List<MgecsRecord> createDataRecords() throws IOException
    {
        String[] pdfFileLines = DownloadUtil.downloadPdfFile(getClassification().getSourceFileLocation());

        // UPDATE:
        //    come to find out that specific whitespace placement can cause the PdfParser to mess up the line order.
        //    thus each 'part' will be temporarily saved, then everything reassmabled afterwards.
        Map<String, String> sectorNameMap = new HashMap<>();
        Map<String, String> groupNameMap = new HashMap<>();
        Map<String, MgecsRecord> industryToRecordMap = new TreeMap<>();  // keep keys sorted


        // search for line where the data 'actually' starts.
        int firstDataLineIndex = findFirstDataRowIndex(pdfFileLines);

        for (int i = firstDataLineIndex; i < pdfFileLines.length; i++)
        {
            String line = pdfFileLines[i].trim();

            if (StringUtils.isEmpty(line)) {
                continue;
            }
            else if (isMorningstarLine(line)) {
                continue;
            }

            if (StringUtils.isNumeric(line))
            {
                String id = line;
                String name = "";

                // note:  page numbers are example of case that would pass 'isNumeric' check, but not pass if-check below
                if (isValidIdLength(id))
                {
                    name = pdfFileLines[++i];  // don't trim (..yet)

                    // note: the name affiliated w/ the group category can be split across multiple lines
                    if (id.length() == SECTOR_ID_LENGTH || id.length() == GROUP_ID_LENGTH)
                    {
                        // SIDE: this is squirrelly
                        //   Problem is that sometimes text goes to next line and the '\n' is the ONLY space character b/w 2 words.
                        //   Thus have to take extra caution so that 2 seperate words don't get concatenated into 1.


                        // temporarily add back new line and don't trim
                        //  b/c spacing can get messed up.
                        String futureLine = pdfFileLines[i+1];

                        while (! StringUtils.isNumeric(futureLine.trim()))
                        {
                            if (StringUtils.isNotEmpty(futureLine.trim())) {
                                name = name + futureLine;
                            }

                            // note:  increment the 'current' i value,
                            // then the futureLine is the next line after the current line  (thus the +1)
                            i++;
                            futureLine = pdfFileLines[i+1];
                        }
                        name = StringUtil.cleanWhitespace(name);
                    }

                    // now finally can trim + clean value
                    name = cleanValue(name);

                    if (id.length() == SECTOR_ID_LENGTH)
                    {
                        sectorNameMap.put(id, name);
                    }
                    else if (id.length() == GROUP_ID_LENGTH)
                    {
                        groupNameMap.put(id, name);
                    }
                    else if (id.length() == INDUSTRY_ID_LENGTH)
                    {
                        // NOTE: sub-optimal soln b/c future lines are examined in getDescription method as well as this loop.
                        //   (even though problem exists, not worth addressing at present)
                        String desc = getDescription(pdfFileLines, i+1);

                        MgecsRecord record = new MgecsRecord();
                        record.setDescription(desc);
                        record.setIndustryId(id);
                        record.setIndustryName(name);

                        industryToRecordMap.put(id, record);
                    }
                }
            }
        }

        // now the industryToRecordMap.values() have all of the records,
        //   but need to backfill in the sector and group information.
        List<MgecsRecord> recordList = new ArrayList<>(industryToRecordMap.values());
        for (MgecsRecord record : recordList)
        {
            String industryId = record.getIndustryId();

            // taking advantage that the sector & group ids are always a substring of the industry id.
            String sectorId = industryId.substring(0, SECTOR_ID_LENGTH);
            String groupId = industryId.substring(0, GROUP_ID_LENGTH);

            record.setSectorId(sectorId);
            record.setSectorName(sectorNameMap.get(sectorId));
            record.setIndustryGroupId(groupId);
            record.setIndustryGroupName(groupNameMap.get(groupId));
        }


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

    private boolean isValidIdLength(String id) {
        int idLength = id.length();
        if (idLength == SECTOR_ID_LENGTH || idLength == GROUP_ID_LENGTH || idLength == INDUSTRY_ID_LENGTH) {
            return true;
        }
        return false;
    }


    private int findFirstDataRowIndex(String[] fileLines)
    {
        for (int i = 0; i < fileLines.length; i++)
        {
            String line = fileLines[i].trim();
            if (line.equalsIgnoreCase(START_LINE_INDICATOR)) {
                return i;
            }
        }
        return -1;  // not found.
    }


    /**
     * check if line contains "Morningstar", which implies it's a header/footer
     * @param line line
     * @return if line contains 'morningstar' keyword
     */
    private boolean isMorningstarLine(String line) {
        return line != null && line.toLowerCase().contains("morningstar");
    }

    protected String cleanValue(String input)
    {
        String result = StringUtil.cleanWhitespace(input);

        // replace special 8212 dash w/ "normal" dash (if necessary)
        if (result.contains("—")) {
            result = result.replace('—', '-');
        }
        return result;
    }

    private String getDescription(String[] pdfFileLines, int startingIndex)
    {
        StringBuilder sb = new StringBuilder();
        int currentIndex = startingIndex;

        while (true)
        {
            if (currentIndex >= pdfFileLines.length) {
                break;
            }
            String currentLine = pdfFileLines[currentIndex++].trim();

            if (StringUtils.isEmpty(currentLine)) {
                continue;
            }
            else if (isMorningstarLine(currentLine)) {
                continue;
            }
            if (StringUtils.isNumeric(currentLine)) {
                // if number is a sector/group etc, then hit next section, thus done reading current description
                // if number is 'small', then it's probably a page number that can be skipped.
                if (currentLine.length() >= SECTOR_ID_LENGTH) {
                    break;
                }
                else {
                    continue;
                }
            }

            sb.append(' ').append(currentLine);
        }

        return StringUtil.cleanWhitespace(sb.toString());
    }

}
