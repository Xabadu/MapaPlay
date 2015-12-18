package net.medialabs.mapaplay;

import net.medialabs.functions.Routing;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ComoLlegar extends FragmentActivity implements
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {

	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private GoogleMap map;
	private UiSettings mapSettings;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private LatLng NEW_POSITION;

	ImageButton comoLlegarBackBtn;
	boolean hide = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		if( (metrics.widthPixels == 240) ||
				(metrics.widthPixels == 320)	||
				(metrics.widthPixels == 480) ||
				(metrics.widthPixels == 720) ) {
			getActionBar().hide();
		} else {
			hide = true;
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		mLocationClient = new LocationClient(this, this, this);


	}

	public void loadMap() {
		setContentView(R.layout.activity_como_llegar);
		if(hide) {
			ImageView statusBar = (ImageView) findViewById(R.id.comoLlegarTopBarImage);
			ImageButton backBtn = (ImageButton) findViewById(R.id.comoLlegarBackBtn);
			statusBar.setVisibility(View.GONE);
			backBtn.setVisibility(View.GONE);
		}
		mCurrentLocation = mLocationClient.getLastLocation();
		if(mCurrentLocation != null) {
			LatLng CURRENT_POSITION = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
			if(map == null) {
				map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.comoLlegarMap)).getMap();
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				mapSettings = map.getUiSettings();
				mapSettings.setCompassEnabled(false);
				mapSettings.setZoomControlsEnabled(false);
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_POSITION, 16));
				map.addMarker(new MarkerOptions()
				.position(CURRENT_POSITION));

				Intent intent = getIntent();
				try {
					JSONObject panoramaInfo = new JSONObject(intent.getStringExtra("data"));
					JSONArray locationData = new JSONArray(panoramaInfo.getString("ubicacion"));
					if(locationData.length() == 1) {
						JSONObject geoPoints = locationData.getJSONObject(0);
						NEW_POSITION = new LatLng(geoPoints.getDouble("lat"), geoPoints.getDouble("lng"));
						map.addMarker(new MarkerOptions()
						.position(NEW_POSITION)
						.title(panoramaInfo.getString("nombre"))
						.snippet(panoramaInfo.getString("direccion")));
					} else if(locationData.length() > 1) {
						PolygonOptions rectOptions = new PolygonOptions();
						for(int j = 0; j < locationData.length(); j++) {
							JSONObject geoPoints = locationData.getJSONObject(j);
							NEW_POSITION = new LatLng(geoPoints.getDouble("lat"), geoPoints.getDouble("lng"));
							rectOptions.add(NEW_POSITION);
						}
						rectOptions.strokeColor(Color.RED);
						rectOptions.strokeWidth(3);
						rectOptions.fillColor(0x7FFF0000);
						map.addPolygon(rectOptions);
						map.addMarker(new MarkerOptions()
						.position(CURRENT_POSITION)
						.title(panoramaInfo.getString("nombre"))
						.snippet(panoramaInfo.getString("direccion")));
					}

					new Routing(ComoLlegar.this,map,
							Color.GREEN).execute(new LatLng(CURRENT_POSITION.latitude, CURRENT_POSITION.longitude),
							new LatLng(NEW_POSITION.latitude, NEW_POSITION.longitude));
				} catch (JSONException e) {
					e.printStackTrace();
				}


			}
		}

		comoLlegarBackBtn = (ImageButton) findViewById(R.id.comoLlegarBackBtn);

		comoLlegarBackBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ComoLlegar.this.finish();
			}
		});
	}

	@Override
    public void onBackPressed() {
		ComoLlegar.this.finish();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mapa_panoramas, menu);
		return true;
	}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
    			ComoLlegar.this.finish();
            	return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

	@Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Desconectado. Por favor reconectar.", Toast.LENGTH_SHORT).show();
		this.finish();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

		if (connectionResult.hasResolution()) {
		    try {
		        connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
		    } catch (IntentSender.SendIntentException e) {
		        e.printStackTrace();
		    }
		} else {
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		if(map == null) {
			mCurrentLocation = mLocationClient.getLastLocation();
			loadMap();
		}
	}


	private class GetRoute extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(ComoLlegar.this, "", "Cargando ruta...", true);
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://maps.googleapis.com/maps/api/directions/json?origin="
			+ params[0] +"," + params[1] + "&destination=" + params[2] + "," + params[3] +"&sensor=true&mode=walking");
            get.setHeader("content-type", "application/json");
            try {
            	HttpResponse resp = client.execute(get);
            	String respStr = EntityUtils.toString(resp.getEntity());
            	return respStr;
          	} catch(Exception e) {
            	Log.d("Error: ", e.toString());
            }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			JSONObject routeObject;
			try {
				routeObject = new JSONObject(result);
				JSONArray routes = routeObject.getJSONArray("routes");
				JSONObject routeInfo = routes.getJSONObject(0);
				JSONArray legs = routeInfo.getJSONArray("legs");
				PolylineOptions lineOptions = new PolylineOptions();
				for(int i = 0; i < legs.length(); i++) {
					JSONObject singleLeg = legs.getJSONObject(i);
					JSONArray steps = singleLeg.getJSONArray("steps");
					for(int j = 0; j < steps.length(); j++) {
						JSONObject singleStep = steps.getJSONObject(j);
						JSONObject startLocation = singleStep.getJSONObject("start_location");
						JSONObject endLocation = singleStep.getJSONObject("end_location");
						lineOptions.add(new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng")));
						lineOptions.add(new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng")));
						lineOptions.geodesic(true);
						map.addPolyline(lineOptions);
					}

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

			dialog.dismiss();
		}

	}

}
