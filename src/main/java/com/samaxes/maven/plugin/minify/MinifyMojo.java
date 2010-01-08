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
     * @required
     */
    private String webappSourceDir;

    /**
     * Webapp target directory.
     * 
     * @parameter expression="${minify.webappTargetDir}"
     *            default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private String webappTargetDir;

    /**
     * CSS source directory.
     * 
     * @parameter expression="${minify.cssDir}" default-value="css"
     * @required
     */
    private String cssDir;

    /**
     * JavaScript source directory.
     * 
     * @parameter expression="${minify.jsDir}" default-value="js"
     * @required
     */
    private String jsDir;

    /**
     * CSS filenames list.
     * 
     * @parameter
     */
    private List<String> cssFiles = new ArrayList<String>();

    /**
     * JavaScript filenames list.
     * 
     * @parameter
     */
    private List<String> jsFiles = new ArrayList<String>();

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
     * Some source control tools don't like files containing lines longer than, say 8000 characters. The linebreak
     * option is used in that case to split long lines after a specific column. It can also be used to make the code
     * more readable, easier to debug (especially with the MS Script Debugger). Specify 0 to get a line break after each
     * semi-colon in JavaScript, and after each rule in CSS. Specify -1 to disallow line breaks.
     * 
     * @parameter expression="${minify.linebreak}" default-value="-1"
     * @required
     */
    private int linebreak;

    /**
     * Minify only. Do not obfuscate local symbols.
     * 
     * @parameter expression="${minify.munge}" default-value="false"
     * @required
     */
    private boolean nomunge;

    /**
     * Display informational messages and warnings.
     * 
     * @parameter expression="${minify.verbose}" default-value="false"
     * @required
     */
    private boolean verbose;

    /**
     * Preserve unnecessary semicolons (such as right before a '}'). This option is useful when compressed code has to
     * be run through JSLint (which is the case of YUI for example).
     * 
     * @parameter expression="${minify.preserveAllSemiColons}" default-value="false"
     * @required
     */
    private boolean preserveAllSemiColons;

    /**
     * Disable all the built-in micro optimizations.
     * 
     * @parameter expression="${minify.disableOptimizations}" default-value="false"
     * @required
     */
    private boolean disableOptimizations;

    /**
     * Size of the buffer used to read source files.
     * 
     * @parameter expression="${minify.bufferSize}" default-value="4096"
     * @required
     */
    private int bufferSize;

    /**
     * Executed when the goal is invoked, it will first invoke a parallel lifecycle, ending at the given phase.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<?> processCSSFilesTask = (cssFiles.isEmpty()) ? null : executor.submit(new ProcessCSSFilesTask(getLog(),
                bufferSize, webappSourceDir, webappTargetDir, cssDir, cssFiles, cssFinalFile, linebreak));
        Future<?> processJSFilesTask = (jsFiles.isEmpty()) ? null : executor.submit(new ProcessJSFilesTask(getLog(),
                bufferSize, webappSourceDir, webappTargetDir, jsDir, jsFiles, jsFinalFile, linebreak, !nomunge,
                verbose, preserveAllSemiColons, disableOptimizations));

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
