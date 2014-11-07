package net.medialabs.mapaplay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.medialabs.functions.Jarvis;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class Splash extends Activity {

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_splash);
	}

	@Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // Obtain the sharedPreference, default to true if not available
        boolean isSplashEnabled = sp.getBoolean("isSplashEnabled", true);
        
        if (isSplashEnabled) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent mainIntent = new Intent(Splash.this, Login.class);
                    Splash.this.startActivity(mainIntent);
                    Splash.this.finish();
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                }
            }, 1000);
        }
        else {
            // if the splash is not enabled, then finish the activity immediately and go to main.
            Intent mainIntent = new Intent(Splash.this, Login.class);
            Splash.this.startActivity(mainIntent);
            finish();
            overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        }
    }	

}
