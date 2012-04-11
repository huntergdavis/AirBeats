package com.misomedia.cameratestapp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback, OnGestureListener{	
	private Camera camera = null;
	private SurfaceHolder holder = null;
	private CameraCallback callback = null;
	private GestureDetector gesturedetector = null;
	
	public CameraSurface(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initialize(context);
	}

	public CameraSurface(Context context) {
		super(context);
		
		initialize(context);
	}

	public CameraSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initialize(context);
	}
	
	public void setCallback(CameraCallback callback){
		this.callback = callback;
	}
	
	public void startPreview(){
		camera.startPreview();
	}
	
	public void startTakePicture(){
	/*	camera.autoFocus(new AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				takePicture();
			}
		});
		*/
		takePicture();
	}
	
	public void takePicture() {
		camera.takePicture(
				new ShutterCallback() {
					@Override
					public void onShutter(){
						if(null != callback) callback.onShutter();
					}
				},
				new PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera camera){
						if(null != callback) callback.onRawPictureTaken(data, camera);
					}
				},
				new PictureCallback() {
					@Override
					public void onPictureTaken(byte[] data, Camera camera){
						if(null != callback) callback.onJpegPictureTaken(data, camera);
					}
				});
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {
		if(null != camera)
		{
			camera.startPreview();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		
		camera = openFrontFacingCamera();
		Parameters params = camera.getParameters();
		params.setFlashMode(Parameters.FOCUS_MODE_MACRO);
		camera.setParameters(params);
		
		try {
			camera.setPreviewDisplay(holder);
			camera.setPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] data, Camera camera) {
					if(null != callback) callback.onPreviewFrame(data, camera);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private Camera openFrontFacingCamera() {
	    Camera camera = null;
	    String TAG = "camera_test";
	 
	    // Look for front-facing camera, using the Gingerbread API.
	    // Java reflection is used for backwards compatibility with pre-Gingerbread APIs.
	    try {
	        Class<?> cameraClass = Class.forName("android.hardware.Camera");
	        Object cameraInfo = null;
	        Field field = null;
	        int cameraCount = 0;
	        Method getNumberOfCamerasMethod = cameraClass.getMethod( "getNumberOfCameras" );
	        if ( getNumberOfCamerasMethod != null ) {
	            cameraCount = (Integer) getNumberOfCamerasMethod.invoke( null, (Object[]) null );
	        }
	        Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
	        if ( cameraInfoClass != null ) {
	            cameraInfo = cameraInfoClass.newInstance();
	        }
	        if ( cameraInfo != null ) {
	            field = cameraInfo.getClass().getField( "facing" );
	        }
	        Method getCameraInfoMethod = cameraClass.getMethod( "getCameraInfo", Integer.TYPE, cameraInfoClass );
	        if ( getCameraInfoMethod != null && cameraInfoClass != null && field != null ) {
	            for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	                getCameraInfoMethod.invoke( null, camIdx, cameraInfo );
	                int facing = field.getInt( cameraInfo );
	                if ( facing == 1 ) { // Camera.CameraInfo.CAMERA_FACING_FRONT
	                    try {
	                        Method cameraOpenMethod = cameraClass.getMethod( "open", Integer.TYPE );
	                        if ( cameraOpenMethod != null ) {
	                            camera = (Camera) cameraOpenMethod.invoke( null, camIdx );
	                        }
	                    } catch (RuntimeException e) {
	                        //Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
	                    }
	                }
	            }
	        }
	    }
	    // Ignore the bevy of checked exceptions the Java Reflection API throws - if it fails, who cares.
	    catch ( ClassNotFoundException e        ) {Log.e(TAG, "ClassNotFoundException" + e.getLocalizedMessage());}
	    catch ( NoSuchMethodException e         ) {Log.e(TAG, "NoSuchMethodException" + e.getLocalizedMessage());}
	    catch ( NoSuchFieldException e          ) {Log.e(TAG, "NoSuchFieldException" + e.getLocalizedMessage());}
	    catch ( IllegalAccessException e        ) {Log.e(TAG, "IllegalAccessException" + e.getLocalizedMessage());}
	    catch ( InvocationTargetException e     ) {Log.e(TAG, "InvocationTargetException" + e.getLocalizedMessage());}
	    catch ( InstantiationException e        ) {Log.e(TAG, "InstantiationException" + e.getLocalizedMessage());}
	    catch ( SecurityException e             ) {Log.e(TAG, "SecurityException" + e.getLocalizedMessage());}
	 
	    if ( camera == null ) {
	        // Try using the pre-Gingerbread APIs to open the camera.
	        try {
	            camera = Camera.open();
	        } catch (RuntimeException e) {
	           
	        }
	    }
	 
	    return camera;
	}
	

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.setPreviewCallback(null);
		camera.release();
		
		camera = null;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gesturedetector.onTouchEvent(event);
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		startTakePicture();
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	private void initialize(Context context) {
		holder = getHolder();
		
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		gesturedetector = new GestureDetector(this);
	}
}