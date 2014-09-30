package com.dgo.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.dgo.R;
import com.dgo.video.EffectsListAndDuration;

public class CreateListOfEffectsActivity extends ListActivity implements OnItemSelectedListener{
	private ArrayList<String> selectedEffects;
	private ArrayList<String> supportedEffects;
	private int selectedDuration = -1;
	private Spinner durationSpinner=null;
	public static EffectsListAndDuration eflist = null;
	
	private ListView selectedEffectsListView=null;
	
	private ArrayAdapter<String> supportedEffectsAdapter;
	private ArrayAdapter<String> listedEffectsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		selectedEffects=new ArrayList<String>();
		
		Intent intent = getIntent();
		supportedEffects = ((EffectsListAndDuration) intent.getBundleExtra(MainActivity.LIST_OF_SUPPORTED_EFFECTS).
														getSerializable(MainActivity.LIST_OF_SUPPORTED_EFFECTS)).getSelectedEffects();
		
		setContentView(R.layout.activity_create_list_of_effects);
		
		durationSpinner = (Spinner) findViewById(R.id.number_picker);
		ArrayAdapter<CharSequence> spinneradapter = ArrayAdapter.createFromResource(this, R.array.duration_array, android.R.layout.simple_spinner_item);
		spinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		durationSpinner.setAdapter(spinneradapter);
					
        ListView supportedEffectListView =  getListView();
        TextView headerSupported = new TextView(this);
        headerSupported.setText("Supported Effects");
        headerSupported.setBackgroundResource(R.drawable.effect_background);
        supportedEffectListView.addHeaderView(headerSupported);
        supportedEffectsAdapter=new ArrayAdapter<String>(this, R.layout.list_item, R.id.label, supportedEffects);
        supportedEffectListView.setAdapter(supportedEffectsAdapter);
        
        selectedEffectsListView = (ListView) findViewById (R.id.selected_effect_list);
        TextView headerSelected = new TextView(this);
        headerSelected.setText("Selected Effects");
        headerSelected.setBackgroundResource(R.drawable.effect_background);
        selectedEffectsListView.addHeaderView(headerSelected);
        listedEffectsAdapter=new ArrayAdapter<String>(this, R.layout.list_item, R.id.label, selectedEffects);
		selectedEffectsListView.setAdapter(listedEffectsAdapter);
        
        
        supportedEffectListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    	if(position - 1 >= 0){  
							String selectedEffect = supportedEffectsAdapter.getItem(position - 1);
							if (selectedEffect != null && !selectedEffect.equals("")) {
		                  		selectedEffects.add(selectedEffect);
		                  		listedEffectsAdapter.notifyDataSetChanged();
		                    }
                    	}
                    }
                }
            );
        selectedEffectsListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						 if(position - 1 < selectedEffects.size() && position - 1 >= 0){ 
	                    	  selectedEffects.remove(position - 1);
	              			  listedEffectsAdapter.notifyDataSetChanged();
						 }
                    }
                }
            );

	}

	public void onCancelClick(View view){
		finish();
	}
	
	public void onDoneClick(View view){
		if(selectedDuration == -1){
			selectedDuration = 5;
		}
		eflist = new EffectsListAndDuration(selectedEffects, selectedDuration);
		Intent resultIntent = new Intent();
		Bundle extras = new Bundle();
	    extras.putSerializable(MainActivity.LIST_OF_SELECTED_EFFECTS, eflist);
		resultIntent.putExtra(MainActivity.LIST_OF_SELECTED_EFFECTS, extras);
		setResult(Activity.RESULT_OK, resultIntent);	
		finish();
	}

	@Override
	public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id) {
		selectedDuration = Integer.parseInt((String) adapter.getItemAtPosition(pos));
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

}
