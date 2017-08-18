package ru.nikartm.googlemaps.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

/**
 * @author Ivan Vodyasov on 16.08.2017.
 */

public class GeocodingTask extends AsyncTask<LatLng, Void, String> {

    private static final String TAG = GeocodingTask.class.getSimpleName();

    private Context context;
    private String result;
    private TextView tvResult;

    public GeocodingTask(Context context, String result){
        super();
        this.context = context;
        this.result = result;
    }

    public GeocodingTask(Context context, TextView result){
        super();
        this.context = context;
        this.tvResult = result;
    }

    @Override
    protected String doInBackground(LatLng... params) {
        String result = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(params[0].latitude, params[0].longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address returnedAddress = addresses.get(0);
                if (returnedAddress.getMaxAddressLineIndex() > 0) {
                    StringBuilder address = new StringBuilder();
                    for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                        address.append(returnedAddress.getAddressLine(i)).append(", ");
                    }
                    result = address.toString().substring(0, address.toString().length() - 2);
                } else {
                    result = String.format("%s, %s", returnedAddress.getLocality(), returnedAddress.getCountryName());
                }
                Log.w(TAG, "Current location : " + result);
            } else {
                Log.w(TAG, "No Address returned!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Get address error", e);
        }
        return result;
    }

    @Override
    protected void onPostExecute(String addressText) {
        result = addressText;
        tvResult.setText(addressText);
    }
}
