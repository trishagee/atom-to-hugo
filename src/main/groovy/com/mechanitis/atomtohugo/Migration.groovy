package com.mechanitis.atomtohugo

import groovy.xml.Namespace

class Migration {
    String metadata = '{\n' +
                      ' "disqus_url" : "http://trishagee.github.io/post/%1$s/",\n' +
                      ' "disqus_title" : "%2$s",\n' +
                      ' "Title": "%2$s",\n' +
                      ' "Pubdate": "%3$s",\n' +
                      ' "Slug": "%1$s",\n' +
                      ' "Section": "post"\n' +
                      '}\n'

    void migrateToMarkdown(String atomFile) {
        def file = new File(atomFile)
        def atom = new Namespace('http://www.w3.org/2005/Atom')
        def content = new XmlParser().parse(file)[atom.entry]
        content.each {
            def title = it.title.text()
            def publishedDate = it.published.text()[0..9]
            def filename = turnEntryTitleIntoFilenameWithNoSpecialCharacters(title)
            def output = new FileWriter("src/test/resources/output/${filename}.md")
            output.write(String.format(metadata, filename, title, publishedDate))
            output.write(it.content.text())
            output.flush()
        }
    }

    private static String turnEntryTitleIntoFilenameWithNoSpecialCharacters(title) {
        //yay me, regular expressions...
        ((title.toLowerCase() =~ '[^a-zA-Z0-9 ]').replaceAll('') =~ ' ').replaceAll('_')
    }
}
