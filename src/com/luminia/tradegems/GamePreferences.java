package com.luminia.tradegems;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class GamePreferences {
     private static final String GAME_SHARED_PREFS = "com.luminia.tradegems.Preferences"; //  Name of the file -.xml
     
     private static final String SOUND_STATE = "1";
     private static final String GAME_STATE = "2";
     //private static final String SOUND_STATE = "1";
     //private static final String SOUND_STATE = "1";
     
     
     private SharedPreferences mGameSharedPrefs;
     private Editor mPrefsEditor;

     public GamePreferences(Context context)
     {
         this.mGameSharedPrefs = context.getSharedPreferences(GAME_SHARED_PREFS, 
        		 Activity.MODE_PRIVATE);
         this.mPrefsEditor = mGameSharedPrefs.edit();
     }

     public String getSmsBody() {
         return mGameSharedPrefs.getString("sound", "");
     }

     
     
     
     //Sound State
     
     /**
      * Return the state of sound, true for available
      * false to mute.
      * 
      * @return sound state, true if the user never save anything
      */
     public Boolean getSoundState() {
    	 //True by default, if find nothing
         return mGameSharedPrefs.getBoolean(SOUND_STATE, true);
     }
     
     /**
      * Set the state of sound, true for available
      * false to mute.
      * 
      * @param sound Boolean value that indicate the sound state (true dor available, false to mute)
      */
     public void setSoundState(Boolean sound) {
         mPrefsEditor.putBoolean(SOUND_STATE, sound);
         mPrefsEditor.commit();
     }
     
}
