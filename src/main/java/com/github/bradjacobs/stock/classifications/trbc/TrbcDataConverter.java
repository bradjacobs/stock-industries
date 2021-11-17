package com.github.bradjacobs.stock.classifications.trbc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.classifications.common.TupleToPojoConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TrbcDataConverter implements DataConverter<TrbcRecord>
{
    private static final TupleToPojoConverter TUPLE_TO_POJO_CONVERTER = new TupleToPojoConverter();

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

    private List<TrbcRecord> parseLines(String[] lines) throws JsonProcessingException
    {
        List<TrbcEntry> entryList = getEntryRecords(lines);
        return TUPLE_TO_POJO_CONVERTER.doConvertToObjects(TrbcRecord.class, entryList);
    }

    /**
     * Grabs all the data in the form of codeId/Title entries.
     *   (taking advantage of the fact that each value is one its own line)
     * @param lines lines to parse
     * @return List<TrbcEntry>
     */
    private List<TrbcEntry> getEntryRecords(String[] lines)
    {
        List<TrbcEntry> resultList = new ArrayList<>();

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();
            if (isSkipableLine(line)) {
                continue;
            }

            String[] lineElements = line.split(" ");

            // if there are exactly 2 elements and the last is a number
            //   then this is a result of a pdf multi-line parse issue
            //     thus prepend the previous 2 lines to create a 'new' line
            if (lineElements.length == 2) {
                if (StringUtils.isNumeric(lineElements[1])) {
                    line = String.join(" ", lines[i-2], lines[i-1], line);
                    lineElements = line.split(" ");
                }
            }

            if (lineElements.length > 2) {
                String trbcId = lineElements[lineElements.length-1];
                if (! StringUtils.isNumeric(trbcId)) {
                    continue;
                }

                //  note: permId can be "{New}" (i.e. can't assume it's a number)
                String permId = lineElements[lineElements.length-2];
                int permIdIndex = line.indexOf(permId);

                // name is immediately before the permId
                String name = line.substring(0, permIdIndex-1);

                name = cleanValue(name);
                TrbcEntry trbcEntry = new TrbcEntry(trbcId, name);
                resultList.add(trbcEntry);
            }
        }

        return resultList;
    }

    protected String cleanValue(String input)
    {
        // todo - some minor upper/lower case fixes.
        return StringUtil.cleanWhitespace(input);
    }

    private boolean isSkipableLine(String line)
    {
        if (StringUtils.isEmpty(line)) {
            return true;
        }
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

    private boolean isTableColumnHeaderRow(String line)
    {
        return line.startsWith("Economic");
    }

    private static class TrbcEntry implements CodeTitleLevelRecord
    {
        private final String code;
        private final String title;

        public TrbcEntry(String code, String title) {
            this.code = code;
            this.title = title;
        }

        @Override
        public String getCodeId() { return code; }
        @Override
        public String getCodeTitle() { return title; }
        @Override
        public int getCodeLevel() {
            return code.length() / 2;
        }  // level is always 1/2 the size of the id.
    }

}
