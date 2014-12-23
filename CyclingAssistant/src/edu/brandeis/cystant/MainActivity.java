package edu.brandeis.cystant;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

    private static final int msgKey1 = 1;
    private TextView mTime;
    private Thread myTimeThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTime = (TextView) findViewById(R.id.mytime);
        myTimeThread = new TimeThread();
        myTimeThread.start();
        //ImageButton StartButton = (ImageButton)findViewById(R.id.StartButton);
        //OnClickListener button_listener = null;
		//StartButton.setOnClickListener(button_listener);
    }
/*    
    private Button.OnClickListener button_listener = new Button.OnClickListener(){     //创建button listener
        public void onClick(View v){
            
            Intent intent = new Intent();                       
            intent.setClass(MainActivity.this,ExecutionActivity.class);// TestActivity 是要跳转到的Activity，需要在src下手动建立TestActivity.java文件
            startActivity(intent);
            setTitle("other Activity!");
          }
        };
  */
    public void startAnotherActivity(View v){
    	myTimeThread.interrupt();
    	Intent intent = new Intent(this,ExecutionActivity.class); 

        //intent.setClass(this,ExecutionActivity.class);// TestActivity 是要跳转到的Activity，需要在src下手动建立TestActivity.java文件
        startActivity(intent);
    	Log.d("ZZH", "click");
        //setTitle("other Activity!");
    }
    public class TimeThread extends Thread {
    	         @Override
    	         public void run () {
    	             while(!Thread.currentThread().isInterrupted()) {
    	                 try {
    	                     Thread.sleep(1000);
    	                     Message msg = new Message();
    	                     msg.what = msgKey1;
    	                     mHandler.sendMessage(msg);
    	                 }
    	                 catch (InterruptedException e) {
    	                     e.printStackTrace();
    	                 }
    	             };
    	         }
    	     }
    	     
    	     private Handler mHandler = new Handler() {
    	         @Override
    	         public void handleMessage (Message msg) {
    	             super.handleMessage(msg);
    	             switch (msg.what) {
    	                 case msgKey1:
    	                     long sysTime = System.currentTimeMillis();
    	                     CharSequence sysTimeStr = DateFormat.format("hh:mm:ss", sysTime);
    	                     mTime.setText(sysTimeStr);
    	                     break;
    	                 
    	                 default:
    	                     break;
    	             }
    	         }
    	     };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
