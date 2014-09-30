package com.dgo.video;

import java.io.Serializable;
import java.util.ArrayList;

public class EffectsListAndDuration implements Serializable{
	private static final long serialVersionUID = 1L;
	private ArrayList<String> selectedEffects = new ArrayList<String>();
	private int duration;
	
	public EffectsListAndDuration(ArrayList<String> effects, int dur) {
		this.selectedEffects = effects;
		this.duration=dur;
	}
	
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public ArrayList<String> getSelectedEffects() {
		return selectedEffects;
	}

	public void setSelectedEffects(ArrayList<String> selectedEffects) {
		this.selectedEffects = selectedEffects;
	}

}
