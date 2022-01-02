package com.github.bradjacobs.stock.classifications.naics;

import bwj.util.excel.ExcelReader;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.common.CodeTitleLevelRecord;
import com.github.bradjacobs.stock.classifications.common.TupleToPojoConverter;
import com.github.bradjacobs.stock.serialize.csv.CsvDeserializer;
import com.github.bradjacobs.stock.serialize.csv.CsvMatrixConverter;
import com.github.bradjacobs.stock.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * NOTE:
 *     https://www.census.gov/naics/?48967
 *
 *   these 2 file types have code values in different columns:
 *      https://www.census.gov/naics/2017NAICS/2-6%20digit_2017_Codes.xlsx
 *      https://www.census.gov/naics/2022NAICS/2-6%20digit_2022_Codes.xlsx
 *        vs
 *      https://www.census.gov/naics/2017NAICS/2017_NAICS_Descriptions.xlsx
 *         (no 2022 link....at least as of last time checked on dec 2021)
 */
public class NaicsDataConverter implements DataConverter<NaicsRecord>
{
    private static final TupleToPojoConverter TUPLE_TO_POJO_CONVERTER = new TupleToPojoConverter();
    private static final int IGNORABLE_ID_LENGTH = 5; // ignore all codes of this length

    @Override
    public Classification getClassification()
    {
        return Classification.NAICS;
    }

    @Override
    public List<NaicsRecord> createDataRecords() throws IOException
    {
        ExcelReader excelReader = ExcelReader.builder().build();
        String csvData = excelReader.convertToCsvText(getClassification().getSourceFileLocation());

        List<RawNaicsRecord> rawRecords = convertToRawRecords(csvData);
        List<RawNaicsRecord> claanRecords = sanitizeList(rawRecords);

        List<NaicsRecord> resultRecords = TUPLE_TO_POJO_CONVERTER.doConvertToObjects(NaicsRecord.class, claanRecords);

        // the final step it to 'tack on' the long descriptions
        Map<String, String> lookupMap = claanRecords.stream()
                .collect(Collectors.toMap(RawNaicsRecord::getCodeId, RawNaicsRecord::getDescription));
        for (NaicsRecord fullRecord : resultRecords) {
            fullRecord.setDescription(lookupMap.get(fullRecord.getIndustryId()));
        }

        return resultRecords;
    }


    private List<RawNaicsRecord> sanitizeList(List<RawNaicsRecord> inputRecords)
    {
        List<RawNaicsRecord> filteredList = inputRecords.stream().sequential()
                .filter(r -> (r.getCodeId().length() != IGNORABLE_ID_LENGTH || !StringUtils.isNumeric(r.getCodeId())))
                .collect(Collectors.toList());

        filteredList.forEach(r -> {
            r.setCodeTitle(cleanValue(r.getCodeTitle()));
            r.setDescription(cleanDescriptionValue(r.getDescription()));
        });
        return filteredList;
    }

    protected String cleanValue(String input)
    {
        String cleanedValue = StringUtil.cleanWhitespace(input);

        // remove any trailing capital 'T' (if exists)
        if (cleanedValue.endsWith("T")) {
            cleanedValue = cleanedValue.substring(0, cleanedValue.length() - 1);
        }
        return cleanedValue;
    }

    protected String cleanDescriptionValue(String description) {
        if (StringUtils.isEmpty(description)) {
            return "";
        }

        // the "illustrative examples" are usually more clutter than they are worth,
        //   BUT, may change decision on this at a later time.
        int examplesIndex = description.indexOf("Illustrative Examples");
        if (examplesIndex > 0) {
            description = description.substring(0, examplesIndex);
        }

        // the downloaded file doesn't actually have any information after "Cross-References"
        //  so just remove that key word, if exists
        int crossReferencesIndex = description.indexOf("Cross-References");
        if (crossReferencesIndex > 0) {
            description = description.substring(0, crossReferencesIndex);
        }

        return cleanValue(description);
    }

    private List<RawNaicsRecord> convertToRawRecords(String csvData) throws IOException
    {
        List<RawNaicsRecord> rawRecords;
        if (csvData.startsWith("Seq") || csvData.startsWith("\"Seq"))
        {
            // the "2-6_digit_xxx_Codes" file is badly formatted, and trying to load the data into a Pojo
            //  was more heartache than desired.  Thus workaround.
            String[][] matrix = CsvMatrixConverter.convertToMatrix(csvData);
            int codeColumnIndex = 1;
            int titleColumnIndex = 2;
            // todo - move hardcode numbers to better spot

            rawRecords = new ArrayList<>();
            for (int i = 1; i < matrix.length; i++)
            {
                String code = matrix[i][codeColumnIndex].trim();
                String title = matrix[i][titleColumnIndex].trim();
                if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(title)) {
                    rawRecords.add(new RawNaicsRecord(code, title));
                }
            }
        }
        else {
            CsvDeserializer csvDeserializer = new CsvDeserializer();
            rawRecords = csvDeserializer.csvToObjectList(RawNaicsRecord.class, csvData);
        }
        return rawRecords;
    }


    private static class RawNaicsRecord implements CodeTitleLevelRecord
    {
        @JsonProperty("Code")
        private String code;
        @JsonProperty("Title")
        private String title;
        @JsonProperty("Description")
        private String description;

        public RawNaicsRecord() {
        }

        public RawNaicsRecord(String code, String title) {
            this.code = code;
            this.title = title;
            this.description = "";
        }

        @Override
        public String getCodeId() { return code; }
        @Override
        public String getCodeTitle() { return title; }
        public String getDescription() { return description; }

        public void setCodeTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }

        @Override
        @JsonIgnore
        public int getCodeLevel() {
            // NOTE: there are some code value 'exceptions' that are actually ranges for sectors:
            //   e.g.    31-33  Manufacturing,   44-45  Retail Trade,   etc
            if (code.contains("-")) {
                return 1;  // top level
            }
            else if (code.length() == 6) {
                return 4;  // special case length 6 is level 4
            }
            else {
                // all others levels are length-1  (i.e. "1111" --> level 3)
                return code.length() - 1;
            }
        }
    }
}
