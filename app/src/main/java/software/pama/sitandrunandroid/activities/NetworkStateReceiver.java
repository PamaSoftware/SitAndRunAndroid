package software.pama.sitandrunandroid.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.d("app", "Network connectivity change");
        if(intent.getExtras()!=null) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if(ni!=null && ni.getState()==NetworkInfo.State.CONNECTED) {
                Log.i("app","Network "+ni.getTypeName()+" connected");
            }
        }
        if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY,Boolean.FALSE)) {
            Log.d("app", "There's no network connectivity");
        }
    }

}