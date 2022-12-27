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

import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import com.github.bradjacobs.stock.util.StringUtil;
import com.github.bradjacobs.stock.util.UrlUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class SicDataConverter implements DataConverter<SicRecord>
{
    private static final String DIVISION_TITLE_PREFIX = "Division ";
    private static final String MAJOR_GROUP_TITLE_PREFIX = "Major Group ";
    private static final String INDUSTRY_GROUP_TITLE_PREFIX = "Industry Group ";

    private static final String BASE_URL = "https://www.osha.gov";

    // NOTE:  found an actual 'typo' on one of the pages, which interferes w/ the parsing
    //    specifically incorrect IndustryGroup title on https://www.osha.gov/data/sic-manual/major-group-94
    //  thus the "fix" is to do a special substitution (which is kludgy)
    //
    //  map KEY: incorrect string --> VALUE: correct string
    private static final Map<String,String> INDUSTRY_GROUP_SUBSTITUTION_MAP =
        Collections.singletonMap("9431 Administration of Public Health Programs", "Industry Group 944: Administration of Social, Human Resource and Income Maintenance Programs");

    @Override
    public Classification getClassification()
    {
        return Classification.SIC;
    }

    @Override
    public List<SicRecord> createDataRecords() throws IOException {
        // note: only 'industryIdToNameMap' really needs sorting
        //   (but it's not hurting leaving all the same here)
        Map<String,String> divisionIdToNameMap = new TreeMap<>();
        Map<String,String> majorGroupIdToNameMap = new TreeMap<>();
        Map<String,String> majorGroupIdDivisionIdMap = new TreeMap<>();
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

        for (Element divisionLinkElement : divisionLinkElements) {
            String divisionLinkTitle = divisionLinkElement.attr("title");
            int divisionColonIndex = divisionLinkTitle.indexOf(":");
            String divisionId = divisionLinkTitle.substring(DIVISION_TITLE_PREFIX.length(), divisionColonIndex);
            String divisionName = cleanValue(divisionLinkTitle.substring(divisionColonIndex+1));
            divisionIdToNameMap.put(divisionId, divisionName);

            // now from the 'division link', go up 2 levels, then fetch all the links in scope.
            //  this will be all the major groups to be affiliated w/ the division.
            Element parentParent = divisionLinkElement.parent().parent();

            Elements majorGroupLinkElements = parentParent.getElementsByAttributeValueStarting("title", MAJOR_GROUP_TITLE_PREFIX);
            for (Element majorGroupLinkElement : majorGroupLinkElements) {
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
                majorGroupIdDivisionIdMap.put(majorGroupId, divisionId);
            }
        }

        // now.... visit each link and capture all the industry group + industry info
        //    side note:  would have to dive another level of web pages in order to
        //       grab full description of the industryId/Name  (not really worth it at present)
        for (Map.Entry<String, String> urlEntry : majorGroupUrlMap.entrySet()) {
            String majorGroupId = urlEntry.getKey();
            URL url = UrlUtil.createURL(urlEntry.getValue());

            // slight pause to be kind.
            try { Thread.sleep(250L); }
            catch (InterruptedException e) { /* ignore */}

            //System.out.println("Fetching URL: " + url);
            String industryGroupHtml = DownloadUtil.downloadFile(url);
            Document industryGroupDoc = Jsoup.parse(industryGroupHtml);

            Elements pElements = industryGroupDoc.getElementsByTag("p");
            for (Element pElement : pElements) {
                String text = pElement.text();

                // kludge!!  handling 'typo' on one of the webpages
                String alternateText = INDUSTRY_GROUP_SUBSTITUTION_MAP.get(text);
                if (alternateText != null) {
                    text = alternateText;
                }

                if (text.startsWith(INDUSTRY_GROUP_TITLE_PREFIX)) {
                    int colonIndex = text.indexOf(":");
                    String industryGroupId = text.substring(INDUSTRY_GROUP_TITLE_PREFIX.length(), colonIndex);
                    String name = cleanValue(text.substring(colonIndex+1));
                    industryGroupIdToNameMap.put(industryGroupId, name);
                }
            }

            Map<String,String> currentPageIndustryIdToNameMap = new HashMap<>();

            // now fetch all the industries.   note: these elements are _not_ nested inside the industryGroup elements.
            Elements industryLinkElements = industryGroupDoc.getElementsByAttributeValueStarting("title", majorGroupId);
            for (Element industryLinkElement : industryLinkElements) {
                String industryLinkTitle = industryLinkElement.attr("title");
                String industryId = industryLinkTitle.substring(0, 4);
                String industryName = cleanValue(industryLinkElement.text());
                industryIdToNameMap.put(industryId, industryName);
                currentPageIndustryIdToNameMap.put(industryId, industryName);
            }

            // extra trickery...
            //   There are some cases where the industry group string was truncated incorrectly
            //      (e.g. it actually looks incorrect on the original html page)
            //      (example: "Industry Group 945: Administration Of Veteran's Affairs, Except", from https://www.osha.gov/data/sic-manual/major-group-94
            //       note it abruptly ends w/ "Except")
            // Thus will attempt to see if can create a better alternate name (if applicable and available)
            Map<String,String> alternateIndustryGroupMap = createAlternateIndustryGroupTitleMap(currentPageIndustryIdToNameMap);
            for (Map.Entry<String, String> entry : alternateIndustryGroupMap.entrySet()) {
                String industryGroup = entry.getKey();
                String industryGroupAlternateTitle = entry.getValue();
                String industryGroupCurrentTiTle = industryGroupIdToNameMap.get(industryGroup);

                if (industryGroupCurrentTiTle != null && industryGroupAlternateTitle != null && industryGroupAlternateTitle.length() > industryGroupCurrentTiTle.length()) {
                    industryGroupIdToNameMap.put(industryGroup, industryGroupAlternateTitle);
                }
            }
        }

        // finally... reassemble everything.
        List<SicRecord> sicRecords = new ArrayList<>();
        for (Map.Entry<String, String> industryIdNameEntry : industryIdToNameMap.entrySet()) {
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

            String divisionId = majorGroupIdDivisionIdMap.get(majorGroupId);
            String divisionName = divisionIdToNameMap.get(divisionId);
            record.setDivisionId(divisionId);
            record.setDivisionName(divisionName);
            sicRecords.add(record);
        }

        return sicRecords;
    }

    // todo: come back and refactor b/c even though the method below works, it's a little magical.
    /**
     * extra trickery...
     *    There are some cases where the industry group string is incorrect on the original html page
     *       (example: "Industry Group 945: Administration Of Veteran's Affairs, Except", from https://www.osha.gov/data/sic-manual/major-group-94
     *     To partially handle this, observation has shown that if an industry group has EXACTLY ONE industry,
     *      then the industry group and the industry should have the same name.
     *     Thus use this fact to 'fix' some incorrect industry groups if possible.
     *
     * Given a map of industryIds/titles, return a map of CANDIDATE industryGroupId->title values
     * @param industryIdToNameMap
     * @return
     */
    private Map<String,String> createAlternateIndustryGroupTitleMap(Map<String,String> industryIdToNameMap) {
        // create a simple map of industryGroup -> list of its industries.
        Map<String,List<String>> industryGroupToIndustryMap = new HashMap<>();
        List<String> industryIds = new ArrayList<>(industryIdToNameMap.keySet());

        for (String industryId : industryIds) {
            String industryGroup = industryId.substring(0, industryId.length()-1);
            List<String> industryList = industryGroupToIndustryMap.computeIfAbsent(industryGroup, k -> new ArrayList<>());
            industryList.add(industryId);
        }

        // now make a new map of industryGroupId -> title
        //     which will ONLY be populated if there was only 1 industry for the group.
        Map<String,String> resultMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : industryGroupToIndustryMap.entrySet()) {
            String industryGroupId = entry.getKey();
            List<String> industryIdList = entry.getValue();

            if (industryIdList.size() == 1) {
                String industryId = industryIdList.get(0);
                String industryTitle = industryIdToNameMap.get(industryId);
                resultMap.put(industryGroupId, industryTitle);
            }
        }
        return resultMap;
    }

    private String cleanValue(String input) {
        return StringUtil.cleanWhitespace(input);
    }

    // todo: FOR REFERENCE -- decide later what to do with some 'extra' sic code that popup up in circulation, but are not generated from the data source.
