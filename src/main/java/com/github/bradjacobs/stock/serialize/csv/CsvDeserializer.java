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
        return toListOfObjects(clazz, data, true);
    }


    // todo: the 'withHeader' param is still up in the air.
    private <T> List<T> toListOfObjects(Class<T> type, String csvData, boolean withHeader) throws IOException
    {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        CsvMapper csvMapper = CsvSerializer.createCsvMapper(false);

        ObjectReader objReader;
        if (withHeader) {
            objReader = csvMapper.readerFor(type).with(schema);
        }
        else {
            // todo: 'readerWithTypedSchemaFor' generally seems nicer,
            //   BUT it seems more prone to error if data isn't perfectly formatted
            objReader = csvMapper.readerWithTypedSchemaFor(type);
        }

        MappingIterator<T> iterator = objReader.readValues(csvData);

        List<T> inputRecords = iterator.readAll();

        return inputRecords;
    }


}
