package com.github.bradjacobs.stock.classifications.sic;


//  todo - document the similarities/difference of data from these links.
// alternate found at:
// https://www.osha.gov/data/sic-manual
// https://www.sec.gov/info/edgar/siccodes.htm
// https://www.gov.uk/government/publications/standard-industrial-classification-of-economic-activities-sic
// http://www.ehso.com/siccodes.php
// https://www.state.nj.us/dep/aqm/es/sic.pdf
//
// https://www.dietrich-direct.com/SIC-Code-Reference-Access.htm

import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.util.DownloadUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SicDataConverter extends BaseDataConverter<SicRecord>
{
    private static final String DIVISION_TITLE_PREFIX = "Division ";
    private static final String MAJOR_GROUP_TITLE_PREFIX = "Major Group ";
    private static final String INDUSTRY_GROUP_TITLE_PREFIX = "Industry Group ";

    private static final String BASE_URL = "https://www.osha.gov";


    @Override
    public Classification getClassification()
    {
        return Classification.SIC;
    }

    @Override
    public List<SicRecord> createDataRecords() throws IOException
    {
        // note: only 'industryIdToNameMap' really needs sorting
        //   (but it's not hurting leaving all the same here)
        Map<String,String> divisionIdToNameMap = new TreeMap<>();
        Map<String,String> majorGroupIdToNameMap = new TreeMap<>();
        Map<String,String> majorGroupIdDivsionIdMap = new TreeMap<>();
        Map<String,String> industryGroupIdToNameMap = new TreeMap<>();
        Map<String,String> industryIdToNameMap = new TreeMap<>();

        Map<String,String> majorGroupUrlMap = new LinkedHashMap<>();


        // first parse the main document for the Division & MajorGroups
        //   AMD...
        // capture URL links of all the majorGroup sub pages.

        // fetch the 'main page' html
        String oshaHtml = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());
        Document oshaDoc = Jsoup.parse(oshaHtml);

        Elements divisionLinkElements = oshaDoc.getElementsByAttributeValueStarting("title", DIVISION_TITLE_PREFIX);

        for (Element divsionLinkElement : divisionLinkElements)
        {
            String divisionLinkTitle = divsionLinkElement.attr("title");

            int divisionColonIndex = divisionLinkTitle.indexOf(":");
            String divisionId = divisionLinkTitle.substring(DIVISION_TITLE_PREFIX.length(), divisionColonIndex);
            String divisionName = cleanValue(divisionLinkTitle.substring(divisionColonIndex+1));
            divisionIdToNameMap.put(divisionId, divisionName);

            // now from the 'division link', go up 2 levels, then fetch all the links in scope.
            //  this will be all the major groups to be affiliated w/ the division.
            Element parentParent = divsionLinkElement.parent().parent();

            Elements majorGroupLinkElements = parentParent.getElementsByAttributeValueStarting("title", MAJOR_GROUP_TITLE_PREFIX);
            for (Element majorGroupLinkElement : majorGroupLinkElements)
            {
                String majorGroupLinkTitle = majorGroupLinkElement.attr("title");
                int majorGroupColonIndex = majorGroupLinkTitle.indexOf(":");
                String majorGroupId = majorGroupLinkTitle.substring(MAJOR_GROUP_TITLE_PREFIX.length(), majorGroupColonIndex);
                String majorGroupName = cleanValue(majorGroupLinkTitle.substring(majorGroupColonIndex+1));
                majorGroupIdToNameMap.put(majorGroupId, majorGroupName);

                // capture links to visit
                String href = majorGroupLinkElement.attr("href");
                String fullUrl = BASE_URL + href;
                majorGroupUrlMap.put(majorGroupId, fullUrl);

                // also need to track majorgroup - division b/c can't tell based on the naming structure.
                majorGroupIdDivsionIdMap.put(majorGroupId, divisionId);
            }
        }


        // now.... visit each link and capture all the industry group + inudstry info
        //    side note:  would have to dive another level of web pages in order to
        //       grab full description of the industryId/Name  (not really worth it at present)

        for (Map.Entry<String, String> urlEntry : majorGroupUrlMap.entrySet())
        {
            String majorGroupId = urlEntry.getKey();

            // slight pause to be kind.
            try { Thread.sleep(250L); }
            catch (InterruptedException e) { /* ignore */}

            String url = urlEntry.getValue();
            //System.out.println("Fetching URL: " + url);

            String indusryGroupHtml = DownloadUtil.downloadFile(url);

            Document industryGroupDoc = Jsoup.parse(indusryGroupHtml);

            Elements pElements = industryGroupDoc.getElementsByTag("p");
            for (Element pElement : pElements)
            {
                String text = pElement.text();
                if (text.startsWith(INDUSTRY_GROUP_TITLE_PREFIX))
                {
                    int colonIndex = text.indexOf(":");
                    String industryGroupId = text.substring(INDUSTRY_GROUP_TITLE_PREFIX.length(), colonIndex);
                    String name = cleanValue(text.substring(colonIndex+1));
                    industryGroupIdToNameMap.put(industryGroupId, name);
                }
            }

            Elements industryLinkElements = industryGroupDoc.getElementsByAttributeValueStarting("title", majorGroupId);
            for (Element industryLinkElement : industryLinkElements)
            {
                String industryLinkTitle = industryLinkElement.attr("title");
                String industryId = industryLinkTitle.substring(0, 4);
                String industryName = cleanValue(industryLinkElement.text());
                industryIdToNameMap.put(industryId, industryName);
            }
        }


        // finally... reassemble everything.
        List<SicRecord> sicRecords = new ArrayList<>();

        for (Map.Entry<String, String> industryIdNameEntry : industryIdToNameMap.entrySet())
        {
            SicRecord record = new SicRecord();

            String industryId = industryIdNameEntry.getKey();
            String industryName = industryIdNameEntry.getValue();
            record.setIndustryId(industryId);
            record.setIndustryName(industryName);

            String industryGroupId = industryId.substring(0, industryId.length()-1);  // strip off last character
            String industryGroupName = industryGroupIdToNameMap.get(industryGroupId);
            record.setIndustryGroupId(industryGroupId);
            record.setIndustryGroupName(industryGroupName);

            String majorGroupId = industryGroupId.substring(0, industryGroupId.length()-1);  // strip off last character
            String majorGroupName = majorGroupIdToNameMap.get(majorGroupId);
            record.setMajorGroupId(majorGroupId);
            record.setMajorGroupName(majorGroupName);

            String divisionId = majorGroupIdDivsionIdMap.get(majorGroupId);
            String divisionName = divisionIdToNameMap.get(divisionId);
            record.setDivisionId(divisionId);
            record.setDivisionName(divisionName);
            sicRecords.add(record);
        }

        return sicRecords;
    }

}
