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
package com.samaxes.maven.minify.demo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter used to test SourceMap HTTP header.
 */
@WebFilter(filterName = "SourceMapFilter", urlPatterns = { "/js/script.min.js" })
public class SourceMapFilter implements Filter {

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    /**
     * <p>
     * Set SourceMap header.
     * </p>
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setHeader("SourceMap", "script.min.js.map");

        filterChain.doFilter(servletRequest, httpServletResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
    }
}
