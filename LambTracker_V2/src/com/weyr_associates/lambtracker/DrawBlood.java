package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.weyr_associates.lambtracker.LambingSheep.IncomingHandler;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import com.google.zxing.client.android.Intents;

public class DrawBlood extends ListActivity {
	private DatabaseHandler dbh;
	int             id;
	String 			logmessages;
	String		tempLabel;
	public int 		thissheep_id, thissire_id, thisdam_id;
	public int		codon171, codon154, codon136;
	int             fedtagid, farmtagid, eidtagid;
	
	public String 	tag_type_label, tag_color_label, tag_location_label, eid_tag_color_label ;
	public String 	eid_tag_location_label, eidText, alert_text;
	public String 	thissire_name, thisdam_name;

	public Cursor 	cursor, cursor2, cursor3, cursor4, cursor5;
	public Object 	crsr, crsr2, crsr3, crsr4, crsr5;
	public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner ;
	public Spinner predefined_note_spinner01, predefined_note_spinner02, predefined_note_spinner03;
	public Spinner predefined_note_spinner04, predefined_note_spinner05;
	public List<String> predefined_notes;
	public List<String> tag_types, tag_locations, tag_colors;
	public List<String> blood_tests;
	public List<Integer> blood_test_id;
	public String[] this_sheeps_tags ;
	public String which_blood_test;
	public int	 which_blood;
	public CheckBox 	 boxblood;
	public String note_text;
	public int predefined_note01, predefined_note02, predefined_note03, predefined_note04, predefined_note05;
	public int             nRecs, nRecs1, nRecs2, nRecs3, nRecs4, nRecs5;
	private int			    recNo;
	// private String[]        colNames;
	private String LabelText = "";
	private String EID = "";
	private String SheepName = "";
	private Boolean AutoPrint = false;
	int[] tagViews;

	ArrayAdapter<String> dataAdapter;
	String     	cmd, dam_name;
	Integer 	i;	
	public Button btn;
	
	public SimpleCursorAdapter myadapter, myadapter2, myadapter3, myadapter4, myadapter5;

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
				stopService(new Intent(DrawBlood.this, eidService.class));
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void LoadPreferences(Boolean NotifyOfChanges) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Log.i("DrawBlood", "Load Pref.");
		try {
			String TextForLabel = preferences.getString("label", "text");
			LabelText = TextForLabel;
			AutoPrint = preferences.getBoolean("autop", false);
		} catch (NumberFormatException nfe) {}
		
	}
	 public ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
//			Log.i("DrawBlood", "At Service.");
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
//		Log.i("DrawBlood", "At isRunning?.");
		if (eidService.isRunning()) {
//			Log.i("DrawBlood", "is.");
			doBindService();
		} else {
//			Log.i("DrawBlood", "is not, start it");
			startService(new Intent(DrawBlood.this, eidService.class));
			doBindService();
		}
//		Log.i("DrawBlood", "Done isRunning.");
	} 	
	
	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
//		Log.i("DrawBlood", "At doBind1.");
		bindService(new Intent(this, eidService.class), mConnection, Context.BIND_AUTO_CREATE);
//		Log.i("DrawBlood", "At doBind2.");

		mIsBound = true;

		if (mService != null) {
//			Log.i("DrawBlood", "At doBind3.");
			try {
				//Request status update
				Message msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
				msg.replyTo = mMessenger;
				mService.send(msg);
//				Log.i("DrawBlood", "At doBind4.");
				//Request full log from service.
				msg = Message.obtain(null, eidService.MSG_UPDATE_LOG_FULL, 0, 0);
				mService.send(msg);
			} catch (RemoteException e) {}
		}
