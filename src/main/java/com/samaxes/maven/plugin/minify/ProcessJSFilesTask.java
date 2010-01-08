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
     * @param filesDir directory containing input files
     * @param filenames filenames list
     * @param finalFilename final filename
     * @param linebreak split long lines after a specific column
     * @param munge minify only
     * @param verbose display informational messages and warnings
     * @param preserveAllSemiColons preserve unnecessary semicolons
     * @param disableOptimizations disable all the built-in micro optimizations
     */
    public ProcessJSFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String filesDir, List<String> filenames, String finalFilename, int linebreak, boolean munge,
            boolean verbose, boolean preserveAllSemiColons, boolean disableOptimizations) {
        super(log, bufferSize, webappSourceDir, webappTargetDir, filesDir, filenames, finalFilename, linebreak);

        this.munge = munge;
        this.verbose = verbose;
        this.preserveAllSemiColons = preserveAllSemiColons;
        this.disableOptimizations = disableOptimizations;
    }

    /**
     * Minifies JavaScript file.
     */
    protected void minify() {
        if (finalFile.exists()) {
            String name = finalFile.getName();
            String extension = name.substring(name.lastIndexOf('.'));
            File destFile = new File(targetDir, name.replace(extension, suffix.concat(extension)));

            log.info("Minifying file " + name);
            try {
                Reader reader = new FileReader(finalFile);
                Writer writer = new FileWriter(destFile);
                JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new JavaScriptErrorReporter(log,
                        name));

                compressor.compress(writer, linebreak, munge, verbose, preserveAllSemiColons, disableOptimizations);
                reader.close();
                writer.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
