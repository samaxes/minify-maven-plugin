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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which combines and minifies CSS and JavaScript files.
 * 
 * @goal minify
 * @phase process-resources
 */
public class MinifyMojo extends AbstractMojo {

    /**
     * Webapp source directory.
     * 
     * @parameter expression="${minify.webappSourceDir}" default-value="${basedir}/src/main/webapp"
     */
    private String webappSourceDir;

    /**
     * Webapp target directory.
     * 
     * @parameter expression="${minify.webappTargetDir}"
     *            default-value="${project.build.directory}/${project.build.finalName}"
     */
    private String webappTargetDir;

    /**
     * CSS source directory.
     * 
     * @parameter expression="${minify.cssSourceDir}" default-value="css"
     */
    private String cssSourceDir;

    /**
     * JavaScript source directory.
     * 
     * @parameter expression="${minify.jsSourceDir}" default-value="js"
     */
    private String jsSourceDir;

    /**
     * CSS source filenames list.
     * 
     * @parameter alias="cssFiles"
     */
    private List<String> cssSourceFiles = new ArrayList<String>();

    /**
     * JavaScript source filenames list.
     * 
     * @parameter alias="jsFiles"
     */
    private List<String> jsSourceFiles = new ArrayList<String>();

    /**
     * CSS files to include. Specified as fileset patterns which are relative to the CSS source directory.
     * 
     * @parameter alias="cssIncludes"
     * @since 1.2
     */
    private List<String> cssSourceIncludes = new ArrayList<String>();

    /**
     * JavaScript files to include. Specified as fileset patterns which are relative to the JavaScript source directory.
     * 
     * @parameter alias="jsIncludes"
     * @since 1.2
     */
    private List<String> jsSourceIncludes = new ArrayList<String>();

    /**
     * CSS files to exclude. Specified as fileset patterns which are relative to the CSS source directory.
     * 
     * @parameter alias="cssExcludes"
     * @since 1.2
     */
    private List<String> cssSourceExcludes = new ArrayList<String>();

    /**
     * JavaScript files to exclude. Specified as fileset patterns which are relative to the JavaScript source directory.
     * 
     * @parameter alias="jsExcludes"
     * @since 1.2
     */
    private List<String> jsSourceExcludes = new ArrayList<String>();

    /**
     * CSS target directory.
     * 
     * @parameter expression="${minify.cssTargetDir}" default-value="css"
     * @since 1.3.2
     */
    private String cssTargetDir;

    /**
     * JavaScript target directory.
     * 
     * @parameter expression="${minify.jsTargetDir}" default-value="js"
     * @since 1.3.2
     */
    private String jsTargetDir;

    /**
     * CSS output filename.
     * 
     * @parameter expression="${minify.cssFinalFile}" default-value="style.css"
     */
    private String cssFinalFile;

    /**
     * JavaScript output filename.
     * 
     * @parameter expression="${minify.jsFinalFile}" default-value="script.js"
     */
    private String jsFinalFile;

    /**
     * The output filename suffix.
     * 
     * @parameter expression="${minify.suffix}" default-value=".min"
     * @since 1.3.2
     */
    private String suffix;

    /**
     * If a supported character set is specified, do a byte-to-char copy operation from an {@code InputStreamReader} to
     * an {@code OutputStreamWriter} using the specified charset. Otherwise, do a byte-to-byte copy operation from an
     * {@code InputStream} to an {@code OutputStream}, keeping the original file encoding.
     * 
     * <p>
     * See the <a href="http://www.iana.org/assignments/character-sets">IANA Charset Registry</a> for a list of valid
     * encoding types.
     * </p>
     * 
     * @parameter expression="${minify.charset}"
     * @since 1.3.2
     */
    private String charset;

    /**
     * Some source control tools don't like files containing lines longer than, say 8000 characters. The linebreak
     * option is used in that case to split long lines after a specific column. It can also be used to make the code
     * more readable, easier to debug (especially with the MS Script Debugger). Specify 0 to get a line break after each
     * semi-colon in JavaScript, and after each rule in CSS. Specify -1 to disallow line breaks.
     * 
     * @parameter expression="${minify.linebreak}" default-value="-1"
     */
    private int linebreak;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Minify only. Do not obfuscate local symbols.
     * 
     * @parameter expression="${minify.munge}" default-value="false"
     */
    private boolean nomunge;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Display informational messages and warnings.
     * 
     * @parameter expression="${minify.verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Preserve unnecessary semicolons (such as right before a '}'). This option is useful when compressed code has to
     * be run through JSLint (which is the case of YUI for example).
     * 
     * @parameter expression="${minify.preserveAllSemiColons}" default-value="false"
     */
    private boolean preserveAllSemiColons;

    /**
     * JAVASCRIPT ONLY OPTION!<br/>
     * Disable all the built-in micro optimizations.
     * 
     * @parameter expression="${minify.disableOptimizations}" default-value="false"
     */
    private boolean disableOptimizations;

    /**
     * Size of the buffer used to read source files.
     * 
     * @parameter expression="${minify.bufferSize}" default-value="4096"
     */
    private int bufferSize;

    /**
     * Executed when the goal is invoked, it will first invoke a parallel lifecycle, ending at the given phase.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> processCSSFilesTask = executor.submit(new ProcessCSSFilesTask(getLog(), bufferSize, webappSourceDir,
                webappTargetDir, cssSourceDir, cssSourceFiles, cssSourceIncludes, cssSourceExcludes, cssTargetDir,
                cssFinalFile, suffix, charset, linebreak));
        Future<?> processJSFilesTask = executor
                .submit(new ProcessJSFilesTask(getLog(), bufferSize, webappSourceDir, webappTargetDir, jsSourceDir,
                        jsSourceFiles, jsSourceIncludes, jsSourceExcludes, jsTargetDir, jsFinalFile, suffix, charset,
                        linebreak, !nomunge, verbose, preserveAllSemiColons, disableOptimizations));

        try {
            if (processCSSFilesTask != null) {
                processCSSFilesTask.get();
            }
            if (processJSFilesTask != null) {
                processJSFilesTask.get();
            }
        } catch (InterruptedException e) {
            getLog().error(e.getMessage(), e);
        } catch (ExecutionException e) {
            getLog().error(e.getMessage(), e);
        }
    }
}
