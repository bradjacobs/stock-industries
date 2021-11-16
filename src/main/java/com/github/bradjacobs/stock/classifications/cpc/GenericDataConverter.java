package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;
import com.github.bradjacobs.stock.util.DownloadUtil;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/CPC_Ver_2_1_english_structure.txt

/**
 */
public class GenericDataConverter implements DataConverter<CpcRecord>
{
    private static final List<String> TAGS_TO_REMOVE = Arrays.asList("<i>", "</i>");

    @Override
    public Classification getClassification()
    {
        return Classification.CPC;
    }

    private static class RawEntryRecord {
        @JsonProperty("CPC21code")
        private String code;
        @JsonProperty("CPC21title")
        private String title;
    }
    private static abstract class SpecialCodeTitleRecord extends CodeTitleRecordObj {
        @Override
        @JsonAlias("CPC21code")
        abstract public String getCode();

        @Override
        @JsonAlias("CPC21title")
        abstract public String getTitle();
    }

    private abstract class ZZZCodeTitleRecord extends CodeTitleRecordObj {
        @JsonAlias("code")  @Override abstract public String getCode();
        @JsonAlias("title") @Override abstract public String getTitle();
    }

    private static class MyJsonSerializer2 extends JsonSerializer<AllLevelsRecord> {
        public void serialize(AllLevelsRecord value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            //  NO... this won't work

            jgen.writeStartObject();
            jgen.writeStringField("my1Id", value.getCodeId(1));
            jgen.writeStringField("my1Name", value.getCodeTitle(1));
            jgen.writeStringField("my2Id", value.getCodeId(2));
            jgen.writeStringField("my2Name", value.getCodeTitle(2));
            jgen.writeStringField("my3Id", value.getCodeId(3));
            jgen.writeStringField("my3Name", value.getCodeTitle(3));
            jgen.writeStringField("my4Id", value.getCodeId(4));
            jgen.writeStringField("my4Name", value.getCodeTitle(4));
            jgen.writeEndObject();
        }
    }


    public static class MyJsonSerializer extends StdSerializer<AllLevelsRecord>
    {
        public MyJsonSerializer() {
            super(AllLevelsRecord.class);
        }

        @Override
        public void serialize(AllLevelsRecord value, JsonGenerator gen, SerializerProvider provider) throws IOException
        {
            //  NO... this won't work
            gen.writeStartObject();
            gen.writeStringField("my1Id", value.getCodeId(1));
            gen.writeStringField("my1Name", value.getCodeTitle(1));
            gen.writeStringField("my2Id", value.getCodeId(2));
            gen.writeStringField("my2Name", value.getCodeTitle(2));
            gen.writeStringField("my3Id", value.getCodeId(3));
            gen.writeStringField("my3Name", value.getCodeTitle(3));
            gen.writeStringField("my4Id", value.getCodeId(4));
            gen.writeStringField("my4Name", value.getCodeTitle(4));
            gen.writeEndObject();
        }
    }

    protected <R extends CodeTitleLevelRecord> void doConvertToObjects(List<R> codeTitleRecords)
            throws JsonProcessingException
    {

    }


