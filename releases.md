# Release notes

**1.3.1**: Using updated version of the `ohco2` and `scm` libraries. Adjustments to take advantage of citation- and corpus-algebra.

**1.3.0**: Using updated version of `.cex`. Added support for CITE Collections and Image Extensions.

- Collections, Images, and Texts are integrated.
- Refactoring toward a future microservice-based app.
- Querying on CITE Properties with the latest version of the `citeobj` library.

**1.2.1**: Improved import of `.cex` files, using the Scala SCM library and enforcing validity. Numerous bug-fixes in searching texts, finding ngrams, and displaying results. New functionality:

- A UI way to see multiple versions of a passage of text.
- New, more sensible navigation when viewing multiple versions of a text.
- Links to online help
- A left-to-right text that quotes a right-to-left language will display as generally expected.

**1.1.0**: Import of repositories in `.cex` format, combining text-data and catalog-data.  New functionality:

- String- and Token-searching
- Search history
- Improved UI
- Various internal refactorings and improvements

**1.0**: initial release.  Import of corpora in 2-column, `.tsv` format. Text browsing and querying for NGrams.
