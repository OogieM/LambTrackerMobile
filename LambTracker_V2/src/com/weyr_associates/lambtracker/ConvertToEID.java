package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.database.Cursor;

public class ConvertToEID extends Activity {
	private DatabaseHandler dbh;
	int             fedtagid, farmtagid, eidtagid;
	public Cursor 	cursor;
	public int 		thissheep_id, new_tag_type, new_tag_color, new_tag_location;
	
	public Button btn;
	public String tag_type_label, tag_color_label, tag_location_label, new_tag_number, eid_tag_color_label ;
	public String eid_tag_location_label, eidText;
	public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner, eid_tag_color_spinner, eid_tag_location_spinner;
	public List<String> tag_types, tag_locations, tag_colors;
	ArrayAdapter<String> dataAdapter;
	String     	cmd;
	Integer 	i;
		
/////////////////////////////////////////////////////
	
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
				gotEID ();	
				break;			
			case eidService.MSG_UPDATE_LOG_APPEND:
//				Bundle b3 = msg.getData();
//				Log.i("Convert", "Add to Log.");
				
				break;
			case eidService.MSG_UPDATE_LOG_FULL:
//				Log.i("Convert", "Log Full.");
				
				break;
			case eidService.MSG_THREAD_SUICIDE:
				Log.i("Convert", "Service informed Activity of Suicide.");
				doUnbindService();
				stopService(new Intent(ConvertToEID.this, eidService.class));
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
 	 public ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mService = new Messenger(service);
 			Log.i("Convert", "At Service.");
 			try {
 				//Register client with service
 				Message msg = Message.obtain(null, eidService.MSG_REGISTER_CLIENT);
 				msg.replyTo = mMessenger;
 				mService.send(msg);

 				//Request a status update.
// 				msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
 //				mService.send(msg);
 				
 				//Request full log from service.
 //				msg = Message.obtain(null, eidService.MSG_UPDATE_LOG_FULL, 0, 0);
 //				mService.send(msg);
 				
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
		Log.i("Convert", "At isRunning?.");
		if (eidService.isRunning()) {
//			Log.i("Convert", "is.");
			doBindService();
		} else {
//			Log.i("Convert", "is not, start it");
			startService(new Intent(ConvertToEID.this, eidService.class));
			doBindService();
		}
//		Log.i("Convert", "Done isRunning.");
	} 	
 	
	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
//		Log.i("Convert", "At doBind1.");
		bindService(new Intent(this, eidService.class), mConnection, Context.BIND_AUTO_CREATE);
//		Log.i("Convert", "At doBind2.");

		mIsBound = true;
		

		if (mService != null) {
//			Log.i("Convert", "At doBind3.");
			try {
				//Request status update
				Message msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
				msg.replyTo = mMessenger;
				mService.send(msg);
				Log.i("Convert", "At doBind4.");
				//Request full log from service.
				msg = Message.obtain(null, eidService.MSG_UPDATE_LOG_FULL, 0, 0);
				mService.send(msg);
			} catch (RemoteException e) {}
		}
//		Log.i("Convert", "At doBind5.");
	}
 	void doUnbindService() {
// 		Log.i("Convert", "At DoUnbindservice");
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
	public void gotEID( )
    {
		Object crsr;
		
	   	//	make the scan eid button red
    	btn = (Button) findViewById( R.id.scan_eid_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	// 	Display the EID number
    	TextView TV = (TextView) findViewById (R.id.eidText);
    	TV.setText( LastEID );
    	
		Log.i("Convert", "Got EID");
    	//	Set up the location and color spinners for EID  
    	eid_tag_color_spinner = (Spinner) findViewById(R.id.eid_tag_color_spinner);
    	tag_colors = new ArrayList<String>();        
        // Select All fields from tag colors to build the spinner
        cmd = "select * from tag_colors_table";
        crsr = dbh.exec( cmd );  
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	tag_colors.add("Select a Color");
         // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		tag_colors.add(cursor.getString(2));
    	}
    	cursor.close();
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_colors);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eid_tag_color_spinner.setAdapter (dataAdapter);
		eid_tag_color_spinner.setSelection(1);
		Log.i("Convert", " Got color spinner set");
		
		eid_tag_location_spinner = (Spinner) findViewById(R.id.eid_tag_location_spinner);
		tag_locations = new ArrayList<String>(); 
		tag_locations.add("Select a Location");
		tag_locations.add("RE");		
		tag_locations.add("LE");
		
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_locations);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		eid_tag_location_spinner.setAdapter (dataAdapter);
		eid_tag_location_spinner.setSelection(1);
		
		Log.i("Convert", " Got location spinner set");
		
	}	
	
