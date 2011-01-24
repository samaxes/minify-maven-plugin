/**
 * 
 */
package com.samaxes.maven.plugin.minify;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

import com.samaxes.maven.plugin.common.JavaScriptErrorReporter;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Task for merging and compressing JavaScript files.
 */
public class ProcessJSFilesTask extends ProcessFilesTask {

    private boolean munge;

    private boolean verbose;

    private boolean preserveAllSemiColons;

    private boolean disableOptimizations;

    /**
     * Task constructor.
     * 
     * @param log Maven plugin log
     * @param bufferSize size of the buffer used to read source files.
     * @param webappSourceDir web resources source directory
     * @param webappTargetDir web resources target directory
     * @param inputDir directory containing source files
     * @param sourceFiles list of source files to include
     * @param sourceIncludes list of source files to include
     * @param sourceExcludes list of source files to exclude
     * @param outputDir directory to write the final file
     * @param finalFile final filename
     * @param linebreak split long lines after a specific column
     * @param munge minify only
     * @param verbose display informational messages and warnings
     * @param preserveAllSemiColons preserve unnecessary semicolons
     * @param disableOptimizations disable all the built-in micro optimizations
     */
    public ProcessJSFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String inputDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
            String outputDir, String finalFile, int linebreak, boolean munge, boolean verbose,
            boolean preserveAllSemiColons, boolean disableOptimizations) {
        super(log, bufferSize, webappSourceDir, webappTargetDir, inputDir, sourceFiles, sourceIncludes, sourceExcludes,
                outputDir, finalFile, linebreak);

        this.munge = munge;
        this.verbose = verbose;
        this.preserveAllSemiColons = preserveAllSemiColons;
        this.disableOptimizations = disableOptimizations;
    }

    /**
     * Minifies JavaScript file.
     */
    protected void minify() {
        if (finalFile != null) {
            String extension = FileUtils.getExtension(finalFile.getName());
            File destFile = new File(targetDir, finalFile.getName().replace(extension, SUFFIX.concat(extension)));

            try {
                log.info("Minifying final file [" + destFile.getName() + "]");
                Reader reader = new FileReader(finalFile);
                Writer writer = new FileWriter(destFile);
                JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new JavaScriptErrorReporter(log,
                        finalFile.getName()));

                compressor.compress(writer, linebreak, munge, verbose, preserveAllSemiColons, disableOptimizations);
                reader.close();
                writer.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
