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
package com.samaxes.maven.minify.common;

import com.google.javascript.jscomp.*;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.jscomp.SourceMap.Format;

import java.util.List;
import java.util.Map;

/**
 * <a href="https://developers.google.com/closure/compiler/">Google Closure Compiler</a> configuration.
 */
public class ClosureConfig {

    private final LanguageMode languageIn;

    private final LanguageMode languageOut;

    private final CompilerOptions.Environment environment;

    private final CompilationLevel compilationLevel;

    private final DependencyOptions dependencyOptions;

    private final List<SourceFile> externs;

    private final Format sourceMapFormat;

    private final Map<DiagnosticGroup, CheckLevel> warningLevels;

    private final Boolean angularPass;

    private final List<String> extraAnnotations;

    /**
     * Init Closure Compiler values.
     *
     * @param languageIn        the version of ECMAScript used to report errors in the code
     * @param languageOut       the version of ECMAScript the code will be returned in
     * @param environment       the set of builtin externs to load
     * @param compilationLevel  the degree of compression and optimization to apply to JavaScript
     * @param dependencyOptions options for how to manage dependencies between input files
     * @param externs           preserve symbols that are defined outside of the code you are compiling
     * @param createSourceMap   create a source map for the minifed/combined production files
     * @param warningLevels     a map of warnings to enable or disable in the compiler
     * @param angularPass       use {@code @ngInject} annotation to generate Angular injections
     * @param extraAnnotations  make extra annotations known to the closure engine
     */
    public ClosureConfig(LanguageMode languageIn, LanguageMode languageOut, CompilerOptions.Environment environment,
                         CompilationLevel compilationLevel, DependencyOptions dependencyOptions,
                         List<SourceFile> externs, boolean createSourceMap,
                         Map<DiagnosticGroup, CheckLevel> warningLevels, boolean angularPass,
                         List<String> extraAnnotations) {
        this.languageIn = languageIn;
        this.languageOut = languageOut;
        this.environment = environment;
        this.compilationLevel = compilationLevel;
        this.dependencyOptions = dependencyOptions;
        this.externs = externs;
        this.sourceMapFormat = (createSourceMap) ? SourceMap.Format.V3 : null;
        this.warningLevels = warningLevels;
        this.angularPass = angularPass;
        this.extraAnnotations = extraAnnotations;
    }

    /**
     * Gets the languageIn.
     *
     * @return the languageIn
     */
    public LanguageMode getLanguageIn() {
        return languageIn;
    }

    /**
     * Gets the languageOut.
     *
     * @return the languageOut
     */
    public LanguageMode getLanguageOut() {
        return languageOut;
    }

    /**
     * Gets the environment.
     *
     * @return the environment
     */
    public CompilerOptions.Environment getEnvironment() {
        return environment;
    }

    /**
     * Gets the compilationLevel.
     *
     * @return the compilationLevel
     */
    public CompilationLevel getCompilationLevel() {
        return compilationLevel;
    }

    /**
     * Gets the dependencyOptions.
     *
     * @return the dependencyOptions
     */
    public DependencyOptions getDependencyOptions() {
        return dependencyOptions;
    }

    /**
     * Gets the externs.
     *
     * @return the externs
     */
    public List<SourceFile> getExterns() {
        return externs;
    }

    /**
     * Gets the sourceMapFormat.
     *
     * @return the sourceMapFormat
     */
    public Format getSourceMapFormat() {
        return sourceMapFormat;
    }

    /**
     * Gets the warningLevels.
     *
     * @return the warningLevels
     */
    public Map<DiagnosticGroup, CheckLevel> getWarningLevels() {
        return warningLevels;
    }

    /**
     * Gets the angularPass.
     *
     * @return the angularPass
     */
    public Boolean getAngularPass() {
        return angularPass;
    }

    /**
     * Gets the extraAnnotations.
     *
     * @return the extraAnnotations
     */
    public List<String> getExtraAnnotations() {
        return extraAnnotations;
    }
}
