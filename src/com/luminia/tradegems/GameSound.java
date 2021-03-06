package com.luminia.tradegems;

import java.util.HashMap;
import android.util.Log;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.luminia.tradegems.GamePreferences;

@SuppressLint("UseSparseArrays")
public class GameSound{
	
	public static final int SWAP = 1;
	public static final int ALL_MATCH = 2;
	public static final int LAST_SECONDS = 3;
	public static final int END = 4;
	private static final String TAG = "GameSound";
	
	private static MediaPlayer mMusicPlayer;
	
	
	private GamePreferences mGamePreferences;
	private boolean mSound;

	private SoundPool mSoundPool;
	private HashMap<Integer, Integer> mSoundPoolMap;
	
	/* 
	 * This boolean variable will be turned to false after we release the sound pool
	 * so that we can avoid some nasty IllegalStateExceptions 
	 * Nelson R. Perez
	 */
	boolean isSoundPoolReady;
	
	//This reference is used to store the context of the calling object that
	//will use the sound class to play sounds
	private Context mContext;

	/*
	 * Constructor. Start the instance with the all sounds needed for an activity/screen loaded.
	 * 
	 * @param context This reference is used to store the context of the calling 
	 * object that will use the sounGd class to play sounds
	 */
	public GameSound(Context context) {
		
		mContext = context;
		
	
		//Get sound state from User Preferences
		mGamePreferences = new GamePreferences(context);
		mSound = mGamePreferences.getSoundState();
		//Start SoundPool with main game effects
		this.init();
		
	    // the music that is played at the game 
	    // loop it when ended
		
	    mMusicPlayer = MediaPlayer.create(context, R.raw.shades_of_spring );
    	mMusicPlayer.setLooping(true);
	}
	
	private void init() {
	    mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 100);
	    mSoundPoolMap = new HashMap<Integer, Integer>();
	    
	    mSoundPoolMap.put(SWAP, mSoundPool.load(mContext,
	    		 R.raw.swap, 1));
	    mSoundPoolMap.put(ALL_MATCH, mSoundPool.load(mContext,
	    		 R.raw.all_match, 1));
	    mSoundPoolMap.put(LAST_SECONDS, mSoundPool.load(mContext,
	    		 R.raw.last_seconds, 1));
	    mSoundPoolMap.put(END, mSoundPool.load(mContext,
	    		 R.raw.end, 1));
	    isSoundPoolReady = true;
	}
	
	private float getVolume() {
		/* Updated: The next 4 lines calculate the current volume in 
	     * a scale of 0.0 to 1.0 */
		AudioManager mgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	    float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
	    float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
	   
	    float volume = streamVolumeCurrent / streamVolumeMax;
	    
	    return volume;
	}
	
	private void playSound(int sound, int loop) {
	    
		float volume = this.getVolume();
		if(isSoundPoolReady){
		    /* Play the sound with the correct volume */
		    mSoundPool.play(mSoundPoolMap.get(sound), volume, volume, 1, loop, 1f);     			
		}
	}
	
	private void stopSound(int sound) {
	    /* Stop the sound */
		if (!mSound) return;
	    mSoundPool.stop(mSoundPoolMap.get(sound));     
	}

	public void mute() {
		mSound = false;
		mGamePreferences.setSoundState(mSound);
	}
	
	public void turnOnSound() {
		mSound = true;
		mGamePreferences.setSoundState(mSound);
	}
	
		
	public void playGameMusic() {
		if (!mSound) return;
	    if (!mMusicPlayer.isPlaying()) {
	    	//the game music have 90% of the current volume
		    float volume = getVolume() * 0.9f;
	    	mMusicPlayer.seekTo(0);
	    	mMusicPlayer.setVolume(volume, volume);
	    	mMusicPlayer.start();
	    }
	}
	
	public void stopGameMusic(){
		if (!mSound) return;
		try{
			mMusicPlayer.stop();
		}
		finally{
		}
	}
	
	public void pauseGameMusic() {
		mMusicPlayer.pause();
	}
	
	public void resumeGameMusic() {
		if (!mSound) return;
	    if (!mMusicPlayer.isPlaying()) {
	    	//the game music have 70% of the current volume
		    float volume = getVolume() * 0.7f;
	    	mMusicPlayer.seekTo(mMusicPlayer.getCurrentPosition());
	    	mMusicPlayer.setVolume(volume, volume);
	    	mMusicPlayer.start();
	    } 
	}
	
	public void playLastSeconds(){
		if (!mSound) return;
		playSound(LAST_SECONDS, -1);
		float volume = getVolume() * 0.3f;
    	mMusicPlayer.setVolume(volume, volume);
	}
	
	public void stopLastSeconds(){
		if (!mSound) return;
		stopSound(LAST_SECONDS);
	}
	
	public void playAllMatch() {
		if (!mSound) return;
		float volume = getVolume() * 0.7f;
    	mMusicPlayer.setVolume(volume, volume);
	    playSound(ALL_MATCH, 0);
	}
	
	public void playSwap() {
		if (!mSound) return;
	    playSound(SWAP, 0);
	}
	
	public void playEnd() {
		if (!mSound) return;
		playSound(END, 0);
	}
	
	/**
	 * Releases the SoundPool
	 */
	public void release() {
	    mSoundPool.release();
	    isSoundPoolReady = false;
//	    mMusicPlayer.stop();
//	    mMusicPlayer.release();
	}

	/**
	 * Method call that will release the media player
	 */
	public static void releaseMediaPlayer(){
		mMusicPlayer.release();
	}
	
	/**
	 * Starts the background music
	 * @param context Application context
	 * @param musicId The id from the raw resource which is to be played
	 */
	public static void playMusic(Context context, int musicId) {
		mMusicPlayer = MediaPlayer.create(context, musicId);
	    mMusicPlayer.setLooping(true);
	    //the game music have 90% of the current volume
	    mMusicPlayer.seekTo(0);
	    mMusicPlayer.start();	    
	}
	
	/**
	 * Stops any background music currently playing
	 */
	public static void stopMusic() {
		mMusicPlayer.stop();
	}
	

}