        public static void main(String[] args) throws Exception {

        String filePath = "/Users/bradjacobs/git/bradjacobs/stock-industries/src/main/java/com/github/bradjacobs/stock/classifications/cpc/cpc.txt";
        String csv = FileUtils.readFileToString(new File(filePath));

        CsvDeserializer csvDeserializer = new CsvDeserializer(null);
        List<RawEntryRecord> rawRecords = csvDeserializer.csvToObjectList(RawEntryRecord.class, csv);

        boolean testit = false;

        if (testit)
        {
            Class clazz = ZZZCodeTitleRecord.class;

            generateMixin(clazz);
            generateMixin(clazz);

            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            CsvMapper csvObjectMapper = MapperBuilder.csv().setArrayWrap(false).build();
            csvObjectMapper = (CsvMapper) csvObjectMapper.addMixIn(CodeTitleRecordObj.class, SpecialCodeTitleRecord.class);
            ObjectReader objReader = csvObjectMapper.readerFor(CodeTitleRecordObj.class).with(schema);
            MappingIterator<CodeTitleRecordObj> iterator = objReader.readValues(csv);
            List<CodeTitleRecordObj> fooo = iterator.readAll();

            int kjkj = 333;
        }




        CsvMapper mapper = new CsvMapper();
        CsvSchema sclema = mapper.schemaFor(CodeTitleRecordObj.class)
                .withSkipFirstDataRow(true);

        MappingIterator<CodeTitleRecordObj> iterator = mapper
                .readerFor(CodeTitleRecordObj.class)
                .with(sclema).readValues(csv);

        List<CodeTitleRecordObj> hotelSummaries = iterator.readAll();

        GenericLevelRecord currentRecord = new GenericLevelRecord();
        AllLevelsRecord currentRecordv2 = new AllLevelsRecord();

        List<GenericLevelRecord> recordList = new ArrayList<>();
        List<AllLevelsRecord> recordListv2 = new ArrayList<>();

        for (CodeTitleRecordObj pairRecord : hotelSummaries) {
            String codeId = pairRecord.getCode();
            String name = pairRecord.getTitle();

            int level = codeId.length();

            if (currentRecord.isLevelIdSet(level)) {
                recordList.add(currentRecord);
                currentRecord = currentRecord.copy(level);
            }
            currentRecord.setLevelIdName(level, codeId, name);

            if (currentRecordv2.isLevelIdSet(level)) {
                recordListv2.add(currentRecordv2);
                currentRecordv2 = currentRecordv2.copy(level);
            }
            currentRecordv2.setLevelIdTitle(level, codeId, name);
        }


        List<AllLevelsRecord> subZList = recordListv2.subList(0,4);

        List<Map<String,String>> listOfMaps = new ArrayList<>();

        for (AllLevelsRecord recordv2 : subZList) {
            Map<String,String> valueMap = new LinkedHashMap<>();
            valueMap.put("sectionId", recordv2.getCodeId(1));
            valueMap.put("sectionName", recordv2.getCodeTitle(1));
            valueMap.put("divisionId", recordv2.getCodeId(2));
            valueMap.put("divisionName", recordv2.getCodeTitle(2));
            valueMap.put("groupId", recordv2.getCodeId(3));
            valueMap.put("groupName", recordv2.getCodeTitle(3));
            valueMap.put("classId", recordv2.getCodeId(4));
            valueMap.put("className", recordv2.getCodeTitle(4));
            listOfMaps.add(valueMap);
        }

        JsonMapper j1Mapper = MapperBuilder.json().build();
        String strJ1 = j1Mapper.writeValueAsString(listOfMaps);

        Map<Integer,String> levelLabelMap = new HashMap<>();
        levelLabelMap.put(1, "section");
        levelLabelMap.put(2, "division");
        levelLabelMap.put(3, "group");
        levelLabelMap.put(4, "class");

        List<Map<String, String>> myListOfMaps = generateListOfMaps(subZList, levelLabelMap);
        String strJ2 = generateJsonString(subZList, levelLabelMap);

        List<CpcRecord> myListABC = j1Mapper.readValue(strJ2, new TypeReference<List<CpcRecord>>() {});

        List<CpcRecord> myListDEF = j1Mapper.convertValue(myListOfMaps, new TypeReference<List<CpcRecord>>() {});


        CsvMapper csvObjectMapper = MapperBuilder.csv().setArrayWrap(false).build();
        CsvMapper csvObjectMapper2 = MapperBuilder.csv().setArrayWrap(false).build();

        boolean tryOne = true;

//        CsvSchema schema = csvObjectMapper.schemaFor(GenericLevelRecordv2.class).withHeader();
//        String csvData = csvObjectMapper.writer(schema).writeValueAsString(subZList);

        CsvSchema schema2 = csvObjectMapper2.schemaFor(AllLevelsRecord.class).withHeader();
        String csvData2 = csvObjectMapper2.writer(schema2).writeValueAsString(subZList);


        int kjkj = 33;

    }

