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
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class SheepManagement extends ListActivity {
	private DatabaseHandler dbh;
	int             id;
	String 			logmessages;
	public int 		thissheep_id, thissire_id, thisdam_id;
	public int		codon171, codon154, codon136;
	int             fedtagid, farmtagid, eidtagid;
	
	public String 	tag_type_label, tag_color_label, tag_location_label, eid_tag_color_label ;
	public String 	eid_tag_location_label, eidText, alert_text;
	public String 	thissire_name, thisdam_name;

	public Cursor 	cursor, cursor2, cursor3, cursor4, cursor5;
	public Object 	crsr, crsr2, crsr3, crsr4, crsr5;
	public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner ;
	public Spinner predefined_note_spinner;
	public Spinner wormer_spinner, vaccine_spinner;
	public List<String> predefined_notes;
	public List<String> tag_types, tag_locations, tag_colors;
	public List<String> wormers, vaccines;
	public List<Integer> wormer_id_drugid, vaccine_id_drugid;
	public int wormer_id, vaccine_id, drug_loc;
	public String[] this_sheeps_tags ;
	public int drug_gone; // 0 = false 1 = true
	public int	drug_type, which_wormer, which_vaccine;
	public RadioGroup radioGroup;
	public String mytoday, mytime;
	public CheckBox 	boxtrimtoes, boxwormer, boxvaccine;
	private int             nRecs;
	private int			    recNo;
	private String[]        colNames;
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
				stopService(new Intent(SheepManagement.this, eidService.class));
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	private void LoadPreferences(Boolean NotifyOfChanges) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		Log.i("SheepManagement", "Load Pref.");
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
			startService(new Intent(SheepManagement.this, eidService.class));
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
		//	make the scan eid button red
		btn = (Button) findViewById( R.id.scan_eid_btn );
		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
//		String eid = this.getIntent().getExtras().getString("com.weyr_associates.lambtracker.LastEID");
//    	Log.i("LookUpSheep", " before input text " + eid);  
//    	Log.i("LookUpSheep", " before input text " + LastEID);  
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
        setContentView(R.layout.sheep_management);
        Log.i("LookUpSheep", " after set content view");
        View v = null;
        String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("LookUpSheep", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
//		Added the variable definitions here    	
      	String          cmd;
      	mytoday = TodayIs(); 
		mytime = TimeIs();
		ArrayList radiobtnlist;
    	String[] radioBtnText;
   	 //////////////////////////////////// 
		CheckIfServiceIsRunning();
		Log.i("SheepMgmt", "back from isRunning");  	
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
		
			Log.i("SheepMgmt", " before filling drug spinners"); 
		//	Get set up to fill drug spinners
			drug_gone = 0;
// TODO			
		// Fill the Wormer Spinner
	    	wormer_spinner = (Spinner) findViewById(R.id.wormer_spinner);
		   	wormers = new ArrayList<String>();  
		   	wormer_id_drugid = new ArrayList<Integer>();
		   	drug_type = 1;
		   	// Select All fields from id types to build the spinner
		   	cmd = String.format( "select id_drugid, user_task_name, drug_lot from drug_table where " +
		   			"drug_gone = %s and drug_type = %s", drug_gone , drug_type);
		   	crsr = dbh.exec( cmd );  
		   	cursor   = ( Cursor ) crsr;
	   	  	dbh.moveToFirstRecord();
	   	  	wormers.add("Select a Dewormer");
	   	  	wormer_id_drugid.add(0);
		        // looping through all rows and adding to list
		   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
		   		wormer_id_drugid.add(cursor.getInt(0));
		   		wormers.add(cursor.getString(1) + " lot " + cursor.getString(2));
		   	}
//		   	cursor.close();    	
		   	Log.i("SheepMgmt", " after filling wormer spinner"); 
		   	// Creating adapter for spinner
		   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, wormers);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				wormer_spinner.setAdapter (dataAdapter);
				wormer_spinner.setSelection(1);	
							
			// Fill the Vaccine Spinner
	    	vaccine_spinner = (Spinner) findViewById(R.id.vaccine_spinner);
		   	vaccines = new ArrayList<String>(); 
		   	vaccine_id_drugid = new ArrayList<Integer>();
		   	drug_type = 2;
		   	// Select All fields from id types to build the spinner
		   	cmd = String.format( "select drug_table.id_drugid, drug_table.user_task_name, drug_table.drug_lot " +
		   			"from drug_table where drug_gone = %s and drug_type = %s", drug_gone , drug_type);
		   	crsr = dbh.exec( cmd );  
		   	cursor   = ( Cursor ) crsr;
	   	  	dbh.moveToFirstRecord();
	   	  	vaccines.add("Select a Vaccine");
	   	  	vaccine_id_drugid.add(0);
		        // looping through all rows and adding to list
		   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
		   		vaccine_id_drugid.add(cursor.getInt(0));
		   		vaccines.add(cursor.getString(1) + " lot " + cursor.getString(2));
		   	}
		   	// Creating adapter for spinner
		   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, vaccines);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				vaccine_spinner.setAdapter (dataAdapter);
				vaccine_spinner.setSelection(1);	
			// 	Create the radio buttons for the shot locations here	
				radiobtnlist = new ArrayList();
