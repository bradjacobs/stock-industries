package com.github.bradjacobs.stock.classifications.icb;

import bwj.util.excel.ExcelReader;
import bwj.util.excel.QuoteMode;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;

import java.io.IOException;
import java.util.ArrayList;
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
        ExcelReader excelReader = ExcelReader.builder().setQuoteMode(QuoteMode.NEVER).setSkipEmptyRows(true).build();
        String[][] origCsvData = excelReader.createCsvMatrix(getClassification().getSourceFileLocation());
        return generateRecords(origCsvData);
    }

    private List<IcbRecord> generateRecords(String[][] origCsvData)
    {
        List<IcbRecord> recordList = new ArrayList<>();

        // note: start at 1, skip header row
        for (int i = 1; i < origCsvData.length; i++)
        {
            String[] rowData = origCsvData[i];

            IcbRecord icbRecord = new IcbRecord();
            icbRecord.setIndustryCode(rowData[0]);
            icbRecord.setIndustry( cleanValue(rowData[1]) );
            icbRecord.setSuperSectorCode(rowData[2]);
            icbRecord.setSuperSector( cleanValue(rowData[3]) );
            icbRecord.setSectorCode(rowData[4]);
            icbRecord.setSector( cleanValue(rowData[5]) );
            icbRecord.setSubSectorCode(rowData[6]);
            icbRecord.setSubSector( cleanValue(rowData[7]) );
            icbRecord.setDefinition( cleanValue(rowData[8]) );

            recordList.add(icbRecord);
        }

        return recordList;
    }
}
