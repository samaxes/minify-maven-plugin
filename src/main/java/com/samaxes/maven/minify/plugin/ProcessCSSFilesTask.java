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
package com.samaxes.maven.minify.plugin;

import java.io.File;
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

    /**
     * Task constructor.
     *
     * @param log Maven plugin log
     * @param bufferSize size of the buffer used to read source files
     * @param debug show source file paths in log output
     * @param skipMerge whether to skip the merge step or not
     * @param skipMinify whether to skip the minify step or not
     * @param webappSourceDir web resources source directory
     * @param webappTargetDir web resources target directory
     * @param inputDir directory containing source files
     * @param sourceFiles list of source files to include
     * @param sourceIncludes list of source files to include
     * @param sourceExcludes list of source files to exclude
     * @param outputDir directory to write the final file
     * @param outputFilename the output file name
     * @param suffix final filename suffix
     * @param nosuffix whether to use a suffix for the minified file name or not
     * @param charset if a character set is specified, a byte-to-char variant allows the encoding to be selected.
     * Otherwise, only byte-to-byte operations are used
     * @param linebreak split long lines after a specific column
     */
    public ProcessCSSFilesTask(Log log, Integer bufferSize, boolean debug, boolean skipMerge, boolean skipMinify,
            String webappSourceDir, String webappTargetDir, String inputDir, List<String> sourceFiles,
            List<String> sourceIncludes, List<String> sourceExcludes, String outputDir, String outputFilename,
            String suffix, boolean nosuffix, String charset, int linebreak) {
        super(log, bufferSize, debug, skipMerge, skipMinify, webappSourceDir, webappTargetDir, inputDir, sourceFiles,
                sourceIncludes, sourceExcludes, outputDir, outputFilename, suffix, nosuffix, charset, linebreak);
    }

    /**
     * Minifies CSS file.
     *
     * @param mergedFile input file resulting from the merged step
     * @param minifiedFile output file resulting from the minify step
     */
    @Override
    protected void minify(File mergedFile, File minifiedFile) {
        if (minifiedFile != null) {
            try {
                
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

                if (debug) {
                    log.info("Creating minified file [" + minifiedFile.getPath() + "].");
                } else {
                    File temp = (nosuffix) ? mergedFile : minifiedFile;
                    log.info("Creating minified file [" + temp.getName() + "].");
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
            
            cleanupFiles(mergedFile, minifiedFile);
        }
    }
}
