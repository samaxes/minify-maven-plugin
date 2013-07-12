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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.common.base.Strings;

/**
 * Goal for combining and minifying CSS and JavaScript files.
 *
 * @goal minify
 * @phase process-resources
 */
public class MinifyMojo extends AbstractMojo {

    /**
     * Webapp source directory.
     *
     * @parameter expression="${webappSourceDir}" default-value="${basedir}/src/main/webapp"
     */
    private String webappSourceDir;

    /**
     * Webapp target directory.
     *
     * @parameter expression="${webappTargetDir}" default-value="${project.build.directory}/${project.build.finalName}"
     */
    private String webappTargetDir;

    /**
     * CSS source directory.
     *
     * @parameter expression="${cssSourceDir}" default-value="css"
     */
    private String cssSourceDir;

    /**
     * JavaScript source directory.
     *
     * @parameter expression="${jsSourceDir}" default-value="js"
     */
    private String jsSourceDir;

    /**
     * CSS source filenames list.
     *
     * @parameter expression="${cssSourceFiles}" alias="cssFiles"
     */
    private ArrayList<String> cssSourceFiles;

    /**
     * JavaScript source filenames list.
     *
     * @parameter expression="${jsSourceFiles}" alias="jsFiles"
     */
    private ArrayList<String> jsSourceFiles;

    /**
     * CSS files to include. Specified as fileset patterns which are relative to the CSS source directory.
     *
     * @parameter expression="${cssSourceIncludes}" alias="cssIncludes"
     * @since 1.2
     */
    private ArrayList<String> cssSourceIncludes;

    /**
     * JavaScript files to include. Specified as fileset patterns which are relative to the JavaScript source directory.
     *
     * @parameter expression="${jsSourceIncludes}" alias="jsIncludes"
     * @since 1.2
     */
    private ArrayList<String> jsSourceIncludes;

    /**
     * CSS files to exclude. Specified as fileset patterns which are relative to the CSS source directory.
     *
     * @parameter expression="${cssSourceExcludes}" alias="cssExcludes"
     * @since 1.2
     */
    private ArrayList<String> cssSourceExcludes;

    /**
     * JavaScript files to exclude. Specified as fileset patterns which are relative to the JavaScript source directory.
     *
     * @parameter expression="${jsSourceExcludes}" alias="jsExcludes"
     * @since 1.2
     */
    private ArrayList<String> jsSourceExcludes;

    /**
     * CSS target directory.
     * <p>
     * Takes the same value as <code>cssSourceDir</code> when empty.
     * </p>
     *
     * @parameter expression="${cssTargetDir}"
     * @since 1.3.2
     */
    private String cssTargetDir;

    /**
     * JavaScript target directory.
     * <p>
     * Takes the same value as <code>jsSourceDir</code> when empty.
     * </p>
     *
     * @parameter expression="${jsTargetDir}"
     * @since 1.3.2
     */
    private String jsTargetDir;

    /**
     * CSS output filename.
     *
     * @parameter expression="${cssFinalFile}" default-value="style.css"
     */
    private String cssFinalFile;

    /**
     * JavaScript output filename.
     *
     * @parameter expression="${jsFinalFile}" default-value="script.js"
     */
    private String jsFinalFile;

    /**
     * The output filename suffix.
     * 
     * @parameter expression="${suffix}" default-value=".min"
     * @since 1.3.2
     */
    private String suffix;

    /**
     * Do not append a suffix to the minified output filename, independently of the value in the <code>suffix</code>
     * parameter.
     *
     * @parameter expression="${nosuffix}" default-value="false"
     * @since 1.7
     */
    private boolean nosuffix;

    /**
     * <p>
     * If a supported character set is specified, it will be used to read the input file. Otherwise, it will assume that
     * the platform's default character set is being used. The output file is encoded using the same character set.
     * </p>
     * <p>
     * See the <a href="http://www.iana.org/assignments/character-sets">IANA Charset Registry</a> for a list of valid
     * encoding types.
     * </p>
     *
     * @parameter expression="${charset}" default-value="UTF-8"
     * @since 1.3.2
     */
    private String charset;

