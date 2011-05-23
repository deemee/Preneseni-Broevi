package com.divlapps.portnums;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MyApplication extends Application
{
   
  @Override
  public void onCreate()
	{
	   popupInfo(this);
	   super.onCreate();
	}

  public static int popupInfo(Context ctx)
  {
	  SharedPreferences prefs;
	  int firstTime = 0;
	  
	  // check if the app is executed for the first time
	  prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
      firstTime = prefs.getInt("first_time", 0);
      SharedPreferences.Editor editor = prefs.edit();
      
      if(firstTime == 0){
  		editor.putInt("first_time", 1);
  		editor.commit();
  		firstTime = 1;
      } else {
			firstTime += 1;
			editor.putInt("first_time", firstTime);
			editor.commit();
      }
      return firstTime;
  }
  
}
