package com.mechanitis.atomtohugo

import groovy.xml.Namespace

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.Files.createDirectory
import static java.nio.file.Files.isDirectory

class Migration {
    String metadata = '{\n' +
                      ' "disqus_url" : "http://trishagee.github.io/post/%1$s/",\n' +
                      ' "disqus_title" : "%2$s",\n' +
                      ' "Title": "%2$s",\n' +
                      ' "Pubdate": "%3$s",\n' +
                      ' "Slug": "%1$s",\n' +
                      ' "Section": "post"\n' +
                      '}'

    void migrateToMarkdown(String atomFile, String outputDirectory) {
        Path outputDirectoryPath = ensureOutputDirectoryExists(outputDirectory)

        def file = new File(atomFile)
        def atom = new Namespace('http://www.w3.org/2005/Atom')
        def content = new XmlParser().parse(file)[atom.entry]
        content.each {
            if (!entryIsAComment(it)) {
                //as long as it's not a comment
                def title = it.title.text()
                def publishedDate = it.published.text()[0..9]
                def filename = turnEntryTitleIntoFilenameWithNoSpecialCharacters(title)

                def outputFile = outputDirectoryPath.resolve("${filename}.md")
                Files.write(outputFile, [String.format(metadata, filename, title, publishedDate), it.content.text()])
            }
        }
    }

    private boolean entryIsAComment(entry) {
        entry["thr:in-reply-to"].size() > 0
    }

    private Path ensureOutputDirectoryExists(String outputDirectory) {
        def outputDirectoryPath = Paths.get(outputDirectory)
        if (Files.exists(outputDirectoryPath)) {
            assert isDirectory(outputDirectoryPath)
        } else {
            createDirectory(outputDirectoryPath)
        }
        outputDirectoryPath
    }

    private static String turnEntryTitleIntoFilenameWithNoSpecialCharacters(title) {
        //yay me, regular expressions...
        ((title.toLowerCase() =~ '[^a-zA-Z0-9 ]').replaceAll('') =~ ' ').replaceAll('_')
    }
}
