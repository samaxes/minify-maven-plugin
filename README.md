# Minify Maven Plugin

Minify Maven Plugin combines and minimizes JavaScript and CSS files using [YUI Compressor](http://developer.yahoo.com/yui/compressor/) for faster page loading. Optionally the [Google Closure Compiler](https://developers.google.com/closure/compiler/) can be used to minimize JavaScript files.

## Benefits

### Reduce HTTP Requests

> 80% of the end-user response time is spent on the front-end. Most of this time is tied up in downloading all the components in the page: images, stylesheets, scripts, etc. Reducing the number of components in turn reduces the number of HTTP requests required to render the page. This is the key to faster pages.

> Combined files are a way to reduce the number of HTTP requests by combining all scripts into a single script, and similarly combining all CSS into a single stylesheet. Combining files is more challenging when the scripts and stylesheets vary from page to page, but making this part of your release process improves response times.

### Compress JavaScript and CSS

> Minification/compression is the practice of removing unnecessary characters from code to reduce its size thereby improving load times. A JavaScript compressor, in addition to removing comments and white-spaces, obfuscates local variables using the smallest possible variable name. This improves response time performance because the size of the downloaded file is reduced. Some popular tools for minifying JavaScript code are [UglifyJS](http://lisperator.net/uglifyjs/), [Closure Compiler](https://developers.google.com/closure/compiler/) and [YUI Compressor](http://developer.yahoo.com/yui/compressor/). The YUI Compressor is also able to safely compress CSS files.

## Usage & Information

Configure your project's `pom.xml` to run the plugin during the project's build cycle.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.samaxes.maven</groupId>
            <artifactId>minify-maven-plugin</artifactId>
            <version>1.7</version>
            <executions>
                <execution>
                    <id>default-minify</id>
                    <phase>process-resources</phase>
                    <configuration>
                        <charset>utf-8</charset>
                        <!-- Google Closure Compiler as an 
                             optional alternative to the 
                             default YUI Compressor -->
                        <jsEngine>closure</jsEngine>
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
```

For more information, check the plugin [documentation](http://samaxes.github.com/minify-maven-plugin/) and the [demo application](https://github.com/downloads/samaxes/minify-maven-plugin/minify-maven-plugin-demo-1.7-src.zip).  
**Note:** For version 1.7 or greater of Minify Maven Plugin, Java SE 7 is required. If you need to support older versions of Java please use the version 1.6 of this plugin.

## License

This distribution is licensed under the terms of the Apache License, Version 2.0 (see LICENSE.txt).
