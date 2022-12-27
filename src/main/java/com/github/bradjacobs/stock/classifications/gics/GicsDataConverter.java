package com.github.bradjacobs.stock.classifications.gics;

import bwj.util.excel.ExcelReader;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvFullSparseConverter;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GicsDataConverter implements DataConverter<GicsRecord>
{
    private static final boolean SKIP_DISCONTINUED_RECORDS = true;
    private static final String DISCONTINUED_IDENTIFIER = "discontinued";  // substring identifier for deprecated records.

    private static final int SRC_DESC_COLUMN = 7;

    // possible a 'description' could be on more than 1 line,
    //  but we will never bother to check for single description
    //  exceeding this many lines.
    private static final int MAX_DESCRIPTION_LINES = 10;

    @Override
    public Classification getClassification()
    {
        return Classification.GICS;
    }

    @Override
    public List<GicsRecord> createDataRecords() throws IOException {
        ExcelReader excelReader = ExcelReader.builder().setSkipEmptyRows(true).build();
        String[][] csvData = excelReader.convertToDataMatrix(getClassification().getSourceFileLocation());

        //   Step 1
        // The 'problem' with the given data is the description value is actually on a separate line following the other data.
        // Thus recreate the CSV such that the description is included with its related data on the same line.
        int startIndex = findFirstDataRowIndex(csvData);
        List<String[]> rowDataList = new ArrayList<>();
        for (int i = startIndex; i < csvData.length; i+=2) {
            //  IMPORTANT NOTE:  the dataRow array actually has a length 1 greater than it should!
            //    (this is a "lucky convenience", because this extra column exactly where the description value should go)
            String[] dataRow = csvData[i];
            String[] extraDescriptionRow = csvData[i+1];

            // clean up newlines and/or weird spaces for all the values.
            for (int j = 0; j < dataRow.length; j++) {
                dataRow[j] = StringUtil.cleanWhitespace(dataRow[j]);
            }
            String desc = extraDescriptionRow[SRC_DESC_COLUMN];

            // handle a description that can be one more than 1 line
            //   todo: this is terrible... should clean up in the future.
            int stopLine = i+MAX_DESCRIPTION_LINES;
            for (int k = i+2; k < stopLine && k < csvData.length; k++) {
                String[] nextLine = csvData[k];
                if (nextLine[6].trim().isEmpty()) {
                    desc += " : " + nextLine[SRC_DESC_COLUMN];
                    i++;
                }
                else {
                    break;
                }
            }

            //  IMPORTANT NOTE:  the dataRow array length by 1 to make room for description
            String[] dataRowCopy = new String[dataRow.length+1];
            System.arraycopy(dataRow, 0, dataRowCopy, 0, dataRow.length);
            dataRowCopy[dataRow.length] = desc;

            rowDataList.add(dataRowCopy);
        }

        //   Step 2
        // Now generate a new CSV data string with desired format (all record data on the same row)
        // IMPORTANT NOTE:  the regenerated CSV string will _NOT_ have a header row.
        CsvMapper csvArrayMapper = MapperBuilder.csv().setArrayWrap(true).build();
        String regeneratedCsvData = csvArrayMapper.writeValueAsString(rowDataList);

        //   Step 3
        // the current CSV string format is in 'sparse mode'  (meaning that a cell doesn't have a value)
        // if the column immediately above it has the same value.  So create a 'full' csv string,
        // in which every cell is populated.
        CsvFullSparseConverter csvFullSparseConverter = new CsvFullSparseConverter();
        String fullCsvData = csvFullSparseConverter.fillCsvData(regeneratedCsvData);

        //   Step 4
        // Now convert the CSV string into a List<GicsRecord>
        // IMPORTANT NOTE:  remember that the csv string does NOT have a header row.
        //   This can be worked around by ensuring that we know the order of each cell
        //   _AND_ that the GicsRecord has 'JsonPropertyOrder' annotation set.
        //   if the JsonPropertyOrder was not set then it would be arbitrary which values
        //   map to which fields in the GicsRecord object.
        //   (also note the objectReader built a little differently)
        CsvMapper csvMapper = MapperBuilder.csv().build();
        ObjectReader objReader = csvMapper.readerWithTypedSchemaFor(GicsRecord.class);
        MappingIterator<GicsRecord> iterator = objReader.readValues(fullCsvData);
        List<GicsRecord> gicsRecords = iterator.readAll();

        //   Step 5
        // Strip out any records that don't belong (discontinued)
        List<GicsRecord> finalGicsRecords = new ArrayList<>();
        for (GicsRecord gicsRecord : gicsRecords) {
            if (! shouldSkip(gicsRecord)) {
                finalGicsRecords.add(gicsRecord);
            }
        }
        return finalGicsRecords;
    }

    private int findFirstDataRowIndex(String[][] csvData) {
        for (int i = 0; i < csvData.length; i++)
        {
            String[] dataRow = csvData[i];
            String firstColumnValue = dataRow[0].trim();
            if (StringUtils.isNumeric(firstColumnValue)) {
                return i;
            }
        }
        return -1;
    }

    private boolean shouldSkip(GicsRecord record)
    {
        return SKIP_DISCONTINUED_RECORDS && isDiscontinued(record);
    }

    // todo: make less lame
    private boolean isDiscontinued(GicsRecord record) {
        if (record.getSectorName().toLowerCase().contains(DISCONTINUED_IDENTIFIER) ||
            record.getGroupName().toLowerCase().contains(DISCONTINUED_IDENTIFIER) ||
            record.getIndustryName().toLowerCase().contains(DISCONTINUED_IDENTIFIER) ||
            record.getSubIndustryName().toLowerCase().contains(DISCONTINUED_IDENTIFIER) ||
            record.getDescription().toLowerCase().contains(DISCONTINUED_IDENTIFIER)) {
            return true;
        }
        return false;
    }
}