/////////////////////////////////////////////////////	
	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert_to_eid);
        String dbname = getString(R.string.real_database_file); 
    	dbh = new DatabaseHandler( this, dbname );
    	
 //////////////////////////////////// 
		CheckIfServiceIsRunning();
		Log.i("Convert", "back from isRunning");  	
////////////////////////////////////    	
    	   	
    	//	make the remove tag buttons red
		Log.i("onCreate", " before setting remove tag buttons red");
    	btn = (Button) findViewById( R.id.remove_fedtag_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	Log.i("onCreate", " remove fed tag button is red");
    	btn = (Button) findViewById( R.id.remove_farmtag_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	Log.i("onCreate", " after setting remove tag buttons red");
    	
    	//	Disable the alert button until we have an alert for this sheep
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false);
    	Log.i("onCreate", " after disable alert button");
    	
    	//	Disable the Next Record and Prev. Record button until we have multiple records
//    	btn = (Button) findViewById( R.id.next_rec_btn );
//    	btn.setEnabled(false); 
//    	btn = (Button) findViewById( R.id.prev_rec_btn );
//    	btn.setEnabled(false);
    	
    	//	Disable the bottom update tag button until we choose to add or update
       	btn = (Button) findViewById( R.id.update_display_btn );
    	btn.setEnabled(false); 
    	fedtagid = 0;
    	farmtagid = 0;
    	eidtagid = 0;
    	new_tag_number = null;

       	}
    
    // user clicked the 'back' button
    public void backBtn( View v )
	    {
		doUnbindService();
		stopService(new Intent(ConvertToEID.this, eidService.class));
       	dbh.closeDB();
    	clearBtn( null );   	
    	finish();
	    }
    // user clicked the 'help' button
    public void takeNote( View v )
    {
    	final Context context = this;
    	//Implement take a note stuff here
    	if (thissheep_id == 0) {
    		Log.i ("takeNote", " no sheep selected " + String.valueOf(thissheep_id));
    	}
    	else {
    		Log.i ("takeNote", " got a sheep, need to get a note to add");
    		
    		LayoutInflater li = LayoutInflater.from(context);
			View promptsView = li.inflate(R.layout.note_prompt, null);

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);

			// set prompts.xml to alertdialog builder
			alertDialogBuilder.setView(promptsView);

			final EditText userInput = (EditText) promptsView
					.findViewById(R.id.note_text);

			// set dialog message
			alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Save Note",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
					// get user input and set it to result
					// edit text
					String note_text = String.valueOf(userInput.getText());
					cmd = String.format("insert into note_table (sheep_id, note_text, note_date) " +
	    					"values ( %s, '%s', '%s' )", thissheep_id, note_text, TodayIs());
	    			Log.i("update notes ", "before cmd " + cmd);
	    			dbh.exec( cmd );	
	    			Log.i("update notes ", "after cmd exec");
				    }
				  })
				.setNegativeButton("Cancel",
				  new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				    }
				  });

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
    	}   	
    }
    
