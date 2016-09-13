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
	String     	cmd, cmd2;
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
		tag_type_spinner.setSelection(2);
		
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

//			Now to test of the sheep has an alert and if so then display the alert & set the alerts button to red
			if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
		       	// make the alert button red
		    	Button btn = (Button) findViewById( R.id.alert_btn );
		    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
		    	btn.setEnabled(true); 
		    	showAlert(v);
			}
	}	
	
	// Catches clicks and updates selectedItem - can't believe this is necessary in this day and age
	@Override protected void onListItemClick (ListView l, View v, int position, long id)
	{
		Log.i("IDManagement", "Listitemclick set id" + String.format("%d", id));
		selectedItem = id;
//		v.setSelected(true);

	}

    // user clicked 'remove tag' button
    public void removeTag( final View v)
    	{
    	
		// Check to see that something has been selected
		if (selectedItem == android.widget.AdapterView.INVALID_ROW_ID)
		{
			Toast.makeText(this, "No Tag Selected", Toast.LENGTH_SHORT).show();
			return;
		}
		
 	   Log.i("removetag", " tag record to remove is item " + String.valueOf(selectedItem) );
 	  final int tag_to_remove = (int) selectedItem;
			AlertDialog.Builder builder = new AlertDialog.Builder( this );
			// need to get the tag data and add to alert
			// tag_number, tag_color_name, id_location_abbrev, idtype_name


		//	builder.setMessage( R.string.delete_tag + cmd2 )
			builder.setMessage( R.string.delete_tag )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- remove the tag
    	        	   //add a tag_date_off of today to the tag
    	        	   String today = Utilities.TodayIs();

					   Log.i("removetag", "date is " + today);
    	        	   Log.i("removetag", "tag record is " + String.valueOf(tag_to_remove) );
    	        	   
    	       		   String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", tag_to_remove );
    	       		   Log.i("removetag", " command is " + cmd);
    	       		   dbh.exec( cmd );
    	       		   findTagsShowAlert (v, thissheep_id); 

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
    	int	 	flock_id; // pointer into the flock id table so for the federal tag flock ID record
    	int		official_id_type;
    	//	Disable the update database button so we don't get 2 records entered
       	btn = (Button) findViewById( R.id.update_database_btn );
    	btn.setEnabled(false); 

    	// Get the values from the UI screen
    	TextView TV = (TextView) findViewById( R.id.sheepnameText );
    	sheepnameText = TV.getText().toString();
    	Log.i("update everything ", "sheep name " + sheepnameText);   	
    	Log.i("update everything ", "sheep_id is " + String.valueOf(thissheep_id));   	

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

		    		}else{
						official_id_flag = 0;

		    		}
					cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male," +
									" tag_color_female, tag_location, tag_date_on, tag_number, official_id) values " +
									" (%s, %s, %s,%s,%s, '%s', '%s', %s) ", thissheep_id, 2, new_tag_color, new_tag_color,
							new_tag_location, today, new_tag_number, official_id_flag);
					Log.i("add tag to ", "db cmd is " + cmd);
					dbh.exec(cmd);
					Log.i("add tag ", "after insert into id_info_table");

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
    }
    public void addNewTag( View v ){
//    	Object crsr;
    	Log.i ("in add tag", " start of addNewTag code");
//       	btn = (Button) findViewById( R.id.update_display_btn );
//    	btn.setEnabled(true); 
    	//	Enable the scanner so we can add EID tags
    	scanEid (v); 
    	
    	//make the new tag number the last eid read
		TextView TV = (TextView) findViewById (R.id.new_tag_number);
		TV.setText( LastEID );

//		new_tag_number = findViewById(R.id.new_tag_number);
//		new_tag_number = LastEID;
    	
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
