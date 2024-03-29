package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.TupleToPojoConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;
import com.github.bradjacobs.stock.util.DownloadUtil;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// https://unstats.un.org/unsd/classifications/Econ/      general info link
// https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/CPC_Ver_2_1_english_structure.txt

/**
 */
public class CpcDataConverter implements DataConverter<CpcRecord>
{
    private static final List<String> TAGS_TO_REMOVE = Arrays.asList("<i>", "</i>");
    private static final TupleToPojoConverter TUPLE_TO_POJO_CONVERTER = new TupleToPojoConverter();

    @Override
    public Classification getClassification() {
        return Classification.CPC;
    }

    @Override
    public List<CpcRecord> createDataRecords() throws IOException {
        String csvData = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());
        CsvDeserializer csvDeserializer = new CsvDeserializer();
        List<RawCpcRecord> rawRecords = csvDeserializer.csvToObjectList(RawCpcRecord.class, csvData);

        return TUPLE_TO_POJO_CONVERTER.doConvertToObjects(CpcRecord.class, rawRecords);
    }

    // todo - use or remove
    protected String cleanValue(String input) {
        String cleanValue = input;
        for (String tag : TAGS_TO_REMOVE) {
            cleanValue = StringUtils.replace(cleanValue, tag, "");
        }
        return StringUtil.cleanWhitespace(cleanValue);
    }

    private static class RawCpcRecord implements CodeTitleLevelRecord {
        @JsonProperty("CPC21code")
        private String code;
        @JsonProperty("CPC21title")
        private String title;

        @Override
        public String getCodeId() { return code; }
        @Override
        public String getCodeTitle() { return title; }
        @Override
        @JsonIgnore
        public int getCodeLevel() {
            return code.length();
        }
    }
}
