package com.weyr_associates.lambtracker;
// This should be the fixed LookUpSheep Class that doesn't leak when the user selects back.

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

public class LookUpSheep extends ListActivity
	{
	private DatabaseHandler dbh;
	int             id;
	String 			logmessages;
	public int 		thissheep_id, thissire_id, thisdam_id;
	int             fedtagid, farmtagid, eidtagid;
	
	public String 	tag_type_label, tag_color_label, tag_location_label, eid_tag_color_label ;
	public String 	eid_tag_location_label, eidText, alert_text;
	public String 	thissire_name, thisdam_name;
	public Cursor 	cursor, cursor2, cursor3, cursor4, cursor5;
	public Object	crsr;
	public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner ;
	public List<String> tag_types, tag_locations, tag_colors;
	public Spinner predefined_note_spinner01, predefined_note_spinner02, predefined_note_spinner03;
	public Spinner predefined_note_spinner04, predefined_note_spinner05;
	public List<String> predefined_notes;
	public String[] this_sheeps_tags ;
	
	public int             nRecs, nRecs4;
	private int			    recNo;
//	private String[]        colNames;
	
	int[] tagViews;

	ArrayAdapter<String> dataAdapter;
	String     	cmd, cmd2;
	Integer 	i;	
	public Button btn;
	
	public SimpleCursorAdapter myadapter, myadapter2;

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
				//We have a good whole EID number	
				Log.i ("in handler case" , "got eid of " + LastEID);
				gotEID ();	
				break;			
			case eidService.MSG_UPDATE_LOG_APPEND:
				//Bundle b3 = msg.getData();
				//Log.i("Convert", "Add to Log.");

				break;
			case eidService.MSG_UPDATE_LOG_FULL:
				//Log.i("Convert", "Log Full.");

				break;
			case eidService.MSG_THREAD_SUICIDE:
				Log.i("Convert", "Service informed Activity of Suicide.");
				doUnbindService();
				stopService(new Intent(LookUpSheep.this, eidService.class));

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
				//msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
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
			//Log.i("Convert", "is.");
			doBindService();
		} else {
			//Log.i("Convert", "is not, start it");
			startService(new Intent(LookUpSheep.this, eidService.class));
			doBindService();
		}
		//Log.i("Convert", "Done isRunning.");
	} 	

	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		//Log.i("Convert", "At doBind1.");
		bindService(new Intent(this, eidService.class), mConnection, Context.BIND_AUTO_CREATE);
		//Log.i("Convert", "At doBind2.");

		mIsBound = true;

		if (mService != null) {
			//Log.i("Convert", "At doBind3.");
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
		//Log.i("Convert", "At doBind5.");
	}
	void doUnbindService() {
		//Log.i("Convert", "At DoUnbindservice");
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

	public void gotEID( )
	{		
		//	make the scan eid button red
		btn = (Button) findViewById( R.id.scan_eid_btn );
		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
		// 	Display the EID number
		TextView TV = (TextView) findViewById (R.id.inputText);
		TV.setText( LastEID );
		Log.i("in gotEID ", "with LastEID of " + LastEID);
		
	}	
	/////////////////////////////////////////////////////	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup_sheep);
        Log.i("LookUpSheep", " after set content view");
        View v = null;
        String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("LookUpSheep", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
    	Object crsr;
    	int     nrCols;
//		Added the variable definitions here    	
      	String          cmd;
      	String 			results, results2;
    	Boolean			exists;

    	 //////////////////////////////////// 
		CheckIfServiceIsRunning();
		Log.i("LookUpSheep", "back from isRunning");  	
		////////////////////////////////////    	
		
		thissheep_id = 0;
		
     	// Fill the Tag Type Spinner
     	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
    	tag_types = new ArrayList<String>();      	
    	
    	// Select All fields from id types to build the spinner
        cmd = "select * from id_type_table";
        crsr = dbh.exec( cmd );  
        cursor5   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	tag_types.add("Select a Type");
         // looping through all rows and adding to list
    	for (cursor5.moveToFirst(); !cursor5.isAfterLast(); cursor5.moveToNext()){
    		tag_types.add(cursor5.getString(1));
    	}
    	
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_type_spinner.setAdapter (dataAdapter);
		//	set initial tag type to look for to be federal tag
		tag_type_spinner.setSelection(1);	

       	// make the alert button normal and disabled
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false);  
 // TODO   	
       	//	Disable the Next Record and Prev. Record button until we have multiple records
    	btn = (Button) findViewById( R.id.next_rec_btn );
    	btn.setEnabled(false); 
    	btn = (Button) findViewById( R.id.prev_rec_btn );
    	btn.setEnabled(false);
    	
		//	make the scan eid button red
		btn = (Button) findViewById( R.id.scan_eid_btn );
		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));

        }
	public void lookForSheep (View v){

		Object crsr, crsr2, crsr3, crsr4;
		Boolean exists;
		TextView TV;
		ListView notelist = (ListView) findViewById(R.id.list2);
        exists = true;
     // Hide the keyboard when you click the button
    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    	
        TV = (TextView) findViewById( R.id.inputText );
    	String	tag_num = TV.getText().toString();
    	
        Log.i("LookForSheep", " got to lookForSheep with Tag Number of " + tag_num);
        Log.i("LookForSheep", " got to lookForSheep with Tag type of " + tag_type_spinner.getSelectedItemPosition());
        exists = tableExists("sheep_table");
        if (exists){
        	if( tag_num != null && tag_num.length() > 0 ){
//        		Get the sheep id from the id table for this tag number and selected tag type
	        	cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
	        			"and id_info_table.tag_type='%s' and id_info_table.tag_date_off is null", tag_num , tag_type_spinner.getSelectedItemPosition());  	        	
	        	Log.i("LookForSheep", " command is  " + cmd);
	        	crsr = dbh.exec( cmd );
	        	cursor   = ( Cursor ) crsr; 
	        	recNo    = 1;
				nRecs    = cursor.getCount();
				Log.i("LookUpSheep", " nRecs = "+ String.valueOf(nRecs));
//				colNames = cursor.getColumnNames();
//	    		startManagingCursor(cursor);
	        	dbh.moveToFirstRecord();
	        	if( dbh.getSize() == 0 )
		    		{ // no sheep with that tag in the database so clear out and return
		    		clearBtn( v );
		    		TV = (TextView) findViewById( R.id.sheepnameText );
		        	TV.setText( "Cannot find this sheep." );
		        	return;
		    		}
	        	// TODO add the next record previous record stuff in here
	        	if (nRecs >1){
	        		//	Have multiple sheep with this tag so enable next button
	            	btn = (Button) findViewById( R.id.next_rec_btn );
	            	btn.setEnabled(true);       		
	        	}
//	        	Log.i("LookForSheep", "This sheep is record " + String.valueOf(thissheep_id));	        	
	        	//	We need to call the format the record method
	        	formatSheepRecord(v);
				}else{
	        	return;
	        }
	        Log.i("lookForSheep", " out of the if statement");
        	}
    		else {
    			clearBtn( null );
            	TV = (TextView) findViewById( R.id.sheepnameText );
                TV.setText( "Sheep Database does not exist." );                
        	}
	}
	// TODO
	
