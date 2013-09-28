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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import com.samaxes.maven.minify.common.FilenameComparator;
import com.samaxes.maven.minify.common.ListOfFiles;

/**
 * Abstract class for merging and compressing a files list.
 */
public abstract class ProcessFilesTask implements Callable<Object> {

    public static final String TEMP_SUFFIX = ".tmp";

    protected Log log;

    protected Integer bufferSize;

    protected boolean debug;

    protected boolean skipMerge;

    protected boolean skipMinify;

    private String mergedFilename;

    private String suffix;

    protected boolean nosuffix;

    protected String charset;

    protected int linebreak;

    private File sourceDir;

    private File targetDir;

    private String extension;

    private boolean sourceFilesEmpty;

    private boolean sourceIncludesEmpty;

    private List<File> files = new ArrayList<File>();

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
    public ProcessFilesTask(Log log, Integer bufferSize, boolean debug, boolean skipMerge, boolean skipMinify,
            String webappSourceDir, String webappTargetDir, String inputDir, List<String> sourceFiles,
            List<String> sourceIncludes, List<String> sourceExcludes, String outputDir, String outputFilename,
            String suffix, boolean nosuffix, String charset, int linebreak) {
        this.log = log;
        this.bufferSize = bufferSize;
        this.debug = debug;
        this.skipMerge = skipMerge;
        this.skipMinify = skipMinify;
        this.mergedFilename = outputFilename;
        this.suffix = suffix;
        this.nosuffix = nosuffix;
        this.charset = charset;
        this.linebreak = linebreak;

        this.sourceDir = new File(webappSourceDir + File.separator + inputDir);
        for (String sourceFilename : sourceFiles) {
            addNewSourceFile(mergedFilename, sourceFilename);
        }
        for (File sourceInclude : getFilesToInclude(sourceIncludes, sourceExcludes)) {
            if (!files.contains(sourceInclude)) {
                addNewSourceFile(mergedFilename, sourceInclude);
            }
        }

        this.targetDir = new File(webappTargetDir + File.separator + outputDir);
        this.extension = "." + FileUtils.getExtension(mergedFilename);
        this.sourceFilesEmpty = sourceFiles.isEmpty();
        this.sourceIncludesEmpty = sourceIncludes.isEmpty();
    }

    /**
     * Method executed by the thread.
     */
    public Object call() {
        if (!files.isEmpty() && (targetDir.exists() || targetDir.mkdirs())) {
            if (skipMerge) {
                log.info("Skipping merge step.");
                String sourceBasePath = sourceDir.getAbsolutePath();

                for (File mergedFile : files) {
                    // Create folders to preserve sub-directory structure when only minifying
                    String originalPath = mergedFile.getAbsolutePath();
                    String subPath = originalPath.substring(sourceBasePath.length(),
                            originalPath.lastIndexOf(File.separator));
                    File targetPath = new File(targetDir.getAbsolutePath() + subPath);
                    targetPath.mkdirs();
                    log.info("nosuffix?" + nosuffix);

                    File minifiedFile = new File(targetPath, (nosuffix) ? mergedFile.getName() : mergedFile.getName()
                            .replace(extension, suffix + extension));
                    log.info("Calling minify on: " + minifiedFile.getName());
                    minify(mergedFile, minifiedFile);
                }
            } else if (skipMinify) {
                File mergedFile = new File(targetDir, mergedFilename);
                merge(mergedFile);
                log.info("Skipping minify step.");
            } else {
                File mergedFile = new File(targetDir, (nosuffix) ? mergedFilename + TEMP_SUFFIX : mergedFilename);
                File minifiedFile = new File(targetDir, (nosuffix) ? mergedFilename
                        : mergedFile.getName().replace(extension, suffix + extension));
                merge(mergedFile);
                minify(mergedFile, minifiedFile);
                if (nosuffix) {
                    if (!mergedFile.delete()) {
                        mergedFile.deleteOnExit();
                    }
                }
            }
        } else if (!sourceFilesEmpty || !sourceIncludesEmpty) {
            // The 'files' list will be empty if the source file paths or names added to the project's POM are wrong.
            String fileType = ("CSS".equalsIgnoreCase(extension.substring(1))) ? "CSS" : "JavaScript";
            log.error("No valid " + fileType + " source files found to process.");
        }

        return null;
    }

