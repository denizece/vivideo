package com.dgo.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dgo.R;
import com.dgo.video.EffectsListAndDuration;
import com.dgo.video.EffectsUtil;
import com.dgo.video.EffectsUtil.VivideoEffect;
import com.dgo.video.RecorderView;

public class MainActivity extends Activity {

	public final static int CALL_MUSIC_ACTIVITY_ID = 1;
	public final static int CALL_PREPARED_LIST_ACTIVITY_ID = 2;
	public final static String SELECTED_MUSIC_DATA = "SELECTED_MUSIC_DATA";
	public final static String LIST_OF_SELECTED_EFFECTS = "LIST_OF_SELECTED_EFFECTS";
	public final static String LIST_OF_SUPPORTED_EFFECTS = "LIST_OF_SUPPORTED_EFFECTS";
	
	public final String BACKGROUND_COLOR_SELECTED_EFFECT ="#FFFFFF";
	public final String BACKGROUND_COLOR_NOTSELECTED_EFFECT ="#682860";

	private Button recordButton;
	private Button musicBrowseButton;
	private Button flashEnableButton;
	private Button settingsButton;
	private Button videosButton;
	private Button pauseButton;
	private RecorderView surfaceView = null;
	private boolean recording = false;
	private boolean pause = false;
	private boolean flashEnabled = false;
	private String pathToSong;
	private MediaPlayer mediaPlayer;
	private int timerDelay = 0;
	private int currentCameraId = 0;
	private boolean frontCameraAvailable = false;
	private CountDownTimer countDownTimer;
	private Chronometer recordTimeChronometer;
	private long timeWhenRecordingPaused = 0;
	private View lastEffectSelected;

