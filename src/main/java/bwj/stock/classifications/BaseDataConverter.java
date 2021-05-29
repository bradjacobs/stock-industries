package bwj.stock.classifications;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
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
        Class<T> clazz = getParameterizedClass();

        // create data records
        List<T> records = generateDataRecords();


        // save data into full CSV format
        File fullCsv = createFileObject(DataFileType.FULL_CSV);
        CsvSchema schema = csvObjectMapper.schemaFor(clazz).withHeader();
        ObjectWriter writer = csvObjectMapper.writerFor(clazz).with(schema);
        writer.writeValues(fullCsv).writeAll(records);


        // read the generated CSV file into a 2-D string array
        String[][] fullArray = csvArrayMapper.readValue(fullCsv, String[][].class);


        // convert to a sparse CSV 2-d array and then save
        File sparseCsv = createFileObject(DataFileType.SPARSE_CSV);
        String[][] sparseArray = createSparseCsvArray(fullArray);
        schema = csvArrayMapper.schema();
        writer = csvArrayMapper.writer(schema);
        writer.writeValues(sparseCsv).writeAll(sparseArray);



        // todo - more to come

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
            .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY); // ALWAYS disable this (or it will change the column order)
    }

    @SuppressWarnings("unchecked")
    private Class<T> getParameterizedClass() {
        return (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[0];
    }

}
