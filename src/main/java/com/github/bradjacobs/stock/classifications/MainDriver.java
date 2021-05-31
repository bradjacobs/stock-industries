package com.github.bradjacobs.stock.classifications;

import com.github.bradjacobs.stock.classifications.gics.GicsDataConverter;
import com.github.bradjacobs.stock.classifications.icb.IcbDataConverter;
import com.github.bradjacobs.stock.classifications.isic.IsicDataConverter;
import com.github.bradjacobs.stock.classifications.mgecs.MgecsDataConverter;
import com.github.bradjacobs.stock.classifications.naics.NaicsDataConverter;
import com.github.bradjacobs.stock.classifications.refinitiv.RefinitivDataConverter;
import com.github.bradjacobs.stock.classifications.sic.SicDataConverter;
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
//
//        NaicsDataConverter naicsDataConverter = new NaicsDataConverter();
//        naicsDataConverter.createDataFiles();
//
//        MgecsDataConverter mgecsDataConverter = new MgecsDataConverter();
//        mgecsDataConverter.createDataFiles();
//
//        RefinitivDataConverter refvDataConverter = new RefinitivDataConverter();
//        refvDataConverter.createDataFiles();

//        SicDataConverter sicDataConverter = new SicDataConverter();
//        sicDataConverter.createDataFiles();

//        IcbDataConverter icbDataConverter = new IcbDataConverter();
//        icbDataConverter.createDataFiles();

        IsicDataConverter isicDataConverter = new IsicDataConverter();
        isicDataConverter.createDataFiles();
    }
}
