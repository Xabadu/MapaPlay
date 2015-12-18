package net.medialabs.mapaplay;

import java.util.ArrayList;
import java.util.HashMap;

import net.medialabs.functions.MusicAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

public class Musica extends Activity {

	ImageButton musicTopBarCloseBtn;
	ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
	ListView musicList;
	MusicAdapter adapter;
	boolean hide;

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
		setContentView(R.layout.activity_musica);
		if(hide) {
			ImageView statusBar = (ImageView) findViewById(R.id.musicTopBarImage);
			ImageButton backBtn = (ImageButton) findViewById(R.id.musicTopBarCloseBtn);
			statusBar.setVisibility(View.GONE);
			backBtn.setVisibility(View.GONE);
		}
		musicTopBarCloseBtn = (ImageButton) findViewById(R.id.musicTopBarCloseBtn);

		Intent intent = getIntent();

		String data = intent.getStringExtra("data");
		try {
			JSONObject panorama = new JSONObject(data);
			final JSONArray canciones = panorama.getJSONArray("canciones");

			for(int i = 0; i < canciones.length(); i++) {
				JSONObject songInfo = canciones.getJSONObject(i);
				HashMap<String, String> songHashMap = new HashMap<String, String>();
				songHashMap.put("artist", songInfo.getString("artista") + " - " + songInfo.getString("nombre"));
				songHashMap.put("image", "http://play.medialabs.net" + songInfo.getString("caratula"));
				songsList.add(songHashMap);
			}

			musicList = (ListView) findViewById(R.id.musicList);
			adapter = new MusicAdapter(this, songsList);
			musicList.setAdapter(adapter);

			musicList.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					try {
						JSONObject songInfo = canciones.getJSONObject(position);
						String url = songInfo.getString("link_android");
	    				Intent intent = new Intent(Intent.ACTION_VIEW);
	    				intent.setData(Uri.parse(url));
	    				startActivity(intent);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			});

		} catch (JSONException e) {
			e.printStackTrace();
		}

		musicTopBarCloseBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Musica.this.finish();
			}
		});

	}

	@Override
    public void onBackPressed() {
		Musica.this.finish();
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
    			Musica.this.finish();
            	return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
