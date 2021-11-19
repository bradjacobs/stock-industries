package com.github.bradjacobs.stock.classifications.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.serialize.json.HeaderFieldDataExtractor;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * converts 'tuple data'  (each record has it's own codeId ant title) to a pojo
 *
 * TODO -- this needs major commenting!
 */
public class TupleToPojoConverter
{
    private static final JsonMapper JSON_MAPPER = MapperBuilder.json().build();
    private static final HeaderFieldDataExtractor headerFieldDataExtractor = new HeaderFieldDataExtractor();

    public <T, R extends CodeTitleLevelRecord> List<T> doConvertToObjects(
            Class<T> clazz, List<R> codeTitleRecords) {
        List<AllLevelsRecord> allLevelsRecords = doConvert(codeTitleRecords);
        List<Map<String,String>> listOfMaps = generateListOfMaps(clazz, allLevelsRecords);

        JavaType javaType = JSON_MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return JSON_MAPPER.convertValue(listOfMaps, javaType);
    }

    public <R extends CodeTitleLevelRecord> List<AllLevelsRecord> doConvert(List<R> codeTitleRecords)
    {
        AllLevelsRecord currentRecord = new AllLevelsRecord();
        List<AllLevelsRecord> resultList = new ArrayList<>();

        for (R pairRecord : codeTitleRecords) {
            String codeId = pairRecord.getCodeId();
            String title = pairRecord.getCodeTitle();
            title = StringUtil.cleanWhitespace(title);  // todo - dumb location for this line.
            int level = pairRecord.getCodeLevel();

            if (currentRecord.isLevelIdSet(level)) {
                resultList.add(currentRecord);
                currentRecord = currentRecord.copy(level);
            }
            currentRecord.setLevelIdTitle(level, codeId, title);
        }
        resultList.add(currentRecord);

        return resultList;
    }

    public <T> List<Map<String,String>> generateListOfMaps(Class<T> clazz, List<AllLevelsRecord> recordList)
    {
        String[] headerFieldNames = headerFieldDataExtractor.getHeaderFields(clazz);

        int maxLevels = headerFieldNames.length / 2;

        List<Map<String,String>> listOfMaps = new ArrayList<>();

        String[] codeIdTitleArray = new String[maxLevels+1];
        String[] codeNameTitleArray = new String[maxLevels+1];
        Arrays.fill(codeIdTitleArray, "");
        Arrays.fill(codeNameTitleArray, "");

        for (int level = 1; level <= maxLevels; level++) {
            // todo - refactor and comment this
            //  level 1 --> index 0
            //  level 2 --> index 2
            //  level 3 --> index 4
            int codeIdFieldIndex = (level - 1) * 2;
            int titleFieldIndex = codeIdFieldIndex + 1;

            codeIdTitleArray[level] = headerFieldNames[codeIdFieldIndex];
            codeNameTitleArray[level] = headerFieldNames[titleFieldIndex];
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

}
