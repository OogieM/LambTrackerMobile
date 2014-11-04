package com.weyr_associates.lambtracker;
// This should be the fixed LookUpSheep Class that doesn't leak when the user selects back.

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Context;
import android.content.res.Configuration;
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
	public Cursor 	cursor, cursor2, cursor3, cursor4, cursor5, drugCursor;
	public Object	crsr;
	public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner ;
	public List<String> tag_types, tag_locations, tag_colors;
	public Spinner predefined_note_spinner01, predefined_note_spinner02, predefined_note_spinner03;
	public Spinner predefined_note_spinner04, predefined_note_spinner05;
	public List<String> predefined_notes;
	public String[] this_sheeps_tags ;
	
	public int             nRecs, nRecs4, drugRecs;
	private int			    recNo;
//	private String[]        colNames;
	
	int[] tagViews;

	ArrayAdapter<String> dataAdapter;
	String     	cmd, cmd2;
	Integer 	i;	
	public Button btn;
	
	public SimpleCursorAdapter myadapter, myadapter2, drugAdapter;

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

//		Object crsr, crsr2, crsr3, crsr4;
		Object crsr;
		Boolean exists;
		TextView TV;
		ListView notelist = (ListView) findViewById(R.id.list2);
		
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
//    						cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
//				        			"and id_info_table.tag_type='%s' and (id_info_table.tag_date_off is null or" +
//				        			" id_info_table.tag_date_off = '') "
//				        			, tag_num , tag_type_spinner.getSelectedItemPosition());  
    						cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
				        			"and id_info_table.tag_type='%s' "
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
				        	Log.i("searchByNumber", " Before formatting the record");	        	
				        	//	We need to call the format the record method
				        	formatSheepRecord(v);   	
    					}
    					break;
				    case 6:
				    	//	got a split
				    	//	Assume no split ears at this time. 
				    	//	Needs modification for future use
				    	TV = (TextView) findViewById( R.id.sheepnameText );
			        	TV.setText( "Cannot search on splits yet." );
				    	// TODO
				        break;
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
				    	// TODO
			        	tag_num = "%" + tag_num + "%";
			        	// Modified this so I can look up removed sheep as well 
//			        	cmd = String.format( "select sheep_id, sheep_name from sheep_table where sheep_name like '%s'" +
//			        			" and (remove_date is null or remove_date = '') "
//			        			, tag_num );  
			        	cmd = String.format( "select sheep_id, sheep_name from sheep_table where sheep_name like '%s'"
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
				    		{ // no sheep with that name in the database so clear out and return
				    		clearBtn( v );
				    		TV = (TextView) findViewById( R.id.sheepnameText );
				        	TV.setText( "Cannot find this sheep." );
				        	return;
				    		}
			        	thissheep_id = dbh.getInt(0);			        	
			        	if (nRecs >1){
			        		//	Have multiple sheep with this name so enable next button
			            	btn = (Button) findViewById( R.id.next_rec_btn );
			            	btn.setEnabled(true);       		
			        	}
			        	//	We need to call the format the record method
			        	formatSheepRecord(v);   	
				        break;
    				} // end of case switch   			
    		}else {
    			clearBtn( null );
            	TV = (TextView) findViewById( R.id.sheepnameText );
                TV.setText( "Sheep Database does not exist." ); 
         	}
	}
	
