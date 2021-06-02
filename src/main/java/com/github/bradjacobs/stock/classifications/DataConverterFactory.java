package com.github.bradjacobs.stock.classifications;

import com.github.bradjacobs.stock.classifications.gics.GicsDataConverter;
import com.github.bradjacobs.stock.classifications.icb.IcbDataConverter;
import com.github.bradjacobs.stock.classifications.isic.IsicDataConverter;
import com.github.bradjacobs.stock.classifications.mgecs.MgecsDataConverter;
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

    // flag to also include the long description in the CSV files
    //   IF AND ONLY IF long description is actually available
    private static final boolean DEFAULT_INCLUDE_LONG_DESCRIPTION = true;


    public static DataConverter createDataConverter(Classification classification) {
        return createDataConverter(classification, DEFAULT_INCLUDE_LONG_DESCRIPTION);
    }

    public static DataConverter createDataConverter(Classification classification, boolean includeLongDescription) {
        if (classification == null) {
            throw new IllegalArgumentException("Must provide a classificaiton!");
        }

        switch (classification) {
//            case BICS:
//                throw new NotImplementedException("No current implemenation for BICS.");
            case GICS:
                return new GicsDataConverter(includeLongDescription);
            case ICB:
                return new IcbDataConverter(includeLongDescription);
            case ISIC:
                return new IsicDataConverter(includeLongDescription);
            case MGECS:
                return new MgecsDataConverter(includeLongDescription);
            case NAICS:
                return new NaicsDataConverter(includeLongDescription);
            case SASB:
                return new SasbDataConverter(includeLongDescription);
            case SIC:
                return new SicDataConverter(includeLongDescription);
            case TRBC:
                return new TrbcDataConverter(includeLongDescription);
            case ZACKS:
                return new ZacksDataConverter(includeLongDescription);
            default:
                throw new IllegalArgumentException("Unrecognized classification type: " + classification);
        }
    }


    public static Map<Classification, DataConverter> createDataConverters(List<Classification> classifications) {
        return createDataConverters(classifications, DEFAULT_INCLUDE_LONG_DESCRIPTION);
    }

    public static Map<Classification, DataConverter> createDataConverters(List<Classification> classifications, boolean includeLongDescription) {

        if (classifications == null || classifications.isEmpty()) {
            throw new IllegalArgumentException("Must provide one or more classificaitons!");
        }

        // using LinkedHashMap to retain order (in case that's important)
        Map<Classification, DataConverter> resultMap = new LinkedHashMap<>();
        for (Classification classification : classifications)
        {
            resultMap.put(classification, createDataConverter(classification, includeLongDescription));
        }

        return resultMap;
    }

}
