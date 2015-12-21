package com.mypdf;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFMergerUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Gennady Zemlyakov
 * gzemlyakov@gmail.com
 */
public class PdfUtils {

    public static final String CHECK_LOG_FILE
            = "Following errors occurred during operation execution. Please, check log file %s";
    public static final String EMPTY_INPUT = "Operation failed. Please, provide non-empty arrays of full file names.";
    public static final String INPUT_OUTPUT_SIZE_ERROR
            = "Operation failed. Array of input and output files must be equal size.";

    public static final int LANDSCAPE_DEGREE = 90;

    private static final Logger LOG = Logger.getLogger(PdfUtils.class.getName());

    private List<String> rotatedDocuments;

    private List<String> errors;
    private File logFile;

    public PdfUtils() {
        initLogger();
    }

    private void initLogger() {
        try {
            logFile = File.createTempFile("basex-pdf-utils", ".log");
            FileHandler fileHandler = new FileHandler(logFile.getAbsolutePath());
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            LOG.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns array of successfully rotated pdf documents names.
     * @param inputFiles Array of full documents names to rotate
     * @param outputFiles Array of full documents names which they will have after rotation
     * @return Array of successfully rotated documents or array of errors if operation failed.
     */
    public String[] rotateLandscape(String[] inputFiles, String[] outputFiles) {
        rotatedDocuments = new ArrayList<>();
        errors = new ArrayList<>();
        if (!checkInput(inputFiles, outputFiles)) return getErrorArray();
        for (int i = 0; i < inputFiles.length; i++) {
            rotateLandscapeDoc(inputFiles[i], outputFiles[i]);
        }
        return rotatedDocuments.toArray(new String[rotatedDocuments.size()]);
    }

    /**
     * Returns name of merged document.
     * @param filesToMerge Array of full documents names to merge
     * @param outputFile Name of merged document
     * @return Name of merged document or error message
     */
    public String merge(String[] filesToMerge, String outputFile) {
        errors = new ArrayList<>();
        if (isInputEmpty(filesToMerge) || StringUtils.isEmpty(outputFile)) return EMPTY_INPUT;
        try {
            PDFMergerUtility mergerUtility = new PDFMergerUtility();
            for (String fileToMerge : filesToMerge) {
                mergerUtility.addSource(fileToMerge);
            }
            mergerUtility.setDestinationFileName(outputFile);
            mergerUtility.mergeDocuments();
            return outputFile;
        } catch (COSVisitorException | IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    @SuppressWarnings("unchecked")
    private void rotateLandscapeDoc(String inputFile, String outputFile) {
        try {
            PDDocument document = PDDocument.load(inputFile);
            PDDocument rotatedDocument = new PDDocument();
            List<PDPage> pages = document.getDocumentCatalog().getAllPages();
            for (PDPage page : pages) {
                page.setRotation(LANDSCAPE_DEGREE);
                rotatedDocument.addPage(page);
            }
            rotatedDocument.save(outputFile);
            document.close();
            rotatedDocument.close();
            registerRotatedDoc(outputFile);
        } catch (IOException | COSVisitorException e) {
            e.printStackTrace();
            registerError(e.getMessage());
        }
    }

    private boolean checkInput(String[] inputFiles, String[] outputFiles) {
        if (isInputEmpty(inputFiles) || isInputEmpty(outputFiles)) return false;
        if (inputFiles.length != outputFiles.length) {
            registerError(INPUT_OUTPUT_SIZE_ERROR);
            return false;
        }
        return true;
    }

    private boolean isInputEmpty(String[] input) {
        if (ArrayUtils.isEmpty(input)) {
            registerError(EMPTY_INPUT);
            return true;
        }
        return false;
    }

    private void registerRotatedDoc(String fileName) {
        rotatedDocuments.add(fileName);
    }

    private void registerError(String errorMessage) {
        errors.add(errorMessage);
    }

    private String[] getErrorArray() {
        if (logFile != null) errors.add(0, String.format(CHECK_LOG_FILE, logFile.getAbsolutePath()));
        return errors.toArray(new String[errors.size()]);
    }

}
