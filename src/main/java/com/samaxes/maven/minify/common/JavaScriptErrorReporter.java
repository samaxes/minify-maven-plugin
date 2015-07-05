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
package com.samaxes.maven.minify.common;

import org.apache.maven.plugin.logging.Log;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * Reports any error occurring during JavaScript files compression.
 */
public class JavaScriptErrorReporter implements ErrorReporter {

    private Log log;

    private String filename;

    private String lastError;

    /**
     * Error reporter constructor.
     *
     * @param log Maven plugin log
     * @param filename JavaScript source filename
     */
    public JavaScriptErrorReporter(Log log, String filename) {
        this.log = log;
        this.filename = filename;
    }

    /**
     * Reports a warning.
     *
     * @param message a String describing the warning
     * @param sourceName a String describing the JavaScript source where the warning occured; typically a filename or
     *        URL
     * @param line the line number associated with the warning
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     */
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        log.warn(prepareMessage(message, sourceName, line, lineSource, lineOffset));
    }

    /**
     * Reports an error. If execution has not yet begun, the JavaScript engine is free to find additional errors rather
     * than terminating the translation. However, it will not execute a script that had errors.
     *
     * @param message a String describing the warning
     * @param sourceName a String describing the JavaScript source where the warning occured; typically a filename or
     *        URL
     * @param line the line number associated with the warning
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     */
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        final String errorMessage = prepareMessage(message, sourceName, line, lineSource, lineOffset);
        lastError = errorMessage;
        log.error(errorMessage);
    }

    /**
     * Creates an EvaluatorException that may be thrown. runtimeErrors, unlike errors, will always terminate the current
     * script.
     *
     * @param message a String describing the warning
     * @param sourceName a String describing the JavaScript source where the warning occured; typically a filename or
     *        URL
     * @param line the line number associated with the warning
     * @param lineSource the text of the line (may be null)
     * @param lineOffset the offset into lineSource where problem was detected
     */
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource,
            int lineOffset) {

        final String errorMessage = prepareMessage(message, sourceName, line, lineSource, lineOffset);
        log.error(errorMessage);
        final String exceptionMessage = lastError != null ? message + " " + lastError : errorMessage;
        return new EvaluatorException(exceptionMessage);
    }

    private String prepareMessage(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
        if (lineSource == null) {
            return "[" + filename + "] " + message;
        } else {
            return "[" + filename + ":" + line + ":" + lineOffset + "] " + message;
        }
    }
}
