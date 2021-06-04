package com.github.bradjacobs.stock.serialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.serialize.canonical.CanonicalHeaderUpdater;
import com.github.bradjacobs.stock.serialize.canonical.objects.ActivityNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.GroupNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.IndustryNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SectorNode;
import com.github.bradjacobs.stock.serialize.canonical.objects.SubIndustryNode;
import com.github.bradjacobs.stock.types.CsvDefinition;
import com.github.bradjacobs.stock.types.JsonDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonSerializer extends BaseSerializer
{
    private final JsonDefinition jsonDefinition;

    public JsonSerializer(JsonDefinition jsonDefinition)
    {
        this.jsonDefinition = jsonDefinition;
    }

    @Override
    public String generateFileSuffix()
    {
        return this.jsonDefinition.generateFileSuffix();
    }


    @Override
    protected <T> String serializeObjects(List<T> objectList) throws IOException
    {
        Class<?> clazz = identifyClass(objectList);
        JsonMapper jsonMapper = createJsonMapper();

        if (! this.jsonDefinition.isIncludeDescription()) {
            jsonMapper.addMixIn(clazz, NoDescriptionMixin.class);
        }

        String jsonData = null;


        if (this.jsonDefinition.isTree()) {
            jsonData = serializeObjectsToTree(jsonMapper, objectList);
        }
        else {
            //  note:  jsonDefinition.getJsonKeyName()  currently not used for this case.
            jsonData = jsonMapper.writeValueAsString(objectList);
        }


        return jsonData;
    }

    //          File canonicalJsonFile = createFileObject(DataFileType.CANONICAL_TREE_JSON);
    //
    //        try (FileOutputStream fileOutputStream = new FileOutputStream(canonicalJsonFile)) {
    //            jsonMapper.writeValue(fileOutputStream, sectorNodeList);
    //        }


    protected <T> String serializeObjectsToTree(JsonMapper jsonMapper, List<T> objectList) throws IOException
    {
        //  for now first convert the objects to a 2-d string array
        //      doesn't have to remain, but works for now.
        CsvDefinition csvDefn = CsvDefinition.builder().makeSparsely(true).withLongDescriptions(false).build();

        CsvSerializer csvSerializer = SerializerFactory.createCsvSerializer(csvDefn);
        String[][] sparseMatrix = csvSerializer.serializeToMatrix(objectList);
        List<SectorNode> sectorNodes = createCanonicalJsonTree(sparseMatrix);

        String jsonTree = null;
        String canonicalTreeJson = jsonMapper.writeValueAsString(sectorNodes);

        if (! this.jsonDefinition.getJsonKeyName().equals(JsonDefinition.JsonKeyName.CANONICAL))
        {
            CanonicalHeaderUpdater canonicalHeaderUpdater = new CanonicalHeaderUpdater(sparseMatrix[0]);
            if (this.jsonDefinition.getJsonKeyName().equals(JsonDefinition.JsonKeyName.NORMAL)) {
                jsonTree = canonicalHeaderUpdater.convertToNormalKeyNames(canonicalTreeJson);
            }
            else if (this.jsonDefinition.getJsonKeyName().equals(JsonDefinition.JsonKeyName.BASIC)) {
                jsonTree = canonicalHeaderUpdater.convertToGenericKeyNames(canonicalTreeJson);
            }
        }
        else {
            jsonTree = canonicalTreeJson;
        }

        return jsonTree;
    }




    private List<SectorNode> createCanonicalJsonTree(String[][] sparseArray) throws IOException
    {
        List<SectorNode> sectorNodeList = new ArrayList<>();
        SectorNode currentSector = null;
        GroupNode currentGroup = null;
        IndustryNode currentIndustry = null;
        SubIndustryNode currentSubIndustry = null;

        // start and index 1 (ignore header row)
        for (int i = 1; i < sparseArray.length; i++) {

            String[] rowData = sparseArray[i];

            if (rowData.length >= 2) {
                SectorNode sectorNode = new SectorNode(rowData[0], rowData[1]);

                if (! sectorNode.getSectorId().isEmpty()) {
                    sectorNodeList.add(sectorNode);
                    currentSector = sectorNode;
                }
            }
            if (rowData.length >= 4) {
                GroupNode groupNode = new GroupNode(rowData[2], rowData[3]);

                if (! groupNode.getGroupId().isEmpty()) {
                    currentSector.addGroup(groupNode);
                    currentGroup = groupNode;
                }
            }
            if (rowData.length >= 6) {
                IndustryNode industryNode = new IndustryNode(rowData[4], rowData[5]);

                if (! industryNode.getIndustryId().isEmpty()) {
                    currentGroup.addIndustry(industryNode);
                    currentIndustry = industryNode;
                }
            }
            if (rowData.length >= 8) {
                SubIndustryNode subIndustryNode = new SubIndustryNode(rowData[6], rowData[7]);

                if (! subIndustryNode.getSubIndustryId().isEmpty()) {
                    currentIndustry.addSubIndustry(subIndustryNode);
                    currentSubIndustry = subIndustryNode;
                }
            }
            if (rowData.length >= 10) {
                ActivityNode activityNode = new ActivityNode(rowData[8], rowData[9]);

                if (! activityNode.getActivityId().isEmpty()) {
                    currentSubIndustry.addActivity(activityNode);
                }
            }
        }

        return sectorNodeList;
    }


    protected JsonMapper createJsonMapper()
    {
        JsonMapper mapper = new JsonMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);  // for now always get pretty indenting

        // note: avoid marshalling out an empty array.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }



    //         if (writeFile)
    //        {
    //            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    //            FileOutputStream fileOutputStream = new FileOutputStream(OUTPUT_FILE);
    //            mapper.writeValue(fileOutputStream, sectorNodeList);
    //            fileOutputStream.close();
    //        }

}
