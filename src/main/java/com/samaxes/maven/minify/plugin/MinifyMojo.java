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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.base.Strings;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

/**
 * Goal for combining and minifying CSS and JavaScript files.
 */
@Mojo(name = "minify", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class MinifyMojo extends AbstractMojo {

    /* Global Options */

    /**
     * Webapp source directory.
     */
    @Parameter(property = "webappSourceDir", defaultValue = "${basedir}/src/main/webapp")
    private String webappSourceDir;

    /**
     * Webapp target directory.
     */
    @Parameter(property = "webappTargetDir", defaultValue = "${project.build.directory}/${project.build.finalName}")
    private String webappTargetDir;

    /**
     * CSS source directory.
     */
    @Parameter(property = "cssSourceDir", defaultValue = "css")
    private String cssSourceDir;

    /**
     * JavaScript source directory.
     */
    @Parameter(property = "jsSourceDir", defaultValue = "js")
    private String jsSourceDir;

    /**
     * CSS source filenames list.
     */
    @Parameter(property = "cssSourceFiles", alias = "cssFiles")
    private ArrayList<String> cssSourceFiles;

    /**
     * JavaScript source filenames list.
     */
    @Parameter(property = "jsSourceFiles", alias = "jsFiles")
    private ArrayList<String> jsSourceFiles;

    /**
     * CSS files to include. Specified as fileset patterns which are relative to the CSS source directory.
     *
     * @since 1.2
     */
    @Parameter(property = "cssSourceIncludes", alias = "cssIncludes")
    private ArrayList<String> cssSourceIncludes;

    /**
     * JavaScript files to include. Specified as fileset patterns which are relative to the JavaScript source directory.
     *
     * @since 1.2
     */
    @Parameter(property = "jsSourceIncludes", alias = "jsIncludes")
    private ArrayList<String> jsSourceIncludes;

    /**
     * CSS files to exclude. Specified as fileset patterns which are relative to the CSS source directory.
     *
     * @since 1.2
     */
    @Parameter(property = "cssSourceExcludes", alias = "cssExcludes")
    private ArrayList<String> cssSourceExcludes;

    /**
     * JavaScript files to exclude. Specified as fileset patterns which are relative to the JavaScript source directory.
     *
     * @since 1.2
     */
    @Parameter(property = "jsSourceExcludes", alias = "jsExcludes")
    private ArrayList<String> jsSourceExcludes;

    /**
     * CSS target directory. Takes the same value as <code>cssSourceDir</code> when empty.
     *
     * @since 1.3.2
     */
    @Parameter(property = "cssTargetDir")
    private String cssTargetDir;

    /**
     * JavaScript target directory. Takes the same value as <code>jsSourceDir</code> when empty.
     *
     * @since 1.3.2
     */
    @Parameter(property = "jsTargetDir")
    private String jsTargetDir;

    /**
     * CSS output filename.
     */
    @Parameter(property = "cssFinalFile", defaultValue = "style.css")
    private String cssFinalFile;

    /**
     * JavaScript output filename.
     */
    @Parameter(property = "jsFinalFile", defaultValue = "script.js")
    private String jsFinalFile;

    /**
     * Define the CSS compressor engine to use.<br/>
     * Possible values are:
     * <ul>
     * <li><code>yui</code> - <a href="http://yui.github.io/yuicompressor/">YUI Compressor</a></li>
     * </ul>
     *
     * @since 1.7.1
     */
    @Parameter(property = "cssEngine", defaultValue = "yui")
    private String cssEngine;

    /**
     * Define the JavaScript compressor engine to use.<br/>
     * Possible values are:
     * <ul>
     * <li><code>yui</code> - <a href="http://yui.github.io/yuicompressor/">YUI Compressor</a></li>
     * <li><code>closure</code> - <a href="https://developers.google.com/closure/compiler/">Google Closure Compiler</a></li>
     * </ul>
     *
     * @since 1.6
     */
    @Parameter(property = "jsEngine", defaultValue = "yui")
    private String jsEngine;

    /**
     * The output filename suffix.
     *
     * @since 1.3.2
     */
    @Parameter(property = "suffix", defaultValue = "min")
    private String suffix;

    /**
     * Do not append a suffix to the minified output filename, independently of the value in the <code>suffix</code>
     * parameter.
     *
     * @since 1.7
     */
    @Parameter(property = "nosuffix", defaultValue = "false")
    private boolean nosuffix;

    /**
     * If a supported character set is specified, it will be used to read the input file. Otherwise, it will assume that
     * the platform's default character set is being used. The output file is encoded using the same character set.<br/>
     * See the <a href="http://www.iana.org/assignments/character-sets">IANA Charset Registry</a> for a list of valid
     * encoding types.
     *
     * @since 1.3.2
     */
    @Parameter(property = "charset", defaultValue = "${project.build.sourceEncoding}")
    private String charset;

    /**
     * Size of the buffer used to read source files.
     */
    @Parameter(property = "bufferSize", defaultValue = "4096")
    private int bufferSize;

    /**
     * Show source file paths in log output.
     *
     * @since 1.5.2
     * @deprecated Use {@link #verbose} instead.
     */
    @Deprecated
    @Parameter(property = "debug")
    private Boolean debug;

    /**
     * Display additional informational messages and warnings.
     */
    @Parameter(property = "verbose", defaultValue = "false")
    private boolean verbose;

    /**
     * Skip the merge step. Minification will be applied to each source file individually.
     *
     * @since 1.5.2
     */
    @Parameter(property = "skipMerge", defaultValue = "false")
    private boolean skipMerge;

    /**
     * Skip the minify step. Useful when merging files that are already minified.
     *
     * @since 1.5.2
     */
    @Parameter(property = "skipMinify", defaultValue = "false")
    private boolean skipMinify;

    /* YUI Compressor Only Options */

    /**
     * Some source control tools don't like files containing lines longer than, say 8000 characters. The linebreak
     * option is used in that case to split long lines after a specific column. It can also be used to make the code
     * more readable and easier to debug. Specify {@code 0} to get a line break after each semi-colon in JavaScript, and
     * after each rule in CSS. Specify {@code -1} to disallow line breaks.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ Global Option ]
     *
     * @deprecated Use {@link #yuiLinebreak} instead.
     */
    @Deprecated
    @Parameter(property = "linebreak")
    private Integer linebreak;

    /**
     * Some source control tools don't like files containing lines longer than, say 8000 characters. The linebreak
     * option is used in that case to split long lines after a specific column. It can also be used to make the code
     * more readable and easier to debug. Specify {@code 0} to get a line break after each semi-colon in JavaScript, and
     * after each rule in CSS. Specify {@code -1} to disallow line breaks.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ Global Option ]
     */
    @Parameter(property = "yuiLinebreak", defaultValue = "-1")
    private int yuiLinebreak;

    /**
     * Minify only. Do not obfuscate local symbols.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ JavaScript Only Option ]
     *
     * @deprecated Use {@link #yuiMunge} instead.
     */
    @Deprecated
    @Parameter(property = "munge")
    private Boolean munge;

    /**
     * Minify only. Do not obfuscate local symbols.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ JavaScript Only Option ]
     */
    @Parameter(property = "yuiMunge", defaultValue = "true")
    private boolean yuiMunge;

    /**
     * Preserve unnecessary semicolons (such as right before a '}'). This option is useful when compressed code has to
     * be run through JSLint.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ JavaScript Only Option ]
     *
     * @deprecated Use {@link #yuiPreserveAllSemiColons} instead.
     */
    @Deprecated
    @Parameter(property = "preserveAllSemiColons")
    private Boolean preserveAllSemiColons;

    /**
     * Preserve unnecessary semicolons (such as right before a '}'). This option is useful when compressed code has to
     * be run through JSLint.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ JavaScript Only Option ]
     */
    @Parameter(property = "yuiPreserveAllSemiColons", defaultValue = "false")
    private boolean yuiPreserveAllSemiColons;

    /**
     * Disable all the built-in micro-optimizations.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ JavaScript Only Option ]
     *
     * @deprecated Use {@link #yuiDisableOptimizations} instead.
     */
    @Deprecated
    @Parameter(property = "disableOptimizations")
    private Boolean disableOptimizations;

    /**
     * Disable all the built-in micro-optimizations.<br/>
     * <strong>Supported engine is</strong>: YUI Compressor [ JavaScript Only Option ]
     */
    @Parameter(property = "yuiDisableOptimizations", defaultValue = "false")
    private boolean yuiDisableOptimizations;

    /* Google Closure Compiler Only Options */

    /**
     * Refers to which version of ECMAScript to assume when checking for errors in your code.<br/>
     * Possible values are:
     * <ul>
     * <li><code>ECMASCRIPT3</code> - Checks code assuming ECMAScript 3 compliance, and gives errors for code using
     * features only present in ECMAScript 5.</li>
     * <li><code>ECMASCRIPT5</code> - Checks code assuming ECMAScript 5 compliance, allowing new features not present in
     * ECMAScript 3.</li>
     * <li><code>ECMASCRIPT5_STRICT</code> - Like ECMASCRIPT5 but assumes compliance with strict mode ('use strict';).</li>
     * </ul>
     *
     * @since 1.7.2
     */
    @Parameter(property = "closureLanguageIn", defaultValue = "ECMASCRIPT3")
    private LanguageMode closureLanguage;

    /**
     * Executed when the goal is invoked, it will first invoke a parallel lifecycle, ending at the given phase.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        checkDeprecatedOptions();

        if (skipMerge && skipMinify) {
            getLog().warn("Both merge and minify steps are configured to be skipped.");
            return;
        }
        if (Strings.isNullOrEmpty(cssTargetDir)) {
            cssTargetDir = cssSourceDir;
        }
        if (Strings.isNullOrEmpty(jsTargetDir)) {
            jsTargetDir = jsSourceDir;
        }

        Collection<ProcessFilesTask> processFilesTasks = new ArrayList<ProcessFilesTask>();
        processFilesTasks.add(new ProcessCSSFilesTask(getLog(), bufferSize, debug, skipMerge, skipMinify, cssEngine,
                webappSourceDir, webappTargetDir, cssSourceDir, cssSourceFiles, cssSourceIncludes, cssSourceExcludes,
                cssTargetDir, cssFinalFile, suffix, nosuffix, charset, linebreak));
        processFilesTasks.add(new ProcessJSFilesTask(getLog(), bufferSize, debug, skipMerge, skipMinify, jsEngine,
                webappSourceDir, webappTargetDir, jsSourceDir, jsSourceFiles, jsSourceIncludes, jsSourceExcludes,
                jsTargetDir, jsFinalFile, suffix, nosuffix, charset, linebreak, munge, verbose, preserveAllSemiColons,
                disableOptimizations, closureLanguage));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Object>> futures = executor.invokeAll(processFilesTasks);
            for (Future<Object> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw new MojoFailureException(e.getMessage(), e);
                }
            }
            executor.shutdown();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private void checkDeprecatedOptions() {
        if (debug == null) {
            debug = verbose;
        } else {
            getLog().warn(
                    "The option 'debug' is deprecated and will be removed on the next version. Use 'verbose' instead.");
        }
        if (linebreak == null) {
            linebreak = yuiLinebreak;
        } else {
            getLog().warn(
                    "The option 'linebreak' is deprecated and will be removed on the next version. Use 'yuiLinebreak' instead.");
        }
        if (munge == null) {
            munge = yuiMunge;
        } else {
            getLog().warn(
                    "The option 'munge' is deprecated and will be removed on the next version. Use 'yuiMunge' instead.");
        }
        if (preserveAllSemiColons == null) {
            preserveAllSemiColons = yuiPreserveAllSemiColons;
        } else {
            getLog().warn(
                    "The option 'preserveAllSemiColons' is deprecated and will be removed on the next version. Use 'yuiPreserveAllSemiColons' instead.");
        }
        if (disableOptimizations == null) {
            disableOptimizations = yuiDisableOptimizations;
        } else {
            getLog().warn(
                    "The option 'disableOptimizations' is deprecated and will be removed on the next version. Use 'yuiDisableOptimizations' instead.");
        }
    }
}
