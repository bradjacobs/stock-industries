package com.github.bradjacobs.stock.util;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

public class PdfUtil
{
    public static String[] getPdfFileLines(InputStream inputStream) throws IOException
    {
        String[] lines = null;
        try (PDDocument document = PDDocument.load(inputStream)) {

            if (!document.isEncrypted()) {

                //PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                //stripper.setSortByPosition(false);

                PDFTextStripper tStripper = new PDFTextStripper();

                String pdfFileInText = tStripper.getText(document);

                // split by whitespace
                lines = pdfFileInText.split("\\r?\\n");
            }
        }

        return lines;
    }

}
