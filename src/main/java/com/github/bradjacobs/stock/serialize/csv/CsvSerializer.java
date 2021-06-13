package com.github.bradjacobs.stock.serialize.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.MapperBuilder;
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

        CsvMapper csvObjectMapper = MapperBuilder.csv().setArrayWrap(false).setClazz(clazz).setIncludeLongDescription(this.csvDefinition.isIncludeDescription()).build();

        CsvSchema schema = csvObjectMapper.schemaFor(clazz).withHeader();

        String csvData = csvObjectMapper.writer(schema).writeValueAsString(objectList);

        if (this.csvDefinition.isSparsely() && csvData.length() > 0) {
            csvData = csvFullSparseConverter.sparseifyCsvData(csvData);
        }
        return csvData;
    }

}
