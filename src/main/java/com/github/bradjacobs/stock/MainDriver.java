package com.github.bradjacobs.stock;

import com.github.bradjacobs.stock.classifications.Classification;
import com.github.bradjacobs.stock.classifications.DataConverter;
import com.github.bradjacobs.stock.classifications.DataConverterFactory;
import com.github.bradjacobs.stock.serialize.BaseSerializer;
import com.github.bradjacobs.stock.serialize.SerializerFactory;
import com.github.bradjacobs.stock.types.CsvDefinition;
import com.github.bradjacobs.stock.types.DataDefinition;
import com.github.bradjacobs.stock.types.JsonDefinition;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainDriver
{
    // todo - add in the permid column for the TRBC for the csv.


    public static void main(String[] args) throws Exception
    {
        // still in demo-mode.....
        MainDriver driver = new MainDriver();
        driver.generateAllSources();

        String dir1 = "/Users/bradjacobs/git/bradjacobs/stock-industries/output/";
        String dir2 = "/Users/bradjacobs/git/bradjacobs/stock-industries/output2/";

        List<File> files = getFiles(dir1);
        for (File file : files) {
            String fileName = file.getName();

            File matchFile = new File(dir2 + fileName);

            String data1 = FileUtils.readFileToString(file, Charset.defaultCharset());
            String data2 = FileUtils.readFileToString(matchFile,Charset.defaultCharset());

            String[] lines1 = data1.split("\n");
            String[] lines2 = data1.split("\n");

            boolean mismatchLine = false;
            boolean mismatchTotal = (lines1.length != lines2.length);

            int minCount = Math.min(lines1.length, lines2.length);
            for (int i = 0; i < minCount; i++)
            {
                String lineA = lines1[i];
                String lineB = lines2[i];

                if (! lineA.equals(lineB)) {
                    mismatchLine = true;
                }
            }

            if (mismatchTotal || mismatchLine) {
                System.out.println(fileName + "  NO MATCH");
            }
            else {
                System.out.println(fileName + "  MATCH");
            }


        }



    }


    // set to true if want to create 'long description' output files, even if the data source doesn't have any.
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
            //if (!classification.equals(Classification.CPC)) {
            if (!classification.equals(Classification.NACE)) {
                System.out.println("skip!");
                continue;
            }


            System.out.println("Creating files for: " + classification);

            DataConverter converter = DataConverterFactory.createDataConverter(classification);
            List<?> recordList = converter.createDataRecords();

            for (DataDefinition dataDefinition : dataDefinitions)
            {
                BaseSerializer serializer = SerializerFactory.createSerializer(dataDefinition);

                String fileName = serializer.generateFileName(classification);

                if (shouldSerialzeFile(classification, fileName))
                {
                    File outFile = new File(outputDirectory, fileName);
                    serializer.serializeToFile(outFile, recordList);
                }
            }
        }
    }


    // a little kludgy...  come back to this
    private boolean shouldSerialzeFile(Classification classification, String fileName)
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
