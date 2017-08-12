package ru.nikartm.googlemaps;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.nikartm.googlemaps.constant.Constants;
import ru.nikartm.googlemaps.util.UtilPlace;

/**
 * Before starting maps need add location permission granted
 *
 * @author Ivan Vodyasov on 09.08.2017.
 * */
public class GoogleMapActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    public static final String TAG = GoogleMapActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 7;
    private static final int SKIPPED_PERMISSIONS_ACCESS_GRANTED = -1;
    private static final int DEFAULT_ZOOM = 17;
    private static final int REQUEST_CODE_START_POINT = 5;
    private static final int REQUEST_CODE_END_POINT = 6;

    private TextView tvStartPoint;
    private TextView tvEndPoint;

    private GoogleMap map;
    private View mapView;

    private double latitude;
    private double longitude;
    private String title;
    private String address;

    private Location lastLocation;
    private LatLng startPoint;
    private LatLng endPoint;

    private boolean isLocationPermissionGranted;
    private boolean isSkippedLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        // Set the back arrow for toolbar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        tvStartPoint = (TextView) findViewById(R.id.tv_start);
        tvEndPoint = (TextView) findViewById(R.id.tv_end);
        initClickByChoosePoint();

        Intent intent = getIntent();
        latitude = intent.getDoubleExtra(Constants.LATITUDE_TAG, 0d);
        longitude = intent.getDoubleExtra(Constants.LONGITUDE_TAG, 0d);
        title = intent.getStringExtra(Constants.NAME_TAG);
        address = intent.getStringExtra(Constants.ADDRESS_TAG);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapView = mapFragment.getView();
        mapFragment.getMapAsync(this);
        setGoogleMapButtonPosition();
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
    }

    private GoogleApiClient createGoogleApiClient() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        googleApiClient.connect();
        return googleApiClient;
    }

    private void setGoogleMapButtonPosition() {
        View locationButton = ((View) mapView
                .findViewById(Integer.parseInt("1")).getParent())
                .findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        rlp.setMargins(0, 350, 200, 0);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng latlng = new LatLng(latitude, longitude);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, DEFAULT_ZOOM));
        map.addMarker(new MarkerOptions()
                .title(title)
                .snippet(address)
                .position(latlng));

        updateLocationUI();
        setPickerPosition(getDeviceLocation(), latlng);
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

    private void updateLocationUI() {
        if (map != null) {
            if (!isSkippedLocationPermissionGranted && checkPermission()) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastLocation = null;
            }
        }
    }

    @Nullable
    private LatLng getDeviceLocation() {
        LatLng currentLocation = null;
        if (checkPermission()) {
            LocationManager locationManager = (LocationManager)
                    getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            lastLocation = locationManager.getLastKnownLocation(locationManager
                    .getBestProvider(criteria, false));
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();
            currentLocation = new LatLng(latitude, longitude);
        }
        return currentLocation;
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        return isLocationPermissionGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        isLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isLocationPermissionGranted = true;
                } else if (grantResults.length > 0 && grantResults[0] == SKIPPED_PERMISSIONS_ACCESS_GRANTED) {
                    isSkippedLocationPermissionGranted = true;
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
            Log.e(TAG, "Place: "
                    + place.getAddress()
                    + place.getPhoneNumber()
                    + place.getLatLng().latitude);

        } else if (requestCode == REQUEST_CODE_END_POINT && resultCode == RESULT_OK) {
            Place place = PlaceAutocomplete.getPlace(this, data);
            endPoint = place.getLatLng();
            tvEndPoint.setText(place.getAddress());
            Log.e(TAG, "Place: "
                    + place.getAddress()
                    + place.getPhoneNumber()
                    + place.getLatLng().latitude
                    + " Start = " + startPoint + " End = " + endPoint);

        } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
            Status status = PlaceAutocomplete.getStatus(this, data);
            Log.e(TAG, "Result error: " + status.getStatusMessage());
        } else if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "The user canceled the operation");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

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