    /**
     * Merges files list.
     *
     * @param mergedFile output file resulting from the merged step
     */
    private void merge(File mergedFile) {
        if (mergedFile != null) {
            ListOfFiles listOfFiles = new ListOfFiles(log, files, debug);

            try {
                log.info("Creating merged file [" + ((debug) ? mergedFile.getPath() : mergedFile.getName()) + "].");
                InputStream sequence = new SequenceInputStream(listOfFiles);
                OutputStream out = new FileOutputStream(mergedFile);

                if (charset == null) {
                    IOUtil.copy(sequence, out, bufferSize);
                } else {
                    InputStreamReader sequenceReader = new InputStreamReader(sequence, charset);
                    OutputStreamWriter outWriter = new OutputStreamWriter(out, charset);

                    IOUtil.copy(sequenceReader, outWriter, bufferSize);
                    IOUtil.close(sequenceReader);
                    IOUtil.close(outWriter);
                }

                IOUtil.close(sequence);
                IOUtil.close(out);
            } catch (IOException e) {
                log.error("An error has occurred while concatenating files.", e);
            }
        }
    }

    /**
     * Minifies source file.
     *
     * @param mergedFile input file resulting from the merged step
     * @param minifiedFile output file resulting from the minify step
     */
    abstract void minify(File file, File minifiedFile);

    /**
     * Logs an addition of a new source file.
     *
     * @param finalFilename the final file name
     * @param sourceFilename the source file name
     */
    private void addNewSourceFile(String finalFilename, String sourceFilename) {
        File sourceFile = new File(sourceDir, sourceFilename);

        addNewSourceFile(finalFilename, sourceFile);
    }

    /**
     * Cleanup files. Remove merged file is nosuffix parameter was set to true.
     *
     * @param mergedFile input file resulting from the merged step
     * @param minifiedFile output file resulting from the minify step
     */
    void cleanupFiles(File mergedFile, File minifiedFile) {
        if (nosuffix) {
            String mergedFilename = mergedFile.getName();
            URI mergedFileURI = mergedFile.toURI();
            if (debug) {
                log.info("Deleting the file [" + mergedFilename + "].");
            }
            mergedFile.delete();
            if (debug) {
                log.info("Renaming the file [" + minifiedFile.getName() + "] to [" + mergedFilename + "].");
            }
            minifiedFile.renameTo(new File(mergedFileURI));
        }
    }

    /**
     * Logs an addition of a new source file.
     *
     * @param finalFilename the final file name
     * @param sourceFile the source file
     */
    private void addNewSourceFile(String finalFilename, File sourceFile) {
        if (sourceFile.exists()) {
            if (finalFilename.equalsIgnoreCase(sourceFile.getName())) {
                log.warn("Source file [" + sourceFile.getName() + "] has the same name as the final file.");
            }
            log.debug("Source file [" + sourceFile.getName() + "] added.");
            files.add(sourceFile);
        } else {
            log.warn("Source file [" + sourceFile.getName() + "] was not included beacause it does not exist.");
        }
    }

    /**
     * Returns the files to copy. Default exclusions are used when the excludes list is empty.
     *
     * @param includes list of source files to include
     * @param excludes list of source files to exclude
     * @return the files to copy
     */
    private List<File> getFilesToInclude(List<String> includes, List<String> excludes) {
        List<File> includedFiles = new ArrayList<File>();

        if (includes != null && !includes.isEmpty()) {
            DirectoryScanner scanner = new DirectoryScanner();

            scanner.setIncludes(includes.toArray(new String[0]));
            scanner.setExcludes(excludes.toArray(new String[0]));
            scanner.addDefaultExcludes();
            scanner.setBasedir(sourceDir);
            scanner.scan();

            for (String includedFilename : scanner.getIncludedFiles()) {
                includedFiles.add(new File(sourceDir, includedFilename));
            }

            Collections.sort(includedFiles, new FilenameComparator());
        }

        return includedFiles;
    }
}
