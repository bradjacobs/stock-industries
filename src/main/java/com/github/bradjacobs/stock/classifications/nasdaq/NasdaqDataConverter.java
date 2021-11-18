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
import java.util.Arrays;
import java.util.HashSet;
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
    private static final String TICKER_KEY = "symbol"; // technically only need for diagnostic purposes.

    // the following tickers had a "unique permutation" of Sector/Industry that __subjectively__ looked suspicious
    //   thus suppressing for now.  There are other tickets with unique Sector/Industry that look correct
    //   and have been left in.
    private static final Set<String> SKIPPABLE_TICKERS = new HashSet<>(Arrays.asList(
            "USLM", // Sector: Energy             Industry: Other Metals and Minerals
            "WOR",  // Sector: Consumer Durables  Industry: Aerospace
            "YGMZ", // Sector: Consumer Services  Industry: Transportation Services
            "ZTO"   // Sector: Transportation     Industry: Advertising
    ));

    @Override
    public Classification getClassification()
    {
        return Classification.NASDAQ;
    }

    @Override
    public List<NasdaqRecord> createDataRecords() throws IOException
    {
        String json = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

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

            if (SKIPPABLE_TICKERS.contains(ticker)) {
                continue;
            }

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

            Set<String> industrySet = sectorEntry.getValue().keySet();
            industryCounter = 0;
            for (String industry : industrySet) {
                String industryId = String.format("%s.%d", sectorId, ++industryCounter);
                recordList.add(new NasdaqRecord(sectorId, sector, industryId, industry));
            }
        }

        //   NOTE: uncomment for debugging purposes
        //NasdaqDataDebugAnalysis.sectorMapValidityCheck(sectorIndustryMap);

        return recordList;
    }
}
