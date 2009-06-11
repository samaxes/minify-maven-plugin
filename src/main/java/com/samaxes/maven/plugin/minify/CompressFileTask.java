/*
 * $Id$
 *
 * MinifyMojo Maven Plugin
 * http://code.google.com/p/maven-samaxes-plugin/
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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.maven.plugin.logging.Log;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Task for compressing a file.
 */
public class CompressFileTask implements Runnable {

    private Log log;

    private String extension;

    private String inputPathname;

    private String outputPathname;

    private int linebreak;

    private boolean munge;

    private boolean verbose;

    private boolean preserveAllSemiColons;

    private boolean disableOptimizations;

    private final String suffix = ".min";

    /**
     * Task constructor.
     * 
     * @param log Maven plugin log
     * @param inputPathname the input pathname
     * @param linebreak split long lines after a specific column
     * @param munge minify only
     * @param verbose display informational messages and warnings
     * @param preserveAllSemiColons preserve unnecessary semicolons
     * @param disableOptimizations disable all the built-in micro optimizations
     */
    public CompressFileTask(Log log, String inputPathname, int linebreak, boolean munge, boolean verbose,
            boolean preserveAllSemiColons, boolean disableOptimizations) {
        this.log = log;
        this.extension = inputPathname.substring(inputPathname.lastIndexOf('.'));
        this.inputPathname = inputPathname;
        this.outputPathname = inputPathname.replace(extension, suffix.concat(extension));
        this.linebreak = linebreak;
        this.munge = munge;
        this.verbose = verbose;
        this.preserveAllSemiColons = preserveAllSemiColons;
        this.disableOptimizations = disableOptimizations;
    }

    /**
     * Method executed by the thread.
     */
    public void run() {
        try {
            Reader reader = new FileReader(inputPathname);
            Writer writer = new FileWriter(outputPathname);

            if (".css".equalsIgnoreCase(extension)) {
                CssCompressor compressor = new CssCompressor(reader);
                compressor.compress(writer, linebreak);
            } else if (".js".equalsIgnoreCase(extension)) {
                JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new JavaScriptErrorReporter(log));
                compressor.compress(writer, linebreak, munge, verbose, preserveAllSemiColons, disableOptimizations);
            }

            reader.close();
            writer.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
