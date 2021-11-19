package com.github.bradjacobs.stock.util;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlUtil
{
    private UrlUtil() {}

    public static URL createURL(String path) {
        try {
            return new URL(path);
        } catch(MalformedURLException e){
            throw new RuntimeException(e);
        }
    }
}
