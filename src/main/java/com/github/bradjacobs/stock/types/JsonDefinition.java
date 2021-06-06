package com.github.bradjacobs.stock.types;

import org.apache.commons.lang3.StringUtils;

// todo - add unit tests for this.
public class JsonDefinition implements DataDefinition
{
    private final boolean includeDescription;
    private final boolean isTree;
    private final JsonKeyName jsonKeyName;

    private static final String EXTENSION = "json";
    private static final String DOT_EXTENSION = "." + EXTENSION;


    //   todo - come back and reorginize a little better.
    // substrings included as part of the fileName specifc format
    private static final String LONG_DESC_FILE_NAME_ID = "_w_desc";  // includes long descriptions (if available, n/a for tree)
    private static final String TREE_FILE_NAME_ID = "_tree";  // indicates hierarchical tree
    private static final String CANONICAL_FILE_NAME_ID = "_canonical";  // indicates canonical form (i.e. 'common header names regardless of customer)
    private static final String BASIC_FILE_NAME_ID = "_basic";  // indicates basic tree form (i.e. simple generic nodes)



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
            sb.append(LONG_DESC_FILE_NAME_ID);
        }
        if (isTree) {
            sb.append(TREE_FILE_NAME_ID);
        }
        if (this.jsonKeyName.equals(JsonKeyName.CANONICAL)) {
            sb.append(CANONICAL_FILE_NAME_ID);
        }
        else if (this.jsonKeyName.equals(JsonKeyName.BASIC)) {
            sb.append(BASIC_FILE_NAME_ID);
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
        if (fileName.contains(TREE_FILE_NAME_ID)) {
            JsonTreeBuilder treeBuilder = builder.asTree();
            if (fileName.contains(CANONICAL_FILE_NAME_ID)) {
                treeBuilder.withCanonicalKeyNames();
            }
            else if (fileName.contains(BASIC_FILE_NAME_ID)) {
                treeBuilder.withGenericKeyNames();
            }
            else {
                treeBuilder.withNormalKeyNames();
            }
            return treeBuilder.build();
        }
        else {
            JsonFlatBuilder pojoListBuilder = builder.asPojoList();
            if (fileName.contains(LONG_DESC_FILE_NAME_ID)) {
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
