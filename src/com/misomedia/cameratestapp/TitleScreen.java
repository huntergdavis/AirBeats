package com.misomedia.cameratestapp;


import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;


public class TitleScreen extends Activity implements MediaPlayer.OnCompletionListener{
	MediaPlayer mediaPlayer;
   
    @Override
    protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
        
     // Set window fullscreen and remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.titlescreen);
       
		// at this point the layout should be inflated, so
		// maximize the title screen logo here
        ImageView imageView = (ImageView) findViewById(R.id.titlescreen);
		imageView.setImageResource(R.drawable.loading);
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		
		OnClickListener buttonClick = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		};
		
		imageView.setOnClickListener(buttonClick);
		
		mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.whoscream);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.start();
		
		//TitleCountDown localTitleCounter = new TitleCountDown(4000,1000);
		//localTitleCounter.start();
		
		
    }
    
    public void onCompletion(MediaPlayer arg0) {
		//finish();
    	ImageView titleScreenImage = (ImageView) findViewById(R.id.titlescreen);
    	titleScreenImage.setImageResource(R.drawable.filelist);
    }
    /*
  //countdowntimer is an abstract class, so extend it and fill in methods
    public class TitleCountDown extends CountDownTimer{
    public TitleCountDown(long millisInFuture, long countDownInterval) {
    super(millisInFuture, countDownInterval);
    }
    @Override
    public void onFinish() {
    finish();
    }
    @Override
    public void onTick(long millisUntilFinished) {
    
    }
    }
 */
}
