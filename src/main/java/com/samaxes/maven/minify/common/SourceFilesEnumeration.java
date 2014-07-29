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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.maven.plugin.logging.Log;

/**
 * Used to initialize a {@code SequenceInputStream} with a {@code Enumeration<? extends InputStream>}. The input streams
 * that are produced by the enumeration will be read, in order, to provide the bytes to be read from the
 * {@code SequenceInputStream}.
 */
public class SourceFilesEnumeration implements Enumeration<InputStream> {

    private List<File> files;

    private int current = 0;

    /**
     * Enumeration public constructor.
     *
     * @param log Maven plugin log
     * @param files list of files
     * @param verbose show source file paths in log output
     */
    public SourceFilesEnumeration(Log log, List<File> files, boolean verbose) {
        this.files = files;

        for (File file : files) {
            log.info("Processing source file [" + ((verbose) ? file.getPath() : file.getName()) + "].");
        }
    }

    /**
     * Tests if this enumeration contains more elements.
     *
     * @return {@code true} if and only if this enumeration object contains at least one more element to provide;
     *         {@code false} otherwise.
     */
    @Override
    public boolean hasMoreElements() {
        return (current < files.size()) ? true : false;
    }

    /**
     * Returns the next element of this enumeration if this enumeration object has at least one more element to provide.
     *
     * @return the next element of this enumeration.
     * @exception NoSuchElementException if no more elements exist.
     */
    @Override
    public InputStream nextElement() {
        InputStream is = null;

        if (!hasMoreElements()) {
            throw new NoSuchElementException("No more files!");
        } else {
            File nextElement = files.get(current);
            current++;

            try {
                is = new FileInputStream(nextElement);
            } catch (FileNotFoundException e) {
                throw new NoSuchElementException("The path [" + nextElement.getPath() + "] cannot be found.");
            }
        }

        return is;
    }
}
