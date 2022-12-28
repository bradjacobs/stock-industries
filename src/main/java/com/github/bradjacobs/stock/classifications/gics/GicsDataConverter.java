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

    @Override
    public Classification getClassification() {
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

        for (int i = startIndex; i < csvData.length; i++) {
            String[] dataRow = csvData[i];

            // grab description(s) which start on the next line
            List<String> descriptionList = getDescriptions(i+1, csvData);
            String description = String.join(" ", descriptionList);

            // adjust current line index for description lines
            i = i + descriptionList.size();

            // IMPORTANT NOTE: incrrease dataRow array length by 1 to make room for description
            String[] dataRowCopy = new String[dataRow.length+1];
            System.arraycopy(dataRow, 0, dataRowCopy, 0, dataRow.length);
            dataRowCopy[dataRow.length] = description;

            boolean isSkippable = cleanRow(dataRowCopy);
            if (!isSkippable) {
                rowDataList.add(dataRowCopy);
            }
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
        return iterator.readAll();
    }

    private List<String> getDescriptions(int startRowIndex, String[][] csvData) {
        List<String> descriptionList = new ArrayList<>();
        for (int j = startRowIndex; j < csvData.length; j++) {
            String[] row = csvData[j];
            String descCell = StringUtil.cleanWhitespace( row[SRC_DESC_COLUMN] );
            String prevCell = StringUtil.cleanWhitespace( row[SRC_DESC_COLUMN-1] );

            if (prevCell.isEmpty() && !descCell.isEmpty()) {
                descriptionList.add(descCell);
            }
            else {
                break;
            }
        }
        return descriptionList;
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

    // side-effect row cleaning any string values as needed
    private boolean cleanRow(String[] dataRow) {
        boolean isskippable = false;
        for (int i = 0; i < dataRow.length; i++) {
            String value = dataRow[i];
            if (value.toLowerCase().contains(DISCONTINUED_IDENTIFIER)) {
                // don't strip out a discontinued identifier
                if (SKIP_DISCONTINUED_RECORDS) {
                    isskippable = true;
                }
            }
            else {
                value = value.trim();
                //  remove any suffixes such as " (New Name)"
                if (!value.isEmpty() && value.charAt(value.length()-1) == ')') {
                    int lastOpenParen = value.lastIndexOf('(');
                    value = value.substring(0, lastOpenParen);
                }
            }
            dataRow[i] = StringUtil.cleanWhitespace(value);
        }
        return isskippable;
    }
}
