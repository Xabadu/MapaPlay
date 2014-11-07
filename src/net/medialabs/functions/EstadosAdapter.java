package net.medialabs.functions;

import net.medialabs.mapaplay.Estados;
import net.medialabs.mapaplay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class EstadosAdapter extends PagerAdapter {
	Context context;
	JSONArray infoEstados;
	private String[] imagenesEstados;
	private String[] tituloEstados;
	private Integer[] idsEstados;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	String fontPath = "fonts/Avenir.ttc";

	public EstadosAdapter(Context context, JSONArray estados){
		this.context = context;
		infoEstados = estados;
		imagenesEstados = new String[infoEstados.length()];
		tituloEstados = new String[infoEstados.length()];
		idsEstados = new Integer[infoEstados.length()];
				
		for(int i = 0; i < infoEstados.length(); i++) {
			JSONObject objEstados;
			try {
				objEstados = infoEstados.getJSONObject(i);
				JSONObject objEmocion = objEstados.getJSONObject("emocion"); 
				imagenesEstados[i] = "http://play.medialabs.net" + objEmocion.getString("imagen");
				tituloEstados[i] = objEmocion.getString("name");
				idsEstados[i] = objEmocion.getInt("id");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
		imageLoader = ImageLoader.getInstance();
		imageLoader.init(config);
		options = new DisplayImageOptions.Builder()
		        .cacheOnDisc()
		        .build();
	}

	@Override
	public int getCount() {
		return infoEstados.length();
	}
 
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}
 
	@Override
	public Object instantiateItem(ViewGroup container, final int position) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.estado_detail_page, null);
		final TextView title = (TextView) view.findViewById(R.id.commentsByText);
		final ImageView icon = (ImageView) view.findViewById(R.id.estadoDetailIconImage);
		final ImageView bg = (ImageView) view.findViewById(R.id.estadoDetailBgImage);
		bg.setVisibility(View.INVISIBLE);
		icon.setVisibility(View.INVISIBLE);
		title.setVisibility(View.INVISIBLE);
		title.setText(tituloEstados[position]);
		
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Estados) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		int iconWidth = 0;
		int iconHeight = 0;
		int density = displaymetrics.densityDpi;

		switch(density) {

			case DisplayMetrics.DENSITY_LOW:
				iconWidth = 192;
				iconHeight = 192;
				 break;

			case DisplayMetrics.DENSITY_MEDIUM:
				iconWidth = 256;
				iconHeight = 256;
				 break;

			case DisplayMetrics.DENSITY_HIGH:
				iconWidth = 384;
				iconHeight = 384;
				 break;

			case DisplayMetrics.DENSITY_XHIGH:
				iconWidth = 512;
				iconHeight = 512;
				 break;
			
			case DisplayMetrics.DENSITY_XXHIGH:
				iconWidth = 768;
				iconHeight = 768;
				break;
				
			default:
				iconWidth = 1024;
				iconHeight = 1024;
				break;
		}
		imageLoader.displayImage(imagenesEstados[position], icon, options, new ImageLoadingListener() {
		    @Override
		    public void onLoadingStarted(String imageUri, View view) {
		    }
		    @Override
		    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
		       
		    }
		    @Override
		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		    	icon.setVisibility(View.VISIBLE);
		    	bg.setVisibility(View.VISIBLE);
		    	title.setVisibility(View.VISIBLE);
		    }
		    @Override
		    public void onLoadingCancelled(String imageUri, View view) {
		    }
		});
		icon.getLayoutParams().height = iconWidth;
		icon.getLayoutParams().width = iconHeight;
		Typeface tf = Typeface.createFromAsset(context.getAssets(), fontPath);
		title.setTypeface(tf);
		
		icon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((Estados) context).startPanoramas(idsEstados[position]);
			}
		});
		
		((ViewPager) container).addView(view, 0);
		return view;
	}
 
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);
	}
}
