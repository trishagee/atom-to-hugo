package com.mechanitis.atomtohugo

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.Files.delete
import static java.nio.file.Files.exists
import static java.nio.file.Files.list

class MigrationSpecification extends Specification {
    static final String TEST_DATA_PATH = 'src/test/resources/atom-test-data.xml'
    static final String OUTPUT_DIRECTORY = 'src/test/resources/output'
    static final String OUTPUT_FILENAME = 'qcon_london_2014.md'
    Migration migration

    def setup() {
        removeDirectoryAndContents(Paths.get(OUTPUT_DIRECTORY))
        migration = new Migration()
    }

    def 'should turn content into markdown'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH, OUTPUT_DIRECTORY)

        then:
        List<String> expectedOutput = Files.readAllLines(Paths.get("src/test/resources/${OUTPUT_FILENAME}"))
        List<String> actualOutput = Files.readAllLines(Paths.get("${OUTPUT_DIRECTORY}/${OUTPUT_FILENAME}"))

        actualOutput.size() == expectedOutput.size()
        actualOutput.eachWithIndex { Comparable<String> line, int i -> assert expectedOutput[i] == line }
    }

    def 'should add meta data to the start of the entry'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH, OUTPUT_DIRECTORY)

        then:
        def filename = 'qcon_london_2014'
        def title = 'QCon London 2014'
        List<String> headerInfo = ['{',
                                   " \"disqus_url\" : \"http://trishagee.github.io/post/${filename}/\",",
                                   " \"disqus_title\" : \"${title}\",",
                                   " \"Title\": \"${title}\",",
                                   ' "Pubdate": "2014-03-11",',
                                   ' "Keywords": ["qcon london", "conferences"],',
                                   ' "Tags": ["qcon london", "conferences"],',
                                   " \"Slug\": \"${filename}\",",
                                   ' "Section": "post"',
                                   '}']
        List<String> expectedOutput = Files.readAllLines(Paths.get(OUTPUT_DIRECTORY, OUTPUT_FILENAME))
        headerInfo.eachWithIndex {
            Comparable<String> line, int i -> assert expectedOutput[i] == line
        }
    }

    def 'should create a file for each blog post ignoring comment, template and settings entries'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH, OUTPUT_DIRECTORY)

        then:
        def numberOfFilesGenerated = 0
        Paths.get(OUTPUT_DIRECTORY).toFile().eachFileMatch(~/.*.md/) { numberOfFilesGenerated++ }
        numberOfFilesGenerated == 1
    }

    def 'should put draft entries into a drafts folder'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH, OUTPUT_DIRECTORY)

        then:
        def generatedFiles = list(Paths.get(OUTPUT_DIRECTORY, 'drafts'))
        generatedFiles.count() == 1
    }

    def 'should name the file after the tile but with underscores instead of spaces'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH, OUTPUT_DIRECTORY)

        then:
        def generatedFiles = list(Paths.get(OUTPUT_DIRECTORY))
        generatedFiles.any { it.fileName.toString() == OUTPUT_FILENAME }
    }

    def 'should create output directory if it does not exist'() {
        given:
        def outputDirectoryThatDidNotExist = 'src/test/resources/tmp'

        def outputPath = Paths.get(outputDirectoryThatDidNotExist)
        removeDirectoryAndContents(outputPath)

        when:
        migration.migrateToMarkdown(TEST_DATA_PATH, outputDirectoryThatDidNotExist)

        then:
        Files.isDirectory(Paths.get(outputDirectoryThatDidNotExist))

        cleanup:
        removeDirectoryAndContents(outputPath)
    }

    private static void removeDirectoryAndContents(Path outputPath) {
        if (exists(outputPath)) {
            list(outputPath).each {
                if (Files.isDirectory(it)) {
                    removeDirectoryAndContents(it)
                }
                delete(it)
            }
        }
    }
}