//				radiobtnlist.add ("Select Vaccine Location");
				radiobtnlist.add ("SQ RS");
				radiobtnlist.add ("SQ LS");
				radiobtnlist.add ("IM N");
//			   	cmd = "select * from drug_location_table";
//			   	crsr = dbh.exec( cmd );  
//				cursor   = ( Cursor ) crsr;
//				nRecs    = cursor.getCount();
//			   	dbh.moveToFirstRecord();
//			    // looping through all rows and adding to list
//			   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
//			   		radiobtnlist.add(cursor.getString(2));
//			   	}
			    radioBtnText = (String[]) radiobtnlist.toArray(new String [radiobtnlist.size()]);
				// Build the radio buttons here
				radioGroup = ((RadioGroup) findViewById(R.id.radioShotLoc));
				addRadioButtons(3, radioBtnText);
				radiobtnlist.clear ();
			
//		TextView TV = (TextView) findViewById( R.id.inputText );	
		
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
   	
        TV = (TextView) findViewById( R.id.inputText );	        
    	String	tag_num = TV.getText().toString();
   	
//    	Log.i("LookForSheep", " got to lookForSheep with Tag Number of " + tag_num);
        exists = tableExists("sheep_table");
        if (exists){
        	if( tag_num != null && tag_num.length() > 0 ){
//        		Get the sheep id from the id table for this tag number and selected tag type
	        	cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
	        			"and id_info_table.tag_type='%s' ", tag_num , tag_type_spinner.getSelectedItemPosition());  	        	
	        	dbh.exec( cmd );
	        	dbh.moveToFirstRecord();
	        	if( dbh.getSize() == 0 )
		    		{ // no sheep with that tag in the database so clear out and return
		    		clearBtn( v );
		    		TV = (TextView) findViewById( R.id.sheepnameText );
		        	TV.setText( "Cannot find this sheep." );
		        	return;
		    		}
	        	thissheep_id = dbh.getInt(0);
	        	Log.i("LookForSheep", "This sheep is record " + String.valueOf(thissheep_id));
	        	//	Go get the sex of this sheep
	        	cmd = String.format( "select sheep_table.sex from sheep_table where sheep_id = %s",thissheep_id);
	        	crsr = dbh.exec( cmd ); 	    		
	    		cursor   = ( Cursor ) crsr; 
	    		startManagingCursor(cursor);
				cursor.moveToFirst();				
				i =  (dbh.getInt(0));
				Log.i("LookForSheep", "This sheep is sex " + String.valueOf(i));	
				
	        	Log.i("LookForSheep", " Before finding all tags");		        	
	        	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
	    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
	    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01,  " +
	    				"sheep_table.sire_id, sheep_table.dam_id " +
	    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
	    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
	    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
	    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
	    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", thissheep_id);

	    		crsr = dbh.exec( cmd ); 	    		
	    		cursor   = ( Cursor ) crsr; 
	    		startManagingCursor(cursor);
	    		recNo    = 1;
				nRecs    = cursor.getCount();
				colNames = cursor.getColumnNames();
				cursor.moveToFirst();				
				TV = (TextView) findViewById( R.id.sheepnameText );
		        TV.setText (dbh.getStr(0));
		        alert_text = dbh.getStr(8);
		    	
		        //	Get the sire and dam id numbers
		        thissire_id = dbh.getInt(9);
//		        Log.i("LookForSheep", " Sire is " + String.valueOf(thissire_id));
		        thisdam_id = dbh.getInt(10);
//		        Log.i("LookForSheep", " Dam is " + String.valueOf(thisdam_id));
		        
		        //	Go get the sire name
		        if (thissire_id != 0){
			        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thissire_id);
//			        Log.i("LookForSheep", " cmd is " + cmd);		        
			        crsr2 = dbh.exec( cmd);
			        Log.i("LookForSheep", " after second db lookup");
			        cursor2   = ( Cursor ) crsr2; 
		    		startManagingCursor(cursor2);
		    		cursor2.moveToFirst();
		    		TV = (TextView) findViewById( R.id.sireName );
		    		thissire_name = dbh.getStr(0);
		    		TV.setText (thissire_name);	 
		    		Log.i("lookForSheep", " Sire is " + thissire_name);
			        Log.i("LookForSheep", " Sire is " + String.valueOf(thissire_id));
		        }
		        if(thisdam_id != 0){
			        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thisdam_id);
			        crsr3 = dbh.exec( cmd);
			        cursor3   = ( Cursor ) crsr3; 
		    		startManagingCursor(cursor3);
		    		cursor3.moveToFirst();
		    		TV = (TextView) findViewById( R.id.damName );
		    		thisdam_name = dbh.getStr(0);
		    		TV.setText (thisdam_name);	
		    		Log.i("lookForSheep", " Dam is " + thisdam_name);
			        Log.i("LookForSheep", " Dam is " + String.valueOf(thisdam_id));
		        }    		
		        
		    	Log.i("lookForSheep", " before formatting results");
				
				//	Get set up to try to use the CursorAdapter to display all the tag data
				//	Select only the columns I need for the tag display section
		        String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
				//	Set the views for each column for each line. A tag takes up 1 line on the screen
		        int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
				myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor ,fromColumns, toViews, 0);
				setListAdapter(myadapter);
				
				Log.i("lookForSheep", " after filling tag data");

				//	Now to test of the sheep has an alert and if so then display the alert
				if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
			       	// make the alert button red
			    	Button btn = (Button) findViewById( R.id.alert_btn );
			    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
			    	btn.setEnabled(true); 
			    	//	testing whether I can put up an alert box here without issues
			    	showAlert(v);
				}
				}else{
					return;
				}
