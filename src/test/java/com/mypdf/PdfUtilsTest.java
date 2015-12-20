package com.mypdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Gennady Zemlyakov
 * gzemlyakov@gmail.com
 */
public class PdfUtilsTest {

    private String[] inputFiles;
    private String[] outputFiles;
    private String mergeOutputFileName;

    @Before
    public void init() throws URISyntaxException {
        File resourcesDir = new File(this.getClass().getResource("/").toURI());
        File samplePdf1 = new File("src/test/java/com/mypdf/sample-1.pdf");
        File samplePdf2 = new File("src/test/java/com/mypdf/sample-2.pdf");
        inputFiles = new String[]{samplePdf1.getAbsolutePath(), samplePdf2.getAbsolutePath()};
        outputFiles = new String[]{resourcesDir.getAbsolutePath() + File.separator + "sample-1-rotated.pdf",
                resourcesDir.getAbsolutePath() + File.separator + "sample-2-rotated.pdf"};
        mergeOutputFileName = resourcesDir.getAbsolutePath() + File.separator + "sample-merged.pdf";
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rotateLandscapeTest() throws IOException, URISyntaxException {
        PdfUtils pdfUtils;
        String[] result;

        pdfUtils = new PdfUtils();
        result = pdfUtils.rotateLandscape(new String[]{}, outputFiles);
        assertTrue(result.length == 2);
        assertTrue(PdfUtils.EMPTY_INPUT.equals(result[1]));

        pdfUtils = new PdfUtils();
        result = pdfUtils.rotateLandscape(inputFiles, null);
        assertTrue(result.length == 2);
        assertTrue(PdfUtils.EMPTY_INPUT.equals(result[1]));

        pdfUtils = new PdfUtils();
        result = pdfUtils.rotateLandscape(Arrays.copyOfRange(inputFiles, 0, 1), outputFiles);
        assertTrue(result.length == 2);
        assertTrue(PdfUtils.INPUT_OUTPUT_SIZE_ERROR.equals(result[1]));

        pdfUtils = new PdfUtils();
        result = pdfUtils.rotateLandscape(inputFiles, Arrays.copyOfRange(inputFiles, 1, 2));
        assertTrue(result.length == 2);
        assertTrue(PdfUtils.INPUT_OUTPUT_SIZE_ERROR.equals(result[1]));

        pdfUtils = new PdfUtils();
        result = pdfUtils.rotateLandscape(inputFiles, outputFiles);
        assertArrayEquals(outputFiles, result);

        for (String fileName : result) {
            PDDocument document = PDDocument.load(fileName);
            List<PDPage> pages = document.getDocumentCatalog().getAllPages();
            for (PDPage page : pages) {
                assertEquals(page.getRotation().intValue(), PdfUtils.LANDSCAPE_DEGREE);
            }
            document.close();
        }
    }

    @Test
    public void mergeTest() throws IOException {
        PdfUtils pdfUtils;
        String result;

        pdfUtils = new PdfUtils();
        result = pdfUtils.merge(null, mergeOutputFileName);
        assertEquals(PdfUtils.EMPTY_INPUT, result);

        pdfUtils = new PdfUtils();
        result = pdfUtils.merge(inputFiles, "");
        assertEquals(PdfUtils.EMPTY_INPUT, result);

        pdfUtils = new PdfUtils();
        result = pdfUtils.merge(inputFiles, mergeOutputFileName);
        assertEquals(mergeOutputFileName, result);

        long pageCount = 0;
        for (String inputFile : inputFiles) {
            PDDocument document = PDDocument.load(inputFile);
            pageCount += document.getNumberOfPages();
            document.close();
        }
        PDDocument mergedDocument = PDDocument.load(mergeOutputFileName);
        assertEquals(pageCount, mergedDocument.getNumberOfPages());
    }

    @After
    public void cleanUp() {
        for (String fileName : outputFiles) {
            new File(fileName).delete();
        }
        new File(mergeOutputFileName).delete();
    }

}
