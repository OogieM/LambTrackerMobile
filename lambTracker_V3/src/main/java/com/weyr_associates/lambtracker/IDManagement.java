package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import android.content.ComponentName;
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
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

public class IDManagement extends ListActivity {
	private DatabaseHandler dbh;
	int             fedtagid, farmtagid, eidtagid, paintid, tattooid, splitid, notchid; // These are record IDs not sheep IDs
	public Cursor 	cursor, cursor2;
	public Object 	crsr, crsr2;
	public int 		thissheep_id, new_tag_type, new_tag_color, new_tag_location;
	private long selectedItem;
	public Button btn;
	public String tag_type_label, tag_color_label, tag_location_label, new_tag_number, eid_tag_color_label ;
	public String eid_tag_location_label, eidText, alert_text;
	public String thissheep_name;
	public Spinner tag_type_spinner, tag_type_spinner2, tag_location_spinner, tag_color_spinner, eid_tag_color_spinner, eid_tag_location_spinner;
	public List<String> tag_types, tag_locations, tag_colors;
	public  List <String> new_tag_numbers;
	public List <String> new_tag_colors, new_tag_locations, new_tag_types;
	ArrayAdapter<String> dataAdapter;
	String     	cmd;
	Integer 	i;
	private int			    recNo;
	public int nRecs, nRecs2;
	public SimpleCursorAdapter myadapter;
	public ListAdapter myadapter2; 
	
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
				stopService(new Intent(IDManagement.this, eidService.class));
				
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
			Log.i("Convert", "is not, start it");
			startService(new Intent(IDManagement.this, eidService.class));
			doBindService();
		}
		Log.i("Convert", "Done isRunning.");
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
	   	//	make the scan eid button red
    	btn = (Button) findViewById( R.id.scan_eid_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	// 	Display the EID number
    	TextView TV = (TextView) findViewById (R.id.inputText);
    	TV.setText( LastEID );   	
		Log.i("Convert", "Got EID");
       	// Fill the Tag Type Spinner
    	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);

    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_type_spinner2.setAdapter (dataAdapter);
		tag_type_spinner2.setSelection(0);	
		Log.i ("in got EID", " after set tag_type_spinner");
		
//    	// Fill the Tag Color Spinner
    	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_colors);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_color_spinner.setAdapter (dataAdapter);
		tag_color_spinner.setSelection(0);
		Log.i ("in got EID", " after set tag_color_spinner");	
		
    	// Fill the Tag Location Spinner
		tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_locations);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_location_spinner.setAdapter (dataAdapter);
		tag_location_spinner.setSelection(0);
		Log.i ("in got EID", " after set tag_location_spinner");
//		btn = (Button) findViewById( R.id.update_display_btn );
//		if (btn.isEnabled()){
//			//	 We are adding a new EID tag so put it in the new tag field
//			TV = (TextView) findViewById (R.id.new_tag_number);
//	    	TV.setText( LastEID );  
//	    	//	Set the tag type to default electronic
//	    	tag_type_spinner2.setSelection(2);
//	    	//	Set the tag color to be the default EID color
//	    	// TODO
//	    	//	need to fix this from the defaults but for now is set to yellow
//	    	tag_color_spinner.setSelection(1);
//	    	//	Set the tag location to default Right Ear
//	    	// TODO
//	    	//	need to fix this from the defaults but for now is set to RE
//	    	tag_location_spinner.setSelection(1);	    	
//		}
			
	}	
	
/////////////////////////////////////////////////////	
//  On Create Section	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_management);
        String dbname = getString(R.string.real_database_file); 
    	dbh = new DatabaseHandler( this, dbname );
//    	Object crsr;
 //////////////////////////////////// 
		CheckIfServiceIsRunning();
		Log.i("Convert", "back from isRunning");  	
////////////////////////////////////    	
    	  
		
		thissheep_id = 0;
    	//	make the remove tag buttons red
		Log.i("onCreate", " before setting remove tag buttons red");
//    	btn = (Button) findViewById( R.id.remove_fedtag_btn );
//    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
//    	Log.i("onCreate", " remove fed tag button is red");
    	btn = (Button) findViewById( R.id.remove_farmtag_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	Log.i("onCreate", " remove farm tag button is red");
//    	btn = (Button) findViewById( R.id.remove_paint_btn );
//    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
//    	Log.i("onCreate", " remove paint tag button is red");
//    	btn = (Button) findViewById( R.id.remove_eidtag_btn );
//    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	Log.i("onCreate", " after setting remove tag buttons red");
   	   	   
    	//	make the scan eid button red
    	btn = (Button) findViewById( R.id.scan_eid_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	
    	//	Disable the alert button until we have an alert for this sheep
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false);
    	Log.i("onCreate", " after disable alert button");
    	
    	//	Disable the Next Record and Prev. Record button until we have multiple records
    	btn = (Button) findViewById( R.id.next_rec_btn );
    	btn.setEnabled(false); 
    	btn = (Button) findViewById( R.id.prev_rec_btn );
    	btn.setEnabled(false);
    	
    	//	Disable the bottom update display button until we choose to add or update
//       	btn = (Button) findViewById( R.id.update_display_btn );
//    	btn.setEnabled(false); 
//    	
    	//	Set my flags and new tag number as required
    	//	id ones are set to zero meaning no data to change initially
    	fedtagid = 0;
    	farmtagid = 0;
    	eidtagid = 0;
    	paintid = 0;
    	tattooid = 0;
    	splitid = 0;
    	notchid = 0;
    	new_tag_number = null;
    	
    	// Fill the Tag Type Spinner
     	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
    	tag_types = new ArrayList<String>();      	
    	
    	// Select All fields from id types to build the spinner
        cmd = "select * from id_type_table";
        crsr2 = dbh.exec( cmd );  
        cursor2   = ( Cursor ) crsr2;
    	dbh.moveToFirstRecord();
    	tag_types.add("Select a Type");
         // looping through all rows and adding to list
    	for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext()){
    		tag_types.add(cursor2.getString(1)); //get the idtype_name field
    	}
    	
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_type_spinner.setAdapter (dataAdapter);
		//	Default to looking up federal tag number
		//	Should be a preference as to which type is the default look-up 
		tag_type_spinner.setSelection(1);	
		
    	//	Set up the location and color spinners
