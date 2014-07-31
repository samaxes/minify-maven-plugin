/*
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

import com.samaxes.maven.minify.common.SourceFilesEnumeration;
import com.samaxes.maven.minify.common.YuiConfig;
import com.samaxes.maven.minify.plugin.MinifyMojo.Engine;

/**
 * Abstract class for merging and compressing a files list.
 */
public abstract class ProcessFilesTask implements Callable<Object> {

    public static final String TEMP_SUFFIX = ".tmp";

    protected final Log log;

    protected final boolean verbose;

    protected final Integer bufferSize;

    protected final String charset;

    protected final String suffix;

    protected final boolean nosuffix;

    protected final boolean skipMerge;

    protected final boolean skipMinify;

    protected final Engine engine;

    protected final YuiConfig yuiConfig;

    private final File sourceDir;

    private final File targetDir;

    private final String mergedFilename;

    private final List<File> files = new ArrayList<File>();

    private final boolean sourceFilesEmpty;

    private final boolean sourceIncludesEmpty;

    /**
     * Task constructor.
     *
     * @param log Maven plugin log
     * @param verbose display additional info
     * @param bufferSize size of the buffer used to read source files
     * @param charset if a character set is specified, a byte-to-char variant allows the encoding to be selected.
     *        Otherwise, only byte-to-byte operations are used
     * @param suffix final file name suffix
     * @param nosuffix whether to use a suffix for the minified file name or not
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
     * @param engine minify processor engine selected
     * @param yuiConfig YUI Compressor configuration
     * @throws FileNotFoundException when the given source file does not exist
     */
    public ProcessFilesTask(Log log, boolean verbose, Integer bufferSize, String charset, String suffix,
            boolean nosuffix, boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir,
            String inputDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
            String outputDir, String outputFilename, Engine engine, YuiConfig yuiConfig) throws FileNotFoundException {
        this.log = log;
        this.verbose = verbose;
        this.bufferSize = bufferSize;
        this.charset = charset;
        this.suffix = suffix + ".";
        this.nosuffix = nosuffix;
        this.skipMerge = skipMerge;
        this.skipMinify = skipMinify;
        this.engine = engine;
        this.yuiConfig = yuiConfig;

        this.sourceDir = new File(webappSourceDir + File.separator + inputDir);
        this.targetDir = new File(webappTargetDir + File.separator + outputDir);
        this.mergedFilename = outputFilename;
        for (String sourceFilename : sourceFiles) {
            addNewSourceFile(mergedFilename, sourceFilename);
        }
        for (File sourceInclude : getFilesToInclude(sourceIncludes, sourceExcludes)) {
            if (!files.contains(sourceInclude)) {
                addNewSourceFile(mergedFilename, sourceInclude);
            }
        }
        this.sourceFilesEmpty = sourceFiles.isEmpty();
        this.sourceIncludesEmpty = sourceIncludes.isEmpty();
    }

    /**
     * Method executed by the thread.
     *
     * @throws IOException when the merge or minify steps fail
     */
    @Override
    public Object call() throws IOException {
        synchronized (log) {
            String fileType = (this instanceof ProcessCSSFilesTask) ? "CSS" : "JavaScript";
            log.info("Starting " + fileType + " task:");

            if (!files.isEmpty() && (targetDir.exists() || targetDir.mkdirs())) {
                if (skipMerge) {
                    log.info("Skipping the merge step...");
                    String sourceBasePath = sourceDir.getAbsolutePath();

                    for (File mergedFile : files) {
                        // Create folders to preserve sub-directory structure when only minifying
                        String originalPath = mergedFile.getAbsolutePath();
                        String subPath = originalPath.substring(sourceBasePath.length(),
                                originalPath.lastIndexOf(File.separator));
                        File targetPath = new File(targetDir.getAbsolutePath() + subPath);
                        targetPath.mkdirs();

                        File minifiedFile = new File(targetPath, (nosuffix) ? mergedFile.getName()
                                : FileUtils.basename(mergedFile.getName()) + suffix
                                        + FileUtils.getExtension(mergedFile.getName()));
                        minify(mergedFile, minifiedFile);
                    }
                } else if (skipMinify) {
                    File mergedFile = new File(targetDir, mergedFilename);
                    merge(mergedFile);
                    log.info("Skipping the minify step...");
                } else {
                    File mergedFile = new File(targetDir, (nosuffix) ? mergedFilename + TEMP_SUFFIX : mergedFilename);
                    merge(mergedFile);
                    File minifiedFile = new File(targetDir, (nosuffix) ? mergedFilename
                            : FileUtils.basename(mergedFilename) + suffix + FileUtils.getExtension(mergedFilename));
                    minify(mergedFile, minifiedFile);
                    if (nosuffix) {
                        if (!mergedFile.delete()) {
                            mergedFile.deleteOnExit();
                        }
                    }
                }
                log.info("");
            } else if (!sourceFilesEmpty || !sourceIncludesEmpty) {
                // 'files' list will be empty if source file paths or names added to the project's POM are invalid.
                log.error("No valid " + fileType + " source files found to process.");
            }
        }

        return null;
    }

