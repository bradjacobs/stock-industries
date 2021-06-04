package com.github.bradjacobs.stock.classifications;

import java.io.IOException;
import java.util.List;


// TODO - after the refactor this abstract class is now a candidate for removal.
abstract public class BaseDataConverter<T> implements DataConverter<T>
{
    public BaseDataConverter()
    {
    }

    abstract public Classification getClassification();

    abstract public List<T> createDataRecords() throws IOException;


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

}
