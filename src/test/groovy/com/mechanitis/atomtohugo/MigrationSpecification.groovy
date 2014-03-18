package com.mechanitis.atomtohugo

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

class MigrationSpecification extends Specification {
    static final String TEST_DATA_PATH = 'src/test/resources/atom-test-data.xml'
    static final String OUTPUT_FILENAME = 'qcon_london_2014.md'
    static final String OUTPUT_DIRECTORY = 'src/test/resources/output'
    Migration migration

    def setup() {
        Files.list(Paths.get(OUTPUT_DIRECTORY)).each { Files.delete(it) }
        migration = new Migration()
    }

    def 'should turn content into markdown'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH)

        then:
        List<String> expectedOutput = Files.readAllLines(Paths.get("src/test/resources/${OUTPUT_FILENAME}"))
        List<String> actualOutput = Files.readAllLines(Paths.get("src/test/resources/output/${OUTPUT_FILENAME}"))

        actualOutput.size() == expectedOutput.size()
        actualOutput.eachWithIndex { Comparable<String> line, int i -> assert expectedOutput[i] == line }
    }

    def 'should add meta data to the start of the entry'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH)

        then:
        def filename = 'qcon_london_2014'
        def title = 'QCon London 2014'
        List<String> headerInfo = ['{',
                                   " \"disqus_url\" : \"http://trishagee.github.io/post/${filename}/\",",
                                   " \"disqus_title\" : \"${title}\",",
                                   " \"Title\": \"${title}\",",
                                   ' "Pubdate": "2014-03-11",',
                                   " \"Slug\": \"${filename}\",",
                                   ' "Section": "post"',
                                   '}']
        List<String> expectedOutput = Files.readAllLines(Paths.get('src/test/resources/output/qcon_london_2014.md'))
        headerInfo.eachWithIndex {
            Comparable<String> line, int i -> assert expectedOutput[i] == line
        }
    }

    def 'should create a file for each blog post'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH)

        then:
        def generatedFiles = Files.list(Paths.get(OUTPUT_DIRECTORY))
        generatedFiles.count() == 2
    }

    def 'should name the file after the tile but with underscores instead of spaces'() {
        when:
        migration.migrateToMarkdown(TEST_DATA_PATH)

        then:
        def generatedFiles = Files.list(Paths.get(OUTPUT_DIRECTORY))
        generatedFiles.any { it.fileName.toString() == OUTPUT_FILENAME }
    }


}
