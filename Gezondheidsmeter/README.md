# Medmij Beeld Beschikbaar Stellen [![License: EUPL-1.2](https://img.shields.io/badge/License-EUPL--1.2-blue.svg)](./LICENSE)

## Introduction

This project contains Java and JavaScript code for implementing the Beeld Beschikbaar Stellen (beeldbeschikbaarheid) dataservice for Dutch Medmij program. Initial implementations are made by Curavista on behalf of De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport. The dataservice Beeld Beschikbaar stellen aims to have hospitals share their radiographic imagery data with PGOs and having PGOs visualize this data. The initial code only pertains to the PGO side:

* Retrieve data from the DVA
* Visualize data

## About the software

The software consists of a backend part in Java and a frontend part in JavaScript

### Requirements

#### Java

* Maven for building
* Java 8 or higher

#### JavaScript

We only used basic JavaScript, there are no noteworthy requirements

### Feature Overview

#### Timeline

The code can be used to generate a basic html structure to display the MHD:ITI-67 request as a timeline and JavaScript to handle the filtering, such that all display requirements can be met. To use this feature you need to:

* Implement the `TimelineEntry` interface
* Implement the `Translations` interface
* Create a new `Timeline`
* Generate the HTML using `TimelineTemplate.generateTimelineHtml`
* Add `window.MedmijTimeline.installFeatures()` in JavaScript in your html page

Note that the generated HTML doesn't contain html or body tags. This allows you to place it in any other HTML. Beware though that filtering does not work properly when you try to add multiple timelines on a page, because the ids are fixed and the filtering looks through all timeline entries on the page.

Also note that CSS code for styling is not included. This has been agreed upon with Medmij so different parties can maintain a competitive position. The structure does contain the necessary styling classes to easily apply styling. In addition to those styling classes that are immediately visible in the generated HTML, the JavaScript also sets `visible` / `hidden` for elements under the element with class `timeline-data` that need to be shown / hidden. Since one of the requirements is to show everything when first opening the timeline, a missing visibility class should be interpreted as `visible`

#### DICOM Viewer

The code can be used to generate a basic HTML structure to display the contents of a DICOM KOS file obtained from MHD:ITI-68 requests and display rendered DICOM data obtained from WADO-RS requests, such that all display requirements can be met. To use this feature you need to:

* Extend `WadoRsDownloader`, adding a implementation for function `doRequest`.
* Implement the `Translations` interface
* Implement an API endpoint where rendered DICOM data can be requested
* Optionally add a new implementation for the `DicomKosRepository` interface (you can look at
  `DicomKosRepositoryFileImpl` for a sample implementation)
* Download a DICOM KOS file using the MHD:ITI-68 request
* Create a `Kos` object from the DICOM KOS file using `DicomKosRepository.fetch`
* Generate the viewer HTML using `ViewerTemplate.generateViewerHtml`. `backendImagesBaseUrl` is used as API endpoint and can be anything you desire, however, the JavaScript code expects that the query string has already started, so please make sure it ends with `?` if you don't add any query parameters yourself.
* Add `window.MedmijDicomViewer.initViewer([viewerEl], [viewerId])` in JavaScript in your HTML page, where `[viewerEl]` is the HTML element containing the viewer structure (the element with class `medmij-dicom-viewer`) and `[viewerId]` can be set as desired. `[viewerId]` is used only for the supporting functions `getViewer` and `removeViewer`

The API endpoint should use the `doDownload`, `doDownloadFrame` and `doDownloadDicom` from `WadoRsDownloader` to retrieve the (rendered) data of images. For traceability, you should make sure that the provided X-Correlation-ID matches the one used to obtain the bearer token. Url parameters send to the API endpoint by the viewer:

* `instanceId`: id of the instance; matches to the id in the DICOM KOS file; always present
* `frameNumber`: 0 to N where N is the amount of frames that this image consists of; only present for multiframe images
* `_download=true`: indicates that the user wants to download the image; only present if the user clicked download; if present the response should contain the content disposition header `Content-Disposition: attachment`
* `dicom=true`: indicates that the user wants to download a dicom image; only present if the user clicked download dicom;
* `_filename`: name including file extension that the download receives; only present if the user clicked download dicom; if present the response should contain the content disposition header `Content-Disposition: attachment; filename="[filename]"`
* `_filenameWithoutExt`: name excluding file extension that the download receives; only present if the user clicked download; if present the response should contain the content disposition header `Content-Disposition: attachment; filename="[filename].[extension]"` where [extension] corresponds to the content type received from the WADO-RS request
* `_overrideContentType=application/octet-stream`: only present if the user clicked download; if present the response should contain the content type header `Content-Type: application/octet-stream`; without this some browsers may display the image rather than downloading it

