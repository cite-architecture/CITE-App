# Release notes

**1.9.5**: Fixed bug in NGram URN reporting; sped it up.

**1.9.4**: Added "server mode" option, which removes the "local/remote" switch for images (using only "remote").

**1.9.3**: Resolved all deprecation warnings from Binding.scala; improved display of text-nodes.

**1.9.2**: Search history menu for relations tab.

**1.9.1**: Using CiteRelations 2.0.4.

**1.9.0**: Relations tab.

**1.8.1**: Replace list of cited works with dropdown menu, for O2 view and NGram view.

**1.8.0**: Implemented CiteRelations and the Commentary Datamodel for texts.

**1.7.3**: Using scala-js-dom 0.9.5.

**1.7.2**: Fixed a terrible bug where exemplar text nodes would show out of order.
Accepting CtsUrn or Cite2Urn request-parameters for auto-loading (and sharing views).

**1.7.1**: Even more rigorous checking for `Option[T]` throughout. Fixed a loophole where bad Cite2Urn values cased a `.get` operation on a `None` value when doing DSE records. Improved CSS for object views.

**1.7.0**: Rebuilt text-display code following more sound binding principles, giving more flexibility to expand text presentation with datamodels. Better integration of text-view with DSE models. More rigorous checking for `Option[T]` throughout.

**1.6.1**: Update OHCO2 library to 10.7.0, xCite library to 3.3.0; links to rull-rez image-downloads when viewing remote images.

**1.6.0**: Fully implemented CiteBinaryImageModel and DSEModel.

**1.5.4**: Added DSE Mappings to Text View. Fixed Issue #128: <https://github.com/cite-architecture/CITE-App/issues/133>. 

**1.5.3**: Fixed Issue #133: <https://github.com/cite-architecture/CITE-App/issues/133>. 

**1.5.2**: Fixed Issue #131: <https://github.com/cite-architecture/CITE-App/issues/131>. 

**1.5.1**: Fixed Issue #132: <https://github.com/cite-architecture/CITE-App/issues/132>. 

**1.5.0**: Implemented the DSE datamodel for data mapped to images.

**1.4.4**: Increased selection menu for NGram size up to 15. Now showing property labels (not property URNs) in object-view.

**1.4.3**: Added a working history menu for object browsing and querying.

**1.4.2**: Significant refactoring of text display. Improved display of citations for Exemplars.

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
