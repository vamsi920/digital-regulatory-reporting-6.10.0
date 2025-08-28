package com.regnosys.drr.report;

import com.google.inject.Injector;
import com.regnosys.testing.reports.ReportTypeSummariser;
import com.rosetta.util.DottedPath;
import org.apache.commons.cli.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Checkout 5.x.x branch, run maven
 * Generate all reports with command (edit the path param):
 * Run DrrReportTypeSummariser with command line options: -c . -g -s 5xx -p /var/folders/25/y2kkt3yd1xz46w99qpw83zq00000gp/T/report-type-summariser15174037038493890859/
 * Checkout master branch, run maven
 * Generate all reports with command:
 * Run DrrReportTypeSummariser with command line options: -c . -g -s master -p /var/folders/25/y2kkt3yd1xz46w99qpw83zq00000gp/T/report-type-summariser15174037038493890859/
 * Merge reports with command:
 * java com.regnosys.drr.report.DrrReportTypeSummariser -m -s1 5xx -s2 master -p /var/folders/25/y2kkt3yd1xz46w99qpw83zq00000gp/T/report-type-summariser15174037038493890859/
 */
public class DrrReportTypeSummariser {

    private static final Set<String> EXCLUDED_REPORTS_FILTER = Set.of("Margin", "Valuation", "MAS_2013");

    public static void main(String[] args) throws IOException, ParseException {
        CommandLine line = readCommandLine(
                args,
                Option.builder("c").longOpt("classpathDir").desc("Classpath directory").build(),
                Option.builder("n").longOpt("namespace").desc("Namespace").build(),
                Option.builder("g").longOpt("generate").desc("Generate report type summary for branch").build(),
                Option.builder("s").longOpt("suffix").desc("File suffix for report type summary file").hasArg().build(),
                Option.builder("p").longOpt("path").desc("Optional generate path. Required when merging.").hasArg().build(),
                Option.builder("m").longOpt("merge").desc("Merge generated report type summaries for branches").build(),
                Option.builder("s1").longOpt("suffix1").desc("File suffix for report type summary file 1").hasArg().build(),
                Option.builder("s2").longOpt("suffix2").desc("File suffix for report type summary file 2").hasArg().build()
        );
        Injector injector = new ReportTestRuntimeModule.InjectorProvider().getInjector();
        ReportTypeSummariser reportTypeSummariser = injector.getInstance(ReportTypeSummariser.class);

        String classpathDir = line.getOptionValue("c", "."); // "drr/rosetta"
        DottedPath namespace = DottedPath.of(line.getOptionValue("n", "drr"));

        if (line.hasOption("g") && line.hasOption("s")) {
            String fileSuffix = line.getOptionValue("s");
            Path path = getPath(line);
            reportTypeSummariser
                    .createTypeSummaryForReports(classpathDir, namespace, EXCLUDED_REPORTS_FILTER, fileSuffix, path);
        } else if (line.hasOption("m") && line.hasOption("s1") && line.hasOption("s2") && line.hasOption("p")) {
            String fileSuffix1 = line.getOptionValue("s1");
            String fileSuffix2 = line.getOptionValue("s2");
            Path path = getPath(line);
            reportTypeSummariser
                    .mergeTypeSummaryForReports(classpathDir, namespace, EXCLUDED_REPORTS_FILTER, fileSuffix1, fileSuffix2, path);
        } else {
            throw new IllegalArgumentException("Either option g (generate) + s (suffix) or m (merge) + s1 (suffix1) + s2 (suffix2) + p (path) must be specified");
        }
    }

    @NotNull
    private static Path getPath(CommandLine line) throws IOException {
        return Path.of(line.hasOption("p") ? 
                line.getOptionValue("p") : 
                Files.createTempDirectory("report-type-summariser").toFile().getAbsolutePath());
    }

    public static CommandLine readCommandLine(String[] args, Option... optionList) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        for (Option option : optionList) {
            options.addOption(option);
        }
        return parser.parse(options, args);
    }
}