package com.github.bradjacobs.stock.classifications.icb;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;

import java.io.IOException;
import java.util.List;

public class IcbDataConverter implements DataConverter<IcbRecord>
{
    @Override
    public Classification getClassification()
    {
        return Classification.ICB;
    }

    @Override
    public List<IcbRecord> createDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NORMAL).setSkipEmptyRows(true).build();

        String csvText = excelReader.createCsvText(getClassification().getSourceFileLocation());

        CsvDeserializer csvDeserializer = new CsvDeserializer();
        return csvDeserializer.deserializeObjects(IcbRecord.class, csvText);
    }
}
