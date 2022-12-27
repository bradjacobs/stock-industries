package com.github.bradjacobs.stock.types;

import com.github.bradjacobs.stock.classifications.Classification;

public interface DataDefinition
{
    String generateFileName(Classification classification);
}