//    	eid_tag_color_spinner = (Spinner) findViewById(R.id.eid_tag_color);
    	tag_colors = new ArrayList<String>();        
        // Select All fields from tag colors to build the spinner
        cmd = "select * from tag_colors_table";
        crsr2 = dbh.exec( cmd );  
        cursor2   = ( Cursor ) crsr2;
    	dbh.moveToFirstRecord();
    	tag_colors.add("Select a Color");
         // looping through all rows and adding to list
    	for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext()){
    		tag_colors.add(cursor2.getString(2)); // get the tag_color_name field
    	}

		tag_locations = new ArrayList<String>(); 
		 // Select All fields from tag locations to build the spinner
        cmd = "select * from id_location_table";
        crsr2 = dbh.exec( cmd );  
        cursor2   = ( Cursor ) crsr2;
    	dbh.moveToFirstRecord();
		tag_locations.add("Select a Location");
		for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext()){
			tag_locations.add(cursor2.getString(2)); // get the id_location_abbrev field
    	}
    }
    
    // user clicked the 'back' button
    public void backBtn( View v )
	    {
		doUnbindService();
		stopService(new Intent(IDManagement.this, eidService.class));
		try {
//			Log.i("Back Button", " In try stmt cursor");   
			cursor.close();
		}
		catch (Exception e) {
//			Log.i("Back Button", " In catch stmt cursor");  
			// In this case there is no adapter so do nothing
		}
       	dbh.closeDB();
    	clearBtn( null );   	
    	finish();
	    }
 
    // user clicked the 'help' button
    public void helpBtn( View v )
    {
   	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_manage_id )
	           .setTitle( R.string.help_warning );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int idx) {
	               // User clicked OK button 
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();		
    }
    
    // user clicked 'clear' button
    public void clearBtn( View v )
	    {
	    // clear out the display of everything
    	TextView TV = (TextView) findViewById( R.id.inputText );
    	TV.setText( "" );		
    	TV = (TextView) findViewById( R.id.sheepnameText );
	    TV.setText( "" );
//	    TV  = (TextView) findViewById( R.id.eidText );
//	    TV.setText( "" );
//	    TV  = (TextView) findViewById( R.id.fedText );
//	    TV.setText( "" );
//	    TV  = (TextView) findViewById( R.id.farmText );
//	    TV.setText( "" );
//	    TV  = (TextView) findViewById( R.id.paintText );
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.fed_colorText );
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.fed_locationText );
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.farm_colorText );
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.farm_locationText);
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.paint_colorText );
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.paint_locationText);
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.eid_colorText );
//	    TV.setText( "" );
//	    TV = (TextView) findViewById( R.id.eid_locationText);
//	    TV.setText( "" );
	    TV = (TextView) findViewById( R.id.new_tag_number);
	    TV.setText( "" );
	    //	Clear out the tag ids
	    fedtagid = 0;
    	farmtagid = 0;
    	eidtagid = 0;
    	paintid = 0;
    	tattooid = 0;
    	splitid = 0;
    	notchid = 0;
    	new_tag_number = null;
    	//	clear out the add tag spinners
    	try {
	    	tag_type_spinner2.setSelection(0);
	    	tag_color_spinner.setSelection(0);
	    	tag_location_spinner.setSelection(0);
    	}catch (Exception e){
    		// 	couldn't clear an empty spinner
    	}   	
    	// make the alert button normal and disabled
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false); 
	   	//	make the scan eid button red
    	btn = (Button) findViewById( R.id.scan_eid_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));   
    	// Clear out the list of tags
		Log.i("clear btn", "before changing myadapter");
		try {
			myadapter.changeCursor(null);
		}
		catch (Exception e) {
			// In this case there is no adapter so do nothing
		}
    }
    
	public void lookForSheep (View v){		
		Object crsr;
		Boolean exists;
		TextView TV;
        exists = true;
        // Hide the keyboard when you click the button
    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
      	//	Disable the Next Record and Prev. Record button until we have multiple records
    	btn = (Button) findViewById( R.id.next_rec_btn );
    	btn.setEnabled(false); 
    	btn = (Button) findViewById( R.id.prev_rec_btn );
    	btn.setEnabled(false);
    	
//    	crsr = null;
//    	fedtagid = 0;
//    	farmtagid = 0;
//    	eidtagid = 0;
//    	paintid = 0;
    	
    	 TV = (EditText) findViewById( R.id.inputText );
         String	tag_num = TV.getText().toString();
     	
         Log.i("LookForSheep", " got to lookForSheep with Tag Number of " + tag_num);
         Log.i("LookForSheep", " got to lookForSheep with Tag type of " + tag_type_spinner.getSelectedItemPosition());
         exists = tableExists("sheep_table");
         
        if (exists){       	
    			switch (tag_type_spinner.getSelectedItemPosition()){
    				case 1:
    				case 2:
    				case 3:
    				case 4:
    				case 5:
    					if( tag_num != null && tag_num.length() > 0 ){       		
    						// Get the sheep id from the id table for this tag number and selected tag type
			        		cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
				        			"and id_info_table.tag_type='%s' and (id_info_table.tag_date_off is null or" +
				        			" id_info_table.tag_date_off = '') "
				        			, tag_num , tag_type_spinner.getSelectedItemPosition());  
				        	Log.i("searchByNumber", "command is " + cmd);
				        	crsr = dbh.exec( cmd );
				    		cursor   = ( Cursor ) crsr; 
				        	recNo    = 1;
							nRecs    = cursor.getCount();
							Log.i("searchByNumber", " nRecs = "+ String.valueOf(nRecs));
				        	dbh.moveToFirstRecord();
				        	Log.i("searchByNumber", " the cursor is of size " + String.valueOf(dbh.getSize()));
				        	if( dbh.getSize() == 0 ){ 
				        		// no sheep with that  tag in the database so clear out and return
					    		clearBtn( v );
					    		TV = (TextView) findViewById( R.id.sheepnameText );
					        	TV.setText( "Cannot find this sheep." );
					        	return;
					    	}
				        	thissheep_id = dbh.getInt(0);
				        
				        	Log.i("searchByNumber", "This sheep is record " + String.valueOf(thissheep_id));
				        	if (nRecs >1){
				        		//	Have multiple sheep with this tag so enable next button
				            	btn = (Button) findViewById( R.id.next_rec_btn );
				            	btn.setEnabled(true);       		
				        	}
				        	Log.i("searchByNumber", " Before finding all tags and formatting the screen");	        	
				        	// Now need to go do the get tags and stuff. 
				        	findTagsShowAlert (v, thissheep_id);
				        	break;
    					}
				    case 6:
//				    		got a split
//				    		Assume no split ears at this time. 
//				    		Needs modification for future use
				    	TV = (TextView) findViewById( R.id.sheepnameText );
			        	TV.setText( "Cannot search on splits yet." );
//				    	// TODO
//				        break;
//    					if( tag_num != null && tag_num.length() > 0 ){       		
    						// Get the sheep id from the id table with split ears
//			        		cmd = String.format( "select sheep_id from id_info_table where "+
//				        			" id_info_table.tag_type='%s' and (id_info_table.tag_date_off is null or" +
//				        			" id_info_table.tag_date_off = '') "
//				        			, tag_type_spinner.getSelectedItemPosition());  
//				        	Log.i("searchByNumber", "command is " + cmd);
//				        	crsr = dbh.exec( cmd );
//				    		cursor   = ( Cursor ) crsr; 
//				        	recNo    = 1;
//							nRecs    = cursor.getCount();
//							Log.i("searchByNumber", " nRecs = "+ String.valueOf(nRecs));
//				        	dbh.moveToFirstRecord();
//				        	Log.i("searchByNumber", " the cursor is of size " + String.valueOf(dbh.getSize()));
//				        	if( dbh.getSize() == 0 ){ 
//				        		// no sheep with that  tag in the database so clear out and return
//					    		clearBtn( v );
//					    		TV = (TextView) findViewById( R.id.sheepnameText );
//					        	TV.setText( "Cannot find this sheep." );
//					        	return;
//					    	}
//				        	thissheep_id = dbh.getInt(0);
//				        
//				        	Log.i("searchByNumber", "This sheep is record " + String.valueOf(thissheep_id));
//				        	if (nRecs >1){
//				        		//	Have multiple sheep with this tag so enable next button
//				            	btn = (Button) findViewById( R.id.next_rec_btn );
//				            	btn.setEnabled(true);       		
//				        	}
//				        	Log.i("searchByNumber", " Before finding all tags");	        	
//				        	// Now need to go do the get tags and stuff. 
//				        	findTagsShowAlert (v, thissheep_id);
				        	break;
//    					}
				    case 7:
				    	//	got a notch
				    	//	Assume no notches at this time. 
				    	//	Needs modification for future use
				    	TV = (TextView) findViewById( R.id.sheepnameText );
			        	TV.setText( "Cannot search on notches yet." );
				    	// TODO
				        break;
				    case 8:
				    	//	got a name				    	
				    	
			        	tag_num = "%" + tag_num + "%";
			        	cmd = String.format( "select sheep_id, sheep_name, alert01 from sheep_table where sheep_name like '%s'" +
			        			" and (remove_date is null or remove_date = '') "
			        			, tag_num );  
			        	Log.i("searchByName", "command is " + cmd);
			        	crsr = dbh.exec( cmd );
			    		cursor   = ( Cursor ) crsr; 
			        	recNo    = 1;
						nRecs    = cursor.getCount();
						Log.i("searchByName", " nRecs = "+ String.valueOf(nRecs));
			        	dbh.moveToFirstRecord();
			        	Log.i("searchByName", " the cursor is of size " + String.valueOf(dbh.getSize()));
			        	if( dbh.getSize() == 0 )
				    		{ // no current sheep with that name in the database so clear out and return
				    		clearBtn( v );
				    		TV = (TextView) findViewById( R.id.sheepnameText );
				        	TV.setText( "Cannot find this sheep." );
				        	return;
				    		}
			        	thissheep_id = dbh.getInt(0);
			        	thissheep_name = dbh.getStr(1);
			        	Log.i("searchByName", " the name is " + thissheep_name);
			        	alert_text = dbh.getStr(2);
			        	if (nRecs >1){
			        		//	Have multiple sheep with this name so enable next button
			            	btn = (Button) findViewById( R.id.next_rec_btn );
			            	btn.setEnabled(true);       		
			        	}
			        	// Now need to go do the get tags and stuff. 
			        	findTagsShowAlert (v, thissheep_id);        	
				        break;
    				} // end of case switch   			
    		}else {
    			clearBtn( null );
            	TV = (TextView) findViewById( R.id.sheepnameText );
                TV.setText( "Sheep Database does not exist." ); 
         	}
        
	}
 	
	public void findTagsShowAlert (View v, Integer thissheep_id){
		TextView TV;
		
		Log.i("find tags", "This sheep is record " + String.valueOf(thissheep_id));	
				
		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
				"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01 " +
				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
				"where id_info_table.sheep_id ='%s' and sheep_table.remove_date = '' and (id_info_table.tag_date_off is null or" +
				        			" id_info_table.tag_date_off = '') order by idtype_name asc", thissheep_id);
		Log.i("findtags", "command is " + cmd);
		crsr2 = dbh.exec( cmd ); 
		Log.i("findtags", " after second query to get all tags. found  " + String.valueOf(dbh.getSize()));	        	
		cursor2   = ( Cursor ) crsr2; 
		cursor2.moveToFirst();	
		
		nRecs2    = cursor2.getCount();
		Log.i("findtags", " nRecs2 is " + String.valueOf(nRecs2));
		
		if (nRecs2 > 0) {		
			thissheep_name = dbh.getStr(0);
			TV = (TextView) findViewById( R.id.sheepnameText );
		    TV.setText (thissheep_name);
		    String alert_text = dbh.getStr(8);
		}else {
			// the sheep with that tag in the database is gone
    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheepnameText );
        	TV.setText( "This sheep was removed." );
        	return;			
		}
//		TV = (TextView) findViewById( R.id.sheepnameText );
//	    TV.setText (thissheep_name);
//	   	Log.i("findtags ", "Alert Text is " + alert_text);
		Log.i("findtags", " before formatting results");
		
		//	Get set up to try to use the CursorAdapter to display all the tag data
		//	Select only the columns I need for the tag display section
	    String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
		Log.i("FormatRecord", "after setting string array fromColumns");
		//	Set the views for each column for each line. A tag takes up 1 line on the screen
	    int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
	    Log.i("FormatRecord", "after setting string array toViews");
	    myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor2 ,fromColumns, toViews, 0);
	    Log.i("FormatRecord", "after setting myadapter");
	    setListAdapter(myadapter);
	    Log.i("FormatRecord", "after setting list adapter");
	    
	  
	    
	    // TODO
	    // 	Now I need to figure out how to get the items selected from the list of tags and pass that on
	    //	to the remove tag code as needed
	    
	    
//	    myadapter.setOnItemClickListener (new OnItemClickListener(){
//	    	
//	    	public void onItemClick(adapterView<?> parent, View v, int pos,long id){
//	    		cursor2.moveToPosition(pos);
//	    		int rowId = cursor2.getInt(cursor2.getColumnIndexOrThrow("_id"));
//	    		
//	    	}
//	    })
	    
	    
////		try {
			// Now we need to check and see if there is an alert for this sheep
