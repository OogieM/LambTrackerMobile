package com.weyr_associates.lambtracker;

import java.util.Calendar;

import com.weyr_associates.lambtracker.EvaluateSheep.IncomingHandler;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.client.android.Intents;



public class PrintLabels extends Activity {

	String     	cmd;
	Integer 	i;
	public int sheep_id, thissheep_id;
	
	int 		id;
	int   		fedtagid, farmtagid, eidtagid;
	private int			    recNo;
	private int             nRecs;	
	private DatabaseHandler dbh;
	private Cursor 	cursor;
	private String LabelText = "";
	private String EID = "";
	private String SheepName = "";
	private Boolean AutoPrint = false;

	Messenger mService = null;
	boolean mIsBound;
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	// variable to hold the string
	public String LastEID ;

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case eidService.MSG_UPDATE_STATUS:
				Bundle b1 = msg.getData();
		
				break;
			case eidService.MSG_NEW_EID_FOUND:
				Bundle b2 = msg.getData();

				LastEID = (b2.getString("info1"));
//				We have a good whole EID number	
				gotEID ( null);	
				break;			
			case eidService.MSG_UPDATE_LOG_APPEND:
//				Bundle b3 = msg.getData();
//				Log.i("Evaluate ", "Add to Log.");
				
				break;
			case eidService.MSG_UPDATE_LOG_FULL:
//				Log.i("Evaluate ", "Log Full.");
				
				break;
			case eidService.MSG_THREAD_SUICIDE:
//				Log.i("Evaluate", "Service informed Activity of Suicide.");
				doUnbindService();
				stopService(new Intent(PrintLabels.this, eidService.class));
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void LoadPreferences(Boolean NotifyOfChanges) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Log.i("PrintLabels", "Load Pref.");
		try {
			String TextForLabel = preferences.getString("label", "text");
			LabelText = TextForLabel;
			AutoPrint = preferences.getBoolean("autop", false);
		} catch (NumberFormatException nfe) {}
		
	}
	 public ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
//			Log.i("Evaluate", "At Service.");
			try {
				//Register client with service
				Message msg = Message.obtain(null, eidService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

				
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do anything with it
			}
		}
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been unexpectedly disconnected - process crashed.
			mService = null;
		}
	};    	

	private void CheckIfServiceIsRunning() {
		//If the service is running when the activity starts, we want to automatically bind to it.
//		Log.i("Evaluate", "At isRunning?.");
		if (eidService.isRunning()) {
//			Log.i("Evaluate", "is.");
			doBindService();
		} else {
//			Log.i("Evaluate", "is not, start it");
			startService(new Intent(PrintLabels.this, eidService.class));
			doBindService();
		}
//		Log.i("Evaluate", "Done isRunning.");
	} 	
	
	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
//		Log.i("Evaluate", "At doBind1.");
		bindService(new Intent(this, eidService.class), mConnection, Context.BIND_AUTO_CREATE);
//		Log.i("Evaluate", "At doBind2.");

		mIsBound = true;
		

		if (mService != null) {
//			Log.i("Evaluate", "At doBind3.");
			try {
				//Request status update
				Message msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
				msg.replyTo = mMessenger;
				mService.send(msg);
//				Log.i("Evaluate", "At doBind4.");
				//Request full log from service.
				msg = Message.obtain(null, eidService.MSG_UPDATE_LOG_FULL, 0, 0);
				mService.send(msg);
			} catch (RemoteException e) {}
		}
//		Log.i("Evaluate", "At doBind5.");
	}
	void doUnbindService() {
//		Log.i("Evaluate", "At DoUnbindservice");
		if (mService != null) {
		try {
			//Stop eidService from sending tags
			Message msg = Message.obtain(null, eidService.MSG_NO_TAGS_PLEASE);
			msg.replyTo = mMessenger;
			mService.send(msg);
			
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
		}
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, eidService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}

	}    	
	
	// use EID reader to look up a sheep
	public void gotEID( View v )
   {
		Integer ii;
	   	//	make the scan eid button red
	   	Button btn = (Button) findViewById( R.id.scan_eid_btn );
	   	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	   	// TODO
	   	clearBtn( null );  
	   	TextView TV = (TextView) findViewById (R.id.eidText);
	   	TV.setText( LastEID );
		Log.i("Evaluate", "Got EID " + LastEID);
		TV = (TextView) findViewById (R.id.inputText);
		TV.setText( LastEID );
		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
				"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off , sheep_table.alert01 " +
				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +	
				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
				"where id_type_table.id_typeid = 2 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", LastEID);
		Log.i("Got EID", " ready for command " + cmd); 
		Object crsr = dbh.exec( cmd ); 
    	cursor   = (Cursor) crsr;
    	dbh.moveToFirstRecord();
    	if( dbh.getSize() == 0 )
			{ // no sheep with that EID tag in the database so clear out and return
			clearBtn( null );
			TV = (TextView) findViewById( R.id.sheepnameText );
	    	TV.setText( "Cannot find this sheep." );
			
		if (AutoPrint) {
			scanEid( null );
			}
		
	    	return;
		}
    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	Log.i("Got EID", " got sheep named  " + dbh.getStr(0));
    	SheepName = dbh.getStr(0);
    	sheep_id = dbh.getInt(1);
    	Log.i("Got EID", " sheep ID is " + String.valueOf(sheep_id));
    	thissheep_id = sheep_id;
    	Log.i("Got EID", " sheep ID is " + String.valueOf(thissheep_id));
