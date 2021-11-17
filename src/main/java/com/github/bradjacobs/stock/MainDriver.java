package com.github.bradjacobs.stock;

import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.DataConverterFactory;
import com.github.bradjacobs.stock.serialize.BaseSerializer;
import com.github.bradjacobs.stock.serialize.SerializerFactory;
import com.github.bradjacobs.stock.types.CsvDefinition;
import com.github.bradjacobs.stock.types.DataDefinition;
import com.github.bradjacobs.stock.types.JsonDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainDriver
{
    public static void main(String[] args) throws Exception
    {
        // still in demo-mode.....
        MainDriver driver = new MainDriver();
        driver.generateAllSources();
    }

    // set to true to create 'long description' output files, even if the data source doesn't have any.
    private final boolean serializeEmptyLongDescriptionFiles;

    public MainDriver() {
        this(false);
    }

    public MainDriver(boolean serializeEmptyLongDescriptionFiles) {
        this.serializeEmptyLongDescriptionFiles = serializeEmptyLongDescriptionFiles;
    }


    public void generateAllSources() throws Exception
    {
        List<DataDefinition> dataDefinitions = createPermutations();

        File outputDirectory = new File("./output");
        if (! outputDirectory.isDirectory()) {
            boolean success = outputDirectory.mkdir();
            if (! success) {
                throw new InternalError("Unable to create output directory.");
            }
        }

        for (Classification classification : Classification.values())
        {
            System.out.println("Creating files for: " + classification);

            DataConverter<?> converter = DataConverterFactory.createDataConverter(classification);
            List<?> recordList = converter.createDataRecords();

            for (DataDefinition dataDefinition : dataDefinitions)
            {
                BaseSerializer serializer = SerializerFactory.createSerializer(dataDefinition);

                String fileName = serializer.generateFileName(classification);

                if (shouldSerializeFile(classification, fileName))
                {
                    File outFile = new File(outputDirectory, fileName);
                    serializer.serializeToFile(outFile, recordList);
                }
            }
        }
    }


    // a little kludgy...  come back to this
    private boolean shouldSerializeFile(Classification classification, String fileName)
    {
        // for the case of
        //   1. classification doesn't have long descriptions available
        //   2. configured to not write long description files if not available
        //   3. this file is a long description file (based on file name)
        // then skip
        if (!serializeEmptyLongDescriptionFiles && !classification.isLongDescriptionAvailable() && fileName.contains("_desc")) {
            return false;
        }

        return true;
    }


    // todo - comments + documentation for below

    private static List<DataDefinition> createPermutations() {
        List<DataDefinition> resultList = new ArrayList<>();
        resultList.addAll(createCsvPermutations());
        resultList.addAll(createJsonPermutations());
        return resultList;
    }

    private static List<CsvDefinition> createCsvPermutations()
    {
        return Arrays.asList(
            CsvDefinition.builder().withLongDescriptions(false).makeSparsely(false).build(),
            CsvDefinition.builder().withLongDescriptions(false).makeSparsely(true).build(),
            CsvDefinition.builder().withLongDescriptions(true).makeSparsely(false).build(),
            CsvDefinition.builder().withLongDescriptions(true).makeSparsely(true).build()
        );
    }

    private static List<JsonDefinition> createJsonPermutations()
    {
        return Arrays.asList(
            JsonDefinition.builder().asPojoList().withLongDescriptions(true).build(),
            JsonDefinition.builder().asPojoList().withLongDescriptions(false).build(),
            JsonDefinition.builder().asTree().withCanonicalKeyNames().build(),
            JsonDefinition.builder().asTree().withNormalKeyNames().build(),
            JsonDefinition.builder().asTree().withGenericKeyNames().build()
        );
    }


    private static List<File> getFiles(String dir)
    {
        File outputDirectory = new File(dir);
        File[] files = outputDirectory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }
        List<File> fileList = new ArrayList<>(Arrays.asList(files));
        Collections.sort(fileList);
        return fileList;
    }


}