//		Log.i("Evaluate", "At doBind5.");
	}
	void doUnbindService() {
		Log.i("DrawBlood", "At DoUnbindservice");
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
		//	make the scan eid button red
		btn = (Button) findViewById( R.id.scan_eid_btn );
		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
//		String eid = this.getIntent().getExtras().getString("com.weyr_associates.lambtracker.LastEID");
//    	Log.i("DrawBlood", " before input text " + eid);  
//    	Log.i("DrawBlood", " before input text " + LastEID);  
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
        setContentView(R.layout.draw_blood);
//        Log.i("DrawBlood", " after set content view");
//        View v = null;
        String 	dbfile = getString(R.string.real_database_file) ;
//        Log.i("DrawBlood", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
//		Added the variable definitions here    	
      	String          cmd;
   	 //////////////////////////////////// 
//		CheckIfServiceIsRunning();
		LoadPreferences (true);
//		Log.i("DrawBlood", "back from isRunning");  	
		////////////////////////////////////    	
		thissheep_id = 0;
    	// Fill the Tag Type Spinner
    	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
	   	tag_types = new ArrayList<String>();      	
	   	
	   	// Select All fields from id types to build the spinner
	       cmd = "select * from id_type_table";
	       crsr = dbh.exec( cmd );  
	       cursor   = ( Cursor ) crsr;
	       dbh.moveToFirstRecord();
	   		tag_types.add("Select a Type");
	        // looping through all rows and adding to list
	   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	   		tag_types.add(cursor.getString(1));
	   	}
	   	
	   	// Creating adapter for spinner
	   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			tag_type_spinner.setAdapter (dataAdapter);
			tag_type_spinner.setSelection(2);	
		
			   	blood_tests = new ArrayList<String>();  
			   	blood_test_id = new ArrayList<Integer>();			   	
		    	//	Get the text for the blood draw types
			    cmd = String.format("select custom_evaluation_traits_table.id_custom_traitid, " +
			    		"custom_evaluation_traits_table.custom_evaluation_item " +
			    		" from custom_evaluation_traits_table " +
			    			" where custom_evaluation_traits_table.id_traitid = 26 "+
			    			" order by custom_evaluation_traits_table.custom_evaluation_order ASC ");
			    	Log.i("fill blood", " blood test text cmd is " + cmd);	    	
			    	crsr = dbh.exec( cmd );
			        cursor   = ( Cursor ) crsr;
			        nRecs4    = cursor.getCount();
			        Log.i ("getting tests", " we have " + String.valueOf(nRecs4) + " tests");
			        dbh.moveToFirstRecord();
			        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			        	blood_test_id.add(cursor.getInt(0));
			        	Log.i ("in for loop", " trait id is " + cursor.getInt(0));
			        	blood_tests.add (cursor.getString(1));
			        	Log.i ("in for loop", " trait name is " + cursor.getString(1));
			    	}

			    	LayoutInflater inflater = getLayoutInflater();	
		    	Log.i ("draw blood", blood_tests.get(0));
		    	for( int ii = 0; ii < nRecs4; ii++ ){	
		    		Log.i ("in for loop", " trait name is " + blood_tests.get(ii));
					TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);	
					Log.i("in for loop", " after TableLayout");
			    	TableRow row = (TableRow)inflater.inflate(R.layout.draw_blood_list_entry, table, false);	    	
			    	tempLabel = blood_tests.get(ii);
			    	Log.i("in for loop", " tempLabel is " + tempLabel);
			    	((TextView)row.findViewById(R.id.blood_test_label)).setText(tempLabel);
			    	Log.i("in for loop", " after set text view");
			    	table.addView(row);
		    	}					
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
			        	cmd = String.format( "select sheep_id, sheep_name from sheep_table where sheep_name like '%s'" +
			        			" and (remove_date is null or remove_date = '') "
			        			, tag_num );  
			        	Log.i("searchByName", "command is " + cmd);
			        	crsr = dbh.exec( cmd );
			    		cursor   = ( Cursor ) crsr; 
			        	recNo    = 1;
						nRecs5    = cursor.getCount();
						Log.i("searchByName", " nRecs5 = "+ String.valueOf(nRecs));
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
//		Object crsr, crsr2, crsr3, crsr4;
		TextView TV;
		
		thissheep_id = cursor.getInt(0);	        	
		Log.i("format record", "This sheep is record " + String.valueOf(thissheep_id));	        	
		
//		Log.i("format record", " recNo = "+ String.valueOf(recNo));
		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
				"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01,  " +
				"sheep_table.sire_id, sheep_table.dam_id, sheep_table.birth_date, birth_type_table.birth_type," +
				"sheep_sex_table.sex_name, sheep_table.birth_weight " +
				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
				"inner join birth_type_table on id_birthtypeid = sheep_table.birth_type " +
				"inner join sheep_sex_table on sheep_sex_table.sex_sheepid = sheep_table.sex " +
				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", thissheep_id);

		crsr= dbh.exec( cmd ); 	    		
		cursor   = ( Cursor ) crsr; 
		cursor.moveToFirst();				
		TV = (TextView) findViewById( R.id.sheepnameText );
	    TV.setText (dbh.getStr(0));
	    SheepName = dbh.getStr(0);
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
//	        Log.i("format record", " after second db lookup");
	        cursor2   = ( Cursor ) crsr2; 
			cursor2.moveToFirst();
			TV = (TextView) findViewById( R.id.sireName );
			thissire_name = dbh.getStr(0);
			TV.setText (thissire_name);	 
