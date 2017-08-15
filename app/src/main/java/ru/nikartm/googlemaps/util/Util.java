package ru.nikartm.googlemaps.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import ru.nikartm.googlemaps.R;

import static ru.nikartm.googlemaps.constant.Constants.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

/**
 * @author Ivan Vodyasov on 15.08.2017.
 */

public class Util {

    /**
     * Switching color white/blue for duration buttons
     * @param ctx activity context
     * @param btnWhite button to change color to white
     * @param btnBlue button or array of buttons to change color to blue
     */
    public static void setWhiteBtnColor(Context ctx, Button btnWhite, @Nullable Button... btnBlue) {
        btnWhite.setBackground(ctx.getDrawable(R.drawable.btn_oval_white));
        btnWhite.setTextColor(ctx.getResources().getColor(R.color.colorBlue));
        btnWhite.getCompoundDrawables()[0].setTint(ctx.getResources().getColor(R.color.colorBlue));
        if(btnBlue != null && btnBlue.length > 0) {
            for (Button bBlue : btnBlue) {
                bBlue.setBackground(ctx.getDrawable(R.drawable.btn_oval_blue));
                bBlue.setTextColor(ctx.getResources().getColor(R.color.colorWhite));
                bBlue.getCompoundDrawables()[0].setTint(ctx.getResources().getColor(R.color.colorWhite));
            }
        }
    }

    /**
     * Checking network connection
     * @param context application context
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Checking permissions
     * @param activity activity context
     * @return boolean permission status, true - if permission granted
     */
    public static boolean checkPermission(Activity activity) {
        boolean isGranted = false;
        if (ContextCompat.checkSelfPermission(activity,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            isGranted = true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        return isGranted;
    }
}
