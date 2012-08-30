spray-nozzle
===============

Enhancements to http://spray.cc for various boilerplate things.

Warning: Very experimental right now

## Features ##

* Singed and encrypted cookie or secure token wrapper
* Header checking spray routes
* Authentication and CSRF prevention spray routes (coming soon)

### Versions ###

Works with Spray 1.0-M2. Will be updated to move along the 1.0 tree as releases are made.

## Using ##

Include the following repository to you Ivy/Maven/SBT file:

    http://repo.theatr.us


Include the following dependency in your `build.sbt`:

    "com.stackfoundry" % "spray-nozzle" % "0.1-SNAPSHOT"
