package com.github.bradjacobs.stock.classifications.nace;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.TupleToPojoConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.util.DownloadUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  NOTE1: the 'CSV' file isn't true csv ??
 *  NOTE2: skipping the 'long description' b/c it's crazy long and not very useful.
 */
public class NaceDataConverter implements DataConverter<NaceRecord>
{
    // ***** NOTE *****
    //   when downloading the file with a GET (instead of a POST), seems to use semicolon ';' instead of comma ',' for separator
    private static final Character COLUMN_SEPARATOR = ';';

    private static final TupleToPojoConverter TUPLE_TO_POJO_CONVERTER = new TupleToPojoConverter();


    @Override
    public Classification getClassification()
    {
        return Classification.NACE;
    }

    @Override
    public List<NaceRecord> createDataRecords() throws IOException
    {
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(COLUMN_SEPARATOR);
        CsvMapper csvObjectMapper = MapperBuilder.csv().setArrayWrap(false).build();

        ObjectReader objReader = csvObjectMapper.readerFor(RawNacoRecord.class).with(schema);
        String csvData = DownloadUtil.downloadFile(this.getClassification().getSourceFileLocation());

        MappingIterator<RawNacoRecord> iterator = objReader.readValues(csvData);
        List<RawNacoRecord> pojoList = iterator.readAll();

        return TUPLE_TO_POJO_CONVERTER.doConvertToObjects(NaceRecord.class, pojoList);
    }


    private static class RawNacoRecord implements CodeTitleLevelRecord
    {
        @JsonProperty("Code")
        private String code;
        @JsonProperty("Description")
        private String title;
        @JsonProperty("Level")
        private int level;
        @JsonIgnore
        private final Map<String, Object> additionalProperties = new HashMap<>();

        @Override
        public String getCodeId() { return code; }
        @Override
        public String getCodeTitle() { return title; }
        @Override
        public int getCodeLevel() {
            return level;
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
