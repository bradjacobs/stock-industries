package com.github.bradjacobs.stock.serialize.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.GenericNode;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.serialize.BaseDeserializer;
import com.github.bradjacobs.stock.serialize.canonical.CanonicalHeaderUpdater;
import com.github.bradjacobs.stock.types.JsonDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JsonDeserializer extends BaseDeserializer
{
    private final JsonDefinition jsonDefinition;
    private final JsonMapper mapper;

    public JsonDeserializer(JsonDefinition jsonDefinition)
    {
        this.jsonDefinition = jsonDefinition;
        this.mapper = MapperBuilder.json().build();

    }

    @Override
    public <T> List<T> deserializeObjects(Class<T> clazz, String json) throws IOException
    {
        if (this.jsonDefinition.isTree())
        {
            return deserializeTreeToObjects(clazz, json);
        }
        else {
            return convertFlatListOfMapsToObjects(clazz, json);
        }
    }


    private <T> List<T> convertFlatListOfMapsToObjects(Class<T> clazz, String json) throws JsonProcessingException
    {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        return mapper.readValue(json, javaType);
    }


    private <T> List<T> deserializeTreeToObjects(Class<T> clazz, String json) throws JsonProcessingException
    {
        // read in a generic form of the map data   (if it's something else, then convert to generic node form)
        String genericTreeJson = convertToGenericTreeJson(clazz, json);

        String[] headerFields = getHeaderFields(clazz);
        GenericNode[] genericNodes = mapper.readValue(genericTreeJson, GenericNode[].class);
        GenericNodeToFlatListOfMapsConverter converter = new GenericNodeToFlatListOfMapsConverter(headerFields);
        List<Map<String, String>> listOfMaps = converter.createFlatMapList(genericNodes);
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        return mapper.convertValue(listOfMaps, javaType);
    }

    // todo - fix naming
    private <T> String convertToGenericTreeJson(Class<T> clazz, String json)
    {
        String[] headerFields = getHeaderFields(clazz);
        JsonDefinition.JsonKeyName jsonTreeType = this.jsonDefinition.getJsonKeyName();
        CanonicalHeaderUpdater canonicalHeaderUpdater = new CanonicalHeaderUpdater(headerFields);


        if (! this.jsonDefinition.isTree()) {
            // todo -- fix.. this is misleading b/c of the definition value version the actual input string.
            throw new IllegalArgumentException("Must be tree-form json");
        }

        if (jsonTreeType.equals(JsonDefinition.JsonKeyName.NORMAL)) {
            String canonicalTreeJson = canonicalHeaderUpdater.convertNormalToCanonicalKeyNames(json);
            return canonicalHeaderUpdater.convertToGenericKeyNames(canonicalTreeJson);
        }
        else if (jsonTreeType.equals(JsonDefinition.JsonKeyName.CANONICAL)) {
            return canonicalHeaderUpdater.convertToGenericKeyNames(json);
        }
        else if (jsonTreeType.equals(JsonDefinition.JsonKeyName.BASIC)) {
            return json;
        }
        else {
            throw new InternalError("Unhandled jsonTreeType: " + jsonTreeType);
        }
    }




    protected <T> String[] getHeaderFields(Class<T> clazz)
    {
        String[] headerValues = null;
        JsonPropertyOrder propOrderAnnotation = clazz.getAnnotation(JsonPropertyOrder.class);
        if (propOrderAnnotation != null)
        {
            boolean isAlphabetic = propOrderAnnotation.alphabetic();
            if (isAlphabetic) {
                // currently not supported
                throw new IllegalArgumentException("'JsonPropertyOrder.alphabetic = true' is currently unsupported.");
            }
            headerValues = propOrderAnnotation.value();
        }

        if (headerValues == null || headerValues.length == 0) {
            throw new IllegalArgumentException("class is missing 'JsonPropertyOrder' annotation: " + clazz.getCanonicalName());
        }

        return headerValues;
    }


    // todo - think of a better class name b/c this is terrible.
    private static class GenericNodeToFlatListOfMapsConverter
    {
        private List<Map<String,String>> resultListOfMaps = new ArrayList<>();
        private final String[] headerRow;
        private int headerCount;

        public GenericNodeToFlatListOfMapsConverter(String[] headerRow)
        {
            this.headerRow = headerRow;
            headerCount = headerRow.length;

            // if odd number ignore the last one.  it's probably a description field that isn't supported in tree format.
            if ((headerCount % 2) == 1) {
                headerCount--;
            }
        }

        public List<Map<String,String>> createFlatMapList(GenericNode[] genericNodes) {
            resultListOfMaps = new ArrayList<>();
            for (GenericNode genericNode : genericNodes)
            {
                createFlatMapList(genericNode, new ArrayList<>());
            }
            return resultListOfMaps;
        }

        private void createFlatMapList(GenericNode node, List<String> parentRowData)
        {
            List<String> rowData = new ArrayList<>(parentRowData);
            rowData.add(node.getId());
            rowData.add(node.getName());

            List<GenericNode> children = node.getChildren();
            if (children != null && children.size() > 0) {
                for (GenericNode child : children) {
                    createFlatMapList(child, rowData);
                }
            }
            else {
                writeRowData(rowData);
            }
        }

        private void writeRowData(List<String> rowData) {
            Map<String,String> entryMap = new LinkedHashMap<>();
            for (int i = 0; i < this.headerCount; i++) {
                entryMap.put(headerRow[i], rowData.get(i));
            }
            this.resultListOfMaps.add(entryMap);
        }
    }


}
