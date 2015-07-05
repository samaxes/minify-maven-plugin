# Minify Maven Plugin

## 1.6.3
* Build should fail if compiler can't parse/compile source files
* Extends logging

## 1.6.2

* Cherry picked from commit 31fe5c91bf2d24c29251595206c3c4ebada1c712: Added `nosuffix` option to avoid the suffix `.min` on the minified output file name (#16).

## 1.6.1

* Cherry picked from commit 924a23a373e6b9aa841af6b9e4300c670eb602aa: Preserve sub-directory structure when only minifying (#29).

## 1.6

* Add support for [Google Closure Compiler](https://developers.google.com/closure/compiler/) for JavaScript compression (#14).

## 1.5.2

* New goal parameter to log full source file paths and new FAQ entry pointing to the plugin goal parameters (#5).
* Option to skip the minify step (#11).
* Option to skip the merge step (#13).

## 1.5.1

* Cannot process the same filename of files in different directories (#2).
* CSS minification fails for base64 encoded background images (#3).

## 1.5

* Fix charset issue (#1).
* Update Maven site skin.
* Use `ExecutorService` to wait for all tasks to finish.
* Add support for CLI-based configuration and Maven 2.2.1. From [Configuring Plugin Goals in Maven 3](http://www.sonatype.com/people/2011/03/configuring-plugin-goals-in-maven-3/):

  > For many plugin parameters it is occasionally convenient to specify their values from the command line via system properties. In the past, this was limited to parameters of simple types like `String` or `Boolean`. The latest Maven release finally allows plugin users to configure collections or arrays from the command line via comma-separated strings. Take for example a plugin parameter like this:
  >
  >     /** @parameter expression="${includes}" */
  >     String[] includes;
  >
  > This can be configured from the command line as follows:
  >
  >     mvn <goal> -Dincludes=Foo,Bar
  >
  > Plugin authors that wish to enable CLI-based configuration of arrays/collections just need to add the `expression` tag to their parameter annotation. Note that if compatibility with older Maven versions is to be kept, the parameter type must not be an interface but a concrete collection class or an array to avoid another shortcoming in the old configurator.

## 1.4

* Move from http://code.google.com/p/maven-samaxes-plugin/ to https://github.com/samaxes/minify-maven-plugin.
* Add Maven Integration for Eclipse (M2E) lifecycle mapping metadata.
* Rename project from Maven Minify Plugin to Minify Maven Plugin:

  > Artifact Ids of the format maven-___-plugin are reserved for  
  > plugins in the Group Id org.apache.maven.plugins  
  > Please change your artifactId to the format ___-maven-plugin  
  > In the future this error will break the build.

## 1.3.5

* Lift restriction that prevented the final filename to be the same as an existing source filename.

## 1.3.4

* Update YUI Compressor to version 2.4.6.

## 1.3.3

* Add debug messages for wrong source file names and source directory paths.

## 1.3.2

* Add `cssTargetDir`, `jsTargetDir`, `suffix`, and `charset` parameters.

## 1.3.1

* Class `java.util.List` cannot be instantiated while running Maven minify goal with versions previous to 3.0.

## 1.3

* Change exclude/include patterns from a comma separated `String` to `List<String>`. Also included a custom file comparator that only compares the file name instead of the full file path.
* Update [YUI Compressor](http://developer.yahoo.com/yui/compressor/) dependency to version 2.4.2.

## 1.2.1

* Don't crash with an `IndexOutOfBoundsException` when a source file does not exist.
* More accurate logging.
* Configure POM to inherit from Sonatype OSS Parent POM.

## 1.2

* Add exclude/include patterns, with the caveat that the developer must name their source files so their lexicographical order is correct for minifying.
* Don't minify a file type if the list of files to process is empty.
* Make JavaScript minify error messages clearer.
* Make file extensions configurable (e.g. it's now possible to save a JavaScript file as `*.jsp` or `*.php`).
* Compile against JDK 1.5 instead of JDK 1.6.
