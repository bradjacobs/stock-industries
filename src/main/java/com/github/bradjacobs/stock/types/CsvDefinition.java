package com.github.bradjacobs.stock.types;

import org.apache.commons.lang3.StringUtils;

public class CsvDefinition implements DataDefinition
{
    private final boolean includeDescription;
    private final boolean sparsely;

    private static final String EXTENSION = "csv";
    private static final String DOT_EXTENSION = "." + EXTENSION;

    private CsvDefinition(boolean includeDescription, boolean sparsely)
    {
        this.includeDescription = includeDescription;
        this.sparsely = sparsely;
    }

    public boolean isIncludeDescription()
    {
        return includeDescription;
    }

    public boolean isSparsely()
    {
        return sparsely;
    }

    @Override
    public String getExtension()
    {
        return EXTENSION;
    }

    @Override
    public String generateFileSuffix() {
        StringBuilder sb = new StringBuilder();
        if (sparsely) {
            sb.append("_sparse");
        }
        if (this.includeDescription) {
            sb.append("_w_desc");
        }
        sb.append(DOT_EXTENSION);
        return sb.toString();
    }


    public static CsvDefinition generateInstance(String fileName)
    {
        // todo - come back to simplify
        if (StringUtils.isEmpty(fileName)) {
            throw new IllegalArgumentException("Must supply a fileName");
        }
        if (fileName.contains(DOT_EXTENSION)) {
            throw new IllegalArgumentException("Not a recognized CSV file extension: " + fileName);
        }

        // todo - fix redundant strings
        Builder builder = new Builder();
        if (fileName.contains("_sparse")) {
            builder.makeSparsely(true);
        }
        if (fileName.contains("_w_desc")) {
            builder.withLongDescriptions(true);
        }
        return builder.build();
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder
    {
        private Builder() { }

        private boolean includeLongDescriptions = false;
        private boolean makeSparsely = false;

        public Builder withLongDescriptions(boolean includeLongDescriptions) {
            this.includeLongDescriptions = includeLongDescriptions;
            return this;
        }
        public Builder makeSparsely(boolean makeSparsely) {
            this.makeSparsely = makeSparsely;
            return this;
        }

        public CsvDefinition build() {
            return new CsvDefinition(includeLongDescriptions, makeSparsely);
        }
    }
}
