package com.github.bradjacobs.stock.classifications;

public class MainDriver
{
    // flag to also include the long description in the CSV files
    //   IF AND ONLY IF long description is actually available
    private static final boolean INCLUDE_LONG_DESCRIPTION = true;


    public static void main(String[] args) throws Exception
    {
        // still in demo-mode.....

        for (Classification classification : Classification.values())
        {
            DataConverter converter = DataConverterFactory.createDataConverter(classification, INCLUDE_LONG_DESCRIPTION);
            System.out.println("Creating files for: " + classification);
            converter.createDataFiles();
        }

    }
}