//			String alert_text = dbh.getStr(8);
//			Now to test of the sheep has an alert and if so then display the alert & set the alerts button to red
			if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
		       	// make the alert button red
		    	Button btn = (Button) findViewById( R.id.alert_btn );
		    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
		    	btn.setEnabled(true); 
		    	//	testing whether I can put up an alert box here without issues
		    	showAlert(v);
			}
	}	
	
	// Catches clicks and updates selectedItem - can't believe this is necessary in this day and age
	@Override protected void onListItemClick (ListView l, View v, int position, long id)
	{
		Log.i("IDManagement", "Listitemclick set id" + String.format("%d", id));
		selectedItem = id;

	}

			// OLD XML file data
//	        <GridLayout
//            android:id="@+id/grid3"
//            android:layout_width="wrap_content"
//            android:layout_height="wrap_content"
//            android:columnCount="6"
//            android:rowCount="9" >
//
//            <TextView
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="0"
//                android:layout_gravity="left"
//                android:layout_row="0"
//                android:inputType="none"
//                android:text="@string/federal_id_lbl" />
//
//            <EditText
//                android:id="@+id/fedText"
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="0"
//                android:layout_gravity="left"
//                android:layout_row="2"
//                android:inputType="number"
//                android:textSize="18sp"
//                android:typeface="monospace" >
//            </EditText>
//
//            <TextView
//                android:layout_width="125dp"
//                android:layout_height="wrap_content"
//                android:layout_column="1"
//                android:layout_gravity="left"
//                android:layout_row="0"
//                android:inputType="none"
//                android:text="@string/tag_color_lbl" />
//
//            <EditText
//                android:id="@+id/fed_colorText"
//                android:layout_width="100dp"
//                android:layout_height="wrap_content"
//                android:layout_column="1"
//                android:layout_gravity="left"
//                android:layout_row="2"
//                android:inputType="text"
//                android:textSize="18sp"
//                android:typeface="monospace" >
//            </EditText>
//
//            <TextView
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="2"
//                android:layout_gravity="left"
//                android:layout_row="0"
//                android:inputType="none"
//                android:text="@string/tag_location_lbl" />
//
//            <EditText
//                android:id="@+id/fed_locationText"
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="2"
//                android:layout_gravity="left"
//                android:layout_row="2"
//                android:inputType="text"
//                android:textSize="18sp"
//                android:typeface="monospace" >
//            </EditText>
//
//            <TextView
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="0"
//                android:layout_gravity="left"
//                android:layout_row="3"
//                android:inputType="none"
//                android:text="@string/farm_id_lbl" />
//
//            <EditText
//                android:id="@+id/farmText"
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="0"
//                android:layout_gravity="left"
//                android:layout_row="4"
//                android:enabled="true"
//                android:inputType="number"
//                android:textSize="18sp"
//                android:typeface="monospace" />
//
//            <TextView
//                android:layout_width="125dp"
//                android:layout_height="wrap_content"
//                android:layout_column="1"
//                android:layout_gravity="left"
//                android:layout_row="3"
//                android:inputType="none"
//                android:text="@string/tag_color_lbl" />
//
//            <EditText
//                android:id="@+id/farm_colorText"
//                android:layout_width="100dp"
//                android:layout_height="wrap_content"
//                android:layout_column="1"
//                android:layout_gravity="left"
//                android:layout_row="4"
//                android:inputType="text"
//                android:textSize="18sp"
//                android:typeface="monospace" >
//            </EditText>
//
//            <TextView
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="2"
//                android:layout_gravity="left"
//                android:layout_row="3"
//                android:inputType="none"
//                android:text="@string/tag_location_lbl" />
//
//            <EditText
//                android:id="@+id/farm_locationText"
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="2"
//                android:layout_gravity="left"
//                android:layout_row="4"
//                android:inputType="text"
//                android:textSize="18sp"
//                android:typeface="monospace" >
//            </EditText>
//
//            <TextView
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="0"
//                android:layout_gravity="left"
//                android:layout_row="5"
//                android:inputType="none"
//                android:text="@string/paint_id_lbl" />
//
//            <EditText
//                android:id="@+id/paintText"
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="0"
//                android:layout_gravity="left"
//                android:layout_row="6"
//                android:enabled="true"
//                android:inputType="number"
//                android:textSize="18sp"
//                android:typeface="monospace" />
//
//            <TextView
//                android:layout_width="125dp"
//                android:layout_height="wrap_content"
//                android:layout_column="1"
//                android:layout_gravity="left"
//                android:layout_row="5"
//                android:inputType="none"
//                android:text="@string/paint_color_lbl" />
//
//            <EditText
//                android:id="@+id/paint_colorText"
//                android:layout_width="100dp"
//                android:layout_height="wrap_content"
//                android:layout_column="1"
//                android:layout_gravity="left"
//                android:layout_row="6"
//                android:inputType="text"
//                android:textSize="18sp"
//                android:typeface="monospace" >
//            </EditText>
//
//            <TextView
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="2"
//                android:layout_gravity="left"
//                android:layout_row="5"
//                android:inputType="none"
//                android:text="@string/tag_location_lbl" />
//
//            <EditText
//                android:id="@+id/paint_locationText"
//                android:layout_width="90dp"
//                android:layout_height="wrap_content"
//                android:layout_column="2"
//                android:layout_gravity="left"
//                android:layout_row="6"
//                android:inputType="text"
//                android:textSize="18sp"
//                android:typeface="monospace" >
//            </EditText>
//
//        </GridLayout>
//		       <GridLayout
//		        android:id="@+id/grid4"
//		        android:layout_width="wrap_content"
//		        android:layout_height="wrap_content"
//		        android:columnCount="3"
//		        android:rowCount="4" > 
//		              
//		        <TextView
//		            android:layout_width="215dp"
//		            android:layout_height="wrap_content"
//		            android:layout_column="0"
//		            android:layout_gravity="left|top"
//		            android:layout_row="0"
//		            android:inputType="none"
//		            android:text="@string/electronic_id_lbl" />
//		        
//		        <EditText
//		            android:id="@+id/eidText"
//		            android:layout_width="215dp"
//		            android:layout_height="wrap_content"
//		            android:layout_column="0"
//		            android:layout_gravity="left"
//		            android:layout_row="1"
//		            android:enabled="true"
//		            android:inputType="text"
//		            android:textSize="18sp"
//		            android:typeface="monospace" />  
//		        
//		        <TextView
//		            android:layout_width="90dp"
//		            android:layout_height="wrap_content"
//		            android:layout_column="2"
//		            android:layout_gravity="left|top"
//		            android:layout_row="0"
//		            android:inputType="none"
//		            android:text="@string/tag_location_lbl" />
//
//		        <EditText
//		  	  		android:id="@+id/eid_locationText"
//		  	  		android:layout_width="100dp"
//		  	  		android:layout_height="wrap_content"
//		  	  		android:layout_column="2"
//		      		android:layout_gravity="left"
//		      		android:layout_row="1"
//		      		android:inputType="text"
//		  	  		android:textSize="18sp"/>
//		        
//		        <TextView
//		            android:layout_width="100dp"
//		            android:layout_height="wrap_content"
//		            android:layout_column="1"
//		            android:layout_gravity="left|top"
//		            android:layout_row="0"
//		            android:inputType="none"
//		            android:text="@string/tag_color_lbl" />
//		        
//		        <EditText
//		  	  		android:id="@+id/eid_colorText"
//		  	  		android:layout_width="100dp"
//		  	  		android:layout_height="wrap_content"
//		  	  		android:layout_column="1"
//		      		android:layout_gravity="left"
//		      		android:layout_row="1"
//		      		android:inputType="text"
//		  	  		android:textSize="18sp"/>
//		        </GridLayout>

//			
//			<Button
//            android:id="@+id/update_display_btn"
//            android:layout_width="100dp"
//            android:layout_height="50dp"
//            android:layout_column="2"
//      		android:layout_gravity="left"
//     	 	android:layout_row="0"
//            android:onClick="updateDisplay"
//            android:text="@string/update_display_btn"
//            android:textSize="14sp" />	
//			
//		
			
//			
//	 		<GridLayout
//	        android:id="@+id/grid6"
//	        android:layout_width="match_parent"
//	        android:layout_height="wrap_content"
//	        android:columnCount="5">   	    
//	     	    
//	        <TextView
//	            android:layout_width="176dp"
//	            android:layout_height="wrap_content"
//	            android:layout_column="1"
//	            android:layout_gravity="left|top"
//	            android:layout_row="0"
//	            android:inputType="none"
//	            android:textStyle="bold"
//	            android:text="@string/tag_number_lbl" />
//	        
//	        <TextView
//	            android:layout_width="65dp"
//	            android:layout_height="wrap_content"
//	            android:layout_column="2"
//	            android:layout_gravity="left"
//	            android:layout_row="0"
//	            android:inputType="none"
//	            android:textStyle="bold"
//	            android:text="@string/tag_color_abbrev_lbl" />
//	        
//	        <TextView
//	            android:layout_width="40dp"
//	            android:layout_height="wrap_content"
//	            android:layout_column="3"
//	            android:layout_gravity="left"
//	            android:layout_row="0"
//	            android:inputType="none"
//	            android:textStyle="bold"
//	            android:text="@string/tag_location_abbrev_lbl" />
//	        
//	        <TextView
//	            android:layout_width="80dp"
//	            android:layout_height="wrap_content"
//	            android:layout_column="4"
//	            android:layout_gravity="left"
//	            android:layout_row="0"
//	            android:inputType="none"
//	            android:textStyle="bold"
//	            android:text="@string/tag_type_abbrev_lbl" />
//        
//     	</GridLayout>
//    	    
//       <ListView
//           android:id="@+id/android:list2"
//           android:layout_width="fill_parent"
//           android:layout_height="15dp" >
//
//		</ListView>  
	
