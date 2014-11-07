package net.medialabs.mapaplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import net.medialabs.functions.EstadosAdapter;

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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.viewpagerindicator.CirclePageIndicator;

public class Estados extends FragmentActivity implements 
GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener {
	
	private LocationClient mLocationClient;
	private Location mCurrentLocation;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	
	private int pagina;
	Drawable[] bgList;
	String fontPath = "fonts/Avenir.ttc";
	private static SharedPreferences mSharedPreferences;
	boolean hide = false;
	
    private ProgressDialog pDialog;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLocationClient = new LocationClient(this, this, this);	
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		if( (metrics.widthPixels == 240) || 
				(metrics.widthPixels == 320)	|| 
				(metrics.widthPixels == 480) || 
				(metrics.widthPixels == 720) ) {
			getActionBar().hide();
		} else {
			hide = true;
		}
		mSharedPreferences = getApplicationContext().getSharedPreferences(
                "PlayFMPreferences", 0);
		obtenerEstados estados = new obtenerEstados(this);
		estados.execute();
		
	}
	
	@SuppressLint("NewApi")
	public void mostrarEstados(final String data, Drawable[] backgrounds) {
		setContentView(R.layout.activity_estados);
		if(hide) {
			ImageView statusBar = (ImageView) findViewById(R.id.estadosTopBarImage);
			statusBar.setVisibility(View.GONE);
		}
		final ViewPager viewPager = (ViewPager) findViewById(R.id.estadosIconosViewPager);
		CirclePageIndicator circleIndicator = (CirclePageIndicator) findViewById(R.id.estadosCircleIndicator);
		
		bgList = backgrounds;
		if(data != null) {
			try {
				JSONArray infoEstados = new JSONArray(data);
				
			    EstadosAdapter adapter = new EstadosAdapter(this, infoEstados);
			    viewPager.setAdapter(adapter);
			    circleIndicator.setViewPager(viewPager);
			    
			    RelativeLayout rl = (RelativeLayout) findViewById(R.id.estadosLayout);

			    rl.setBackground(bgList[0]);

		        TextView estadosTitleText = (TextView) findViewById(R.id.estadosTitleText);
		 
		        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
		        
		        estadosTitleText.setTypeface(tf, Typeface.BOLD);
			    
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		pagina = viewPager.getCurrentItem();
		
		circleIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}

			@Override
			public void onPageSelected(int arg0) {
				pagina = viewPager.getCurrentItem();
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.estadosLayout);
			    Animation mAnim = AnimationUtils.loadAnimation(Estados.this, android.R.anim.fade_out);
		        rl.startAnimation(mAnim);
			    rl.setBackground(bgList[pagina]);
			    mAnim = AnimationUtils.loadAnimation(Estados.this, android.R.anim.fade_in);
			    rl.startAnimation(mAnim);
			}
			
		});
		
		Random generator = new Random();
		int shareValue = generator.nextInt(10) + 1;
		if(shareValue == 2) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Comparte con tus amigos");
			builder.setMessage("¿Quieres contarle a tus amigos en Facebook sobre Mapa Play?");
			builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int id) {
    				shareApp();
    			}
    		});
			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			
		}

	}

	public void startPanoramas(int id) {
		Intent intent = new Intent(this, Panoramas.class);
		intent.putExtra("estado", id);
		startActivity(intent);
	}
	
	public void shareApp() {

		Bundle params = new Bundle();
	    params.putString("name", "Mapa Play");
	    params.putString("caption", "La guía para la ciudad de PlayFM");
	    params.putString("description", "Cuéntale a tus amigos que estás usando Mapa Play e invítalos a descubrir nuevos panoramas.");
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
	                        Toast.makeText(Estados.this,
	                            "Has compartido Mapa Play con tus amigos",
	                            Toast.LENGTH_SHORT).show();
	                    } else {
	                        // User clicked the Cancel button
	                        Toast.makeText(Estados.this, 
	                            "Cancelado", 
	                            Toast.LENGTH_SHORT).show();
	                    }
	                } else if (error instanceof FacebookOperationCanceledException) {
	                    // User clicked the "x" button
	                    Toast.makeText(Estados.this, 
	                        "Cancelado", 
	                        Toast.LENGTH_SHORT).show();
	                } else {
	                    // Generic, ex: network error
	                    Toast.makeText(Estados.this, 
	                        "Error, inténtalo nuevamente", 
	                        Toast.LENGTH_SHORT).show();
	                }
	            }

	        })
	        .build();
	    feedDialog.show();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
    
    @Override
    public void onBackPressed() {
		this.finish();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
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
		}

	}

	@Override
	public void onConnected(Bundle arg0) {
		mCurrentLocation = mLocationClient.getLastLocation();
	}
    
	private class obtenerEstados extends AsyncTask<Void, Integer, EstadosObjects> {
		
		EstadosObjects objetosEstadosResp;
		Drawable[] backgrounds = null;
		private ProgressDialog dialog;
		private Estados activityRef;
		double latitude;
		double longitude;
		int id;
		
		public obtenerEstados(Estados activityRef) {
			this.activityRef = activityRef;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(mCurrentLocation != null) {
				latitude = mCurrentLocation.getLatitude();
				longitude = mCurrentLocation.getLongitude();
				id = mSharedPreferences.getInt("USER_ID", 0);
			}
			
		    dialog = ProgressDialog.show(Estados.this, "", "Cargando estados...", true);
		    
		}
		
		@Override
		protected EstadosObjects doInBackground(Void... params) {
		    
			String respStr = "";
			
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://play.medialabs.net/emociones/all.json");
            get.setHeader("content-type", "application/json");
            try {
            	HttpResponse resp = client.execute(get);
            	respStr = EntityUtils.toString(resp.getEntity());
            	
          	} catch(Exception e) {
            	Log.d("Entra al catch", e.toString());
            }
            
            if(mCurrentLocation != null) {
            	HttpPost post = new HttpPost("http://play.medialabs.net/geolocation/used.json");
                post.setHeader("content-type", "application/json");
                
                JSONObject location = new JSONObject();
    			try {
    				location.put("user_id", id);
    				location.put("lat", String.valueOf(latitude));
    				location.put("long", String.valueOf(longitude));
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
    			try {
    				StringEntity entity = new StringEntity(location.toString(), HTTP.UTF_8);
    				post.setEntity(entity);
    			} catch (UnsupportedEncodingException e1) {
    					e1.printStackTrace();
    			}
    				
    			try {
    				HttpResponse resp = client.execute(post);
    				Log.d("Result location", EntityUtils.toString(resp.getEntity()));
    				
    			} catch (ClientProtocolException e1) {
    				e1.printStackTrace();
    			} catch (IOException e1) {
    				e1.printStackTrace();
    			}
            }
            
            try {
				JSONArray infoEstados = new JSONArray(respStr);
				
				try {
					DisplayMetrics metrics = new DisplayMetrics();
					getWindowManager().getDefaultDisplay().getMetrics(metrics);
					backgrounds = new Drawable[infoEstados.length()];
					for(int i = 0; i < backgrounds.length; i++) {
						JSONObject objEstados = infoEstados.getJSONObject(i);
					    JSONObject emocion = objEstados.getJSONObject("emocion");
					    String url = "";
					    if(metrics.heightPixels != 320 && 
								metrics.heightPixels != 480 && 
								metrics.heightPixels != 800 && 
								metrics.heightPixels != 1024 && 
								metrics.heightPixels != 1280 ) {
							url = "http://play.medialabs.net" 
						    		+ emocion.getString("background_" 
									+ Integer.toString(800) + "x" 
									+ Integer.toString(1280));
							
						} else {
							if(metrics.heightPixels == 976) {
								url = "http://play.medialabs.net" 
							    		+ emocion.getString("background_" 
										+ Integer.toString(metrics.widthPixels) + "x" 
										+ Integer.toString(1024));
							} else if(metrics.heightPixels == 1184) {
								url = "http://play.medialabs.net" 
							    		+ emocion.getString("background_" 
										+ Integer.toString(800) + "x" 
										+ Integer.toString(1280));
							}
							else {
								url = "http://play.medialabs.net" 
							    		+ emocion.getString("background_" 
										+ Integer.toString(metrics.widthPixels) + "x" 
										+ Integer.toString(metrics.heightPixels));
							}
						}
						String filename = "fondo.jpg";
						backgrounds[i] = ImageOperations(Estados.this, url, filename);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            objetosEstadosResp = new EstadosObjects(respStr, backgrounds);
			
            return objetosEstadosResp;
		}
		
		@Override
		protected void onPostExecute(EstadosObjects result) {
			super.onPostExecute(result);
			
			
			activityRef.mostrarEstados(result.getData(), result.getBackgrounds());
			dialog.dismiss();
			
		}
		
		private Drawable ImageOperations(Context ctx, String url, String saveFilename) {
	        try {
	            InputStream is = (InputStream) this.fetch(url);
	            Drawable d = Drawable.createFromStream(is, saveFilename);
	            return d;
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	            return null;
	        } catch (IOException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    public Object fetch(String address) throws MalformedURLException,IOException {
	        URL url = new URL(address);
	        Object content = url.getContent();
	        return content;
	    }
		
	}
	
	
}
