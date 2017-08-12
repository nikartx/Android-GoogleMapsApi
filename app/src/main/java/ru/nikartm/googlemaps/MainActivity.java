package ru.nikartm.googlemaps;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;

import ru.nikartm.googlemaps.fragment.PlacePickerFragment;

public class MainActivity extends FragmentActivity  {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 7;
    private static final int SKIPPED_PERMISSIONS_ACCESS_GRANTED = -1;

    private FragmentManager fManager;
    private FragmentTransaction fTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fManager = getSupportFragmentManager();
    }

    public void pickPlace(View view) {
        if (isPermissionGranted()) {
            initPickerFragment();
        }
    }

    private void initPickerFragment() {
        fTransaction = fManager.beginTransaction().replace(R.id.select_container, new PlacePickerFragment());
        fTransaction.commit();
    }

    private boolean isPermissionGranted() {
        boolean isGranted = false;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        return isGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initPickerFragment();
                } else if (grantResults.length > 0 && grantResults[0] == SKIPPED_PERMISSIONS_ACCESS_GRANTED) {
                    // Do something if skipped dialog
                }
                break;
            default:
                break;
        }
    }
}
