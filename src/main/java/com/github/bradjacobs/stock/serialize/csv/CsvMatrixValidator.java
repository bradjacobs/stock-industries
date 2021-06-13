package com.github.bradjacobs.stock.serialize.csv;

public class CsvMatrixValidator
{
    /**
     * Checks if the 2-D array looks like well-formed csv data
     * @param dataArray
     * @throws IllegalArgumentException exception throw if error is detected.
     */
    public void validateMatrix(String[][] dataArray) throws IllegalArgumentException
    {
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
