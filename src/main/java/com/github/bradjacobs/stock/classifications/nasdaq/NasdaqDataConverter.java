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
 */
public class NasdaqDataConverter implements DataConverter<NasdaqRecord>
{
    private static final String SECTOR_KEY = "sector";
    private static final String INDUSTRY_KEY = "industry";
    private static final String TICKER_KEY = "symbol"; // <- only need for diagnostic purposes.

    private static final boolean PRINT_UNIQUE_TICKER_SECTOR_INDUSTRY = false;  // for debugging


    @Override
    public Classification getClassification()
    {
        return Classification.NASDAQ;
    }

    @Override
    public List<NasdaqRecord> createDataRecords() throws IOException
    {
        String json = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        JsonMapper mapper = MapperBuilder.json().build();
        String innerRowsJson = mapper.writeValueAsString(mapper.readTree(json).get("data").get("rows"));
        List<Map<String,String>> listOfMaps = mapper.readValue(innerRowsJson, new TypeReference<List<Map<String, String>>>() {});

        // using TreeMap & TreeSet to keep everything sorted.
        // key   = sector
        // value =   Map... key = industry,  value = all tickers for the sector/industry
        Map<String, Map<String,Set<String>>> sectorIndustryMap = new TreeMap<>();

        for (Map<String, String> tickerMap : listOfMaps)
        {
            String ticker = tickerMap.get(TICKER_KEY);
            String sector = tickerMap.get(SECTOR_KEY);
            String industry = tickerMap.get(INDUSTRY_KEY);

            if (SKIPPABLE_TICKERS.contains(ticker)) {
                continue;
            }

            if (StringUtils.isNotEmpty(sector) && StringUtils.isNotEmpty(industry))
            {
                Map<String, Set<String>> industryMap = sectorIndustryMap.computeIfAbsent(sector, k -> new TreeMap<>());
                Set<String> tickerSet = industryMap.computeIfAbsent(industry, k -> new TreeSet<>());
                tickerSet.add(ticker);
            }
        }

        // scan over the entire data structure to see if there are any Sector/Industry combinations
        //   that correlate with exactly _ONE_ ticker.  Meaning there 'could' be a data error.
        StringBuilder msgBuilder = new StringBuilder();
        for (Map.Entry<String, Map<String, Set<String>>> sectorEntry : sectorIndustryMap.entrySet())
        {
            String sector = sectorEntry.getKey();
            Map<String, Set<String>> industryMap = sectorEntry.getValue();
            for (Map.Entry<String, Set<String>> industryEntry : industryMap.entrySet())
            {
                String industry = industryEntry.getKey();
                Set<String> tickerSet = industryEntry.getValue();
                if (tickerSet.size() == 1)
                {
                    msgBuilder.append(String.format("| Ticker: %-5s | Sector: %-21s | Industry: %-39s |\n", tickerSet.iterator().next(), sector, industry));
                }
            }
        }

        if (msgBuilder.length() > 0 && PRINT_UNIQUE_TICKER_SECTOR_INDUSTRY)
        {
            System.out.println("NOTE: Nasdaq data has following sector/industry pairs only once...");
            System.out.println(msgBuilder.toString());
            System.out.println();
        }

        List<NasdaqRecord> recordList = new ArrayList<>();

        int sectorCounter = 0;
        for (Map.Entry<String, Map<String, Set<String>>> sectorEntry : sectorIndustryMap.entrySet())
        {
            String sector = sectorEntry.getKey();
            String sectorId = String.valueOf(++sectorCounter);

            Set<String> industrySet = sectorEntry.getValue().keySet();
            int industryCounter = 0;
            for (String industry : industrySet)
            {
                String industryId = String.format("%s.%d", sectorId, ++industryCounter);
                recordList.add(new NasdaqRecord(sectorId, sector, industryId, industry));
            }
        }

        return recordList;
    }


    // the following tickers had a "unique permutation" of Sector/Industry that _subjectively_ looked suspicious
    //   thus suppressing for now there are other tickets with unique Sector/Industry that look correct
    //   and have been left in.
    private static final Set<String> SKIPPABLE_TICKERS = new HashSet<>(Arrays.asList(
        "USLM", // Sector: Energy             Industry: Consumer Electronics/Appliances
        "WOR",  // Sector: Consumer Durables  Industry: Aerospace
        "ZTO"   // Sector: Transportation     Industry: Advertising
    ));

    // REFERENCE ONLY:
    //   as of 06/10/2021, all the Sector/Industry combinations that match
    //   to EXACTLY one ticker is listed below:
    /*
        | Ticker: APG   | Sector: Basic Industries      | Industry: Engineering & Construction              |
        | Ticker: NEWA  | Sector: Basic Industries      | Industry: Miscellaneous                           |
        | Ticker: TTC   | Sector: Capital Goods         | Industry: Tools/Hardware                          |
        | Ticker: WOR   | Sector: Consumer Durables     | Industry: Aerospace                               |
        | Ticker: WHR   | Sector: Consumer Durables     | Industry: Consumer Electronics/Appliances         |
        | Ticker: CMT   | Sector: Consumer Durables     | Industry: Electronic Components                   |
        | Ticker: FERG  | Sector: Consumer Durables     | Industry: Industrial Machinery/Components         |
        | Ticker: DOGZ  | Sector: Consumer Durables     | Industry: Miscellaneous manufacturing industries  |
        | Ticker: CVR   | Sector: Consumer Non-Durables | Industry: Industrial Specialties                  |
        | Ticker: FIGS  | Sector: Consumer Non-Durables | Industry: Medical Specialities                    |
        | Ticker: CLII  | Sector: Consumer Services     | Industry: Automotive Aftermarket                  |
        | Ticker: CHSCM | Sector: Consumer Services     | Industry: Farming/Seeds/Milling                   |
        | Ticker: EBF   | Sector: Consumer Services     | Industry: Office Equipment/Supplies/Services      |
        | Ticker: UTME  | Sector: Energy                | Industry: Consumer Electronics/Appliances         |
        | Ticker: USLM  | Sector: Energy                | Industry: Other Metals and Minerals               |
        | Ticker: HI    | Sector: Miscellaneous         | Industry: Consumer Specialties                    |
        | Ticker: CHRA  | Sector: Public Utilities      | Industry: Medical Specialities                    |
        | Ticker: SGU   | Sector: Public Utilities      | Industry: Oil Refining/Marketing                  |
        | Ticker: SPH   | Sector: Public Utilities      | Industry: Other Specialty Stores                  |
        | Ticker: MICT  | Sector: Technology            | Industry: EDP Peripherals                         |
        | Ticker: ROL   | Sector: Technology            | Industry: Industrial Specialties                  |
        | Ticker: JCOM  | Sector: Technology            | Industry: Telecommunications Equipment            |
        | Ticker: ZTO   | Sector: Transportation        | Industry: Advertising                             |
        | Ticker: GATX  | Sector: Transportation        | Industry: Rental/Leasing Companies                |
     */
}
