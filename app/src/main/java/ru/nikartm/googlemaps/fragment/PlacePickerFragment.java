package ru.nikartm.googlemaps.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import ru.nikartm.googlemaps.GoogleMapActivity;
import ru.nikartm.googlemaps.R;
import ru.nikartm.googlemaps.constant.Constants;
import ru.nikartm.googlemaps.util.UtilPlace;


public class PlacePickerFragment extends Fragment {

    public static final String TAG = PlacePickerFragment.class.getSimpleName();

    public PlacePickerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_place_picker, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        UtilPlace.pickOrFindPlace(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                Log.e(TAG, "Place: "
                        + place.getAddress()
                        + place.getPhoneNumber()
                        + place.getLatLng().latitude);

                Intent intent = new Intent(getActivity(), GoogleMapActivity.class);
                intent.putExtra(Constants.LATITUDE_TAG, place.getLatLng().latitude);
                intent.putExtra(Constants.LONGITUDE_TAG, place.getLatLng().longitude);
                intent.putExtra(Constants.NAME_TAG, place.getName());
                intent.putExtra(Constants.ADDRESS_TAG, place.getAddress());
                startActivity(intent);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Log.e(TAG, "Result error: " + status.getStatusMessage());
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "The user canceled the operation");
            }
        }
    }
}
