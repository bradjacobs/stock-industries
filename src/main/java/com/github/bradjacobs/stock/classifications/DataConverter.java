package com.github.bradjacobs.stock.classifications;

import java.io.IOException;
import java.util.List;

// TODO - need to rename this interface (and related classes)
public interface DataConverter<T>
{
    // create data records
    List<T> createDataRecords() throws IOException;

}
