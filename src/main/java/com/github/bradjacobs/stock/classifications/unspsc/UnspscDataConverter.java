package com.github.bradjacobs.stock.classifications.unspsc;

import bwj.util.excel.ExcelReader;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.classifications.common.TupleToPojoConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 *
 * SEE  https://www.ungm.org/Public/UNSPSC
 */


/*
SIDE NOTE:  duplicate "Combination volume expanders" will be removed

 E  -  Medical, Laboratory & Test Equipment & Supplies & Pharmaceuticals
   51000000  -  Drugs and Pharmaceutical Products
       51130000  -  Hematolic drugs
           51132000  -  Hemorrheologic agents
               51132001  -  Pentoxifylline
               51132300  -  Combination volume expanders    <=******
            ....
           51132200  -  Combination non-opioid analgesics
           51132300  -  Combination volume expanders        <=******
               51132308  -  Idarucizumab
 */
public class UnspscDataConverter implements DataConverter<UnspscRecord>
{
    private static final int CODE_STR_LENGTH = 8;
    private static final TupleToPojoConverter TUPLE_TO_POJO_CONVERTER = new TupleToPojoConverter();

    @Override
    public Classification getClassification()
    {
        return Classification.UNSPSC;
    }

    @Override
    public List<UnspscRecord> createDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().setSkipEmptyRows(true).build();
        String csvData = excelReader.convertToCsvText(getClassification().getSourceFileLocation());

        CsvDeserializer csvDeserializer = new CsvDeserializer();
        List<RawUnspscRecord> rawRecords = csvDeserializer.csvToObjectList(RawUnspscRecord.class, csvData);

        // need a custom comparator for special logic for sorting records.
        CustomRawUnspscRecordComparator comparator = new CustomRawUnspscRecordComparator(rawRecords);

        List<RawUnspscRecord> dedupedRecords = removeDuplicateCodeRecords(rawRecords);
        dedupedRecords.sort(comparator);

        return TUPLE_TO_POJO_CONVERTER.doConvertToObjects(UnspscRecord.class, dedupedRecords);
    }


    /**
     * Remove any duplicate records that have the same CODE_ID
     *   (not be be confused with the 'key' field which is also unique)
     * @param rawRecords recordList
     * @return List of records that all have unique codeId
     */
    private List<RawUnspscRecord> removeDuplicateCodeRecords(List<RawUnspscRecord> rawRecords) {
        Map<String,RawUnspscRecord> codeRecordMap = new HashMap<>();
        for (RawUnspscRecord record : rawRecords) {
            if (!codeRecordMap.containsKey(record.getCodeId())) {
                codeRecordMap.put(record.getCodeId(), record);
            }
        }
        return new ArrayList<>(codeRecordMap.values());
    }


    /**
     * Special Comparator for RawUnspscRecord object because can NOT sort on the codeId alone.
     */
    private static class CustomRawUnspscRecordComparator implements Comparator<RawUnspscRecord> {

        private final SectorCodeLookup sectorCodeLookup;

        public CustomRawUnspscRecordComparator(List<RawUnspscRecord> rawRecords) {
            this.sectorCodeLookup = new SectorCodeLookup(rawRecords);;
        }

        @Override
        public int compare(RawUnspscRecord record1, RawUnspscRecord record2) {
            String sector1 = sectorCodeLookup.getRootSectorLetter(record1);
            String sector2 = sectorCodeLookup.getRootSectorLetter(record2);
            int compareValue = sector1.compareTo(sector2);
            if (compareValue == 0) {
                compareValue = Integer.compare(record1.getCodeId().length(), record2.getCodeId().length());
                if (compareValue == 0) {
                    compareValue = record1.getCodeId().compareTo(record2.getCodeId());
                }
            }
            return compareValue;
        }
    }


    private static class RawUnspscRecord implements CodeTitleLevelRecord {
        @JsonProperty("Key")
        private String key;
        @JsonProperty("Parent Key")
        @JsonAlias("Parent key")
        private String parentKey;
        @JsonProperty("Code")
        private String code;
        @JsonProperty("Title")
        private String title;

        public String getKey() { return key; }
        public String getParentKey() { return parentKey; }

        @Override
        public String getCodeId() {
            return code;
        }

        @Override
        public String getCodeTitle() {
            return title;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RawUnspscRecord)) return false;
            RawUnspscRecord that = (RawUnspscRecord) o;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }

        @Override
        public int getCodeLevel() {
            return getLevel(this.code);
        }
    }


    // todo - could be cleaner
    private static class SectorCodeLookup
    {
        private final Map<String,RawUnspscRecord> keyRecordLookuMap = new HashMap<>();
        private final Map<String,String> keyToRootSectorLetterMap = new HashMap<>();

        public SectorCodeLookup(List<RawUnspscRecord> inputList) {
            for (RawUnspscRecord record : inputList) {
                keyRecordLookuMap.put(record.getKey(), record);
            }
        }

        public String getRootSectorLetter(RawUnspscRecord record) {
            String sectorLetter = keyToRootSectorLetterMap.get(record.getKey());
            if (sectorLetter == null)
            {
                if (StringUtils.isEmpty(record.getParentKey())) {
                    sectorLetter = record.getCodeId();
                }
                else {
                    RawUnspscRecord parent = keyRecordLookuMap.get(record.getParentKey());
                    sectorLetter =  getRootSectorLetter(parent);
                }
                keyToRootSectorLetterMap.put(record.getKey(), sectorLetter);
            }
            return sectorLetter;
        }
    }


    /**
     * Calculate level depth based on the format of the codeId
     * @param codeId codeId
     * @return level
     */
    private static int getLevel(String codeId)
    {
        if (codeId.length() == 1) {
            return 1;
        }
        else if (codeId.length() == CODE_STR_LENGTH)
        {
            char[] codeChars = codeId.toCharArray();
            for (int i = codeChars.length - 1; i >= 0; i--) {
                // 0 1 2 3 4 5 6 7  (index of first non-zero char (from right to left)
                // 2 2 3 3 4 4 5 5  (level value)
                //    example:  "46181500"  right-most non-zero char at index 5, so level = 4
                char c = codeChars[i];
                if (c != '0') {
                    return ((i+4)/2);
                }
            }
        }
        return -1;
    }
}
