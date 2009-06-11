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
 * @phase process-sources
 */
public class MinifyMojo extends AbstractMojo {

    /**
     * Webapp source directory.
     * 
     * @parameter expression="${minify.webAppDir}" default-value="${basedir}/src/main/webapp"
     * @required
     */
    private String webAppDir;

    /**
     * Webapp output directory.
     * 
     * @parameter expression="${minify.webAppTargetDir}"
     *            default-value="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    private String webAppTargetDir;

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
    private List<String> cssFilenames = new ArrayList<String>();

    /**
     * JavaScript filenames list.
     * 
     * @parameter
     */
    private List<String> jsFilenames = new ArrayList<String>();

    /**
     * CSS output filename.
     * 
     * @parameter expression="${minify.cssFinalName}" default-value="style.css"
     */
    private String cssFinalName;

    /**
     * JavaScript output filename.
     * 
     * @parameter expression="${minify.jsFinalName}" default-value="script.js"
     */
    private String jsFinalName;

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
        Future<String> cssMergeTask = executor.submit(new MergeFilesTask(getLog(), bufferSize, webAppDir,
                webAppTargetDir, cssDir, cssFilenames, cssFinalName));
        Future<String> jsMergeTask = executor.submit(new MergeFilesTask(getLog(), bufferSize, webAppDir,
                webAppTargetDir, jsDir, jsFilenames, jsFinalName));
        Future<?> cssCompressTask;
        Future<?> jsCompressTask;

        try {
            cssCompressTask = executor.submit(new CompressFileTask(getLog(), cssMergeTask.get(), linebreak, !nomunge,
                    verbose, preserveAllSemiColons, disableOptimizations));
            jsCompressTask = executor.submit(new CompressFileTask(getLog(), jsMergeTask.get(), linebreak, !nomunge,
                    verbose, preserveAllSemiColons, disableOptimizations));
            cssCompressTask.get();
            jsCompressTask.get();
        } catch (InterruptedException e) {
            getLog().error(e.getMessage(), e);
        } catch (ExecutionException e) {
            getLog().error(e.getMessage(), e);
        }
    }
}