//    private static final List<SicRecord> EXTRA_RECORDS = Arrays.asList(
//            new SicRecord("", "", "", "", "", "", "3576", "Computer Communications Equipment"),
//            new SicRecord("", "", "", "", "", "", "4955", "Hazardous Waste Management"),
//            new SicRecord("", "", "", "", "", "", "4991", "Cogeneration Services and Small Power Producers"),
//            new SicRecord("", "", "", "", "", "", "5412", "Convenience Stores"),
//            new SicRecord("", "", "", "", "617", "????", "6172", "Finance Lessors"),
//            new SicRecord("", "", "", "", "617", "????", "6189", "Asset-backed Securities"),
//            new SicRecord("", "", "", "", "617", "????", "6199", "Finance Services"),
//            new SicRecord("", "", "", "", "", "", "6532", "Real Estate Dealers (for Their Own Account)"),
//            new SicRecord("", "", "", "", "677", "????", "6770", "Blank Checks"),
//            new SicRecord("", "", "", "", "", "", "6795", "Mineral Royalty Traders"),
//            new SicRecord("", "", "", "", "", "", "7385", "Telephone Interconnect Systems"),
//            new SicRecord("", "", "", "", "888", "American Depositary Receipts", "8888", "Foreign Governments"),
//            new SicRecord("", "", "", "", "", "", "9995", "Non-operating Establishments")
//    );

}
