package net.medialabs.functions;

import java.util.ArrayList;
import java.util.HashMap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.medialabs.mapaplay.Estados;
import net.medialabs.mapaplay.R;

public class PanoramaAdapter extends BaseAdapter{
	
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	String proximaNovaLight = "fonts/ProximaNova-Light.otf";
	String proximaNovaRegular = "fonts/ProximaNova-Regular.otf";
	DisplayImageOptions options;
	private final String baseURL = "http://play.medialabs.net";
	
	public PanoramaAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
		.cacheInMemory()
        .cacheOnDisc()
        .build();		
	}
	
	public int getCount() {
		return data.size();
	}
	
	public Object getItem(int position) {
		return position;
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if(convertView == null) {
			vi = inflater.inflate(R.layout.panorama_detail_cell, null);
		}
		
		Typeface tfLight = Typeface.createFromAsset(activity.getAssets(), proximaNovaLight);
		Typeface tfRegular = Typeface.createFromAsset(activity.getAssets(), proximaNovaRegular);
		
		RelativeLayout layout = (RelativeLayout) vi.findViewById(R.id.panoramaDetailCellLayout);
		TextView title = (TextView) vi.findViewById(R.id.panoramaDetailCellTitle);
		TextView address = (TextView) vi.findViewById(R.id.panoramaDetailCellAddressText);
		TextView description = (TextView) vi.findViewById(R.id.panoramaDetailCellDescriptionText);
		TextView comments = (TextView) vi.findViewById(R.id.panoramaDetailCellCommentsNumberText);
		TextView price = (TextView) vi.findViewById(R.id.panoramaDetailCellPriceText);
		final ImageView image = (ImageView) vi.findViewById(R.id.panoramaDetailCellImage);
		image.setVisibility(View.INVISIBLE);
		HashMap<String, String> panorama = new HashMap<String, String>();
		panorama = data.get(position);
		
		if(position % 2 == 0) {
			layout.setBackgroundColor(Color.WHITE);
		} 
		
		if(panorama.get("title").equalsIgnoreCase("")) {
			title.setText("Sin nombre");
		} else {
			title.setText(panorama.get("title"));
		}
		title.setTypeface(tfLight);
		if(panorama.get("address").equalsIgnoreCase("")) {
			address.setText("Sin direcci—n");
		} else {
			address.setText(panorama.get("address"));
		}
		address.setTypeface(tfRegular);
		if(panorama.get("description").equalsIgnoreCase("")) {
			description.setText(panorama.get("Sin descripci—n"));
		} else {
			description.setText(panorama.get("description"));
		}
		description.setTypeface(tfRegular);
		comments.setText(panorama.get("comments"));
		comments.setTypeface(tfRegular);
		if(Integer.parseInt(panorama.get("comments")) < 10) {
			comments.setPadding(4, 0, 0, 0);
		}
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		int iconWidth = 0;
		int iconHeight = 0;
		int density = displaymetrics.densityDpi;

		switch(density) {

			case DisplayMetrics.DENSITY_LOW:
				iconWidth = 45;
				iconHeight = 45;
				 break;

			case DisplayMetrics.DENSITY_MEDIUM:
				iconWidth = 60;
				iconHeight = 60;
				 break;

			case DisplayMetrics.DENSITY_HIGH:
				iconWidth = 90;
				iconHeight = 90;
				 break;

			case DisplayMetrics.DENSITY_XHIGH:
				iconWidth = 120;
				iconHeight = 120;
				 break;
		}
		
		if(!panorama.get("image").equalsIgnoreCase("")) {
			imageLoader.displayImage(baseURL + panorama.get("image"), image, options, new ImageLoadingListener() {
			    @Override
			    public void onLoadingStarted(String imageUri, View view) {
			    }
			    @Override
			    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			       
			    }
			    @Override
			    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			    	image.setVisibility(View.VISIBLE);
			    }
			    @Override
			    public void onLoadingCancelled(String imageUri, View view) {
			    }
			});
		} else {
			image.setVisibility(View.VISIBLE);
		}
		
		image.getLayoutParams().height = iconHeight;
		image.getLayoutParams().width = iconWidth;

		if(panorama.get("price_mod").equalsIgnoreCase("free")) {
			price.setText("Gratis");
			price.setPadding(10, 0, 0, 0);
		} else if(panorama.get("price_mod").equalsIgnoreCase("unique")) {
			price.setText("$ " + panorama.get("price"));
			price.setPadding(10, 0, 0, 0);
		} else if(panorama.get("price_mod").equalsIgnoreCase("range")) {
			price.setText("$" + panorama.get("price_start") + " - $" + panorama.get("price_end"));
			price.setTextSize(9);
			price.setPadding(1, 4, 0, 0);
		}
		price.setTypeface(tfRegular);
		return vi;
	}

}