// user clicked the 'help' button
    
    public void helpBtn( View v )
    {
   	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_convert )
	           .setTitle( R.string.help_warning );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int idx) {
	               // User clicked OK button 
	        	  
	    		   clearBtn( null );
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();		
    }
    
    // user clicked 'clear' button
    public void clearBtn( View v )
	    {
	    // clear out the display of everything
    	TextView TV10 = (TextView) findViewById( R.id.inputText );
    	TV10.setText( "" );		
    	TextView TV = (TextView) findViewById( R.id.sheepnameText );
	    TV.setText( "" );
	    TextView TV1  = (TextView) findViewById( R.id.eidText );
	    TV1.setText( "" );
	    TextView TV2  = (TextView) findViewById( R.id.fedText );
	    TV2.setText( null );
	    TextView TV3  = (TextView) findViewById( R.id.farmText );
	    TV3.setText( null );
	    TextView TV4 = (TextView) findViewById( R.id.fed_colorText );
	    TV4.setText( "" );
	    TextView TV5 = (TextView) findViewById( R.id.fed_locationText );
	    TV5.setText( "" );
	    TextView TV6 = (TextView) findViewById( R.id.farm_colorText );
	    TV6.setText( "" );
	    TextView TV7 = (TextView) findViewById( R.id.farm_locationText);
	    TV7.setText( "" );
    	eid_tag_color_spinner = (Spinner) findViewById(R.id.eid_tag_color_spinner);
		eid_tag_color_spinner.setSelection(0);

		eid_tag_location_spinner = (Spinner) findViewById(R.id.eid_tag_location_spinner);
		eid_tag_location_spinner.setSelection(0);


	    fedtagid = 0;
    	farmtagid = 0;
    	eidtagid = 0;
    	// make the alert button normal and disabled
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false); 
    	//	make the scan button normal
    	btn = (Button) findViewById( R.id.scan_eid_btn );
    	btn.getBackground().setColorFilter(null);
    	
    }
 // user clicked 'Search Fed' button
    public void searchFedTag( View v )
    	{
    	String          cmd;
    	TextView		TV = (TextView) findViewById( R.id.inputText );
    	String			fed = TV.getText().toString();
    	Integer			ii;
    	// Hide the keyboard when you click the button
    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    	
// 		Start of the actual code to process the button click
    	if( fed != null && fed.length() > 0 )
    		{
//			Search for the sheep with the entered federal tag number. 
//    		assumes no duplicate federal tag numbers, ok for our flock not ok for the general case
    		
    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
    				"id_info_table.id_infoid, id_info_table.tag_date_off, sheep_table.alert01 " +
    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
    				"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", fed);
 //   		Log.i("Convert", cmd);
    		}	
    	else
    	{
    		return;
     	}
    	Object crsr = dbh.exec( cmd );   	
    	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
    		{ // no sheep with that federal tag in the database so clear out and return
    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheepnameText );
        	TV.setText( "Cannot find this sheep." );
        	return;
    		}
// This section would allow for multiple sheep with same tag if we implement next and previous
//    	buttons but is commented out for now as our sheep have unique federal tags
//    	if( dbh.getSize() >1){
//
// 			Enable the previous and next record buttons
//    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
//    		btn2.setEnabled(true);  
//    		//	Set up the various pointers and cursor data needed to traverse the sequence
//    		recNo    = 1;
//    		cursor   = (Cursor) crsr;
//    		nRecs    = cursor.getCount();
//    		colNames = cursor.getColumnNames();
//    		cursor.moveToFirst();
//    	}
    	fedtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database
		Log.i("Convert", String.valueOf(fedtagid));
		
    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	TextView TV2 = (TextView) findViewById(R.id.fedText)	;
    	TV2.setText(dbh.getStr(4));
    	TextView TV3 = (TextView) findViewById(R.id.fed_colorText);
    	TV3.setText(dbh.getStr(3));
    	TextView TV4 = (TextView) findViewById(R.id.fed_locationText);
    	TV4.setText(dbh.getStr(5));
    	ii = dbh.getInt(1);
    	thissheep_id = ii;
    	
    	// Now we need to check and see if there is an alert for this sheep
       	String alert_text = dbh.getStr(8);
       	Log.i("in find fed ", "Alert Text is " + alert_text);
//    	Now to test of the sheep has an alert and if so then display the alert & set the alerts button to red
		if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
	       	// make the alert button red
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	//	testing whether I can put up an alert box here without issues
	    	showAlert(v);
		}
     	
