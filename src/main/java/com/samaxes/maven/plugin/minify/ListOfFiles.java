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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.maven.plugin.logging.Log;

/**
 * {@code ListOfFiles} is used to initialize the SequenceInputStream which uses {@code ListOfFiles} to get a new
 * InputStream for every file listed.
 */
public class ListOfFiles implements Enumeration<InputStream> {

    private Log log;

    private List<File> files;

    private int current = 0;

    /**
     * ListOfFiles public constructor.
     * 
     * @param log Maven plugin log
     * @param files list of files
     */
    public ListOfFiles(Log log, List<File> files) {
        this.log = log;
        this.files = files;
    }

    /**
     * Tests if this enumeration contains more elements.
     * 
     * @return <code>true</code> if and only if this enumeration object contains at least one more element to provide;
     *         <code>false</code> otherwise.
     */
    public boolean hasMoreElements() {
        return (current < files.size()) ? true : false;
    }

    /**
     * Returns the next element of this enumeration if this enumeration object has at least one more element to provide.
     * 
     * @return the next element of this enumeration.
     * @exception NoSuchElementException if no more elements exist.
     */
    public InputStream nextElement() {
        InputStream is = null;

        if (!hasMoreElements())
            throw new NoSuchElementException("No more files.");
        else {
            try {
                File nextElement = files.get(current);
                current++;
                is = new FileInputStream(nextElement);
            } catch (FileNotFoundException e) {
                log.error("The file [" + files.get(current).getName() + "] was not found.", e);
            }
        }

        return is;
    }

    /**
     * Returns the list of files contained in this object.
     * 
     * @return the list of files
     */
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("[");
        for (File file : files) {
            if (stringBuilder.length() > 1) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(file.getName());
        }

        return stringBuilder.append("]").toString();
    }

    /**
     * Returns the number of elements in this list.
     * 
     * @return the number of elements in this list
     */
    public int size() {
        return files.size();
    }
}