	public ArrayList<String> selectedListofEffects = new ArrayList<String>();
	public int durationForListofEffects = -1;
	public ArrayList<String> supportedEffects = new ArrayList<String>();
	private boolean effectListPrepared = false;
	private CountDownTimer countDownTimerPreparedEffect;
	private int currentPreparedEffect = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);
		initializeWidgets();
		
		int numCameras = Camera.getNumberOfCameras();
		if (numCameras > 1) {
			frontCameraAvailable = true;
		}
	}
	
	private void initializeWidgets(){
		recordButton = (Button) findViewById(R.id.startRecord);

		musicBrowseButton = (Button) findViewById(R.id.music);

		flashEnableButton = (Button) findViewById(R.id.flash);

		settingsButton = (Button) findViewById(R.id.settings);

		videosButton = (Button) findViewById(R.id.videos);

		pauseButton = (Button) findViewById(R.id.pause);
		pauseButton.setVisibility(View.GONE);

		recordTimeChronometer = (Chronometer) findViewById(R.id.chronometer);
		recordTimeChronometer.setVisibility(View.INVISIBLE);
		
		surfaceView = (RecorderView) findViewById(R.id.preview);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		surfaceView.getLayoutParams().width = metrics.widthPixels * 3 / 5;
		surfaceView.getLayoutParams().height = metrics.heightPixels * 3 / 5;
	}
	
	private OnClickListener effectOnclickListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onEffectClick(v);
			}
		};
	}
	
	public void onEffectClick(View v) {
		Parameters params = surfaceView.getmCamera().getParameters();
		if (lastEffectSelected.getId() != v.getId()) {
			((TextView) v).setTextColor(Color.parseColor(BACKGROUND_COLOR_SELECTED_EFFECT));
			((TextView) lastEffectSelected).setTextColor(Color.parseColor(BACKGROUND_COLOR_NOTSELECTED_EFFECT));
			lastEffectSelected = v;
		}
		
		params.setColorEffect(Camera.Parameters.EFFECT_NONE);
		surfaceView.setEffectApplied(VivideoEffect.effect_none);
		if(EffectsUtil.isAndroidEffect(v.getId())){
			String[] andrEfs = getResources().getStringArray(R.array.androidEffectsList);
			params.setColorEffect(andrEfs[v.getId()]);
		} else {
			surfaceView.setEffectApplied(VivideoEffect.values()[v.getId()]);
			surfaceView.setGifCounter(1);
		}
		surfaceView.getmCamera().setParameters(params);
	}

	private void fillSupportedEffects() {
		Parameters params = surfaceView.getmCamera().getParameters();
		supportedEffects.clear();
		if (params != null) {
			List<String> supef = params.getSupportedColorEffects();
			String[] andrEfs = getResources().getStringArray(R.array.androidEffectsList);
			for (int i = 0; i < andrEfs.length; i++) {
				if (supef.contains(andrEfs[i])) {
					supportedEffects.add(andrEfs[i]);
				}
			}
			String[] vividEfs = getResources().getStringArray(R.array.vivideoEffectsList);
			for (int i = 0; i < vividEfs.length; i++) {
				supportedEffects.add(vividEfs[i]);
			}
		}
		
		LinearLayout effectListLayout = (LinearLayout) findViewById(R.id.effect_list_layout);
		if(effectListLayout.getChildCount() > 0) {
		    effectListLayout.removeAllViews(); 
		}
		for(String effectName:supportedEffects){
			LayoutParams lparams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			int stringid = getResources().getIdentifier("effect_" + effectName, "string", getPackageName());
			TextView tv = new TextView(this);
			final float scale = getResources().getDisplayMetrics().density;
			int pixels = (int) (100 * scale + 0.5f);
			tv.setWidth(pixels);
			tv.setLayoutParams(lparams);
			tv.setText(getString(stringid));
			tv.setBackgroundResource(R.drawable.effect_background);
			tv.setTextColor(Color.parseColor(BACKGROUND_COLOR_NOTSELECTED_EFFECT));
			tv.setGravity(Gravity.CENTER);
			tv.setClickable(true);
			tv.setOnClickListener(effectOnclickListener());
			tv.setId(EffectsUtil.getIdFromName("effect_" + effectName));
			effectListLayout.addView(tv);
		}
	}

	public void onVideoClick(View v) {
		Intent intent = new Intent(MainActivity.this, ListVideosActivity.class);
		startActivity(intent);
	}

	public void onPauseClick(View v) {
		if (pause) {
			pauseButton.setBackgroundResource(R.drawable.pause);
			pause = false;
			if (mediaPlayer != null) {
				mediaPlayer.start();
			}
			surfaceView.resumeRecording();
			recordTimeChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenRecordingPaused);
			recordTimeChronometer.start();
		} else {
			pauseButton.setBackgroundResource(R.drawable.play);
			pause = true;
			if (mediaPlayer != null) {
				mediaPlayer.pause();
			}
			surfaceView.pauseRecording();
			timeWhenRecordingPaused = recordTimeChronometer.getBase() - SystemClock.elapsedRealtime();
			recordTimeChronometer.stop();
		}
	}

	public void onRecordClick(View v) {
		int resid;
		int buttonVisibility;
		if (recording) {
			if (countDownTimer != null) {
				countDownTimer.cancel();
			}

			if (countDownTimerPreparedEffect != null) {
				countDownTimerPreparedEffect.cancel();
			}

			if (recordTimeChronometer != null) {
				recordTimeChronometer.stop();
			}
			recordTimeChronometer.setVisibility(View.INVISIBLE);
			pauseButton.setVisibility(View.GONE);
			timeWhenRecordingPaused = 0;
			recordTimeChronometer.setBase(SystemClock.elapsedRealtime());

			TextView tv = (TextView) findViewById(R.id.timerValue);
			if (timerDelay != 0) {
				tv.setText(getString(R.string.timer) + timerDelay);
			} else {
				tv.setText("");
			}
			
			buttonVisibility = Button.VISIBLE;
			surfaceView.stopRecording();
			resid = R.drawable.record;
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
			}
		} else {
			resid = R.drawable.stop;
			buttonVisibility = Button.GONE;
			startTimerForRecording();
		}
		
		recording = !recording;
		musicBrowseButton.setVisibility(buttonVisibility);
		settingsButton.setVisibility(buttonVisibility);
		videosButton.setVisibility(buttonVisibility);
		recordButton.setBackgroundResource(resid);
	}

	public void startTimerForRecording() {
		countDownTimer = new CountDownTimer(timerDelay * 1000, 1000) {
			TextView tv = (TextView) findViewById(R.id.timerValue);

			public void onTick(long millisUntilFinished) {
				tv.setText(getString(R.string.timer) + millisUntilFinished / 1000);
			}

			public void onFinish() {
				pauseButton.setVisibility(View.VISIBLE);
				pause = false;
				pauseButton.setBackgroundResource(R.drawable.pause);
				tv.setText(getString(R.string.recording));
				surfaceView.startRecording();
				if (pathToSong != null && !pathToSong.equals("")) {
					try {
						mediaPlayer = new MediaPlayer();
						mediaPlayer.setDataSource(pathToSong);
						mediaPlayer.prepare();
						mediaPlayer.start();
					} catch (Exception e) {
					}
				}
				recordTimeChronometer.setBase(SystemClock.elapsedRealtime());
				recordTimeChronometer.setVisibility(View.VISIBLE);
				recordTimeChronometer.start();
				if (effectListPrepared) {
					changeEffectAutomatically();
				}
			}
		}.start();
	}

	private void changeEffectAutomatically() {
		countDownTimerPreparedEffect = new CountDownTimer(durationForListofEffects * 1000, 1000) {
			@Override
			public void onFinish() {
				currentPreparedEffect++;
				if (currentPreparedEffect == selectedListofEffects.size()) {
					currentPreparedEffect = 0;
				}
				String effectName = selectedListofEffects.get(currentPreparedEffect);
				View effectView = findViewById(EffectsUtil.getIdFromName("effect_" + effectName));
				onEffectClick(effectView);
				changeEffectAutomatically();
			}

			@Override
			public void onTick(long millisUntilFinished) {
			}
		}.start();
	}

	public void onMusicClick(View v) {
		Intent intent = new Intent(MainActivity.this, ListMusicActivity.class);
		startActivityForResult(intent, CALL_MUSIC_ACTIVITY_ID);
	}

	public void flipCamera(){
		if (frontCameraAvailable) {
			if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
				flashEnableButton.setVisibility(View.INVISIBLE);
				currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
			} else {
				flashEnableButton.setVisibility(View.VISIBLE);
				currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
			}
			surfaceView.changeCamera(currentCameraId);
			fillSupportedEffects();
			lastEffectSelected = (View) findViewById(VivideoEffect.effect_none.ordinal());
			onEffectClick(lastEffectSelected);
			((TextView) lastEffectSelected).setTextColor(Color.parseColor(BACKGROUND_COLOR_SELECTED_EFFECT));
		} else {
			new AlertDialog.Builder(MainActivity.this)
					.setTitle(getString(R.string.error))
					.setMessage(getString(R.string.front_camera_error_message))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which) {
								}
							}).show();
		}
	}
	
	public void listTimerOptions() {
		CharSequence[] items = { getString(R.string.no_timer), getString(R.string.five_seconds), getString(R.string.ten_seconds), getString(R.string.fifteen_seconds) };
		new AlertDialog.Builder(this).setTitle(getString(R.string.set_timer))
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						timerDelay = 5 * which;
						TextView tv = (TextView) findViewById(R.id.timerValue);
						if (timerDelay != 0) {
							tv.setText(getString(R.string.timer) + String.valueOf(timerDelay));
						} else {
							tv.setText("");
						}
					}
				}).show();
	}
	
	public void showPrepareListView(){
		Intent intent = new Intent(MainActivity.this, CreateListOfEffectsActivity.class);
		EffectsListAndDuration eflist = new EffectsListAndDuration(supportedEffects, 0);
		
		Bundle extras = new Bundle();
	    extras.putSerializable(MainActivity.LIST_OF_SUPPORTED_EFFECTS, eflist);
	    intent.putExtra(LIST_OF_SUPPORTED_EFFECTS, extras);
		startActivityForResult(intent, CALL_PREPARED_LIST_ACTIVITY_ID);
	}
	
	public void onOptionsClick(View v) {
		CharSequence[] items = { getString(R.string.set_timer), getString(R.string.flip_camera), getString(R.string.prepare_effect_list) };
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.options))
				.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								listTimerOptions();
								break;
							case 1:
								flipCamera();
								break;
							case 2:
								showPrepareListView();
								break;
							}
					}
				}).show();
			}

	public void onFlashClick(View v) {
		Camera.Parameters p = surfaceView.getmCamera().getParameters();
		if (flashEnabled) {
			p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			flashEnableButton.setBackgroundResource(R.drawable.flashon);
		} else {
			p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			flashEnableButton.setBackgroundResource(R.drawable.flashoff);
		}
		flashEnabled = !flashEnabled;
		surfaceView.getmCamera().setParameters(p);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case CALL_MUSIC_ACTIVITY_ID:
				if (resultCode == Activity.RESULT_OK) {
					pathToSong = data.getStringExtra(SELECTED_MUSIC_DATA);
					TextView tv = (TextView) findViewById(R.id.selectedMusic);
					tv.setText(pathToSong.split("/")[pathToSong.split("/").length - 1]);
				}
				break;
			case CALL_PREPARED_LIST_ACTIVITY_ID:
				boolean listNotPrepared = true;
				if (resultCode == Activity.RESULT_OK) {
					Bundle extras = data.getBundleExtra(LIST_OF_SELECTED_EFFECTS);
					EffectsListAndDuration effectList = (EffectsListAndDuration) extras.getSerializable(LIST_OF_SELECTED_EFFECTS);
					selectedListofEffects = effectList.getSelectedEffects();
					if(selectedListofEffects.size() > 0){
						durationForListofEffects = effectList.getDuration();
						effectListPrepared = true;
						View effectsSlider = findViewById(R.id.effects_scroll_view);
						effectsSlider.setVisibility(View.GONE);
						listNotPrepared = false;
					} 
				} 
				
				if(listNotPrepared){
					effectListPrepared = false;
					View effectsSlider = findViewById(R.id.effects_scroll_view);
					effectsSlider.setVisibility(View.VISIBLE);
				}
				break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		surfaceView.initializeCamera();
		fillSupportedEffects();
		if (effectListPrepared) {
			if (selectedListofEffects != null && selectedListofEffects.size() > 0) {
				String effectName = selectedListofEffects.get(0);
				lastEffectSelected = findViewById(EffectsUtil.getIdFromName("effect_" + effectName));
			}
		} else {
			if(lastEffectSelected == null){
				lastEffectSelected = (View) findViewById(VivideoEffect.effect_none.ordinal());
			}  else {
				lastEffectSelected = (View) findViewById(lastEffectSelected.getId());
			}
			((TextView) lastEffectSelected).setTextColor(Color.parseColor(BACKGROUND_COLOR_SELECTED_EFFECT));
		}
		flashEnableButton.setBackgroundResource(R.drawable.flashon);
		onEffectClick(lastEffectSelected);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (recording) {
			recording = false;
			surfaceView.stopRecording();
		}
		try {
    		surfaceView.stopPreview();
    		surfaceView.getmCamera().setPreviewCallback(null);
            surfaceView.getmCamera().release();
            surfaceView.setmCamera(null);
        } catch (RuntimeException e) {
		}
	}
}