public void formatSheepRecord (View v){
	Object crsr, crsr2, crsr3, crsr4;
	TextView TV;
	ListView notelist = (ListView) findViewById(R.id.list2);
	
	thissheep_id = cursor.getInt(0);	        	
	Log.i("format record", "This sheep is record " + String.valueOf(thissheep_id));	        	
	
//	Log.i("format record", " recNo = "+ String.valueOf(recNo));
	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
			"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
			"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01,  " +
			"sheep_table.sire_id, sheep_table.dam_id, sheep_table.birth_date, birth_type_table.birth_type," +
			"sheep_sex_table.sex_name " +
			"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
			"inner join birth_type_table on id_birthtypeid = sheep_table.birth_type " +
			"inner join sheep_sex_table on sheep_sex_table.sex_sheepid = sheep_table.sex " +
			"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
			"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
			"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
			"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", thissheep_id);

	crsr = dbh.exec( cmd ); 	    		
	cursor5   = ( Cursor ) crsr; 
	cursor5.moveToFirst();				
	TV = (TextView) findViewById( R.id.sheepnameText );
    TV.setText (dbh.getStr(0));
    TV = (TextView) findViewById( R.id.birth_date );
    TV.setText (dbh.getStr(11));
    TV = (TextView) findViewById( R.id.birth_type );
    TV.setText (dbh.getStr(12));
    TV = (TextView) findViewById( R.id.sheep_sex );
    TV.setText (dbh.getStr(13));
    
    alert_text = dbh.getStr(8);
	
    //	Get the sire and dam id numbers
    thissire_id = dbh.getInt(9);
    Log.i("format record", " Sire is " + String.valueOf(thissire_id));
    thisdam_id = dbh.getInt(10);
    Log.i("format record", " Dam is " + String.valueOf(thisdam_id));
    
    //	Go get the sire name
    if (thissire_id != 0){
        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thissire_id);
        Log.i("format record", " cmd is " + cmd);		        
        crsr2 = dbh.exec( cmd);
        Log.i("format record", " after second db lookup");
        cursor2   = ( Cursor ) crsr2; 
