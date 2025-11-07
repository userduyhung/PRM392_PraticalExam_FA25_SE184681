package com.example.nguyenduyhung_se184681.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Utility class for checking network connectivity
 */
public class NetworkUtils {

    /**
     * Check if the device has an active internet connection
     * @param context Application context
     * @return true if connected to internet, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager =
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For Android M and above
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return false;
            }

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } else {
            // For older Android versions
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }
    }

    /**
     * Get a user-friendly message about network status
     * @param context Application context
     * @return Human-readable network status message
     */
    public static String getNetworkStatusMessage(Context context) {
        if (isNetworkAvailable(context)) {
            return "Connected to internet";
        } else {
            return "No internet connection";
        }
    }
}

