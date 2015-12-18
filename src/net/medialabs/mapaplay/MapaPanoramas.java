package net.medialabs.mapaplay;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapaPanoramas extends FragmentActivity implements OnInfoWindowClickListener {

	private GoogleMap map;
	private UiSettings mapSettings;
	private LatLng CURRENT_POSITION = new LatLng(-33.431864,-70.61514);
	private LatLng NEW_POSITION = new LatLng(-33.432064, -70.61516);
	private ImageButton mapaBackBtn;
	private ImageButton mapaListBtn;
	JSONArray resultArray = null;
	Bitmap[] iconos;
	boolean hide = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
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
		ObtenerIconos obtenerIconos = new ObtenerIconos(this);
		obtenerIconos.execute(intent.getStringExtra("data"));

	}

	public void loadMap(Bitmap[] markerIcons) {
		setContentView(R.layout.activity_mapa_panoramas);
		if(hide) {
			ImageView statusBar = (ImageView) findViewById(R.id.mapaTopBarImage);
			ImageButton backBtn = (ImageButton) findViewById(R.id.mapaBackBtn);
			statusBar.setVisibility(View.GONE);
			backBtn.setVisibility(View.GONE);
		}
		if(map == null) {
			map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.panoramasMap)).getMap();
			map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			Intent intent = getIntent();

			try {
				resultArray = new JSONArray(intent.getStringExtra("data"));
				for(int i = 0; i < resultArray.length(); i++) {
					JSONObject panoramaInfo = resultArray.getJSONObject(i);
					JSONArray locationData = new JSONArray(panoramaInfo.getString("ubicacion"));
					if(locationData.length() == 1) {
						JSONObject geoPoints = locationData.getJSONObject(0);
						LatLng markerPosition = new LatLng(geoPoints.getDouble("lat"), geoPoints.getDouble("lng"));
						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.title(panoramaInfo.getString("nombre"));
						markerOptions.snippet(panoramaInfo.getString("direccion"));
						markerOptions.position(markerPosition);
						if(panoramaInfo.has("icono") && !panoramaInfo.isNull("icono")) {
							if(panoramaInfo.getJSONObject("icono").has("imagen") && !panoramaInfo.getJSONObject("icono").isNull("imagen") &&
									!panoramaInfo.getJSONObject("icono").getString("imagen").equals("")) {
								markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcons[0]));
							}
						}
						map.addMarker(markerOptions);
					} else if(locationData.length() > 1) {
						PolygonOptions rectOptions = new PolygonOptions();
						for(int j = 0; j < locationData.length(); j++) {
							JSONObject geoPoints = locationData.getJSONObject(j);
							LatLng markerOptions = new LatLng(geoPoints.getDouble("lat"), geoPoints.getDouble("lng"));
							rectOptions.add(markerOptions);
							CURRENT_POSITION = markerOptions;
						}
						rectOptions.strokeColor(Color.RED);
						rectOptions.strokeWidth(3);
						rectOptions.fillColor(0x7FFF0000);
						map.addPolygon(rectOptions);
						MarkerOptions markerOptions = new MarkerOptions();
						markerOptions.title(panoramaInfo.getString("nombre"));
						markerOptions.snippet(panoramaInfo.getString("direccion"));
						markerOptions.position(CURRENT_POSITION);
						if(panoramaInfo.has("icono") && !panoramaInfo.isNull("icono")) {
							if(panoramaInfo.getJSONObject("icono").has("imagen") && !panoramaInfo.getJSONObject("icono").isNull("imagen") &&
									!panoramaInfo.getJSONObject("icono").getString("imagen").equals("")) {
								markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcons[i]));
							}
						}
						map.addMarker(markerOptions);
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			mapSettings = map.getUiSettings();
			mapSettings.setCompassEnabled(false);
			mapSettings.setZoomControlsEnabled(false);
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_POSITION, 16));

		} else {
			Toast.makeText(this, "Noes", Toast.LENGTH_SHORT).show();
		}

		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				String markerId = marker.getId();
				String[] numberMarker = markerId.split("m");
				JSONObject finalInfo = null;
				try {
					finalInfo = resultArray.getJSONObject(Integer.valueOf(numberMarker[1]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				Intent previous = getIntent();
				Intent intent = new Intent(MapaPanoramas.this, PanoramaDetail.class);
				try {
					intent.putExtra("id", finalInfo.getInt("id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				intent.putExtra("estado", previous.getStringExtra("estado"));
				startActivity(intent);
			}

		});

		mapaBackBtn = (ImageButton) findViewById(R.id.mapaBackBtn);
		mapaListBtn = (ImageButton) findViewById(R.id.mapaListListBtn);

		mapaBackBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MapaPanoramas.this.finish();
			}
		});

		mapaListBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				MapaPanoramas.this.finish();
			}
		});
	}

	@Override
    public void onBackPressed() {
		MapaPanoramas.this.finish();
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
    			MapaPanoramas.this.finish();
            	return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onInfoWindowClick(Marker arg0) {

	}

	private class ObtenerIconos extends AsyncTask<String, Void, Bitmap[]> {

		ProgressDialog dialog;
		MapaPanoramas activityRef;

		public ObtenerIconos(MapaPanoramas activityRef) {
			this.activityRef = activityRef;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(MapaPanoramas.this, "", "Cargando...", true);
		}

		@Override
		protected Bitmap[] doInBackground(String... params) {
			try {
				JSONArray tempInfo = new JSONArray(params[0]);
				iconos = new Bitmap[tempInfo.length()];
				for(int i = 0; i < tempInfo.length(); i++) {
					JSONObject objInfo = tempInfo.getJSONObject(i);
					JSONObject iconInfo = objInfo.getJSONObject("icono");
					if(iconInfo.has("imagen") && !iconInfo.isNull("imagen") && !iconInfo.getString("imagen").equals("")) {
						URL url = new URL("http://play.medialabs.net" + iconInfo.getString("imagen"));
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				        connection.setDoInput(true);
				        connection.connect();
				        InputStream input = connection.getInputStream();
				        iconos[i] = BitmapFactory.decodeStream(input);
					}
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
				return null;
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return iconos;

		}

		@Override
		protected void onPostExecute(Bitmap[] icons) {
			super.onPostExecute(icons);
			dialog.dismiss();
			if(icons != null) {
				activityRef.loadMap(icons);
			}

		}

	}

}
