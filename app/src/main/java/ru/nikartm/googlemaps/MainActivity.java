package ru.nikartm.googlemaps;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import ru.nikartm.googlemaps.constant.Constants;
import ru.nikartm.googlemaps.util.Util;
import ru.nikartm.googlemaps.util.UtilPlace;

import static ru.nikartm.googlemaps.constant.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;
import static ru.nikartm.googlemaps.constant.Constants.SKIPPED_PERMISSIONS_ACCESS_GRANTED;

public class MainActivity extends FragmentActivity  {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // OnClick go to map
    public void pickPlace(View view) {
        if (Util.checkPermission(this)) {
            if (Util.isNetworkAvailable(MainActivity.this)) {
                UtilPlace.pickOrFindPlace(this);
            } else {
                Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    UtilPlace.pickOrFindPlace(this);
                } else if (grantResults.length > 0
                        && grantResults[0] == SKIPPED_PERMISSIONS_ACCESS_GRANTED) {
                    // Do something if skipped dialog
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(MainActivity.this, data);

                Intent intent = new Intent(MainActivity.this, GoogleMapActivity.class);
                intent.putExtra(Constants.LATITUDE_TAG, place.getLatLng().latitude);
                intent.putExtra(Constants.LONGITUDE_TAG, place.getLatLng().longitude);
                intent.putExtra(Constants.NAME_TAG, place.getName());
                intent.putExtra(Constants.ADDRESS_TAG, place.getAddress());
                startActivity(intent);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(MainActivity.this, data);
                Log.e(TAG, "Result error: " + status.getStatusMessage());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "The user canceled the operation");
            }
        }
    }
}
