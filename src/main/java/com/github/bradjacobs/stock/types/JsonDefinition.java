package com.github.bradjacobs.stock.types;

import java.awt.*;

public class JsonDefinition implements DataDefinition
{
    private final boolean includeDescription;
    private final boolean isTree;
    private final JsonKeyName jsonKeyName;

    public JsonDefinition(boolean includeDescription, boolean isTree, JsonKeyName jsonKeyName)
    {
        this.includeDescription = includeDescription;
        this.isTree = isTree;
        this.jsonKeyName = jsonKeyName;
    }

    public boolean isIncludeDescription()
    {
        return includeDescription;
    }

    public boolean isTree()
    {
        return isTree;
    }

    public JsonKeyName getJsonKeyName()
    {
        return jsonKeyName;
    }

    @Override
    public String generateFileSuffix()
    {
        StringBuilder sb = new StringBuilder();

        if (this.includeDescription) {
            sb.append("_w_desc");
        }
        if (isTree) {
            sb.append("_tree");
        }
        if (this.jsonKeyName.equals(JsonKeyName.CANONICAL)) {
            sb.append("_canonical");
        }
        else if (this.jsonKeyName.equals(JsonKeyName.BASIC)) {
            sb.append("_basic");
        }
        sb.append(".json");
        return sb.toString();
    }

    public enum JsonKeyName {
        NORMAL,
        CANONICAL,
        BASIC
    }

    public static JsonBuilder builder() {
        return new JsonBuilder();
    }


    public static class JsonBuilder
    {
        private JsonBuilder() { }

        public JsonTreeBuilder asTree() {
            return new JsonTreeBuilder();
        }
        public JsonFlatBuilder asPojoList() {
            return new JsonFlatBuilder();
        }
    }

    public static class JsonTreeBuilder
    {
        private JsonKeyName jsonKeyName = JsonKeyName.NORMAL;

        public JsonTreeBuilder withCanonicalKeyNames() {
            this.jsonKeyName = JsonKeyName.CANONICAL;
            return this;
        }
        public JsonTreeBuilder withNormalKeyNames() {
            this.jsonKeyName = JsonKeyName.NORMAL;
            return this;
        }
        public JsonTreeBuilder withGenericKeyNames() {
            this.jsonKeyName = JsonKeyName.BASIC;
            return this;
        }

        public JsonDefinition build() {
            return new JsonDefinition( false,  true, jsonKeyName);
        }
    }

    public static class JsonFlatBuilder
    {
        private boolean includeLongDescriptions = false;

        public JsonFlatBuilder withLongDescriptions(boolean includeLongDescriptions) {
            this.includeLongDescriptions = includeLongDescriptions;
            return this;
        }

        public JsonDefinition build() {
            return new JsonDefinition(includeLongDescriptions, false, JsonKeyName.NORMAL);
        }
    }

}
