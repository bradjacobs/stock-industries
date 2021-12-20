package com.github.bradjacobs.stock.classifications.nasdaq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Sectors based on Nasdaq data.
 *
 * NOTES:
 *   1. was unable to find 'official' document explaining hierarchy (thus have 'faked' the id values)
 *   2. these sector/industries show up on many other places (not just Nasdaq)
 *
 * UPDATE:
 *   upon review, skeptical of putting much trust in original sector/industry data
 */
public class NasdaqDataConverter implements DataConverter<NasdaqRecord>
{
    private static final String SECTOR_KEY = "sector";
    private static final String INDUSTRY_KEY = "industry";
    private static final String TICKER_KEY = "symbol";

    //   NEW --   each unique Sector/Industry combination MUST be affiliated
    //               with at least these meny ticker symbols (or else it will be ignored)
    private static final int MIN_TICKERS_FOR_SECTOR_INDUSTRY = 2;

    @Override
    public Classification getClassification()
    {
        return Classification.NASDAQ;
    }

    @Override
    public List<NasdaqRecord> createDataRecords() throws IOException
    {
        String json = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());
        return createDataRecords(json);
    }

    public List<NasdaqRecord> createDataRecords(String json) throws IOException
    {
        // using TreeMap/TreeSet to keep everything sorted.
        //   { "Sector" : { "Industry" : [(all ticker values for sector/industry]  }  }
        Map<String, Map<String,Set<String>>> sectorIndustryMap = new TreeMap<>();

        JsonMapper mapper = MapperBuilder.json().build();
        String innerRowsJson = mapper.writeValueAsString(mapper.readTree(json).get("data").get("rows"));
        List<Map<String,String>> listOfMaps = mapper.readValue(innerRowsJson, new TypeReference<List<Map<String, String>>>() {});

        for (Map<String, String> tickerRecordMap : listOfMaps)
        {
            String ticker = tickerRecordMap.get(TICKER_KEY);
            String sector = tickerRecordMap.get(SECTOR_KEY);
            String industry = tickerRecordMap.get(INDUSTRY_KEY);

            if (StringUtils.isNotEmpty(sector) && StringUtils.isNotEmpty(industry)) {
                Map<String, Set<String>> industryMap = sectorIndustryMap.computeIfAbsent(sector, k -> new TreeMap<>());
                Set<String> tickerSet = industryMap.computeIfAbsent(industry, k -> new TreeSet<>());
                tickerSet.add(ticker);
            }
        }

        List<NasdaqRecord> recordList = new ArrayList<>();

        // counters are used to give 'pseudo' id values.
        int sectorCounter = 0;
        int industryCounter = 0;

        for (Map.Entry<String, Map<String, Set<String>>> sectorEntry : sectorIndustryMap.entrySet())
        {
            String sector = sectorEntry.getKey();
            String sectorId = String.valueOf(++sectorCounter);

            Map<String, Set<String>> industryTickerMap = sectorEntry.getValue();
            Set<String> industrySet = industryTickerMap.keySet();

            industryCounter = 0;
            for (String industry : industrySet) {
                Set<String> tickerValues = industryTickerMap.get(industry);
                if (tickerValues.size() < MIN_TICKERS_FOR_SECTOR_INDUSTRY) {
                    continue;
                }
                String industryId = String.format("%s.%d", sectorId, ++industryCounter);
                recordList.add(new NasdaqRecord(sectorId, sector, industryId, industry));
            }
        }

        //   NOTE: uncomment for debugging purposes
        //NasdaqDataDebugAnalysis.sectorMapValidityCheck(sectorIndustryMap);

        return recordList;
    }
}
