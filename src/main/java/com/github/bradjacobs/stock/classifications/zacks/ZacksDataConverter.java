package com.github.bradjacobs.stock.classifications.zacks;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.util.DownloadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZacksDataConverter extends BaseDataConverter<ZacksRecord>
{
    @Override
    public Classification getClassification()
    {
        return Classification.ZACKS;
    }

    @Override
    public List<ZacksRecord> createDataRecords() throws IOException
    {
        // download html page..
        String html = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        // grab the nextes json within the page
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

        return resultList;
    }

    private String cleanUpName(String inputName) {
        String extractedName = extractTitleFromSpanTag(inputName);
        return cleanValue(extractedName);
    }


    // todo - revisit a better way
    protected String cleanValue(String input) {
        String result = super.cleanValue(input);

        result = capitalizeLetterAfterDash(result);
        result = result.replace("Exploration&production", "Exploration & Production");  // z
        result = result.replace("Oil&gas", "Oil & Gas");  // z

        result = result.replace(" Whole Sales", " Wholesale");  // z
        result = result.replace("Elec Pwr", "Electric Power");  // z
        result = result.replace("Utility-Gas Distr", "Utility-Gas Distribution");  // z
        result = result.replace(" And And ", " and ");  // z
        result = result.replace(" Rual", " Rural");  // z


        result = result.replace(" And ", " and ");
        result = result.replace(" and ", " & ");
        result = result.replace(" R & D ", " R&D ");  // note: was originally "... R And D Services ..."

        result = result.replace("Reit ", "REIT ");
        result = result.replace(" Rv ", " RV ");
        result = result.replace(" Mlb", " MLB");
        result = result.replace("-Us", "-US");
        result = result.replace(" Sbic", " SBIC");
        result = result.replace(" Hmos", " HMOs");
        result = result.replace(" It ", " IT ");

        // redo the super in case our replacements messed up whitespace (cautious/pedantic)
        result = super.cleanValue(result);

        return result;
    }


    // todo -- not the best all cases, i.e. "non-ferrous"
    protected String capitalizeLetterAfterDash(String input)
    {
        String result = input;
        // capitalize a letter immediately following a dash
        String regex = "(-[a-z])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(result);

        while (matcher.find()) {
            result = result.replaceFirst(matcher.group(), matcher.group(1).toUpperCase());
        }
        return result;
    }

    private String extractTitleFromSpanTag(String str)
    {
        int titleIndex = str.indexOf("title=\"");
        if (titleIndex > 0)
        {
            int startQuote = str.indexOf("\"", titleIndex);
            int endQuote = str.indexOf("\"", startQuote+1);

            return str.substring(startQuote+1, endQuote);
        }
        else {
            return str;
        }
    }


    public String extractNestedJson(String html)
    {
        String[] lines = html.split("\n");

        String magicLine = null;

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("\"data\" ") && line.contains("Sector Group"))
            {
                magicLine = line;
                break;
            }
        }

        String json = magicLine;
        if (json == null) {
            throw new InternalError("Unable to find data within file.");
        }

        int firstBracketIndex = json.indexOf('[');
        if (firstBracketIndex > 0)
        {
            json = json.substring(firstBracketIndex);
        }

        int lastBracketIndex = json.lastIndexOf(']');
        if (lastBracketIndex > 0)
        {
            json = json.substring(0, lastBracketIndex+1);
        }

        return json;
    }

}