//    	TV = (TextView) findViewById(R.id.eidText)	;
//    	TV.setText(dbh.getStr(3));
    	String alert_text = dbh.getStr(6);
    	Log.i("Got EID ", "Alert Text is " + alert_text);
//    	Now to test of the sheep has an alert and if so then set the alerts button to red
//    	if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){


	     	
    	if (alert_text != null && !alert_text.isEmpty() ){
			// make the alert button red and enable it and pop up the alert text
			btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true);
			if (!AutoPrint) {
	    	showAlert(v);
			}
		 }
    	
    	
//		Now we need to get the farm tag for that sheep and fill the display with data
    	
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, " +
		"id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", thissheep_id);

//    	Log.i("Evaluate ", cmd);    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a farm tag installed
			TV = (TextView) findViewById( R.id.farmText );
			TV.setText( "No tag" );
    	} else {
    		TextView TV5 = (TextView) findViewById(R.id.farmText)	;
    		TV5.setText(dbh.getStr(3));
    		Log.i(" got EID ", "now got a farm tag " + dbh.getStr(3));
//    		ii = dbh.getInt(1);
    		farmtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
    	}
//		Now we need to get the federal tag for the sheep
		
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", thissheep_id);
    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
    	
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a federal tag installed
			TV = (TextView) findViewById( R.id.fedText );
			TV.setText( "No tag" );
    	} else {
        	fedtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database   	
        	TextView TV5 = (TextView) findViewById(R.id.fedText)	;
        	Log.i(" got EID ", "now got a fed tag " + TV5);
        	TV5.setText(dbh.getStr(3));
        	ii = dbh.getInt(1);
    	}
		// TODO
		if (AutoPrint) {
			printLabel(v);
			}
    	
   }	

	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.print_labels);
        String 			dbname = getString(R.string.real_database_file); 
        String          cmd;
        Button 			btn;
        TextView TV;       
        Object 			crsr;
        dbh = new DatabaseHandler( this, dbname );
       
		CheckIfServiceIsRunning();
		LoadPreferences(true);
		
    }
	@Override
	public void onResume (){	
		super.onResume();
		Log.i("PrintLabel", " OnResume");
		scanEid( null );
	}
	
	
	public void printLabel( View v ){ 

		// Ken add the printing code here
	    try
	    {
		String[] lines = EID.split("\n"); // works for both		
	    String contents = LastEID.substring(0, 3) + LastEID.substring(4, 16);
	   					    		
		Intent encodeIntent = new Intent("weyr.LT.ENCODE");
		encodeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		encodeIntent.addCategory(Intent.CATEGORY_DEFAULT); 
		encodeIntent.putExtra("ENCODE_FORMAT", "CODE_128");
		encodeIntent.putExtra("ENCODE_SHOW_CONTENTS", false);
		encodeIntent.putExtra("ENCODE_DATA", contents);
		encodeIntent.putExtra("ENCODE_AUTOPRINT", "false");

		if (AutoPrint) {
		encodeIntent.putExtra("ENCODE_AUTOPRINT", "true");
	     };
		
		encodeIntent.putExtra("ENCODE_DATA1", LabelText);						
		encodeIntent.putExtra("ENCODE_DATE", TheDateIs() + "  " + TheTimeIs());
		encodeIntent.putExtra("ENCODE_SHEEPNAME", SheepName);
	    startActivity(encodeIntent);
		
	    }
	    catch(Exception r)
	    {
	        Log.v("EIDService", "RunTimeException: " + r);
	    }			
	}
	
	private String TheDateIs() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);

		return Make2Digits(month + 1) + "/" +  Make2Digits(day) + "/" + year;
	}
	
	private String TheTimeIs() {
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		return Make2Digits(hours) + ":" + Make2Digits(minutes) + ":"
				+ Make2Digits(seconds) + " ";
	}

	   public void backBtn( View v )
	    {
//		   	Log.i("evaluate", " Back button pressed before close DB");
		   	dbh.closeDB();
//		   	Log.i("evaluate", " Back button pressed after close DB");
		   	doUnbindService();
			stopService(new Intent(PrintLabels.this, eidService.class));
			clearBtn( null );   	
			finish();
	    }
	 
	public void showAlert(View v)
	{
		String	alert_text;
		String 			dbname = getString(R.string.real_database_file); 
       String          cmd;    
       Object 			crsr;
//       	dbh = new DatabaseHandler( this, dbname );
		// Display alerts here   	
				AlertDialog.Builder builder = new AlertDialog.Builder( this );
				cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_id =%d", sheep_id);
				Log.i("evalGetAlert ", cmd);  
				crsr = dbh.exec( cmd );
		        cursor   = ( Cursor ) crsr;
		        dbh.moveToFirstRecord();		       
		        alert_text = (dbh.getStr(0));
		        Log.i("evalShowAlert ", alert_text); 
				builder.setMessage( alert_text )
			           .setTitle( R.string.alert_warning );
				builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int idx) {
			               // User clicked OK button   	  
			               }
			       });		
				AlertDialog dialog = builder.create();
				dialog.show();
