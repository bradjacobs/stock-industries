package com.github.bradjacobs.stock.serialize.csv;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.serialize.BaseDeserializer;
import com.github.bradjacobs.stock.types.CsvDefinition;

import java.io.IOException;
import java.util.List;

public class CsvDeserializer extends BaseDeserializer
{
    private static final CsvFullSparseConverter csvFullSparseConverter = new CsvFullSparseConverter();

    private final CsvDefinition csvDefinition;

    public CsvDeserializer() {
        this(null);
    }
    public CsvDeserializer(CsvDefinition csvDefinition) {
        this.csvDefinition = csvDefinition;
    }

    @Override
    public <T> List<T> deserializeObjects(Class<T> clazz, String data) throws IOException
    {
        // if the incoming data is 'sparse', then populate it.
        //     todo: fix naming b/c it's confusing
        if (this.csvDefinition != null && this.csvDefinition.isSparsely()) {
            data = csvFullSparseConverter.fillCsvData(data);
        }

        return csvToObjectList(clazz, data);
    }

    public <T> List<T> csvToObjectList(Class<T> clazz, String csvDataString) throws IOException
    {
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        CsvMapper csvObjectMapper = MapperBuilder.csv().setArrayWrap(false).build();
        ObjectReader objReader = csvObjectMapper.readerFor(clazz).with(schema);
        MappingIterator<T> iterator = objReader.readValues(csvDataString);
        return iterator.readAll();
    }
}
