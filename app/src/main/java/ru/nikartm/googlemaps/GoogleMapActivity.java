package ru.nikartm.googlemaps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;

import ru.nikartm.googlemaps.constant.Constants;
import ru.nikartm.googlemaps.util.GeocodingTask;
import ru.nikartm.googlemaps.util.Util;
import ru.nikartm.googlemaps.util.UtilPlace;

import static android.location.Criteria.ACCURACY_FINE;
import static ru.nikartm.googlemaps.constant.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static ru.nikartm.googlemaps.constant.Constants.SKIPPED_PERMISSIONS_ACCESS_GRANTED;

/**
 * Before starting maps need add location permission granted
 * @author Ivan Vodyasov on 09.08.2017.
 */
public class GoogleMapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final String TAG = GoogleMapActivity.class.getSimpleName();

    private static final int DEFAULT_ZOOM = 17;
    private static final int REQUEST_CODE_START_POINT = 5;
    private static final int REQUEST_CODE_END_POINT = 6;
    private String MAPS_API_KEY;

    private TextView tvStartPoint;
    private TextView tvEndPoint;
    private TextView tvDistance;
    private ImageView ivStartPicker;
    private Button btnDurationWalk;
    private Button btnDurationCar;
    private FloatingActionButton fabBuildRoute;

    private GoogleMap map;
    private GoogleApiClient googleApiClient;
    private IconGenerator iconFactory;
    private View mapView;
    private Direction walkDirection;
    private Direction carDirection;

    private double latitude;
    private double longitude;
    private String title;
    private String address;

    private Location lastLocation;
    private LatLng startPoint;
    private LatLng endPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        MAPS_API_KEY = getResources().getString(R.string.google_maps_api_key);

        // Set the back arrow for toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        iconFactory = new IconGenerator(this);
        tvStartPoint = (TextView) findViewById(R.id.tv_start);
        tvEndPoint = (TextView) findViewById(R.id.tv_end);
        ivStartPicker = (ImageView) findViewById(R.id.iv_start_picker);
        btnDurationWalk = (Button) findViewById(R.id.btn_duration_walk);
        btnDurationCar = (Button) findViewById(R.id.btn_duration_car);
        tvDistance = (TextView) findViewById(R.id.tv_distance);
        fabBuildRoute = (FloatingActionButton) findViewById(R.id.fab_build_route);

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(Constants.LATITUDE_TAG, 0d);
        longitude = intent.getDoubleExtra(Constants.LONGITUDE_TAG, 0d);
        title = intent.getStringExtra(Constants.NAME_TAG);
        address = intent.getStringExtra(Constants.ADDRESS_TAG);

        buildGoogleApiClient();
        initClickByChoosePoint();
        initDurationInfoBtn();
        setMapsFragment();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng latlng = new LatLng(latitude, longitude);
        setPickerPosition(startPoint, latlng);
        updateLocationUI();

        // Load and build walk route by default
        loadWalkRoute();
        loadCarRoute(false);
    }

    private void setMapsFragment() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        setGoogleMapButtonPosition();
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        googleApiClient.connect();
    }

    private void setGoogleMapButtonPosition() {
        View locationButton = ((View) mapView
                .findViewById(Integer.parseInt("1")).getParent())
                .findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 480, 0, 0);
    }

    private void setPickerPosition(LatLng start, LatLng end) {
        String startAddress = "";
        if (start != null) {
            startAddress = UtilPlace.getAddressByLatLng(getApplicationContext(), start);
        }
        tvStartPoint.setText(startAddress);
        tvEndPoint.setText(address);
        startPoint = start;
        endPoint = end;
    }

    private void initClickByChoosePoint() {
        tvStartPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilPlace.searchPlace(GoogleMapActivity.this, REQUEST_CODE_START_POINT);
            }
        });
        tvEndPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UtilPlace.searchPlace(GoogleMapActivity.this, REQUEST_CODE_END_POINT);
            }
        });
        ivStartPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPoint = getDeviceLocation();
                tvStartPoint.setText(UtilPlace
                        .getAddressByLatLng(getApplicationContext(), startPoint));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(startPoint, DEFAULT_ZOOM));
            }
        });
    }

    private void initDurationInfoBtn() {
        btnDurationWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.setWhiteBtnColor(GoogleMapActivity.this, btnDurationWalk, btnDurationCar);
                if (walkDirection != null) {
                    buildRoute(walkDirection);
                } else {
                    loadWalkRoute();
                }
            }
        });
        btnDurationCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.setWhiteBtnColor(GoogleMapActivity.this, btnDurationCar, btnDurationWalk);
                if (carDirection != null) {
                    buildRoute(carDirection);
                } else {
                    loadCarRoute(true);
                }
            }
        });
        fabBuildRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRoute();
            }
        });
    }

    private void loadWalkRoute() {
        GoogleDirection.withServerKey(MAPS_API_KEY)
                .from(startPoint)
                .to(endPoint)
                .unit(Unit.METRIC)
                .optimizeWaypoints(true)
                .transportMode(TransportMode.WALKING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            walkDirection = direction;
                            buildRoute(direction);
                            btnDurationWalk.setText(direction
                                    .getRouteList().get(0)
                                    .getLegList().get(0)
                                    .getDuration().getText());
                            Util.setWhiteBtnColor(GoogleMapActivity.this, btnDurationWalk, btnDurationCar);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.e(TAG, "Direction failed execute", t);
                    }
                });
    }

    private void loadCarRoute(final boolean isReload) {
        GoogleDirection.withServerKey(MAPS_API_KEY)
                .from(startPoint)
                .to(endPoint)
                .unit(Unit.METRIC)
                .optimizeWaypoints(true)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            carDirection = direction;
                            btnDurationCar.setText(direction
                                    .getRouteList().get(0)
                                    .getLegList().get(0)
                                    .getDuration().getText());
                            if (isReload) {
                                buildRoute(direction);
                            }
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Log.e(TAG, "Direction failed execute", t);
                    }
                });
    }

    private void buildRoute(Direction direction) {
        map.clear();
        Route route = direction.getRouteList().get(0);
        Leg leg = route.getLegList().get(0);
        ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
        PolylineOptions polylineOptions = DirectionConverter
                .createPolyline(GoogleMapActivity.this, directionPositionList, 10, Color.RED);
        polylineOptions.geodesic(true);
        map.addPolyline(polylineOptions);

        tvDistance.setText(leg.getDistance().getText());

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : directionPositionList) {
            builder.include(latLng);
        }
        final LatLngBounds bounds = builder.build();
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 200);
        map.animateCamera(update);

        new GeocodingTask(getApplicationContext(), tvStartPoint).execute(startPoint);

        // Set markers on route
        addIcon(iconFactory, leg.getDistance().getText(), directionPositionList.get(directionPositionList.size()/2));
        map.addMarker(new MarkerOptions().position(startPoint).title(leg.getStartAddress()));
        map.addMarker(new MarkerOptions().position(endPoint).title(leg.getEndAddress()));
    }

    private void updateRoute(){
        if (tvStartPoint.getText().toString().isEmpty() || startPoint == null) {
            Toast.makeText(this, "Please choose a start point", Toast.LENGTH_SHORT).show();
        } else if (tvEndPoint.getText().toString().isEmpty() || endPoint == null) {
            Toast.makeText(this, "Please choose a destination", Toast.LENGTH_SHORT).show();
        } else {
            loadWalkRoute();
            loadCarRoute(false);
        }
    }

    private void updateLocationUI() {
        if (map != null) {
            if (Util.checkPermission(this)) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastLocation = null;
            }
        }
    }

    private void addIcon(IconGenerator iconFactory, CharSequence text, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(text)))
                .position(position)
                .anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        map.addMarker(markerOptions);
    }

    @Nullable
    private LatLng getDeviceLocation() {
        LatLng currentLocation = null;
        String bestProvider;
        if (Util.checkPermission(this)) {
            try {
                LocationManager locationManager = (LocationManager)
                        getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(ACCURACY_FINE);
                lastLocation = locationManager.getLastKnownLocation(locationManager
                        .getBestProvider(criteria, true));
                if (lastLocation == null) {
                    bestProvider = LocationManager.NETWORK_PROVIDER;
                    lastLocation = locationManager.getLastKnownLocation(bestProvider);
                }
                double latitude = lastLocation.getLatitude();
                double longitude = lastLocation.getLongitude();
                currentLocation = new LatLng(latitude, longitude);
            } catch (Exception e) {
                Log.e(TAG, "Unknown device location", e);
            }
        }
        return currentLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Do something
                } else if (grantResults.length > 0
                        && grantResults[0] == SKIPPED_PERMISSIONS_ACCESS_GRANTED) {
                    // Do something
                }
                break;
        }
        updateLocationUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_START_POINT && resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            startPoint = place.getLatLng();
            tvStartPoint.setText(place.getAddress());
            Log.d(TAG, "Place origin: "
                    + place.getAddress()
                    + " " + place.getLatLng().latitude);

        } else if (requestCode == REQUEST_CODE_END_POINT && resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            endPoint = place.getLatLng();
            tvEndPoint.setText(place.getAddress());
            Log.d(TAG, "Place destination: "
                    + place.getAddress()
                    + " " + place.getLatLng().latitude);

        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(this, data);
            Log.e(TAG, "Result error: " + status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "The user canceled the operation");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "GoogleApiClient connection failed : " + connectionResult.getErrorMessage());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Do something
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Do something
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                int count = getSupportFragmentManager().getBackStackEntryCount();
                if (count == 0) {
                    finish();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
