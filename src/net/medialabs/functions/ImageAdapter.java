package net.medialabs.functions;

import net.medialabs.mapaplay.R;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class ImageAdapter extends PagerAdapter {
	Context context;
	JSONArray images;
	String url = "http://play.medialabs.net";
	ImageLoader imageLoader;
	DisplayImageOptions options;
	private Integer[] GalImages = { R.drawable.img_teleferico, R.drawable.img_teleferico, R.drawable.img_teleferico };
	private String[] PanoramaImages;
	
	public ImageAdapter(Context context, JSONArray images){
		this.context = context;
		this.images = images;
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
		.cacheInMemory()
        .cacheOnDisc()
        .build();
		PanoramaImages = new String[images.length()];
		for(int i = 0; i < images.length(); i++) {
			try {
				PanoramaImages[i] = images.getString(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getCount() {
		if(images.length() > 0) {
			return images.length();
		} else {
			return GalImages.length;
		}
	}
 
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((RelativeLayout) object);
	}
 
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.panorama_detail_pager, null);
		final ImageView imageView = (ImageView) view.findViewById(R.id.panoramaDetailPagerImage);
		imageView.setVisibility(View.INVISIBLE);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
		if(PanoramaImages.length > 0) {
			String fullURL = url + PanoramaImages[position];
			Log.d("Url", fullURL);
			imageLoader.displayImage(fullURL, imageView, options, new ImageLoadingListener() {
			    @Override
			    public void onLoadingStarted(String imageUri, View view) {
			    }
			    @Override
			    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			       Log.d("Fail", failReason.toString());
			    }
			    @Override
			    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			    	imageView.setVisibility(View.VISIBLE);
			    }
			    @Override
			    public void onLoadingCancelled(String imageUri, View view) {
			    }
			});
		} else {
			imageView.setImageResource(GalImages[position]);
			imageView.setVisibility(View.VISIBLE);
		}
		((ViewPager) container).addView(view, 0);
		return view;
	}
 
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((RelativeLayout) object);
	}
}
