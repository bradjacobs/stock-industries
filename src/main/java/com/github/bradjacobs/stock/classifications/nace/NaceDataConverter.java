package com.github.bradjacobs.stock.classifications.nace;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.classifications.BaseDataConverter;
import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.util.DownloadUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  NOTE1: the 'CSV' file isn't true csv
 *  NOTE2: skipping the 'long description' b/c it's crazy long and not very useful.
 */
public class NaceDataConverter extends BaseDataConverter<NaceRecord>
{
    // ***** NOTE *****
    //   when downloading the file with a GET (instead of a POST), seems to use semicolon ';' instead of comma ',' for separator
    private static final Character COLUMN_SEPARATOR = ';';

    @Override
    public Classification getClassification()
    {
        return Classification.NACE;
    }

    @Override
    public List<NaceRecord> createDataRecords() throws IOException
    {
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(COLUMN_SEPARATOR);
        CsvMapper csvObjectMapper = MapperBuilder.csv().setArrayWrap(false).build();

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
