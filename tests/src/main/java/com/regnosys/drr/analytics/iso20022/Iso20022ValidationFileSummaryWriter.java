package com.regnosys.drr.analytics.iso20022;

import com.regnosys.drr.analytics.AttributeValidationData;
import com.regnosys.drr.analytics.ConditionValidationData;
import com.regnosys.drr.analytics.TypeValidationData;
import com.regnosys.drr.analytics.ValidationData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.regnosys.drr.analytics.iso20022.Iso20022ValidationSummaryWriter.*;

class Iso20022ValidationFileSummaryWriter {

    private static final String[] VALIDATION_PER_FILE_HEADER = new String[]{"#", "Projection", "Test Pack", "Sample", "DRR: Error", "DRR: CARDINALITY-Name", "DRR: CARDINALITY-Count", "DRR: TYPE_FORMAT-Name", "DRR: TYPE_FORMAT-Count", "DRR: CONDITION-Name", "DRR: CONDITION-Count", "DRR: CONDITION-All-Valid", "", "ISO: Error", "ISO: CARDINALITY-Name", "ISO: CARDINALITY-Count", "ISO: TYPE_FORMAT-Name", "ISO: TYPE_FORMAT-Count", "", "ISO: Schema Failures"};
    private static final Logger LOGGER = LoggerFactory.getLogger(Iso20022ValidationFileSummaryWriter.class);
    
    private final Path analyticsFilePath;
    
    public Iso20022ValidationFileSummaryWriter(Path analyticsPath) {
        this.analyticsFilePath = analyticsPath.resolve("validation-file-summary.csv");
    }


    public void writeCsv(List<ValidationData> validationDataList) throws IOException {
        if (TEST_WRITE_BASE_PATH.isEmpty()) {
            LOGGER.info("TEST_WRITE_BASE_PATH not set");
            return;
        }
        writeFileSummary(validationDataList);
    }

    private void writeFileSummary(List<ValidationData> validationDataList) throws IOException {
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(VALIDATION_PER_FILE_HEADER)
                .setRecordSeparator(NEW_LINE)
                .setTrim(false)
                .build();
        StringBuilder csvContents = new StringBuilder();

        try (CSVPrinter csvBuilder = new CSVPrinter(csvContents, csvFormat)) {
            AtomicInteger count = new AtomicInteger();
            Collections.sort(validationDataList);
            for (ValidationData validationData : validationDataList) {
                printFileSummaryRow(csvBuilder, count, validationData);
            }
        }

        Path analyticsPath = TEST_WRITE_BASE_PATH.get().resolve(analyticsFilePath);
        writeFile(analyticsPath, csvContents.toString(), true);
    }

    private static void printFileSummaryRow(CSVPrinter csvBuilder, AtomicInteger count, ValidationData validationData) throws IOException {
        csvBuilder.print(count.getAndIncrement());
        csvBuilder.print(validationData.getFunctionName());
        csvBuilder.print(validationData.getTestPack());
        csvBuilder.print(validationData.getSampleName());

        TypeValidationData transactionReportData = validationData.getInputData();
        print(csvBuilder, transactionReportData.getExceptionData());
        print(csvBuilder, transactionReportData.getCardinalityValidationData());
        print(csvBuilder, transactionReportData.getTypeFormatValidationData());

        ConditionValidationData transactionCondition = transactionReportData.getConditionValidationData();
        print(csvBuilder, transactionCondition);

        csvBuilder.print("");

        TypeValidationData isoReportData = validationData.getOutputData();
        print(csvBuilder, isoReportData.getExceptionData());
        print(csvBuilder, isoReportData.getCardinalityValidationData());
        print(csvBuilder, isoReportData.getTypeFormatValidationData());

        csvBuilder.print("");

        csvBuilder.print(validationData.getXsdSchemaValidationErrors());

        csvBuilder.printRecord();
    }

    private static void print(CSVPrinter csvBuilder, Exception exceptionData) throws IOException {
        csvBuilder.print(exceptionData);
    }

    private static void print(CSVPrinter csvBuilder, ConditionValidationData data) throws IOException {
        int failureCount = data.getFailureCount();
        csvBuilder.print(sortAndJoin(data.getFailedConditions()));
        csvBuilder.print(failureCount);
        int allValid = failureCount == 0 ? 1:0;
        csvBuilder.print(allValid);
    }

    private static void print(CSVPrinter csvBuilder, AttributeValidationData data) throws IOException {
        int failureCount = data.getFailureCount();
        csvBuilder.print(sortAndJoin(data.getFailedAttributeNames()));
        csvBuilder.print(failureCount);
    }

    private static String sortAndJoin(List<String> strings) {
        Collections.sort(strings);
        return String.join(DELIMITER, strings);
    }
}

