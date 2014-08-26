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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.mozilla.javascript.EvaluatorException;

import com.google.common.collect.Lists;
import com.google.javascript.jscomp.CommandLineRunner;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.samaxes.maven.minify.common.ClosureConfig;
import com.samaxes.maven.minify.common.JavaScriptErrorReporter;
import com.samaxes.maven.minify.common.YuiConfig;
import com.samaxes.maven.minify.plugin.MinifyMojo.Engine;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Task for merging and compressing JavaScript files.
 */
public class ProcessJSFilesTask extends ProcessFilesTask {

    private final ClosureConfig closureConfig;

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
     * @param closureConfig Google Closure Compiler configuration
     * @throws FileNotFoundException when the given source file does not exist
     */
    public ProcessJSFilesTask(Log log, boolean verbose, Integer bufferSize, String charset, String suffix,
            boolean nosuffix, boolean skipMerge, boolean skipMinify, String webappSourceDir, String webappTargetDir,
            String inputDir, List<String> sourceFiles, List<String> sourceIncludes, List<String> sourceExcludes,
            String outputDir, String outputFilename, Engine engine, YuiConfig yuiConfig, ClosureConfig closureConfig)
            throws FileNotFoundException {
        super(log, verbose, bufferSize, charset, suffix, nosuffix, skipMerge, skipMinify, webappSourceDir,
                webappTargetDir, inputDir, sourceFiles, sourceIncludes, sourceExcludes, outputDir, outputFilename,
                engine, yuiConfig);

        this.closureConfig = closureConfig;
    }

    /**
     * Minifies a JavaScript file.
     *
     * @param mergedFile input file resulting from the merged step
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
                case CLOSURE:
                    log.debug("Using Google Closure Compiler engine.");

                    CompilerOptions options = new CompilerOptions();
                    closureConfig.getCompilationLevel().setOptionsForCompilationLevel(options);
                    options.setOutputCharset(charset);
                    options.setLanguageIn(closureConfig.getLanguage());
                    options.setAngularPass(closureConfig.getAngularPass());
                    options.setDependencyOptions(closureConfig.getDependencyOptions());

                    File sourceMapResult = new File(minifiedFile.getPath() + ".map");
                    if (closureConfig.getSourceMapFormat() != null) {
                        options.setSourceMapFormat(closureConfig.getSourceMapFormat());
                        options.setSourceMapOutputPath(sourceMapResult.getPath());
                        // options.setSourceMapLocationMappings(Lists.newArrayList(new
                        // SourceMap.LocationMapping(sourceDir.getPath() + File.separator, "")));
                    }

                    SourceFile input = SourceFile.fromInputStream(mergedFile.getName(), in);
                    List<SourceFile> externs = closureConfig.getExterns();
                    if (closureConfig.getUseDefaultExterns()) {
                        externs.addAll(CommandLineRunner.getDefaultExterns());
                    }

                    Compiler compiler = new Compiler();
                    compiler.compile(externs, Lists.newArrayList(input), options);

                    if (compiler.hasErrors()) {
                        throw new EvaluatorException(compiler.getErrors()[0].description);
                    }

                    writer.append(compiler.toSource());

                    if (closureConfig.getSourceMapFormat() != null) {
                        log.info("Creating the minified file map ["
                                + ((verbose) ? sourceMapResult.getPath() : sourceMapResult.getName()) + "].");

                        sourceMapResult.createNewFile();
                        flushSourceMap(sourceMapResult, minifiedFile.getName(), compiler.getSourceMap());

                        writer.append(System.getProperty("line.separator"));
                        writer.append("//# sourceMappingURL=" + sourceMapResult.getName());
                    }

                    break;
                case YUI:
                    log.debug("Using YUI Compressor engine.");

                    JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new JavaScriptErrorReporter(log,
                            mergedFile.getName()));
                    compressor.compress(writer, yuiConfig.getLineBreak(), yuiConfig.isMunge(), verbose,
                            yuiConfig.isPreserveSemicolons(), yuiConfig.isDisableOptimizations());
                    break;
                default:
                    log.warn("JavaScript engine not supported.");
                    break;
            }
        } catch (IOException e) {
            log.error(
                    "Failed to compress the JavaScript file ["
                            + ((verbose) ? mergedFile.getPath() : mergedFile.getName()) + "].", e);
            throw e;
        }

        logCompressionGains(mergedFile, minifiedFile);
    }

    private void flushSourceMap(File sourceMapOutputFile, String minifyFileName, SourceMap sourceMap) {
        try (FileWriter out = new FileWriter(sourceMapOutputFile)) {
            sourceMap.appendTo(out, minifyFileName);
        } catch (IOException e) {
            log.error("Failed to write the JavaScript Source Map file ["
                    + ((verbose) ? sourceMapOutputFile.getPath() : sourceMapOutputFile.getName()) + "].", e);
        }
    }
}
