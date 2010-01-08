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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.logging.Log;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Task for merging and compressing a list of files.
 */
public class ProcessFilesTask implements Runnable {

    private Log log;

    private Integer bufferSize;

    private File sourceDir;

    private File targetDir;

    private List<File> files = new ArrayList<File>();

    private File finalFile;

    private String finalFileExtension;

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
     * @param bufferSize size of the buffer used to read source files.
     * @param webappSourceDir web resources source directory
     * @param webappTargetDir web resources target directory
     * @param filesDir directory containing input files
     * @param filenames filenames list
     * @param finalFilename final filename
     * @param finalFileExtension final file extension
     * @param linebreak split long lines after a specific column
     * @param munge minify only
     * @param verbose display informational messages and warnings
     * @param preserveAllSemiColons preserve unnecessary semicolons
     * @param disableOptimizations disable all the built-in micro optimizations
     */
    public ProcessFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String filesDir, List<String> filenames, String finalFilename, String finalFileExtension, int linebreak,
            boolean munge, boolean verbose, boolean preserveAllSemiColons, boolean disableOptimizations) {
        this.log = log;
        this.bufferSize = bufferSize;
        this.sourceDir = new File(webappSourceDir.concat(File.separator).concat(filesDir));
        this.targetDir = new File(webappTargetDir.concat(File.separator).concat(filesDir));
        for (String filename : filenames) {
            files.add(new File(sourceDir, filename));
        }
        if (targetDir.exists() || targetDir.mkdirs()) {
            this.finalFile = new File(targetDir, finalFilename);
        }
        this.finalFileExtension = finalFileExtension;

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
        mergeFiles();
        minify();
    }

    /**
     * Merges a list of files.
     */
    private void mergeFiles() {
        ListOfFiles listOfFiles = new ListOfFiles(this.log, files);
        log.info("Merging files " + listOfFiles.toString());

        if (listOfFiles.size() > 0) {
            try {
                SequenceInputStream sequence = new SequenceInputStream(listOfFiles);
                OutputStream out = new FileOutputStream(finalFile);
                byte[] buffer = new byte[bufferSize];
                int length;
                while ((length = sequence.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                sequence.close();
                out.close();
            } catch (IOException e) {
                log.error("An error has occurred while concatenating files.", e);
            }
        }
    }

    /**
     * Minifies a CSS or JavaScript file.
     * 
     * @param sourceFile the source file
     */
    private void minify() {
        if (finalFile.exists()) {
            String name = finalFile.getName();
            log.info("Minifying file " + name);

            String extension = name.substring(name.lastIndexOf('.'));
            File destFile = new File(targetDir, name.replace(extension, suffix.concat(extension)));

            try {
                Reader reader = new FileReader(finalFile);
                Writer writer = new FileWriter(destFile);

                if (finalFileExtension.equalsIgnoreCase(extension)) {
                    CssCompressor compressor = new CssCompressor(reader);
                    compressor.compress(writer, linebreak);
                } else if (finalFileExtension.equalsIgnoreCase(extension)) {
                    JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new JavaScriptErrorReporter(log,
                            name));
                    compressor.compress(writer, linebreak, munge, verbose, preserveAllSemiColons, disableOptimizations);
                }

                reader.close();
                writer.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