Implementation for the MHD:ITI-68 request is not included, but this is basically the same as the request for documents in the PDF/a dataservice. Things to note with this request for the beelden dataservice:

* The type of file might be listed as `application/dicom` in the MHD:ITI-67 request, in which case you can still request `application/dicom+json` to get the DICOM KOS file in JSON format.
* When requesting a pdf file you might get an error saying that there is no valid file type available. We recommend sending the accept header `application/pdf;q=0.9, text/xml;q=0.1` to resolve this problem

Note that the generated HTML doesn't contain HTML or body tags. This allows you to place it in any other HTML. Beware though that the viewer does not work properly when you try to add multiple viewers on a page, because the ids are fixed. Even if this is fixed, it is not recommended to have multiple viewers on one page, due to memory usage

Also note that CSS code for styling is not included. This has been agreed upon with Medmij so different parties can maintain a competitive position. The structure does contain the necessary styling classes to easily apply styling. In addition to those styling classes that are immediately visible in the generated HTML, the JavaScript also sets `hidden` to nodes that should be hidden and removes the same class again when they need to be visible again.

### Third party dependencies

#### Java

Dependencies management is handled with Apache Maven

* [FasterXML 2.x](https://github.com/FasterXML/jackson) for parsing DICOM KOS and metadata files

##### Testing only

* [junit](https://github.com/junit-team/junit4) for unit testing

#### JavaScript

We used only plain JavaScript, no dependencies

### Project structure

The top level Java folder contains backend code to parse and/or forward responses obtained from the MHD:ITI-68 and WADO-RS requests. This also contains code to generate the HTML required for the frontend to run properly. The top level folder JavaScript contains the JavaScript code, which displays the DICOM KOS and works in tandem with your backend to do API request to obtain rendered DICOM data.

### Setup

#### Java

To include the Java code in your own project you can use the maven install command from the top level Java directory. This will build it as a library and install it on your system to make it available in the build process. If you use Apache Maven for your dependency management, you can then add the following dependency in your pom file to add it to your project:

```
<dependency>
	<groupId>nl.medmij.pgo</groupId>
	<artifactId>medmij-dicom-viewer</artifactId>
	<version>1.0.2</version>
</dependency>
```

If you use another product for your dependency management, you will need to do something equivalent to add the library to your project.

#### JavaScript

Make the JavaScript files available somewhere on your server and include them in your HTML using `<script src="[PATH-TO-JS]MedmijTimeline.js" charset="UTF-8"></script>` and `<script src="[PATH-TO-JS]MedmijDicomViewer.js" charset="UTF-8"></script>`.

## Design choices

This library consists of a part Java and a part JavaScript. The intention for the Java code was that it would fit all the needed functionality, but along the way we discovered that for certain functions (for instance networking code) it would be better to provide an interface that would need to be implemented by the user of the library. The current functionality would probably fit better in a fully JavaScript oriented library, but since that requires a lot of additional work with minimal gain for us, it hasn't been high on our priority list.

We chose for Java 8, since our core application is also written in Java 8.

We are using a library for reading JSON, because there are several good JSON libraries. We picked the fasterxml 2.x library, because we are familiar with this library. While we used the latest version to compile the Java code, it has been tested to work against a provided version 2.15.2.

## Support & Maintenance

This project is provided as open source and maintained on a best-effort basis. Curavista will remain involved until at least 2027, after which responsibility is signed over to De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport. Curavista will review issues and pull requests when possible, but we cannot guarantee response times or ongoing development. No guarantees are given for involvement of Curavista afterward.

## Contributing

We welcome contributions of all kinds! By contributing, you agree that your contributions will be licensed under the EUPL-1.2.

## Ideas for improvement

Migrate the functionality to a full JavaScript implementation. For instance a single JavaScript function `parseKos(url)`, that gets the KOS from the provided url, parses it, dynamically generates the HTML structure for it and finally applies the correct JavaScript functionality. We recommend keeping the functionality from `WadoRsDownloader.doRequest` in the backend, because otherwise you may run into problems with client certificates.

Separate JavaScript code into classes. For instance:

* DicomKos
* DicomMetadata
* Cache
* Downloader
* Timeline
* DicomViewer

Apply timeline functionality to the proper scope and make id's dynamic, to allow for multiple timelines on one page

This text is available under CC-BY-4.0