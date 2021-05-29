package bwj.stock.classifications;

enum DataFileType
{
    FULL_CSV("_full.csv"),
    SPARSE_CSV("_sparse.csv"),
    TREE_JSON("_tree.json"),
    CANONICAL_TREE_JSON("_canonical_tree.json");


    private final String suffix;

    DataFileType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
