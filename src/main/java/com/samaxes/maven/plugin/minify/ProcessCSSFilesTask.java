/*
 * $Id$
 *
 * Minify Maven Plugin
 * https://github.com/samaxes/minify-maven-plugin
 *
 * Copyright (c) 2009 samaxes.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samaxes.maven.plugin.minify;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.IOUtil;

import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * Task for merging and compressing CSS files.
 */
public class ProcessCSSFilesTask extends ProcessFilesTask {

    private boolean debug;

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
     * @param finalFilename final filename
     * @param suffix final filename suffix
     * @param charset if a character set is specified, a byte-to-char variant allows the encoding to be selected.
     *        Otherwise, only byte-to-byte operations are used
     * @param linebreak split long lines after a specific column
     * @param debug show source file paths in log output
     */
    public ProcessCSSFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String inputDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
            String outputDir, String finalFilename, String suffix, String charset, int linebreak, boolean debug) {
        super(log, bufferSize, webappSourceDir, webappTargetDir, inputDir, sourceFiles, sourceIncludes, sourceExcludes,
                outputDir, finalFilename, suffix, charset, linebreak, debug);
        this.debug = debug;
    }

    /**
     * Minifies CSS file.
     */
    @Override
    protected void minify() {
        if (minifiedFile != null) {
            try {
                log.info("Creating minified file [" + ((debug) ? minifiedFile.getPath() : minifiedFile.getName())
                        + "].");

                InputStream in = new FileInputStream(mergedFile);
                OutputStream out = new FileOutputStream(minifiedFile);
                InputStreamReader reader;
                OutputStreamWriter writer;
                if (charset == null) {
                    reader = new InputStreamReader(in);
                    writer = new OutputStreamWriter(out);
                } else {
                    reader = new InputStreamReader(in, charset);
                    writer = new OutputStreamWriter(out, charset);
                }

                CssCompressor compressor = new CssCompressor(reader);
                compressor.compress(writer, linebreak);

                IOUtil.close(reader);
                IOUtil.close(writer);
                IOUtil.close(in);
                IOUtil.close(out);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
