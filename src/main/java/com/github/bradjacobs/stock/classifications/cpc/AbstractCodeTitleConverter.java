package com.github.bradjacobs.stock.classifications.cpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// https://unstats.un.org/unsd/classifications/Econ/Download/In%20Text/CPC_Ver_2_1_english_structure.txt

/**
 */
abstract public class AbstractCodeTitleConverter
{
    private static final JsonMapper JSON_MAPPER = MapperBuilder.json().build();

    private final String[] levelLabelLookup;
    private final int maxLevels;
    private final String codeIdSuffic;
    private final String codeLabelSuffix;

    public AbstractCodeTitleConverter(String[] levelLabels, String codeIdSuffix, String codeLabelSuffix)
    {
        this.levelLabelLookup = new String[levelLabels.length + 1];
        System.arraycopy(levelLabels, 0, this.levelLabelLookup, 1, levelLabels.length);

        this.maxLevels = levelLabels.length;
        this.codeIdSuffic = codeIdSuffix;
        this.codeLabelSuffix = codeLabelSuffix;

    }

    protected <T, R extends CodeTitleLevelRecord> List<T> doConvertToObjects(
            Class<T> clazz, List<R> codeTitleRecords) throws JsonProcessingException {
        List<AllLevelsRecord> allLevelsRecords = doConvert(codeTitleRecords);
        List<Map<String,String>> listOfMaps = generateListOfMaps(allLevelsRecords);

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

    public List<Map<String,String>> generateListOfMaps(List<AllLevelsRecord> recordList)
    {
        List<Map<String,String>> listOfMaps = new ArrayList<>();

        String[] codeIdTitleArray = new String[maxLevels+1];
        String[] codeNameTitleArray = new String[maxLevels+1];
        Arrays.fill(codeIdTitleArray, "");
        Arrays.fill(codeNameTitleArray, "");

        for (int level = 1; level <= maxLevels; level++) {
            String levelFieldName = levelLabelLookup[level];
            if (levelFieldName != null) {
                codeIdTitleArray[level] = levelFieldName + codeIdSuffic;
                codeNameTitleArray[level] = levelFieldName + codeLabelSuffix;
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

}
