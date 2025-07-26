package com.query.InitializerPackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.fragment.app.FragmentActivity;

public class ConnectivityReceiver extends BroadcastReceiver {

    private NoInternetDialogFragment dialogFragment;

    public Context context;
    public  IntentFilter filter;



    public void startInternetReceiver(Context ctx) {
        // Register the ConnectivityReceiver
        context=ctx;
        filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(this, filter);
    }
    public void endInternetReceiver() {
        // Unregister the ConnectivityReceiver
       context.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            // Internet connection is available
            dialogFragment = new NoInternetDialogFragment();
            dialogFragment.setCancelable(false);
            dialogFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "NoInternetDialogFragment");

        } else {
            // Internet connection is not available
            if (dialogFragment != null && dialogFragment.isVisible()) {
                dialogFragment.dismiss();
            }
        }
    }
}
