package com.github.bradjacobs.stock.classifications.common;

import com.github.bradjacobs.stock.classifications.DataFileType;
import com.github.bradjacobs.stock.classifications.common.CanonicalHeaderUpdater;
import com.github.bradjacobs.stock.classifications.common.objects.ActivityNode;
import com.github.bradjacobs.stock.classifications.common.objects.GroupNode;
import com.github.bradjacobs.stock.classifications.common.objects.IndustryNode;
import com.github.bradjacobs.stock.classifications.common.objects.SectorNode;
import com.github.bradjacobs.stock.classifications.common.objects.SubIndustryNode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract public class BaseDataConverter<T>
{
    abstract public String getFilePrefix();

    abstract public List<T> generateDataRecords() throws IOException;

    private final CsvMapper csvObjectMapper;
    private final CsvMapper csvArrayMapper;
    private final File outputDirectory;

    public BaseDataConverter()
    {
        this.csvObjectMapper = createCsvObjectMapper();
        this.csvArrayMapper = createCsvArrayMapper();

        outputDirectory = new File("./output");
        if (! outputDirectory.isDirectory()) {
            boolean success = outputDirectory.mkdir();
            if (! success) {
                throw new InternalError("Unable to create output directory.");
            }
        }
    }

    /**
     * Fetches data and creates multiple CSV/JSON files representing the sector/industry definitions.
     * @throws IOException
     */
    public void createDataFiles() throws IOException
    {
        // create data records
        List<T> records = generateDataRecords();

        // save the data into full CSV file format
        writeFullCsv(records);

        // read in the generated CSV file into a 2-D string array
        File fullCsv = createFileObject(DataFileType.FULL_CSV);
        String[][] fullArray = csvArrayMapper.readValue(fullCsv, String[][].class);


        String[][] sparseArray = createSparseCsvArray(fullArray);

        // save the data sparse CSV file format
        writeSparseCsv(sparseArray);

        // save the data canonical JSON format
        writeCanonicalJsonTree(sparseArray);


        // todo:  below is (even more) kludgy (but startring with something that just works for now)
        //
        File canonicalFile = createFileObject(DataFileType.CANONICAL_TREE_JSON);
        if (! canonicalFile.exists()) {
            throw new InternalError("Canonical Json file was expected to exist!");
        }

        String canonicalJson = new String ( Files.readAllBytes( Paths.get(canonicalFile.getAbsolutePath()) ) );

        CanonicalHeaderUpdater canonicalHeaderUpdater = new CanonicalHeaderUpdater(fullArray[0]);

        // create json tree using 'original' labels (that are unique for each customer)
        String origJson = canonicalHeaderUpdater.convertJsonKeyNames(canonicalJson, DataFileType.TREE_JSON);
        File jsonFile = createFileObject(DataFileType.TREE_JSON);
        FileUtils.writeStringToFile(jsonFile, origJson, StandardCharsets.UTF_8);

        // create another json tree using basic/generic names (so all 'nodes' have same labels)
        String basicJson = canonicalHeaderUpdater.convertJsonKeyNames(canonicalJson, DataFileType.BASIC_TREE_JSON);
        File basicJsonFile = createFileObject(DataFileType.BASIC_TREE_JSON);
        FileUtils.writeStringToFile(basicJsonFile, basicJson, StandardCharsets.UTF_8);
    }


    private void writeFullCsv(List<T> records) throws IOException
    {
        Class<T> clazz = getParameterizedClass();

        File fullCsv = createFileObject(DataFileType.FULL_CSV);

        CsvSchema schema = csvObjectMapper.schemaFor(clazz).withHeader();
        ObjectWriter writer = csvObjectMapper.writerFor(clazz).with(schema);
        writer.writeValues(fullCsv).writeAll(records);
    }

    private void writeSparseCsv(String[][] sparseArray) throws IOException
    {
        File sparseCsv = createFileObject(DataFileType.SPARSE_CSV);
        CsvSchema schema = csvArrayMapper.schema();
        ObjectWriter writer = csvArrayMapper.writer(schema);
        writer.writeValues(sparseCsv).writeAll(sparseArray);
    }


    private void writeCanonicalJsonTree(String[][] sparseArray) throws IOException
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


        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

        // note: avoid marshalling out an empty array.
        mapper = mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);


        File canonicalJsonFile = createFileObject(DataFileType.CANONICAL_TREE_JSON);

        try (FileOutputStream fileOutputStream = new FileOutputStream(canonicalJsonFile)) {
            mapper.writeValue(fileOutputStream, sectorNodeList);
        }
    }



    private File createFileObject(DataFileType dataFileType) {
        String fileName = this.getFilePrefix() + dataFileType.getSuffix();
        return new File(this.outputDirectory, fileName);
    }


    private String[][] createSparseCsvArray(String[][] dataArray)
    {
        int rowCount = dataArray.length;
        int colCount = dataArray[0].length;

        // create a new 2-D matrix with the same size as original
        String[][] sparseDataArray = new String[rowCount][colCount];

        // start by just copying over the first row (header row) from the original dataArray
        sparseDataArray[0] = Arrays.copyOf(dataArray[0], colCount);

        // Note: starting at index 1
        for (int i = 1; i < rowCount; i++)
        {
            String[] previousRow = dataArray[i-1];
            String[] currentRow = dataArray[i];

            for (int j = 0; j < colCount; j++)
            {
                String prevCell = previousRow[j];
                String currCell = currentRow[j];

                if (currCell.equals(prevCell)) {
                    sparseDataArray[i][j] = "";
                }
                else {
                    sparseDataArray[i][j] = currCell;
                }
            }
        }

        return sparseDataArray;
    }


    protected String cleanValue(String input)
    {
        if (input == null) {
            return "";
        }

        String result = cleanWhitespace(input);

        // todo - add here

        return result;
    }


    protected String cleanWhitespace(String input) {
        // replace 2 or more adjacent spaces w/ a single space (i.e. "aaa   bbb" -> "aaa bbb")
        //   as well as replace any \n, \r, \t, etc w/ space
        String result = input.replaceAll("\\s+", " ");
        return result.trim();
    }


    ///////////////////////////////////////////////////////////////////


    protected CsvMapper createCsvObjectMapper() {
        return createDefaultCsvMapperBuilder().build();
    }

    protected CsvMapper createCsvArrayMapper() {
        // Important Note:
        //   must create a new builder (and can NOT 'reuse' a builder)
        //     b/c csv builder.build() will give the same instance (and not a new one)
        CsvMapper.Builder builder = createDefaultCsvMapperBuilder();
        builder.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        return builder.build();
    }

    protected CsvMapper.Builder createDefaultCsvMapperBuilder() {
        return CsvMapper.builder()
            .enable(CsvParser.Feature.SKIP_EMPTY_LINES)
            .enable(CsvParser.Feature.TRIM_SPACES)
            .enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)  // todo - double check
            .enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY); // ALWAYS disable this (or it can change the column order)
    }

    @SuppressWarnings("unchecked")
    private Class<T> getParameterizedClass() {
        return (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
    }

}