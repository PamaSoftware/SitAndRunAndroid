package software.pama.sitandrunandroid.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.d("", "Network connectivity change");
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network = manager.getActiveNetworkInfo();
            if (network != null && network.isConnected()) {
                Log.i("", "Network " + network.getTypeName() + " connected");
            } else {
                Log.i("", "No network connection");
            }
    }

}