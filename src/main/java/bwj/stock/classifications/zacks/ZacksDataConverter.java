package bwj.stock.classifications.zacks;

import bwj.stock.classifications.BaseDataConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZacksDataConverter extends BaseDataConverter<ZacksRecord>
{
    private static final String SOURCE_FILE = "https://www.zacks.com/zrank/sector-industry-classification.php";

    private static final String KEY_SECTOR_NAME = "Sector Group";
    private static final String KEY_SECTOR_CODE = "Sector Code";
    private static final String KEY_MEDIUM_NAME = "Medium(M) Industry Group";
    private static final String KEY_MEDIUM_CODE = "Medium(M) Industry Code";
    private static final String KEY_EXPANDED_NAME = "Expanded(X) Industry Group";
    private static final String KEY_EXPANDED_CODE = "Expanded(X) Industry Code";

    @Override
    public String getFilePrefix()
    {
        return "zacks";
    }

    @Override
    public List<ZacksRecord> generateDataRecords() throws IOException
    {
        String html = getSourceFileContents();
        String json = extractNestedJson(html);

        List<Map<String, String>> listOfMaps = convertToListOfMaps(json);

        List<ZacksRecord> recordList = new ArrayList<>();

        for (Map<String, String> recordMap : listOfMaps) {

            ZacksRecord record = generateRecord(recordMap);

            //   the '0' is an odd 'index' sector
            //if (record.getSectorCode().equals("0")) {
            //    continue;
            //}

            recordList.add(record);
        }

        Collections.sort(recordList);

        return recordList;
    }


    private ZacksRecord generateRecord(Map<String, String> recordMap)
    {
        String sectorGroup = recordMap.get(KEY_SECTOR_NAME);
        String sectorCode = recordMap.get(KEY_SECTOR_CODE);
        String mediumGroup = recordMap.get(KEY_MEDIUM_NAME);
        String mediumCode = recordMap.get(KEY_MEDIUM_CODE);
        String expandedGroup = recordMap.get(KEY_EXPANDED_NAME);
        String expandedCode = recordMap.get(KEY_EXPANDED_CODE);

        sectorGroup = extractTitleFromSpanTag(sectorGroup);
        mediumGroup = extractTitleFromSpanTag(mediumGroup);
        expandedGroup = extractTitleFromSpanTag(expandedGroup);

        sectorGroup = cleanValue(sectorGroup);
        mediumGroup = cleanValue(mediumGroup);
        expandedGroup = cleanValue(expandedGroup);

        ZacksRecord record = new ZacksRecord();
        record.setSectorName(sectorGroup);
        record.setSectorCode(sectorCode);
        record.setMediumIndustryName(mediumGroup);
        record.setMediumIndustryCode(mediumCode);
        record.setExpandedIndustryName(expandedGroup);
        record.setExpandedIndustryCode(expandedCode);

        return record;
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

    // todo - revisit a better way
    protected String cleanValue(String input) {
        String result = super.cleanValue(input);

        result = capitalizeLetterAfterDash(result);

        result = result.replace("Oil&gas", "Oil & Gas");  // z
        result = result.replace(" Whole Sales", " Wholesale");  // z
        result = result.replace("Elec Pwr", "Electric Power");  // z
        result = result.replace("Utility-Gas Distr", "Utility-Gas Distribution");  // z
        result = result.replace(" And And ", " and ");  // z
        result = result.replace(" Rual", " Rural");  // z


        result = result.replace(" And ", " and ");
        result = result.replace(" and ", " & ");
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



    public static List<Map<String, String>> convertToListOfMaps(String json)
    {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }

        List<Map<String, String>> listOfMaps = null;

        try {
            JsonMapper mapper = new JsonMapper();
            listOfMaps = mapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert json string to list of maps: " + e.getMessage(), e);
        }

        return listOfMaps;
    }



    private String getSourceFileContents() throws IOException
    {
        URL url = null;
        try {
            url = new URL(SOURCE_FILE);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }

        try (InputStream inputStream = url.openStream()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        }
    }



    public String extractNestedJson(String html) throws IOException
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
