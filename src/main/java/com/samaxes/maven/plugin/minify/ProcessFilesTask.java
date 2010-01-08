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

import org.apache.maven.plugin.logging.Log;

/**
 * Abstract class for merging and compressing a files list.
 */
public abstract class ProcessFilesTask implements Runnable {

    protected final static String suffix = ".min";

    protected Log log;

    protected File targetDir;

    protected File finalFile;

    protected int linebreak;

    private Integer bufferSize;

    private File sourceDir;

    private List<File> files = new ArrayList<File>();

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
    public ProcessFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String filesDir, List<String> filenames, String finalFilename, int linebreak) {
        this.log = log;
        this.bufferSize = bufferSize;
        this.sourceDir = new File(webappSourceDir.concat(File.separator).concat(filesDir));
        this.targetDir = new File(webappTargetDir.concat(File.separator).concat(filesDir));
        this.linebreak = linebreak;
        for (String filename : filenames) {
            files.add(new File(sourceDir, filename));
        }
        if (targetDir.exists() || targetDir.mkdirs()) {
            this.finalFile = new File(targetDir, finalFilename);
        }
    }

    /**
     * Method executed by the thread.
     */
    public void run() {
        mergeFiles();
        minify();
    }

    /**
     * Merges files list.
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
     * Minifies source file.
     */
    abstract void minify();
}
