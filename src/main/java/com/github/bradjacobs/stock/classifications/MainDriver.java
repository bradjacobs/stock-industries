package com.github.bradjacobs.stock.classifications;

import com.github.bradjacobs.stock.serialize.BaseSerializer;
import com.github.bradjacobs.stock.serialize.SerializerFactory;
import com.github.bradjacobs.stock.types.CsvDefinition;
import com.github.bradjacobs.stock.types.DataDefinition;
import com.github.bradjacobs.stock.types.JsonDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainDriver
{

    public static void main(String[] args) throws Exception
    {
        // still in demo-mode.....

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
            if (! classification.equals(Classification.GICS)) {
                continue;
            }

            System.out.println("Creating files for: " + classification);


            DataConverter converter = DataConverterFactory.createDataConverter(classification);
            List<?> recordList = converter.createDataRecords();

            for (DataDefinition dataDefinition : dataDefinitions)
            {
                BaseSerializer serializer = SerializerFactory.createSerializer(dataDefinition);

                String fileName = serializer.createFileName(classification);
                File outFile = new File(outputDirectory, fileName);

                serializer.serializeToFile(outFile, recordList);
            }
        }
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

}
