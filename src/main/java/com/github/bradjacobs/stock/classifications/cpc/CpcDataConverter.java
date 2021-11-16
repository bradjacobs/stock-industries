package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/CPC_Ver_2_1_english_structure.txt

/**
 */
public class CpcDataConverter extends AbstractCodeTitleConverter<CpcRecord>
{
    private static final List<String> TAGS_TO_REMOVE = Arrays.asList("<i>", "</i>");

    private static final String[] LEVEL_LABELS = new String[]{"section", "division", "group", "class", "subClass"};

    public CpcDataConverter() {
        super(LEVEL_LABELS, "Id", "Name");
    }

    @Override
    public Classification getClassification() {
        return Classification.CPC;
    }

    @Override
    protected Class<CpcRecord> getClassType() {
        return CpcRecord.class;
    }


    @Override
    public List<CpcRecord> createDataRecords() throws IOException
    {
        String filePath = "/Users/bradjacobs/git/bradjacobs/stock-industries/src/main/java/com/github/bradjacobs/stock/classifications/cpc/cpc.txt";
        String csvData = FileUtils.readFileToString(new File(filePath));
        //String csvData = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        CsvDeserializer csvDeserializer = new CsvDeserializer(null);
        List<RawCpcRecord> rawRecords = csvDeserializer.csvToObjectList(RawCpcRecord.class, csvData);

        return doConvertToObjects(rawRecords);
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


    private static class RawCpcRecord implements CodeTitleLevelRecord
    {
        private String code;
        private String title;

        @Override
        public String getCodeId() {
            return code; }
        @JsonProperty("CPC21code")
        public void setCode(String code) { this.code = code; }

        @Override
        public String getCodeTitle() { return title; }

        @JsonProperty("CPC21title")
        public void setTitle(String title) { this.title = title; }

        @Override
        @JsonIgnore
        public int getCodeLevel() {
            return code.length();
        }
    }

}
