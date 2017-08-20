package ru.nikartm.googlemaps.util;

import android.app.Activity;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

/**
 * @author Ivan Vodyasov on 18.08.2017.
 */

public class LocationHelper extends Service implements LocationListener {

    public static final String TAG = LocationHelper.class.getSimpleName();

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_FOR_UPDATES = 1000 * 30;

    private final Activity activity;

    private LocationManager locationManager;
    private boolean isLocationAvailable = false;

    private Location location;
    private double latitude;
    private double longitude;

    public LocationHelper(Activity activity) {
        this.activity = activity;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

            // GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(GPS_PROVIDER);
            // Network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(activity, "Disabled network providers", Toast.LENGTH_SHORT).show();
            } else {
                isLocationAvailable = true;
                if (isGPSEnabled) {
                    setGPSLocation();
                } else {
                    setNetworkLocation();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    private void setGPSLocation() {
        if (Util.checkPermission(activity)) {
            if (location == null) {
                locationManager.requestLocationUpdates(
                        GPS_PROVIDER,
                        MIN_TIME_FOR_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                location = locationManager.getLastKnownLocation(GPS_PROVIDER);
                if (locationManager != null && location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d(TAG, "GPS provider enabled");
                }
            }
        }
    }

    private void setNetworkLocation() {
        if (Util.checkPermission(activity)) {
            if (location == null) {
                locationManager.requestLocationUpdates(
                        NETWORK_PROVIDER,
                        MIN_TIME_FOR_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                location = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
                if (locationManager != null && location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Log.d(TAG, "Network provider enabled");
                }
            }
        }
    }

    public double getLatitude(){
        return location != null ? location.getLatitude() : latitude;
    }

    public double getLongitude(){
        return location != null ? location.getLongitude() : longitude;
    }

    public boolean isLocationAvailable() {
        return isLocationAvailable;
    }

    /**
     * Function to show GPS settings alert dialog
     */
    public void showSettingsDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("GPS is disabled");
        alertDialog.setMessage("Do you want to go to settings menu and enable GPS on your device?");

        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void stopHelper(){
        if(locationManager != null){
            locationManager.removeUpdates(LocationHelper.this);
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
