package com.github.bradjacobs.stock.types;

public class CsvDefinition implements DataDefinition
{
    private final boolean includeDescription;
    private final boolean sparsely;

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
    public String generateFileSuffix() {
        StringBuilder sb = new StringBuilder();
        if (sparsely) {
            sb.append("_sparse");
        }
        if (this.includeDescription) {
            sb.append("_w_desc");
        }
        sb.append(".csv");
        return sb.toString();
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
