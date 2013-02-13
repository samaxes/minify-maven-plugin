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

import java.io.File;
import java.util.Comparator;

/**
 * Custom filename comparator. Compares file name instead of file path.
 */
public class FilenameComparator implements Comparator<File> {

    /**
     * Compares two filenames lexicographically, ignoring case differences. This method returns an integer whose sign is
     * that of calling compareTo with normalized versions of the strings where case differences have been eliminated by
     * calling Character.toLowerCase(Character.toUpperCase(character)) on each character.
     *
     * @param o1 The first file object to be compared
     * @param o2 The second file object to be compared
     * @return Zero if both the files have the same name, a value less than zero if the first file is lexicographically
     *         less than the second, or a value greater than zero if the first file is lexicographically greater than
     *         the second
     */
    @Override
    public int compare(File o1, File o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
    }
}
