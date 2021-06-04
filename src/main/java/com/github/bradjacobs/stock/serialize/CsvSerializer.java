package com.github.bradjacobs.stock.serialize;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.types.CsvDefinition;

import java.io.IOException;
import java.util.List;

public class CsvSerializer extends BaseSerializer
{
    private static final FullSparseMatrixConverter csvMatrixConverter = new FullSparseMatrixConverter();

    private final CsvDefinition csvDefinition;


    public CsvSerializer(CsvDefinition csvDefinition)
    {
        this.csvDefinition = csvDefinition;
    }

    @Override
    public String generateFileSuffix()
    {
        return this.csvDefinition.generateFileSuffix();
    }

    @Override
    public <T> String serializeObjects(List<T> objectList) throws IOException
    {
        Class<?> clazz = identifyClass(objectList);
        CsvMapper csvObjectMapper = createCsvMapper(false, clazz, this.csvDefinition.isIncludeDescription());
        CsvSchema schema = csvObjectMapper.schemaFor(clazz).withHeader();

        String csvData = csvObjectMapper.writer(schema).writeValueAsString(objectList);

        if (this.csvDefinition.isSparsely() && csvData.length() > 0) {
            csvData = sparseifyCsvData(csvData);
        }
        return csvData;
    }


    public <T> String[][] serializeToMatrix(List<T> objectList) throws IOException
    {
        String serializedString = serialize(objectList);
        return serializeToMatrix(serializedString);
    }


    protected String sparseifyCsvData(String csvData) throws IOException
    {
        String[][] csvMatrix = serializeToMatrix(csvData);
        String[][] sparseMatrix = csvMatrixConverter.createSparseCsvArray(csvMatrix);
        return serializeToCsv(sparseMatrix);
    }

    protected String[][] serializeToMatrix(String csv) throws IOException
    {
        if (csv.isEmpty()) {
            return new String[0][0];
        }

        CsvMapper csvArrayMapper = createCsvMapper(true);
        return csvArrayMapper.readValue(csv, String[][].class);
    }

    protected String serializeToCsv(String[][] csvMatrix) throws IOException
    {
        if (csvMatrix.length == 0) {
            return "";
        }

        CsvMapper csvArrayMapper = createCsvMapper(true);
        return csvArrayMapper.writeValueAsString(csvMatrix);
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