//				cursor.close();
	}
	
	
	public void helpBtn( View v )
   {
  	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_print )
	           .setTitle( R.string.help_warning );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int idx) {
	               // User clicked OK button 
	        	  
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();
		
   }
	public void clearBtn( View v )
   {
		// clear out the display of everything here
				TextView TV ;
				Button btn;
				TV = (TextView) findViewById( R.id.inputText );
				TV.setText( "" );		
				TV = (TextView) findViewById( R.id.sheepnameText );
				TV.setText( "" );
				TV = (TextView) findViewById( R.id.fedText );
				TV.setText( "" );
				TV = (TextView) findViewById( R.id.farmText );
				TV.setText( "" );
				TV = (TextView) findViewById( R.id.eidText );
				TV.setText( "" );
		      	// make the alert button normal and disabled
		    	btn = (Button) findViewById( R.id.alert_btn );
		    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
		    	btn.setEnabled(false);  
		
   }
	   private String TodayIs() {
			Calendar calendar = Calendar.getInstance();
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
	        //12 hour format
//			int hour = cal.get(Calendar.HOUR);
	        //24 hour format
			int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			  
			return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) + "_" + hourofday + ":" + minute + ":" + second ;
		}
	    private String Make2Digits(int i) {
			if (i < 10) {
				return "0" + i;
			} else {
				return Integer.toString(i);
			}
		}	
//user clicked 'Scan' button    
public void scanEid( View v){
	// Here is where I need to get a tag scanned and put the data into the variable LastEID
		if (mService != null) {
		try {
			//Start eidService sending tags
			Message msg = Message.obtain(null, eidService.MSG_SEND_ME_TAGS);
			msg.replyTo = mMessenger;
			mService.send(msg);
		   	//	make the scan eid button green
	    	Button btn = (Button) findViewById( R.id.scan_eid_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0x0000FF00, 0xFFCC0000));
			
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
		}    	    	
}

	 // user clicked 'Search Fed' button
 public void searchFedTag( View v )
 	{
 	String          cmd;
 	TextView		TV = (TextView) findViewById( R.id.inputText );
 	String			fed = TV.getText().toString();
 	Log.i("Evaluate ", " federal tag is " + fed);
 	Integer			ii;
 	// Hide the keyboard when you click the button
 	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 	
//		Start of the actual code to process the button click
 	if( fed != null && fed.length() > 0 )
 		{
//			Search for the sheep with the entered federal tag number. 
// 		assumes no duplicate federal tag numbers, ok for our flock not ok for the general case
 		
 		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
 				"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off , sheep_table.alert01 " +
 				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +	
 				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
 				"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", fed);
// 		Log.i("Evaluate ", cmd);
 		}	
 	else
 	{
 		return;
  	}
 	Object crsr = dbh.exec( cmd ); 
 	cursor   = (Cursor) crsr;
 	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
 		{ // no sheep with that federal tag in the database so clear out and return
 		clearBtn( v );
 		TV = (TextView) findViewById( R.id.sheepnameText );
     	TV.setText( "Cannot find this sheep." );
     	return;
 		}
