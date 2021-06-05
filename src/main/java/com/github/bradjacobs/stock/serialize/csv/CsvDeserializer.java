package com.github.bradjacobs.stock.serialize.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.serialize.BaseDeserializer;
import com.github.bradjacobs.stock.types.CsvDefinition;

import java.io.IOException;
import java.util.List;

public class CsvDeserializer extends BaseDeserializer
{
    private static final CsvFullSparseConverter csvFullSparseConverter = new CsvFullSparseConverter();

    private final CsvDefinition csvDefinition;

    public CsvDeserializer(CsvDefinition csvDefinition)
    {
        this.csvDefinition = csvDefinition;
    }

    @Override
    public <T> List<T> deserializeObjects(Class<T> clazz, String data) throws IOException
    {
        // if the incoming data is 'sparse', then populate it.
        //     todo: fix naming b/c it's confusing
        if (this.csvDefinition.isSparsely()) {
            data = csvFullSparseConverter.fillCsvData(data);
        }

        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        CsvMapper csvMapper = CsvSerializer.createCsvMapper(false);

        ObjectReader objReader = csvMapper.readerFor(clazz).with(schema);

        MappingIterator<T> iterator = objReader.readValues(data);

        List<T> inputRecords = iterator.readAll();

        return inputRecords;
    }

}
