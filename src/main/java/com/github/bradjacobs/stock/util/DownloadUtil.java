package com.github.bradjacobs.stock.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Simple helper Util for downloading files.
 */
public class DownloadUtil
{
    private static final int CONNECTION_TIMEOUT = 20000;
    private static final int READ_TIMEOUT = 30000;

    private DownloadUtil() { }

    public static String downloadFile(String urlPath) throws IOException
    {
        try (InputStream in = createInputStream(urlPath))
        {
            return IOUtils.toString(in, StandardCharsets.UTF_8.name());
        }
    }

    public static String[] downloadPdfFile(String urlPath) throws IOException
    {
        if (urlPath != null && !urlPath.endsWith(".pdf")) {
            throw new IllegalArgumentException("Must provide a file location ending with '.pdf'");
        }

        try (InputStream in = createInputStream(urlPath))
        {
            return PdfUtil.getPdfFileLines(in);
        }
    }


    // **** NOTE *****:
    //    would be 'better' to use an htmlClient, but will only worry about that iff necessary.
    private static InputStream createInputStream(String urlPath) throws IOException
    {
        if (StringUtils.isEmpty(urlPath)) {
            throw new IllegalArgumentException("Must provide a urlPath");
        }
        URL url = new URL(urlPath);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        // HttpURLConnection won't always set a User-Agent, and there are some websites
        //  that will automatically return a 403 if User-Agent is not present.
        connection.setRequestProperty("User-Agent", "Java_HttpURLConnection/" + System.getProperty("java.version"));
        connection.setRequestProperty("Accept", "*/*");

        return new BufferedInputStream(connection.getInputStream());
    }



}
