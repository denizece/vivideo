package com.dgo.video;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvAdd;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvFlip;
import static com.googlecode.javacv.cpp.opencv_core.cvMerge;
import static com.googlecode.javacv.cpp.opencv_core.cvRealScalar;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSet1D;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSplit;
import static com.googlecode.javacv.cpp.opencv_core.cvSub;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BLUR;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GRAY2BGR;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GRAY2RGB;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSobel;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;

import com.dgo.R;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


public class EffectsUtil {

	public static enum VivideoEffect {
		effect_none, effect_mono, effect_sepia, effect_negative, effect_aqua, effect_posterize, effect_whiteboard, effect_blackboard, 
		effect_solarize, effect_sobel, effect_four, effect_mirror, effect_tunel, effect_beach, effect_fishes, effect_stars, effect_snow,
		effect_bubbles, effect_three, effect_six, effect_flip_horizontal, effect_flip_vertical, effect_pixelize, effect_hearts,
		effect_blur, effect_red, effect_green, effect_blue, effect_four_color, effect_half_flip, effect_nine
	}
    
    public static boolean isAndroidEffect(int id){
    	return id < 9;
    }
    
    public static int getIdFromName(String effectName){
    	return VivideoEffect.valueOf(effectName).ordinal();
    }
    
    public static void overlayImage(IplImage mask, IplImage background){
    	IplImage r = IplImage.create(mask.width(), mask.height(), IPL_DEPTH_8U, 1);
    	IplImage g = IplImage.create(mask.width(),  mask.height(), IPL_DEPTH_8U, 1);
    	IplImage b = IplImage.create(mask.width(),  mask.height(), IPL_DEPTH_8U, 1);
    	IplImage a = IplImage.create(mask.width(),  mask.height(), IPL_DEPTH_8U, 1);
    	IplImage maskEmpty = IplImage.create(mask.width(),  mask.height(), IPL_DEPTH_8U, 4);
    	cvSplit(mask, r, g, b, a); 
    	cvSet1D(r, 0, cvRealScalar(255));
    	cvMerge(r, r, r, a, maskEmpty);
    	cvSub(background, maskEmpty, background, null);
    	cvAdd(mask, background, background, null);
    }
    
