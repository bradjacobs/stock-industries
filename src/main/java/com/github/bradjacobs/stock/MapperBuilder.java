package com.github.bradjacobs.stock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;

public class MapperBuilder
{
    private MapperBuilder() {}

    public static JsonMapperBuilder json() {
        return new JsonMapperBuilder();
    }

    public static CsvMapperBuilder csv() {
        return new CsvMapperBuilder();
    }


    public static class JsonMapperBuilder {
        private boolean indentOutput = true;   // default to pretty mode
        private boolean suppressNullArrays = true;
        private boolean includeLongDescription = true;
        private Class clazz = null;

        public JsonMapperBuilder setIncludeLongDescription(boolean includeLongDescription) {
            this.includeLongDescription = includeLongDescription;
            return this;
        }

        public JsonMapperBuilder setClazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public JsonMapperBuilder setIndentOutput(boolean indentOutput) {
            this.indentOutput = indentOutput;
            return this;
        }

        public JsonMapperBuilder setSuppressNullArrays(boolean suppressNullArrays) {
            this.suppressNullArrays = suppressNullArrays;
            return this;
        }

        public JsonMapper build() {

            JsonMapper mapper = new JsonMapper();
            if (indentOutput) {
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
            }
            if (suppressNullArrays) {
                // avoid marshalling out a null array.
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }

            if (! includeLongDescription) {
                // if don't want long description, than add a MixIn to suppress it
                if (this.clazz == null) {
                    throw new IllegalArgumentException("The Clazz must be set when includeLongDescription = false");
                }
                mapper.addMixIn(clazz, NoDescriptionMixin.class);
            }

            return mapper;
        }
    }


    public static class CsvMapperBuilder {
        private boolean arrayWrap = false;
        private boolean includeLongDescription = true;
        private Class clazz = null;

        public CsvMapperBuilder setArrayWrap(boolean arrayWrap) {
            this.arrayWrap = arrayWrap;
            return this;
        }

        public CsvMapperBuilder setIncludeLongDescription(boolean includeLongDescription) {
            this.includeLongDescription = includeLongDescription;
            return this;
        }

        public CsvMapperBuilder setClazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public CsvMapper build()
        {
            // all of this stuff is always included for now
            CsvMapper.Builder builder = CsvMapper.builder()
                .enable(CsvParser.Feature.SKIP_EMPTY_LINES)
                .enable(CsvParser.Feature.TRIM_SPACES)
                .enable(CsvParser.Feature.FAIL_ON_MISSING_COLUMNS)
                .enable(MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY); // ALWAYS disable this (or it can change the column order)

            if (arrayWrap) {
                builder = builder.enable(CsvParser.Feature.WRAP_AS_ARRAY);
            }

            if (! includeLongDescription) {
                // if don't want long description, than add a MixIn to suppress it
                if (this.clazz == null) {
                    throw new IllegalArgumentException("The Clazz must be set when includeLongDescription = false");
                }
                builder = builder.addMixIn(clazz, NoDescriptionMixin.class);
            }

            return builder.build();
        }
    }

    // Mixin used to suppress serialization of 'full descriptions'
    protected static abstract class NoDescriptionMixin {
        @JsonIgnore
        abstract public String getDescription();
        @JsonIgnore abstract public String getDefinition();
    }

}
