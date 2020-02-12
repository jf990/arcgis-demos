/**
 * Esri ArcGIS Runtime demonstration with the Runtime Android SDK.
 *
 * + 2D map with custom basemap
 * + track device location
 * + place search
 * + graphics overlay
 * + operational layers from feature service
 *
 */
package com.esri.arcgisruntime.startermapapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.ArcGISVectorTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.IdentifyGraphicsOverlayResult;
import com.esri.arcgisruntime.mapping.view.LocationDisplay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedEvent;
import com.esri.arcgisruntime.mapping.view.ViewpointChangedListener;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeParameters;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;
import com.esri.arcgisruntime.util.ListenableList;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private LocationDisplay mLocationDisplay = null;
    private GraphicsOverlay mGraphicsOverlay;
    private LocatorTask mLocator = null;
    private Portal mPortalService = null;
    private String mPlaceCategory = "Museum";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapView);
        setupMap();
        trackLocationOn();
    }

    // Create a map and assign it to the map view
    private void setupMap() {

        // License the app with your Runtime Lite license key (set in app_settings.xml)
        ArcGISRuntimeEnvironment.setLicense(getResources().getString(R.string.arcgis_license_key));

        if (mMapView != null) {

            // Use a standard Esri basemap
//            Basemap basemap = Basemap.createNavigationVector();
//            ArcGISMap map = new ArcGISMap(basemap);

            mPortalService = new Portal(getResources().getString(R.string.arcgis_portal), false);

            // Connect to ArcGIS Online and get the custom basemap
            PortalItem portalItemLayer = new PortalItem(mPortalService, getResources().getString(R.string.custom_basemap));
            ArcGISVectorTiledLayer myCustomTileLayer = new ArcGISVectorTiledLayer(portalItemLayer);
            ArcGISMap map = new ArcGISMap(new Basemap(myCustomTileLayer));

            // set the initial map view. this is only a fall back when location tracking is off or not enabled.
            double latitude = 38.902970048906099;
            double longitude = -77.023439974464736;
            double scale = 10000;
            map.setInitialViewpoint(new Viewpoint(latitude, longitude, scale));
            mMapView.setMap(map);

            // add data layers to the map
            setupBikeTrailLayer(map);
            setupBreweryLayer(map);

            // when the map viewpoint changes, rerun the place search to find new places at the new map location
            mMapView.addViewpointChangedListener(new ViewpointChangedListener() {
                @Override
                public void viewpointChanged(ViewpointChangedEvent viewpointChangedEvent) {
                    if (mGraphicsOverlay == null) {
                        mGraphicsOverlay = new GraphicsOverlay();
                        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
                        setupPlaceTouchListener();
                        setupNavigationChangedListener();
                        mMapView.removeViewpointChangedListener(this);
                        findPlaces(mPlaceCategory);
                    }
                }
            });
        }
    }

    // Connect to the brewery layer on ArcGIS Online and add it to the operational layers
    private void setupBreweryLayer(final ArcGISMap map) {
        final PortalItem breweryLayerItem = new PortalItem(mPortalService, getResources().getString(R.string.brewery_layer));
        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(breweryLayerItem, 0);
        FeatureLayer breweryLayer = new FeatureLayer(serviceFeatureTable);
        map.getOperationalLayers().add(breweryLayer);
    }

    // Connect to the bke trail layer from ArcGIS HUB and add it to the operational layers
    private void setupBikeTrailLayer(final ArcGISMap map) {
        ServiceFeatureTable serviceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.bike_trail_layer));
        FeatureLayer featureLayer = new FeatureLayer(serviceFeatureTable);
        map.getOperationalLayers().add(featureLayer);
    }

    // When the map changes view either pan or zoom perform a new place search with the updated location
    private void setupNavigationChangedListener() {
        mMapView.addNavigationChangedListener(navigationChangedEvent -> {
            if (!navigationChangedEvent.isNavigating()) {
                mMapView.getCallout().dismiss();
                findPlaces(mPlaceCategory);
            }
        });
    }

    // Setup the ability to do place search
    private void setupLocatorService() {
        if (mLocator == null) {
            mLocator = new LocatorTask(getResources().getString(R.string.esri_geocoder_service));
        }
    }

    // Send a request to find places using the current map view location
    private void findPlaces(String placeCategory) {
        setupLocatorService();
        GeocodeParameters parameters = new GeocodeParameters();
        Point searchPoint;

        if (mMapView.getVisibleArea() != null) {
            searchPoint = mMapView.getVisibleArea().getExtent().getCenter();
            if (searchPoint == null) {
                return;
            }
        } else {
            return;
        }
        parameters.setPreferredSearchLocation(searchPoint);
        parameters.setMaxResults(25);

        List<String> outputAttributes = parameters.getResultAttributeNames();
        outputAttributes.add("Place_addr");
        outputAttributes.add("PlaceName");
        outputAttributes.add("URL");

        // Execute the search and add the places to the graphics overlay.
        final ListenableFuture<List<GeocodeResult>> results = mLocator.geocodeAsync(placeCategory, parameters);
        results.addDoneListener(() -> {
            try {
                ListenableList<Graphic> graphics = mGraphicsOverlay.getGraphics();
                graphics.clear();
                List<GeocodeResult> places = results.get();
                for (GeocodeResult result : places) {

                    // Add a graphic representing each location with a simple marker symbol.
                    SimpleMarkerSymbol placeSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 12);
                    placeSymbol.setOutline(new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLACK, 2));
                    Graphic graphic = new Graphic(result.getDisplayLocation(), placeSymbol);
                    java.util.Map<String, Object> attributes = result.getAttributes();

                    // Store the location attributes with the graphic for later recall when this location is identified.
                    for (String key : attributes.keySet()) {
                        String value = attributes.get(key).toString();
                        graphic.getAttributes().put(key, value);
                    }
                    graphics.add(graphic);
                }
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
            }
        });
    }

    // Show a callout of place attributes when a place on teh graphics layer is tapped.
    private void setupPlaceTouchListener() {
        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent motionEvent) {

                // Dismiss a prior callout.
                mMapView.getCallout().dismiss();

                // get the screen point where user tapped
                final android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));

                // identify graphics on the graphics overlay
                final ListenableFuture<IdentifyGraphicsOverlayResult> identifyGraphic = mMapView.identifyGraphicsOverlayAsync(mGraphicsOverlay, screenPoint, 10.0, false, 2);

                identifyGraphic.addDoneListener(() -> {
                    try {
                        IdentifyGraphicsOverlayResult graphicsResult = identifyGraphic.get();
                        // get the list of graphics returned by identify graphic overlay
                        List<Graphic> graphicList = graphicsResult.getGraphics();

                        // get the first graphic selected and show its attributes with a callout
                        if (!graphicList.isEmpty()){
                            showCalloutAtLocation(graphicList.get(0), mMapView.screenToLocation(screenPoint));
                        }
                    } catch (InterruptedException | ExecutionException exception) {
                        exception.printStackTrace();
                    }
                });
                return super.onSingleTapConfirmed(motionEvent);
            }
        });
    }

    private void showCalloutAtLocation(Graphic graphic, Point mapPoint) {
        Callout callout = mMapView.getCallout();
        TextView calloutContent = new TextView(getApplicationContext());
        String placeName = graphic.getAttributes().get("PlaceName").toString();
        String address = graphic.getAttributes().get("Place_addr").toString();
        String url = graphic.getAttributes().get("URL").toString();

        if (url.length() > 0) {
            placeName = "<a href=\"" + url + "\">" +placeName + " </a>";
        }
        callout.setLocation(graphic.computeCalloutLocation(mapPoint, mMapView));
        calloutContent.setTextColor(Color.BLACK);
        calloutContent.setText(Html.fromHtml("<b>" + placeName + "</b><br>" + address));
        callout.setContent(calloutContent);
        callout.show();
    }

    // Setup the ability to track the device location
    private void setupLocationDisplay() {
        mLocationDisplay = mMapView.getLocationDisplay();
        mLocationDisplay.addDataSourceStatusChangedListener(dataSourceStatusChangedEvent -> {
            if (dataSourceStatusChangedEvent.isStarted() || dataSourceStatusChangedEvent.getError() == null) {
                return;
            }

            int requestPermissionsCode = 2;
            String[] requestPermissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

            if (!(ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[0]) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(MainActivity.this, requestPermissions[1]) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(MainActivity.this, requestPermissions, requestPermissionsCode);
            } else {
                String message = String.format("Error in DataSourceStatusChangedListener: %s",
                        dataSourceStatusChangedEvent.getSource().getLocationDataSource().getError().getMessage());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
        mLocationDisplay.setAutoPanMode(LocationDisplay.AutoPanMode.COMPASS_NAVIGATION);
    }

    // Turn on location display to track the device location on the map
    public void trackLocationOn() {
        if (mLocationDisplay == null) {
            setupLocationDisplay();
        }
        if (mLocationDisplay != null && ! mLocationDisplay.isStarted()) {
            mLocationDisplay.startAsync();
        }
    }

    // Turn off location display to conserve battery
    public void trackLocationOff() {
        if (mLocationDisplay != null && mLocationDisplay.isStarted()) {
            mLocationDisplay.stop();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationDisplay.startAsync();
        } else {
            Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        if (mMapView != null) {
            mMapView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        if (mMapView != null) {
            mMapView.dispose();
        }
        super.onDestroy();
    }
}
