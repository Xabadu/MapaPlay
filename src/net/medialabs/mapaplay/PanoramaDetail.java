package net.medialabs.mapaplay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.medialabs.functions.ImageAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.viewpagerindicator.CirclePageIndicator;


public class PanoramaDetail extends Activity {
	
	ImageButton panoramaDetailBackBtn;
	ImageButton panoramaDetailComoLlegarBtn;
	ImageButton panoramaDetailCommentsBtn;
	ImageButton panoramaDetailShareBtn;
	ImageButton panoramaDetailMusicBtn;
	RatingBar panoramaDetailRatingBar;
	TextView panoramaDetailPrice;
	TextView panoramaDetailTitle;
	TextView panoramaDetailHours;
	TextView panoramaDetailDescription;
	TextView panoramaDetailCommentsText;
	
	int panorama;
	int ranking;
	int usuario;
	
	SharedPreferences mSharedPreferences;
	
	String proximaNovaLight = "fonts/ProximaNova-Light.otf";
	String proximaNovaRegular = "fonts/ProximaNova-Regular.otf";
	boolean hide = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
	    mSharedPreferences = getApplicationContext().getSharedPreferences(
                "PlayFMPreferences", 0);
	    Intent intent = getIntent();
	    panorama = intent.getIntExtra("id", 0);
	    usuario = mSharedPreferences.getInt("USER_ID", 0);
	    DetallePanorama detallePanorama = new DetallePanorama(this);
	    detallePanorama.execute(intent.getIntExtra("id", 0));
	}
	
	public void cargarPanorama(final String data) {
		setContentView(R.layout.activity_panorama_detail);
		
	    panoramaDetailBackBtn = (ImageButton) findViewById(R.id.panoramaDetailBackBtn);
	    panoramaDetailComoLlegarBtn = (ImageButton) findViewById(R.id.panoramaDetailComoLlegarBtn);
	    panoramaDetailCommentsBtn = (ImageButton) findViewById(R.id.panoramaDetailCommentsBtn);
	    panoramaDetailShareBtn = (ImageButton) findViewById(R.id.panoramaDetailShareBtn);
	    panoramaDetailMusicBtn = (ImageButton) findViewById(R.id.panoramaDetailMusicBtn);
	    panoramaDetailTitle = (TextView) findViewById(R.id.panoramaDetailTitleText);
	    panoramaDetailHours = (TextView) findViewById(R.id.panoramaDetailHoursText);
	    panoramaDetailDescription = (TextView) findViewById(R.id.panoramaDetailDescriptionText);
	    panoramaDetailPrice = (TextView) findViewById(R.id.panoramaDetailValueText);
	    panoramaDetailCommentsText = (TextView) findViewById(R.id.panoramaDetailCommentsText);
	    panoramaDetailRatingBar = (RatingBar) findViewById(R.id.panoramaDetailRatingBar);
	    if(hide) {
			ImageView statusBar = (ImageView) findViewById(R.id.panoramaDetailTopBarImage);
			statusBar.setVisibility(View.GONE);
			panoramaDetailBackBtn.setVisibility(View.GONE);
		}
	    
	    ViewPager viewPager = (ViewPager) findViewById(R.id.panoramaDetailPicturesViewPager);
	    CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.panoramaDetailCircleIndicator);
	    
	    Typeface tfLight = Typeface.createFromAsset(getAssets(), proximaNovaLight);
		Typeface tfRegular = Typeface.createFromAsset(getAssets(), proximaNovaRegular);
	    
	    try {
			JSONObject panoramaInfo = new JSONObject(data);
			panoramaDetailTitle.setText(panoramaInfo.getString("nombre"));
			panoramaDetailTitle.setTypeface(tfLight);
			panoramaDetailDescription.setText(panoramaInfo.getString("descripcion"));
			panoramaDetailDescription.setTypeface(tfRegular);
			panoramaDetailHours.setText(panoramaInfo.getString("dias"));
			panoramaDetailHours.setTypeface(tfRegular);
			if(panoramaInfo.has("precio_desde") && panoramaInfo.has("precio_hasta")) {
				panoramaDetailPrice.setText("$" + panoramaInfo.getString("precio_desde") + " - $" + panoramaInfo.getString("precio_hasta"));
				panoramaDetailPrice.setTextSize(10);
			} else if(panoramaInfo.has("precio_desde") && !panoramaInfo.has("precio_hasta")) {
				panoramaDetailPrice.setText("$" + panoramaInfo.getString("precio_desde"));
			} else if(!panoramaInfo.has("precio_desde") && panoramaInfo.has("precio_hasta")) {
				panoramaDetailPrice.setText("$" + panoramaInfo.getString("precio_hasta"));
			} else if(!panoramaInfo.has("precio_desde") && !panoramaInfo.has("precio_hasta")) {
				panoramaDetailPrice.setText("Gratis");
			}
			panoramaDetailRatingBar.setProgress(panoramaInfo.getInt("ranking"));
			panoramaDetailPrice.setTypeface(tfRegular);
			panoramaDetailCommentsText.setText(Integer.toString(panoramaInfo.getInt("total_comentarios")));
			panoramaDetailCommentsText.setTypeface(tfRegular);
			if(panoramaInfo.getInt("total_comentarios") < 10) {
				panoramaDetailCommentsText.setPadding(12, 0, 0, 0);
			} else if(panoramaInfo.getInt("total_comentarios") < 100) {
				panoramaDetailCommentsText.setPadding(4, 0, 0, 0);
			}
			JSONArray panoramaImages = panoramaInfo.getJSONArray("imagenes");
		    ImageAdapter adapter = new ImageAdapter(this, panoramaImages);
		    viewPager.setAdapter(adapter);

		    circleIndicator.setViewPager(viewPager);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	    
	    panoramaDetailRatingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
	    	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
	    		ranking = (int) rating;
	    		RankingPanorama rankingPanorama = new RankingPanorama();
	    		rankingPanorama.execute(panorama, usuario, ranking);
	    	}
	    });
	    
	    panoramaDetailBackBtn.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		PanoramaDetail.this.finish();
	    	}
	    });
	    
	    panoramaDetailComoLlegarBtn.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(PanoramaDetail.this, ComoLlegar.class);
	    		Intent previous = getIntent();
	    		intent.putExtra("data", data);
	    		intent.putExtra("id", previous.getIntExtra("id", 0));
	    		startActivity(intent);
	    	}
	    });
	    
	    panoramaDetailMusicBtn.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(PanoramaDetail.this, Musica.class);
	    		intent.putExtra("data", data);
	    		startActivity(intent);
	    	}
	    });
	    
	    panoramaDetailCommentsBtn.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		Intent intent = new Intent(PanoramaDetail.this, Comments.class);
	    		Intent previous = getIntent();
	    		intent.putExtra("id", previous.getIntExtra("id", 0));
	    		startActivity(intent);
	    	}
	    });
	    
	    panoramaDetailShareBtn.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		JSONObject compartirInfo;
				try {
					compartirInfo = new JSONObject(data);
					compartirPanorama(compartirInfo.getString("nombre"), compartirInfo.getString("direccion"),
		    				compartirInfo.getString("descripcion_corta"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    	}
	    });
	}
	
	public void compartirPanorama(String nombre, String direccion, String descripcion) {

		Bundle params = new Bundle();
	    params.putString("name", nombre);
	    params.putString("caption", direccion);
	    params.putString("description", descripcion);
	    params.putString("link", "http://www.playfm.cl");
	    params.putString("picture", "https://d2uykijsw1jrmd.cloudfront.net/media/cache/f9/e9/f9e9730885f781660c35e23292e283a2.jpg");

	    WebDialog feedDialog = (
	        new WebDialog.FeedDialogBuilder(this,
	            Session.getActiveSession(),
	            params))
	        .setOnCompleteListener(new OnCompleteListener() {

	            @Override
	            public void onComplete(Bundle values,
	                FacebookException error) {
	                if (error == null) {
	                    // When the story is posted, echo the success
	                    // and the post Id.
	                    final String postId = values.getString("post_id");
	                    if (postId != null) {
	                        Toast.makeText(PanoramaDetail.this,
	                            "Has compartido un panorama de Mapa Play con tus amigos",
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(PanoramaDetail.this, 
	                            "Cancelado", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(PanoramaDetail.this, 
	                        "Cancelado", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(PanoramaDetail.this, 
	                        "Error, intŽntalo nuevamente", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
	
	@Override
    public void onBackPressed() {
		Intent intent = new Intent(PanoramaDetail.this, Panoramas.class);
		Intent previous = getIntent();
		intent.putExtra("estado", previous.getIntExtra("estado", 0));
		startActivity(intent);
		PanoramaDetail.this.finish();
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
    			PanoramaDetail.this.finish();
            	return true;
            
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	private class DetallePanorama extends AsyncTask<Integer, Void, String> {
		
		private ProgressDialog dialog;
		private PanoramaDetail activityRef;
		
		public DetallePanorama(PanoramaDetail activityRef) {
			this.activityRef = activityRef;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(PanoramaDetail.this, "", "Cargando...", true);
		}
		
		@Override
		protected String doInBackground(Integer... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://play.medialabs.net/panorama/" + Integer.toString(params[0])+ ".json");
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
			dialog.dismiss();
			Log.d("Resultado", result);
			activityRef.cargarPanorama(result);
		}
		
	}
	
	private class RankingPanorama extends AsyncTask<Integer, Void, String> {
		
		ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(PanoramaDetail.this, "", "Enviando voto...", true);
		}
		
		@Override
		protected String doInBackground(Integer... params) {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://play.medialabs.net/ranking.json");
            post.setHeader("content-type", "application/json");
            
            JSONObject ranking = new JSONObject();
			try {
				ranking.put("panorama", params[0]);
				ranking.put("usuario", params[1]);
				ranking.put("valor", params[2]);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				StringEntity entity = new StringEntity(ranking.toString(), HTTP.UTF_8);
				post.setEntity(entity);
			} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
			}
				
			try {
				HttpResponse resp = client.execute(post);
				return EntityUtils.toString(resp.getEntity());
				
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Log.d("Resultado", result);
			try {
				JSONObject resultado = new JSONObject(result);
				int valor = resultado.getInt("ranking");
				panoramaDetailRatingBar.setProgress(valor);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dialog.dismiss();
		}
		
	}
	
}