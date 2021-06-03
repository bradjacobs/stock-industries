package com.github.bradjacobs.stock.classifications;

import com.github.bradjacobs.stock.classifications.gics.GicsDataConverter;
import com.github.bradjacobs.stock.classifications.icb.IcbDataConverter;
import com.github.bradjacobs.stock.classifications.isic.IsicDataConverter;
import com.github.bradjacobs.stock.classifications.mgecs.MgecsDataConverter;
import com.github.bradjacobs.stock.classifications.nace.NaceDataConverter;
import com.github.bradjacobs.stock.classifications.naics.NaicsDataConverter;
import com.github.bradjacobs.stock.classifications.sasb.SasbDataConverter;
import com.github.bradjacobs.stock.classifications.sic.SicDataConverter;
import com.github.bradjacobs.stock.classifications.trbc.TrbcDataConverter;
import com.github.bradjacobs.stock.classifications.zacks.ZacksDataConverter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DataConverterFactory
{
    private DataConverterFactory() { }


    public static DataConverter createDataConverter(Classification classification) {
        if (classification == null) {
            throw new IllegalArgumentException("Must provide a classificaiton!");
        }

        switch (classification) {
//            case BICS:
//                throw new NotImplementedException("No current implemenation for BICS.");
            case GICS:
                return new GicsDataConverter();
            case ICB:
                return new IcbDataConverter();
            case ISIC:
                return new IsicDataConverter();
            case MGECS:
                return new MgecsDataConverter();
            case NACE:
                return new NaceDataConverter();
            case NAICS:
                return new NaicsDataConverter();
            case SASB:
                return new SasbDataConverter();
            case SIC:
                return new SicDataConverter();
            case TRBC:
                return new TrbcDataConverter();
            case ZACKS:
                return new ZacksDataConverter();
            default:
                throw new IllegalArgumentException("Unrecognized classification type: " + classification);
        }
    }



    public static Map<Classification, DataConverter> createDataConverters(List<Classification> classifications) {

        if (classifications == null || classifications.isEmpty()) {
            throw new IllegalArgumentException("Must provide one or more classificaitons!");
        }

        // using LinkedHashMap to retain order (in case that's important)
        Map<Classification, DataConverter> resultMap = new LinkedHashMap<>();
        for (Classification classification : classifications)
        {
            resultMap.put(classification, createDataConverter(classification));
        }

        return resultMap;
    }

}
