package com.github.bradjacobs.stock.classifications.common;

import com.github.bradjacobs.stock.classifications.DataFileType;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CanonicalHeaderUpdater
{
    private final Map<String,String> canonicalToOrigHeaderMap;
    private final Map<String,String> canonicalToGenericMap;

    public CanonicalHeaderUpdater(String[] originalDataHeaderRow)
    {
        canonicalToOrigHeaderMap = createMapping(originalDataHeaderRow);
        canonicalToGenericMap = createQuotedKeyValueMap(CANONICAL_TO_GENERIC_MAP);
    }


    public String convertJsonKeyNames(String canonicalJson, DataFileType dataFileType)
    {
        Map<String,String> entryMap = null;
        if (DataFileType.TREE_JSON.equals(dataFileType)) {
            entryMap = canonicalToOrigHeaderMap;
        }
        else if (DataFileType.BASIC_TREE_JSON.equals(dataFileType)) {
            entryMap = canonicalToGenericMap;
        }
        else {
            throw new IllegalArgumentException("Unsupported dataFileType: " + dataFileType);
        }

        String result = canonicalJson;
        for (Map.Entry<String, String> entry : entryMap.entrySet()) {
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
            map.put(CANONICAL_LEVEL_1_CHILDREN_HEADER, pluralizeName(headerRow[3]));
        }
        if (headerRow.length >= 6) {
            map.put(CANONICAL_LEVEL_3_ID_HEADER, headerRow[4]);
            map.put(CANONICAL_LEVEL_3_NAME_HEADER, headerRow[5]);
            map.put(CANONICAL_LEVEL_2_CHILDREN_HEADER, pluralizeName(headerRow[5]));
        }
        if (headerRow.length >= 8) {
            map.put(CANONICAL_LEVEL_4_ID_HEADER, headerRow[6]);
            map.put(CANONICAL_LEVEL_4_NAME_HEADER, headerRow[7]);
            map.put(CANONICAL_LEVEL_3_CHILDREN_HEADER, pluralizeName(headerRow[7]));
        }
        if (headerRow.length >= 10) {
            // todo: currently it's always the same name, but can't assume that always
        }

        return createQuotedKeyValueMap(map);
    }


    private String pluralizeName(String input) {
        String result = input.replace("Name", "");
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

    private static Map<String,String> CANONICAL_TO_GENERIC_MAP = new HashMap<String,String>(){{
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
    }};


}
