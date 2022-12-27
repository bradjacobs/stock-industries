package com.github.bradjacobs.stock.classifications.tradingview;

import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Sectors based on TradingView data.
 *     NOT at all official.  more for curiosity.
 */
public class TradingViewDataConverter implements DataConverter<TradingViewRecord>
{
    private static final int INDUSTRY_COLUMN_INDEX = 0;
    private static final int SECTOR_COLUMN_INDEX = 5;

    @Override
    public Classification getClassification()
    {
        return Classification.TRADINGVIEW;
    }

    @Override
    public List<TradingViewRecord> createDataRecords() throws IOException {
        List<TradingViewRecord> recordList = new ArrayList<>();

        String htmlData = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());
        Document doc = Jsoup.parse(htmlData);
        // *** NOTE ***  "Assuming" there's only 1 table !!
        Elements panelsElementsCollection = doc.getElementsByTag("table");
        if (panelsElementsCollection.size() == 0) {
            throw new InternalError("Unable to find primary table.");
        }

        Map<String, Set<String>> sectorIndustryMap = new TreeMap<>();
        Element tableElement = panelsElementsCollection.get(0);
        Elements rowElements = tableElement.getElementsByTag("tr");

        for (Element rowElement : rowElements) {
            Elements cellElements = rowElement.getElementsByTag("td");

            if (cellElements.size() > INDUSTRY_COLUMN_INDEX && cellElements.size() > SECTOR_COLUMN_INDEX) {
                String industryName = cellElements.get(INDUSTRY_COLUMN_INDEX).text().trim();
                String sectorName = cellElements.get(SECTOR_COLUMN_INDEX).text().trim();
                Set<String> industrySet = sectorIndustryMap.computeIfAbsent(sectorName, k -> new TreeSet<>());
                industrySet.add(industryName);
            }
        }

        // counters are used to give 'pseudo' id values.
        int sectorCounter = 0;
        int industryCounter = 0;

        for (Map.Entry<String, Set<String>> sectorEntry : sectorIndustryMap.entrySet()) {
            String sector = sectorEntry.getKey();
            String sectorId = String.valueOf(++sectorCounter);

            Set<String> industrySet = sectorEntry.getValue();
            industryCounter = 0;
            for (String industry : industrySet) {
                String industryId = String.format("%s.%d", sectorId, ++industryCounter);
                recordList.add(new TradingViewRecord(sectorId, sector, industryId, industry));
            }
        }
        return recordList;
    }
}
