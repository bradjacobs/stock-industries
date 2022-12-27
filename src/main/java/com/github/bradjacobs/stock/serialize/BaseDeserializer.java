package com.github.bradjacobs.stock.serialize;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

abstract public class BaseDeserializer
{
    abstract protected <T> List<T> deserializeObjects(Class<T> clazz, String data) throws IOException;

    public <T> List<T> deserializeFromFile(Class<T> clazz, String filePath) throws IOException {
        return deserializeFromFile(clazz, new File(filePath));
    }

    public <T> List<T> deserializeFromFile(Class<T> clazz, File file) throws IOException {
        String fileData = readFile(file);
        return deserializeObjects(clazz, fileData);
    }

    protected String readFile(File file) throws IOException {
        validateFile(file);
        return new String ( Files.readAllBytes( Paths.get(file.getAbsolutePath()) ) );
    }

    protected void validateFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Must provide a file");
        }
        if (!file.exists()) {
            throw new IllegalArgumentException("Unable to read file.  File doesn't exist: " + file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new IllegalArgumentException("Unable to read file.  Directory was given instead of file: " + file.getAbsolutePath());
        }
    }
}
