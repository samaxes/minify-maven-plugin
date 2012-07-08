# Minify Maven Plugin

## 1.5.1

* [MINIFY-2] Cannot process the same filename of files in different directories.

## 1.5

* [MINIFY-1] Fixed charset issue.
* Updated Maven site skin.
* Use `ExecutorService` to wait for all tasks to finish.
* Added support for CLI-based configuration and Maven 2.2.1. From [Configuring Plugin Goals in Maven 3](http://www.sonatype.com/people/2011/03/configuring-plugin-goals-in-maven-3/):

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

* Moved from http://code.google.com/p/maven-samaxes-plugin/ to https://github.com/samaxes/minify-maven-plugin.
* Renamed project from Maven Minify Plugin to Minify Maven Plugin.
* Added Maven Integration for Eclipse (M2E) lifecycle mapping metadata.

## 1.3.5

* Lifted restriction that prevented the final filename to be the same as an existing source filename.

## 1.3.4

* Updated YUI Compressor to version 2.4.6.

## 1.3.3

* Added debug messages for wrong source file names and source directory paths.

## 1.3.2

* Added cssTargetDir, jsTargetDir, suffix, and charset parameters.

## 1.3.1

* Class 'java.util.List' cannot be instantiated while running minify goal with Maven versions before 3.

## 1.3

* Exclude/include patterns changed from a comma separated String to List<String>. Also included a custom file comparator that only compares the file name instead of the full file path.
* Updated YUI Compressor dependency to version 2.4.2.

## 1.2.1

* Doesn't crash anymore with an IndexOutOfBoundsException when a source file does not exist.
* More accurate logging.
* Configure POM to inherit from Sonatype OSS Parent POM.

## 1.2

* Exclude/include patterns added, with the caveat that the developer must name their source files so their lexicographical order is correct for minifying.
* Don't minify a file type if the list of files to process is empty.
* Make JavaScript minify error messages clearer.
* Make file extensions configurable (e.g. it's now possible to save a JavaScript file as *.jsp or *.php).
* Compiled against JDK 1.5 instead of JDK 1.6.
