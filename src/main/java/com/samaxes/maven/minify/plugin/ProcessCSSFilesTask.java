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

import com.samaxes.maven.minify.common.YuiConfig;
import com.samaxes.maven.minify.plugin.MinifyMojo.Engine;
import com.yahoo.platform.yui.compressor.CssCompressor;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.util.List;

/**
 * Task for merging and compressing CSS files.
 */
public class ProcessCSSFilesTask extends ProcessFilesTask {

    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private Log log;
        private boolean verbose;
        private Integer bufferSize;
        private String charset;
        private String suffix;
        private boolean nosuffix;
        private boolean skipMerge;
        private boolean skipMinify;
        private String webappSourceDir;
        private String webappTargetDir;
        private String inputDir;
        private List<String> sourceFiles;
        private List<String> sourceIncludes;
        private List<String> sourceExcludes;
        private String outputDir;
        private String outputFilename;
        private Engine engine;
        private YuiConfig yuiConfig;

        public ProcessCSSFilesTask build() {
            return new ProcessCSSFilesTask(log, verbose, bufferSize, charset, suffix,
                    nosuffix, skipMerge, skipMinify, webappSourceDir, webappTargetDir,
                    inputDir, sourceFiles, sourceIncludes, sourceExcludes,
                    outputDir, outputFilename, engine, yuiConfig);
        }

        public Builder setLog(Log log) {
            this.log = log;
            return this;
        }

        public Builder setVerbose(boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        public Builder setBufferSize(Integer bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder setNosuffix(boolean nosuffix) {
            this.nosuffix = nosuffix;
            return this;
        }

        public Builder setSkipMerge(boolean skipMerge) {
            this.skipMerge = skipMerge;
            return this;
        }

        public Builder setSkipMinify(boolean skipMinify) {
            this.skipMinify = skipMinify;
            return this;
        }

        public Builder setWebappSourceDir(String webappSourceDir) {
            this.webappSourceDir = webappSourceDir;
            return this;
        }

        public Builder setWebappTargetDir(String webappTargetDir) {
            this.webappTargetDir = webappTargetDir;
            return this;
        }

        public Builder setInputDir(String inputDir) {
            this.inputDir = inputDir;
            return this;
        }

        public Builder setSourceFiles(List<String> sourceFiles) {
            this.sourceFiles = sourceFiles;
            return this;
        }

        public Builder setSourceIncludes(List<String> sourceIncludes) {
            this.sourceIncludes = sourceIncludes;
            return this;
        }

        public Builder setSourceExcludes(List<String> sourceExcludes) {
            this.sourceExcludes = sourceExcludes;
            return this;
        }

        public Builder setOutputDir(String outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder setOutputFilename(String outputFilename) {
            this.outputFilename = outputFilename;
            return this;
        }

        public Builder setEngine(Engine engine) {
            this.engine = engine;
            return this;
        }

        public Builder setYuiConfig(YuiConfig yuiConfig) {
            this.yuiConfig = yuiConfig;
            return this;
        }
    }

    /**
     * Task constructor.
     *
     * @param log             Maven plugin log
     * @param verbose         display additional info
     * @param bufferSize      size of the buffer used to read source files
     * @param charset         if a character set is specified, a byte-to-char variant allows the encoding to be selected.
     *                        Otherwise, only byte-to-byte operations are used
     * @param suffix          final file name suffix
     * @param nosuffix        whether to use a suffix for the minified file name or not
     * @param skipMerge       whether to skip the merge step or not
     * @param skipMinify      whether to skip the minify step or not
     * @param webappSourceDir web resources source directory
     * @param webappTargetDir web resources target directory
     * @param inputDir        directory containing source files
     * @param sourceFiles     list of source files to include
     * @param sourceIncludes  list of source files to include
     * @param sourceExcludes  list of source files to exclude
     * @param outputDir       directory to write the final file
     * @param outputFilename  the output file name
     * @param engine          minify processor engine selected
     * @param yuiConfig       YUI Compressor configuration
     */
    public ProcessCSSFilesTask(Log log, boolean verbose, Integer bufferSize, String charset, String suffix,
                               boolean nosuffix, boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir,
                               String inputDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
                               String outputDir, String outputFilename, Engine engine, YuiConfig yuiConfig) {
        super(log, verbose, bufferSize, charset, suffix, nosuffix, skipMerge, skipMinify, webappSourceDir,
                webappTargetDir, inputDir, sourceFiles, sourceIncludes, sourceExcludes, outputDir, outputFilename,
                engine, yuiConfig);
    }

    /**
     * Minifies a CSS file.
     *
     * @param mergedFile   input file resulting from the merged step
     * @param minifiedFile output file resulting from the minify step
     * @throws IOException when the minify step fails
     */
    @Override
    protected void minify(File mergedFile, File minifiedFile) throws IOException {
        try (InputStream in = new FileInputStream(mergedFile);
             OutputStream out = new FileOutputStream(minifiedFile);
             InputStreamReader reader = new InputStreamReader(in, charset);
             OutputStreamWriter writer = new OutputStreamWriter(out, charset)) {
            log.info("Creating the minified file [" + ((verbose) ? minifiedFile.getPath() : minifiedFile.getName())
                    + "].");

            switch (engine) {
                case YUI:
                    log.debug("Using YUI Compressor engine.");

                    CssCompressor compressor = new CssCompressor(reader);
                    compressor.compress(writer, yuiConfig.getLinebreak());
                    break;
                default:
                    log.warn("CSS engine not supported.");
                    break;
            }
        } catch (IOException e) {
            log.error("Failed to compress the CSS file [" + mergedFile.getName() + "].", e);
            throw e;
        }

        logCompressionGains(mergedFile, minifiedFile);
    }
}