//		Now we need to get the farm tag for that sheep and fill the display with data
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
		"id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
		"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", ii);

    	//   	Log.i("Convert", cmd);    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a farm tag installed
			TV = (TextView) findViewById( R.id.farm_colorText );
			TV.setText( "No tag" );
    	} else {
    		TextView TV5 = (TextView) findViewById(R.id.farmText)	;
    		TV5.setText(dbh.getStr(4));
    		TextView TV6 = (TextView) findViewById(R.id.farm_colorText);
    		TV6.setText(dbh.getStr(3));
    		TextView TV7 = (TextView) findViewById(R.id.farm_locationText);
    		TV7.setText(dbh.getStr(5));
    		ii = dbh.getInt(1);
    		farmtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database
    	}
    	}
// 	user clicked 'Search Farm Tag' button
    public void searchFarmTag( View v )
    	{
    	String          cmd;
    	TextView		TV = (TextView) findViewById( R.id.inputText );
    	String			farm = TV.getText().toString();
    	Integer			ii;
    	// Hide the keyboard when you click the button
    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    	
// 		Start of the actual code to process the button click
    	if( farm != null && farm.length() > 0 )
    		{
//			Search for the sheep with the entered farm tag number. 
//    		assumes no duplicate farm tag numbers, ok for our flock not ok for the general case
    		
    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
    				"id_info_table.id_infoid, id_info_table.tag_date_off, sheep_table.alert01 " +
    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
    				"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", farm);
    		
//    		Log.i("Convert", "building command ");
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
    	
    	farmtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database
    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	TV = (TextView) findViewById(R.id.farmText)	;
    	TV.setText(dbh.getStr(4));
    	TV = (TextView) findViewById(R.id.farm_colorText);
    	TV.setText(dbh.getStr(3));
    	TV = (TextView) findViewById(R.id.farm_locationText);
    	TV.setText(dbh.getStr(5));
    	ii = dbh.getInt(1);
    	thissheep_id = ii;
    	
    	// Now we need to check and see if there is an alert for this sheep
       	String alert_text = dbh.getStr(8);
//    	Now to test of the sheep has an alert and if so then set the alerts button to red
		if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
	       	// make the alert button red and enable it and pop up the alert text
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	showAlert(v);
		}
	
//		Now we need to get the rest of the tags and fill the display with data
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
		"id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
		"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", ii);
    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
    	
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a federal tag installed
			TV = (TextView) findViewById( R.id.fed_colorText );
			TV.setText( "No tag" );
    	} else {
        	fedtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database   	
        	TV = (TextView) findViewById(R.id.fedText)	;
        	TV.setText(dbh.getStr(4));
        	TV = (TextView) findViewById(R.id.fed_colorText);
        	TV.setText(dbh.getStr(3));
        	TV = (TextView) findViewById(R.id.fed_locationText);
        	TV.setText(dbh.getStr(5));
        	ii = dbh.getInt(1);
    	}
    	}    
        

    // user clicked 'remove fed tag' button   
    public void removeFedTag( View v )
    	{
    	if( fedtagid != 0 )
    		{
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.delete_tag )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- remove the federal tag
    	        	   // add a tag_date_off of today to the tag   	       		
    	        	   	String today = TodayIs();
    	        	   	Log.i("removefedtag", String.valueOf(fedtagid));
    	        	   	String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", fedtagid );
    	       		   	dbh.exec( cmd );
//    	    		   	Clear the display of the tags
    	    		   	TextView TV = (TextView) findViewById(R.id.fedText)	;
    	           	    TV.setText(null);
    	           	    TV = (TextView) findViewById(R.id.fed_colorText);
    	           		TV.setText("");
    	           		TV = (TextView) findViewById(R.id.fed_locationText);
    	           		TV.setText("");
    	           		fedtagid = 0;
    	               }
    	       });
    		builder.setNegativeButton( R.string.cancel_btn, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User cancelled the dialog
    	           }
    	       });
    		
    		AlertDialog dialog = builder.create();
    		dialog.show();
    		}
    	}    
    
    // user clicked 'remove farm tag' button   
    public void removeFarmTag( View v )
    	{
    	if( farmtagid != 0 )
    		{
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.delete_tag )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- remove the farm tag
    	        	   //add a tag_date_off of today to the tag
    	        	   String today = TodayIs();
 //   	        	   Log.i("removefarmtag", today);
    	        	   Log.i("removefarmtag", String.valueOf(farmtagid));
    	       		   String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", farmtagid );
    	    		   dbh.exec( cmd );
//   	    		   	Clear the display of the tags
   	    		   	TextView TV = (TextView) findViewById(R.id.farmText)	;
   	           	    TV.setText(null);
   	           		TV = (TextView) findViewById(R.id.farm_colorText);
   	           		TV.setText("");
   	           		TV = (TextView) findViewById(R.id.farm_locationText);
   	           		TV.setText("");  
   	           		farmtagid = 0;
   	           		}
    	       });
    		builder.setNegativeButton( R.string.cancel_btn, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User cancelled the dialog
    	           }
    	       });
    		
    		AlertDialog dialog = builder.create();
    		dialog.show();
    		}
    	}  
    private String TodayIs() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) ;
	}
    private String Make2Digits(int i) {
		if (i < 10) {
			return "0" + i;
		} else {
			return Integer.toString(i);
		}
	}
