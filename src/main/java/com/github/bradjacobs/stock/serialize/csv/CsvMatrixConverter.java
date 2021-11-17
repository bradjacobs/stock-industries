package com.github.bradjacobs.stock.serialize.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.github.bradjacobs.stock.MapperBuilder;

import java.io.IOException;

/**
 * Converts to/from a raw csvData string (i.e. the entire content of a csv file)
 *  to a 2-D string array
 */
public class CsvMatrixConverter
{
    private static final CsvMapper csvArrayMapper = MapperBuilder.csv().setArrayWrap(true).build();
    private static final CsvMatrixValidator csvMatrixValidator = new CsvMatrixValidator();

    public static String[][] convertToMatrix(String csv) throws IOException
    {
        if (csv.isEmpty()) {
            return new String[0][0];  // tbd to allow this
        }
        return csvArrayMapper.readValue(csv, String[][].class);
    }

    public static String convertToCsv(String[][] csvMatrix) throws IOException
    {
        csvMatrixValidator.validateMatrix(csvMatrix);

        if (csvMatrix.length == 0) {
            return "";  // tbd to allow this
        }
        return csvArrayMapper.writeValueAsString(csvMatrix);
    }


}
