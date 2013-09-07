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

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

/**
 * <a href="https://developers.google.com/closure/compiler/">Google Closure Compiler</a> configuration.
 */
public class ClosureConfig {

    private final LanguageMode language;

    private final CompilationLevel compilationLevel;

    /**
     * Init Closure Compiler values.
     *
     * @param language the version of ECMAScript used to report errors in the code
     * @param compilationLevel the degree of compression and optimization to apply to JavaScript
     */
    public ClosureConfig(LanguageMode language, CompilationLevel compilationLevel) {
        this.language = language;
        this.compilationLevel = compilationLevel;
    }

    /**
     * Gets the language.
     *
     * @return the language
     */
    public LanguageMode getLanguage() {
        return language;
    }

    /**
     * Gets the compilationLevel.
     *
     * @return the compilationLevel
     */
    public CompilationLevel getCompilationLevel() {
        return compilationLevel;
    }
}
