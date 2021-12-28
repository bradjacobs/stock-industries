package com.github.bradjacobs.stock.serialize.json;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.serialize.BaseSerializer;
import com.github.bradjacobs.stock.serialize.csv.CsvMatrixConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvSerializer;
import com.github.bradjacobs.stock.types.JsonDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

// TODO - some code will be redundantly called
//    i.e. common when calling tree with canonical vs basic.
//       this is basically a low-level optimization that can be dealt with later
public class JsonSerializer extends BaseSerializer
{
    // common (arbitrarily picked) level names
    private static final List<LevelHeaderNames> canonicalHeaders = Arrays.asList(
            new LevelHeaderNames("", "rootId", "rootName"),  // level 0 placeholder
            new LevelHeaderNames("sectors", "sectorId", "sectorName"),
            new LevelHeaderNames("groups", "groupId", "groupName"),
            new LevelHeaderNames("industries","industryId", "industryName"),
            new LevelHeaderNames("subIndustries","subIndustryId", "subIndustryName"),
            new LevelHeaderNames("activities", "activityId", "activityName"),
            new LevelHeaderNames("subActivities", "subActivityId", "subActivityName")
    );

    // genericHeaders are simple id/name.  create with same number of records as the canonicalHeaders above
    private static final List<LevelHeaderNames> genericHeaders = IntStream.range(0, canonicalHeaders.size())
            .mapToObj(i -> new LevelHeaderNames("children", "id", "name"))
            .collect(toList());

    private final JsonDefinition jsonDefinition;

    public JsonSerializer(JsonDefinition jsonDefinition)
    {
        this.jsonDefinition = jsonDefinition;
    }

    @Override
    public String generateFileName(Classification classification)
    {
        return this.jsonDefinition.generateFileName(classification);
    }

    @Override
    protected <T> String serializeObjects(List<T> objectList) throws IOException
    {
        Class<?> clazz = identifyClass(objectList);
        JsonMapper jsonMapper = MapperBuilder.json().setClazz(clazz).setIncludeLongDescription(this.jsonDefinition.isIncludeDescription()).build();

        String jsonString;
        if (this.jsonDefinition.isTree()) {

            List<LevelHeaderNames> headerLabelList;

            JsonDefinition.JsonKeyName jsonKeyName = this.jsonDefinition.getJsonKeyName();
            if (jsonKeyName.equals(JsonDefinition.JsonKeyName.NORMAL)) {
                HeaderFieldDataExtractor headerFieldDataExtractor = new HeaderFieldDataExtractor();
                String[] headerFields = headerFieldDataExtractor.getHeaderFields(clazz);
                headerLabelList = generateTreeHeaders(headerFields);
            }
            else if (jsonKeyName.equals(JsonDefinition.JsonKeyName.CANONICAL)) {
                headerLabelList = canonicalHeaders;
            }
            else if (jsonKeyName.equals(JsonDefinition.JsonKeyName.BASIC)) {
                headerLabelList = genericHeaders;
            }
            else {
                throw new IllegalStateException("Unknown jsonKeyName: " + jsonKeyName);
            }
            List<Map<String, Object>> hierarchyListOfMaps = convertToTreeListOfMaps(objectList, headerLabelList);
            jsonString = jsonMapper.writeValueAsString(hierarchyListOfMaps);
        }
        else {
            //  note:  jsonDefinition.getJsonKeyName()  currently not used for this case.
            jsonString = jsonMapper.writeValueAsString(objectList);
        }

        return jsonString;
    }


    protected <T> List<Map<String, Object>> convertToTreeListOfMaps(List<T> objectList, List<LevelHeaderNames> headerLabelList) throws IOException
    {
        String csvString = CsvSerializer.serializeToCsvString(objectList);
        String[][] csvMatrix = CsvMatrixConverter.convertToMatrix(csvString);

        GenericLevelNode rootNode = new GenericLevelNode(0, "0000", "root");
        Map<String, GenericLevelNode> lookupMap = new HashMap<>();

        // total levels if 1/2 of number of columns (and any extra 'description' column will get dropped off)
        int totalLevels = csvMatrix[0].length / 2;

        // start at index 1 (e.g. skip the header row)
        for (int i = 1; i < csvMatrix.length; i++)
        {
            String[] dataRow = csvMatrix[i];
            for (int j = 0; j < totalLevels; j++) {
                String idValue = dataRow[j * 2];

                if (!lookupMap.containsKey(idValue)) {
                    int currentLevel = j+1;
                    String titleValue = dataRow[(j * 2)+1];

                    GenericLevelNode parentNode;
                    if (j == 0) {
                        parentNode = rootNode;
                    }
                    else {
                        String parentIdValue = dataRow[(j-1) * 2];
                        parentNode = lookupMap.get(parentIdValue);
                    }

                    GenericLevelNode genericLevelNode = new GenericLevelNode(currentLevel, idValue, titleValue);
                    parentNode.addChild(genericLevelNode);
                    lookupMap.put(idValue, genericLevelNode);
                }
            }
        }

        return generateChildNodeMaps(rootNode, headerLabelList);
    }


    private List<LevelHeaderNames> generateTreeHeaders(String[] headerRow) {
        List<LevelHeaderNames> resultList = new ArrayList<>();
        resultList.add(new LevelHeaderNames("", "rootId", "rootName"));  // level 0 placeholder

        for (int i = 1; i < headerRow.length; i+=2) {
            String idLabel = headerRow[i-1];
            String titleLabel = headerRow[i];
            String groupLabel = pluralizeName(idLabel);
            resultList.add(new LevelHeaderNames(groupLabel, idLabel, titleLabel));
        }
        return resultList;
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


    private static class LevelHeaderNames {
        private final String groupLabel;
        private final String idLabel;
        private final String titleLabel;

        public LevelHeaderNames(String groupLabel, String idLabel, String titleLabel) {
            this.groupLabel = groupLabel;
            this.idLabel = idLabel;
            this.titleLabel = titleLabel;
        }
    }

    private static class GenericLevelNode {
        private final int level;
        private final String id;
        private final String name;
        private final List<GenericLevelNode> children = new ArrayList<>();

        public GenericLevelNode(int level, String id, String name) {
            this.level = level;
            this.id = id;
            this.name = name;
        }

        public void addChild(GenericLevelNode node) {
            children.add(node);
        }
    }


    private Map<String,Object> generateValueMap(GenericLevelNode node, List<LevelHeaderNames> levelHeaderNames) {

        LevelHeaderNames levelValues = levelHeaderNames.get(node.level);

        Map<String,Object> resultMap = new LinkedHashMap<>();
        resultMap.put(levelValues.idLabel, node.id);
        resultMap.put(levelValues.titleLabel, node.name);

        if (!node.children.isEmpty()) {
            String childrenGroupLabel = levelHeaderNames.get(node.level+1).groupLabel;
            List<Map<String,Object>> childValueList = generateChildNodeMaps(node, levelHeaderNames);
            resultMap.put(childrenGroupLabel, childValueList);
        }
        return resultMap;
    }

    private List<Map<String,Object>> generateChildNodeMaps(GenericLevelNode node, List<LevelHeaderNames> levelHeaderNames)
    {
        List<Map<String,Object>> childValueList = new LinkedList<>();
        for (GenericLevelNode child : node.children) {
            childValueList.add(generateValueMap(child, levelHeaderNames));
        }
        return childValueList;
    }
}
