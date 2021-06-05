package com.github.bradjacobs.stock.types;

import org.apache.commons.lang3.StringUtils;


public class JsonDefinition implements DataDefinition
{
    private final boolean includeDescription;
    private final boolean isTree;
    private final JsonKeyName jsonKeyName;

    private static final String EXTENSION = "json";
    private static final String DOT_EXTENSION = "." + EXTENSION;


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
    public String getExtension()
    {
        return EXTENSION;
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
        sb.append(DOT_EXTENSION);
        return sb.toString();
    }

    public static JsonDefinition generateInstance(String fileName)
    {
        // todo - come back to simplify
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("Must supply a fileName");
        }
        if (fileName.contains(DOT_EXTENSION)) {
            throw new IllegalArgumentException("Not a recognized JSON file extension: " + fileName);
        }

        // todo - fix redundant strings
        Builder builder = new Builder();
        if (fileName.contains("_tree")) {
            JsonTreeBuilder treeBuilder = builder.asTree();
            if (fileName.contains("_canonical")) {
                treeBuilder.withCanonicalKeyNames();
            }
            else if (fileName.contains("_basic")) {
                treeBuilder.withGenericKeyNames();
            }
            else {
                treeBuilder.withNormalKeyNames();
            }
            return treeBuilder.build();
        }
        else {
            JsonFlatBuilder pojoListBuilder = builder.asPojoList();
            if (fileName.contains("_w_desc")) {
                pojoListBuilder.withLongDescriptions(true);
            }
            return pojoListBuilder.build();
        }
    }


    public enum JsonKeyName {
        NORMAL,
        CANONICAL,
        BASIC
    }

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder
    {
        private Builder() { }

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

        // note: the tree form will never have the long description (presently)
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