//	        Log.i("lookForSheep", " out of the if statement");
        	}
    		else {
    			clearBtn( null );
            	TV = (TextView) findViewById( R.id.sheepnameText );
                TV.setText( "Sheep Database does not exist." ); 
        	}
	}	
	
	 public void updateDatabase( View v ){
	    	
			// Disable Update Database button and make it red to prevent getting 2 records at one time
	    	btn = (Button) findViewById( R.id.update_database_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(false);
			//	Get the value of the checkbox for trim toes
			Log.i("before checkbox", " getting ready to get trim toes or not ");
			boxtrimtoes = (CheckBox) findViewById(R.id.checkBoxTrimToes);
			if (boxtrimtoes.isChecked()){
				//	go update the database with a toe trimming date and time
				Log.i("toes trimmed ", String.valueOf(boxtrimtoes));				
			}			
// TODO
			//	Need to figure out the id_drugid for what we are giving this sheep
			boxwormer = (CheckBox) findViewById(R.id.checkBoxGiveWormer);
			if (boxwormer.isChecked()){
				//	Go get which wormer was selected in the spinner
				wormer_spinner = (Spinner) findViewById(R.id.wormer_spinner);
				which_wormer = wormer_spinner.getSelectedItemPosition();
				Log.i("wormer spinner", " position is" + String.valueOf(which_wormer));
				//	go update the database with a drug record for this wormer and this sheep
				// The drug_id is at the same position in the wormer_id_drugid list as the spinner position			
				i = wormer_id_drugid.get(which_wormer);
				Log.i("wormer id", " value is " + String.valueOf(i));
				//	Drug location 5 is by mouth
				cmd = String.format("insert into sheep_drug_table (sheep_id, drug_id, drug_date_on," +
		  				" drug_time_on, drug_location) values " +
		  				" (%s, '%s', '%s', '%s' , %s) ", thissheep_id, i, mytoday, mytime, 5);
		  		Log.i("add drug to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into sheep_drug_table");							
			}	
		
			boxvaccine = (CheckBox) findViewById(R.id.checkBoxGiveVaccine);
			if (boxvaccine.isChecked()){
				//	Go get which vaccine was selected in the spinner
				vaccine_spinner = (Spinner) findViewById(R.id.vaccine_spinner);
		    	which_vaccine = vaccine_spinner.getSelectedItemPosition();
				//	go update the database with a drug record for this vaccine and this sheep
		    	Log.i("vaccine spinner", " position is" + String.valueOf(which_vaccine));
				//	go update the database with a drug record for this wormer and this sheep
				// The drug_id is at the same position in the wormer_id_drugid list as the spinner position			
				i = vaccine_id_drugid.get(which_vaccine);
				Log.i("vaccine id", " value is " + String.valueOf(i));
				// Go get drug location for the shot
				//	Need to set up a radio button for the locations and read it
//				Get the radio group selected for the location
				Log.i("before radio group", " getting ready to get the shot location ");
				radioGroup=(RadioGroup)findViewById(R.id.radioShotLoc);
		 		drug_loc = radioGroup.getCheckedRadioButtonId()+1;				
				cmd = String.format("insert into sheep_drug_table (sheep_id, drug_id, drug_date_on," +
		  				" drug_time_on, drug_location) values " +
		  				" (%s, '%s', '%s', '%s' , %s) ", thissheep_id, i, mytoday, mytime, drug_loc);
		  		Log.i("add drug to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into sheep_drug_table");						
			}	
			
	 }
	public void printLabel( View v ){ 

		// Ken add the printing code here
		String[] lines = EID.split("\n"); // works for both
		
	    String contents = LastEID.substring(0, 3) + LastEID.substring(4, 16);
	   	
	    try
	    {					    		
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
		encodeIntent.putExtra("ENCODE_DATE", TodayIs() + "  " + TimeIs());
		encodeIntent.putExtra("ENCODE_SHEEPNAME", SheepName);
	    startActivity(encodeIntent);
		
	    }
	    catch(Exception r)
	    {
	        Log.v("EIDService", "RunTimeException: " + r);
	    }			
	}
	   
	    public void backBtn( View v )
	    {
    	doUnbindService();
		stopService(new Intent(SheepManagement.this, eidService.class));   	
    	// Added this to close the database if we go back to the main activity  	
    	stopManagingCursor (cursor);
    	cursor.close();
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
//				cursor.close();
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
		Log.i("clear btn", "after changing myadapter");
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
    public void takeNote( View v )
    {	    	
    	final Context context = this;
		//	First fill the predefined note spinner with possibilities
    	predefined_notes = new ArrayList<String>();
		predefined_notes.add("Select a Predefined Note");
//		Log.i ("takeNote", " after adding Select a Predefined Note");
    	// Select All fields from predefined_notes_table to build the spinner
        cmd = "select * from predefined_notes_table";
//        Log.i ("takeNote", " cmd is " + cmd);
        crsr = dbh.exec( cmd );  
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
         // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		predefined_notes.add(cursor.getString(1));
//    		Log.i ("takeNote", " in for loop predefined note id is " + String.valueOf(cursor.getString(1)));
    	}
    	cursor.close();    
    	Log.i ("takeNote", " after set the predefined note spinner ");
    	Log.i ("takeNote", " this sheep is " + String.valueOf(thissheep_id));
    	//Implement take a note stuff here
    	if (thissheep_id == 0) {
    		Log.i ("takeNote", " no sheep selected " + String.valueOf(thissheep_id));
    	}
    	else {
//    		Log.i ("takeNote", " got a sheep, need to get a note to add");
    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//    		Log.i ("takeNote", " after getting new alertdialogbuilder");
    		
    		LayoutInflater li = LayoutInflater.from(context);
			View promptsView = li.inflate(R.layout.note_prompt, null);
//			Log.i ("takeNote", " after inflating layout");	

			// set view note_prompt to alertdialog builder
			alertDialogBuilder.setView(promptsView);
			Log.i ("takeNote", " after setting view");
		   	// Creating adapter for spinner
	    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, predefined_notes);
//	    	Log.i ("takeNote", " after create new array adapter for the spinner ");
	    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//	    	Log.i ("takeNote", " after set dropdown resource for the spinner ");
	    	predefined_note_spinner = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner);
//	    	Log.i ("takeNote", " after set promptsView for the spinner ");
	    	predefined_note_spinner.setAdapter (dataAdapter);
//			Log.i ("takeNote", " after set the adapter for the spinner ");
			predefined_note_spinner.setSelection(0);
//			Log.i ("takeNote", " after set spinner to location 0");
			
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
					//	Get id_predefinednotesid from a spinner here 
					int predefined_note = predefined_note_spinner.getSelectedItemPosition();
					// Update the notes table with the data
					cmd = String.format("insert into note_table (sheep_id, note_text, note_date, note_time, id_predefinednotesid) " +
	    					"values ( %s, '%s', '%s', '%s', %s )", thissheep_id, note_text, TodayIs(), TimeIs(), predefined_note);
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
	public boolean tableExists (String table){
		try {
	        dbh.exec("select * from "+ table);   
	        return true;
		} catch (SQLiteException e) {
			return false;
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
		   private String TimeIs() {
				Calendar calendar = Calendar.getInstance();
		        //12 hour format
//						int hour = cal.get(Calendar.HOUR);
		        //24 hour format
				int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				int second = calendar.get(Calendar.SECOND);
				  
				return Make2Digits(hourofday) + ":" + Make2Digits(minute) + ":" + Make2Digits(second) ;
			}
		   private void addRadioButtons(int numButtons, String[] radioBtnText) {
			  	  int i;

			  	  for(i = 0; i < numButtons; i++){
			  	    //instantiate...
			  	    RadioButton radioBtn = new RadioButton(this);

			  	    //set the values that you would otherwise hardcode in the xml...
			  	  	radioBtn.setLayoutParams 
			  	      (new LayoutParams 
			  	      (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			  	    //label the button...
			  	  	radioBtn.setText(radioBtnText[i]);
//			  	  	Log.i("addradiobuttons", radioBtnText[i]);
			  	  	radioBtn.setId(i);

			  	    //add it to the group.
			  	    radioGroup.addView(radioBtn, i);
			  	  }
			  	}   
}
