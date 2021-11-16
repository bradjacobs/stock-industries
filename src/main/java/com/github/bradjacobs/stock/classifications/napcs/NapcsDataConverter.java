package com.github.bradjacobs.stock.classifications.napcs;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.AbstractCodeTitleConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * https://www.census.gov/naics/napcs
 * https://www.census.gov/eos/www/napcs/structure.html
 */
public class NapcsDataConverter extends AbstractCodeTitleConverter implements DataConverter<NapcsRecord>
{
    private static final String[] LEVEL_LABELS =
            new String[]{"section", "subSection", "division", "group", "subGroup", "trilateralProduct"};

    public NapcsDataConverter() {
        super(LEVEL_LABELS, "Id", "Name");
    }

    @Override
    public Classification getClassification()
    {
        return Classification.NAPCS;
    }


    @Override
    public List<NapcsRecord> createDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.LENIENT).setSkipEmptyRows(true).build();

        String filePath = "/Users/bradjacobs/git/bradjacobs/stock-industries/src/main/java/com/github/bradjacobs/stock/classifications/napcs/2017NAPCSStructure-1.xlsx";

//        String csvData = excelReader.createCsvText(getClassification().getSourceFileLocation());
        String csvData = excelReader.createCsvText(filePath);

        CsvDeserializer csvDeserializer = new CsvDeserializer(null);
        List<RawNapcsRecord> rawRecords = csvDeserializer.csvToObjectList(RawNapcsRecord.class, csvData);

        return doConvertToObjects(NapcsRecord.class, rawRecords);
    }

    private static class RawNapcsRecord implements CodeTitleLevelRecord
    {
        // map to determine the depth level based on the length of the id.
        private static final Map<Integer,Integer> LENGTH_TO_LEVEL_MAP =  new HashMap<Integer, Integer>() {{
            put( 2, 1);
            put( 3, 2);
            put( 5, 3);
            put( 7, 4);
            put( 9, 5);
            put(11, 6);
        }};

        @JsonProperty("2017 NAPCS Code")
        private String code;
        @JsonProperty("Title")
        private String title;
        @JsonIgnore
        private final Map<String, Object> additionalProperties = new HashMap<>();


        @Override
        public String getCodeId() {
            return code; }

        @Override
        public String getCodeTitle() { return title; }

        @Override
        @JsonIgnore
        public int getCodeLevel() {
            Integer level = LENGTH_TO_LEVEL_MAP.get(this.code.length());
            if (level != null) {
                return level;
            }
            return 0;
        }

        @JsonIgnore
        public Map<String, Object> getAdditionalProperties() {
            return this.additionalProperties;
        }
        @JsonAnySetter
        public void setAdditionalProperty(String name, Object value) {
            this.additionalProperties.put(name, value);
        }
    }

}