//			Log.i("format record", " Sire is " + thissire_name);
//	        Log.i("format record", " Sire is " + String.valueOf(thissire_id));
	    }
	    if(thisdam_id != 0){
	        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thisdam_id);
	        crsr3 = dbh.exec( cmd);
	        cursor3   = ( Cursor ) crsr3; 
			cursor3.moveToFirst();
			TV = (TextView) findViewById( R.id.damName );
			thisdam_name = dbh.getStr(0);
			TV.setText (thisdam_name);	
//			Log.i("format record", " Dam is " + thisdam_name);
//	        Log.i("format record", " Dam is " + String.valueOf(thisdam_id));
	    }  
    	try {
		Log.i("try block", " Before finding an electronic tag if it exists");		        		        	
    	cmd = String.format( "select sheep_table.sheep_id, id_type_table.idtype_name, " +
				"id_info_table.tag_number " +
				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null and " +
				"id_info_table.tag_type = 2 order by idtype_name asc", thissheep_id);
		crsr4 = dbh.exec( cmd ); 	    		
		cursor4   = ( Cursor ) crsr4; 		    	
		cursor4.moveToFirst();	
//		Log.i("getlastEID filled", "This sheep is id " + String.valueOf(dbh.getInt(0)));
//		Log.i("getlastEID filled", "This sheep id type is " + dbh.getStr(1));
		LastEID = dbh.getStr(2);
		Log.i("LastEID is ", dbh.getStr(2));
	}
	catch(Exception r)
    {
		LastEID = "000_000000000000";
		Log.v("fill LAST EID ", " in sheep management RunTimeException: " + r);
    }	
//    	Log.i("FormatRecord", " before formatting results");
		//	Get set up to try to use the CursorAdapter to display all the tag data
		//	Select only the columns I need for the tag display section
	    String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
		Log.i("FormatRecord", "after setting string array fromColumns");
		//	Set the views for each column for each line. A tag takes up 1 line on the screen
	    int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
	    Log.i("FormatRecord", "after setting string array toViews");
	    myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor ,fromColumns, toViews, 0);
	    Log.i("FormatRecord", "after setting myadapter");
	    setListAdapter(myadapter);
	    Log.i("FormatRecord", "after setting list adapter");

		// Now we need to check and see if there is an alert for this sheep
