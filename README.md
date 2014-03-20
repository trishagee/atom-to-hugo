# Atom to Hugo

Very rough tool that takes an Atom XML feed like the one produced by exporting your Blogger blog,
and turns it into a series of markdown files.  This tool assumes the `content` tag is HTML, and will create markdown files that are HTML,
not pure markdown.  The markdown includes very basic meta data for use with [Hugo](http://github.com/spf13/hugo)

# To Use
You can call the functionality from a Groovy script or a Java class, using the following:

    com.mechanitis.atomtohugo.Migration.migrateToMarkdown(<path to input XML>, <output directory>)

For example:

    com.mechanitis.atomtohugo.Migration.migrateToMarkdown('resources/blog-03-14-2014.xml', 'resources/output')

This will create in the output directory a `*.md` file for every entry in the Atom XML file that is not a comment or settings.  In
addition, if any of the entries are drafts, these will be placed in a `drafts` subdirectory in the output folder.

The metadata will include:

 - a heading, based on the original entry's title
 - a disqus URL and title
 - the original tags from the entry will be used for both Keywords and Tags
 - a slug / path generated from the title (removing all special characters and replacing spaces with underscores).

See `MigrationSpecification` for more details.

# Known issues

 - Any script tags embedded in the original content will not be correctly migrated, as per [this issue]
 (https://github.com/spf13/hugo/issues/174).  You will need to fix or remove these manually.