    /**
     * Merges a list of source files.
     *
     * @param mergedFile output file resulting from the merged step
     * @throws IOException when the merge step fails
     */
    protected void merge(File mergedFile) throws IOException {
        try (InputStream sequence = new SequenceInputStream(new SourceFilesEnumeration(log, files, verbose));
                OutputStream out = new FileOutputStream(mergedFile);
                InputStreamReader sequenceReader = new InputStreamReader(sequence, charset);
                OutputStreamWriter outWriter = new OutputStreamWriter(out, charset)) {
            log.info("Creating the merged file [" + ((verbose) ? mergedFile.getPath() : mergedFile.getName()) + "].");

            IOUtil.copy(sequenceReader, outWriter, bufferSize);
        } catch (IOException e) {
            log.error("Failed to concatenate files.", e);
            throw e;
        }
    }

    /**
     * Minifies a source file.
     *
     * @param mergedFile input file resulting from the merged step
     * @param minifiedFile output file resulting from the minify step
     * @throws IOException when the minify step fails
     */
    abstract void minify(File mergedFile, File minifiedFile) throws IOException;

    /**
     * Logs compression gains.
     *
     * @param mergedFile input file resulting from the merged step
     * @param minifiedFile output file resulting from the minify step
     */
    void logCompressionGains(File mergedFile, File minifiedFile) {
        try {
            File temp = File.createTempFile(minifiedFile.getName(), ".gz");

            try (InputStream in = new FileInputStream(minifiedFile);
                    OutputStream out = new FileOutputStream(temp);
                    GZIPOutputStream outGZIP = new GZIPOutputStream(out)) {
                IOUtil.copy(in, outGZIP, bufferSize);
            }

            log.info("Uncompressed size: " + mergedFile.length() + " bytes.");
            log.info("Compressed size: " + minifiedFile.length() + " bytes minified (" + temp.length()
                    + " bytes gzipped).");

            temp.deleteOnExit();
        } catch (IOException e) {
            log.debug("Failed to calculate the gzipped file size.", e);
        }
    }

    /**
     * Logs an addition of a new source file.
     *
     * @param finalFilename the final file name
     * @param sourceFilename the source file name
     * @throws FileNotFoundException when the given source file does not exist
     */
    private void addNewSourceFile(String finalFilename, String sourceFilename) throws FileNotFoundException {
        File sourceFile = new File(sourceDir, sourceFilename);

        addNewSourceFile(finalFilename, sourceFile);
    }

    /**
     * Logs an addition of a new source file.
     *
     * @param finalFilename the final file name
     * @param sourceFile the source file
     * @throws FileNotFoundException when the given source file does not exist
     */
    private void addNewSourceFile(String finalFilename, File sourceFile) throws FileNotFoundException {
        if (sourceFile.exists()) {
            if (finalFilename.equalsIgnoreCase(sourceFile.getName())) {
                log.warn("The source file [" + ((verbose) ? sourceFile.getPath() : sourceFile.getName())
                        + "] has the same name as the final file.");
            }
            log.debug("Adding source file [" + ((verbose) ? sourceFile.getPath() : sourceFile.getName()) + "].");
            files.add(sourceFile);
        } else {
            throw new FileNotFoundException("The source file ["
                    + ((verbose) ? sourceFile.getPath() : sourceFile.getName()) + "] does not exist.");
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

            Collections.sort(includedFiles, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                }
            });
        }

        return includedFiles;
    }
}
