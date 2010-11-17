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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.IOUtil;

import com.samaxes.maven.plugin.common.FilenameComparator;
import com.samaxes.maven.plugin.common.ListOfFiles;

/**
 * Abstract class for merging and compressing a files list.
 */
public abstract class ProcessFilesTask implements Runnable {

    protected static final String SUFFIX = "min.";

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
     * @param sourceFiles list of source files to include
     * @param sourceIncludes list of source files to include
     * @param sourceExcludes list of source files to exclude
     * @param finalFile final filename
     * @param linebreak split long lines after a specific column
     */
    public ProcessFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String filesDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
            String finalFile, int linebreak) {
        this.log = log;
        this.bufferSize = bufferSize;
        this.linebreak = linebreak;
        this.sourceDir = new File(webappSourceDir.concat(File.separator).concat(filesDir));
        this.targetDir = new File(webappTargetDir.concat(File.separator).concat(filesDir));

        for (String sourceFile : sourceFiles) {
            logNewSourceFile(finalFile, sourceFile);
            files.add(new File(sourceDir, sourceFile));
        }

        for (File sourceInclude : getFilesToInclude(sourceDir, sourceIncludes, sourceExcludes)) {
            if (!files.contains(sourceInclude)) {
                logNewSourceFile(finalFile, sourceInclude.getName());
                files.add(sourceInclude);
            }
        }

        if (!files.isEmpty() && (targetDir.exists() || targetDir.mkdirs())) {
            this.finalFile = new File(targetDir, finalFile);
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
     * Logs an addition of a new source file.
     * 
     * @param finalFilename the final file name
     * @param sourceFilename the source file name
     */
    private void logNewSourceFile(String finalFilename, String sourceFilename) {
        if (finalFilename.equalsIgnoreCase(sourceFilename)) {
            throw new IllegalArgumentException("Source file should not have the same name as final file ["
                    + sourceFilename + "]");
        } else {
            log.debug("Adding source file [" + sourceFilename + "]");
        }
    }

    /**
     * Returns the files to copy. Default exclusions are used when the excludes list is empty.
     * 
     * @param baseDir the base directory to start from
     * @param includes list of source files to include
     * @param excludes list of source files to exclude
     * @return the files to copy
     */
    private List<File> getFilesToInclude(File baseDir, List<String> includes, List<String> excludes) {
        List<File> includedFiles = new ArrayList<File>();

        if (includes != null && !includes.isEmpty()) {
            DirectoryScanner scanner = new DirectoryScanner();

            scanner.setIncludes(includes.toArray(new String[] {}));
            scanner.setExcludes(excludes.toArray(new String[] {}));
            scanner.addDefaultExcludes();
            scanner.setBasedir(baseDir);
            scanner.scan();

            for (String includedFilename : scanner.getIncludedFiles()) {
                includedFiles.add(new File(baseDir, includedFilename));
            }

            Collections.sort(includedFiles, new FilenameComparator());
        }

        return includedFiles;
    }

    /**
     * Merges files list.
     */
    private void mergeFiles() {
        if (!files.isEmpty()) {
            ListOfFiles listOfFiles = new ListOfFiles(log, files);

            try {
                log.info("Creating final file [" + finalFile.getName() + "]");
                InputStream sequence = new SequenceInputStream(listOfFiles);
                OutputStream out = new FileOutputStream(finalFile);

                IOUtil.copy(sequence, out, bufferSize);
                IOUtil.close(sequence);
                IOUtil.close(out);
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
