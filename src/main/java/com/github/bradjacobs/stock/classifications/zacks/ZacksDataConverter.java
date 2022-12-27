package com.github.bradjacobs.stock.classifications.zacks;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// alternate definition location:
//    http://www.zacksdata.com/app/download/247340904/Zacks+Sector+Industry+Mapping+Scheme.pdf

//  SIDE NODE:
//     record '180' (Textile - Apparel) has a different sector depending upon the data source
//        i.e.   1-Consumer Staples   vs  2-Consumer Discretionary

// TODO - source data bug... 234 should be "Wireline - Regional - Rural" .... but can get value of WIRELESS
public class ZacksDataConverter implements DataConverter<ZacksRecord>
{
    private static final String NESTED_JSON_PREFIX = "\"data\"  : ";

    // todo - used to alter the ids to make unique for the json tree generator
    //    can come back for better soln so this isn't needed later.
    private static final String S_NUM_FORMAT_STR = "%02d";
    private static final String M_NUM_FORMAT_STR = "%02d";
    private static final String X_NUM_FORMAT_STR = "%03d";

    @Override
    public Classification getClassification() {
        return Classification.ZACKS;
    }

    @Override
    public List<ZacksRecord> createDataRecords() throws IOException {
        // download html page..
        String html = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        // grab the neested json within the page
        String json = extractNestedJson(html);

        // convert the JSON to ZacksRecords  (the 'ZacksRecord' has the header alias definitions)
        JsonMapper mapper = MapperBuilder.json().build();
        ZacksRecord[] recordArray = mapper.readValue(json, ZacksRecord[].class);

        // even though now have records, the names are in a bad format (html cruft)
        //  so now iterate and clean up the name on each record.
        List<ZacksRecord> resultList = new ArrayList<>();
        for (ZacksRecord record : recordArray) {
            record.setSectorName( cleanUpName(record.getSectorName()) );
            record.setMediumIndustryName( cleanUpName(record.getMediumIndustryName()) );
            record.setExpandedIndustryName( cleanUpName(record.getExpandedIndustryName()) );
            resultList.add(record);
        }

        // TODO - TO FIX LATER
        //   currently some json tree building code relies on each id be unique (regardless of level)
        //   (see comment at bottom for example)
        //   for now will 'fake it' by adding a prefix to ensure uniqueness
        for (ZacksRecord record : resultList) {
            String s = String.format(S_NUM_FORMAT_STR, Integer.parseInt(record.getSectorCode()));
            String m = String.format(M_NUM_FORMAT_STR, Integer.parseInt(record.getMediumIndustryCode()));
            String x = String.format(X_NUM_FORMAT_STR, Integer.parseInt(record.getExpandedIndustryCode()));

            record.setSectorCode(s);
            record.setMediumIndustryCode(s + m);
            record.setExpandedIndustryCode(s + m + x);
        }

        Collections.sort(resultList);

        // remove a "0" record if it exists
        ZacksRecord firstRecord = resultList.get(0);
        if (firstRecord.getSectorCode().equals("0") || firstRecord.getSectorCode().equals("00")) {
            resultList.remove(0);
        }

        return resultList;
    }

    private String cleanUpName(String inputName) {
        String extractedName = extractTitleFromSpanTag(inputName);
        return cleanValue(extractedName);
    }

    private String extractTitleFromSpanTag(String str) {
        String extractedTitle = StringUtils.substringBetween(str, "title=\"", "\"");
        if (extractedTitle == null) {
            extractedTitle = str;
        }
        return extractedTitle;
    }

    public String extractNestedJson(String html) {
        return StringUtils.substringBetween(html, NESTED_JSON_PREFIX, "\n");
    }

    // todo: mess to deal with at later date.
    private static final Map<String,String> CUSTOM_POST_CLEAN_SUB_MAP = new LinkedHashMap<String,String>() {{
        put("And And ", "And ");              // fix data with a 'double and'
        put(" Rual", " Rural");               // fix data typo
        put(" Non - Us", " Non-US");          // one-off format change
        put(" R & D ", " R&D ");              // one-off format change
        put(" Elec Pwr", " Electric Power");  // one-off format change
        put(" Non Ferrous", " Non-Ferrous");  // one-off format change
        put("Chem ", "Chemical ");            // one-off format change
        put(" Whole Sales", " Wholesale");    // appears to be typo
        put("Reit ", "REIT ");                // adjust caps on acronym
        put(" Rv ", " RV ");                  // adjust caps on acronym
        put(" Hmos", " HMOs");                // adjust caps on acronym
        put(" It ", " IT ");                  // adjust caps on acronym
        put(" Sbic ", " SBIC ");              // adjust caps on acronym
        put(" Mlb", " MLP");                  // adjust caps on acronym (and fix typo)
        put(" Master Limited Partnerships", " MLP");  // use acronym instead

        put(" Bkrs ", " Bankers ");           // custom change (b/c orig value subjectively looks odd imho)
        put(" Mgrs", " Managers");            // custom change (b/c orig value subjectively looks odd imho)
    }};

    /**
     * Going thru arguably too much pain to make all strings look 'pretty'
     */
    private String cleanValue(String input) {
        String result = input;
        // add spaces around characters to allow for capitialization fomratting
        //    (note:  alt impl is required if performance becomes important)
        result = StringUtils.replace(result, "&", " & ");
        result = StringUtils.replace(result, "-", " - ");
        result = StringUtils.replace(result, "/", " / ");
        result = StringUtils.replace(result, "(", "( ");

        result = WordUtils.capitalizeFully(result);

        result = StringUtils.replace(result, " And ", " & ");

        // remove the spaces that were added.
        result = StringUtils.replace(result, "( ", "(");
        result = result.replaceAll("\\s+/\\s+", "/");
        result = result.replaceAll("\\s+-\\s+", " - ");
        result = result.replaceAll("\\s+&\\s+", " & ");

        // special extra substitutions
        for (Map.Entry<String, String> entry : CUSTOM_POST_CLEAN_SUB_MAP.entrySet()) {
            result = StringUtils.replace(result, entry.getKey(), entry.getValue());
        }

        // one last special case
        if (result.endsWith("Distr")) {
            result = StringUtils.replace(result, "Distr", "Distribution");
        }

        return result;
    }

    /*
        NOTES: below is an example of the same "id" used at different levels.

        "sectorCode" : "8",   <-----------------
        "sectorName" : "Construction",

        "sectorCode" : "2",
        "sectorName" : "Consumer Discretionary",
        "mediumIndustryCode" : "8",      <-----------------
        "mediumIndustryName" : "Home Furnishing - Appliance",

        "sectorCode" : "5",
        "sectorName" : "Auto, Tires & Trucks",
        "mediumIndustryCode" : "18",
        "mediumIndustryName" : "Autos - Tires - Trucks",
        "expandedIndustryCode" : "8",       <-----------------
        "expandedIndustryName" : "Automotive - Foreign"
     */
}