//		startManagingCursor(cursor2);
		cursor2.moveToFirst();
		TV = (TextView) findViewById( R.id.sireName );
		thissire_name = dbh.getStr(0);
		TV.setText (thissire_name);	 
		Log.i("format record", " Sire is " + thissire_name);
        Log.i("format record", " Sire is " + String.valueOf(thissire_id));
    }
    if(thisdam_id != 0){
        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thisdam_id);
        crsr3 = dbh.exec( cmd);
        cursor3   = ( Cursor ) crsr3; 
//		startManagingCursor(cursor3);
		cursor3.moveToFirst();
		TV = (TextView) findViewById( R.id.damName );
		thisdam_name = dbh.getStr(0);
		TV.setText (thisdam_name);	
		Log.i("format record", " Dam is " + thisdam_name);
        Log.i("format record", " Dam is " + String.valueOf(thisdam_id));
    }    		
	Log.i("FormatRecord", " before formatting results");
	
	//	Get set up to try to use the CursorAdapter to display all the tag data
	//	Select only the columns I need for the tag display section
    String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
	Log.i("FormatRecord", "after setting string array fromColumns");
	//	Set the views for each column for each line. A tag takes up 1 line on the screen
    int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
    Log.i("FormatRecord", "after setting string array toViews");
    myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor5 ,fromColumns, toViews, 0);
    Log.i("FormatRecord", "after setting myadapter");
    setListAdapter(myadapter);
    Log.i("FormatRecord", "after setting list adapter");

	// Now we need to check and see if there is an alert for this sheep
//   	Log.i("Alert Text is " , alert_text);
//	Now to test of the sheep has an alert and if so then display the alert & set the alerts button to red
	if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
       	// make the alert button red
    	Button btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	btn.setEnabled(true); 
    	//	testing whether I can put up an alert box here without issues
    	showAlert(v);
	}
	//	Now go get all the notes for this sheep and format them
	cmd = String.format( "select sheep_note_table.id_noteid as _id, sheep_note_table.note_date, sheep_note_table.note_time, " +
			"sheep_note_table.note_text, predefined_notes_table.predefined_note_text " +
			" from sheep_note_table left join predefined_notes_table " +
			"on predefined_notes_table.id_predefinednotesid = sheep_note_table.id_predefinednotesid01" +
			" where sheep_id='%s' "+
			"order by note_date desc ", thissheep_id);  	        	
	Log.i("format record", " command is  " + cmd);
	crsr4 = dbh.exec( cmd );
	cursor4   = ( Cursor ) crsr4; 
