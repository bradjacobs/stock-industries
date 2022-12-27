package com.github.bradjacobs.stock.classifications.sitc;

import bwj.util.excel.ExcelReader;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.classifications.common.TupleToPojoConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;

import java.io.IOException;
import java.util.List;

// https://unstats.un.org/unsd/classifications/Econ/
/**
 *
 */
public class SitcDataConverter implements DataConverter<SitcRecord>
{
    private static final TupleToPojoConverter TUPLE_TO_POJO_CONVERTER = new TupleToPojoConverter();

    @Override
    public Classification getClassification() {
        return Classification.SITC;
    }

    @Override
    public List<SitcRecord> createDataRecords() throws IOException {
        ExcelReader excelReader = ExcelReader.builder().build();
        String csvText = excelReader.convertToCsvText(getClassification().getSourceFileLocation());

        CsvDeserializer csvDeserializer = new CsvDeserializer();
        List<RawSitcRecord> rawRecords = csvDeserializer.csvToObjectList(RawSitcRecord.class, csvText);

        return TUPLE_TO_POJO_CONVERTER.doConvertToObjects(SitcRecord.class, rawRecords);
    }

    private static class RawSitcRecord implements CodeTitleLevelRecord {
        @JsonProperty("Classification")
        private String classification;
        @JsonProperty("Code")
        private String code;
        @JsonProperty("Description")
        private String title;
        @JsonProperty("Parent Code")
        private String parent;
        @JsonProperty("Level")
        private Integer level;
        @JsonProperty("IsBasicLevel")
        private Integer isBasicLevel;

        @Override
        public String getCodeId() { return code; }
        @Override
        public String getCodeTitle() { return title; }
        @Override
        public int getCodeLevel() {
            return level;
        }
    }
}
