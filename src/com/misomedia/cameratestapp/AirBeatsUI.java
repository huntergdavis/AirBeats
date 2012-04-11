package com.misomedia.cameratestapp;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;


public class AirBeatsUI extends Activity implements
		CameraCallback {
	private FrameLayout cameraholder = null;
	private CameraSurface camerasurface = null;
	private SoundManager soundManager;
	private GLSurfaceView glView;

 
	private long[] lastThresholdTime = new long[6];
	private long[] thisThresholdTime = new long[6];
	private double currentSecond = 0;
	private HappyTogether ourSong;

	final static long INTERVAL = 100;
	final static long TIMEOUT = 100;
	final static long RESETINTERVAL = 200;
	long elapsed;
	Timer timer;
	Timer drumTimer1;
	Timer drumTimer2;
	Timer drumTimer3;
	Timer drumTimer4;
	Timer drumTimer5;
	Timer drumTimer6;
	
	// our song countdown timer
	// TitleCountDown songCountDown;

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == UiThreadMessages.SCRATCH.value()) {

			}
			super.handleMessage(msg);
		}
	};

	@Override
	public void onDestroy() {
		soundManager.closeSoundPool();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		for (int i = 0; i < 6; i++) {
			thisThresholdTime[i] = 0;
		}
		currentSecond = 0;
		elapsed = 0;
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Éand the notification bar. That way, we can use the full screen.
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);
		cameraholder = (FrameLayout) findViewById(R.id.camera_preview);

		setupPictureMode();

		// create the new intent
		Intent it = new Intent(this, TitleScreen.class);
		// start activity.
		startActivity(it);

		
		soundManager = new SoundManager(this);
		ourSong = new HappyTogether();

		// songCountDown = new TitleCountDown(515000, 200);

		timer = new Timer();
		drumTimer1 = new Timer();
		drumTimer2 = new Timer();
		drumTimer3 = new Timer();
		drumTimer4 = new Timer(); 
		drumTimer5 = new Timer();
		drumTimer6 = new Timer();
		
		

		// ((ImageButton)findViewById(R.id.takepicture)).setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.Drum1))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.Drum2))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.Drum3))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.Drum4))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.Drum5))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.Drum6))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.PlayPause))
				.setOnClickListener(onButtonClick);
		((ImageButton) findViewById(R.id.Reset))
		.setOnClickListener(onButtonClick);
	}
	
	private void clearNotes() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switchDrums(1, 10);
				switchDrums(2, 10);
				switchDrums(3, 10);
				switchDrums(4, 10);
				switchDrums(5, 10);
				switchDrums(6, 10);
			}
		});
	}

	private void processNotes() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				currentSecond += 0.1;
				int ourSongpos = ourSong.GetAtNextPosition(currentSecond);
				processCurrentNotes(ourSongpos, currentSecond);
			}
		});
	}

	private void setupPictureMode() {
		camerasurface = new CameraSurface(this);

		cameraholder.addView(camerasurface, new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		camerasurface.setCallback(this);
	}

	@Override
	public void onJpegPictureTaken(byte[] data, Camera camera) {
		try {
			FileOutputStream outStream = new FileOutputStream(String.format(
					"/sdcard/%d.jpg", System.currentTimeMillis()));

			outStream.write(data);
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		camerasurface.startPreview();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// Log.d("preview frame called","preview frame!");
		int width = camera.getParameters().getPreviewSize().width;
		int height = camera.getParameters().getPreviewSize().height;
		YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height,
				null);
		Rect rect = new Rect(0, 0, width, height);
		ByteArrayOutputStream output_stream = new ByteArrayOutputStream();
		yuvimage.compressToJpeg(rect, 100, output_stream);
		Bitmap imagePreviewBitmap = BitmapFactory.decodeByteArray(
				output_stream.toByteArray(), 0, output_stream.size());

		// ((ImageButton)
		// findViewById(R.id.Drum4)).setImageBitmap(imagePreviewBitmap);
		for (int i = 0; i < 6; i++) {
			thisThresholdTime[i] = System.currentTimeMillis();
			if ((thisThresholdTime[i] - lastThresholdTime[i]) > 200) {
				if (pixelDetection(imagePreviewBitmap, i)) {
					switchDrums(i + 1, 11);
					soundManager.playDrumNumber(i + 1, getBaseContext());
					setTimerForThisDrum(i);
					lastThresholdTime[i] = System.currentTimeMillis();
				}
			}
		}

	}
	
	public void setTimerForThisDrum(int drumNumber) {
		// set up the .5 second freeplay timer
		TimerTask timer1Task = new TimerTask() {
			@Override
			public void run() {
				elapsed += INTERVAL;
				if (elapsed >= TIMEOUT) {
					clearNotes();
					drumTimer1.cancel();
				}
			} 
		};
		TimerTask timer2Task = new TimerTask() {
			@Override
			public void run() {
				elapsed += INTERVAL;
				if (elapsed >= TIMEOUT) {
					clearNotes();
					drumTimer2.cancel();
				}
			} 
		};
		TimerTask timer3Task = new TimerTask() {
			@Override
			public void run() {
				elapsed += INTERVAL;
				if (elapsed >= TIMEOUT) {
					clearNotes();
					drumTimer3.cancel();
				}
			} 
		};
		TimerTask timer4Task = new TimerTask() {
			@Override
			public void run() {
				elapsed += INTERVAL;
				if (elapsed >= TIMEOUT) {
					clearNotes();
					drumTimer4.cancel();
				}
			} 
		};
		TimerTask timer5Task = new TimerTask() {
			@Override
			public void run() {
				elapsed += INTERVAL;
				if (elapsed >= TIMEOUT) {
					clearNotes();
					drumTimer5.cancel();
				}
			} 
		};
		TimerTask timer6Task = new TimerTask() {
			@Override
			public void run() {
				elapsed += INTERVAL;
				if (elapsed >= TIMEOUT) {
					clearNotes();
					drumTimer6.cancel();
				}
			} 
		};
		
		switch (drumNumber) {
		case 1:
			drumTimer1 = new Timer();
			drumTimer1.schedule(timer1Task, RESETINTERVAL);
			break;
		case 2:
			drumTimer2 = new Timer();
			drumTimer2.schedule(timer2Task, RESETINTERVAL);
			break;

		case 3:
			drumTimer3 = new Timer();
			drumTimer3.schedule(timer3Task, RESETINTERVAL);
			break;

		case 4:
			drumTimer4 = new Timer();
			drumTimer4.schedule(timer4Task, RESETINTERVAL);
			break;

		case 5:
			drumTimer5 = new Timer();
			drumTimer5.schedule(timer5Task, RESETINTERVAL);
			break;

		case 6:
			drumTimer6 = new Timer();
			drumTimer6.schedule(timer6Task, RESETINTERVAL);
			break;
		default:
			break;
		}
		
	

	}

	public Boolean pixelDetection(Bitmap bmp, int quadrant) {
		// hard coding stupid quadrants value -- @heena

		int[] quad_x;
		int[] quad_y;
		int brightenOffset = 70;

		quad_x = new int[6];
		quad_y = new int[6];

		quad_x[0] = 0;
		quad_y[0] = 120;
		quad_x[1] = 80;
		quad_y[1] = 120;
		quad_x[2] = 160;
		quad_y[2] = 120;
		quad_x[3] = 240;
		quad_y[3] = 120;
		quad_x[4] = 0;
		quad_y[4] = 0;
		quad_x[5] = 240;
		quad_y[5] = 0;

		int selected_quad = quadrant;
		int pixel_value;

		double eucledianDist;
		double eucledianDistThreshold = 180.0;

		// Looping through the quad and getting pixel value : babystep #1
		for (int y = quad_y[selected_quad]; y < (quad_y[selected_quad] + 120); y = y + 2) {
			for (int x = quad_x[selected_quad]; x < (quad_x[selected_quad] + 80); x++) {
				pixel_value = bmp.getPixel(x, y);
				int r = (pixel_value >> 16) & 0xff;
				int g = (pixel_value >> 8) & 0xff;
				int b = pixel_value & 0xff;
				r = Math.max(0, Math.min(255, r + brightenOffset));
				g = Math.max(0, Math.min(255, g + brightenOffset));
				b = Math.max(0, Math.min(255, b + brightenOffset));

				eucledianDist = calculateEucledianDist(r, g, b);

				if (eucledianDist < eucledianDistThreshold) {
					return true;
				}
			}
		}
		return false;
	}

	// functions involving pixel detection
	public double calculateEucledianDist(double r, double g, double b) {
		double eucledianDist = 0.0;

		double red_d = Math.pow((r - 0.0), 2.0);
		double green_d = Math.pow((g - 255.0), 2.0);
		double blue_d = Math.pow((b - 0.0), 2.0);
		eucledianDist = Math.sqrt(red_d + green_d + blue_d);
		return eucledianDist;

	}

	public boolean doRadialTest(int x, int y, Bitmap bmp) {
		int pixel;
		int r_comp;
		int g_comp;
		int b_comp;
		double eucledianDist;
		double eucledianDistThreshold = 10;
		for (int i = y - 1; i <= y + 1; i++) {
			for (int j = x - 1; j <= x + 1; j++) {
				pixel = bmp.getPixel(j, i);
				r_comp = Color.red(pixel);
				g_comp = Color.green(pixel);
				b_comp = Color.blue(pixel);
				eucledianDist = calculateEucledianDist(r_comp, g_comp, b_comp);
				if (eucledianDist < eucledianDistThreshold) {
					// we have found another pixel
					return true;
				}

			}
		}
		return false;

	}

	@Override
	public void onRawPictureTaken(byte[] data, Camera camera) {
	}

	@Override
	public void onShutter() {
	}


	public void processCurrentNotes(int currentNote, double currentSecond) {
		double currentSecondPart = currentSecond - Math.floor(currentSecond);
		currentSecondPart *= 10; 
		int currentIntegerSecondPart = (int) Math.floor(currentSecondPart+ .0001);

		// Log.d("note="+currentNote,"second="+currentSecond+", integerpart="+currentIntegerSecondPart);

		switch (currentNote) {
		case 0:
				switchDrums(1, 10);
				switchDrums(2, 10);
				switchDrums(3, 10);
				switchDrums(4, 10);
				switchDrums(5, 10);
				switchDrums(6, 10);
			break;
		case 1:
			switchDrums(1, currentIntegerSecondPart);
			break;
		case 2:
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 3:
			switchDrums(3, currentIntegerSecondPart);
			break;
		case 4:
			switchDrums(4, currentIntegerSecondPart);
			break;
		case 5:
			switchDrums(5, currentIntegerSecondPart);
			break;
		case 6:
			switchDrums(6, currentIntegerSecondPart);
			break;
		case 12:
			switchDrums(1, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 13:
			switchDrums(1, currentIntegerSecondPart);
			switchDrums(3, currentIntegerSecondPart);
			break;
		case 14:
			switchDrums(1, currentIntegerSecondPart);
			switchDrums(4, currentIntegerSecondPart);
			break;
		case 15:

			switchDrums(1, currentIntegerSecondPart);
			switchDrums(5, currentIntegerSecondPart);
			break;
		case 16:

			switchDrums(1, currentIntegerSecondPart);
			switchDrums(6, currentIntegerSecondPart);
			break;
		case 21:

			switchDrums(1, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 23:

			switchDrums(1, currentIntegerSecondPart);
			switchDrums(3, currentIntegerSecondPart);
			break;
		case 24:

			switchDrums(4, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 25:

			switchDrums(5, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 26:

			switchDrums(6, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 31:

			switchDrums(1, currentIntegerSecondPart);
			switchDrums(3, currentIntegerSecondPart);
			break;
		case 32:

			switchDrums(3, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 34:

			switchDrums(3, currentIntegerSecondPart);
			switchDrums(4, currentIntegerSecondPart);
			break;
		case 35:

			switchDrums(3, currentIntegerSecondPart);
			switchDrums(5, currentIntegerSecondPart);
			break;
		case 36:

			switchDrums(3, currentIntegerSecondPart);
			switchDrums(6, currentIntegerSecondPart);
			break;
		case 41:

			switchDrums(1, currentIntegerSecondPart);
			switchDrums(4, currentIntegerSecondPart);
			break;
		case 42:

			switchDrums(4, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 43:

			switchDrums(4, currentIntegerSecondPart);
			switchDrums(3, currentIntegerSecondPart);
			break;
		case 45:

			switchDrums(4, currentIntegerSecondPart);
			switchDrums(5, currentIntegerSecondPart);
			break;
		case 46:

			switchDrums(4, currentIntegerSecondPart);
			switchDrums(6, currentIntegerSecondPart);
			break;
		case 51:

			switchDrums(5, currentIntegerSecondPart);
			switchDrums(1, currentIntegerSecondPart);
			break;
		case 52:

			switchDrums(5, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 53:

			switchDrums(3, currentIntegerSecondPart);
			switchDrums(5, currentIntegerSecondPart);
			break;
		case 54:

			switchDrums(5, currentIntegerSecondPart);
			switchDrums(4, currentIntegerSecondPart);
			break;
		case 56:

			switchDrums(5, currentIntegerSecondPart);
			switchDrums(6, currentIntegerSecondPart);
			break;
		case 61:

			switchDrums(1, currentIntegerSecondPart);
			switchDrums(6, currentIntegerSecondPart);
			break;
		case 62:

			switchDrums(2, currentIntegerSecondPart);
			switchDrums(2, currentIntegerSecondPart);
			break;
		case 63:

			switchDrums(6, currentIntegerSecondPart);
			switchDrums(3, currentIntegerSecondPart);
			break;
		case 64:

			switchDrums(6, currentIntegerSecondPart);
			switchDrums(4, currentIntegerSecondPart);
			break;
		case 65:

			switchDrums(6, currentIntegerSecondPart);
			switchDrums(5, currentIntegerSecondPart);
			break;
		default:
			break;
		}
	}

	public void switchDrums(int drum, int framePos) {
		if (drum == 1) {
			switchDrumOne(framePos);
		} else if (drum == 2) {
			switchDrumTwo(framePos);
		} else if (drum == 3) {
			switchDrumThree(framePos);
		} else if (drum == 4) {
			switchDrumFour(framePos);
		} else if (drum == 5) {
			switchDrumFive(framePos);
		} else if (drum == 6) {
			switchDrumSix(framePos);
		}
	}

	public void switchDrumFive(int framePos) {
		ImageButton button = (ImageButton) findViewById(R.id.Drum5);
		int drumResourceImage = R.drawable.cymbal1_d;
		switch (framePos) {
		case 1:
		case 2:
			drumResourceImage = R.drawable.cymbal1_0;
			break;	
		case 3:
		case 4:
			drumResourceImage = R.drawable.cymbal1_1;
			break;
		case 5:
		case 6:
			drumResourceImage = R.drawable.cymbal1_2;
			break;
		case 7:
		case 8:
			drumResourceImage = R.drawable.cymbal1_3;
			break;
		case 9:
		case 10:
			drumResourceImage = R.drawable.cymbal1;
			break;
		case 11:
			drumResourceImage = R.drawable.cymbal1_d;
		default:
			break;
		}
		button.setBackgroundResource(drumResourceImage);
		button.invalidate();
	}

	public void switchDrumSix(int framePos) {
		ImageButton button = (ImageButton) findViewById(R.id.Drum6);
		int drumResourceImage = R.drawable.cymbal2_d;
		switch (framePos) {
		case 1:
		case 2:
			drumResourceImage = R.drawable.cymbal2_0;
			break;
		case 3:
		case 4:
			drumResourceImage = R.drawable.cymbal2_1;
			break;
		case 5:
		case 6:
			drumResourceImage = R.drawable.cymbal2_2;
			break;
		case 7:
		case 8:
			drumResourceImage = R.drawable.cymbal2_3;
			break;
		case 9:
		case 10:
			drumResourceImage = R.drawable.cymbal2;
			break;
		case 11:
			drumResourceImage = R.drawable.cymbal2_d;
		default:
			break;
		}
		button.setBackgroundResource(drumResourceImage);
		button.invalidate();
	}

	public void switchDrumOne(int framePos) {
		ImageButton button = (ImageButton) findViewById(R.id.Drum1);
		int drumResourceImage = R.drawable.drum4_d;
		switch (framePos) {
		case 1:
		case 2:
			drumResourceImage = R.drawable.drum4_0;
			break;
		case 3:
		case 4:
			drumResourceImage = R.drawable.drum4_1;
			break;
		case 5:
		case 6:
			drumResourceImage = R.drawable.drum4_2;
			break;
		case 7:
		case 8:
			drumResourceImage = R.drawable.drum4_3;
			break;
		case 9:
		case 10:
			drumResourceImage = R.drawable.drum4;
			break;
		case 11:
			drumResourceImage = R.drawable.drum4_d;
		default:
			break;
		}
		button.setBackgroundResource(drumResourceImage);
		button.invalidate();
	}

	public void switchDrumTwo(int framePos) {
		ImageButton button = (ImageButton) findViewById(R.id.Drum2);
		int drumResourceImage = R.drawable.drum5_d;
		switch (framePos) {
		case 1:
		case 2:
			drumResourceImage = R.drawable.drum5_0;
			break;
		case 3:
		case 4:
			drumResourceImage = R.drawable.drum5_1;
			break;
		case 5:
		case 6:
			drumResourceImage = R.drawable.drum5_2;
			break;
		case 7:
		case 8:
			drumResourceImage = R.drawable.drum5_3;
			break;
		case 9:
		case 10:
			drumResourceImage = R.drawable.drum5;
			break;
		case 11:
			drumResourceImage = R.drawable.drum5_d;
		default:
			break;
		}
		button.setBackgroundResource(drumResourceImage);
		button.invalidate();
	}

	public void switchDrumThree(int framePos) {
		ImageButton button = (ImageButton) findViewById(R.id.Drum3);
		int drumResourceImage = R.drawable.drum6_d;
		switch (framePos) {
		case 1:
		case 2:
			drumResourceImage = R.drawable.drum6_0;
			break;
		case 3:
		case 4:
			drumResourceImage = R.drawable.drum6_1;
			break;
		case 5:
		case 6:
			drumResourceImage = R.drawable.drum6_2;
			break;
		case 7:
		case 8:
			drumResourceImage = R.drawable.drum6_3;
			break;
		case 9:
		case 10:
			drumResourceImage = R.drawable.drum6;
			break;
		case 11:
			drumResourceImage = R.drawable.drum6_d;
		default:
			break;
		}
		button.setBackgroundResource(drumResourceImage);
		button.invalidate();
	}

	public void switchDrumFour(int framePos) {
		ImageButton button = (ImageButton) findViewById(R.id.Drum4);
		int drumResourceImage = R.drawable.drum7_d;
		switch (framePos) {
		case 1:
		case 2:
			drumResourceImage = R.drawable.drum7_0;
			break;
		case 3:
		case 4:
			drumResourceImage = R.drawable.drum7_1;
			break;
		case 5:
		case 6:
			drumResourceImage = R.drawable.drum7_2;
			break;
		case 7:
		case 8:
			drumResourceImage = R.drawable.drum7_3;
			break;
		case 9:
		case 10:
			drumResourceImage = R.drawable.drum7;
			break;
		case 11:
			drumResourceImage = R.drawable.drum7_d;
		default:
			break;
		}
		button.setBackgroundResource(drumResourceImage);
		button.invalidate();
	}

	@Override
	public String onGetVideoFilename() {
		String filename = String.format("/sdcard/%d.3gp",
				System.currentTimeMillis());

		return filename;
	}

	private void displayAboutDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(getString(R.string.app_name));
		builder.setMessage("Just an about dialog");

		builder.show();
	}

	private View.OnClickListener onButtonClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.Drum1: {

				soundManager.playDrumNumber(1, v.getContext());
				break;
			}
			case R.id.Drum2: {

				soundManager.playDrumNumber(2, v.getContext());
				break;
			}
			case R.id.Drum3: {

				soundManager.playDrumNumber(3, v.getContext());
				break;
			}
			case R.id.Drum4: {

				soundManager.playDrumNumber(4, v.getContext());
				break;
			}
			case R.id.Drum5: {

				//switchDrumFive(2);
				soundManager.playDrumNumber(5, v.getContext());
				break;
			}
			case R.id.Drum6: {

				soundManager.playDrumNumber(6, v.getContext());
				break;
			}
			case R.id.Reset: {
				soundManager.reset();
				soundManager.songPlaying = true;
					timer.cancel();
					currentSecond = 0;
					ImageButton playButton = (ImageButton) findViewById(R.id.PlayPause);
					playButton.setBackgroundResource(R.drawable.pause);
			}
			case R.id.PlayPause: {
				ImageButton playButton = (ImageButton) findViewById(R.id.PlayPause);
				if (soundManager.songPlaying) {
					soundManager.pauseSong();
					// songCountDown.cancel();
					timer.cancel();
					playButton.setBackgroundResource(R.drawable.play);
				} else {
					soundManager.playSong();
					
					TimerTask task = new TimerTask() {
						@Override
						public void run() {
							elapsed += INTERVAL;
							if (elapsed >= TIMEOUT) {
								processNotes();
								return;
							}
						} 
					};
					timer = new Timer();
					timer.scheduleAtFixedRate(task, INTERVAL, INTERVAL);
					
					// songCountDown.start();
					playButton.setBackgroundResource(R.drawable.pause);
				}
				break;
			}
			}
		}

	};
}