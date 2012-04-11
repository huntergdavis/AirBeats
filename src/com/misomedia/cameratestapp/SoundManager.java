package com.misomedia.cameratestapp;

import java.util.HashMap;

import com.misomedia.cameratestapp.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;

public class SoundManager implements MediaPlayer.OnCompletionListener{
	public static final int SOUND_SCRATCH = 1;
	public static final int DRUM_ONE = 2;
	public static final int DRUM_TWO = 3;
	public static final int DRUM_THREE = 4;
	public static final int DRUM_FOUR = 5;
	public static final int DRUM_FIVE = 6;
	public static final int DRUM_SIX = 7;

	private SoundPool soundPool;
	MediaPlayer mediaPlayer;
	private HashMap<Integer, Integer> soundPoolMap;
	private Handler soundHandler;

	public boolean songPlaying;

	public SoundManager(Context context) {
		initSounds(context);
		songPlaying = false;
	}

	private void initSounds(Context context) {
		soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap<Integer, Integer>();
		soundPoolMap.put(SOUND_SCRATCH,
				soundPool.load(context, R.raw.drum1, 1));
		soundPoolMap.put(DRUM_ONE, soundPool.load(context, R.raw.drum1, 2));
		soundPoolMap.put(DRUM_TWO, soundPool.load(context, R.raw.drum2, 3));
		soundPoolMap.put(DRUM_THREE, soundPool.load(context, R.raw.drum3, 4));
		soundPoolMap.put(DRUM_FOUR, soundPool.load(context, R.raw.drum4, 5));
		soundPoolMap.put(DRUM_FIVE, soundPool.load(context, R.raw.drum5, 6));
		soundPoolMap.put(DRUM_SIX, soundPool.load(context, R.raw.drum6, 7));

		mediaPlayer = MediaPlayer.create(context, R.raw.who);
		mediaPlayer.setOnCompletionListener(this);

	}

	public void playSong() {
		if (!mediaPlayer.isPlaying()) {
			songPlaying = true;
			mediaPlayer.start();
		}
	}

	public void pauseSong() {
		songPlaying = false;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}
	
	public void reset() {
		songPlaying = false;
		mediaPlayer.seekTo(0);
	}
	
    public void onCompletion(MediaPlayer arg0) {
		if (!mediaPlayer.isPlaying()) { 
			mediaPlayer.start();
		}
    }


	public void playSound(int sound, Context context) {
		/*
		 * Updated: The next 4 lines calculate the current volume in a scale of
		 * 0.0 to 1.0
		 */
		AudioManager mgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float streamVolumeMax = mgr
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		float volume = streamVolumeCurrent / streamVolumeMax;

		/* Play the sound with the correct volume */
		soundPool.play(soundPoolMap.get(sound), volume, volume, 1, 0, 1f);
	}

	public void scratch(Context context) {
		playSound(SOUND_SCRATCH, context);
	}

	public void playDrumNumber(int drumNumber, Context context) {
		switch (drumNumber) {
		case 1:
			playSound(DRUM_ONE, context);
			break;
		case 2:
			playSound(DRUM_TWO, context);
			break;
		case 3:
			playSound(DRUM_THREE, context);
			break;
		case 4: 
			playSound(DRUM_FOUR, context);
			break;
		case 5:
			playSound(DRUM_FIVE, context);
			break;
		case 6:
			playSound(DRUM_SIX, context);
			break;

		default:
			break;
		}
	}

	public void closeSoundPool() {
		soundPool.release();
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
	}

}