//     user clicked 'Scan' button    
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
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0x0000FF00, 0xff00ff00));
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
		}    	    	
    }
    
    public void updateDatabase( View v ){
    	Object crsr;
    	String sheepnameText, fedText, fed_colorText, fed_locationText;
    	String farmText, farm_colorText, farm_locationText, eidText, eid_colorText, eid_locationText;
    	int		fed_colorid, farm_colorid, eid_colorid, fed_locationid, farm_locationid, eid_locationid;
    	int		fed_number, farm_number;
    	
    	eid_colorid = 0;
    	eid_locationid = 0;
    	eidText = null;
    	
    	// Get the values from the UI screen
    	TextView TV = (TextView) findViewById( R.id.sheepnameText );
    	sheepnameText = TV.getText().toString();
    	Log.i("update everything ", "sheep name " + sheepnameText);
    	
    	Log.i("update everything ", "sheep_id is " + String.valueOf(thissheep_id));
    	
    	Log.i("update everything ", "fed info record " + fedtagid);
   
    	TV  = (TextView) findViewById( R.id.fedText );
	    fedText = TV.getText().toString();
//	    fed_number = Integer.valueOf(fedText);
	    Log.i("update everything ", "fed tag " + fedText);
//	    Log.i("update everything ", "fed tag integer " + String.valueOf(fed_number));
	    
	    Log.i("update everything ", "farm info record " + farmtagid);
	    TV  = (TextView) findViewById( R.id.farmText );
	    farmText = TV.getText().toString();
//	    farm_number = Integer.valueOf(farmText);
	    Log.i("update everything ", "farm number " + farmText);
	    	    
	    Log.i("update everything ", "eid info record " + eidtagid);
	    TV  = (TextView) findViewById( R.id.eidText );
	    eidText = TV.getText().toString();	
	    Log.i("update everything ", "EID Tag " + eidText);
	    
	    if (eidText != null && !eidText.isEmpty()){
	    	// Get the data from the EID tag section of the screen
	    	eid_tag_color_spinner = (Spinner) findViewById(R.id.eid_tag_color_spinner);
	    	eid_tag_location_spinner = (Spinner) findViewById(R.id.eid_tag_location_spinner);
	    	
	     	eid_tag_color_label = eid_tag_color_spinner.getSelectedItem().toString();
	    	Log.i("update everything ", "EID color is " + eid_tag_color_label);
	    	cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
	    			"where tag_color_name='%s'", eid_tag_color_label);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        eid_colorid = dbh.getInt(0);
	        
	    	eid_tag_location_label = eid_tag_location_spinner.getSelectedItem().toString();
	    	Log.i("update everything ", "EID location is " + eid_tag_location_label);
	    	cmd = String.format("select id_location_table.id_locationid from id_location_table " +
	    			"where id_location_abbrev='%s'", eid_tag_location_label);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        eid_locationid = dbh.getInt(0);
	    }
	 
	    //	Need to add tests to see what data we really have and only update if there is some
	    String today = TodayIs();
	    if (fedtagid != 0) {
	    	// 	update the Federal tag data if it has changed?
	    	//	not implemented at this time. Assumed we either are adding new tags or taking off tags first
	    	//	no update of an existing tag record is done in this module.
	    	Log.i("updatefed", " tag record id is not zero, but has data changed?");
	    }
	    	else {
	    		// fedtagid is zero so need to test whether there is a federal tag and add a record if there is one
	    		if (fedText != null && !fedText.isEmpty()){
	    			//have a federal tag but no fedtagid so add a new record;
	    			fed_number = Integer.valueOf(fedText);
	    		    TV = (TextView) findViewById( R.id.fed_colorText );
	    		    fed_colorText = TV.getText().toString();
	    		    Log.i("update everything ", "fed color " + fed_colorText);	    
	    		    cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
	    	    			"where tag_color_name='%s'", fed_colorText);
	    	    	crsr = dbh.exec( cmd );
	    	        cursor   = ( Cursor ) crsr;
	    	        dbh.moveToFirstRecord();
	    	        fed_colorid = dbh.getInt(0);
	    	        Log.i("update everything ", "fed color integer " + String.valueOf(fed_colorid));
	    	        	    
	    		    TV = (TextView) findViewById( R.id.fed_locationText );
	    		    fed_locationText = TV.getText().toString();
	    		    Log.i("update everything ", "fed location " + fed_locationText);
	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
	    	    			"where id_location_abbrev='%s'", fed_locationText);
	    	    	crsr = dbh.exec( cmd );
	    	        cursor   = ( Cursor ) crsr;
	    	        dbh.moveToFirstRecord();
	    	        fed_locationid = dbh.getInt(0);
	    	        Log.i("update everything ", "fed location integer " + String.valueOf(fed_locationid));
	    	      
	    			Log.i("updatefed", " tag record id is 0 but have fed tag data need to add a new record to id_info_table here");
	    			Log.i("update everything ", "sheep_id is " + String.valueOf(thissheep_id));
	    			Log.i("update everything ", "fed color integer " + String.valueOf(fed_colorid));
	    			Log.i("update everything ", "fed location integer " + String.valueOf(fed_locationid));
	    			Log.i("update everything ", "today " + today);
	    			Log.i("update everything ", "fed tag integer " + String.valueOf(fed_number));
	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
	    					"values ( %s, 1, %s, %s, %s, '%s', %s )", thissheep_id, fed_colorid, fed_colorid, fed_locationid, today, fed_number);
	    			Log.i("update everything ", "before cmd " + cmd);
	    			dbh.exec( cmd );	
	    			Log.i("update everything ", "after cmd exec");
	    		}
	    		else{
	    			// no federal tag to enter so return
	    			Log.i("updatefed", " no federal tag so nothing to do");	
	    		}
	    	}
	    
