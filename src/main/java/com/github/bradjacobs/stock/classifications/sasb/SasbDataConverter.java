package com.github.bradjacobs.stock.classifications.sasb;

import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import com.github.bradjacobs.stock.util.StringUtil;
import com.github.bradjacobs.stock.util.UrlUtil;
import com.sun.javafx.fxml.builder.URLBuilder;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SasbDataConverter implements DataConverter<SasbRecord>
{
    private static final String PANELS_CLASS = "vc_tta-panels";

    private static final URL DESCRIPTION_URL = UrlUtil.createURL("https://www.sasb.org/standards/download/");
    private static final String DESCRIPTION_LABEL_CLASS = "hs-form-booleancheckbox-display";
    private static final String DESCRIPTION_CLASS = "single-company-description";

    @Override
    public Classification getClassification()
    {
        return Classification.SASB;
    }


    @Override
    public List<SasbRecord> createDataRecords() throws IOException
    {
        String htmlData = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        Document doc = Jsoup.parse(htmlData);

        Elements panelsElementsCollection = doc.getElementsByClass(PANELS_CLASS);

        if (panelsElementsCollection.size() == 0) {
            throw new InternalError("Unable to find primary class name: " + PANELS_CLASS);
        }

        Element panelElements = panelsElementsCollection.get(0);

        List<SasbRecord> recordList = new ArrayList<>();

        for (Element childSectorElement : panelElements.children())
        {
            // note: assuming there's only ONE table for the child sector element
            Elements innerTableRowElements = childSectorElement.getElementsByTag("tr");


            SasbRecord prevRecord = new SasbRecord();

            int rowCount = innerTableRowElements.size();

            // note: start at row index 1 (skip header row)
            for (int j = 1; j < rowCount; j++)
            {
                Element tableRowElement = innerTableRowElements.get(j);
                SasbRecord record = createRecord(tableRowElement, prevRecord);
                recordList.add(record);
                prevRecord = record;
            }
        }

        // fetch descriptions (from a different page) and append to the records
        appendDescriptions(recordList);


        return recordList;
    }


    private SasbRecord createRecord(Element tableRowElement, SasbRecord prevRecord)
    {
        // NOTE assume there's always __6__ columns
        Elements cellElements = tableRowElement.getElementsByTag("td");

        String sectorId = getCellTextOrDefault(cellElements.get(0), prevRecord.getSectorId());
        String sectorName = getCellTextOrDefault(cellElements.get(1), prevRecord.getSectorName());
        String subSectorId = getCellTextOrDefault(cellElements.get(2), prevRecord.getSubSectorId());
        String subSectorName = getCellTextOrDefault(cellElements.get(3), prevRecord.getSectorName());
        String industryId = getCellTextOrDefault(cellElements.get(4), prevRecord.getIndustryId());
        String industryName = getCellTextOrDefault(cellElements.get(5), prevRecord.getIndustryName());

        return new SasbRecord(sectorId, sectorName, subSectorId, subSectorName, industryId, industryName);
    }


    private String getCellTextOrDefault(Element cellElement, String defaultValue) {
        String value = StringUtil.cleanWhitespace(cellElement.text());
        if (StringUtils.isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }


    private void appendDescriptions(List<SasbRecord> recordList) throws IOException
    {
        String htmlData = DownloadUtil.downloadFile(DESCRIPTION_URL);

        Document doc = Jsoup.parse(htmlData);

        Elements industryLabelElements = doc.getElementsByClass(DESCRIPTION_LABEL_CLASS);
        Elements industryDescriptionElements = doc.getElementsByClass(DESCRIPTION_CLASS);

        if (industryLabelElements.size() != industryDescriptionElements.size()) {
            throw new InternalError("Unable to get descriptions: element size mismatch");
        }

        Map<String,String> descriptionLookupMap = new HashMap<>();

        int industryCount = industryLabelElements.size();
        for (int i = 0; i < industryCount; i++)
        {
            String industryName = StringUtil.cleanWhitespace(industryLabelElements.get(i).text());
            String description = industryDescriptionElements.get(i).text().trim();
            descriptionLookupMap.put(industryName.toLowerCase(), description);
        }

        for (SasbRecord record : recordList)
        {
            String description = descriptionLookupMap.get(record.getIndustryName().toLowerCase());
            record.setDescription(description);
        }
    }

}
