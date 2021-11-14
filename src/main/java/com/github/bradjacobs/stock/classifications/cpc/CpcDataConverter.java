package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;
import com.github.bradjacobs.stock.util.DownloadUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/CPC_Ver_2_1_english_structure.txt
/**
 */
public class CpcDataConverter extends BaseDataConverter<CpcRecord>
{
    private static final List<String> TAGS_TO_REMOVE = Arrays.asList("<i>", "</i>");

    @Override
    public Classification getClassification()
    {
        return Classification.CPC;
    }

    private static class RawEntryRecord {
        @JsonProperty("CPC21code")
        private String code;
        @JsonProperty("CPC21title")
        private String title;
    }

    @Override
    public List<CpcRecord> createDataRecords() throws IOException
    {
        String csvData = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        CsvDeserializer csvDeserializer = new CsvDeserializer(null);
        List<RawEntryRecord> rawRecords = csvDeserializer.csvToObjectList(RawEntryRecord.class, csvData);

        List<CpcRecord> recordList = new ArrayList<>();
        CpcRecord currentRecord = new CpcRecord();

        for (RawEntryRecord rawRecord : rawRecords) {
            String id = rawRecord.code;
            String name = cleanValue(rawRecord.title);

            int level = id.length();
            if (level == 1) {
                currentRecord.setSectionId(id);
                currentRecord.setSectionName(name);
            }
            else if (level == 2) {
                if (!currentRecord.getDivisionId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setDivisionId(id);
                currentRecord.setDivisionName(name);
            }
            else if (level == 3) {
                if (!currentRecord.getGroupId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setGroupId(id);
                currentRecord.setGroupName(name);
            }
            else if (level == 4) {
                if (!currentRecord.getClassId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setClassId(id);
                currentRecord.setClassName(name);
            }
            else if (level == 5) {
                if (!currentRecord.getSubClassId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setSubClassId(id);
                currentRecord.setSubClassName(name);
            }
        }

        recordList.add(currentRecord);
        return recordList;
    }

    @Override
    protected String cleanValue(String input)
    {
        String cleanValue = input;
        for (String tag : TAGS_TO_REMOVE) {
            cleanValue = StringUtils.replace(cleanValue, tag, "");
        }
        return super.cleanValue(cleanValue);
    }
}
