# CITE Application

A single-page browser application implementing user-interaction with CITE/CTS libraries, written in [Scala.js](http://www.scala-js.org/).

## Current release: 1.0

See [release notes](releases.md).

Work on version 1.1 is available in the `v.1.1.0` branch.

## License

[GPL 3.0](https://opensource.org/licenses/gpl-3.0.html)



## Using

CITE Application is a single web page you can open in a browser.  A precompiled version is available in the [compiled-one-file-app directory](compiled-one-file-app).

## Building, testing

CITE Application is targetted for Scala 2.11.

Build with `sbt` using normal tasks for [ScalaJS projects](https://www.scala-js.org/doc/project/building.html):  `sbt fastOptJS` or `sbt fullOptJS`.  Output in `target/scala-2.11/classes/` includes the `.html` file you can open in a web browser.


## Credits

CITEApplication, by Neel Smith and Christopher Blackwell.
