package com.github.bradjacobs.stock.classifications.icb;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;

import java.io.IOException;
import java.util.List;

public class IcbDataConverter extends BaseDataConverter<IcbRecord>
{
    public IcbDataConverter(boolean includeDescriptions)
    {
        super(includeDescriptions);
    }

    @Override
    public Classification getClassification()
    {
        return Classification.ICB;
    }

    @Override
    public List<IcbRecord> generateDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NORMAL).setSkipEmptyRows(true).build();

        String csvText = excelReader.createCsvText(getClassification().getSourceFileLocation());

        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader objReader = csvObjectMapper.readerFor(IcbRecord.class).with(schema);

        MappingIterator<IcbRecord> iterator = objReader.readValues(csvText);
        return iterator.readAll();
    }
}
