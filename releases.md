# Release notes

**1.4.1**: Fixed bug where collection-level URNs in object property values would cause browsing to fail silently.

**1.4.0**: Added Objects Tab for viewing and querying CITE Collections. Added Images tab, taking advantage of the `CiteBinaryImage` datamodel in CEX, to show binary image data.

**1.3.10**: Updated dependencies.

**1.3.9**: Fixed bug that prevented previous-search menu from activating on the "Explore Texts" tab.

**1.3.8**: Improved UI for NGram threshold. Fixed bug where NGram URNs would get returned for every exemplar of an edition, and they should not have been.

**1.3.7**: Improved UI for Exploring. Using OHCO2 Library 10.4.2, which fixes bugs in labelling works in Catalogs, and in delivering first-node URNs for corpora.

**1.3.6**: Fixed bug that prevented NGrams from showing up. Also fixed a bug where URNs for NGrams were not being labelled correctly.

**1.3.5**: Fixed bug that caused erroneous information in the "Works in this Corpus" sidebar. Added sorting of works in listing so all versions and exemplars are together (as opposed to appearing in whatever order their `#!ctsdata` appears in the CEX).

**1.3.4**: Fixed bug that prevented searching for strings in a single work as opposed to the whole corpus.

**1.3.3**: Moved a few blocking UI actions to use `Future[T]` to improve perceived responsiveness.

**1.3.2**: Using updated version of the `ohco2` and `scm` libraries. Improvements to prev and next, and to NGram searching.

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
