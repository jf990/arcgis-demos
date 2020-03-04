#!/bin/bash
# sh ./curltest.sh "Washington%20Convention%20Center%2C%20Washington%20DC"
##################################################
protocol="https://"
host="geocode.arcgis.com"
endpoint="/arcgis/rest/services/World/GeocodeServer/findAddressCandidates"
url="${protocol}${host}${endpoint}"
parameters="f=json&category=POI&outFields=*&SingleLine=${1}"
echo "curl ${url}?${parameters}"
curl "${url}?${parameters}"
