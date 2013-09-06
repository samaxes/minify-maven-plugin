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

/**
 * <a href="http://yui.github.io/yuicompressor/">YUI Compressor</a> configuration.
 */
public class YuiConfig {

    private final int linebreak;

    private final boolean munge;

    private final boolean preserveAllSemiColons;

    private final boolean disableOptimizations;

    /**
     * Init YuiConfig values.
     *
     * @param linebreak split long lines after a specific column
     * @param munge minify only
     * @param preserveAllSemiColons preserve unnecessary semicolons
     * @param disableOptimizations disable all the built-in micro optimizations
     */
    public YuiConfig(int linebreak, boolean munge, boolean preserveAllSemiColons, boolean disableOptimizations) {
        this.linebreak = linebreak;
        this.munge = munge;
        this.preserveAllSemiColons = preserveAllSemiColons;
        this.disableOptimizations = disableOptimizations;
    }

    /**
     * Gets the linebreak.
     *
     * @return the linebreak
     */
    public int getLinebreak() {
        return linebreak;
    }

    /**
     * Gets the munge.
     *
     * @return the munge
     */
    public boolean isMunge() {
        return munge;
    }

    /**
     * Gets the preserveAllSemiColons.
     *
     * @return the preserveAllSemiColons
     */
    public boolean isPreserveAllSemiColons() {
        return preserveAllSemiColons;
    }

    /**
     * Gets the disableOptimizations.
     *
     * @return the disableOptimizations
     */
    public boolean isDisableOptimizations() {
        return disableOptimizations;
    }
}
