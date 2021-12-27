package com.github.bradjacobs.stock.serialize.canonical;

import com.github.bradjacobs.stock.classifications.common.AllLevelsRecord;
import com.github.bradjacobs.stock.serialize.canonical.objects.ActivityNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.GroupNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.IndustryNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SectorNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SubActivityNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SubIndustryNode;
import com.github.bradjacobs.stock.serialize.json.HeaderFieldDataExtractor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO - this whole class needs refactoring help
 */
public final class CanonicalHeaderUpdater
{
    private final Map<String,String> canonicalToOrigHeaderMap;
    private final Map<String,String> origToCanonicalHeaderMap;
    private final Map<String,String> canonicalToGenericMap;

    private static final Map<Integer, Class<?>> levelToNodeClassMap = new TreeMap<Integer, Class<?>>() {{
        put(1, SectorNode.class);
        put(2, GroupNode.class);
        put(3, IndustryNode.class);
        put(4, SubIndustryNode.class);
        put(5, ActivityNode.class);
        put(6, SubActivityNode.class);
    }};

    private static HeaderLabeler CANONICAL_HEADER_LABELER = createCanonicalHeaderLabeler();


    public CanonicalHeaderUpdater(String[] originalDataHeaderRow)
    {
        canonicalToOrigHeaderMap = createMapping(originalDataHeaderRow);
        origToCanonicalHeaderMap = reverseKeyValues(canonicalToOrigHeaderMap);

        canonicalToGenericMap = createQuotedKeyValueMap(CANONICAL_TO_GENERIC_MAP);
    }

    public String convertToNormalKeyNames(String canonicalJson)
    {
        return convertKeyNames(canonicalJson, canonicalToOrigHeaderMap);
    }

    // Todo - fix... this is horrible naming!
    public String convertNormalToCanonicalKeyNames(String normalJson)
    {
        return convertKeyNames(normalJson, origToCanonicalHeaderMap);
    }

    public String convertToGenericKeyNames(String canonicalJson)
    {
        return convertKeyNames(canonicalJson, canonicalToGenericMap);
    }