//	startManagingCursor(cursor4);
	nRecs4    = cursor4.getCount();
	Log.i("lookForSheep", " nRecs4 is " + String.valueOf(nRecs4));
	cursor4.moveToFirst();	
	if (nRecs4 > 0) {
    	// format the note records
		//	Select only the columns I need for the note display section
    	String[] fromColumns2 = new String[ ]{ "note_date", "note_time", "note_text", "predefined_note_text"};
		Log.i("LookForSheep", "after setting string array fromColumns for notes");
		//	Set the views for each column for each line. A tag takes up 1 line on the screen
		int[] toViews2 = new int[] { R.id.note_date, R.id.note_time, R.id.note_text, R.id.predefined_note_text};
        Log.i("LookForSheep", "after setting string array toViews for notes");
        myadapter2 = new SimpleCursorAdapter(this, R.layout.note_entry, cursor4 ,fromColumns2, toViews2, 0);
        Log.i("LookForSheep", "after setting myadapter to show notes");
        notelist.setAdapter(myadapter2);
        Log.i("LookForSheep", "after setting list adapter to show notes");			
	}   
}
//  user clicked 'Scan' button    
 public void scanEid( View v){
 	// Here is where I need to get a tag scanned and put the data into the variable LastEID
	 clearBtn( v );
	 tag_type_spinner.setSelection(2);
	 if (mService != null) {
		try {
			//Start eidService sending tags
			Message msg = Message.obtain(null, eidService.MSG_SEND_ME_TAGS);
			msg.replyTo = mMessenger;
			mService.send(msg);
		   	//	make the scan eid button  green 0x0000FF00, 0xff00ff00
	    	Button btn = (Button) findViewById( R.id.scan_eid_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0x0000FF00, 0xff00ff00));
			
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
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

    public void helpBtn( View v )
    {
   	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_look_up_sheep)
	           .setTitle( R.string.help_warning );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int idx) {
	               // User clicked OK button 
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();		
    }
    
    // user clicked the 'back' button
    public void backBtn( View v )
	    {
//    	Log.i("Back Button", " In the lookupsheep back code at beginning");   
    	doUnbindService();
//    	Log.i("Back Button", " In lookupsheep back after dounbindservice");   
		stopService(new Intent(LookUpSheep.this, eidService.class));   	
//    	Log.i("Back Button", " In lookupsheep back after stop service");   
    	// Added this to close the database if we go back to the main activity  
    	//	Close cursors if there are any but fall out if we don't have any in use
		try {
//			Log.i("Back Button", " In try stmt cursor");   
//			stopManagingCursor (cursor);
			cursor.close();
		}
		catch (Exception e) {
//			Log.i("Back Button", " In catch stmt cursor");  
			// In this case there is no adapter so do nothing
		}
		try {
//			Log.i("Back Button", " In try stmt cursor2");   
//			stopManagingCursor (cursor2);
			cursor2.close();
		}
		catch (Exception e) {
//			Log.i("Back Button", " In catch stmt cursor2");  
			// In this case there is no adapter so do nothing
		}
		try {
//			Log.i("Back Button", " In try stmt cursor3");   
//			stopManagingCursor (cursor3);
			cursor3.close();
		}
		catch (Exception e) {
//			Log.i("Back Button", " In catch stmt cursor3");  
			// In this case there is no adapter so do nothing
		}
    	dbh.closeDB();
    	clearBtn( null );
    	//Go back to main
      	finish();
	    }
 
    public void showAlert (View v){
//    		String	alert_text;
            String          cmd;    
            Object 			crsr2;
            
    		// Display alerts here   	
    				AlertDialog.Builder builder = new AlertDialog.Builder( this );
    				Log.i("ShowAlert", "Alert Text is" + alert_text);
    				builder.setMessage( alert_text )
    			           .setTitle( R.string.alert_warning );
    				builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int idx) {
    			               // User clicked OK button   	  
    			               }
    			       });		
    				AlertDialog dialog = builder.create();
    				dialog.show();
//    				cursor2.close();	
    	}

   
    // user clicked 'clear' button
    public void clearBtn( View v )
	    {
    	thissheep_id = 0;
		TextView TV ;
		TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
		TV = (TextView) findViewById( R.id.sireName );
		TV.setText( "" );
		TV = (TextView) findViewById( R.id.damName );
		TV.setText( "" );
	    TV = (TextView) findViewById( R.id.birth_date );
	    TV.setText( "" );
	    TV = (TextView) findViewById( R.id.birth_type );
	    TV.setText( "" );
	    TV = (TextView) findViewById( R.id.sheep_sex );
	    TV.setText( "" );
		//	Need to clear out the rest of the tags here 
		Log.i("clear btn", "before changing myadapter");
		try {
			myadapter.changeCursor(null);
		}
		catch (Exception e) {
			// In this case there is no adapter so do nothing
		}
		try {
//			Log.i("lookup clrbtn", " before set notes to null");
			myadapter2.changeCursor(null);
		} catch (Exception e) {
			// In this case there is no adapter so do nothing
		}
//		Log.i("clear btn", "after changing myadapter and myadapter2");
		
    }
    public void doNote( View v )
    {	 
    	Utilities.takeNote(v, thissheep_id, this);
    }
