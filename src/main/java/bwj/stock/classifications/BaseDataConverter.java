package bwj.stock.classifications;

import bwj.stock.classifications.common.objects.ActivityNode;
import bwj.stock.classifications.common.objects.GroupNode;
import bwj.stock.classifications.common.objects.IndustryNode;
import bwj.stock.classifications.common.objects.SectorNode;
import bwj.stock.classifications.common.objects.SubIndustryNode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
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
        outputDirectory.mkdir();
    }

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


        // todo - more to come

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
