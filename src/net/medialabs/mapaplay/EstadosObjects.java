package net.medialabs.mapaplay;

import android.graphics.drawable.Drawable;

public class EstadosObjects {

	private String data;
	private Drawable[] backgrounds;
	
	public EstadosObjects(String data, Drawable[] backgrounds) {
		this.data = data;
		this.backgrounds = backgrounds;
	}
	
	public String getData() {
		return data;
	}
	
	public Drawable[] getBackgrounds() {
		return backgrounds;
	}
	
}
