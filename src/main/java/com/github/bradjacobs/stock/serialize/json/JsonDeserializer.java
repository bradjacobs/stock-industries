package com.github.bradjacobs.stock.serialize.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.bradjacobs.stock.MapperBuilder;
import com.github.bradjacobs.stock.serialize.BaseDeserializer;
import com.github.bradjacobs.stock.types.JsonDefinition;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.util.List;

public class JsonDeserializer extends BaseDeserializer
{
    private final JsonDefinition jsonDefinition;
    private final JsonMapper mapper;
    private final HeaderFieldDataExtractor headerFieldDataExtractor = new HeaderFieldDataExtractor();

    public JsonDeserializer(JsonDefinition jsonDefinition)
    {
        this.jsonDefinition = jsonDefinition;
        this.mapper = MapperBuilder.json().build();

    }

    @Override
    public <T> List<T> deserializeObjects(Class<T> clazz, String json) throws IOException
    {
        if (this.jsonDefinition.isTree())
        {
            throw new NotImplementedException("deserialize json tree not implemented");
        }
        else {
            return convertFlatListOfMapsToObjects(clazz, json);
        }
    }


    private <T> List<T> convertFlatListOfMapsToObjects(Class<T> clazz, String json) throws JsonProcessingException
    {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, clazz);
        return mapper.readValue(json, javaType);
    }

}
