@echo OFF
REM Windows batch script to use cURL to ArcGIS geocoder to find places near the given parameter
REM curltest this-place
SET protocol=https://
SET host=geocode.arcgis.com
SET endpoint=/arcgis/rest/services/World/GeocodeServer/findAddressCandidates
SET url=%protocol%%host%%endpoint%
SET parameters=f=json^&category=POI^&outField=*^&SingleLine=%1
echo "curl %url%?%parameters%"
curl "%url%?%parameters%"