//	<Button
//    android:id="@+id/remove_fedtag_btn"
//    android:layout_width="100dp"
//    android:layout_height="50dp"
//    android:layout_column="0"
//    android:layout_gravity="left|top"
//    android:layout_row="1"
//    android:onClick="removeFedTag"
//    android:text="@string/remove_fedtag_btn"
//    android:textSize="14sp" />		
		
	
//	
//    <Button
//    android:id="@+id/remove_paint_btn"
//    android:layout_width="100dp"
//    android:layout_height="50dp"
//    android:layout_column="2"
//    android:layout_gravity="left|top"
//    android:layout_row="1"
//    android:onClick="removePaintTag"
//    android:text="@string/remove_paint_btn"
//    android:textSize="14sp" />
//
//<Button
//    android:id="@+id/remove_eidtag_btn"
//    android:layout_width="100dp"
//    android:layout_height="50dp"
//    android:layout_column="3"
//    android:layout_gravity="left|top"
//    android:layout_row="1"
//    android:onClick="removeEIDTag"
//    android:text="@string/remove_eidtag_btn"
//    android:textSize="14sp" />

			
//		}catch (Exception e){
//    		// 	couldn't get a new alert for this sheep
//	    	} 

		// Need to fill the federal and farm tag info from the returned cursor here
	    // looping through all rows and adding to list
//		try {
//			for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2.moveToNext()){
//				// get the tag type of the first record
//				i = dbh.getInt(2);
//				Log.i("in for loop", " tag type is " + String.valueOf(i));
//				switch (i){		
//			    case 1:
//					//Got a federal tag
//			    	Log.i("in for loop", " got fed tag " + dbh.getStr(4));
//			    	TV = (TextView) findViewById(R.id.fedText)	;
//			    	TV.setText(dbh.getStr(4));
//			    	Log.i("in for loop", " fed tag number is " + dbh.getStr(4));
//			    	TV = (TextView) findViewById(R.id.fed_colorText);
//			    	TV.setText(dbh.getStr(3));
//			    	Log.i("in for loop", " fed tag color is " + dbh.getStr(3));
//			    	TV = (TextView) findViewById(R.id.fed_locationText);
//			    	TV.setText(dbh.getStr(5));
//			    	Log.i("in for loop", " fed tag location is " + dbh.getStr(5));
//			    	fedtagid = dbh.getInt(6);
//			    	Log.i("in for loop", " fed tag rec id is " + String.valueOf(fedtagid));
//			        break;
//			    case 2:
//			    	// Got an EID tag
//			    	Log.i("in for loop", " got EID tag " + dbh.getStr(4));
//			    	TV = (TextView) findViewById(R.id.eidText)	;
//			    	TV.setText(dbh.getStr(4));
//			    	//	Need to set the EID Tag color and location here by reading the data
//			    	TV = (TextView) findViewById(R.id.eid_colorText);
//			    	TV.setText(dbh.getStr(3));
//			    	TV = (TextView) findViewById(R.id.eid_locationText);
//			    	TV.setText(dbh.getStr(5));
//			    	eidtagid = dbh.getInt(6);
//			    	Log.i("in for loop", " EID tag id is " + String.valueOf(eidtagid));
//			        break;
//			    case 3:
//					// Got a paint brand
//			    	Log.i("in for loop", " got paint mark " + dbh.getStr(4));
//			    	TV = (TextView) findViewById(R.id.paintText)	;
//			    	TV.setText(dbh.getStr(4));
//			    	TV = (TextView) findViewById(R.id.paint_colorText);
//			    	TV.setText(dbh.getStr(3));
//			    	TV = (TextView) findViewById(R.id.paint_locationText);
//			    	TV.setText(dbh.getStr(5));
//			    	paintid = dbh.getInt(6);
//			    	Log.i("in for loop", " paint id is " + String.valueOf(paintid));
//			        break;
//			    case 4:
//			    	// got a farm tag
//			    	Log.i("in for loop", " got Farm tag " + dbh.getStr(4));	
//		    		TextView TV5 = (TextView) findViewById(R.id.farmText)	;
//		    		TV5.setText(dbh.getStr(4));
//		    		TextView TV6 = (TextView) findViewById(R.id.farm_colorText);
//		    		TV6.setText(dbh.getStr(3));
//		    		TextView TV7 = (TextView) findViewById(R.id.farm_locationText);
//		    		TV7.setText(dbh.getStr(5));
//		    		farmtagid = dbh.getInt(6);
//		    		Log.i("in for loop", " farm tag id is " + String.valueOf(farmtagid));
//			        break;
//			    case 5:
//			    case 6:
//			    case 7:
//			    	//	got a tattoo, split or notch
//			    	// TODO
//			    	//	Need to set a way to show tattoo, split and notches
//			        break;
//				} // end of case switch
//				Log.i("out of case", " switch " );
//			} // end of for loop
//			Log.i("out of for", " loop " );
//		} catch (Exception e) {
//			// No tags so dump out
//		}
			
//	//	Now to test of the sheep has an alert and if so then display the alert & set the alerts button to red
//		if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
//	       	// make the alert button red
//	    	Button btn = (Button) findViewById( R.id.alert_btn );
//	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
//	    	btn.setEnabled(true); 
//	    	showAlert(v);
//		}
//	}
    // user clicked 'remove fed tag' button   
//    public void removeFedTag( View v )
//    	{
//    	if( fedtagid != 0 )
//    		{
//    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
//    		builder.setMessage( R.string.delete_tag )
//    	           .setTitle( R.string.delete_warning );
//    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
//    	           public void onClick(DialogInterface dialog, int idx) {
//    	               // User clicked OK button -- remove the federal tag
//    	        	   // add a tag_date_off of today to the tag   	       		
//    	        	   	String today = Utilities.TodayIs();
//    	        	   	Log.i("removefedtag", String.valueOf(fedtagid));
//    	        	   	String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", fedtagid );
//    	        	   	Log.i("removefedtag", " command is " + cmd);
//    	        	   	dbh.exec( cmd );
////    	    		   	Clear the display of the tags
////    	    		   	TextView TV = (TextView) findViewById(R.id.fedText)	;
////    	           	    TV.setText(null);
////    	           	    TV = (TextView) findViewById(R.id.fed_colorText);
////    	           		TV.setText("");
////    	           		TV = (TextView) findViewById(R.id.fed_locationText);
////    	           		TV.setText("");
//    	           		fedtagid = 0;
//    	               }
//    	       });
//    		builder.setNegativeButton( R.string.cancel_btn, new DialogInterface.OnClickListener() {
//    	           public void onClick(DialogInterface dialog, int idx) {
//    	               // User cancelled the dialog
//    	           }
//    	       });
//    		
//    		AlertDialog dialog = builder.create();
//    		dialog.show();
//    		}
//    	}    
    
    // user clicked 'remove farm tag' button   
    public void removeFarmTag( final View v)
    	{
    	
		// Check to see that something has been selected
		if (selectedItem == android.widget.AdapterView.INVALID_ROW_ID)
		{
			Toast.makeText(this, "No Tag Selected", Toast.LENGTH_SHORT).show();
			return;
		}
		
 	   Log.i("removefarmtag", " tag record to remove is item " + String.valueOf(selectedItem) );
 	  final int tag_to_remove = (int) selectedItem;
//    	if( farmtagid != 0 )
//    		{
 	  
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.delete_tag )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- remove the farm tag
    	        	   //add a tag_date_off of today to the tag
    	        	   String today = Utilities.TodayIs();

//    	       		   tag_to_remove = fetchSingleField(String.format("SELECT id_infoid FROM id_info_table where id_infoid = %s;", selectedItem),"user_task_name");
 //   	        	   Log.i("removefarmtag", today);
    	        	   Log.i("removefarmtag", " farm tag record is " + String.valueOf(tag_to_remove) );
    	        	   
    	       		   String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", tag_to_remove );
    	       		   Log.i("removefarmtag", " command is " + cmd);
    	       		   dbh.exec( cmd );
    	       		   findTagsShowAlert (v, thissheep_id); 
//   	    		   	Clear the display of the tags
//   	    		   	TextView TV = (TextView) findViewById(R.id.farmText)	;
//   	           	    TV.setText(null);
//   	           		TV = (TextView) findViewById(R.id.farm_colorText);
//   	           		TV.setText("");
//   	           		TV = (TextView) findViewById(R.id.farm_locationText);
//   	           		TV.setText("");  
//   	           		farmtagid = 0;
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
//    	}  
    
	// Fetches a single unique field. Will only retrieve the first if there are multiple rows returned.
	// Craft your query carefully.
	public String fetchSingleField(String query, String field)
	{
		Cursor tempCursor;
		Object tempCrsr;
		int numRecs;
		try {
		tempCrsr = dbh.exec(query);
		tempCursor = (Cursor) tempCrsr;
		numRecs = tempCursor.getCount();
		tempCursor.moveToFirst();
		} catch (Exception e)
		{
			Toast.makeText(this, "SQL error", Toast.LENGTH_SHORT).show();
			return null;
		}

		if (numRecs > 0)
		{
			String fieldValue = dbh.getStr(field);
			tempCursor.close();
			return fieldValue;
		}
		else
		{
			tempCursor.close();
			// Return empty string instead of null for crash safety.
			return "";
		}
	}
	
	
    // user clicked 'remove Paint tag' button   
