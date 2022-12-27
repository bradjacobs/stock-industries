package com.github.bradjacobs.stock.classifications.unspsc;

import bwj.util.excel.ExcelReader;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.classifications.common.TupleToPojoConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * SEE  https://www.ungm.org/Public/UNSPSC
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

        // Important Note:  cannot fully rely on the key/parentKey fields to build record hierarchy
        //    have found there's sometimes 'missing or incorrect' values, thus have to rely
        //    mostly on the codeId value to figure out tree structure.
        // with the exception that must rely on the key value for the top-most parent objects

        // split the list into 2 sub-lists
        //   [0] collection of parent records  &  [1] collection of all the other records
        List<List<RawUnspscRecord>> partitionLists = new ArrayList<>(
                rawRecords.stream()
                .collect(Collectors.partitioningBy(s -> !s.getParentKey().isEmpty()))
                        .values());

        // create special map of the 'parent objects'
        //     key --> record object  (using TreeMap to preserve sort key order)
        Map<String,RawUnspscRecord> parentKeyToRecordMap = partitionLists.get(0)
                .stream()
                .collect(Collectors.toMap(
                        RawUnspscRecord::getKey, Function.identity(),
                        (e1, e2) -> e1,
                        TreeMap::new)
                );

        // use a Set to 'de-dupe' entries
        Set<RawUnspscRecord> childRecords = new HashSet<>(partitionLists.get(1));

        // for all immediate children of the parent objects,
        // create a lookup map of the prefix (first part) of the code to parent key
        //    Example Record: {key:104269, parentKey:104, code:42000000}
        //       renders entry {key:"42", value:"104"}
        Map<String,String> prefixToRootParentKeyMap = childRecords
                .stream()
                .filter(k -> k.getParentKey().length() == 3)  // criteria to find 'immediate children of parents'
                .collect(Collectors.toMap(RawUnspscRecord::getCodeIdPrefix, RawUnspscRecord::getParentKey));

        // create a map where all records fall into a collection
        //  under the top-most parent 3-digit key
        Map<String, List<RawUnspscRecord>> categoryBucketMap = childRecords
                .stream()
                .collect(Collectors.groupingBy( k -> prefixToRootParentKeyMap.get(k.getCodeIdPrefix())));

        // Build final record list
        //   Iterate thru all the keys (which are in order)
        //     add the parent object, then add all its children in sorted order
        List<RawUnspscRecord> masterList = new ArrayList<>();
        for (String key : parentKeyToRecordMap.keySet()) {
            masterList.add( parentKeyToRecordMap.get(key) );
            List<RawUnspscRecord> recordList = categoryBucketMap.get(key);
            recordList.sort(Comparator.comparing(RawUnspscRecord::getCodeId));
            masterList.addAll(recordList);
        }

        // finally run the list of records thru the converter
        return TUPLE_TO_POJO_CONVERTER.doConvertToObjects(UnspscRecord.class, masterList);
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

        // gets the first 2 digits of the codeId
        public String getCodeIdPrefix() {
            if (code.isEmpty()) {
                return "";
            }
            return code.substring(0,2);
        }

        @Override
        public String getCodeTitle() {
            return title;
        }

        // NOTE: using equals/hashcode on the "CODE" field (instead of 'key')
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RawUnspscRecord)) return false;
            RawUnspscRecord that = (RawUnspscRecord) o;
            return code.equals(that.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(code);
        }

        @Override
        public int getCodeLevel() {
            return getLevel(this.code);
        }
    }

    /**
     * Calculate level depth based on the format of the codeId
     * @param codeId codeId
     * @return level
     */
    private static int getLevel(String codeId) {
        if (codeId.length() == 1) {
            return 1;
        }
        else if (codeId.length() == CODE_STR_LENGTH) {
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