public void formatSheepRecord (View v){
	Object crsr, crsr2, crsr3, crsr4, drugCrsr;
	TextView TV;
	ListView notelist = (ListView) findViewById(R.id.list2);
	ListView drugList = (ListView) findViewById(R.id.druglist);
	
	thissheep_id = cursor.getInt(0);	        	
	Log.i("format record", "This sheep is record " + String.valueOf(thissheep_id));	        	
//	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
//			"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
//			"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01,  " +
//			"sheep_table.sire_id, sheep_table.dam_id, sheep_table.birth_date, birth_type_table.birth_type," +
//			"sheep_sex_table.sex_name, sheep_table.birth_weight, sheep_table.remove_date, sheep_table.death_date," +
//			"remove_reason_table.remove_reason " +
//			"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
//			"inner join birth_type_table on id_birthtypeid = sheep_table.birth_type " +
//			"inner join sheep_sex_table on sheep_sex_table.sex_sheepid = sheep_table.sex " +
//			"inner join remove_reason_table on sheep_table.remove_reason = remove_reason_table.remove_reasonid " +
//			"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
//			"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
//			"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
//			"where id_info_table.sheep_id ='%s' and (id_info_table.tag_date_off is null or " +
//			"id_info_table.tag_date_off is '')order by idtype_name asc", thissheep_id);
	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
			"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
			"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01,  " +
			"sheep_table.sire_id, sheep_table.dam_id, sheep_table.birth_date, birth_type_table.birth_type," +
			"sheep_sex_table.sex_name, sheep_table.birth_weight, sheep_table.remove_date, sheep_table.death_date  " +
//			"remove_reason_table.remove_reason " +
			"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
			"inner join birth_type_table on id_birthtypeid = sheep_table.birth_type " +
			"inner join sheep_sex_table on sheep_sex_table.sex_sheepid = sheep_table.sex " +
//			"inner join remove_reason_table on sheep_table.remove_reason = remove_reason_table.remove_reasonid " +
			"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
			"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
			"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
			"where id_info_table.sheep_id ='%s' and (id_info_table.tag_date_off is null or " +
			"id_info_table.tag_date_off is '')order by idtype_name asc", thissheep_id);
	
	
	Log.i("format record", " comand is " + cmd);	
	crsr = dbh.exec( cmd ); 
	Log.i("format record", " after run the command");
	cursor5   = ( Cursor ) crsr; 
	Log.i("format record", " the cursor is of size " + String.valueOf(dbh.getSize()));
	cursor5.moveToFirst();				
	TV = (TextView) findViewById( R.id.sheepnameText );
    TV.setText (dbh.getStr(0));
    Log.i("format record", "after get sheep name ");
    TV = (TextView) findViewById( R.id.birth_date );
    TV.setText (dbh.getStr(11));
    Log.i("format record", "after get birth date ");
    TV = (TextView) findViewById( R.id.birth_type );
    TV.setText (dbh.getStr(12));
    Log.i("format record", "after get birth type ");
    TV = (TextView) findViewById( R.id.sheep_sex );
    TV.setText (dbh.getStr(13));
    Log.i("format record", "after get sheep sex ");
    TV = (TextView) findViewById( R.id.birth_weight );
    TV.setText (String.valueOf(dbh.getReal(14)));
    Log.i("format record", "after get birth weight ");
    TV = (TextView) findViewById( R.id.remove_date );
    TV.setText (dbh.getStr(15));
    Log.i("format record", "after get remove date ");
