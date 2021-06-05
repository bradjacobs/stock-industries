package com.github.bradjacobs.stock.serialize.csv;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;

import java.io.IOException;
import java.util.Arrays;

// TODO:
//   1 think of better class name
//   2 think of better method names
//   3 add javadocs to explain this class
public class CsvFullSparseConverter
{

    public String sparseifyCsvData(String csvData) throws IOException {
        return convertCsvData(csvData, true);
    }

    public String fillCsvData(String csvData) throws IOException {
        return convertCsvData(csvData, false);
    }

    public String[][] sparseifyCsvArray(String[][] fullDataArray) {
        return convertMatrix(fullDataArray, true);
    }

    public String[][] fillCsvArray(String[][] sparseDataArray) {
        return convertMatrix(sparseDataArray, false);
    }


    private String convertCsvData(String csvData, boolean makeSparse) throws IOException
    {
        String[][] origMatrix = CsvMatrixConverter.convertToMatrix(csvData);
        String[][] convertedMatrix = convertMatrix(origMatrix, makeSparse);
        return CsvMatrixConverter.convertToCsv(convertedMatrix);
    }

    private String[][] convertMatrix(String[][] dataArray, boolean makeSparse)
    {
        validateInput(dataArray);

        int rowCount = dataArray.length;
        int colCount = dataArray[0].length;

        // create a new 2-D matrix with the same size as original
        String[][] resultDataArray = new String[rowCount][colCount];

        // start by just copying over the first row (header row) from the original dataArray
        resultDataArray[0] = Arrays.copyOf(dataArray[0], colCount);

        // Note: starting at index 1
        for (int i = 1; i < rowCount; i++)
        {
            // the previous row to compare against depends if we are making sparse or making full
            //  tend the need the one that has the 'more' data, so depends which way we're going.
            String[] previousRow;
            if (makeSparse) {
                previousRow = dataArray[i-1];
            }
            else {
                previousRow = resultDataArray[i-1];
            }

            String[] currentRow = dataArray[i];

            for (int j = 0; j < colCount; j++)
            {
                String prevCell = previousRow[j];
                String currCell = currentRow[j];

                if (makeSparse && currCell.equals(prevCell)) {
                    resultDataArray[i][j] = "";
                }
                else if (!makeSparse && currCell.equals("")) {
                    resultDataArray[i][j] = prevCell;
                }
                else {
                    resultDataArray[i][j] = currCell;
                }
            }
        }

        return resultDataArray;
    }


    private void validateInput(String[][] dataArray) {
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