    private String convertKeyNames(String canonicalJson, Map<String,String> headerMap)
    {
        String result = canonicalJson;
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            result = StringUtils.replace(result, entry.getKey(), entry.getValue());
        }
        return result;
    }






    // todo - would be better to read from the object annotations
    //   b/c this is technically duplication

    private static final String CANONICAL_LEVEL_1_ID_HEADER = "sectorId";
    private static final String CANONICAL_LEVEL_1_NAME_HEADER = "sectorName";

    private static final String CANONICAL_LEVEL_1_CHILDREN_HEADER = "groups";

    private static final String CANONICAL_LEVEL_2_ID_HEADER = "groupId";
    private static final String CANONICAL_LEVEL_2_NAME_HEADER = "groupName";

    private static final String CANONICAL_LEVEL_2_CHILDREN_HEADER = "industries";

    private static final String CANONICAL_LEVEL_3_ID_HEADER = "industryId";
    private static final String CANONICAL_LEVEL_3_NAME_HEADER = "industryName";

    private static final String CANONICAL_LEVEL_3_CHILDREN_HEADER = "subIndustries";

    private static final String CANONICAL_LEVEL_4_ID_HEADER = "subIndustryId";
    private static final String CANONICAL_LEVEL_4_NAME_HEADER = "subIndustryName";

    private static final String CANONICAL_LEVEL_4_CHILDREN_HEADER = "activities";

    private static final String CANONICAL_LEVEL_5_ID_HEADER = "activityId";
    private static final String CANONICAL_LEVEL_5_NAME_HEADER = "activityName";

    private static final String CANONICAL_LEVEL_5_CHILDREN_HEADER = "subActivities";

    private static final String CANONICAL_LEVEL_6_ID_HEADER = "subActivityId";
    private static final String CANONICAL_LEVEL_6_NAME_HEADER = "subActivityName";

    private Map<String,String> createMapping(String[] headerRow)
    {
        Map<String,String> map = new LinkedHashMap<>();  // preserve insert order

        if (headerRow.length >= 2) {
            map.put(CANONICAL_LEVEL_1_ID_HEADER, headerRow[0]);
            map.put(CANONICAL_LEVEL_1_NAME_HEADER, headerRow[1]);
        }
        if (headerRow.length >= 4) {
            map.put(CANONICAL_LEVEL_2_ID_HEADER, headerRow[2]);
            map.put(CANONICAL_LEVEL_2_NAME_HEADER, headerRow[3]);
            map.put(CANONICAL_LEVEL_1_CHILDREN_HEADER, pluralizeName(headerRow[2]));
        }
        if (headerRow.length >= 6) {
            map.put(CANONICAL_LEVEL_3_ID_HEADER, headerRow[4]);
            map.put(CANONICAL_LEVEL_3_NAME_HEADER, headerRow[5]);
            map.put(CANONICAL_LEVEL_2_CHILDREN_HEADER, pluralizeName(headerRow[4]));
        }
        if (headerRow.length >= 8) {
            map.put(CANONICAL_LEVEL_4_ID_HEADER, headerRow[6]);
            map.put(CANONICAL_LEVEL_4_NAME_HEADER, headerRow[7]);
            map.put(CANONICAL_LEVEL_3_CHILDREN_HEADER, pluralizeName(headerRow[6]));
        }
        if (headerRow.length >= 10) {
            map.put(CANONICAL_LEVEL_5_ID_HEADER, headerRow[8]);
            map.put(CANONICAL_LEVEL_5_NAME_HEADER, headerRow[9]);
            map.put(CANONICAL_LEVEL_4_CHILDREN_HEADER, pluralizeName(headerRow[8]));
        }
        if (headerRow.length >= 12) {
            map.put(CANONICAL_LEVEL_6_ID_HEADER, headerRow[10]);
            map.put(CANONICAL_LEVEL_6_NAME_HEADER, headerRow[11]);
            map.put(CANONICAL_LEVEL_5_CHILDREN_HEADER, pluralizeName(headerRow[10]));
        }

        return createQuotedKeyValueMap(map);
    }


    private Map<String,String> reverseKeyValues(Map<String,String> inputMap)
    {
        Map<String,String> resultMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            resultMap.put(entry.getValue(), entry.getKey());
        }
        return resultMap;
    }


    private String pluralizeName(String input) {
        String result = input.replace("Id", "");
        result = result.replace("Code", "");
        if (result.endsWith("y")) {
            return result.substring(0, result.length() - 1) + "ies";
        }
        else {
            return result + "s";
        }
    }


    private Map<String,String> createQuotedKeyValueMap(Map<String,String> inputMap)
    {
        Map<String,String> resultMap = new LinkedHashMap<>();  // preserve insert order
        for (Map.Entry<String, String> entry : inputMap.entrySet())
        {
            resultMap.put("\"" + entry.getKey() + "\"", "\"" + entry.getValue() + "\"");
        }
        return resultMap;
    }


    private static final String GENERIC_ID_HEADER = "id";
    private static final String GENERIC_NAME_HEADER = "name";
    private static final String GENERIC_CHILDREN_HEADER = "children";

    private static final Map<String,String> CANONICAL_TO_GENERIC_MAP = new HashMap<String,String>(){{
        put(CANONICAL_LEVEL_1_ID_HEADER, GENERIC_ID_HEADER);
        put(CANONICAL_LEVEL_1_NAME_HEADER, GENERIC_NAME_HEADER);
        put(CANONICAL_LEVEL_1_CHILDREN_HEADER, GENERIC_CHILDREN_HEADER);
        put(CANONICAL_LEVEL_2_ID_HEADER, GENERIC_ID_HEADER);
        put(CANONICAL_LEVEL_2_NAME_HEADER, GENERIC_NAME_HEADER);
        put(CANONICAL_LEVEL_2_CHILDREN_HEADER, GENERIC_CHILDREN_HEADER);
        put(CANONICAL_LEVEL_3_ID_HEADER, GENERIC_ID_HEADER);
        put(CANONICAL_LEVEL_3_NAME_HEADER, GENERIC_NAME_HEADER);
        put(CANONICAL_LEVEL_3_CHILDREN_HEADER, GENERIC_CHILDREN_HEADER);
        put(CANONICAL_LEVEL_4_ID_HEADER, GENERIC_ID_HEADER);
        put(CANONICAL_LEVEL_4_NAME_HEADER, GENERIC_NAME_HEADER);
        put(CANONICAL_LEVEL_4_CHILDREN_HEADER, GENERIC_CHILDREN_HEADER);
        put(CANONICAL_LEVEL_5_ID_HEADER, GENERIC_ID_HEADER);
        put(CANONICAL_LEVEL_5_NAME_HEADER, GENERIC_NAME_HEADER);
        put(CANONICAL_LEVEL_5_CHILDREN_HEADER, GENERIC_CHILDREN_HEADER);
        put(CANONICAL_LEVEL_6_ID_HEADER, GENERIC_ID_HEADER);
        put(CANONICAL_LEVEL_6_NAME_HEADER, GENERIC_NAME_HEADER);
    }};


    private static HeaderLabeler createCanonicalHeaderLabeler()
    {
        HeaderFieldDataExtractor headerFieldDataExtractor = new HeaderFieldDataExtractor();
        HeaderLabeler canonicalHeaderLabeler = new HeaderLabeler();

        for (Map.Entry<Integer, Class<?>> entry : levelToNodeClassMap.entrySet()) {
            Integer level = entry.getKey();
            Class<?> clazz = entry.getValue();
            String[] headers = headerFieldDataExtractor.getHeaderFields(clazz);
            if (headers.length == 3) {
                canonicalHeaderLabeler.setLevelIdTitleChildren(level, headers[0], headers[1], headers[2]);
            }
            else if (headers.length == 2) {
                canonicalHeaderLabeler.setLevelIdTitleChildren(level, headers[0], headers[1], "");
            }
        }
        return canonicalHeaderLabeler;
    }


    private static class HeaderLabeler extends AllLevelsRecord {
        private final String[] childrenLevelTitles = new String[MAX_ARRAY_SIZE];

        public HeaderLabeler() {
            super();
            Arrays.fill(childrenLevelTitles, "");
        }

        public void setLevelIdTitleChildren(int level, String idLabel, String titleLabel, String childrenLabel)
        {
            if (isValidLevel(level)) {
                setLevelIdTitle(level, idLabel, titleLabel);
                this.childrenLevelTitles[level] = childrenLabel;
            }
        }
    }


}
