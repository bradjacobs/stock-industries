package com.github.bradjacobs.stock.classifications.nasdaq;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;
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
 */
public class NasdaqDataConverter extends BaseDataConverter<NasdaqRecord>
{
    private static final String SECTOR_KEY = "sector";
    private static final String INDUSTRY_KEY = "industry";

    @Override
    public Classification getClassification()
    {
        return Classification.NASDAQ;
    }

    @Override
    public List<NasdaqRecord> createDataRecords() throws IOException
    {
        String json = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        JsonMapper mapper = new JsonMapper();
        String innerRowsJson = mapper.writeValueAsString(mapper.readTree(json).get("data").get("rows"));
        List<Map<String,String>> listOfMaps = mapper.readValue(innerRowsJson, new TypeReference<List<Map<String, String>>>() {});

        // using TreeMap & TreeSet to keep everything sorted.
        Map<String, Set<String>> sectorIndustryMap = new TreeMap<>();

        for (Map<String, String> tickerMap : listOfMaps)
        {
            String sector = tickerMap.get(SECTOR_KEY);
            String industry = tickerMap.get(INDUSTRY_KEY);

            if (StringUtils.isNotEmpty(sector) && StringUtils.isNotEmpty(industry))
            {
                Set<String> industrySet = sectorIndustryMap.computeIfAbsent(sector, k -> new TreeSet<>());
                industrySet.add(industry);
            }
        }

        List<NasdaqRecord> recordList = new ArrayList<>();

        int sectorCounter = 0;
        for (Map.Entry<String, Set<String>> sectorEntry : sectorIndustryMap.entrySet())
        {
            String sector = sectorEntry.getKey();
            String sectorId = String.valueOf(++sectorCounter);

            Set<String> industrySet = sectorEntry.getValue();
            int industryCounter = 0;
            for (String industry : industrySet)
            {
                String industryId = String.format("%s.%d", sectorId, ++industryCounter);
                recordList.add(new NasdaqRecord(sectorId, sector, industryId, industry));
            }
        }

        return recordList;
    }
}
