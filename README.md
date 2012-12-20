# Minify Maven Plugin

Minify Maven Plugin combines and minimizes JavaScript and CSS files using [YUI Compressor](http://developer.yahoo.com/yui/compressor/) for faster page loading.
Optionally the [Google Closure Compiler](https://developers.google.com/closure/compiler/) can be used to minimize JavaScript files. 

## Features

* **Reduce HTTP Requests**

> 80% of the end-user response time is spent on the front-end. Most of this time is tied up in downloading all the components in the page: images, stylesheets, scripts, Flash, etc. Reducing the number of components in turn reduces the number of HTTP requests required to render the page. This is the key to faster pages.

> Combined files are a way to reduce the number of HTTP requests by combining all scripts into a single script, and similarly combining all CSS into a single stylesheet. Combining files is more challenging when the scripts and stylesheets vary from page to page, but making this part of your release process improves response times.

* **Minify JavaScript and CSS**

> Minification is the practice of removing unnecessary characters from code to reduce its size thereby improving load times. When code is minified all comments are removed, as well as unneeded white space characters (space, newline, and tab). In the case of JavaScript, this improves response time performance because the size of the downloaded file is reduced. Three popular tools for minifying JavaScript code are [JSMin](http://crockford.com/javascript/jsmin), [Google Closure Compiler](https://developers.google.com/closure/compiler/) and [YUI Compressor](http://developer.yahoo.com/yui/compressor/). The YUI compressor can also minify CSS.

> Obfuscation is an alternative optimization that can be applied to source code. It's more complex than minification and thus more likely to generate bugs as a result of the obfuscation step itself. In a survey of ten top U.S. web sites, minification achieved a 21% size reduction versus 25% for obfuscation. Although obfuscation has a higher size reduction, minifying JavaScript is less risky.

## Usage & Information

Configure your project's `pom.xml` to run the plugin during the project's build cycle.
For more information, check the plugin [Maven site](http://samaxes.github.com/minify-maven-plugin/) or the [demo application](https://github.com/downloads/samaxes/minify-maven-plugin/minify-maven-plugin-demo-1.5.2-src.zip).

    <build>
        <plugins>
            <plugin>
                <groupId>com.samaxes.maven</groupId>
                <artifactId>minify-maven-plugin</artifactId>
                <version>1.5.2</version>
                <executions>
                    <execution>
                        <id>default-minify</id>
                        <phase>process-resources</phase>
                        <configuration>
                            <!-- Google Closure Compiler as an 
                                 optional alternative to the 
                                 default YUI Compressor -->
                            <jsEngine>clojure</jsEngine> 
                            <cssSourceDir>css</cssSourceDir>
                            <cssSourceFiles>
                                <cssSourceFile>file-1.css</cssSourceFile>
                                <!-- ... -->
                                <cssSourceFile>file-n.css</cssSourceFile>
                            </cssSourceFiles>
                            <cssFinalFile>style.css</cssFinalFile>
                            <jsSourceDir>js</jsSourceDir>
                            <jsSourceFiles>
                                <jsSourceFile>file-1.js</jsSourceFile>
                                <!-- ... -->
                                <jsSourceFile>file-n.js</jsSourceFile>
                            </jsSourceFiles>
                            <jsFinalFile>script.js</jsFinalFile>
                        </configuration>
                        <goals>
                            <goal>minify</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