//	   	Log.i("Alert Text is " , alert_text);
//		Now to test of the sheep has an alert and if so then display the alert & set the alerts button to red
		if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
	       	// make the alert button red
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	//	testing whether I can put up an alert box here without issues
	    	showAlert(v);
		}
	}	
	 public void updateDatabase( View v ){
	    	
		 	TextView TV;
		 	String temp_string;
	    	Float trait11_data = 0.0f;
	    	
			// Disable Update Database button and make it red to prevent getting 2 records at one time
	    	btn = (Button) findViewById( R.id.update_database_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(false);
	    	// If there is no sheep ID then drop out completely
	    	// thissheep_id is 0 if no sheep has been selected.
	    	//	need to figure out how to loop around if it's 0 and do this stuff if not 0
	    	// TODO
	    	    	
    		//	Get the date and time to enter into the database.
    		String mytoday = Utilities.TodayIs();
    		String mytime = Utilities.TimeIs();
    		TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
    		Log.i("in draw blood", " number of types of tests is  " + String.valueOf(nRecs4)); 
//    		if (nRecs != 0) {
    			for( int ii = 0; ii < nRecs4; ii++ ){	
    				Log.i("in update db", " in 1st for loop ii is" + String.valueOf(ii)); 
    				TableRow row1= (TableRow)table.getChildAt(ii);
    				boxblood = (CheckBox) row1.getChildAt(0);   			
    				which_blood_test = String.valueOf (blood_tests.get(ii));
//    				which_blood_test = String.valueOf (row1.getChildAt(1));
    				Log.i("in which blood", " test is " + which_blood_test);
//    				//	Get the value of the checkbox for take  blood
    				if (boxblood.isChecked()){
    					//	go update the database with blood pull date and time as a note 
    					note_text = "Blood for ";
    					// Go get the reason for the blood test	
    					String temp_text = which_blood_test;
    					Log.i("blood test", " value is " + temp_text);			
    					note_text = note_text + temp_text;
    					predefined_note01 = 47; // hard coded the code for blood sample taken				
    					cmd = String.format("insert into sheep_note_table (sheep_id, note_text, note_date, note_time, id_predefinednotesid01) " +
    	    					"values ( %s, '%s', '%s', '%s', %s )", thissheep_id, note_text, mytoday, mytime, predefined_note01);
    	    			Log.i("update notes ", "before cmd " + cmd);
    	    			dbh.exec( cmd );	
    	    			Log.i("update notes ", "after cmd exec");
    					Log.i("blood taken ", String.valueOf(boxblood));
    					try {
    					//	Update the sheep record to remove the Scrapie blood in alert
    					cmd = String.format("update sheep_table set alert01 = replace " +
    							"( alert01, 'Scrapie Blood', '') where sheep_id =%d ", thissheep_id ) ;
//    					Log.i("update alerts ", "before cmd " + cmd);
    					dbh.exec( cmd );
//    					Log.i("update alerts ", "after cmd " + cmd);
    					}
    					catch (Exception e){
    						Log.w("scrapie", "No scrapie alert");
    					}
    					try {
    						//	Update the sheep record to remove the Brucellosis blood in alert
    						cmd = String.format("update sheep_table set alert01 = replace " +
    								"( alert01, 'Brucellosis Blood', '') where sheep_id =%d ", thissheep_id ) ;
//    						Log.i("update alerts ", "before cmd " + cmd);
    						dbh.exec( cmd );
//    						Log.i("update alerts ", "after cmd " + cmd);
    						}
    						catch (Exception e){
    							Log.w("scrapie", "No Brucellosis alert");
    						}
    					try {
    						//	Update the sheep record to remove the OPP blood in alert
    						cmd = String.format("update sheep_table set alert01 = replace " +
    								"( alert01, 'OPP Blood', '') where sheep_id =%d ", thissheep_id ) ;
//    						Log.i("update alerts ", "before cmd " + cmd);
    						dbh.exec( cmd );
//    						Log.i("update alerts ", "after cmd " + cmd);
    						}
    						catch (Exception e){
    							Log.w("scrapie", "No OPP alert");
    						}
    				}			
    			}	
			clearBtn( null );
	 }
	public void printLabel( View v ){ 
		try
	    {
		String[] lines = EID.split("\n"); // works for both
	    String contents = LastEID.substring(0, 3) + LastEID.substring(4, 16);
	    Log.i("PrintLabel btn ", " contents " + contents);		    		
		Intent encodeIntent = new Intent("weyr.LT.ENCODE");
		encodeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		encodeIntent.addCategory(Intent.CATEGORY_DEFAULT); 
		encodeIntent.putExtra("ENCODE_FORMAT", "CODE_128");
		encodeIntent.putExtra("ENCODE_SHOW_CONTENTS", false);
		encodeIntent.putExtra("ENCODE_DATA", contents);
		encodeIntent.putExtra("ENCODE_AUTOPRINT", "false");

		if (AutoPrint) {
			encodeIntent.putExtra("ENCODE_AUTOPRINT", "true");
			Log.i("PrintLabel btn ", " autoprint is true ");	
	     };
		
		encodeIntent.putExtra("ENCODE_DATA1", LabelText);						
		encodeIntent.putExtra("ENCODE_DATE", Utilities.TodayIs() + "  " + Utilities.TimeIs());
		Log.i("PrintLabel btn ", " before put extra sheepName ");
		encodeIntent.putExtra("ENCODE_SHEEPNAME", SheepName);
		Log.i("PrintLabel btn ", " after put extra sheepName " + SheepName);
	    startActivity(encodeIntent);
	    Log.i("PrintLabel btn ", " after start activity encode " );
	    }
	    catch(Exception r)
	    {
	        Log.v("PrintLabel ", " in sheep management RunTimeException: " + r);
	    }			
	}
	   
	    public void backBtn( View v )
	    {
    	doUnbindService();
		stopService(new Intent(DrawBlood.this, eidService.class));   	
    	// Added this to close the database if we go back to the main activity  	
    	try {
    		cursor.close();
    	}catch (Exception r)
    	{
    		Log.i("back btn", "cursor RunTimeException: " + r);
    	}
    	try {
    		cursor2.close();
    	}catch (Exception r)
    	{
    		Log.i("back btn", "cursor2 RunTimeException: " + r);
    	}
    	try {
    		cursor3.close();
    	}catch (Exception r)
    	{
    		Log.i("back btn", " cursor3 RunTimeException: " + r);
    	}
    	dbh.closeDB();
    	clearBtn( null );
    	//Go back to main
      	finish();
	    }

	    public void showAlert(View v)
	{
		String	alert_text;
		String 			dbname = getString(R.string.real_database_file); 
      String          cmd;    
      Object 			crsr;
		// Display alerts here   	
				AlertDialog.Builder builder = new AlertDialog.Builder( this );
				cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_id =%d", thissheep_id);
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
	}
	
	public void clearBtn( View v )
    {
		TextView TV ;
		TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
		TV = (TextView) findViewById( R.id.sireName );
		TV.setText( "" );
		TV = (TextView) findViewById( R.id.damName );
		TV.setText( "" );
		//	Need to clear out the rest of the tags here 
		Log.i("clear btn", "before changing myadapter");
		try {
			myadapter.changeCursor(null);
		}
		catch (Exception e) {
			// In this case there is no adapter so do nothing
		}
//		Log.i("clear btn", "after changing myadapter");
		try {
			TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
//			Log.i("in clear button", " number rating bars is " + String.valueOf(nRecs)); 
			if (nRecs4 != 0) {
				for( int ii = 0; ii < nRecs4; ii++ ){	
//					Log.i("in clear button", " in 1st for loop ii is" + String.valueOf(ii)); 
					TableRow row1= (TableRow)table.getChildAt(ii);
					boxblood = (CheckBox) row1.getChildAt(0);
					boxblood.setChecked(false);
				}
			}
		}catch (Exception e){
			//	something failed so log it
			Log.i("in clear button", " in catch of try clearing blood checkboxes " );
		}

		// Enable Update Database button and make it normal
    	btn = (Button) findViewById( R.id.update_database_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF000000));
    	btn.setEnabled(true);
    	
      	// make the alert button normal and disabled
	   	btn = (Button) findViewById( R.id.alert_btn );
	   	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
	   	btn.setEnabled(false);    	
    }
	@Override
	public void onResume (){	
		super.onResume();
		CheckIfServiceIsRunning();
		Log.i("DrawBlood", " OnResume");
		scanEid( null );	
//		Log.i("DrawBlood", " OnResume after scanEID(null)");
	}

	@Override
	public void onPause (){	
		super.onPause();
		Log.i("DrawBlood", " OnPause");
		doUnbindService();
	}

	public void helpBtn( View v )
  {
 	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_management )
	           .setTitle( R.string.help_warning );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int idx) {
	               // User clicked OK button 
	        	  
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();
  }
 
	public boolean tableExists (String table){
		try {
	        dbh.exec("select * from "+ table);   
	        return true;
		} catch (SQLiteException e) {
			return false;
	        		}
	        	}

		   public void doNote( View v )
		   {	 
			   Utilities.takeNote(v, thissheep_id, this);
		   }
			//  user clicked 'Scan' button    
	public void scanEid( View v){
			 	// Here is where I need to get a tag scanned and put the data into the variable LastEID
				 clearBtn( v );
				 tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
				 tag_type_spinner.setSelection(2);
//				 Log.i("in ScanEID", " after set tag_type_spinner ");
				 if (mService != null) {
					try {
						//Start eidService sending tags
						Message msg = Message.obtain(null, eidService.MSG_SEND_ME_TAGS);
						msg.replyTo = mMessenger;
						mService.send(msg);
					   	//	make the scan eid button  0x0000FF00, 0xff00ff00
				    	Button btn = (Button) findViewById( R.id.scan_eid_btn );
				    	btn.getBackground().setColorFilter(new LightingColorFilter(0x0000FF00, 0xff00ff00));
						
					} catch (RemoteException e) {
						// In this case the service has crashed before we could even do anything with it
					}
					} else{ 
						Log.i("in ScanEID", " mService is null " );
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
			    	thissheep_id = cursor5.getInt(0);
			    	Log.i("in next record", "this sheep ID is " + String.valueOf(thissheep_id));
			    	recNo         += 1;
			    	formatSheepRecord(v);
//		    		// I've moved forward so I need to enable the previous record button
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
//			    	Clear out the display first
			    	clearBtn( v );
			    	Log.i("in prev record", "this sheep ID is " + String.valueOf(thissheep_id));
			    	cursor.moveToPrevious();
			    	Log.i("in prev record", "after moving the cursor5 ");
			    	thissheep_id = cursor5.getInt(0);
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
