package bwj.stock.classifications;

import bwj.stock.classifications.gics.GicsDataConverter;

public class MainDriver
{
    public static void main(String[] args) throws Exception
    {
        GicsDataConverter gicsDataConverter = new GicsDataConverter();

        gicsDataConverter.createDataFiles();

    }
}
