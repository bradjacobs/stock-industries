package com.github.bradjacobs.stock.serialize;

import com.github.bradjacobs.stock.classifications.Classification;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

abstract public class BaseSerializer
{
    abstract protected <T> String serializeObjects(List<T> objectList) throws IOException;

    abstract public String generateFileName(Classification classification);

    public <T> String serialize(List<T> objectList) throws IOException
    {
        validateObjectList(objectList);
        return serializeObjects(objectList);
    }

    public <T> void serializeToFile(File file, List<T> objectList) throws IOException
    {
        validateFile(file);
        String fileContent = serialize(objectList);
        FileUtils.writeStringToFile(file, fileContent, StandardCharsets.UTF_8);
    }


    protected <T> Class<?> identifyClass(List<T> objectList) {
        if (objectList == null || objectList.isEmpty()) {
            throw new IllegalArgumentException("cannot check class type of an empty object list.");
        }
        return objectList.get(0).getClass();
    }



    protected void validateFilePath(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("Must provide a complete filepath");
        }
    }

    protected void validateFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Must provide a file");
        }
    }

    protected <T> void validateObjectList(List<T> objectList) {
        if (objectList == null) {
            throw new IllegalArgumentException("Cannot serialize a null objectList");
        }
        if (objectList.isEmpty()) {
            //   note:  might change to 'allow' this (maybe empty list becomes empty string)
            //    but can't see reason to support that scenario......yet
            throw new IllegalArgumentException("Must provide 1 or more objects to be serialized");
        }

        // check for nulls in list  (never want to worry about this scenario ever again)
        for (T object : objectList) {
            if (object == null) {
                throw new IllegalArgumentException("ObjectList contains 1 or more 'null' object values");
            }
        }
    }

}
