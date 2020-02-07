#!/bin/bash
# Geocode the requested parameter
##################################################
protocol="https://"
host="geocode.arcgis.com"
endpoint="/arcgis/rest/services/World/GeocodeServer/findAddressCandidates"
url="${protocol}${host}${endpoint}"
maxLocations=5
parameters="f=json&forStorage=false&category=POI&outFields=PlaceName,Place_addr,Phone,URL,Type&maxLocations=${maxLocations}&SingleLine=${1}"
echo "${url}?${parameters}"
curl "${url}?${parameters}" | jq ".candidates"
