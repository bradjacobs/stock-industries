package com.github.bradjacobs.stock.classifications;

public class MainDriver
{

    public static void main(String[] args) throws Exception
    {
        // still in demo-mode.....

        for (Classification classification : Classification.values())
        {

//            if (! classification.equals(Classification.MGECS)) {
//                continue;
//            }


            DataConverter converter = DataConverterFactory.createDataConverter(classification);
            System.out.println("Creating files for: " + classification);
            converter.createDataFiles();
        }

    }
}
