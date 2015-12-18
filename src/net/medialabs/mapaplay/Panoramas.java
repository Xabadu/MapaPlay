package net.medialabs.mapaplay;

import java.util.ArrayList;
import java.util.HashMap;

import net.medialabs.functions.PanoramaAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

public class Panoramas extends Activity {

	ListView list;
	PanoramaAdapter adapter;
	int estado;
	ImageButton panoramasBackBtn;
	ImageButton panoramasMapBtn;
	JSONArray panoramaArray = null;
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
		setContentView(R.layout.activity_panoramas);
		if(hide) {
			ImageView statusBar = (ImageView) findViewById(R.id.panoramasTopBarImage);
			ImageButton backBtn = (ImageButton) findViewById(R.id.panoramasBackBtn);
			statusBar.setVisibility(View.GONE);
			backBtn.setVisibility(View.GONE);
		}
		Intent intent = getIntent();
		estado = intent.getIntExtra("estado", 0);
		obtenerPanoramas panoramas = new obtenerPanoramas(this);
		panoramas.execute(estado);
	}

	public void listarPanoramas(final String result) {
		Log.d("Resultado", result);
		ArrayList<HashMap<String, String>> panoramasList = new ArrayList<HashMap<String, String>>();



		try {
			panoramaArray = new JSONArray(result);
			for(int i = 0; i < panoramaArray.length(); i++) {
				JSONObject panoramaObject = panoramaArray.getJSONObject(i);
				HashMap<String, String> panoramaInfo = new HashMap<String, String>();
				panoramaInfo.put("title", panoramaObject.getString("nombre"));
				panoramaInfo.put("address", panoramaObject.getString("direccion"));
				panoramaInfo.put("description", panoramaObject.getString("descripcion_corta"));
				panoramaInfo.put("comments",Integer.toString(panoramaObject.getInt("total_comentarios")));
				panoramaInfo.put("ranking", Integer.toString(panoramaObject.getInt("ranking")));
				panoramaInfo.put("value", panoramaObject.getString("precio_desde"));
				panoramaInfo.put("image", panoramaObject.getString("image"));

				if(panoramaObject.has("precio_desde") && panoramaObject.has("precio_hasta")) {
					panoramaInfo.put("price_mod", "range");
					panoramaInfo.put("price_start", panoramaObject.getString("precio_desde"));
					panoramaInfo.put("price_end", panoramaObject.getString("precio_hasta"));
				} else if(panoramaObject.has("precio_desde") && !panoramaObject.has("precio_hasta")) {
					if(panoramaObject.getString("precio_desde").equalsIgnoreCase("0")) {
						panoramaInfo.put("price_mod", "free");
					} else {
						panoramaInfo.put("price_mod", "unique");
						panoramaInfo.put("price", panoramaObject.getString("precio_desde"));
					}

				} else if(!panoramaObject.has("precio_desde") && panoramaObject.has("precio_hasta")) {
					if(panoramaObject.getString("precio_hasta").equalsIgnoreCase("0")) {
						panoramaInfo.put("price_mod", "free");
					} else {
						panoramaInfo.put("price_mod", "unique");
						panoramaInfo.put("price", panoramaObject.getString("precio_hasta"));
					}
				} else if(!panoramaObject.has("precio_desde") && !panoramaObject.has("precio_hasta")) {
					panoramaInfo.put("price_mod", "free");
				}

				panoramasList.add(panoramaInfo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}



		list = (ListView) findViewById(R.id.musicList);

		adapter = new PanoramaAdapter(this, panoramasList);
		Log.d("ARRAYLIST", panoramasList.toString());
		list.setAdapter(adapter);


		list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	JSONObject panorama;
				try {
					panorama = panoramaArray.getJSONObject(position);
					Intent intent = new Intent(Panoramas.this, PanoramaDetail.class);
	            	intent.putExtra("id", panorama.getInt("id"));
	            	intent.putExtra("estado", estado);
	            	startActivity(intent);
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
        });

		panoramasBackBtn = (ImageButton) findViewById(R.id.panoramasBackBtn);
		panoramasMapBtn = (ImageButton) findViewById(R.id.mapaMapBtn);

		panoramasBackBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Panoramas.this.finish();
			}
		});

		panoramasMapBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Panoramas.this, MapaPanoramas.class);
				intent.putExtra("data", result);
				intent.putExtra("estado", estado);
				startActivity(intent);
			}
		});

	}

	@Override
    public void onBackPressed() {
		Intent intent = new Intent(Panoramas.this, Estados.class);
		startActivity(intent);
		Panoramas.this.finish();
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
    			Panoramas.this.finish();
            	return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


	private class obtenerPanoramas extends AsyncTask<Integer, Integer, String> {

		private ProgressDialog dialog;
		private Panoramas activityRef;

		public obtenerPanoramas(Panoramas activityRef) {
			this.activityRef = activityRef;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		    dialog = ProgressDialog.show(Panoramas.this, "", "Cargando panoramas...", true);

		}

		@Override
		protected String doInBackground(Integer... params) {

			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://play.medialabs.net/panoramas/" + Integer.toString(params[0])+ ".json");
            get.setHeader("content-type", "application/json");
            try {
            	HttpResponse resp = client.execute(get);
            	String respStr = EntityUtils.toString(resp.getEntity());
            	return respStr;
          	} catch(Exception e) {
            	Log.d("Entra al catch", e.toString());
            }
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result != null) {
				activityRef.listarPanoramas(result);
				dialog.dismiss();
			} else {
				dialog.dismiss();
			}


		}

	}


}