//    public void removePaintTag( View v )
//    	{
//    	if( paintid != 0 )
//    		{
//    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
//    		builder.setMessage( R.string.delete_tag )
//    	           .setTitle( R.string.delete_warning );
//    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
//    	           public void onClick(DialogInterface dialog, int idx) {
//    	               // User clicked OK button -- remove the paint brand
//    	        	   //add a tag_date_off of today to the tag
//    	        	   String today = Utilities.TodayIs();
//     	        	   Log.i("removepaint", " paint mark record is " + String.valueOf(paintid) );
//    	       		   String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", paintid );
//    	       		   Log.i("removepaint", " command is " + cmd);
//    	       			dbh.exec( cmd );
//    	    		  
////   	    		   	Clear the display of the tags
////   	    		   	TextView TV = (TextView) findViewById(R.id.paintText)	;
////   	           	    TV.setText(null);
////   	           		TV = (TextView) findViewById(R.id.paint_colorText);
////   	           		TV.setText("");
////   	           		TV = (TextView) findViewById(R.id.paint_locationText);
////   	           		TV.setText("");  
//   	           		paintid = 0;
//   	           		}
//    	       });
//    		builder.setNegativeButton( R.string.cancel_btn, new DialogInterface.OnClickListener() {
//    	           public void onClick(DialogInterface dialog, int idx) {
//    	               // User cancelled the dialog
//    	           }
//    	       });
//    		
//    		AlertDialog dialog = builder.create();
//    		dialog.show();
//    		}
//    	}
//   
    // user clicked 'remove eid tag' button   
//    public void removeEIDTag( View v )
//    	{
//    	if( eidtagid != 0 )
//    		{
//    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
//    		builder.setMessage( R.string.delete_tag )
//    	           .setTitle( R.string.delete_warning );
//    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
//    	           public void onClick(DialogInterface dialog, int idx) {
//    	               // User clicked OK button -- remove the eid tag
//    	        	   // add a tag_date_off of today to the tag   	       		
//    	        	   	String today = Utilities.TodayIs();
//    	        	   	Log.i("removeeidtag", String.valueOf(eidtagid));
//    	        	   	String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", eidtagid );
//    	        	   	Log.i("removeeidtag", " command is " + cmd);
//    	        	   	dbh.exec( cmd );
////    	    		   	Clear the display of the tags
////    	    		   	TextView TV = (TextView) findViewById(R.id.eidText)	;
////    	           	    TV.setText(null);   	           	    
////    	           	    TV = (TextView) findViewById(R.id.eid_colorText);
////    	           		TV.setText("");
////    	           		TV = (TextView) findViewById(R.id.eid_locationText);
////    	           		TV.setText("");
//    	           		eidtagid = 0;
//    	               }
//    	       });
//    		builder.setNegativeButton( R.string.cancel_btn, new DialogInterface.OnClickListener() {
//    	           public void onClick(DialogInterface dialog, int idx) {
//    	               // User cancelled the dialog
//    	           }
//    	       });
//    		
//    		AlertDialog dialog = builder.create();
//    		dialog.show();
//    		}
//    	}    
//    
    public void doNote( View v )
    {	 
    	Utilities.takeNote(v, thissheep_id, this);
    }
//     user clicked 'Scan' button    
    public void scanEid( View v){
   
   	 	tag_type_spinner.setSelection(2);
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
//    	Object crsr;
    	String sheepnameText;
//    	String fedText, fed_colorText, fed_locationText;
//    	String farmText, farm_colorText, farm_locationText, eidText, eid_colorText, eid_locationText;
//    	String paintText, paint_colorText, paint_locationText ;
//    	int		fed_colorid, farm_colorid, eid_colorid, fed_locationid, farm_locationid, eid_locationid;
//    	int 	paint_colorid, paint_locationid ;
    	int	 	flock_id; // pointer into the flock id table so for the federal tag flock ID record
    	int		official_id_type;
    	//	Disable the update database button so we don't get 2 records entered
       	btn = (Button) findViewById( R.id.update_database_btn );
    	btn.setEnabled(false); 
    	
//    	official_id_type = 1;
//    	eid_colorid = 0;
//    	eid_locationid = 0;
//    	eidText = null;
//    	
    	// Get the values from the UI screen
    	TextView TV = (TextView) findViewById( R.id.sheepnameText );
    	sheepnameText = TV.getText().toString();
    	Log.i("update everything ", "sheep name " + sheepnameText);   	
    	Log.i("update everything ", "sheep_id is " + String.valueOf(thissheep_id));   	
//    	Log.i("update everything ", "fed info record " + fedtagid);  
//    	TV  = (TextView) findViewById( R.id.fedText );
//	    fedText = TV.getText().toString();
//	    Log.i("update everything ", "fed tag " + fedText);	    
//	    Log.i("update everything ", "farm info record " + farmtagid);
//	    TV  = (TextView) findViewById( R.id.farmText );
//	    farmText = TV.getText().toString();
//	    Log.i("update everything ", "farm tag " + farmText);	    	    
//	    Log.i("update everything ", "eid info record " + eidtagid);
//	    TV  = (TextView) findViewById( R.id.eidText );
//	    eidText = TV.getText().toString();	
//	    Log.i("update everything ", "EID Tag " + eidText);
	    
//	    TV  = (TextView) findViewById( R.id.paintText );
//	    paintText = TV.getText().toString();	
    		String today = Utilities.TodayIs();	
        	// Get the data from the add tag section of the screen
        	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
        	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
        	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
        	
        	tag_type_label = tag_type_spinner2.getSelectedItem().toString();
        	Log.i("updateTag", "Tag type label is " + tag_type_label);
        	tag_color_label = tag_color_spinner.getSelectedItem().toString();
        	Log.i("updateTag", "Tag color is " + tag_color_label);
        	tag_location_label = tag_location_spinner.getSelectedItem().toString();
        	Log.i("updateTag", "Tag location is " + tag_location_label);
        	
        	TV  = (TextView) findViewById( R.id.new_tag_number);
        	new_tag_number = TV.getText().toString();
        	 
        	if (tag_type_label.contains("Split")){
        		new_tag_number = "Split";
        		tag_color_label = "Not Applicable";
        		tag_color_spinner.setSelection(19);
        	}
        	Log.i("before if", " new tag number " + new_tag_number); 
        	Log.i("before if", "Tag color is " + tag_color_label);
        	Log.i("before if", "Tag location is " + tag_location_label);

         	if (tag_type_label == "Select a Type" || tag_location_label == "Select a Location" || tag_color_label == "Select a Color"
        			|| new_tag_number.isEmpty()) {
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
        		crsr2 = dbh.exec( cmd );
        		cursor2   = ( Cursor ) crsr2;
        		dbh.moveToFirstRecord();
        		new_tag_type = dbh.getInt(0);
        		Log.i("New tag type ", String.valueOf(new_tag_type));
        		
           		cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
           				"where tag_color_name='%s'", tag_color_label);
           	    crsr2 = dbh.exec( cmd );
        		cursor2   = ( Cursor ) crsr2;
        		dbh.moveToFirstRecord();
        		new_tag_color = dbh.getInt(0);
        		Log.i("New tag color ", String.valueOf(new_tag_color));
        		
        		cmd = String.format("select id_location_table.id_locationid, id_location_table.id_location_abbrev from id_location_table " +
    			"where id_location_abbrev='%s'", tag_location_label);
        		crsr2 = dbh.exec( cmd );
        		cursor2   = ( Cursor ) crsr2;
        		dbh.moveToFirstRecord();
        		new_tag_location = dbh.getInt(0);
        		Log.i("New Location ID ", String.valueOf(new_tag_location));
         		tag_location_label = dbh.getStr(1);
        		Log.i("New Location ", tag_location_label);
	     	    		
        		switch (new_tag_type) {   		
        		case 1:
        			//	have a federal tag to add
	        		flock_id = 1;
	    	        Log.i("updatefed ", "location integer " + String.valueOf(new_tag_location));
	    			Log.i("updatefed ", "sheep_id is " + String.valueOf(thissheep_id));
	    			Log.i("updatefed ", "fed color integer " + String.valueOf(new_tag_color));
	    			Log.i("updatefed ", "today " + today);
	    			Log.i("updatefed ", "flock ID " + String.valueOf(flock_id));
	    			//	 Set the official_id flag to indicate this is the official ID
	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number, id_flockid, official_id) " +
	    					"values ( %s, 1, %s, %s, %s, '%s', %s, %s, 1 )", thissheep_id, new_tag_color, new_tag_color, new_tag_location, today, new_tag_number, flock_id);
	    			Log.i("updatefed ", "before cmd " + cmd);
	    			dbh.exec( cmd );	
	    			Log.i("updatefed ", "after cmd exec");
	        		break;
        		case 2:
        			// 	have an electronic tag to add
        			
        		    int official_id_flag;
		    		Log.i("updateeid ", "beginning of update eid code");
		    		//	assume ID is not an official one at this time so 
		    		official_id_flag = 0;
		    		//	get the first 3 digits of the EID tag number
		    		String str = new_tag_number;
		    		Log.i("updateeid ", "string of eid " + str);
		    		str = str.substring(0,3);
		    		Boolean isOfficial = "840".equals(str);
		    		Log.i("updateeid ", "substring of eid " + str);
		    		if (isOfficial){
		    			official_id_type = 2;
		    			official_id_flag = 1;
		    	        Log.i("updateEID ", "location integer " + String.valueOf(new_tag_location));
		    			Log.i("updateEID ", "sheep_id is " + String.valueOf(thissheep_id));
		    			Log.i("updateEID ", "fed color integer " + String.valueOf(new_tag_color));
		    			Log.i("updateEID ", "today " + today);
		    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number, id_flockid, official_id) " +
		    					"values ( %s, 2, %s, %s, %s, '%s', %s, %s,null,null)", thissheep_id, new_tag_color, new_tag_color, new_tag_location, today, new_tag_number);
		    			Log.i("updateEID ", "before cmd " + cmd);
		    			dbh.exec( cmd );	
		    			Log.i("updateEID ", "after cmd exec");
		    		}else{
		    	        Log.i("updateEID ", "location integer " + String.valueOf(new_tag_location));
		    			Log.i("updateEID ", "sheep_id is " + String.valueOf(thissheep_id));
		    			Log.i("updateEID ", "fed color integer " + String.valueOf(new_tag_color));
		    			Log.i("updateEID ", "today " + today);
		    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number, id_flockid, official_id) " +
		    					"values ( %s, 2, %s, %s, %s, '%s', %s, %s, 1, %s)", thissheep_id, new_tag_color, new_tag_color, new_tag_location, today, new_tag_number,official_id_flag);
		    			Log.i("updateEID ", "before cmd " + cmd);
		    			dbh.exec( cmd );	
		    			Log.i("updateEID ", "after cmd exec");
		    		}
	        		break;
        		case 3:
        			//	have a paint tag to add
	    	        Log.i("updatePaint ", "location integer " + String.valueOf(new_tag_location));
	    			Log.i("updatePaint ", "sheep_id is " + String.valueOf(thissheep_id));
	    			Log.i("updatePaint ", "fed color integer " + String.valueOf(new_tag_color));
	    			Log.i("updatePaint ", "today " + today);
	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number, id_flockid, official_id) " +
	    					"values ( %s, 3, %s, %s, %s, '%s', %s, %s,null,null)", thissheep_id, new_tag_color, new_tag_color, new_tag_location, today, new_tag_number);
	    			Log.i("updatePaint ", "before cmd " + cmd);
	    			dbh.exec( cmd );	
	    			Log.i("updatePaint ", "after cmd exec");
	        		break;
        		case 4:
           			// 	have a farm tag to add
	    	        Log.i("updateFarm ", "location integer " + String.valueOf(new_tag_location));
	    			Log.i("updateFarm ", "sheep_id is " + String.valueOf(thissheep_id));
	    			Log.i("updateFarm ", "fed color integer " + String.valueOf(new_tag_color));
	    			Log.i("updateFarm ", "today " + today);
	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
	    					"values ( %s, 4, %s, %s, %s, '%s', %s)", thissheep_id, new_tag_color, new_tag_color, new_tag_location, today, new_tag_number);
	    			Log.i("updateFarm ", "before cmd " + cmd);
	    			dbh.exec( cmd );	
	    			Log.i("updateFarm ", "after cmd exec");
	        		break;
        		case 5:
        		case 6:
        		case 7:
        			//	note dealing with tattoo's splits or notches yet
        			break;
        		}        			
        		tag_type_spinner2.setSelection(0);	
        		tag_color_spinner.setSelection(0);
         		tag_location_spinner.setSelection(0);
         		TV  = (TextView) findViewById( R.id.new_tag_number);
         		TV.setText( "" );
         		findTagsShowAlert (v, thissheep_id); 
        	}	

        		//	old code no longer used