//    TV = (TextView) findViewById( R.id.remove_reason );
//    TV.setText (dbh.getStr(16));
//    Log.i("format record", "after get remove reason ");
    TV = (TextView) findViewById( R.id.death_date );
    TV.setText (dbh.getStr(16));
    Log.i("format record", "after get death date ");
    
    alert_text = dbh.getStr(8);
    Log.i("format record", "after get alert ");
   
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
		cursor2.moveToFirst();
		TV = (TextView) findViewById( R.id.sireName );
		thissire_name = dbh.getStr(0);
		TV.setText (thissire_name);	 
		Log.i("format record", " Sire is " + thissire_name);
        Log.i("format record", " Sire is " + String.valueOf(thissire_id));
    }
    //	Go get the dam name
    if(thisdam_id != 0){
        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thisdam_id);
        crsr3 = dbh.exec( cmd);
        cursor3   = ( Cursor ) crsr3; 
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
	cmd = String.format( "select sheep_note_table.id_noteid as _id, sheep_note_table.note_date, " +
	// cmd = String.format( "select sheep_note_table.id_noteid as _id, sheep_note_table.note_date, sheep_note_table.note_time, " +
			"sheep_note_table.note_text, predefined_notes_table.predefined_note_text " +
			" from sheep_note_table left join predefined_notes_table " +
			"on predefined_notes_table.id_predefinednotesid = sheep_note_table.id_predefinednotesid01" +
			" where sheep_id='%s' "+
			"order by note_date desc ", thissheep_id);  	        	
	Log.i("format record", " command is  " + cmd);
	crsr4 = dbh.exec( cmd );
	cursor4   = ( Cursor ) crsr4; 
	nRecs4    = cursor4.getCount();
	Log.i("lookForSheep", " nRecs4 is " + String.valueOf(nRecs4));
	cursor4.moveToFirst();	
	if (nRecs4 > 0) {
    	// format the note records
		// Pulled this from the note entry xml file to remove the time from a note. Can be added back if needed
//		<TextView
//        android:id="@+id/note_time"
//        android:layout_width="wrap_content"
//        android:layout_height="wrap_content"
//        android:textSize="14sp" />
		//	Select only the columns I need for the note display section
//    	String[] fromColumns2 = new String[ ]{ "note_date", "note_time", "note_text", "predefined_note_text"};
    	String[] fromColumns2 = new String[ ]{ "note_date", "note_text", "predefined_note_text"};
		Log.i("LookForSheep", "after setting string array fromColumns for notes");
		//	Set the views for each column for each line. A tag takes up 1 line on the screen
		//Removed the time from a note
//		int[] toViews2 = new int[] { R.id.note_date, R.id.note_time, R.id.note_text, R.id.predefined_note_text};
		int[] toViews2 = new int[] { R.id.note_date, R.id.note_text, R.id.predefined_note_text};
        Log.i("LookForSheep", "after setting string array toViews for notes");
        myadapter2 = new SimpleCursorAdapter(this, R.layout.note_entry, cursor4 ,fromColumns2, toViews2, 0);
        Log.i("LookForSheep", "after setting myadapter to show notes");
        notelist.setAdapter(myadapter2);
        Log.i("LookForSheep", "after setting list adapter to show notes");			
	}  		
	//	Bugfix: Last sheep's notes remain if sheep with no notes looked up
	//	Publish an empty notes list if the sheep doesn't have any notes.
	//	From: Alex Evans <alex.evans@gmail.com>
	//	Date: Tue, 24 Jun 2014 17:09:01 -0600
	else {
				// No note data - publish an empty list to clear notes
				Log.i("LookForSheep", "no notes for this sheep");
				myadapter2 = new SimpleCursorAdapter(this, R.layout.note_entry, null, null, null, 0);
				notelist.setAdapter(myadapter2);
	} 
		
		// Look up drug data for this sheep		
		cmd = String.format("SELECT sheep_drug_table.id_sheepdrugid AS _id, sheep_drug_table.drug_date_on, drug_table.drug_lot, drug_table.official_drug_name " +
					"FROM drug_table, sheep_drug_table " +
					"WHERE sheep_drug_table.drug_id  = drug_table.id_drugid " +
					"AND sheep_drug_table.sheep_id = '%s' " +
					"ORDER BY drug_date_on desc",thissheep_id);
		
		drugCrsr = dbh.exec(cmd);
		drugCursor = (Cursor) drugCrsr;
		drugRecs = drugCursor.getCount();		
		Log.i("lookForSheep", " drugRecs is " + String.valueOf(drugRecs));		
		drugCursor.moveToFirst();	
		if (drugRecs > 0) {
	    	// format the drug records
			//	Select drug record columns
	    	String[] fromColumnsDrug = new String[ ]{ "drug_date_on", "drug_lot", "official_drug_name"};
			Log.i("LookForSheep", "after setting string array fromColumns for drugs");
			int[] toViewsDrug = new int[] { R.id.drug_date_on, R.id.drug_lot, R.id.official_drug_name};
	        Log.i("LookForSheep", "after setting string array toViews for drugs");
	        drugAdapter = new SimpleCursorAdapter(this, R.layout.drug_entry, drugCursor, fromColumnsDrug, toViewsDrug, 0);
	        Log.i("LookForSheep", "after setting drugAdapter to show drugs");
	        drugList.setAdapter(drugAdapter);
	        Log.i("LookForSheep", "after setting list to show drugs");			
		}   
		else
		{
			// No drug data - publish an empty list to clear drugs
			Log.i("LookForSheep", "no drugs for this sheep");
			drugAdapter = new SimpleCursorAdapter(this, R.layout.drug_entry, null, null, null, 0);
			drugList.setAdapter(drugAdapter);
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
	    TV = (TextView) findViewById( R.id.birth_weight );
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

				try {
						drugAdapter.changeCursor(null);
					} catch (Exception e) {
						// In this case there is no adapter so do nothing
					}		
    }
    public void doNote( View v )
    {	 
    	Utilities.takeNote(v, thissheep_id, this);
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
