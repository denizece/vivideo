package com.dgo.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.dgo.R;
import com.dgo.music.Music;
import com.dgo.music.MusicDataGetter;

public class ListMusicActivity extends ListActivity {
    private CharSequence searchFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_music);

        ListView lv = getListView();
        final MusicDataGetter myMDG=new MusicDataGetter();
        final ArrayList<Music> mp3s=myMDG.getMusics(this);
        ArrayList<String> names=myMDG.getListofNames(mp3s);

        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.list_item, R.id.label, names);
        this.setListAdapter(adapter);

        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                adapter.getFilter().filter(cs);
                setSearchFilter(cs);
            }

			@Override
			public void afterTextChanged(Editable arg0) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
        });

        lv.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	  Music selected = myMDG.getMusicByName(adapter.getItem(position),mp3s);
                      if (selected!=null) {
                          Intent resultIntent = new Intent();
                          resultIntent.putExtra(MainActivity.SELECTED_MUSIC_DATA, selected.data);
                          setResult(Activity.RESULT_OK, resultIntent);
                      }
                      finish();
                }
            }
        );
	}

	public CharSequence getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(CharSequence searchFilter) {
		this.searchFilter = searchFilter;
	}
}
