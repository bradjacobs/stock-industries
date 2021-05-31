package com.github.bradjacobs.stock.classifications.isic;

import com.github.bradjacobs.stock.classifications.common.BaseDataConverter;
import com.github.bradjacobs.stock.util.PdfUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
Dev note:  copied over from another project
TODO
   need to figure out what is going on and refactor and comment
    b/c there's 'magic' involved currently.
 */
public class IsicDataConverter extends BaseDataConverter<IsicRecord>
{
    private static final String SOURCE_FILE = "https://unstats.un.org/unsd/publication/seriesM/seriesm_4rev4e.pdf";


    @Override
    public String getFilePrefix()
    {
        return "isic";
    }

    @Override
    public List<IsicRecord> generateDataRecords() throws IOException
    {
        URL url = new URL(SOURCE_FILE);
        String[] pdfLines = PdfUtil.getPdfFileLines(url.openStream());
        return generateDataRecords(pdfLines);
    }

    private boolean isTableColumnHeaderRow(String line)
    {
        return line.equalsIgnoreCase("Division Group Class Description");
    }
    

    public List<IsicRecord> generateDataRecords(String[] lines) throws IOException
    {
        List<IsicRecord> recordList = new ArrayList<>();

        String sectionId = "";
        String sectionName = "";
        String divisionId = "";
        String divisionName = "";
        String groupId = "";
        String groupName = "";
        String classId = "";
        String className = "";

        lines = getDataLinesSection(lines);

        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();

            if (isTableColumnHeaderRow(line)) {
                continue;
            }
            if (line.length() <= 2) {
                // most likely a page number
                continue;
            }

            // todo... what is with the "9"
            if (line.length() == 9 && line.toLowerCase().startsWith("section"))
            {
                sectionId = line.substring(line.length() -1).toUpperCase();
                sectionName = lines[++i].trim();

                // NOTE: cannot 'assume' that the section name is contained to 1 line.
                //   (i.e. section T).   For simplicity going to 'assume' the section
                //   name is a max of 2 lines  (rather than N lines)
                String nextLine = lines[i+1].trim();

                if (! isTableColumnHeaderRow(nextLine))
                {
                    sectionName = sectionName + " " + nextLine;
                    i++;
                }
                sectionName = cleanValue(sectionName);
            }
            else if (line.startsWith("Division "))
            {
                int divisionIdIndexStart = "Division ".length();
                int divisionIdIndexEnd = line.indexOf(" ", divisionIdIndexStart+1);
                divisionId = line.substring(divisionIdIndexStart, divisionIdIndexEnd);
                divisionName = line.substring(divisionIdIndexEnd+1);

                // NOTE: cannot 'assume' that the division name is contained to 1 line.
                //   (i.e. Division 16).   For simplicity going to 'assume' the division
                //   name is a max of 2 lines  (rather than N lines)
                String nextLine = lines[i+1].trim();

                String[] lineElements = nextLine.split(" ");
                String firstElement = lineElements[0];

                if (! StringUtils.isNumeric(firstElement))
                {
                    divisionName = divisionName + " " + nextLine;
                    i++;
                }

                divisionName = cleanValue(divisionName);
            }
            else
            {
                String[] lineElements = line.split(" ");
                String element1 = lineElements[0];

                if (lineElements.length < 2)
                {
                    // skip!!
                    continue;
                }

                String element2 = lineElements[1];

                if (StringUtils.isNumeric(element1))
                {
                    String lastNumber = element1;
                    if (StringUtils.isNumeric(element2)) {
                        lastNumber = element2;
                    }

                    int nameStartIndex = line.indexOf(lastNumber) + lastNumber.length() + 1;
                    String name = line.substring(nameStartIndex);

                    // NOTE: cannot 'assume' that the Group/Class Description is contained to 1 line.
                    //   (i.e. Group 151, Class 1629).   For simplicity going to 'assume' the
                    //   name is a max of 2 lines  (rather than N lines)
                    String nextLine = lines[i+1].trim();
                    String[] nextLineElements = nextLine.split(" ");
                    String nextLineFirstElement = nextLineElements[0];

                    if ( !nextLineFirstElement.equalsIgnoreCase("Division") &&
                        !StringUtils.isNumeric(nextLineFirstElement) &&
                        !nextLineFirstElement.equalsIgnoreCase("International") &&
                        !nextLineFirstElement.equalsIgnoreCase("Detailed") &&
                        !nextLineFirstElement.equalsIgnoreCase("Section"))
                    {
                        name = name + " " + nextLine;
                        i++;
                    }

                    // todo - fix potential bug if entry is before a new section

                    name = cleanValue(name);

                    boolean groupAssigned = false;
                    boolean classAssigned = false;

                    if (element1.length() == 3)
                    {
                        groupId = element1;
                        groupName = name;
                        groupAssigned = true;
                    }
                    if (element1.length() == 4)
                    {
                        classId = element1;
                        className = name;
                        classAssigned = true;
                    }
                    else if (StringUtils.isNumeric(element2))
                    {
                        classId = element2;
                        className = name;
                        classAssigned = true;
                    }

                    if (classAssigned)
                    {
//                            System.out.println(String.format("****\n%s:%s\n%s:%s\n%s:%s\n%s:%s\n\n",
//                                sectionId, sectionName, divisionId, divisionName,
//                                groupId, groupName, classId, className));

                        IsicRecord record = new IsicRecord(sectionId, sectionName, divisionId, divisionName, groupId, groupName, classId, className);
                        recordList.add(record);
                    }
                }
                else
                {
                    // todo - this "shouldn't happen"
                }
            }
        }

        return recordList;
    }



    /**
     * Examines the lines and returns a subset of the array that contain the lines with desired data
     * @param lines
     * @return subset of line array with the desired data definition info.
     */
    private String[] getDataLinesSection(String[] lines)
    {
        int indexStart = 0;
        int indexEnd = 0;
        for (int i = 0; i < lines.length; i++)
        {
            String line = lines[i].trim();
            if (line.equalsIgnoreCase("Chapter II"))
            {
                if (lines[i+1].trim().equalsIgnoreCase("Detailed structure")) {
                    i++;
                    indexStart = i;
                    continue;
                }
            }
            if (line.equalsIgnoreCase("Part Three"))
            {
                if (lines[i+1].trim().equalsIgnoreCase("Detailed structure and explanatory"))
                {
                    indexEnd = i;
                }
            }
        }

        if (indexStart == 0 || indexEnd == 0 || indexStart >= indexEnd) {
            throw new InternalError("Unabel to find data line subsection of file!");
        }

        List<String> lineList = Arrays.asList(lines);
        List<String> subList = lineList.subList(indexStart, indexEnd);

        return subList.toArray(new String[0]);
    }

}
