package com.github.bradjacobs.stock.classifications.zacks;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;
import com.github.bradjacobs.stock.util.PdfUtil;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// alternate definition location:
//    http://www.zacksdata.com/app/download/247340904/Zacks+Sector+Industry+Mapping+Scheme.pdf

//  SIDE NODE:
//     record '180' (Textile - Apparel) has a different sector depending upon the data source
//        i.e.   1-Consumer Staples   vs  2-Consumer Discretionary
public class ZacksDataConverter implements DataConverter<ZacksRecord>
{
    private static final String NESTED_JSON_PREFIX = "\"data\"  : ";

    @Override
    public Classification getClassification() {
        return Classification.ZACKS;
    }

    @Override
    public List<ZacksRecord> createDataRecords() throws IOException
    {
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
        for (ZacksRecord record : recordArray)
        {
            record.setSectorName( cleanUpName(record.getSectorName()) );
            record.setMediumIndustryName( cleanUpName(record.getMediumIndustryName()) );
            record.setExpandedIndustryName( cleanUpName(record.getExpandedIndustryName()) );
            resultList.add(record);
        }

        Collections.sort(resultList);

        // remove a "0" record if it exists
        ZacksRecord firstRecord = resultList.get(0);
        if (firstRecord.getSectorCode().equals("0")) {
            resultList.remove(0);
        }

        return resultList;
    }

    private String cleanUpName(String inputName) {
        String extractedName = extractTitleFromSpanTag(inputName);
        return cleanValue(extractedName);
    }


    private String extractTitleFromSpanTag(String str)
    {
        String extractedTitle = StringUtils.substringBetween(str, "title=\"", "\"");
        if (extractedTitle == null) {
            extractedTitle = str;
        }
        return extractedTitle;
    }

    public String extractNestedJson(String html)
    {
        return StringUtils.substringBetween(html, NESTED_JSON_PREFIX, "\n");
    }


    private static final Map<String,String> CUSTOM_POST_CLEAN_SUB_MAP = new LinkedHashMap<String,String>() {{
        put("And And ", "And ");              // fix data with a 'double and'
        put(" Rual", " Rural");               // fix data typo
        put(" Non - Us", " Non-US");          // one-off format change
        put(" R & D ", " R&D ");              // one-off format change
        put(" Elec Pwr", " Electric Power");  // one-off format change
        put(" Non Ferrous", " Non-Ferrous");  // one-off format change
        put("Chem ", "Chemical ");            // one-off format change
        put(" Whole Sales", " Wholesale");    // appears to be typo
        put(" Reit ", " REIT ");              // adjust caps on acronym
        put(" Rv ", " RV ");                  // adjust caps on acronym
        put(" Hmos", " HMOs");                // adjust caps on acronym
        put(" It ", " IT ");                  // adjust caps on acronym
        put(" Mlb", " MLP");                  // adjust caps on acronym (and fix typo)
        put(" Master Limited Partnerships", " MLP");  // use acroynm instead

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

        result = WordUtils.capitalizeFully(result);  // todo - update library for recent func

        result = StringUtils.replace(result, " And ", " & ");

        // remove back teh spaces that were added.
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



    // how to read from http://www.zacksdata.com/app/download/247340904/Zacks+Sector+Industry+Mapping+Scheme.pdf
    //  for reference
//    public List<ZacksRecord> createDataRecords_Alternate() throws IOException
//    {
//        String filePath = "";
//        String[] pdfFileLines = PdfUtil.getPdfFileLines(new File(filePath));
//
//        List<ZacksRecord> resultList = new ArrayList<>();
//        for (int i = 0; i < pdfFileLines.length; i++)
//        {
//            String line = pdfFileLines[i].trim();
//            String[] tokens = line.split(" ");
//            if (tokens.length >= 6 && StringUtils.isNumeric(tokens[0])) {
//                if (tokens[1].equals("of")) {
//                    continue;
//                }
//
//                String xIndustryCode = tokens[0];
//                String mIndustryCode = "";
//                String sectorCode = "";
//
//                List<String> xIndustryTokens = new ArrayList<>();
//                List<String> mIndustryTokens = new ArrayList<>();
//                List<String> sectorTokens = new ArrayList<>();
//
//                List<String> currentList = xIndustryTokens;
//
//                for (int j = 1; j < tokens.length; j++)
//                {
//                    String token = tokens[j];
//                    if (StringUtils.isNumeric(token)) {
//                        if (mIndustryCode.equals("")) {
//                            mIndustryCode = token;
//                            currentList = mIndustryTokens;
//                        }
//                        else {
//                            sectorCode = token;
//                            currentList = sectorTokens;
//                        }
//                    }
//                    else {
//                        currentList.add(token);
//                    }
//                }
//
//                String xIndustryName = String.join(" ", xIndustryTokens.toArray(new String[0]));
//                String mIndustryName = String.join(" ", mIndustryTokens.toArray(new String[0]));
//                String sectorName = String.join(" ", sectorTokens.toArray(new String[0]));
//
//                ZacksRecord record = new ZacksRecord();
//                record.setSectorName(sectorName);
//                record.setSectorCode(sectorCode);
//                record.setMediumIndustryName(mIndustryName);
//                record.setMediumIndustryCode(mIndustryCode);
//                record.setExpandedIndustryName(xIndustryName);
//                record.setExpandedIndustryCode(xIndustryCode);
//                resultList.add(record);
//            }
//        }
//
//        Collections.sort(resultList);
//        return resultList;
//    }

}