//	    //Update the Farm Tag data
	    if (farmtagid != 0) {
	    	// update the Farm tag data
	    	//	not implemented at this time. Assumed we either are adding new tags or taking off tags first
	    	//	no update of an existing tag record is done.
	    	Log.i("updatefarm", " tag record id is not zero, but has data changed?");
		    }
	    	else {
	    		// farmtagid is zero so need to test whether there is a farm tag and add a record if there is one
	    		if (farmText != null && !farmText.isEmpty()){
	    			
	    		    farm_number = Integer.valueOf(farmText);
	    		    TV = (TextView) findViewById( R.id.farm_locationText);
	    		    farm_locationText = TV.getText().toString();
	    		    Log.i("update everything ", "farm location " + farm_locationText);
	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
	    	    			"where id_location_abbrev='%s'", farm_locationText);
	    	    	crsr = dbh.exec( cmd );
	    	        cursor   = ( Cursor ) crsr;
	    	        dbh.moveToFirstRecord();
	    	        farm_locationid = dbh.getInt(0);
	    	        Log.i("update everything ", "farm color integer " + String.valueOf(farm_locationid));
	    	        
	    		    TV = (TextView) findViewById( R.id.farm_colorText );
	    		    farm_colorText = TV.getText().toString();
	    		    Log.i("update everything ", "farm color " + farm_colorText);
	    		    cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
	    	    			"where tag_color_name='%s'", farm_colorText);
	    	    	crsr = dbh.exec( cmd );
	    	        cursor   = ( Cursor ) crsr;
	    	        dbh.moveToFirstRecord();
	    	        farm_colorid = dbh.getInt(0);
	    	        Log.i("update everything ", "farm location integer " + String.valueOf(farm_locationid));

	    			//have a farm tag but no farmtagid so add a new record;
	    			Log.i("updatefarm", " tag record id is 0 but have farm tag data need to add a new record to id_info_table here");
	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
	    					"values ( %s, 4, %s, %s, %s, '%s', %s )", thissheep_id, farm_colorid, farm_colorid, farm_locationid, today, farm_number);
	    			dbh.exec( cmd );	
	    		}
	    		else{
	    			// no farm tag to enter so return
	    			Log.i("updatefarm", " no farm tag so nothing to do");
	    			
	    		}
	    	}
