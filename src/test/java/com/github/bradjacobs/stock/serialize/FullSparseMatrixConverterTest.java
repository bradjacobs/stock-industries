package com.github.bradjacobs.stock.serialize;

import org.testng.annotations.Test;

import static org.testng.Assert.*;


public class FullSparseMatrixConverterTest
{
    private static final String[][] FULL_TEST_DATA = new String[][] {
        { "sectorId", "sectorName", "groupId", "groupName" },
        { "10", "Health Care", "1010", "Medical Equipment" },
        { "10", "Health Care", "1020", "Pharmaceuticals" },
        { "10", "Health Care", "1030", "Biotechnology" },
        { "20", "Financials", "2010", "Banks" },
        { "30", "Energy", "3010", "Oil" },
        { "30", "Energy", "3020", "Gas" },
    };

    private static final String[][] SPARSE_TEST_DATA = new String[][] {
        { "sectorId", "sectorName", "groupId", "groupName" },
        { "10", "Health Care", "1010", "Medical Equipment" },
        { "", "", "1020", "Pharmaceuticals" },
        { "", "", "1030", "Biotechnology" },
        { "20", "Financials", "2010", "Banks" },
        { "30", "Energy", "3010", "Oil" },
        { "", "", "3020", "Gas" },
    };


    @Test
    public void testFullToSparse() throws Exception
    {
        FullSparseMatrixConverter converter = new FullSparseMatrixConverter();
        String[][] sparseMatrix = converter.createSparseCsvArray(FULL_TEST_DATA);
        assert2dArraysEquals(sparseMatrix, SPARSE_TEST_DATA);
    }

    @Test
    public void testSparseToFull() throws Exception
    {
        FullSparseMatrixConverter converter = new FullSparseMatrixConverter();
        String[][] fullMatrix = converter.createFullCsvArray(SPARSE_TEST_DATA);
        assert2dArraysEquals(fullMatrix, FULL_TEST_DATA);
    }


    /**
     * helper method for assert compare 2-d string array.
     * @param actual actual value
     * @param expected expected value
     */
    private void assert2dArraysEquals(String[][] actual, String[][] expected)
    {
        if (expected == null) {
            assertNull(actual, "expected null array");
            return;
        }
        else {
            assertNotNull(actual, "expected non-null array");
        }

        int actualRowCount = actual.length;
        int expectedRowCount = expected.length;
        assertEquals(actualRowCount, expectedRowCount, "mismatch expected number of rows");

        String[] actualRowOne = actual[0];
        String[] expectedRowOne = expected[0];
        assertEquals(actualRowOne.length, expectedRowOne.length, "mismatch expected number of columns");

        for (int i = 0; i < expectedRowCount; i++)
        {
            String[] actualRow = actual[i];
            String[] expectedRow = expected[i];
            assertEquals(actualRow, expectedRow, "mismatch of expected row values on row index: " + i);
        }
    }

}
