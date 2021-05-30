package bwj.stock.classifications;

import bwj.stock.classifications.gics.GicsDataConverter;
import bwj.stock.classifications.zacks.ZacksDataConverter;

public class MainDriver
{
    public static void main(String[] args) throws Exception
    {
        // still in demo-mode.....

        GicsDataConverter gicsDataConverter = new GicsDataConverter();
        gicsDataConverter.createDataFiles();

        ZacksDataConverter zacksDataConverter = new ZacksDataConverter();
        zacksDataConverter.createDataFiles();

    }
}
