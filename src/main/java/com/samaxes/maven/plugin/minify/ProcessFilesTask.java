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
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

import com.samaxes.maven.plugin.common.ListOfFiles;

/**
 * Abstract class for merging and compressing a files list.
 */
public abstract class ProcessFilesTask implements Runnable {

    protected static final String SUFFIX = ".min";

    private static final String[] EMPTY_STRING_ARRAY = {};

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
     * @param sourceIncludes comma separated list of source files to include
     * @param sourceExcludes comma separated list of source files to exclude
     * @param finalFilename final filename
     * @param linebreak split long lines after a specific column
     */
    public ProcessFilesTask(Log log, Integer bufferSize, String webappSourceDir, String webappTargetDir,
            String filesDir, List<String> filenames, String sourceIncludes, String sourceExcludes,
            String finalFilename, int linebreak) {
        this.log = log;
        this.bufferSize = bufferSize;
        this.linebreak = linebreak;
        this.sourceDir = new File(webappSourceDir.concat(File.separator).concat(filesDir));
        this.targetDir = new File(webappTargetDir.concat(File.separator).concat(filesDir));

        List<File> includedFiles = (sourceIncludes != null && !"".equals(sourceIncludes)) ? getFilesToInclude(
                sourceDir, sourceIncludes, sourceExcludes) : new ArrayList<File>();
        for (String filename : filenames) {
            File sourceFile = new File(sourceDir, filename);
            log.debug("Adding source file [" + sourceFile.getName() + "]");
            files.add(sourceFile);
        }
        for (File includedFile : includedFiles) {
            if (!files.contains(includedFile)) {
                log.debug("Adding source file [" + includedFile.getName() + "]");
                files.add(includedFile);
            }
        }

        if (targetDir.exists() || targetDir.mkdirs()) { // TODO targetDir.exists() may return true
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
     * Returns a string array of the includes to be used when adding source files.
     * 
     * @param include comma separated list of source files to include
     * @return an array of tokens to include
     */
    private String[] getIncludes(String include) {
        return StringUtils.split(StringUtils.defaultString(include), ",");
    }

    /**
     * Returns a string array of the excludes to be used when adding source files.
     * 
     * @param exclude comma separated list of source files to exclude
     * @return an array of tokens to exclude
     */
    private String[] getExcludes(String exclude) {
        return (StringUtils.isNotEmpty(exclude)) ? StringUtils.split(exclude, ",") : EMPTY_STRING_ARRAY;
    }

    /**
     * Returns the files to copy. Even if the excludes are <code>null</code>, the default excludes are used.
     * 
     * @param baseDir the base directory to start from
     * @param include comma separated list of source files to include
     * @param exclude comma separated list of source files to exclude
     * @return the files to copy
     */
    private List<File> getFilesToInclude(File baseDir, String include, String exclude) {
        DirectoryScanner scanner = new DirectoryScanner();
        List<File> includedFiles = new ArrayList<File>();
        String[] includedFilenames;
        String[] includes = getIncludes(include);
        String[] excludes = getExcludes(exclude);

        scanner.setBasedir(baseDir);

        if (excludes != null) {
            scanner.setExcludes(excludes);
        }
        scanner.addDefaultExcludes();

        if (includes != null && includes.length > 0) {
            scanner.setIncludes(includes);
        }

        scanner.scan();
        includedFilenames = scanner.getIncludedFiles();

        for (String includedFilename : includedFilenames) {
            includedFiles.add(new File(baseDir, includedFilename));
        }

        return includedFiles;
    }

    /**
     * Merges files list.
     */
    private void mergeFiles() {
        ListOfFiles listOfFiles = new ListOfFiles(files);

        if (listOfFiles.size() > 0) {
            try {
                log.info("Merging files " + listOfFiles.toString());
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