//	    //Update the EID Tag data
	    if (eidtagid != 0) {
	    	// update the EID tag data
	    	Log.i("updateEID", " tag record id is not zero, needs update here");
		    }
	    	else {
	    		// eidtagid is zero so need to test whether there is an EID tag and add a record if there is one
	    		if (eidText != null && !eidText.isEmpty()){
	    			//have an EID tag but no eidtagid so add a new record;
	    			Log.i("updateEID", " tag record id is 0 need to add a new record to id_info_table here");
	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
	    					"values ( %s, 2, %s, %s, %s, '%s', '%s' )", thissheep_id, eid_colorid, eid_colorid, eid_locationid, today, eidText);
	    			dbh.exec( cmd );	
	    		}
	    		else{
	    			// no EID tag to enter so return
	    			Log.i("updateEID", " no eid tag so nothing to do");
	    		}
	    	}
	    cursor.close();
	    clearBtn( v );
	    }
    public void addNewTag( View v ){
 //   	String temp;
    	Object crsr;
    	
       	btn = (Button) findViewById( R.id.update_display_btn );
    	btn.setEnabled(true); 
    	new_tag_number = null;
       	// Fill the Tag Type Spinner
     	//	Decided to only allow Federal and Farm as tag type options for this task
    	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
    	tag_types = new ArrayList<String>();      	
    	tag_types.add("Select a Type");
    	tag_types.add("Federal");
    	tag_types.add("Farm");
    	
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_type_spinner.setAdapter (dataAdapter);
		tag_type_spinner.setSelection(0);	
    	
    	// Fill the Tag Color Spinner
    	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
     	tag_colors = new ArrayList<String>();       	
        // Select All fields from tag colors to build the spinner
        cmd = "select * from tag_colors_table";
        crsr = dbh.exec( cmd );  
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	tag_colors.add("Select a Color");
         // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		tag_colors.add(cursor.getString(2));
    	}
    	cursor.close();
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_colors);

		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_color_spinner.setAdapter (dataAdapter);
		tag_color_spinner.setSelection(1);
				
    	// Fill the Tag Location Spinner
		// Only allow ear locations for tags for this task
		tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
		tag_locations = new ArrayList<String>();        
		tag_locations.add("Select a Location");
		tag_locations.add("Right Ear");		
		tag_locations.add("Left Ear");
		
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_locations);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_location_spinner.setAdapter (dataAdapter);
		tag_location_spinner.setSelection(1);
	}
    
    public void showAlert (View v)
    {
    		String	alert_text;
            String          cmd;    
            Object 			crsr;        
    		// Display alerts here   	
    				AlertDialog.Builder builder = new AlertDialog.Builder( this );
    				cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_id =%d", thissheep_id);
//    				Log.i("get alert ", cmd);  
    				crsr = dbh.exec( cmd );
    		        cursor   = ( Cursor ) crsr;
    		        dbh.moveToFirstRecord();		       
    		        alert_text = (dbh.getStr(0));
//    		        Log.i("get alert ", alert_text); 
    				builder.setMessage( alert_text )
    			           .setTitle( R.string.alert_warning );
    				builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int idx) {
    			               // User clicked OK button   	  
    			               }
    			       });		
    				AlertDialog dialog = builder.create();
    				dialog.show();
    				cursor.close();	
    	}
    
    public void updateTag( View v ){
    	Object 			crsr;
    	String 			cmd;
    	TextView 		TV;
    	// Get the data from the add tag section of the screen
    	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
    	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
    	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
    	
    	tag_type_label = tag_type_spinner.getSelectedItem().toString();
//    	Log.i("updateTag", "Tag type is " + tag_type_label);
    	tag_color_label = tag_color_spinner.getSelectedItem().toString();
//    	Log.i("updateTag", "Tag color is " + tag_color_label);
    	tag_location_label = tag_location_spinner.getSelectedItem().toString();
//    	Log.i("updateTag", "Tag location is " + tag_location_label);
    	
    	TV  = (TextView) findViewById( R.id.new_tag_number);
    	new_tag_number = TV.getText().toString();
//    	Log.i("before if", " new tag number " + new_tag_number);
    	
    	// 	Fill the new tag data with where it is in the database tables
    	//	Integers to hold the info new_tag_type, new_tag_color, new_tag_location
    	if (tag_type_label == "Select a Type" || tag_location_label == "Select a Location" || tag_color_label == "Select a Color"
    			|| TV.getText().toString().isEmpty()) {
    		new_tag_type = 0;
    		// Missing data so  display an alert 	
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.convert_fill_fields )
    	           .setTitle( R.string.convert_fill_fields );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button 
     	    		   return;
    	               }
    	       });		
    		AlertDialog dialog = builder.create();
    		dialog.show();		   		
    	}else
    	{
    		cmd = String.format("select id_type_table.id_typeid from id_type_table " +
			"where idtype_name='%s'", tag_type_label);
    		crsr = dbh.exec( cmd );
    		cursor   = ( Cursor ) crsr;
    		dbh.moveToFirstRecord();
    		new_tag_type = dbh.getInt(0);
    		
       		cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
       				"where tag_color_name='%s'", tag_color_label);
       	    crsr = dbh.exec( cmd );
    		cursor   = ( Cursor ) crsr;
    		dbh.moveToFirstRecord();
    		new_tag_color = dbh.getInt(0);

    		cmd = String.format("select id_location_table.id_locationid, id_location_table.id_location_abbrev from id_location_table " +
			"where id_location_name='%s'", tag_location_label);
    		crsr = dbh.exec( cmd );
    		cursor   = ( Cursor ) crsr;
    		dbh.moveToFirstRecord();
    		new_tag_location = dbh.getInt(0);
