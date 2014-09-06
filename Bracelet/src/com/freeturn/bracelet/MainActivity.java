package com.freeturn.bracelet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.http.impl.conn.tsccm.WaitingThread;

import com.freeturn.bluetooth.WaitThread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity 
{
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
	
	private static final int SERVER_PORT = 1234;
	int fiction_i = 0;
	int hand	  = 0;
	int[][] fiction_protocol_data = {{60, 90}, {50, 90}, {30, 90}, {10,90}, {9, 87}, {4, 90}, {2, 90}, {0,0}};
	int soundID;
	IntentFilter intentFilter;
	private WifiP2pManager mManager;
	private Channel mChannel;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				int gg = 1;
				RefreshNavigation(gg);      
			};
        });
        
		final RadioGroup my_radio_group = (RadioGroup) findViewById(R.id.radioGroup1);
		my_radio_group.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) 
            {
                if (checkedId == R.id.left_radio) {
                	hand = -1;
                } else if (checkedId == R.id.right_radio) {
                	hand = 1;
                }
                
               // button.setText(Integer.toString(hand));
            }

        });
		WaitThread bt_wait_connection_thread = new WaitThread();
		bt_wait_connection_thread.run();
        //RefreshNavigation(); // при запуске грузим первое значение
        
    };
	    
    public void	RefreshNavigation(int data)
    {
    	/*
    	int current_distance = fiction_protocol_data[fiction_i][0];
    	int current_azimuth  = fiction_protocol_data[fiction_i][1];
    	*/
    	int current_distance = data;
    	int current_azimuth  = data;
    	DoAction(current_distance, current_azimuth);
    };
    
    public void DoAction(int distance, int azimuth)
    {

    	DrawProgress(distance, azimuth);
    	if ((distance <= 4) && (azimuth*hand > 0))
    	{
    		StartVibrating(10*distance, 10*distance, 0);
    	}
    	else if ((distance < 10) && (azimuth*hand > 0))
    	{
    		StartVibrating(10*distance, 10*distance, 1);
    	}
    	else if ((distance >= 10) && (distance < 30) && (azimuth*hand > 0))
    	{
    		StartVibrating(2000, 1000, 1);
    	}
    	else if ((distance >= 30) && (distance < 60) && (azimuth*hand > 0))
    	{
    		StartVibrating(1000, 1000, 1);
    	}
    	else
    	{
    		StopVibrating();
    	}

    	
    	if (distance == 0)
    		Fanfaras();
    	
    };
    
    public void DrawProgress(int distance, int azimuth)
    {
    	final TextView distance_container = (TextView) findViewById(R.id.distance);
    	final TextView azimuth_container = (TextView) findViewById(R.id.azimuth);
    	
    	distance_container.setText(distance + " m");
    	azimuth_container.setText(azimuth + "°");
    	fiction_i++;
    	if (fiction_i == fiction_protocol_data.length)
    		fiction_i = 0;
       	
       	final ProgressBar progress = (ProgressBar) findViewById(R.id.dist_progress);
       	if ((distance >= 0)&&(distance < 60))
       		progress.setProgress(100 - (distance*100)/60);
       	else
       		progress.setProgress(0);
       	     	
    }
    
    public void StartVibrating(int duration, int pause, int repeat) 
    {
		Vibrator vibro = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		long[]	pattern = {0, duration, pause};
		
		if (repeat != 0)
			vibro.vibrate(pattern, repeat);
		else
			vibro.vibrate(4000);
		
	}
    
    public void StopVibrating()
    {
		Vibrator vibro = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibro.cancel();	
	}
    
    public void StartBeeping(int duration, int pause, int repeat)
    {
    	Beeper beeper = new Beeper();
    	long[]	pattern = {0, duration, pause};
    	beeper.beep(pattern, repeat);
    }

    public void Fanfaras()
    {
		long[]	pattern = {0, 300,200, 80,150, 80,150, 100,250, 300,300, 120,150, 150};
		
		Vibrator vibro = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibro.vibrate(pattern, -1);
		
		Beeper beeper = new Beeper();
		beeper.beep(pattern, -1);
    }
    
    public class Beeper
    {

    	Beeper()
    	{

    	}
    	public void beep(long[] pattern, int repeat)
    	{

	    }
    	
        public void StopBeeping()
        {

        }
        
    }
}



    