    /**
     * Some source control tools don't like files containing lines longer than, say 8000 characters. The linebreak
     * option is used in that case to split long lines after a specific column. It can also be used to make the code
     * more readable, easier to debug (especially with the MS Script Debugger). Specify 0 to get a line break after each
     * semi-colon in JavaScript, and after each rule in CSS. Specify -1 to disallow line breaks.
     *
     * @parameter expression="${linebreak}" default-value="-1"
     */
    private int linebreak;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Minify only. Do not obfuscate local symbols.
     *
     * @parameter expression="${munge}" default-value="false"
     */
    private boolean nomunge;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Display informational messages and warnings.
     *
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Preserve unnecessary semicolons (such as right before a '}'). This option is useful when compressed code has to
     * be run through JSLint (which is the case of YUI for example).
     *
     * @parameter expression="${preserveAllSemiColons}" default-value="false"
     */
    private boolean preserveAllSemiColons;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Disable all the built-in micro optimizations.
     *
     * @parameter expression="${disableOptimizations}" default-value="false"
     */
    private boolean disableOptimizations;

    /**
     * Size of the buffer used to read source files.
     *
     * @parameter expression="${bufferSize}" default-value="4096"
     */
    private int bufferSize;

    /**
     * Show source file paths in log output.
     *
     * @parameter expression="${debug}" default-value="false"
     * @since 1.5.2
     */
    private boolean debug;

    /**
     * Skip the merge step. Minification will be applied to each source file individually.
     *
     * @parameter expression="${skipMerge}" default-value="false"
     * @since 1.5.2
     */
    private boolean skipMerge;

    /**
     * Skip the minify step. Useful when merging files that are already minified.
     *
     * @parameter expression="${skipMinify}" default-value="false"
     * @since 1.5.2
     */
    private boolean skipMinify;

    /**
     * <p>
     * JAVASCRIPT ONLY OPTION!<br/>
     * Define the JavaScript compressor engine to use.
     * </p>
     * <p>
     * Possible values are:
     * </p>
     * <ul>
     * <li><code>yui</code> - <a href="http://developer.yahoo.com/yui/compressor/">YUI Compressor</a></li>
     * <li><code>closure</code> - <a href="https://developers.google.com/closure/compiler/">Google Closure Compiler</a></li>
     * </ul>
     *
     * @parameter expression="${jsEngine}" default-value="yui"
     * @since 1.6
     */
    private String jsEngine;

    /**
     * Executed when the goal is invoked, it will first invoke a parallel lifecycle, ending at the given phase.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.skipMerge && this.skipMinify) {
            this.getLog().warn("Both merge and minify steps are configured to be skipped.");
            return;
        }
        if (Strings.isNullOrEmpty(this.cssTargetDir)) {
            this.cssTargetDir = this.cssSourceDir;
        }
        if (Strings.isNullOrEmpty(this.jsTargetDir)) {
            this.jsTargetDir = this.jsSourceDir;
        }

        final Collection<ProcessFilesTask> processFilesTasks = new ArrayList<ProcessFilesTask>();
        processFilesTasks.add(new ProcessCSSFilesTask(this.getLog(), this.bufferSize, this.debug, this.skipMerge, this.skipMinify,
                this.webappSourceDir, this.webappTargetDir, this.cssSourceDir, this.cssSourceFiles, this.cssSourceIncludes, this.cssSourceExcludes,
                this.cssTargetDir, this.cssFinalFile, this.suffix, this.nosuffix, this.charset, this.linebreak));
        processFilesTasks.add(new ProcessJSFilesTask(this.getLog(), this.bufferSize, this.debug, this.skipMerge, this.skipMinify,
                this.webappSourceDir, this.webappTargetDir, this.jsSourceDir, this.jsSourceFiles, this.jsSourceIncludes, this.jsSourceExcludes,
                this.jsTargetDir, this.jsFinalFile, this.suffix, this.nosuffix, this.charset, this.linebreak, this.jsEngine, !this.nomunge, this.verbose,
                this.preserveAllSemiColons, this.disableOptimizations));

        final ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final List<Future<Object>> futures = executor.invokeAll(processFilesTasks);
            for (final Future<Object> future : futures) {
                try {
                    future.get();
                } catch (final ExecutionException e) {
                    throw new MojoFailureException(e.getMessage(), e);
                }
            }
            executor.shutdown();
        } catch (final InterruptedException e) {
            executor.shutdownNow();
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
