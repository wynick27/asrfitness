package edu.brandeis.cystant;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import edu.cmu.pocketsphinx.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ExecutionActivity extends Activity
implements MessageListener {
	

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;
    private SpeechService speech;
    private LocationService location;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.excution);
	    // TODO Auto-generated method stub
	    speech=new SpeechService();
        speech.start(this,this);
        location=new LocationService();
        location.start(this, this);
	}
	
	public void startAnotherActivity(View v){
    	Intent intent = new Intent(this,SummaryActivity.class); 
        startActivity(intent);
    	Log.d("ZZH", "click");
        //setTitle("other Activity!");
    }

	 
    @Override
	public void OnMessage(String type, Object param) {
		// TODO Auto-generated method stub
		switch (type)
		{
		case SpeechService.MSG_SpeechRecognized:
			String result=(String)param;
			if (result.contains("time"))
			{
				Calendar calendar = Calendar.getInstance();
				   SimpleDateFormat simpleDateFormat = 
				     new SimpleDateFormat("HH:mm a");
				   String curtime = "current time is " + simpleDateFormat.format(calendar.getTime());
				   Log.i("Time", curtime);
				   speech.speak(curtime);
			}
			if (result.contains("hi assistant"))
			{
				try {
				    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				    Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
				    r.play();
				} catch (Exception e) {
				    e.printStackTrace();
				}
			}
			if (result.contains("location"))
			{
				location.requireLocation();
			}
			if (result.contains("speed"))
			{
				location.requireSpeed();
			}
			break;
		case LocationService.CUR_LOC:
			Address addr=(Address)param;
			String curloc =String.format(
					"current location is %s, %s, %s",
					// If there's a street address, add it
					addr.getMaxAddressLineIndex() > 0 ? addr
							.getAddressLine(0) : "",
					// Locality is usually a city
					addr.getLocality(),
					// The country of the address
					addr.getCountryName());
			speech.speak(curloc);
			break;
		case LocationService.CUR_SPEED:
			String curspeed = String.format("current speed is %.2f m/s",  ((float)param));
			speech.speak(curspeed);
			break;
		case LocationService.LOC_CHANGE:
			break;
		}
	}

	

}