    public static List<Object> applyEffect(IplImage rgbimage, VivideoEffect effectApplied, 
    									int currentCameraId, int gifCounter, Context context){
    	int rgbaImageWidth = rgbimage.width(); 
		int rgbaImageHeight = rgbimage.height();
    	IplImage smallimage = null;
        Drawable dh = null;
        Bitmap bh = null;
        int imageToLoad = -1;
        IplImage maskimage = null;
        IplImage gray = null;
        if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
        	cvFlip(rgbimage, rgbimage, 1);
        }
        switch(effectApplied){
        	case effect_none:
        		break;
        	case effect_red:
            	rgbimage = cancelColorChannels(rgbimage, false, true, true);
        		break;
        	case effect_green:
        		rgbimage = cancelColorChannels(rgbimage, true, false, true);
        		break;
        	case effect_blue:
        		rgbimage = cancelColorChannels(rgbimage, true, true, false);
        		break;
        	case effect_four_color:
                gray = IplImage.create(rgbaImageWidth / 2, rgbaImageHeight / 2, IPL_DEPTH_8U, 1);
            	smallimage = IplImage.create(rgbaImageWidth / 2, rgbaImageHeight / 2, IPL_DEPTH_8U, 4);
        		cvResize(rgbimage, smallimage);
        		
        		cvSetImageROI(rgbimage, new CvRect(0, 0, rgbaImageWidth / 2, rgbaImageHeight / 2));
        		cvCopy(cancelColorChannels(smallimage.clone(), false, true, true), rgbimage);
        		
        		cvSetImageROI(rgbimage, new CvRect(rgbaImageWidth / 2, 0, rgbaImageWidth / 2, rgbaImageHeight / 2));
        		cvCopy(cancelColorChannels(smallimage.clone(), true, false, true), rgbimage);
        		
        		cvSetImageROI(rgbimage, new CvRect(0, rgbaImageHeight / 2, rgbaImageWidth / 2, rgbaImageHeight / 2));
        		cvCopy(cancelColorChannels(smallimage.clone(), true, true, false), rgbimage);
        		
        		cvCvtColor(smallimage, gray, CV_BGR2GRAY);
        		cvCvtColor(gray, smallimage, CV_GRAY2RGB);
        		cvSetImageROI(rgbimage, new CvRect(rgbaImageWidth / 2, rgbaImageHeight / 2, rgbaImageWidth / 2, rgbaImageHeight / 2));
        		cvCopy(smallimage, rgbimage);
        		cvResetImageROI(rgbimage);
        		break;	
        	case effect_sobel:
                gray = IplImage.create(rgbaImageWidth, rgbaImageHeight, IPL_DEPTH_8U, 1);
                cvCvtColor(rgbimage, gray, CV_BGR2GRAY);
                cvSobel(gray, gray, 1, 1, 5);
                cvCvtColor(gray, rgbimage, CV_GRAY2BGR);
        		break;
        	case effect_three:
        		rgbimage = tileXByY(rgbimage, 3, 1);
        		break;
        	case effect_four:
        		rgbimage = tileXByY(rgbimage, 2, 2);
        		break;
        	case effect_six:
        		rgbimage = tileXByY(rgbimage, 3, 2);
        		break;
        	case effect_nine:
        		rgbimage = tileXByY(rgbimage, 3, 3);
        		break;
        	case effect_mirror:
        		smallimage = IplImage.create(rgbaImageWidth / 2, rgbaImageHeight, IPL_DEPTH_8U, 4);
        		cvResize(rgbimage, smallimage);
        		cvSetImageROI(rgbimage, new CvRect(0, 0, rgbaImageWidth / 2, rgbaImageHeight));
        		cvCopy(smallimage, rgbimage);
        		cvSetImageROI(rgbimage, new CvRect(rgbaImageWidth / 2, 0, rgbaImageWidth / 2, rgbaImageHeight));
        		cvFlip(smallimage, smallimage, 1);
        		cvCopy(smallimage, rgbimage);
        		cvResetImageROI(rgbimage);
        		break;
        	case effect_half_flip:
        		cvSetImageROI(rgbimage, new CvRect(0, rgbaImageHeight / 2, rgbaImageWidth, rgbaImageHeight / 2));
        		cvFlip(rgbimage, rgbimage, 1);
        		cvResetImageROI(rgbimage);
        		break;
        	case effect_flip_horizontal:
        		cvFlip(rgbimage, rgbimage, 1);
        		break;	
        	case effect_flip_vertical:
        		cvFlip(rgbimage, rgbimage, 0);
        		break;		
        	case effect_blur:
        		cvSmooth(rgbimage, rgbimage, CV_BLUR, 7);
        		break;
        	case effect_pixelize:
        		smallimage = IplImage.create(rgbaImageWidth / 10, rgbaImageHeight / 10, IPL_DEPTH_8U, 4);
        		cvResize(rgbimage, smallimage);
        		cvResize(smallimage, rgbimage);
        		break;		
        	case effect_tunel:
        		int multiplier = 2;
        		for(int i = 1; i < 4; i++){
        			multiplier = (int) Math.pow(2, i);
        			int widthSmall = rgbaImageWidth * (20 - multiplier) / 20;
        			int heightSmall = rgbaImageHeight * (20 - multiplier) / 20;
            		int smallImageStartX = rgbaImageWidth * (multiplier / 2) / 20;
            		int smallImageStartY = rgbaImageHeight * (multiplier / 2) / 20;
        			smallimage = IplImage.create(widthSmall, heightSmall, IPL_DEPTH_8U, 4);
            		cvResize(rgbimage, smallimage);
            		cvSetImageROI(rgbimage, new CvRect(smallImageStartX, smallImageStartY, widthSmall, heightSmall));
            		cvCopy(smallimage, rgbimage);
            		cvResetImageROI(rgbimage);
        		}
        		break;
        	case effect_beach:
        		dh = context.getResources().getDrawable(R.drawable.beach); 
        		break;
        	case effect_hearts:
        		dh = context.getResources().getDrawable(R.drawable.hearts); 
        		break;	
        	case effect_fishes:
        		imageToLoad = context.getResources().getIdentifier("fishes" + String.valueOf(gifCounter), 
        																"drawable", context.getPackageName());
        		gifCounter++;
        		if(gifCounter == 11) gifCounter = 1;
            	dh = context.getResources().getDrawable(imageToLoad); 
        		break;	
        	case effect_stars:
        		imageToLoad = context.getResources().getIdentifier("stars" + String.valueOf(gifCounter), 
																		"drawable", context.getPackageName());
        		gifCounter++;
        		if(gifCounter == 4) gifCounter = 1;
            	dh = context.getResources().getDrawable(imageToLoad); 
        		break;	
        	case effect_snow:
        		imageToLoad = context.getResources().getIdentifier("snow" + String.valueOf(gifCounter), 
																		"drawable", context.getPackageName());
        		gifCounter++;
        		if(gifCounter == 10) gifCounter = 1;
            	dh = context.getResources().getDrawable(imageToLoad); 
        		break;	
        	case effect_bubbles:
        		imageToLoad = context.getResources().getIdentifier("bubbles" + String.valueOf(gifCounter), 
																		"drawable", context.getPackageName());
        		gifCounter++;
        		if(gifCounter == 13) gifCounter = 1;
            	dh = context.getResources().getDrawable(imageToLoad); 
        		break;	
        }
        
