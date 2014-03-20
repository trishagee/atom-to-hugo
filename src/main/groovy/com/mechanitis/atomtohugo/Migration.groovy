package com.mechanitis.atomtohugo

import groovy.xml.Namespace

import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.Files.createDirectory
import static java.nio.file.Files.exists
import static java.nio.file.Files.isDirectory
import static java.nio.file.Files.write

class Migration {
    final static String METADATA = '{\n' +
                                   ' "disqus_url" : "http://trishagee.github.io/post/%1$s/",\n' +
                                   ' "disqus_title" : "%2$s",\n' +
                                   ' "Title": "%2$s",\n' +
                                   ' "Pubdate": "%3$s",\n' +
                                   ' "Keywords": %4$s,\n' +
                                   ' "Tags": %4$s,\n' +
                                   ' "Slug": "%1$s",\n' +
                                   ' "Section": "post"\n' +
                                   '}'

    static void migrateToMarkdown(String atomFile, String outputDirectory) {
        Path outputDirectoryPath = Paths.get(outputDirectory)
        ensureDirectoryExists(outputDirectoryPath)

        def atom = new Namespace('http://www.w3.org/2005/Atom')
        def content = new XmlParser().parse(new File(atomFile))[atom.entry]
        content.each {
            def tags = []
            def entryType = determineTypeAndPopulateTags(it, tags)
            if (!ignoreEntry(entryType)) {
                def title = it.title.text()
                def publishedDate = it.published.text()[0..9]
                def filename = turnEntryTitleIntoFilenameWithNoSpecialCharacters(title)

                def outputFile
                if (entryIsDraft(it)) {
                    def draftsDirectory = outputDirectoryPath.resolve('drafts')
                    ensureDirectoryExists(draftsDirectory)
                    outputFile = draftsDirectory.resolve("${filename}.md")
                } else {
                    outputFile = outputDirectoryPath.resolve("${filename}.md")
                }
                write(outputFile, [String.format(METADATA, filename, title, publishedDate, tags), it.content.text()])
            }
        }
    }

    private static boolean ignoreEntry(String entryType) {
        entryType == 'comment' || entryType == 'settings' || entryType == 'template'
    }

    private static String determineTypeAndPopulateTags(entry, tags) {
        String type = null
        entry.category.'@term'.each {
            def entryTypeAttribute = 'http://schemas.google.com/blogger/2008/kind#'
            if (it.startsWith(entryTypeAttribute)) {
                type = it[entryTypeAttribute.length()..-1]
            } else {
                tags.push("\"${it}\"")
            }
        }
        type
    }

    private static boolean entryIsDraft(entry) {
        entry['app:control']['app:draft'].text() == 'yes'
    }

    private static void ensureDirectoryExists(Path outputPath) {
        if (exists(outputPath)) {
            assert isDirectory(outputPath)
        } else {
            createDirectory(outputPath)
        }
    }

    private static String turnEntryTitleIntoFilenameWithNoSpecialCharacters(title) {
        //yay me, regular expressions...
        ((title.toLowerCase() =~ '[^a-zA-Z0-9 ]').replaceAll('') =~ ' ').replaceAll('_')
    }

}
