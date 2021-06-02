package com.github.bradjacobs.stock.classifications.isic;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.gics.GicsDataConverter;
import com.github.bradjacobs.stock.classifications.gics.GicsRecord;
import com.github.bradjacobs.stock.util.DownloadUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @see <a href="https://ilostat.ilo.org/resources/concepts-and-definitions/classification-economic-activities/">https://ilostat.ilo.org/resources/concepts-and-definitions/classification-economic-activities/</a>
 */
public class IsicDataConverter extends BaseDataConverter<IsicRecord>
{
    // explicit tab name in the Excel file
    private static final String EXCEL_TAB_NAME = "ISIC_Rev_4";


    public IsicDataConverter(boolean includeDescriptions)
    {
        super(includeDescriptions);
    }

    @Override
    public Classification getClassification()
    {
        return Classification.ISIC;
    }

    @Override
    public List<IsicRecord> generateDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NORMAL).setSkipEmptyRows(true).setSheetName(EXCEL_TAB_NAME).build();

        String csvText = excelReader.createCsvText(getClassification().getSourceFileLocation());

        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectReader objReader = csvObjectMapper.readerFor(IsicRecord.class).with(schema);

        MappingIterator<IsicRecord> iterator = objReader.readValues(csvText);
        List<IsicRecord> recordList = iterator.readAll();

        return recordList;
    }

}
