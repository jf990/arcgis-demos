# ArcGIS REST API

There are many ways to make REST requests and get responses back from REST servers. Here are just a few.

## Use the web browser

You can make a REST request if the method is GET. For example, here is a geocode you can run in a browser:

```html
https://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer/findAddressCandidates?f=json&SingleLine=Washington+Convention+Center%2C+Washington+DC&category=POI&outFields=*&forStorage=false
```

Note the parameters must all be made URL safe.

## Use command line

You can make a request from the command line using cURL. The `curltest.sh` file is an example shell script written for this purpose. To run this, enter a terminal (or command prompt) then enter:

`sh ./curltest.sh "Washington+Convention+Center%2C+Washington+DC"`

Replace the quoted string with any point of interest you wish to geocode. The string must be URL and CLI safe.

For more information about cURL, see [curl.haxx.se](https://curl.haxx.se/).

## Use HTML forms

You can make a request using an HTML form. See `postform.html` for an example. Edit the file with your text editor to change the parameters or how it works. Load it in your web browser and give it a try.

Because the `<form>` element supports the `method` attribute, you can use GET or POST.

## Use Postman

[Postman](https://www.getpostman.com/apps) is an app well suited for trying REST endpoints. It handles everything you need to make requests: URLs, parameters, methods, and organizing your requests into collections. Esri España created a handy repository with many ArcGIS REST endpoints organized into collections. See [ArcGIS REST API](https://github.com/esri-es/ArcGIS-REST-API) on github.com.

## Use JavaScript

You can write an app and have greater control over your REST requests and processing the responses. See `fetchapp.html` and `xmlhttprequest.html` as examples. Edit the file with your text editor to change the parameters or how it works. Load it in your web browser and give it a try. You must run this from a web server so that the origin protocol is `HTTP://` or `HTTPS://`, not `FILE://`, due to CORS restrictions it will not fetch from FILE:.

Since this is written with JavaScript, you have much better control over how it works and more importantly, how to process the response.

## Use node JS with ArcGIS REST JS

You can perform ArcGIS REST requests using [Node.js](https://nodejs.org) and the [ArcGIS REST JS](https://esri.github.io/arcgis-rest-js/) open source library. Review the content in the `node-example` folder. To run this project do the following:

1. Clone this repo to your local computer.
2. `cd` into the `ArcGIS-REST-API/node-example` folder.
3. Run `npm install`
4. Run `npm start`

Example the code in the `index.js` file to understand how the demo is set up and how to run the various code snippets.

## Resources

Links to many of the dependencies used in these demos:

* [ArcGIS for Developers](https://developers.arcgis.com)
* [ArcGIS REST API](https://developers.arcgis.com/rest/)
* [ArcGIS REST JS](https://esri.github.io/arcgis-rest-js/)
* [ArcGIS REST API Postman collections](https://github.com/esri-es/ArcGIS-REST-API)
* [Node.js](https://nodejs.org)
* [Postman](https://www.getpostman.com/apps)
* [curl.haxx.se](https://curl.haxx.se/)
* [jq](https://stedolan.github.io/jq/)
* [HTTP protocol](https://developer.mozilla.org/en-US/docs/Web/HTTP)
* [REST Architectural style dissertation](https://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm)
* [XMLHTTPRequest](https://developer.mozilla.org/en-US/docs/Web/API/XMLHttpRequest)
* [fetch](https://developer.mozilla.org/en-US/docs/Web/API/WindowOrWorkerGlobalScope/fetch)
