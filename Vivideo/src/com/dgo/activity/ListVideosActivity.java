package com.dgo.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dgo.R;

public class ListVideosActivity extends ListActivity {
	private List<String> videosList = new ArrayList<String>();
	private List<File> videoFileList = new ArrayList<File>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_videos);

        ListView lv = getListView();
        File dir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM + "/" + getString(R.string.app_name));
	    String pattern = "." + getString(R.string.video_format);

	    final File listFile[] = dir.listFiles();

	    if (listFile != null) {
	        for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].getName().endsWith(pattern)) {
                   videosList.add(listFile[i].getName());
                   videoFileList.add(listFile[i]);
                }
	        }
	    }
	    
        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(this, R.layout.list_item, R.id.label, videosList);
        this.setListAdapter(adapter);

        lv.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    try {
                    	Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                	    File file = videoFileList.get(position); 
                	    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                	    String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                	    intent.setDataAndType(Uri.fromFile(file),mimetype);
                	    startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
            }
        );
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
