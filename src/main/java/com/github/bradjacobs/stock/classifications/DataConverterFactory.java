package com.github.bradjacobs.stock.classifications;

import com.github.bradjacobs.stock.classifications.cpc.CpcDataConverter;
import com.github.bradjacobs.stock.classifications.gics.GicsDataConverter;
import com.github.bradjacobs.stock.classifications.icb.IcbDataConverter;
import com.github.bradjacobs.stock.classifications.isic.IsicDataConverter;
import com.github.bradjacobs.stock.classifications.mgecs.MgecsDataConverter;
import com.github.bradjacobs.stock.classifications.nace.NaceDataConverter;
import com.github.bradjacobs.stock.classifications.naics.NaicsDataConverter;
import com.github.bradjacobs.stock.classifications.napcs.NapcsDataConverter;
import com.github.bradjacobs.stock.classifications.nasdaq.NasdaqDataConverter;
import com.github.bradjacobs.stock.classifications.sasb.SasbDataConverter;
import com.github.bradjacobs.stock.classifications.sic.SicDataConverter;
import com.github.bradjacobs.stock.classifications.trbc.TrbcDataConverter;
import com.github.bradjacobs.stock.classifications.unspsc.UnspscDataConverter;
import com.github.bradjacobs.stock.classifications.zacks.ZacksDataConverter;

public class DataConverterFactory
{
    private DataConverterFactory() { }


    public static DataConverter<?> createDataConverter(Classification classification) {
        if (classification == null) {
            throw new IllegalArgumentException("Must provide a classification!");
        }

        switch (classification) {
            case CPC:
                return new CpcDataConverter();
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
            case NAPCS:
                return new NapcsDataConverter();
            case NASDAQ:
                return new NasdaqDataConverter();
            case SASB:
                return new SasbDataConverter();
            case SIC:
                return new SicDataConverter();
            case TRBC:
                return new TrbcDataConverter();
            case UNSPSC:
                return new UnspscDataConverter();
            case ZACKS:
                return new ZacksDataConverter();
            default:
                throw new IllegalArgumentException("Unrecognized classification type: " + classification);
        }
    }
}
