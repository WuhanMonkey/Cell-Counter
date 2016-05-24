package com.example.impedancedetector;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;
import java.util.UUID;





















import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
	
	private Handler mHandler;
    public BluetoothSocket mBlueToothSocket;	
  public  BluetoothDevice global_device;
  public  BluetoothAdapter mBluetoothAdapter;
  public BroadcastReceiver mReceiver = null;
  private double min_current = Double.MAX_VALUE;
  private double min_base = Double.MAX_VALUE;
  private boolean mConnected = false;
  private ArrayList<Integer> res_list = null;

  
  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final int REQUEST_ENABLE_BT=1;

		 mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		 if (mBluetoothAdapter!=null){
			 
			  if (!mBluetoothAdapter.isEnabled()) {		
		      Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		      startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			  
			   }
		    }
		  if(mBluetoothAdapter.isDiscovering()){
				mBluetoothAdapter.cancelDiscovery();
			}
		  
		  mBluetoothAdapter.startDiscovery();
		   mReceiver = new BroadcastReceiver(){
			  public void onReceive(Context context, Intent intent) {
				  String name = new String();
			        String action = intent.getAction();
			        // When discovery finds a device
			        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
			            // Get the BluetoothDevice object from the Intent
			            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			            // Add the name and address to an array adapter to show in a ListView
			           // ArrayAdapter mArrayAdapter=  new ArrayAdapter();
						//mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			           name= device.getName();
			           //Log.d("btdebug:", name);
			           if(name.compareTo("HC-05")==0){
			        	   
			        	   //Log.d("btdebug:", name);
			        	  	//Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT);
			        	   global_device = device;
			        	   ConnectThread connecting = new ConnectThread(global_device);
			     		   connecting.run();
			           
			           }
			           else{
			        	   
			        	   Log.d("btdebug:", "does not match");
			           }			           			           
			        }
			    }
		  };
		// Register the BroadcastReceiver
					IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
					registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
					
		
		  
		  
	}
  
  
  @Override
  public void onDestroy()
  {
	  
	  if(mReceiver!=null){
	unregisterReceiver(mReceiver);
	mReceiver = null;
	
	  }
	super.onDestroy();  
	  
  }
  
  @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}	

  
	/**public void sendMessage(View view){
	        
	    	//Intent intent = new Intent(this, .class);
	    	EditText editText = (EditText) findViewById(R.id.edit_message);
	    	EditText editText2 = (EditText) findViewById(R.id.edit_message2);
	    	EditText editText3 = (EditText) findViewById(R.id.edit_message3);
	    	String message = editText.getText().toString();
	    	String message2 = editText2.getText().toString();
	    	String message3 = editText3.getText().toString();
	    	ConnectedThread write = new ConnectedThread(mBlueToothSocket);
	    	Log.d("btdebug:", "Goog here");
	    	//intent.putExtra(EXTRA_MESSAGE, message);
	    	//BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
	    	//byte b= 10; 
	    	String test = "Hello World!\n";
	    	
	    	
	    	
	    	
	    	byte[] bytes;
	    	
	    	bytes = message.getBytes();
	    	write.write(bytes);
	    	bytes = message2.getBytes();
	    	write.write(bytes);
	    	bytes = message3.getBytes();
	    	write.write(bytes);
	    	write.start();
	    	
	    	
	    	//public static void main(String args[]) {
	    	   // new ExtCls().function();
	    	   // }
	    	
	       // startActivity(intent);
	    }
	**/
   public void calibrate(View view){
	   InputStream inputStream = getResources().openRawResource(R.raw.sample_data);
       CSVFile csvFile = new CSVFile(inputStream);
       List scoreList = csvFile.read();
       Iterator itr = scoreList.iterator();
       final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
       double base_val = 0;
       while(itr.hasNext()){
       	String[] str = (String[]) itr.next();
       	base_val = Double.parseDouble(str[2])/10;
       	min_base = min_base>base_val?base_val:min_base;     	
       }
       TextView baseValueNumber = (TextView)findViewById(R.id.baseValueNumber);
       min_base = Math.abs(min_base);
       baseValueNumber.setText(String.valueOf(min_base) + " uA");
	   
   }
    public void plot_recv(ArrayList list){
    	
    }
	
	public void plot(View view){
		TreeMap<Double, Double> data = new TreeMap<Double,Double>();
		
		
        InputStream inputStream = getResources().openRawResource(R.raw.sample_data);
        CSVFile csvFile = new CSVFile(inputStream);
        List scoreList = csvFile.read();
        Iterator itr = scoreList.iterator();
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        GraphView graph = (GraphView) viewGroup.findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();
        graph.setTitle("Current (uA) vs Voltage (V)");

        //NumberFormat nf = NumberFormat.getInstance();
        //nf.setMaximumFractionDigits(3);
        //nf.setMaximumIntegerDigits(2);

       // graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf));
       
        
        
        GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
        //gridLabel.setHorizontalAxisTitle("Voltage (V)");
        //gridLabel.setVerticalAxisTitle("Current (uA)");
        double voltage = 0;
        double current = 0;
        while(itr.hasNext()){
        	String[] str = (String[]) itr.next();
        	voltage = Double.parseDouble(str[1]);
        	current = Double.parseDouble(str[3])/10;
        	min_current = min_current>current? current: min_current;
        	data.put(voltage, current);
        	
        }
        TextView min_cur = (TextView)findViewById(R.id.peakCurrentValue);
        min_current = Math.abs(min_current);
        min_cur.setText(String.valueOf(min_current) + " uA");
        
        //calculate cell number
        double cell_number = Math.pow(10,((116.09508-(min_current/min_base)*72.1)/21.44016));
        
        cell_number = Math.round(cell_number);
        TextView cell_num = (TextView)findViewById(R.id.cellNumberValue);
        cell_num.setText(String.valueOf(cell_number));
        
        
        
        
        double cur_max = Integer.MIN_VALUE;
        double cur_min = Integer.MAX_VALUE;
        double vol_max = Integer.MIN_VALUE;
        double vol_min = Integer.MAX_VALUE;
        for(Map.Entry<Double,Double> entry : data.entrySet()) {
        	  voltage = entry.getKey();
        	  current = entry.getValue();
        	 
        	  series.appendData(new DataPoint(voltage, current), false, 500);
        	  cur_max = current>cur_max? current:cur_max;
        	  cur_min = current<cur_min? current:cur_min;
        	  vol_max = voltage>vol_max? voltage:vol_max;
        	  vol_min = voltage<vol_min? voltage:vol_min;
        	 Log.i("test", String.valueOf(voltage) + " " + String.valueOf(current) +"\n");
        	}
        
     // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(vol_min*101/100);
        graph.getViewport().setMaxX(vol_max*101/100);
        
        
        // set manual Y bounds
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(cur_min*101/100);
        graph.getViewport().setMaxY(cur_max*101/100);
        graph.addSeries(series);
	}
	

	
	
	
	

		// initiate a connection client 

		 class ConnectThread extends Thread{ 
		    //private final BluetoothSocket mmSocket;
		    private  BluetoothDevice mmDevice;
		    private UUID MY_UUID;
		    public ConnectThread(BluetoothDevice device) {
		        // Use a temporary object that is later assigned to mmSocket,
		        // because mmSocket is final
		        BluetoothSocket tmp = null;
		        mmDevice = device;
		        
		        //SharedPreferences prefs = getApplicationContext().getSharedPreferences("device_id",0);
		        
		        MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		        
		        
		        
		        // Get a BluetoothSocket to connect with the given BluetoothDevice
		        try {
		            // MY_UUID is the app's UUID string, also used by the server code
		            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
		        } catch (IOException e) { }
		        mBlueToothSocket = tmp;
		    }
		 
		    public void run() {
		        // Cancel discovery because it will slow down the connection
		    	mBluetoothAdapter.cancelDiscovery();
		 
		        try {
		            // Connect the device through the socket. This will block
		            // until it succeeds or throws an exception
		        	Log.d("btdebug:", "start to connect...");
		        	
		        	mBlueToothSocket.connect();
		        	//Log.d("btdebug:", "C");
		        } catch (IOException connectException) {
		            // Unable to connect; close the socket and get out
		            try {
		            	
		            	Log.d("btdebug:", "Socket connection failed. start to close...");
		            	Toast.makeText(getApplicationContext(), "Socket connection failed. start to close...", Toast.LENGTH_LONG);
		            	mBlueToothSocket.close();
		            } catch (IOException closeException) { }
		            return;
		        }
		 
		        Log.d("btdebug:", "Socket connection Success.");
		        Toast.makeText(getApplicationContext(), "Socket connection Success.", Toast.LENGTH_LONG);
		        ConnectedThread recv = new ConnectedThread(mBlueToothSocket);
		    	Log.d("btdebug:", "Good here");
		    	recv.start();
		    }
		        // Do work to manage the connection (in a separate thread)
		    //manageConnectedSocket(mmSocket);
		   
		 
		    /** Will cancel an in-progress connection, and close the socket */
		    public void cancel() {
		        try {
		        	mBlueToothSocket.close();
		        } catch (IOException e) { }
		    }
		
		
	
	
		
		}
		
		
		// send the message
		 private class ConnectedThread extends Thread {
			    private final BluetoothSocket mmSocket;
			    private final InputStream mmInStream;
			    private final OutputStream mmOutStream;
			 
			    public ConnectedThread(BluetoothSocket socket) {
			        mmSocket = socket;
			        InputStream tmpIn = null;
			        OutputStream tmpOut = null;
			 
			        // Get the input and output streams, using temp objects because
			        // member streams are final
			        try {
			            tmpIn = socket.getInputStream();
			            tmpOut = socket.getOutputStream();
			        } catch (IOException e) { }
			 
			        mmInStream = tmpIn;
			       

			        mmOutStream = tmpOut;
			    }
			 
			    public void run() {
			        byte[] buffer = new byte[255];  // buffer store for the stream
			        int bytes; // bytes returned from read()
			        Log.d("btdebug:", "Start listening the input stream.");
			        res_list = new ArrayList<Integer>();

			        // Keep listening to the InputStream until an exception occurs
			        while (true) {
			            try {
			                // Read from the InputStream
			                bytes = mmInStream.read(buffer, 0, 1);
			                Log.d("btdebug:", "Byte received: " + Integer.toString(buffer[0]));
			                

			                //Byte b = buffer[0];
			                //res_list.add(b.intValue());
			                //if(res_list.size() == 255){
			                //	plot_recv(res_list);
			                //	break;
			                //}
			                //Log.d("btdebug:", "length of list: " + Integer.toString(res_list.size()));
			                try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			               
			            } catch (IOException e) {
			                break;
			            }
			        }
			    }
			 
			    /* Call this from the main activity to send data to the remote device */
			    public void write(byte[] bytes) {
			        try {
			            mmOutStream.write(bytes);
			        } catch (IOException e) { }
			    }
			 
			    /* Call this from the main activity to shutdown the connection */
			    public void cancel() {
			        try {
			            mmSocket.close();
			        } catch (IOException e) { }
			    }
			}
	
	
	
		
	
		
		
}


	
