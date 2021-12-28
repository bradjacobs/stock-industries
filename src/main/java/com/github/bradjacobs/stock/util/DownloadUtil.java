package com.github.bradjacobs.stock.util;

import org.apache.commons.io.IOUtils;

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

    public static String downloadFile(URL url) throws IOException
    {
        try (InputStream in = createInputStream(url))
        {
            return IOUtils.toString(in, StandardCharsets.UTF_8.name());
        }
    }

    public static String[] downloadPdfFile(URL url) throws IOException
    {
        try (InputStream in = createInputStream(url))
        {
            return PdfUtil.getPdfFileLines(in);
        }
    }

    // **** NOTE *****:
    //    would be 'better' to use an htmlClient, but will only worry about that iff necessary.
    public static InputStream createInputStream(URL url) throws IOException
    {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);

        // HttpURLConnection won't always set a User-Agent, and there are some websites
        //  that will automatically return a 403 if User-Agent is not present.
        connection.addRequestProperty("User-Agent","jclient/" + System.getProperty("java.version"));
        connection.setRequestProperty("Accept", "*/*");

        return new BufferedInputStream(connection.getInputStream());
    }

}
