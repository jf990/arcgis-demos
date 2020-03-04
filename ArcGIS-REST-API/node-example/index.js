/**
 * ArcGIS REST JS node.js demo.
 * There are 4 demos here, designed to demonstrate some of the features of the ArcGIS REST JS library.
 * 1. geocodeFromCommandLine(string) to convert a string to locations using the World Geocoder Service.
 * 2. findPlaces(placeType, location) to locate places of a given type lear a location using the World Geocoder Service.
 * 3. applicationSession demonstrates how to set up application authentication in order to use a service that requires a token.
 * 4. getDirections(stops) demonstrates how to get directions between multiple locations using the routing service.
 */

require('dotenv').config();
require("isomorphic-form-data");
const fetch = require("cross-fetch");
const { setDefaultRequestOptions } = require("@esri/arcgis-rest-request");
const { geocode } = require("@esri/arcgis-rest-geocoding");
const { solveRoute } = require("@esri/arcgis-rest-routing");

// When using ArcGIS REST JS in a node application there needs to be a fetch polyfill.
// (This is not required if running in a web browser.)
setDefaultRequestOptions({ fetch });

/**
 * Geocode what was provided.
 * Logs the result to the console.
 * https://esri.github.io/arcgis-rest-js/api/geocoding/geocode/
 * 
 * @param {string} singleLine A string of test to search for with the geocoder.
 * @returns doesn't return anything, but logs the result to the console.
 */
function geocodeFromCommandLine(singleLine) {
  if (!singleLine) {
    console.log("Nothing to search for - done!");
  } else {
    geocode({
      singleLine: singleLine,
      category: "POI",
      outFields: "PlaceName,Place_addr,Phone,URL"
    })
    .then(response => {
      if (response.candidates.length > 0) {
        console.log(JSON.stringify(response.candidates));
      } else {
        console.log("Nothing found for " + singleLine);
      }
    }, error => {
      console.log("Geocoding error " + error.toString());
    });
  }
}

/**
 * Find places of a given category at or near a given location.
 * Logs the result to the console.
 * 
 * @param {string} placeType Category of places to search for.
 * @param {object} location {x,y} key value to define the x and y coordinates around where to perform the search.
 */
function findPlaces(placeType, location) {
  const maxLocations = 10;
  if (placeType == null || placeType == "") {
    placeType = "Landmark,Historical Monument,Museum";
  }
  if (location == null) {
    location = {
      x: -77.023439974464736,
      y: 38.902970048906099
    }
  }
  geocode({
    singleLine: "",
    outFields: "PlaceName,Place_addr,Phone,URL,Type",
    params: {
      category: placeType,
      maxLocations: maxLocations,
      location: `${location.x},${location.y}`
    }
  })
  .then(response => {
    const places = response.candidates;
    if (places.length > 0) {
      for (let i = 0; i < places.length; i ++) {
        let place = places[i].attributes;
        if (place.PlaceName) {
          console.log(`${place.PlaceName} (${place.Type})\n${place.Place_addr}\n${place.Phone} ${place.URL}\n\n`);
        }
      }
    } else {
      console.log(`Nothing found near ${JSON.stringify(location)} matching ${placeType}`);
    }
  }, error => {
    console.log("Find places error " + error.toString());
  });
}

/**
 * Setup application authentication from registered application.
 * Put your app's ClientID and ClientSecret in a .env file.
 * https://esri.github.io/arcgis-rest-js/api/auth/ApplicationSession/
 * https://developers.arcgis.com/applications
 */
const { ApplicationSession } = require("@esri/arcgis-rest-auth");
const applicationSession = new ApplicationSession({
  clientId: process.env.CLIENTID,
  clientSecret: process.env.CLIENTSECRET
});

/**
 * Get directions between two points.
 * Routing requires an authentication token.
 * https://esri.github.io/arcgis-rest-js/api/routing/solveRoute/
 * Logs the result to the console.
 * 
 * @param {array} stops is an array of point arrays representing each stop. Each entry is [x, y].
 */
function getDirections(stops) {
  if (stops == null || stops.length < 2) {
    stops = [
      [-77.023439974464736, 38.902970048906099],
      [-77.036430054965564, 38.897929948352669]
    ];
  }
  solveRoute({
    stops: stops,
    authentication: applicationSession
  })
  .then(response => {
    const summary = response.directions[0].summary;
    const features = response.directions[0].features;
    console.log(JSON.stringify(summary));
    console.log(features);
    console.log(features.length + " direction steps");
    for (let i = 0; i < features.length; i ++) {
      let step = features[i];
      if (step.attributes && step.attributes.text) {
        console.log(features[i].attributes.text);
      }
    }
  }, error => {
    console.log("Routing error " + error.toString());
  });
};

// Perform a geocode if given something to search for on the command line.
const commandLineParameter = process.argv.length > 2 ? process.argv[2] : "";
if (commandLineParameter != "") {
  geocodeFromCommandLine(commandLineParameter);
}

// Get directions with the default start and end stops.
getDirections(null);

// Find places with the default search.
// findPlaces("", null);
