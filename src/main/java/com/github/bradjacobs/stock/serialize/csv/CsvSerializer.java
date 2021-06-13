package com.github.bradjacobs.stock.serialize.csv;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.serialize.BaseSerializer;
import com.github.bradjacobs.stock.types.CsvDefinition;

import java.io.IOException;
import java.util.List;

public class CsvSerializer extends BaseSerializer
{
    private static final CsvFullSparseConverter csvFullSparseConverter = new CsvFullSparseConverter();

    private final CsvDefinition csvDefinition;


    public CsvSerializer(CsvDefinition csvDefinition)
    {
        this.csvDefinition = csvDefinition;
    }

    @Override
    public String generateFileName(Classification classification)
    {
        return csvDefinition.generateFileName(classification);
    }

    @Override
    public <T> String serializeObjects(List<T> objectList) throws IOException
    {
        Class<?> clazz = identifyClass(objectList);
        CsvMapper csvObjectMapper = createCsvMapper(false, clazz, this.csvDefinition.isIncludeDescription());
        CsvSchema schema = csvObjectMapper.schemaFor(clazz).withHeader();

        String csvData = csvObjectMapper.writer(schema).writeValueAsString(objectList);

        if (this.csvDefinition.isSparsely() && csvData.length() > 0) {
            csvData = csvFullSparseConverter.sparseifyCsvData(csvData);
        }
        return csvData;
    }


    // TODO... fix below...it's kludgy
    public static CsvMapper createCsvMapper(boolean isArrayMapper)
    {
        return createCsvMapper(isArrayMapper, null, true);
    }

    public static CsvMapper createCsvMapper(boolean isArrayMapper, Class clazz, boolean includeDescriptions) {
        CsvMapper.Builder builder = CsvMapper.builder()
            .enable(CsvParser.Feature.SKIP_EMPTY_LINES)
            .enable(CsvParser.Feature.TRIM_SPACES)
            .enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
            .enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY); // ALWAYS disable this (or it can change the column order)

        if (isArrayMapper) {
            builder = builder.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        }

        if (! includeDescriptions) {
            builder = builder.addMixIn(clazz, NoDescriptionMixin.class);
        }

        return builder.build();
    }

}