    public static List<Map<String,String>> generateListOfMaps(List<AllLevelsRecord> recordList, Map<Integer, String> levelFieldMap) throws JsonProcessingException {
        List<Map<String,String>> listOfMaps = new ArrayList<>();

        int maxLevels = 8;

        String[] codeIdTitleArray = new String[8];
        String[] codeNameTitleArray = new String[8];
        Arrays.fill(codeIdTitleArray, "");
        Arrays.fill(codeNameTitleArray, "");

        for (int level = 1; level <= maxLevels; level++) {
            String levelFieldName = levelFieldMap.get(level);
            if (levelFieldName != null) {
                codeIdTitleArray[level] = levelFieldName + "Id";
                codeNameTitleArray[level] = levelFieldName + "Name";
            }
            else {
                break;
            }
        }

        for (AllLevelsRecord record : recordList)
        {
            Map<String,String> valueMap = new LinkedHashMap<>();

            for (int level = 1; level <= maxLevels; level++)
            {
                String levelIdValue = record.getCodeId(level);
                String levelNameValue = record.getCodeTitle(level);
                String levelIdLabel = codeIdTitleArray[level];
                String levelNameLabel = codeNameTitleArray[level];

                if (StringUtils.isEmpty(levelIdValue) ||
                        StringUtils.isEmpty(levelNameValue) ||
                        StringUtils.isEmpty(levelIdLabel) ||
                        StringUtils.isEmpty(levelNameLabel))
                {
                    break;
                }

                valueMap.put(levelIdLabel, levelIdValue);
                valueMap.put(levelNameLabel, levelNameValue);
            }

            if (! valueMap.isEmpty()) {
                listOfMaps.add(valueMap);
            }
        }

        return listOfMaps;
    }


    public static String generateJsonString(List<AllLevelsRecord> recordList, Map<Integer, String> levelFieldMap) throws JsonProcessingException {

        List<Map<String,String>> listOfMaps = generateListOfMaps(recordList, levelFieldMap);

        JsonMapper j1Mapper = MapperBuilder.json().build();
        String strJ1 = j1Mapper.writeValueAsString(listOfMaps);

        return strJ1;
    }




    @Override
    public List<CpcRecord> createDataRecords() throws IOException
    {
        String csvData = DownloadUtil.downloadFile(getClassification().getSourceFileLocation());

        CsvDeserializer csvDeserializer = new CsvDeserializer(null);
        List<RawEntryRecord> rawRecords = csvDeserializer.csvToObjectList(RawEntryRecord.class, csvData);

        List<CpcRecord> recordList = new ArrayList<>();
        CpcRecord currentRecord = new CpcRecord();

        for (RawEntryRecord rawRecord : rawRecords) {
            String id = rawRecord.code;
            String name = cleanValue(rawRecord.title);

            int level = id.length();
            if (level == 1) {
                currentRecord.setSectionId(id);
                currentRecord.setSectionName(name);
            }
            else if (level == 2) {
                if (!currentRecord.getDivisionId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setDivisionId(id);
                currentRecord.setDivisionName(name);
            }
            else if (level == 3) {
                if (!currentRecord.getGroupId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setGroupId(id);
                currentRecord.setGroupName(name);
            }
            else if (level == 4) {
                if (!currentRecord.getClassId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setClassId(id);
                currentRecord.setClassName(name);
            }
            else if (level == 5) {
                if (!currentRecord.getSubClassId().isEmpty()) {
                    recordList.add(currentRecord);
                    currentRecord = currentRecord.copy(level);
                }
                currentRecord.setSubClassId(id);
                currentRecord.setSubClassName(name);
            }
        }

        recordList.add(currentRecord);
        return recordList;
    }

    protected String cleanValue(String input)
    {
        String cleanValue = input;
        for (String tag : TAGS_TO_REMOVE) {
            cleanValue = StringUtils.replace(cleanValue, tag, "");
        }
        return StringUtil.cleanWhitespace(cleanValue);
    }



    private static Class<CodeTitleRecordObj> generateMixin(Class clazz) throws NoSuchMethodException {
//        ZZZGenericRecord instance = new ZZZGenericRecord();

        Method[] methods = clazz.getDeclaredMethods();
        Method m1 = clazz.getDeclaredMethod("getCode");
        m1.setAccessible(true);

        JsonAlias myAnnotation = m1.getAnnotation(JsonAlias.class);

        // https://stackoverflow.com/questions/14268981/modify-a-class-definitions-annotation-string-parameter-at-runtime

        String key = "value";
        String[] newValue = new String[]{"xkekjkejkej"};

        Object handler = Proxy.getInvocationHandler(myAnnotation);
        Field f;
        try {
            f = handler.getClass().getDeclaredField("memberValues");
            int kjkj = 333;
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        f.setAccessible(true);
        Map<String, Object> memberValues;
        try {
            memberValues = (Map<String, Object>) f.get(handler);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        Object oldValue = memberValues.get(key);
        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
            throw new IllegalArgumentException();
        }
        memberValues.put(key,newValue);






        Annotation[] declAnnotations = m1.getDeclaredAnnotations();

        String[] v1 = myAnnotation.value();
        v1[0] = "fooooo";



        String[] v2 = myAnnotation.value();


        //JsonAlias aliasAnnotation = new JsonAlias();

        return null;
    }

}
