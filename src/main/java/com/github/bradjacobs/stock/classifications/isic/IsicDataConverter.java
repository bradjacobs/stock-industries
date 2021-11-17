package com.github.bradjacobs.stock.classifications.isic;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;

import java.io.IOException;
import java.util.List;

/**
 *
 * @see <a href="https://ilostat.ilo.org/resources/concepts-and-definitions/classification-economic-activities/">https://ilostat.ilo.org/resources/concepts-and-definitions/classification-economic-activities/</a>
 */
public class IsicDataConverter implements DataConverter<IsicRecord>
{
    // explicit tab name in the Excel file
    private static final String EXCEL_TAB_NAME = "ISIC_Rev_4";

    @Override
    public Classification getClassification()
    {
        return Classification.ISIC;
    }

    @Override
    public List<IsicRecord> createDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NORMAL).setSkipEmptyRows(true).setSheetName(EXCEL_TAB_NAME).build();

        String csvText = excelReader.createCsvText(getClassification().getSourceFileLocation());

        CsvDeserializer csvDeserializer = new CsvDeserializer();
        return csvDeserializer.deserializeObjects(IsicRecord.class, csvText);
    }

}
