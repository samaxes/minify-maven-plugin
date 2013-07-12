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
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

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

    protected final Log log;

    protected final Integer bufferSize;

    protected final boolean debug;

    protected final boolean skipMerge;

    protected final boolean skipMinify;

    protected final String charset;

    protected final int linebreak;

    protected final boolean nosuffix;

    protected final String suffix;

    private final String mergedFilename;

    private final File sourceDir;

    private final File targetDir;

    private final boolean sourceFilesEmpty;

    private final boolean sourceIncludesEmpty;

    private final List<File> files = new ArrayList<File>();

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
     * @param sourceFilenames list of source files to include
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
    public ProcessFilesTask(final Log log, final Integer bufferSize, final boolean debug, final boolean skipMerge, final boolean skipMinify,
            final String webappSourceDir, final String webappTargetDir, final String inputDir, final List<String> sourceFilenames,
            final List<String> sourceIncludes, final List<String> sourceExcludes, final String outputDir, final String outputFilename,
            final String suffix, final boolean nosuffix, final String charset, final int linebreak) {
        this.log = log;
        this.bufferSize = bufferSize;
        this.debug = debug;
        this.skipMerge = skipMerge;
        this.skipMinify = skipMinify;
        this.charset = charset;
        this.linebreak = linebreak;
        this.nosuffix = nosuffix;
        this.mergedFilename = outputFilename;
        this.suffix = suffix;

        this.sourceDir = new File(webappSourceDir + File.separator + inputDir);
        for (final String sourceFilename : sourceFilenames) {
            this.addNewSourceFile(this.mergedFilename, sourceFilename);
        }
        for (final File sourceInclude : this.getFilesToInclude(sourceIncludes, sourceExcludes))
        {
            if (!this.files.contains(sourceInclude)) {
                this.addNewSourceFile(this.mergedFilename, sourceInclude);
            }
        }

        this.targetDir = new File(webappTargetDir + File.separator + outputDir);
        this.sourceFilesEmpty = sourceFilenames.isEmpty();
        this.sourceIncludesEmpty = sourceIncludes.isEmpty();
    }

    /**
     * Method executed by the thread.
     *
     * @throws IOException when the merge or minify steps fail
     */
    @Override
    public Object call() throws IOException {
        synchronized (this.log) {
            final String fileType = (this instanceof ProcessCSSFilesTask) ? "CSS" : "JavaScript";
            this.log.info("Starting " + fileType + " task:");

            if (!this.files.isEmpty() && (this.targetDir.exists() || this.targetDir.mkdirs())) {
                if (this.skipMerge) {

                    this.log.info("Skipping the merge step...");
                    final String sourceBasePath = this.sourceDir.getAbsolutePath();

                    for (final File mergedFile : this.files) {

                        final String originalPath = mergedFile.getAbsolutePath();
                        final String subDirectory = originalPath.substring(sourceBasePath.length(),
                                originalPath.lastIndexOf(File.separator));

                        final File targetDir = new File(this.targetDir.getAbsolutePath() + subDirectory);
                        targetDir.mkdirs();

                        String extension = FileUtils.getExtension(mergedFile.getName());
                        if (extension.length() > 0)
                        {
                            // need to manually add the dot which is explicitly not returned
                            extension = "." + extension;
                        }
                        final String targetFileName = (this.nosuffix) ? mergedFile.getName() : (FileUtils.removeExtension(FileUtils
                                .filename(mergedFile.getName())) + this.suffix + extension);
                        final File minifiedFile = new File(targetDir, targetFileName);

                        this.minify(mergedFile, minifiedFile);
                    }
                } else if (this.skipMinify) {

                    final File mergedFile = new File(this.targetDir, this.mergedFilename);
                    this.merge(mergedFile);
                    this.log.info("Skipping the minify step...");

                } else {

                    final File mergedFile = new File(this.targetDir, (this.nosuffix) ? this.mergedFilename + TEMP_SUFFIX : this.mergedFilename);
                    this.merge(mergedFile);

                    String extension = FileUtils.getExtension(this.mergedFilename);
                    if (extension.length() > 0)
                    {
                        // need to manually add the dot which is explicitly not returned
                        extension = "." + extension;
                    }
                    final File minifiedFile = new File(this.targetDir, (this.nosuffix) ? this.mergedFilename
                            : FileUtils.removeExtension(this.mergedFilename) + this.suffix + extension);
                    this.minify(mergedFile, minifiedFile);

                    if (this.nosuffix) {
                        mergedFile.deleteOnExit();
                    }
                }
                this.log.info("Completed  " + fileType + " task");
            } else if (!this.sourceFilesEmpty || !this.sourceIncludesEmpty) {
                // 'files' list will be empty if source file paths or names added to the project's POM are invalid.
                this.log.error("No valid " + fileType + " source files found to process.");
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
    protected void merge(final File mergedFile) throws IOException {
        try (InputStream sequence = new SequenceInputStream(new ListOfFiles(this.log, this.files, this.debug));
                OutputStream out = new FileOutputStream(mergedFile);
                InputStreamReader sequenceReader = new InputStreamReader(sequence, this.charset);
                OutputStreamWriter outWriter = new OutputStreamWriter(out, this.charset)) {
            this.log.info("Creating the merged file [" + ((this.debug) ? mergedFile.getPath() : mergedFile.getName()) + "].");

            IOUtil.copy(sequenceReader, outWriter, this.bufferSize);
        } catch (final IOException e) {
            this.log.error("Failed to concatenate files.", e);
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
    void logCompressionGains(final File mergedFile, final File minifiedFile) {
        try {
            final File temp = File.createTempFile(minifiedFile.getName(), ".gz");

            try (InputStream in = new FileInputStream(minifiedFile);
                    OutputStream out = new FileOutputStream(temp);
                    GZIPOutputStream outGZIP = new GZIPOutputStream(out)) {
                IOUtil.copy(in, outGZIP, this.bufferSize);
            }

            this.log.info("Uncompressed size: " + mergedFile.length() + " bytes.");
            this.log.info("Compressed size: " + minifiedFile.length() + " bytes minified (" + temp.length()
                    + " bytes gzipped).");

            temp.deleteOnExit();
        } catch (final IOException e) {
            this.log.debug("Failed to calculate the gzipped file size.", e);
        }
    }

    /**
     * Logs an addition of a new source file.
     *
     * @param finalFilename the final file name
     * @param sourceDir the sources directory
     * @param sourceFilename the source file name
     */
    private void addNewSourceFile(final String finalFilename, final String sourceFilename)
    {
        final File sourceFile = new File(this.sourceDir, sourceFilename);

        this.addNewSourceFile(finalFilename, sourceFile);
    }

    /**
     * Logs an addition of a new source file.
     *
     * @param finalFilename the final file name
     * @param sourceDir the sources directory
     * @param sourceFile the source file
     */
    private void addNewSourceFile(final String finalFilename, final File sourceFile)
    {
        if (sourceFile.exists()) {
            if (finalFilename.equalsIgnoreCase(sourceFile.getName())) {
                this.log.warn("The source file [" + ((this.debug) ? sourceFile.getPath() : sourceFile.getName())
                        + "] has the same name as the final file.");
            }
            this.log.debug("Adding source file [" + ((this.debug) ? sourceFile.getPath() : sourceFile.getName()) + "].");
            this.files.add(sourceFile);
        } else {
            this.log.warn("The source file [" + ((this.debug) ? sourceFile.getPath() : sourceFile.getName())
                    + "] does not exist.");
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
    private List<File> getFilesToInclude(final List<String> includes, final List<String> excludes)
    {
        final List<File> includedFiles = new ArrayList<File>();

        if (includes != null && !includes.isEmpty()) {
            final DirectoryScanner scanner = new DirectoryScanner();

            scanner.setIncludes(includes.toArray(new String[0]));
            scanner.setExcludes(excludes.toArray(new String[0]));
            scanner.addDefaultExcludes();
            scanner.setBasedir(this.sourceDir);
            scanner.scan();

            for (final String includedFilename : scanner.getIncludedFiles()) {
                includedFiles.add(new File(this.sourceDir, includedFilename));
            }

            Collections.sort(includedFiles, new FilenameComparator());
        }

        return includedFiles;
    }
}
