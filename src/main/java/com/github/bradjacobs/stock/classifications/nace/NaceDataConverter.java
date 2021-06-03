package com.github.bradjacobs.stock.classifications.nace;

//   file:///Users/bjacob101/Downloads/NACE_REV2_20210603_033502.htm

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;
import com.github.bradjacobs.stock.util.DownloadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
  NOTE: the 'CSV' file isn't true csv
 */

/**
 *
 */
public class NaceDataConverter extends BaseDataConverter<NaceRecord>
{
    // ***** NOTE *****
    //   when downloading the file with a GET (instead of a POST), seems to use semicolon ';' instead of comma ',' for separator
    private static final Character COLUMN_SEPARATOR = ';';


    public static void main(String[] args) throws IOException
    {
        NaceDataConverter converter = new NaceDataConverter();
        List<NaceRecord> recordList = converter.generateDataRecords();

        int kj = 33;
        System.out.println("kjkj");
    }

    @Override
    public Classification getClassification()
    {
        return Classification.NACE;
    }

    @Override
    public List<NaceRecord> generateDataRecords() throws IOException
    {
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(COLUMN_SEPARATOR);
        ObjectReader objReader = csvObjectMapper.readerFor(NacePojo.class).with(schema);

        String csvData = DownloadUtil.downloadFile(this.getClassification().getSourceFileLocation());

        MappingIterator<NacePojo> iterator = objReader.readValues(csvData);
        List<NacePojo> pojoList = iterator.readAll();

        List<NaceRecord> recordList = new ArrayList<>();
        NaceRecord currentRecord = new NaceRecord();

        for (NacePojo inputPojo : pojoList)
        {
            int level = inputPojo.level;
            String code = inputPojo.code;
            String name = cleanValue(inputPojo.description);

            if (! currentRecord.getClassCode().isEmpty()) {
                recordList.add(currentRecord);
                currentRecord = currentRecord.copy(level);
            }

            if (level == 1) {
                currentRecord.setSectionCode(code);
                currentRecord.setSectionName(name);
            }
            else if (level == 2) {
                currentRecord.setDivisionCode(code);
                currentRecord.setDivisionName(name);
            }
            else if (level == 3) {
                currentRecord.setGroupCode(code);
                currentRecord.setGroupName(name);
            }
            else if (level == 4) {
                currentRecord.setClassCode(code);
                currentRecord.setClassName(name);
                currentRecord.setDescription(null);  // todo
            }
        }

        recordList.add(currentRecord);

        return recordList;
    }


    private static class NacePojo
    {
        @JsonProperty("Order") Integer order;
        @JsonProperty("Level") Integer level;
        @JsonProperty("Code") String code;
        @JsonProperty("Parent") String parent;
        @JsonProperty("Description") String description;
        @JsonProperty("This item includes") String includes;
        @JsonProperty("This item also includes") String alsoIncludes;
        @JsonProperty("Rulings") String rulings;
        @JsonProperty("This item excludes") String excludes;
        @JsonProperty("Reference to ISIC Rev. 4") String refToIsic;
    }
}
