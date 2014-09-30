package com.dgo.video;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;

import java.io.File;
import java.io.IOException;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dgo.R;
import com.dgo.video.EffectsUtil.VivideoEffect;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class RecorderView extends SurfaceView implements SurfaceHolder.Callback {

	private boolean recording = false;
    private boolean pause	= false;
	private volatile FFmpegFrameRecorder recorder;
	private long startTime = 0;
	private int imageWidth = 0;
	private int imageHeight = 0;
	private long pauseDelay = 0;
	private long pauseStartTime = 0;
	public int rgbaImageWidth = 0;
	public int rgbaImageHeight = 0;
    private boolean isPreviewOn = false;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Bitmap bitmapToshow;
    private int gifCounter = 1;
    
    private AudioRecord audioRecord;
    private AudioRecordRunnable audioRecordRunnable;
    private Thread audioThread;
    volatile boolean runAudioThread = true;
    private int sampleAudioRateInHz = 44100;
    private int frameRate = 30;
    private VivideoEffect effectApplied = EffectsUtil.VivideoEffect.effect_none;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
   
	public RecorderView(Context context, AttributeSet atts) {
		super(context, atts);
		this.setWillNotDraw(false);
	}
	
	@SuppressWarnings("deprecation")
	public void initializeCamera(){
    	if(mCamera == null){
    		if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
    			mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
    		} else {
    			mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
    		}
    	} else {
    		try {
				mCamera.reconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	mHolder = getHolder();
		mHolder.addCallback(RecorderView.this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
	
	public void surfaceCreated(SurfaceHolder holder) {
		rgbaImageWidth = getWidth();
		rgbaImageHeight = getHeight();
		stopPreview();
    	try {
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
	private void calculateCameraFrameSize() {
		try {
		   Camera.Parameters camParams = mCamera.getParameters();
	       imageWidth = camParams.getPreviewSize().width;
	       imageHeight = camParams.getPreviewSize().height;
	       mCamera.setParameters(camParams);
		} catch(Exception e){
			e.printStackTrace();
		}
	      
       bitmapToshow = Bitmap.createBitmap(rgbaImageWidth, rgbaImageHeight, Bitmap.Config.ARGB_8888);
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    	mCamera.setPreviewCallback(getPreviewCallBack());
    	calculateCameraFrameSize();
    	startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
    
	public void changeCamera(int changeToCameraId){
    	int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    	currentCameraId = changeToCameraId;
    	if(changeToCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
    		cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    	} 
    	
    	stopPreview();
    	mCamera.setPreviewCallback(null);
    	mCamera.release();
    	mCamera = Camera.open(cameraId);
    	mHolder = getHolder();
		mHolder.addCallback(RecorderView.this);
    	mCamera.setPreviewCallback(getPreviewCallBack());
    	calculateCameraFrameSize();
    	startPreview();
    	try {
			mCamera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public PreviewCallback getPreviewCallBack(){
		return new PreviewCallback() {
			@Override
		    public void onPreviewFrame(byte[] data, Camera camera) {
		    	IplImage yuvimage = IplImage.create(imageWidth, imageHeight * 3 / 4, IPL_DEPTH_8U, 2); 
		        yuvimage.getByteBuffer().put(data);
		        IplImage yuvimage2 = IplImage.create(rgbaImageWidth, rgbaImageHeight * 3 / 2, IPL_DEPTH_8U, 2); 
		        cvResize(yuvimage, yuvimage2);
		        
		        IplImage rgbimage = IplImage.create(rgbaImageWidth, rgbaImageHeight, IPL_DEPTH_8U, 4);
		        cvCvtColor(yuvimage2, rgbimage, CV_YUV2BGR_NV12);
		        List<Object> result = EffectsUtil.applyEffect(rgbimage, effectApplied, currentCameraId, gifCounter, getContext());
		        rgbimage = (IplImage) result.get(0);
		        gifCounter = (Integer) result.get(1);
		        bitmapToshow.copyPixelsFromBuffer(rgbimage.getByteBuffer());
				postInvalidate();
		        if (recording  && !pause) {
		            try {
		                long t = 1000 * (System.currentTimeMillis() - pauseDelay - startTime);
		                if (t > recorder.getTimestamp()) {
		                    recorder.setTimestamp(t);
		                }
		                recorder.record(rgbimage);
		            } catch (FFmpegFrameRecorder.Exception e) {
		                e.printStackTrace();
		            }
		        }
		    }
		};
	}
	
    public void startPreview() {
        if (!isPreviewOn && mCamera != null) {
            isPreviewOn = true;
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        if (isPreviewOn && mCamera != null) {
            isPreviewOn = false;
            mCamera.stopPreview();
        }
    }

	public void onDraw(Canvas canvas) { 
    	if(bitmapToshow != null){
    		canvas.drawBitmap(bitmapToshow, 0, 0, null);
    	}
    } 

    public void startRecording() {

        try {
        	initRecorder();
            recorder.start();
            startTime = System.currentTimeMillis();
            pauseDelay = 0;
            pauseStartTime = 0;
            recording = true;
            pause = false;
            audioThread.start();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (recorder != null && recording) {
            runAudioThread = false;
        	recording = false;
        	pause = true;
            try {
                recorder.stop();
                recorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                e.printStackTrace();
            }
            recorder = null;
        }
    }
    
    public void pauseRecording(){
    	if (recording) {
        	pause = true;
        	pauseStartTime = System.currentTimeMillis();
        }
    }
    
    public void resumeRecording(){
    	if (pause) {
        	pause = false;
        	pauseDelay = pauseDelay + System.currentTimeMillis() - pauseStartTime;
        	pauseStartTime = 0;
        }
    }

    private void initRecorder() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM), getContext().getString(R.string.app_name));
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	        }
	    }
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALY).format(new Date());
	   
        recorder = new FFmpegFrameRecorder( mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + "." + 
        										getContext().getString(R.string.video_format) , rgbaImageWidth, rgbaImageHeight, 1);
        recorder.setFormat(getContext().getString(R.string.video_format));
        recorder.setSampleRate(sampleAudioRateInHz);
        recorder.setFrameRate(frameRate);

        audioRecordRunnable = new AudioRecordRunnable();
        audioThread = new Thread(audioRecordRunnable);
        runAudioThread = true;
    }
    
    class AudioRecordRunnable implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

            int bufferSize;
            short[] audioData;
            int bufferReadResult;

            bufferSize = AudioRecord.getMinBufferSize(sampleAudioRateInHz, 
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleAudioRateInHz, 
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

            audioData = new short[bufferSize];

            audioRecord.startRecording();

            while (runAudioThread) {
                bufferReadResult = audioRecord.read(audioData, 0, audioData.length);
	                if (bufferReadResult > 0) {
	                    if (recording && !pause) {
	                        try {
	                            recorder.record(ShortBuffer.wrap(audioData, 0, bufferReadResult));
	                        } catch (FFmpegFrameRecorder.Exception e) {
	                            e.printStackTrace();
	                        }
	                    } 
	                }
            	}

            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        }
    }
    
	public Camera getmCamera() {
		return mCamera;
	}

	public void setmCamera(Camera mCamera) {
		this.mCamera = mCamera;
	}

	public VivideoEffect getEffectApplied() {
		return effectApplied;
	}

	public void setEffectApplied(VivideoEffect effectApplied) {
		this.effectApplied = effectApplied;
	}

	public int getGifCounter() {
		return gifCounter;
	}

	public void setGifCounter(int gifCounter) {
		this.gifCounter = gifCounter;
	}

	public SurfaceHolder getmHolder() {
		return mHolder;
	}

	public void setmHolder(SurfaceHolder mHolder) {
		this.mHolder = mHolder;
	}
}