//This section would allow for multiple sheep with same tag if we implement next and previous
// 	buttons but is commented out for now as our sheep have unique federal tags
// 	if( dbh.getSize() >1){
//
//			Enable the previous and next record buttons
// 		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
// 		btn2.setEnabled(true);  
// 		//	Set up the various pointers and cursor data needed to traverse the sequence
// 		recNo    = 1;
// 		cursor   = (Cursor) crsr;
// 		nRecs    = cursor.getCount();
// 		colNames = cursor.getColumnNames();
// 		cursor.moveToFirst();
// 	}
		
		// TODO
 	fedtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
		Log.i("Evaluate", " id infor table id is " + String.valueOf(fedtagid));
		
 	TV = (TextView) findViewById(R.id.sheepnameText);
 	TV.setText(dbh.getStr(0));
 	Log.i("Evaluate", " sheep name is " + dbh.getStr(0));
 	sheep_id = dbh.getInt(1);
 	thissheep_id = sheep_id;
 	Log.i("Evaluate", " sheep id is " + String.valueOf(thissheep_id));
 	TV = (TextView) findViewById(R.id.fedText)	;
 	TV.setText(dbh.getStr(3));
 	Log.i("Evaluate", " sheep fed tag is " + dbh.getStr(3));
 	String alert_text = dbh.getStr(6);
 	Log.i("Evaluate", " sheep alert text is " + alert_text);
// 	Now to test of the sheep has an alert and if so then set the alerts button to red
		if (alert_text != null && !alert_text.isEmpty() ){
//		if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
			// make the alert button red and enable it and pop up the alert text
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	showAlert(v);
		}
		
 	
//		Now we need to get the farm tag for that sheep and fill the display with data
 	
 	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, " +
		"id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", thissheep_id);

 	Log.i("Evaluate ", "ready to get farm tags cmd is " + cmd);    	
 	crsr = dbh.exec( cmd );
 	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a farm tag installed
			TV = (TextView) findViewById( R.id.farmText );
			TV.setText( "No tag" );
 	} else {
 		TextView TV5 = (TextView) findViewById(R.id.farmText)	;
 		TV5.setText(dbh.getStr(3));
 		ii = dbh.getInt(1);
 		farmtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
 	}
 	}
//	user clicked 'Search Farm Tag' button
 public void searchFarmTag( View v )
 	{
 	String          cmd;
 	TextView		TV = (TextView) findViewById( R.id.inputText );
 	String			farm = TV.getText().toString();
 	Integer			ii;
 	// Hide the keyboard when you click the button
 	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 	
//		Start of the actual code to process the button click
 	if( farm != null && farm.length() > 0 )
 		{
//			Search for the sheep with the entered farm tag number. 
// 		assumes no duplicate farm tag numbers, ok for our flock not ok for the general case  
 		
 		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
 				"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off, sheep_table.alert01 " +
 				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
 				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
 				"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", farm);
 		
// 		Log.i("Evaluate", "building command search for farm tag ");
 		}	
 	else
 	{
 		return;
  	}
 	Object crsr = dbh.exec( cmd );   	
 	dbh.moveToFirstRecord();
 	if( dbh.getSize() == 0 )
 		{ // no sheep with that farm tag in the database so clear out and return
 		clearBtn( v );
 		TV = (TextView) findViewById( R.id.sheepnameText );
     	TV.setText( "Cannot find this sheep." );
     	return;
 		}
//Need to add next and previous buttons if we have duplicate farm tags
// 	if( dbh.getSize() >1){
//
//			Enable the previous and next record buttons
// 		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
// 		btn2.setEnabled(true);  
// 		//	Set up the various pointers and cursor data needed to traverse the sequence
// 		recNo    = 1;
// 		cursor   = (Cursor) crsr;
// 		nRecs    = cursor.getCount();
//// 		colNames = cursor.getColumnNames();
// 		cursor.moveToFirst();
// 	}
 	
 	farmtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
 	TV = (TextView) findViewById(R.id.sheepnameText);
 	TV.setText(dbh.getStr(0));
 	sheep_id = dbh.getInt(1);
 	TV = (TextView) findViewById(R.id.farmText)	;
 	TV.setText(dbh.getStr(3));
 	ii = dbh.getInt(1);
 	
// 	Now to test of the sheep has an alert and if so then set the alerts button to red
 	String alert_text = dbh.getStr(6);
// 	if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
 	if (alert_text != null && !alert_text.isEmpty() ){
			// make the alert button red and enable it and pop up the alert text
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	showAlert(v);
		}
//		Now we need to get the rest of the tags and fill the display with data
		
 	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", ii);
 	
 	crsr = dbh.exec( cmd );
 	dbh.moveToFirstRecord();
 	
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a federal tag installed
			TV = (TextView) findViewById( R.id.fedText );
			TV.setText( "No tag" );
 	} else {
     	fedtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database   	
     	TextView TV5 = (TextView) findViewById(R.id.fedText)	;
     	TV5.setText(dbh.getStr(3));
     	ii = dbh.getInt(1);
 	}
 	}    
	
}
