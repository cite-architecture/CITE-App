

![CiteApp 1.9.4](https://img.shields.io/badge/CiteApp-1.9.4-blue.svg) ![Scala 2.12.3](https://img.shields.io/badge/scala-2.12.3-brightgreen.svg) ![ScalaJS 0.6.22](https://img.shields.io/badge/scala%20js-0.6.22-brightgreen.svg) ![xCite 3.3.0](https://img.shields.io/badge/xcite-3.3.0-green.svg) ![OHCO2 10.8.0](https://img.shields.io/badge/ohco2-10.8.0-green.svg) ![SCM 6.0.0](https://img.shields.io/badge/scm-6.0.0-green.svg) ![CiteObj 7.0.1](https://img.shields.io/badge/citeobj-7.0.1-green.svg) ![CiteRelations 2.0.4](https://img.shields.io/badge/citerelations-2.0.4-green.svg) ![CiteBinaryImage 1.1.2](https://img.shields.io/badge/citebinaryimage-1.1.2-green.svg) ![Harvard CHS, Cite Architecture](https://img.shields.io/badge/harvard%20chs-cite--architecture-A51C30.svg) ![Furman Classics](https://img.shields.io/badge/furman-classics-582C83.svg) ![Holy Cross Classics](https://img.shields.io/badge/holy%20cross-classics-602d89.svg)

# CITE Application

A single-page browser application for exploring citable resources, written in [Scala.js](http://www.scala-js.org/).

## Current version: 1.9.7

See [release notes](releases.md).

## License

[GPL 3.0](https://opensource.org/licenses/gpl-3.0.html)

CITE Application, by default, downloads a sample corpus of texts. Licensing and attribution for those texts is available in the [downloads directory](downloads).

## Using

CITE Application is a single web page you can open in a browser. Precompiled versions are available in the [downloads directory](downloads), and named `cite-VERSION.html`.

**N.b.** In the `downloads` directory there is a `js` directory that CiteApp uses for providing zooming views of binary images, using [OpenSeadragon](https://openseadragon.github.io). 

**Images not working?** You probably need to deal with browser restrictions. See the [Wiki page on this topic](https://github.com/cite-architecture/CITE-App/wiki/Local-File-and-Cross-Domain-Restrictions).

## Building

CITE Application is targetted for Scala 2.12.You can use normal `sbt` using normal tasks for [ScalaJS projects](https://www.scala-js.org/doc/project/building.html) such as  `sbt fastOptJS` or `sbt fullOptJS`.

If `sbt fastOptJS` completes successfully, run the app by opening `CITE-App/target/scala-2.12/classes/index-dev.html` in your browser.

If `sbt fullOptJS` completes successfully, run the app by opening `CITE-App/target/scala-2.12/classes/index-opt.html` in your browser.

In addition, the project defines a custom `spa` task that builds a single-page application named `cite-VERSION.html` (where `VERSION` is the current version defined in `build.sbt`), in the `downloads` directory.

If `sbt fastOptJS` completes successfully, run the app by opening `CITE-App/target/scala-2.12/classes/index-dev.html` in your browser.

If `sbt fullOptJS` completes successfully, run the app by opening `CITE-App/target/scala-2.12/classes/index-opt.html` in your browser.

## Images for CiteApp

Documentation for configuring collections of images for CiteApp is underway at <https://github.com/cite-architecture/CITE-image-configurations>.

The `downloads/image_archive` directory contains a one-image sample setup showing how to implement URN-aware DeepZoom images.

## Python SimpleServer

Running CiteApp locally, and attempting to access images, can run afoul of the Cross-Origin-Restrictions (CORS). It may be possible to tell your browser to ignore that security precaution. Or, a safer alternative, is to serve CiteApp locally. If Python is installed, navigate to the directory that contains `cite-VERSION.html`, and execute:

`python -m SimpleHTTPServer`


## Credits

CITEApplication, © 2017, 2018: Neel Smith and Christopher Blackwell. Available for use, modification, and distribution under the terms of the [GPL 3.0](https://opensource.org/licenses/gpl-3.0.html) license. Based on the [CITE and CTS protocols](http://cite-architecture.github.io), by Neel Smith and Christopher Blackwell.
