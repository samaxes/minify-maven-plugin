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
     * @param nosuffix whether to use a suffix for the minified filename or not
     * @param charset if a character set is specified, a byte-to-char variant allows the encoding to be selected.
     *        Otherwise, only byte-to-byte operations are used
     * @param linebreak split long lines after a specific column
     */
    public ProcessCSSFilesTask(final Log log, final Integer bufferSize, final boolean debug, final boolean skipMerge, final boolean skipMinify,
            final String webappSourceDir, final String webappTargetDir, final String inputDir, final List<String> sourceFiles,
            final List<String> sourceIncludes, final List<String> sourceExcludes, final String outputDir, final String outputFilename,
            final String suffix, final boolean nosuffix, final String charset, final int linebreak) {
        super(log, bufferSize, debug, skipMerge, skipMinify, webappSourceDir, webappTargetDir, inputDir, sourceFiles,
                sourceIncludes, sourceExcludes, outputDir, outputFilename, suffix, nosuffix, charset, linebreak);
    }

    /**
     * Minifies a CSS file.
     *
     * @param mergedFile input file resulting from the merged step
     * @param minifiedFile output file resulting from the minify step
     * @throws IOException when the minify step fails
     */
    @Override
    protected void minify(final File mergedFile, final File minifiedFile) throws IOException {
        try
        {
            final InputStream in = new FileInputStream(mergedFile);
            try
            {
                final OutputStream out = new FileOutputStream(minifiedFile);
                try
                {
                    final InputStreamReader reader = new InputStreamReader(in, this.charset);
                    try
                    {
                    final OutputStreamWriter writer = new OutputStreamWriter(out, this.charset);
                        try
                        {
                            this.log.info("Creating the minified file [" + ((this.debug) ? minifiedFile.getPath() : minifiedFile.getName())
                                    + "].");

                            final CssCompressor compressor = new CssCompressor(reader);
                            compressor.compress(writer, this.linebreak);
                        }
                        finally
                        {
                            writer.close();
                        }
                    }
                    finally
                    {
                        reader.close();
                    }
                }
                finally
                {
                    // may already be closed but make sure
                    out.close();
                }
            }
            finally
            {
                // may already be closed but make sure
                in.close();
            }
        } catch (final IOException e) {
            this.log.error("Failed to compress the CSS file [" + mergedFile.getName() + "].", e);
            throw e;
        }

        this.logCompressionGains(mergedFile, minifiedFile);
    }
}