//	    if (fedtagid != 0) {
//	    	// 	update the Federal tag data if it has changed?
//	    	//	not implemented at this time. Assumed we either are adding new tags or taking off tags first
//	    	//	no update of an existing tag record is done in this module.
//	    	Log.i("updatefed", " tag record id is not zero");
//	    }
//	    	else {
//	    		// fedtagid is zero so need to test whether there is a federal tag and add a record if there is one
////	    		if (fedText != null && !fedText.isEmpty()){
//	    			//have a federal tag but no fedtagid so add a new record;
////	    		    TV = (TextView) findViewById( R.id.fed_colorText );
//	    		    fed_colorText = TV.getText().toString();
//	    		    Log.i("update everything ", "fed color " + fed_colorText);	    
//	    		    cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
//	    	    			"where tag_color_name='%s'", fed_colorText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        fed_colorid = dbh.getInt(0);
//	    	        Log.i("update everything ", "fed color integer " + String.valueOf(fed_colorid));
//	    	        
////	    		    TV = (TextView) findViewById( R.id.fed_locationText );
//	    		    fed_locationText = TV.getText().toString();
//	    		    Log.i("update everything ", "fed location " + fed_locationText);
//	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
//	    	    			"where id_location_abbrev='%s'", fed_locationText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        fed_locationid = dbh.getInt(0);
//	    	        // Set the flock ID to be the Desert Weyr Flock
//	    	        // Will have to change to handle the general case. 
//	    	        // In our case we assume all  federal tags being applied are with our 
//	    	        //	CODL01 flock ID
//	    	        // TODO 
//	    	        //	This should be a user setting from default preferences that we use instead. 
//	    	        flock_id = 1;
//	    	        
//	    	        Log.i("updatefed ", "fed location integer " + String.valueOf(fed_locationid));
//	    			Log.i("updatefed ", "tag record id is 0 but have fed tag data will add a new record to id_info_table here");
//	    			Log.i("updatefed ", "sheep_id is " + String.valueOf(thissheep_id));
//	    			Log.i("updatefed ", "fed color integer " + String.valueOf(fed_colorid));
//	    			Log.i("updatefed ", "fed location integer " + String.valueOf(fed_locationid));
//	    			Log.i("updatefed ", "today " + today);
//	    			Log.i("updatefed ", "flock ID " + String.valueOf(flock_id));
//	    			//	 Set the official_id flag to indicate this is the official ID
//	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number, id_flockid, official_id) " +
//	    					"values ( %s, 1, %s, %s, %s, '%s', %s, %s, 1 )", thissheep_id, fed_colorid, fed_colorid, fed_locationid, today, new_tag_number, flock_id);
//	    			Log.i("updatefed ", "before cmd " + cmd);
//	    			dbh.exec( cmd );	
//	    			Log.i("updatefed ", "after cmd exec");
//	    		}
////	    		else{
////	    			// no federal tag to enter so return
////	    			Log.i("updatefed ", "no federal tag so nothing to do");	
////	    		}
////	    	}
////	    Update the split mark data
////	    if (splitid != 0) {
////	    	// update the split ear data
////	    	//	not implemented at this time. Assumed we either are adding new tags or taking off tags first
////	    	//	no update of an existing tag record is done.
////	    	Log.i("updatesplit ", " tag record id is not zero");
////		    }
////	    	else {
////	    		// splitid is zero so need to test whether there is a location and add a record if there is one
////	    		if (paintText != null && !paintText.isEmpty()){	    			
////	    		    paint_number = Integer.valueOf(paintText);
////	    		    TV = (TextView) findViewById( R.id.paint_locationText);
////	    		    paint_locationText = TV.getText().toString();
////	    		    Log.i("updatesplit ", "paint location " + paint_locationText);
////	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
////	    	    			"where id_location_abbrev='%s'", paint_locationText);
////	    	    	crsr = dbh.exec( cmd );
////	    	        cursor   = ( Cursor ) crsr;
////	    	        dbh.moveToFirstRecord();
////	    	        paint_colorid = 19; // no color for a split
////	    	        paint_locationid = dbh.getInt(0);
////	    	        Log.i("updatesplit ", "split location integer " + String.valueOf(paint_locationid));
////	    			//have a paint tag but no paintid so add a new record;
////	    	        paintText = ""; // make the tag number empty
////	    			Log.i("updatesplit ", "tag record id is 0 but have paint tag data need to add a new record to id_info_table here");
////	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
////	    					"values ( %s, 6, %s, %s, %s, '%s', %s )", thissheep_id, paint_colorid, paint_colorid, paint_locationid, today, paintText);
////	    			dbh.exec( cmd );	
////	    		}
////	    		else{
////	    			// no split to enter so return
////	    			Log.i("updatesplit ", "no split so nothing to do");
////	    			
////	    		}
////	    	}
//	    
//	    // Update the Farm Tag data
//	    if (farmtagid != 0) {
//	    	// update the Farm tag data
//	    	//	not implemented at this time. Assumed we either are adding new tags or taking off tags first
//	    	//	no update of an existing tag record is done.
//	    	Log.i("updatefarm ", " tag record id is not zero");
//		    }
//	    	else {
//	    		// farmtagid is zero so need to test whether there is a farm tag and add a record if there is one
//	    		if (new_tag_number != null && !new_tag_number.isEmpty()){
////	    		    TV = (TextView) findViewById( R.id.farm_locationText);
//	    		    farm_locationText = TV.getText().toString();
//	    		    Log.i("updatefarm ", "farm location " + farm_locationText);
//	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
//	    	    			"where id_location_abbrev='%s'", farm_locationText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        farm_locationid = dbh.getInt(0);
//	    	        Log.i("updatefarm ", "farm location integer " + String.valueOf(farm_locationid));
////	    		    TV = (TextView) findViewById( R.id.farm_colorText );
//	    		    farm_colorText = TV.getText().toString();
//	    		    Log.i("updatefarm ", "farm color " + farm_colorText);
//	    		    cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
//	    	    			"where tag_color_name='%s'", farm_colorText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        farm_colorid = dbh.getInt(0);
//	    	        Log.i("updatefarm ", "farm color integer " + String.valueOf(farm_colorid));
//	    			//have a farm tag but no farmtagid so add a new record;
//	    			Log.i("updatefarm ", "tag record id is 0 but have farm tag data need to add a new record to id_info_table here");
//	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
//	    					"values ( %s, 4, %s, %s, %s, '%s', '%s' )", thissheep_id, farm_colorid, farm_colorid, farm_locationid, today, new_tag_number);
//	    			Log.i("updatefarm ", cmd);	
//	    			dbh.exec( cmd );	
//	    			Log.i("updatefarm ", "after cmd exec");
//	    		}
//	    		else{
//	    			// no farm tag to enter so return
//	    			Log.i("updatefarm ", "no farm tag so nothing to do");
//	    		}
//	    	}
//	    
////	    Update the Paint mark data
//	    if (paintid != 0) {
//	    	// update the Paint tag data
//	    	//	not implemented at this time. Assumed we either are adding new tags or taking off tags first
//	    	//	no update of an existing tag record is done.
//	    	Log.i("updatepaint ", " tag record id is not zero");
//		    }
//	    	else {
//	    		// paintid is zero so need to test whether there is a paint tag and add a record if there is one
//	    		if (new_tag_number != null && !new_tag_number.isEmpty()){
//	    			
////	    		    paint_number = Integer.valueOf(paintText);
////	    		    TV = (TextView) findViewById( R.id.paint_locationText);
//	    		    paint_locationText = TV.getText().toString();
//	    		    Log.i("updatepaint ", "paint location " + paint_locationText);
//	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
//	    	    			"where id_location_abbrev='%s'", paint_locationText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        paint_locationid = dbh.getInt(0);
//	    	        Log.i("updatepaint ", "paint color integer " + String.valueOf(paint_locationid));
////	    		    TV = (TextView) findViewById( R.id.paint_colorText );
//	    		    paint_colorText = TV.getText().toString();
//	    		    Log.i("updatepaint ", "paint color " + paint_colorText);
//	    		    cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
//	    	    			"where tag_color_name='%s'", paint_colorText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        paint_colorid = dbh.getInt(0);
//	    	        Log.i("updatepaint ", "paint location integer " + String.valueOf(paint_locationid));
//	    			//have a paint tag but no paintid so add a new record;
//	    			Log.i("updatepaint ", "tag record id is 0 but have paint tag data need to add a new record to id_info_table here");
//	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
//	    					"values ( %s, 3, %s, %s, %s, '%s', %s )", thissheep_id, paint_colorid, paint_colorid, paint_locationid, today, new_tag_number);
//	    			dbh.exec( cmd );	
//	    		}
//	    		else{
//	    			// no paint tag to enter so return
//	    			Log.i("updatepaint ", "no paint tag so nothing to do");
//	    			
//	    		}
//	    	}
////	    Update the tattoo mark data
//	    if (tattooid != 0) {
//	    	// update the tattoo tag data
//	    	//	not implemented at this time. Assumed we either are adding new tags or taking off tags first
//	    	//	no update of an existing tag record is done.
//	    	Log.i("updatetattoot ", " tag record id is not zero");
//		    }
//	    	else {
//	    		// tattooid is zero so need to test whether there is a paint tag and add a record if there is one
//	    		if (new_tag_number != null && !new_tag_number.isEmpty()){
//	    			
////	    		    paint_number = Integer.valueOf(paintText);
////	    		    TV = (TextView) findViewById( R.id.paint_locationText);
//	    		    paint_locationText = TV.getText().toString();
//	    		    Log.i("updatetattoo ", "paint location " + paint_locationText);
//	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
//	    	    			"where id_location_abbrev='%s'", paint_locationText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        paint_locationid = dbh.getInt(0);
//	    	        Log.i("updatetattoo ", "tattoo color integer " + String.valueOf(paint_locationid));
////	    		    TV = (TextView) findViewById( R.id.paint_colorText );
//	    		    paint_colorText = TV.getText().toString();
//	    		    Log.i("updatetattoo ", "paint color " + paint_colorText);
//	    		    cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
//	    	    			"where tag_color_name='%s'", paint_colorText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        paint_colorid = dbh.getInt(0);
//	    	        Log.i("updatetattoo ", "paint location integer " + String.valueOf(paint_locationid));
//	    			//have a paint tag but no paintid so add a new record;
//	    			Log.i("updatetattoo ", "tag record id is 0 but have paint tag data need to add a new record to id_info_table here");
//	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number) " +
//	    					"values ( %s, 5, %s, %s, %s, '%s', %s )", thissheep_id, paint_colorid, paint_colorid, paint_locationid, today, new_tag_number);
//	    			dbh.exec( cmd );	
//	    		}
//	    		else{
//	    			// no tattoo to enter so return
//	    			Log.i("updatetattoo ", "no tattoo so nothing to do");
//	    			
//	    		}
//	    	}
//
//	    //Update the EID Tag data
//	    int official_id_flag;
//	    if (eidtagid != 0) {
//	    	// update the EID tag data
//	    	Log.i("updateEID ", "tag record id is not zero, needs update here");
//		    }
//	    	else {
//	    		Log.i("updateeid ", "beginning of update eid code");
//	    		//	assume ID is not an official one at this time so 
//	    		official_id_flag = 0;
//	    		//	get the first 3 digits of the EID tag number
//	    		String str = eidText;
//	    		Log.i("updateeid ", "string of eid " + str);
//	    		str = str.substring(0,3);
//	    		Boolean isOfficial = "840".equals(str);
//	    		Log.i("updateeid ", "substring of eid " + str);
//	    		if (isOfficial){
//	    			official_id_type = 2;
//	    			official_id_flag = 1;
//	    		}
//	    		// eidtagid is zero so need to test whether there is an EID tag and add a record if there is one
//	    			if (eidText != null && !eidText.isEmpty()){
//	    		
////	    			TV = (TextView) findViewById( R.id.eid_locationText);
//	    			eid_locationText = TV.getText().toString();
//	    		    Log.i("updateeid ", "eid location " + eid_locationText);
//	    		    cmd = String.format("select id_location_table.id_locationid from id_location_table " +
//	    	    			"where id_location_abbrev='%s'", eid_locationText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        eid_locationid = dbh.getInt(0);
//	    	        Log.i("updateeid ", "eid color integer " + String.valueOf(eid_locationid));
////	    		    TV = (TextView) findViewById( R.id.eid_colorText );
//	    		    eid_colorText = TV.getText().toString();
//	    		    Log.i("updateeid ", "eid color " + eid_colorText);
//	    		    cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
//	    	    			"where tag_color_name='%s'", eid_colorText);
//	    	    	crsr = dbh.exec( cmd );
//	    	        cursor   = ( Cursor ) crsr;
//	    	        dbh.moveToFirstRecord();
//	    	        eid_colorid = dbh.getInt(0);
//	    	        Log.i("updateeid ", "eid location integer " + String.valueOf(eid_locationid));
//	    			//have an EID tag but no eidtagid so add a new record;
//	    			Log.i("updateEID ", "tag record id is 0 need to add a new record to id_info_table here");
//	    			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male, tag_color_female, tag_location, tag_date_on, tag_number, official_id) " +
//	    					"values ( %s, 2, %s, %s, %s, '%s', '%s', %s )", thissheep_id, eid_colorid, eid_colorid, eid_locationid, today, eidText, official_id_flag);
//	    			dbh.exec( cmd );	
//	    		}
//	    		else{
//	    			// no EID tag to enter so return
//	    			Log.i("updateEID ", "no eid tag so nothing to do");
//	    		}
//	    	}
//	    clearBtn( v );
//		Enable the update database button so we can continue to update
//       	btn = (Button) findViewById( R.id.update_database_btn );
//    	btn.setEnabled(true); 
    }
    public void addNewTag( View v ){
//    	Object crsr;
    	Log.i ("in add tag", " start of addNewTag code");
//       	btn = (Button) findViewById( R.id.update_display_btn );
//    	btn.setEnabled(true); 
    	//	Enable the scanner so we can add EID tags
    	scanEid (v); 
    	
    	//make the new tag number empty at first
    	new_tag_number = null;
    	
       	// Fill the Tag Type Spinner    	    	    	
    	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);

    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_type_spinner2.setAdapter (dataAdapter);
		tag_type_spinner2.setSelection(0);	
		Log.i ("in add tag", " after set tag_type_spinner");
		
