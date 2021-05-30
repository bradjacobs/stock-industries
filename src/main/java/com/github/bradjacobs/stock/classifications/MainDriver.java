package com.github.bradjacobs.stock.classifications;

import com.github.bradjacobs.stock.classifications.gics.GicsDataConverter;
import com.github.bradjacobs.stock.classifications.naics.NaicsDataConverter;
import com.github.bradjacobs.stock.classifications.zacks.ZacksDataConverter;

public class MainDriver
{
    public static void main(String[] args) throws Exception
    {
        // still in demo-mode.....

//        GicsDataConverter gicsDataConverter = new GicsDataConverter();
//        gicsDataConverter.createDataFiles();
//
//        ZacksDataConverter zacksDataConverter = new ZacksDataConverter();
//        zacksDataConverter.createDataFiles();

        NaicsDataConverter naicsDataConverter = new NaicsDataConverter();
        naicsDataConverter.createDataFiles();


    }
}