        if(dh != null){
		    bh =((BitmapDrawable)dh).getBitmap(); 
		    bh = Bitmap.createScaledBitmap(bh, rgbimage.width(), rgbimage.height(), true);
    		maskimage = IplImage.create(bh.getWidth(), bh.getHeight(), IPL_DEPTH_8U,4);
    		bh.copyPixelsToBuffer(maskimage.getByteBuffer());
    		bh.recycle();
    		overlayImage(maskimage, rgbimage);
        }
        addWatermark(rgbimage, context);
        List<Object> result = new ArrayList<Object>();
        result.add(rgbimage);
        result.add(gifCounter);
        return result;
	}
    
    private static IplImage addWatermark(IplImage background, Context context){
    	Drawable dh = context.getResources().getDrawable(R.drawable.watermark); 
	    Bitmap bh =((BitmapDrawable)dh).getBitmap(); 
		IplImage watermark = IplImage.create(bh.getWidth(), bh.getHeight(), IPL_DEPTH_8U, 4);
		bh.copyPixelsToBuffer(watermark.getByteBuffer());
    	cvSetImageROI(background, new CvRect(background.width() - watermark.width(), 0, watermark.width(), watermark.height()));
    	overlayImage(watermark, background);
    	cvResetImageROI(background);
    	return background;
    }
    
    private static IplImage tileXByY(IplImage image, int columns, int rows){
    	int height = image.height() / rows;
		int width = image.width() / columns;
		IplImage smallimage = IplImage.create(width, height, IPL_DEPTH_8U, 4);
		cvResize(image, smallimage);
		for(int x = 0; x < columns; x++){
			for(int y = 0; y < rows; y ++){
				cvSetImageROI(image, new CvRect(x * width, y * height, width, height));
        		cvCopy(smallimage, image);
			}
		}
		cvResetImageROI(image);
		return image;
    }
    
    private static IplImage cancelColorChannels(IplImage image, boolean ch1, boolean ch2, boolean ch3){
    	IplImage r = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);
    	IplImage g = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);
    	IplImage b = IplImage.create(image.width(), image.height(), IPL_DEPTH_8U, 1);
    	cvSplit(image, r, g, b, null); 
    	if(ch1){
    		cvZero(r);
    	}
    	if(ch2){
        	cvZero(g);
    	}
    	if(ch3){
    		cvZero(b);
    	}
		cvMerge(r,g,b,null,image);
		
		return image;
    }
}
