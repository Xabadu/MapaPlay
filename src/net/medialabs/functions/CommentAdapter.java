package net.medialabs.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.ocpsoft.prettytime.PrettyTime;

import net.medialabs.mapaplay.Estados;
import net.medialabs.mapaplay.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class CommentAdapter extends BaseAdapter{
	
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	DisplayImageOptions options;
	String proximaNovaRegular = "fonts/ProximaNova-Regular.otf";
	
	public CommentAdapter(Activity a, ArrayList<HashMap<String, String>> d) {
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
			vi = inflater.inflate(R.layout.comment_detail_cell, null);
		}
		
		TextView user = (TextView) vi.findViewById(R.id.commentDetailCellUserText);
		TextView comment = (TextView) vi.findViewById(R.id.commentDetailCellCommentText);
		TextView by = (TextView) vi.findViewById(R.id.commentsByText);
		TextView ago = (TextView) vi.findViewById(R.id.commentsDetailCellAgoText);
		TextView date = (TextView) vi.findViewById(R.id.commentDetailCellDateText);
		final ImageView image = (ImageView) vi.findViewById(R.id.commentDetailCellImage);
		image.setVisibility(View.INVISIBLE);
		Typeface tfRegular = Typeface.createFromAsset(activity.getAssets(), proximaNovaRegular);
		
		HashMap<String, String> commentObj = new HashMap<String, String>();
		commentObj = data.get(position);
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		int iconWidth = 0;
		int iconHeight = 0;
		int density = displaymetrics.densityDpi;

		switch(density) {

			case DisplayMetrics.DENSITY_LOW:
				iconWidth = 49;
				iconHeight = 49;
				 break;

			case DisplayMetrics.DENSITY_MEDIUM:
				iconWidth = 65;
				iconHeight = 65;
				 break;

			case DisplayMetrics.DENSITY_HIGH:
				iconWidth = 98;
				iconHeight = 98;
				 break;

			case DisplayMetrics.DENSITY_XHIGH:
				iconWidth = 130;
				iconHeight = 130;
				 break;
		}
		
		if(!commentObj.get("image").equals("")) {
			imageLoader.displayImage(commentObj.get("image"), image, options, new ImageLoadingListener() {
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
		}
		image.getLayoutParams().height = iconHeight;
    	image.getLayoutParams().width = iconWidth;
		image.setVisibility(View.VISIBLE);
		
		user.setText(commentObj.get("user").toUpperCase(Locale.ENGLISH));
		user.setTypeface(tfRegular, Typeface.BOLD);
		comment.setText(commentObj.get("comment"));
		comment.setTypeface(tfRegular);
		by.setTypeface(tfRegular, Typeface.BOLD);
		ago.setTypeface(tfRegular, Typeface.BOLD);
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		formatter.setLenient(false);
		Date oldDate = null;
		try {
			oldDate = formatter.parse(commentObj.get("date"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		PrettyTime p = new PrettyTime();
		date.setText(p.format(oldDate).toUpperCase());
		date.setTypeface(tfRegular, Typeface.BOLD);
		
		return vi;
	}

}
