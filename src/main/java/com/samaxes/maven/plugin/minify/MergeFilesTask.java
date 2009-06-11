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
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.maven.plugin.logging.Log;

/**
 * Task for merging files.
 */
public class MergeFilesTask implements Callable<String> {

    private Log log;

    private Integer bufferSize;

    private String inputDir;

    private String outputDir;

    private List<String> pathnames = new ArrayList<String>();

    private String outputFilename;

    /**
     * Task constructor.
     * 
     * @param log Maven plugin log
     * @param bufferSize the size of the buffer used to read source files.
     * @param webAppSourceDir the web resources source directory
     * @param webAppTargetDir the web resources target directory
     * @param filesDir the directory containing the input files
     * @param filenames the filenames list
     * @param outputFilename the output filename
     */
    public MergeFilesTask(Log log, Integer bufferSize, String webAppSourceDir, String webAppTargetDir, String filesDir,
            List<String> filenames, String outputFilename) {
        this.log = log;
        this.bufferSize = bufferSize;
        this.inputDir = webAppSourceDir.concat("/").concat(filesDir).concat("/");
        this.outputDir = webAppTargetDir.concat("/").concat(filesDir).concat("/");
        for (String filename : filenames) {
            pathnames.add(inputDir.concat(filename));
        }
        this.outputFilename = outputFilename;
    }

    /**
     * Method executed by the thread.
     * 
     * @return the output pathname
     */
    public String call() throws Exception {
        mergeFiles();
        removeFiles();

        return outputDir.concat(outputFilename);
    }

    /**
     * Merges a list of files.
     */
    private void mergeFiles() {
        ListOfFiles listOfFiles = new ListOfFiles(this.log, pathnames);

        if (listOfFiles != null) {
            try {
                SequenceInputStream sequence = new SequenceInputStream(listOfFiles);
                OutputStream out = new FileOutputStream(outputDir.concat(outputFilename));
                byte[] buffer = new byte[bufferSize];
                int length;
                while ((length = sequence.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }

                sequence.close();
                out.close();
            } catch (IOException e) {
                log.error("An error occurred while concatenating files.", e);
            }
        }
    }

    /**
     * Removes original files.
     */
    private void removeFiles() {
        File dir = new File(outputDir);

        for (File file : dir.listFiles()) {
            if (!file.getName().equals(outputFilename)) {
                file.delete();
            }
        }
    }
}