//    	// Fill the Tag Color Spinner
    	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_colors);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_color_spinner.setAdapter (dataAdapter);
		tag_color_spinner.setSelection(0);
		Log.i ("in add tag", " after set tag_color_spinner");	
		
    	// Fill the Tag Location Spinner
		tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_locations);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_location_spinner.setAdapter (dataAdapter);
		//	Default to  right ear
		tag_location_spinner.setSelection(0);
		Log.i ("in add tag", " after set tag_location_spinner");
	}
    
    public void showAlert (View v)
    {
    		String	alert_text;
            String	cmd; 
            Cursor 	cursor3;
            Object 	crsr3;        
    		// Display alerts here   	
    				AlertDialog.Builder builder = new AlertDialog.Builder( this );
    				cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_id =%d", thissheep_id);
    				Log.i("get alert ", cmd);  
    				crsr3 = dbh.exec( cmd );
    		        cursor3   = ( Cursor ) crsr3;
    		        dbh.moveToFirstRecord();		       
    		        alert_text = (dbh.getStr(0));
    		        Log.i("get alert ", alert_text); 
    				builder.setMessage( alert_text )
    			           .setTitle( R.string.alert_warning );
    				builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int idx) {
    			               // User clicked OK button   	  
    			               }
    			       });		
    				AlertDialog dialog = builder.create();
    				dialog.show();
    	}
   // TODO 
    public void updateDisplay( View v ){
    	String 			cmd;
    	TextView 		TV;
//    	btn = (Button) findViewById( R.id.update_display_btn );
//    	btn.setEnabled(false); 
    	// Get the data from the add tag section of the screen
    	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
    	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
    	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
    	
    	tag_type_label = tag_type_spinner2.getSelectedItem().toString();
    	Log.i("updateTag", "Tag type label is " + tag_type_label);
    	tag_color_label = tag_color_spinner.getSelectedItem().toString();
    	Log.i("updateTag", "Tag color is " + tag_color_label);
    	tag_location_label = tag_location_spinner.getSelectedItem().toString();
    	Log.i("updateTag", "Tag location is " + tag_location_label);
    	
    	TV  = (TextView) findViewById( R.id.new_tag_number);
    	new_tag_number = TV.getText().toString();
    	 
    	if (tag_type_label.contains("Split")){
    		new_tag_number = "Split";
    		tag_color_label = "Not Applicable";
    		tag_color_spinner.setSelection(19);
    	}
    	Log.i("before if", " new tag number " + new_tag_number); 
    	Log.i("before if", "Tag color is " + tag_color_label);
    	Log.i("before if", "Tag location is " + tag_location_label);

     	if (tag_type_label == "Select a Type" || tag_location_label == "Select a Location" || tag_color_label == "Select a Color"
    			|| new_tag_number.isEmpty()) {
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
    		crsr2 = dbh.exec( cmd );
    		cursor2   = ( Cursor ) crsr2;
    		dbh.moveToFirstRecord();
    		new_tag_type = dbh.getInt(0);
    		Log.i("New tag type ", String.valueOf(new_tag_type));
    		
       		cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
       				"where tag_color_name='%s'", tag_color_label);
       	    crsr2 = dbh.exec( cmd );
    		cursor2   = ( Cursor ) crsr2;
    		dbh.moveToFirstRecord();
    		new_tag_color = dbh.getInt(0);
    		Log.i("New tag color ", String.valueOf(new_tag_color));
    		
    		cmd = String.format("select id_location_table.id_locationid, id_location_table.id_location_abbrev from id_location_table " +
			"where id_location_abbrev='%s'", tag_location_label);
    		crsr2 = dbh.exec( cmd );
    		cursor2   = ( Cursor ) crsr2;
    		dbh.moveToFirstRecord();
    		new_tag_location = dbh.getInt(0);
    		Log.i("New Location ID ", String.valueOf(new_tag_location));
     		tag_location_label = dbh.getStr(1);
    		Log.i("New Location ", tag_location_label);
    		
    	   	// 	Fill the new tag data with where it is in the screen display
        	//	Integers to hold the info new_tag_type, new_tag_color, new_tag_location    		
    		// Need to fill in the lists with the new tag info and call the list adapter
    		// TODO
    		// public  List <String> new_tag_numbers, new_tag_colors, new_tag_locations, new_tag_types;
    		
//    		new_tag_numbers.add(new_tag_number);
//    		new_tag_colors.add(tag_color_label);
//    		new_tag_locations.add(tag_location_label);
//    		new_tag_types.add(tag_type_label);
//    		
//    		ListView newtags = (ListView) findViewById(R.id.list2);
//      	    	
//    		myadapter2 =  new SimpleListAdapter(this, R.layout.list_entry, newtags);
//    		setListAdapter(myadapter2);
    		
    	    
//    		switch (new_tag_type) {   		
//    		case 1:
//    			if (fedtagid != 0){
//    				//	Already have a federal tag must delete one first
//    	    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
//    	    		builder.setMessage( R.string.id_management_remove_tag )
//    	    	           .setTitle( R.string.id_management_remove_tag_header );
//    	    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
//    	    	           public void onClick(DialogInterface dialog, int idx) {
//    	    	               // User clicked OK button 
//    	     	    		   return;
//    	    	               }
//    	    	       });		
//    	    		AlertDialog dialog = builder.create();
//    	    		dialog.show();		  				
//    			}else{
//	        		//	Federal Tag so update federal section and set needs database update
//	        		// 	by setting id of 0 meaning either no tag or needs update
//	        		Log.i("in if", "Got a new federal tag type");
////	        	    TV  = (TextView) findViewById( R.id.fedText );
////	        	    TV.setText(new_tag_number);
////	        	    TV = (TextView) findViewById( R.id.fed_colorText );
////	        	    TV.setText(tag_color_label);
////	        	    TV = (TextView) findViewById( R.id.fed_locationText );
////	        	    TV.setText(tag_location_label);
//	        	    fedtagid = 0;
//    			}
//    			break;
//    		case 2:
//    			if (eidtagid != 0){
//    				//	Already have an EID tag must delete one first
//    	    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
//    	    		builder.setMessage( R.string.id_management_remove_tag )
//    	    	           .setTitle( R.string.id_management_remove_tag_header );
//    	    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
//    	    	           public void onClick(DialogInterface dialog, int idx) {
//    	    	               // User clicked OK button 
//    	     	    		   return;
//    	    	               }
//    	    	       });		
//    	    		AlertDialog dialog = builder.create();
//    	    		dialog.show();		  				
//    			}else{
//            		//	EID Tag so update eid section and set needs database update
//            		//	by setting id of 0 meaning either no tag or needs update       		
//            		Log.i("in if", "Got a new eid tag type");
////            	    TV  = (TextView) findViewById( R.id.eidText );
////            	    TV.setText(new_tag_number);
////            	    TV = (TextView) findViewById( R.id.eid_colorText );
////            	    TV.setText(tag_color_label);
////            	    TV = (TextView) findViewById( R.id.eid_locationText );
////            	    TV.setText(tag_location_label);
//            	    eidtagid = 0;
//    			}
//    			break;
//    		case 3:
//	    		//	Paint mark so update paint section and set needs database update
//	    		// 	by setting id of 0 meaning either no tag or needs update
//	    		Log.i("in if", "Got a new paint mark type");
////	    	    TV  = (TextView) findViewById( R.id.paintText );
////	    	    TV.setText(new_tag_number);
////	    	    TV = (TextView) findViewById( R.id.paint_colorText );
////	    	    TV.setText(tag_color_label);
////	    	    TV = (TextView) findViewById( R.id.paint_locationText );
////	    	    TV.setText(tag_location_label);
//	    	    paintid = 0;
//    			break;
//    		case 4:
//        		//	Farm Tag so update farm section and set needs database update
//        		//	by setting id of 0 meaning either no tag or needs update       		
//        		Log.i("in if", "Got a new farm tag type");
//        	    TV  = (TextView) findViewById( R.id.farmText );
////        	    TV.setText(new_tag_number);
////        	    TV = (TextView) findViewById( R.id.farm_colorText );
////        	    TV.setText(tag_color_label);
////        	    TV = (TextView) findViewById( R.id.farm_locationText );
////        	    TV.setText(tag_location_label);
//        	    farmtagid = 0;
//    			break;
////    		case 5:
//////    			Tattoo so update farm section and set needs database update
////        		//	by setting id of 0 meaning either no tag or needs update       		
////        		Log.i("in if", "Got a new tattoo tag type");
////        	    TV  = (TextView) findViewById( R.id.farmText );
////        	    TV.setText(new_tag_number);
////        	    TV = (TextView) findViewById( R.id.farm_colorText );
////        	    TV.setText(tag_color_label);
////        	    TV = (TextView) findViewById( R.id.farm_locationText );
////        	    TV.setText(tag_location_label);
////        	   	tattooid = 0;
////    			break;
////    		case 6:
////        		//	Split so update farm section and set needs database update
////        		//	by setting id of 0 meaning either no tag or needs update       		
////        		Log.i("in if", "Got a new split ear type");
////        	    TV  = (TextView) findViewById( R.id.farmText );
////        	    TV.setText("Split");
////        	    TV = (TextView) findViewById( R.id.farm_locationText );
////        	    TV.setText(tag_location_label);
////        	    splitid = 0;
////    			break;
////    		case 6:
////        		//	Notch so update farm section and set needs database update
////        		//	by setting id of 0 meaning either no tag or needs update       		
////        		Log.i("in if", "Got a new farm tag type");
////        	    TV  = (TextView) findViewById( R.id.farmText );
////        	    TV.setText(new_tag_number);
////        	    TV = (TextView) findViewById( R.id.farm_locationText );
////        	    TV.setText(tag_location_label);
////        	    notchid = 0;
////    			break;
//    		default:
//    			break;
//    		}
        	//	Clear out the add tag section    	
        	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
        	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
        	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
        	TV  = (TextView) findViewById( R.id.new_tag_number);
        	//	Should reset these to the defaults per user preferences but for now just set to need data
        	tag_type_spinner2.setSelection(0);
        	tag_color_spinner.setSelection(0);
        	tag_location_spinner.setSelection(0);
        	TV.setText( "" );
        	}
     	}
	public boolean tableExists (String table){
		try {
	        dbh.exec("select * from "+ table);   
	        return true;
		} catch (SQLiteException e) {
			return false;
	        		}
	        	}
	
    // user clicked the "next record" button
    public void nextRecord( View v)
    {
    	//	Clear out the display first
    	clearBtn( v );
    	//	Go get the sheep id of this record
    	Log.i("in next record", "this sheep ID is " + String.valueOf(thissheep_id));
    	cursor.moveToNext();
    	Log.i("in next record", "after moving the cursor ");
    	thissheep_id = cursor.getInt(0);
    	thissheep_name = cursor.getString(1);
    	Log.i("in next record", "this sheep ID is " + String.valueOf(thissheep_id));
    	Log.i("in next record", "this sheep name is " + thissheep_name);
    	recNo         += 1;
    	findTagsShowAlert(v, thissheep_id);
//		// I've moved forward so I need to enable the previous record button
		Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
		btn3.setEnabled(true);	    	
    	if (recNo == (nRecs)) {
    		// at end so disable next record button
    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
        	btn2.setEnabled(false);   		
    	}
    }

    // user clicked the "previous record" button
    public void previousRecord( View v){
//    	Clear out the display first
    	clearBtn( v );
    	Log.i("in prev record", "this sheep ID is " + String.valueOf(thissheep_id));
    	cursor.moveToPrevious();
    	Log.i("in prev record", "after moving the cursor ");
    	thissheep_id = cursor.getInt(0);
    	Log.i("in prev record", "this sheep ID is " + String.valueOf(thissheep_id));
    	recNo         -= 1;
    	findTagsShowAlert(v, thissheep_id);
		// I've moved back so enable the next record button
		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
		btn2.setEnabled(true);      		
    	if (recNo == 1) {
    		// at beginning so disable prev record button
    		Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
        	btn3.setEnabled(false);   		
    	}
    }
}