//    private String TodayIs() {
// 		Calendar calendar = Calendar.getInstance();
// 		int day = calendar.get(Calendar.DAY_OF_MONTH);
// 		int month = calendar.get(Calendar.MONTH);
// 		int year = calendar.get(Calendar.YEAR);
// 		return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) ;
// 	}
//     private String Make2Digits(int i) {
// 		if (i < 10) {
// 			return "0" + i;
// 		} else {
// 			return Integer.toString(i);
// 		}
// 	}
//     public void takeNote( View v )
//     {	    	
//     	final Context context = this;
//     	if (thissheep_id == 0) {
//     		Log.i ("takeNote", " no sheep selected " + String.valueOf(thissheep_id));
//     		return;
//     	}
//     	else {
//     		//	First fill the predefined note spinner with possibilities
//         	predefined_notes = new ArrayList<String>();
//     		predefined_notes.add("Select a Predefined Note");
////     		Log.i ("takeNote", " after adding Select a Predefined Note");
//         	// Select All fields from predefined_notes_table to build the spinner
//             cmd = "select * from predefined_notes_table";
////             Log.i ("takeNote", " cmd is " + cmd);
//             crsr = dbh.exec( cmd );  
//             cursor5   = ( Cursor ) crsr;
//         	dbh.moveToFirstRecord();
//              // looping through all rows and adding to list
//         	for (cursor5.moveToFirst(); !cursor5.isAfterLast(); cursor5.moveToNext()){
//         		predefined_notes.add(cursor5.getString(1));
////         		Log.i ("takeNote", " in for loop predefined note id is " + String.valueOf(cursor.getString(1)));
//         	}
//         	cursor5.close();    
//         	Log.i ("takeNote", " after set the predefined note spinner ");
//         	Log.i ("takeNote", " this sheep is " + String.valueOf(thissheep_id));
//         	//Implement take a note stuff here
////     		Log.i ("takeNote", " got a sheep, need to get a note to add");
//     		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
////     		Log.i ("takeNote", " after getting new alertdialogbuilder");
//     		
//     		LayoutInflater li = LayoutInflater.from(context);
// 			View promptsView = li.inflate(R.layout.note_prompt, null);
//// 			Log.i ("takeNote", " after inflating layout");	
//
// 			// set view note_prompt to alertdialog builder
// 			alertDialogBuilder.setView(promptsView);
// 			Log.i ("takeNote", " after setting view");
// 		   	// Creating adapter for predefined notes spinners
// 	    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, predefined_notes);
// 	    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// 	    	predefined_note_spinner01 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner01);
// 	    	predefined_note_spinner01.setAdapter (dataAdapter);
// 			predefined_note_spinner01.setSelection(0);
// 			
// 	    	predefined_note_spinner02 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner02);
// 	    	predefined_note_spinner02.setAdapter (dataAdapter);
// 			predefined_note_spinner02.setSelection(0);
//
// 	    	predefined_note_spinner03 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner03);
// 	    	predefined_note_spinner03.setAdapter (dataAdapter);
// 			predefined_note_spinner03.setSelection(0);
//
// 	    	predefined_note_spinner04 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner04);
// 	    	predefined_note_spinner04.setAdapter (dataAdapter);
// 			predefined_note_spinner04.setSelection(0);
//
// 	    	predefined_note_spinner05 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner05);
// 	    	predefined_note_spinner05.setAdapter (dataAdapter);
// 			predefined_note_spinner05.setSelection(0);
//
// 			final EditText userInput = (EditText) promptsView
// 					.findViewById(R.id.note_text);
//
// 			// set dialog message
// 			alertDialogBuilder
// 				.setCancelable(false)
// 				.setPositiveButton("Save Note",
// 				  new DialogInterface.OnClickListener() {
// 				    public void onClick(DialogInterface dialog,int id) {
// 					// get user input and set it to result
// 					// edit text
// 					String note_text = dbh.fixApostrophes(String.valueOf(userInput.getText()));
// 					Log.i("update notes ", "note text is " + note_text);
// 					//	Get id_predefinednotesid from a spinner here 
// 					int predefined_note01 = predefined_note_spinner01.getSelectedItemPosition();
// 					int predefined_note02 = predefined_note_spinner02.getSelectedItemPosition();
// 					int predefined_note03 = predefined_note_spinner03.getSelectedItemPosition();
// 					int predefined_note04 = predefined_note_spinner04.getSelectedItemPosition();
// 					int predefined_note05 = predefined_note_spinner05.getSelectedItemPosition();
// 					// Update the notes table with the data
// 					if (predefined_note01 > 0) {
// 						cmd = String.format("insert into sheep_note_table (sheep_id, note_text, note_date, note_time, " +
// 							"id_predefinednotesid01) " +
// 							"values ( %s, '%s', '%s', '%s', %s )",
// 	    					thissheep_id, note_text, Utilities.TodayIs(), Utilities.TimeIs(), predefined_note01);
//	 	    			Log.i("update notes ", "before cmd " + cmd);
//	 	    			dbh.exec( cmd );	
//	 	    			Log.i("update notes ", "after cmd exec");
//	 	    			Log.i("take note","first note written with predefined note");
// 				    }else{
// 				    	//	no predefined note so write one without it
// 				    	cmd = String.format("insert into sheep_note_table (sheep_id, note_text, note_date, note_time) " +
// 	 							"values ( %s, '%s', '%s', '%s')",
// 	 	    					thissheep_id, note_text, Utilities.TodayIs(), Utilities.TimeIs());
//	 	    			Log.i("update notes ", "before cmd " + cmd);
//	 	    			dbh.exec( cmd );	
//	 	    			Log.i("update notes ", "after cmd exec");
//	 	    			Log.i("take note","first note written ");
// 				    }
// 	    			if (predefined_note02 > 0) {
// 	    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
// 	 							"id_predefinednotesid01) " +
// 	 							"values ( %s, '%s', '%s', %s)",
// 	 	    					thissheep_id, Utilities.TodayIs(), Utilities.TimeIs(), predefined_note02 );
// 	 	    			Log.i("update notes ", "before cmd " + cmd);
// 	 	    			dbh.exec( cmd );
// 	 	    			Log.i("take note","second note written");
// 	    	 		}
// 	    			if (predefined_note03 > 0) {
// 	    	 			Log.i("take note","third note written");
// 	    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
// 	 							"id_predefinednotesid01) " +
// 	 							"values ( %s, '%s', '%s', %s)",
// 	 	    					thissheep_id, Utilities.TodayIs(), Utilities.TimeIs(), predefined_note03 );
// 	 	    			Log.i("update notes ", "before cmd " + cmd);
// 	 	    			dbh.exec( cmd );	
// 	    	 		}
// 	    			if (predefined_note04 > 0) {
// 	    	 			Log.i("take note","fourth note written");
// 	    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
// 	 							"id_predefinednotesid01) " +
// 	 							"values ( %s, '%s', '%s', %s)",
// 	 	    					thissheep_id, Utilities.TodayIs(), Utilities.TimeIs(), predefined_note04 );
// 	 	    			Log.i("update notes ", "before cmd " + cmd);
// 	 	    			dbh.exec( cmd );	
// 	    	 		}
// 	    			if (predefined_note05 > 0) {
// 	    	 			Log.i("take note","fifth note written");
// 	    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
// 	 							"id_predefinednotesid01) " +
// 	 							"values ( %s, '%s', '%s', %s)",
// 	 	    					thissheep_id, Utilities.TodayIs(), Utilities.TimeIs(), predefined_note05 );
// 	 	    			Log.i("update notes ", "before cmd " + cmd);
// 	 	    			dbh.exec( cmd );	
// 	    	 		}
// 				    }
// 				  })
// 				.setNegativeButton("Cancel",
// 				  new DialogInterface.OnClickListener() {
// 				    public void onClick(DialogInterface dialog,int id) {
// 					dialog.cancel();
// 				    }
// 				  });
// 			// create alert dialog
// 			AlertDialog alertDialog = alertDialogBuilder.create();
// 			// show it
// 			alertDialog.show();
//     	}   	
//     }
//	    private String TimeIs() {
//			Calendar calendar = Calendar.getInstance();
//	        //12 hour format
////			int hour = cal.get(Calendar.HOUR);
//	        //24 hour format
//			int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
//			int minute = calendar.get(Calendar.MINUTE);
//			int second = calendar.get(Calendar.SECOND);
//			  
//			return Make2Digits(hourofday) + ":" + Make2Digits(minute) + ":" + Make2Digits(second) ;
//		}
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
	    	Log.i("in next record", "this sheep ID is " + String.valueOf(thissheep_id));
	    	recNo         += 1;
	    	formatSheepRecord(v);
//    		// I've moved forward so I need to enable the previous record button
    		Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
    		btn3.setEnabled(true);	    	
	    	if (recNo == (nRecs)) {
	    		// at end so disable next record button
	    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
	        	btn2.setEnabled(false);   		
	    	}
	    }

	    // user clicked the "previous record" button
	    public void prevRecord( View v){
//	    	Clear out the display first
	    	clearBtn( v );
	    	Log.i("in prev record", "this sheep ID is " + String.valueOf(thissheep_id));
	    	cursor.moveToPrevious();
	    	Log.i("in prev record", "after moving the cursor ");
	    	thissheep_id = cursor.getInt(0);
	    	Log.i("in prev record", "this sheep ID is " + String.valueOf(thissheep_id));
	    	recNo         -= 1;
	    	formatSheepRecord(v);
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