//    		Log.i("New Location ID ", String.valueOf(new_tag_location));
     		tag_location_label = dbh.getStr(1);
//    		Log.i("New Location ", tag_location_label);
    		
        	if (new_tag_type == 1){
        		//	Federal Tag so update federal section and set needs database update
        		Log.i("in if", "Got a new federal tag type");
        	    TV  = (TextView) findViewById( R.id.fedText );
        	    TV.setText(new_tag_number);
        	    TV = (TextView) findViewById( R.id.fed_colorText );
        	    TV.setText(tag_color_label);
        	    TV = (TextView) findViewById( R.id.fed_locationText );
        	    TV.setText(tag_location_label);
        	    fedtagid = 0;
         	}
        	if (new_tag_type == 4){
        		//	Farm Tag so update farm section and set needs database update
        		Log.i("in if", "Got a new farm tag type");
        	    TV  = (TextView) findViewById( R.id.farmText );
        	    TV.setText(new_tag_number);
        	    TV = (TextView) findViewById( R.id.farm_colorText );
        	    TV.setText(tag_color_label);
        	    TV = (TextView) findViewById( R.id.farm_locationText );
        	    TV.setText(tag_location_label);
        	    farmtagid = 0;
        	}
        	//	Clear out the add tag section    	
        	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
        	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
        	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
        	TV  = (TextView) findViewById( R.id.new_tag_number);
        	tag_type_spinner.setSelection(0);
        	tag_color_spinner.setSelection(0);
        	tag_location_spinner.setSelection(0);
        	TV.setText( "" );
        	}
     	}
}
