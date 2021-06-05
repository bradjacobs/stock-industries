package com.github.bradjacobs.stock.serialize.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.io.IOException;

/**
 * Converts to/from a raw csvData string (i.e. the entire content of a csv file)
 *  to a 2-D string array
 */
public class CsvMatrixConverter
{
    private static final CsvMapper csvArrayMapper = CsvSerializer.createCsvMapper(true);

    public static String[][] convertToMatrix(String csv) throws IOException
    {
        if (csv.isEmpty()) {
            return new String[0][0];  // tbd to allow this
        }
        return csvArrayMapper.readValue(csv, String[][].class);
    }

    public static String convertToCsv(String[][] csvMatrix) throws IOException
    {
        validateMatrixInput(csvMatrix);

        if (csvMatrix.length == 0) {
            return "";  // tbd to allow this
        }
        return csvArrayMapper.writeValueAsString(csvMatrix);
    }



    // todo - fix redundant method
    private static void validateMatrixInput(String[][] dataArray) {
        if (dataArray == null) {
            throw new IllegalArgumentException("dataArray cannot be null.");
        }

        if (dataArray.length == 0) {
            throw new IllegalArgumentException("cannot convert array with zero rows.");
        }

        String[] firstRow = dataArray[0];
        if (firstRow == null || firstRow.length == 0) {
            throw new IllegalArgumentException("cannot convert array with no columns.");
        }
        // add others as needed
    }

}
