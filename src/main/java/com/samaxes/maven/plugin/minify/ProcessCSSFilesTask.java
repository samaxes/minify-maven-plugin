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

import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * Task for merging and compressing CSS files.
 */
public class ProcessCSSFilesTask extends ProcessFilesTask {

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
     */
    public ProcessCSSFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String filesDir, List<String> filenames, String finalFilename, int linebreak) {
        super(log, bufferSize, webappSourceDir, webappTargetDir, filesDir, filenames, finalFilename, linebreak);
    }

    /**
     * Minifies CSS file.
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
                CssCompressor compressor = new CssCompressor(reader);

                compressor.compress(writer, linebreak);
                reader.close();
                writer.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
