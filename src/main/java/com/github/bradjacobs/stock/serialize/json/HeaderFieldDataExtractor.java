package com.github.bradjacobs.stock.serialize.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class HeaderFieldDataExtractor
{
    public <T> String[] getHeaderFields(Class<T> clazz)
    {
        String[] headerValues = null;
        JsonPropertyOrder propOrderAnnotation = clazz.getAnnotation(JsonPropertyOrder.class);
        if (propOrderAnnotation != null)
        {
            boolean isAlphabetic = propOrderAnnotation.alphabetic();
            if (isAlphabetic) {
                // currently not supported
                throw new IllegalArgumentException("'JsonPropertyOrder.alphabetic = true' is currently unsupported.");
            }
            headerValues = propOrderAnnotation.value();
        }

        if (headerValues == null || headerValues.length == 0) {
            throw new IllegalArgumentException("class is missing 'JsonPropertyOrder' annotation: " + clazz.getCanonicalName());
        }

        return headerValues;
    }

}
