package com.github.bradjacobs.stock.serialize;

import com.github.bradjacobs.stock.types.CsvDefinition;
import com.github.bradjacobs.stock.types.DataDefinition;
import com.github.bradjacobs.stock.types.JsonDefinition;

public class SerializerFactory
{
    private SerializerFactory() { }

    public static BaseSerializer createSerializer(DataDefinition dataDefinition)
    {
        if (dataDefinition == null) {
            throw new IllegalArgumentException("Must provide a dataDefinition!");
        }

        if (dataDefinition instanceof CsvDefinition) {
            return new CsvSerializer((CsvDefinition)dataDefinition);
        }
        else if (dataDefinition instanceof JsonDefinition) {
            return new JsonSerializer((JsonDefinition)dataDefinition);
        }
        else {
            throw new IllegalArgumentException("Unrecognized dataDefinition type: " + dataDefinition.getClass());
        }
    }


    public static CsvSerializer createCsvSerializer(CsvDefinition csvDefinition) {
        return new CsvSerializer((CsvDefinition)csvDefinition);
    }

}
