package net.medialabs.mapaplay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import net.medialabs.functions.CommentAdapter;
import net.medialabs.functions.Jarvis;

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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class Comments extends Activity {

	private ListView commentsListView;
	private CommentAdapter adapter;
	private EditText commentsTextField;
	private ImageButton commentsCloseBtn;
	private ImageButton sendCommentBtn;
	private ImageView userImage;
	private TextView commentsTitleText;
	private JSONArray listadoComentarios;
	private String VAGRoundedStdBold = "fonts/VAGRoundedStd-Bold.otf";
	ArrayList<HashMap<String, String>> comentarios = new ArrayList<HashMap<String, String>>();
	ImageLoader imageLoader;
	DisplayImageOptions options;
	SharedPreferences mSharedPreferences;
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
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
		.cacheInMemory()
        .cacheOnDisc()
        .build();
		Intent intent = getIntent();
		ObtenerComentarios obtenerComentarios = new ObtenerComentarios(this);
		obtenerComentarios.execute(intent.getIntExtra("id", 0));
	}

	public void loadComments(String data) {
		setContentView(R.layout.activity_comments);
		if(hide) {
			ImageView statusBar = (ImageView) findViewById(R.id.musicTopBarImage);
			ImageButton backBtn = (ImageButton) findViewById(R.id.musicTopBarCloseBtn);
			commentsTitleText = (TextView) findViewById(R.id.commentsByText);
			statusBar.setVisibility(View.GONE);
			backBtn.setVisibility(View.GONE);
			commentsTitleText.setVisibility(View.GONE);
		}
		mSharedPreferences = getApplicationContext().getSharedPreferences(
                "PlayFMPreferences", 0);

		commentsCloseBtn = (ImageButton) findViewById(R.id.musicTopBarCloseBtn);
		commentsTitleText = (TextView) findViewById(R.id.commentsByText);
		commentsTextField = (EditText) findViewById(R.id.commentsFormUserText);
		sendCommentBtn = (ImageButton) findViewById(R.id.commentsFormSendBtn);
		userImage = (ImageView) findViewById(R.id.commentsFormUserImage);

		userImage.setVisibility(View.INVISIBLE);

		Typeface tfVag = Typeface.createFromAsset(getAssets(), VAGRoundedStdBold);

		try {
			listadoComentarios = new JSONArray(data);
			if(listadoComentarios.length() == 1) {
				commentsTitleText.setText("1 comentario");
			} else {
				if(hide) {
					getActionBar().setTitle(listadoComentarios.length() + " comentarios");
				} else {
					commentsTitleText.setText(Integer.toString(listadoComentarios.length()) + " comentarios");
				}

			}
			commentsTitleText.setTypeface(tfVag);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		comentarios.clear();
		for(int i = 0; i < listadoComentarios.length(); i++) {
			try {
				JSONObject infoComentario = listadoComentarios.getJSONObject(i);
				JSONObject infoUsuario = infoComentario.getJSONObject("usuario");
				HashMap<String, String> parComentario = new HashMap<String, String>();
				parComentario.put("user", infoUsuario.getString("name"));
				parComentario.put("comment", infoComentario.getString("comentario"));
				parComentario.put("image", infoUsuario.getString("avatar"));
				String date = infoComentario.getString("fecha");
				String[] filteredDate = date.split("T");
				date = filteredDate[0] + " " + filteredDate[1];
				filteredDate = date.split("Z");
				String[] newDate = filteredDate[0].split(" ");
				date = newDate[0];
				String time = newDate[1];
				filteredDate = date.split("-");
				String[] filteredTime = time.split(":");
				date = filteredDate[2] + "-" + filteredDate[1] + "-" + filteredDate[0];
				time = filteredTime[0] + ":" + filteredTime[1];
				date = date + " " + time;
				parComentario.put("date", date);
				Log.d("Fecha", date);
				comentarios.add(parComentario);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		int iconWidth = 0;
		int iconHeight = 0;
		int density = displaymetrics.densityDpi;

		switch(density) {

			case DisplayMetrics.DENSITY_LOW:
				iconWidth = 38;
				iconHeight = 38;
				 break;

			case DisplayMetrics.DENSITY_MEDIUM:
				iconWidth = 50;
				iconHeight = 50;
				 break;

			case DisplayMetrics.DENSITY_HIGH:
				iconWidth = 75;
				iconHeight = 75;
				 break;

			case DisplayMetrics.DENSITY_XHIGH:
				iconWidth = 100;
				iconHeight = 100;
				 break;
		}

		imageLoader.displayImage(mSharedPreferences.getString("USER_AVATAR", ""), userImage, options, new ImageLoadingListener() {
		    @Override
		    public void onLoadingStarted(String imageUri, View view) {
		    }
		    @Override
		    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		       Log.d("Fail", failReason.toString());
		    }
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

		    	userImage.setVisibility(View.VISIBLE);
		    }
		    @Override
		    public void onLoadingCancelled(String imageUri, View view) {
		    }
		});
		userImage.getLayoutParams().height = iconWidth;
    	userImage.getLayoutParams().width = iconHeight;
		commentsListView = (ListView) findViewById(R.id.musicList);

		adapter = new CommentAdapter(this, comentarios);
		commentsListView.setAdapter(adapter);
		commentsListView.setSelection(comentarios.size() - 1);

		commentsCloseBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Comments.this.finish();
			}
		});

		sendCommentBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!commentsTextField.getText().toString().equals("")) {
					Intent intent = getIntent();
					Log.d("param 1", commentsTextField.getText().toString());
					Log.d("param 2", Integer.toString(intent.getIntExtra("id", 0)));
					Log.d("param 3", Integer.toString(mSharedPreferences.getInt("USER_ID", 0)));
					EnviarComentario enviarComentario = new EnviarComentario(Comments.this);
					enviarComentario.execute(commentsTextField.getText().toString(), Integer.toString(intent.getIntExtra("id", 0)),
							Integer.toString(mSharedPreferences.getInt("USER_ID", 0)));
				}
			}
		});

	}

	@Override
    public void onBackPressed() {
		Comments.this.finish();
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
    			Comments.this.finish();
            	return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

	private class ObtenerComentarios extends AsyncTask<Integer, Void, String> {

		private Comments activityRef;
		private ProgressDialog dialog;

		public ObtenerComentarios(Comments activityRef) {
			this.activityRef = activityRef;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(Comments.this, "", "Cargando comentarios", true);
		}

		@Override
		protected String doInBackground(Integer... params) {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("http://play.medialabs.net/comentarios/" + Integer.toString(params[0])+ ".json");
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
			Log.d("Comentarios", result);
			activityRef.loadComments(result);
			dialog.dismiss();
		}


	}

	private class EnviarComentario extends AsyncTask<String, Void, String> {

		Comments activityRef;
		ProgressDialog dialog;

		public EnviarComentario(Comments activityRef) {
			this.activityRef = activityRef;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(Comments.this, "", "Enviando...", true);
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient client = new DefaultHttpClient();

            HttpPost post = new HttpPost("http://play.medialabs.net/comentario/new.json");
            post.setHeader("content-type", "application/json");

            JSONObject comentario = new JSONObject();

            try {

				comentario.put("comentario", params[0]);
				comentario.put("panorama_id", Integer.valueOf(params[1]));
				comentario.put("user_id", Integer.valueOf(params[2]));

				try {
					StringEntity entity = new StringEntity(comentario.toString(), HTTP.UTF_8);
					post.setEntity(entity);
				} catch (UnsupportedEncodingException e1) {
						e1.printStackTrace();
				}

				try {
					HttpResponse resp = client.execute(post);
					HttpGet get = new HttpGet("http://play.medialabs.net/comentarios/" + params[1] + ".json");
					get.setHeader("content-type", "application/json");
					resp = client.execute(get);
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
			Log.d("Resultado", result);
			InputMethodManager imm = (InputMethodManager)getSystemService(
    			      Context.INPUT_METHOD_SERVICE);
    			imm.hideSoftInputFromWindow(commentsTextField.getWindowToken(), 0);
			activityRef.loadComments(result);
			dialog.dismiss();
		}

	}

}
