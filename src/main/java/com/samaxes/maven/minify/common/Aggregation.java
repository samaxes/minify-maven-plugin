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

import java.util.Collections;
import java.util.List;

/**
 * Maps a single bundle defined in {@link AggregationConfiguration}.
 */
public class Aggregation {

    /**
     * Defines the aggregation type.
     */
    public enum AggregationType {
        css, js;
    }

    private AggregationType type;

    private String name;

    private List<String> files = Collections.emptyList();

    /**
     * Gets the type.
     *
     * @return the type
     */
    public AggregationType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type the type to set
     */
    public void setType(AggregationType type) {
        this.type = type;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the files.
     *
     * @return the files
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * Sets the files.
     *
     * @param files the files to set
     */
    public void setFiles(List<String> files) {
        this.files = files;
    }
}
