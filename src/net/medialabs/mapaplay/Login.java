package net.medialabs.mapaplay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import net.medialabs.functions.AlertDialogs;
import net.medialabs.functions.ConectivityTools;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

public class Login extends Activity {
	
	// Progress dialog
    ProgressDialog pDialog;
 
    // Shared Preferences
    private static SharedPreferences mSharedPreferences;
    
    // Custom utilities
    private ConectivityTools ct;
    private AlertDialogs alert = new AlertDialogs();
    
    // UI Elements
    ImageButton fbLoginBtn;
    
    // Facebook elements
    private static final List<String> READ_PERMISSIONS = Arrays.asList("email", "user_likes", "user_hometown", "user_about_me", 
    		"user_birthday", "user_interests");

    @SuppressLint("NewApi")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.activity_login);
		
		if (Build.VERSION.SDK_INT > 9) {
        	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        	StrictMode.setThreadPolicy(policy);
        }

		ct = new ConectivityTools(getApplicationContext());
		 
        if (!ct.isConnectingToInternet()) {
            alert.showAlertDialog(Login.this, "Error de Conexi—n",
                    "Esta aplicaci—n necesita una conexi—n a Internet para funcionar.", false);
            return;
        }
		
        mSharedPreferences = getApplicationContext().getSharedPreferences(
                "PlayFMPreferences", 0);
        
        if(Session.openActiveSessionFromCache(this) != null) {
        	Intent intent = new Intent(this, Estados.class);
        	startActivity(intent);
        	this.finish();
        } else {
        	fbLoginBtn = (ImageButton) findViewById(R.id.loginFbBtn);
           	final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.button_fadein);
          	fbLoginBtn.startAnimation(animationFadeIn);
           	try {
           	    PackageInfo info = getPackageManager().getPackageInfo(
           	            "net.medialabs.mapaplay", 
           	            PackageManager.GET_SIGNATURES);
           	    for (Signature signature : info.signatures) {
           	        MessageDigest md = MessageDigest.getInstance("SHA");
           	        md.update(signature.toByteArray());
           	        Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
       	        }
           	} catch (NameNotFoundException e) {
           		e.printStackTrace();
           	} catch (NoSuchAlgorithmException e) {
           		e.printStackTrace();
           	}
        }
	}
	
	public void loginFacebook(View view) {
	
		pDialog = ProgressDialog.show(Login.this, "",
                "Un momento...");
		    	
		openActiveSession(Login.this, true, new Session.StatusCallback() {

			@Override
			public void call(Session session, SessionState state, Exception exception) {
				if (state.isOpened()) {
					
				    Request.Callback callback = new Request.Callback() {

				        @Override
				        public void onCompleted(Response response) {
				            // response should have the likes
				            Log.d("Response", response.toString());
				            GraphObject graphObject = response.getGraphObject();
				            JSONObject result = graphObject.getInnerJSONObject();
				            pDialog.dismiss();
				            UpdateUser updateUser = new UpdateUser();
				            updateUser.execute(result);
				        }
				    };
				    Bundle params = new Bundle();
				    params.putString("fields", "id,first_name, middle_name,last_name,name,username," +
				    		"link,gender,birthday,hometown,location,email,interests,likes,picture");
				    Request request = new Request(session, "me", params, HttpMethod.GET, callback);
				    RequestAsyncTask task = new RequestAsyncTask(request);
				    task.execute();
					
				}
				if(state.isClosed()) {
					pDialog.dismiss();
					Log.d("error", state.toString());
					Toast.makeText(Login.this, "Autentificaci—n cancelada", Toast.LENGTH_SHORT).show();
				}
			}
		}, READ_PERMISSIONS);
		
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
	private static Session openActiveSession(Activity activity, boolean allowLoginUI, StatusCallback callback, List<String> permissions) {
	    OpenRequest openRequest = new OpenRequest(activity).setPermissions(permissions).setCallback(callback);
	    Session session = new Builder(activity).build();
	    if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
	        Session.setActiveSession(session);
	        session.openForRead(openRequest);
	        return session;
	    }
	    return null;
	}
	
	
	
	private class UpdateUser extends AsyncTask<JSONObject, Void, String> {
		
		String picture = "";
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = ProgressDialog.show(Login.this, "", "Validando...", true);
		}
		
		@Override
		protected String doInBackground(JSONObject... params) {
			HttpClient client = new DefaultHttpClient();
            
            HttpPost post = new HttpPost("http://play.medialabs.net/usuario/update.json");
            post.setHeader("content-type", "application/json");
            
            JSONObject usuario = new JSONObject();
            try {
				if(params[0].has("picture") && !params[0].isNull("picture")) {
					JSONObject picInfo = params[0].getJSONObject("picture");
					if(picInfo.has("data") && !picInfo.isNull("data")) {
						JSONObject picData = picInfo.getJSONObject("data");
						if(picData.has("url") && !picData.isNull("url")) {
							picture = picData.getString("url");
						}
					}
					
				}
            	
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            try {
        				                					                	
				usuario.put("fb_id", params[0].getString("id"));
				if(params[0].has("first_name") && !params[0].getString("first_name").equals("")) {
					usuario.put("first_name", params[0].getString("first_name"));
				} 
				if(params[0].has("middle_name") && !params[0].getString("middle_name").equals("")) {
					usuario.put("middle_name", params[0].getString("middle_name"));
				}
				if(params[0].has("last_name") && !params[0].getString("last_name").equals("")) {
					usuario.put("last_name", params[0].getString("last_name"));
				}
				if(params[0].has("name") && !params[0].getString("name").equals("")) {
					usuario.put("full_name", params[0].getString("name"));
				}
				if(params[0].has("birthday") && !params[0].getString("birthday").equals("")) {
					usuario.put("birthday", params[0].getString("birthday"));
				}
				if(params[0].has("username") && !params[0].getString("username").equals("")) {
					usuario.put("username", params[0].getString("username"));
				}
				if(params[0].has("location") && !params[0].getJSONObject("location").getString("name").equals("")) {
					usuario.put("location", params[0].getJSONObject("location").getString("name"));
				}
				if(params[0].has("link") && !params[0].getString("link").equals("")) {
					usuario.put("fb_url", params[0].getString("link"));
				}
				if(params[0].has("email") && !params[0].getString("email").equals("")) {
					usuario.put("email", params[0].getString("email"));
				}
				if(params[0].has("gender") && !params[0].getString("gender").equals("")) {
					usuario.put("gender", params[0].getString("gender"));
				}
				if(params[0].has("birthday") && !params[0].getString("birthday").equals("")) {
					usuario.put("age", Integer.toString(Jarvis.getAge(params[0].getString("birthday"))));
				}
				if(params[0].has("hometown") && !params[0].getJSONObject("hometown").getString("name").equals("")) {
					usuario.put("city", params[0].getJSONObject("hometown").getString("name"));
				}
				if(params[0].has("interests")) {
					usuario.put("interests", params[0].getJSONObject("interests").toString());
				}
				if(params[0].has("likes")) {
				}
				usuario.put("avatar", params[0].getJSONObject("picture").getJSONObject("data").getString("url"));
				
				
				
				try {
					StringEntity entity = new StringEntity(usuario.toString(), HTTP.UTF_8);
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
				
				
					
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
            
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			try {
				JSONObject userInfo = new JSONObject(result);
				Editor e = mSharedPreferences.edit();					               
	            e.putInt("USER_ID", userInfo.getInt("id"));
	            e.putInt("FB_ID", userInfo.getInt("fb_id"));
	            e.putString("USER_FIRSTNAME", userInfo.getString("first_name"));
	            e.putString("USER_FULLNAME", userInfo.getString("full_name"));
	            e.putString("USER_EMAIL", userInfo.getString("email"));
	            e.putString("USER_AVATAR", picture);
	            e.commit();
	            
			} catch (JSONException e) {
				e.printStackTrace();
			}
			Log.d("Resultado", result);
			Intent intent = new Intent(Login.this, Estados.class);
			startActivity(intent);
			pDialog.dismiss();
			Login.this.finish();
		}
	}

}

