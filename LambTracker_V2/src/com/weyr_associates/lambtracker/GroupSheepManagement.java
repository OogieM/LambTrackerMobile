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
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.google.zxing.client.android.Intents;

public class GroupSheepManagement extends ListActivity {
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
	public Spinner predefined_note_spinner01, predefined_note_spinner02, predefined_note_spinner03;
	public Spinner predefined_note_spinner04, predefined_note_spinner05;
	public Spinner wormer_spinner, vaccine_spinner, drug_spinner, drug_location_spinner, blood_spinner;
	public List<String> predefined_notes;
	public List<String> tag_types, tag_locations, tag_colors;
	public List<String> wormers, vaccines, drugs, drug_location, blood_tests;
	public List<Integer> wormer_id_drugid, vaccine_id_drugid, drug_id_drugid, blood_test_id;
	public int wormer_id, vaccine_id, shot_loc, drug_loc;
	public String[] this_sheeps_tags ;
	public int drug_gone; // 0 = false 1 = true
	public int	drug_type, which_wormer, which_vaccine, which_drug, which_blood, id_sheepdrugid;
	public RadioGroup radioGroup;
	public CheckBox 	boxtrimtoes, boxwormer, boxvaccine, boxweight, boxblood, boxdrug, boxweaned, boxshear, boxremovedrug;
	public String note_text, empty_string_field;
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
				stopService(new Intent(GroupSheepManagement.this, eidService.class));
				
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
//			Log.i("SheepMgmt", "At Service.");
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
//		Log.i("SheepMgmt", "At isRunning?.");
		if (eidService.isRunning()) {
//			Log.i("SheepMgmt", "is.");
			doBindService();
		} else {
//			Log.i("SheepMgmt", "is not, start it");
			startService(new Intent(GroupSheepManagement.this, eidService.class));
			doBindService();
		}
//		Log.i("SheepMgmt", "Done isRunning.");
	} 	
	
	void doBindService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
//		Log.i("SheepMgmt", "At doBind1.");
		bindService(new Intent(this, eidService.class), mConnection, Context.BIND_AUTO_CREATE);
//		Log.i("SheepMgmt", "At doBind2.");

		mIsBound = true;

		if (mService != null) {
//			Log.i("SheepMgmt", "At doBind3.");
			try {
				//Request status update
				Message msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
				msg.replyTo = mMessenger;
				mService.send(msg);
//				Log.i("SheepMgmt", "At doBind4.");
				//Request full log from service.
				msg = Message.obtain(null, eidService.MSG_UPDATE_LOG_FULL, 0, 0);
				mService.send(msg);
			} catch (RemoteException e) {}
		}
//		Log.i("Evaluate", "At doBind5.");
	}
	void doUnbindService() {
		Log.i("SheepMgmt", "At DoUnbindservice");
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
//    	Log.i("SheepMgmt", " before input text " + eid);  
//    	Log.i("SheepMgmt", " before input text " + LastEID);  
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
        Log.i("SheepMgmt", " before set content view");

        setContentView(R.layout.group_sheep_management);
        Log.i("SheepMgmt", " after set content view");
//        View v = null;
        String 	dbfile = getString(R.string.real_database_file) ;
//        Log.i("SheepMgmt", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
//		Added the variable definitions here    	
      	String          cmd;
		ArrayList radiobtnlist;
    	String[] radioBtnText;
   	 //////////////////////////////////// 
//		CheckIfServiceIsRunning();
		LoadPreferences (true);
//		Log.i("SheepMgmt", "back from isRunning");  	
		////////////////////////////////////    	
		thissheep_id = 0;
		empty_string_field = "";
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
// TODO		Set this up to look at the default item and put that as the one selected. 	
		// Fill the Wormer Spinner
	    	wormer_spinner = (Spinner) findViewById(R.id.wormer_spinner);
		   	wormers = new ArrayList<String>();  
		   	wormer_id_drugid = new ArrayList<Integer>();
		   	drug_type = 1;
		   	// Select All fields from id types to build the spinner
		   	cmd = String.format( "select id_drugid, user_task_name, drug_lot from drug_table where " +
		   			"drug_gone = %s and (drug_type = 1 or drug_type = 5) ", drug_gone);
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
		   	Log.i("SheepMgmt", " after filling wormer spinner"); 
		   	// Creating adapter for spinner
		   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, wormers);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				wormer_spinner.setAdapter (dataAdapter);
				//	Set the wormer to be a specific one 
				//	Should be a preference or default but fixed to be Ivermectin for now
				//  However, we need to make sure there is a wormer to do so!
				if (cursor.getCount() > 0){ wormer_spinner.setSelection(0);	}

				// Fill the Blood Sample Spinner
		    	blood_spinner = (Spinner) findViewById(R.id.blood_spinner);
			   	blood_tests = new ArrayList<String>();  
			   	blood_test_id = new ArrayList<Integer>();			   	
			   	// Select All fields from blood test types to build the spinner			   	
			   	Log.i("SheepMgmt", " before filling blood test spinner"); 
		    	cmd = String.format("select evaluation_trait_table.trait_name, evaluation_trait_table.id_traitid, " +
			        	"custom_evaluation_name_table.custom_eval_number " +
		    			"from evaluation_trait_table " +
			        	" inner join custom_evaluation_name_table on evaluation_trait_table.id_traitid = " +
		        		" custom_evaluation_name_table.id_traitid where evaluation_trait_table.id_traitid = 26 ") ;
//		    	Log.i("evaluate2", " cmd is " + cmd);
		    	crsr = dbh.exec( cmd );
		        cursor   = ( Cursor ) crsr;		       
		    	dbh.moveToFirstRecord();
//		    	Integer num_blood_tests = (cursor.getInt(2));
		    	Log.i("SheepMgmt", " after filling blood tests spinner"); 
		    	//	Get the text for the spinner
			    cmd = String.format("select custom_evaluation_traits_table.id_traitid, " +
			    		"custom_evaluation_traits_table.custom_evaluation_item " +
			    		" from custom_evaluation_traits_table " +
			    			" where custom_evaluation_traits_table.id_traitid = 26 "+
			    			" order by custom_evaluation_traits_table.custom_evaluation_order ASC ");
			    	Log.i("fill blood", " ready to get spinner text cmd is " + cmd);	    	
			    	crsr = dbh.exec( cmd );
			        cursor   = ( Cursor ) crsr;
			        nRecs4    = cursor.getCount();
			        Log.i ("getting tests", " we have " + String.valueOf(nRecs4) + " tests");
			        dbh.moveToFirstRecord();
			        blood_tests.add("Select a Blood Test");
			        blood_test_id.add(0);
			        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			        	blood_test_id.add(cursor.getInt(0));
			        	blood_tests.add (cursor.getString(1));
			    	}
					   	
			   	// Creating adapter for spinner
			   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, blood_tests);
					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					blood_spinner.setAdapter (dataAdapter);
					//	Set the blood test to be a specific one 
					//	Should be a preference or default but set to Scrapie DNA Sample for now
					//  However, we need to make sure there is a wormer to do so!
					if (cursor.getCount() > 0){ blood_spinner.setSelection(0);	}
					
			// Fill the Vaccine Spinner
	    	vaccine_spinner = (Spinner) findViewById(R.id.vaccine_spinner);
		   	vaccines = new ArrayList<String>(); 
		   	vaccine_id_drugid = new ArrayList<Integer>();
		   	drug_type = 2;
//		   	Log.i("SheepMgmt", " before filling vaccine spinner"); 
		   	// Select All fields from id types to build the spinner
		   	cmd = String.format( "select drug_table.id_drugid, drug_table.user_task_name, drug_table.drug_lot " +
		   			"from drug_table where drug_gone = %s and drug_type = %s", drug_gone , drug_type);
//		   	Log.i("SheepMgmt", " command is " + cmd);
		   	crsr = dbh.exec( cmd );  
		   	cursor   = ( Cursor ) crsr;
	   	  	dbh.moveToFirstRecord();
	   	  	vaccines.add("Select a Vaccine");
	   	  	vaccine_id_drugid.add(0);
		        // looping through all rows and adding to list
		   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
		   		vaccine_id_drugid.add(cursor.getInt(0));
		   		vaccines.add(cursor.getString(1) + " lot " + cursor.getString(2));
//		   		Log.i("SheepMgmt", " for loop vaccine to add is " + cursor.getString(1) + " lot " + cursor.getString(2));
		   	}
		   	// Creating adapter for spinner
		   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, vaccines);
				dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				vaccine_spinner.setAdapter (dataAdapter);
				//	Set the vaccine to use to be the first one  Should come from the defaults
				if (cursor.getCount() > 0){ vaccine_spinner.setSelection(0);	}
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
				//	Default to right side SQ location
				((RadioButton)radioGroup.getChildAt(0)).setChecked(true);
				// radiobtnlist.clear ();
			
				// Fill the Drug Spinner
		    	drug_spinner = (Spinner) findViewById(R.id.drug_spinner);
			   	drugs = new ArrayList<String>();  
			   	drug_id_drugid = new ArrayList<Integer>();
			   	
			   	// Select All fields from drug types to build the spinner
			   	cmd = String.format( "select id_drugid, user_task_name, drug_lot from drug_table where " +
			   			"drug_gone = %s and (drug_type = 3 or drug_type = 4 or " +
			   			"drug_type = 5 or drug_type = 6) ", drug_gone );
			   	crsr = dbh.exec( cmd );  
			   	cursor   = ( Cursor ) crsr;
		   	  	dbh.moveToFirstRecord();
		   	  	drugs.add("Select a Drug");
		   	  	drug_id_drugid.add(0);
			        // looping through all rows and adding to list
			   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			   		drug_id_drugid.add(cursor.getInt(0));
			   		drugs.add(cursor.getString(1) + " lot " + cursor.getString(2));
			   	}
//			   	Log.i("SheepMgmt", " after filling drug spinner"); 
			   	// Creating adapter for spinner
			   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, drugs);
					dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					drug_spinner.setAdapter (dataAdapter);
					//	Set the drug to select a drug
					drug_spinner.setSelection(0);		
					
				// Fill the drug location Spinner
			    	drug_location_spinner = (Spinner) findViewById(R.id.drug_location_spinner);
				   	drug_location = new ArrayList<String>();      	
				   	
				   	// Select All fields from drug locations to build the spinner
				       cmd = "select * from drug_location_table";
				       crsr = dbh.exec( cmd );  
				       cursor   = ( Cursor ) crsr;
				       dbh.moveToFirstRecord();
				       drug_location.add("Select a Location");
				        // looping through all rows and adding to list
				   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
				   		drug_location.add(cursor.getString(1));
				   	}
				   	
				   	// Creating adapter for spinner
				   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, drug_location);
						dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						drug_location_spinner.setAdapter (dataAdapter);
						drug_location_spinner.setSelection(0);
		
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
	    	
		 	TextView 	TV;
		 	String 		temp_string;
	    	Float 		trait11_data = 0.0f;
	    	int 		temp_integer;
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
			boxvaccine = (CheckBox) findViewById(R.id.checkBoxGiveVaccine);
			if (boxvaccine.isChecked()){
				//	Go get which vaccine was selected in the spinner
				vaccine_spinner = (Spinner) findViewById(R.id.vaccine_spinner);
		    	which_vaccine = vaccine_spinner.getSelectedItemPosition();
				if (which_vaccine == 0){
					//	Need to require a value for vaccine here
					//  Missing data so  display an alert 					
		    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		    		builder.setMessage( R.string.vaccine_fill_fields )
		    	           .setTitle( R.string.vaccine_fill_fields );
		    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int idx) {
		    	               	// User clicked OK button 
		    	         		// make update database button normal and enabled so we can try again
		    	           		btn = (Button) findViewById( R.id.update_database_btn );
		    	           		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
		    	            	btn.setEnabled(true);
		     	    		   return;
		    	               }
		    	       });		
		    		AlertDialog dialog = builder.create();
		    		dialog.show();	
		    		return;
				}
				else{
				Log.i("vaccine ", String.valueOf(which_vaccine));
				}
		    			    	
				//	go update the database with a drug record for this vaccine and this sheep
//		    	Log.i("vaccine spinner", " position is" + String.valueOf(which_vaccine));
				//	go update the database with a drug record for this vaccine and this sheep
				// The drug_id is at the same position in the vaccine_id_drugid list as the spinner position			
				i = vaccine_id_drugid.get(which_vaccine);
//				Log.i("vaccine id", " value is " + String.valueOf(i));
				// Go get drug location for the shot
				//	Need to set up a radio button for the locations and read it
//				Get the radio group selected for the location
//				Log.i("before radio group", " getting ready to get the shot location ");
				radioGroup=(RadioGroup)findViewById(R.id.radioShotLoc);
		 		shot_loc = radioGroup.getCheckedRadioButtonId()+1;	
		 		if (shot_loc == 0){
		    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		    		builder.setMessage( R.string.drug_loc_fill_fields )
		    	           .setTitle( R.string.drug_loc_fill_fields );
		    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int idx) {
		    	               	// User clicked OK button 
		    	         		// make update database button normal and enabled so we can try again
		    	           		btn = (Button) findViewById( R.id.update_database_btn );
		    	           		btn.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF000000));
		    	            	btn.setEnabled(true);
		     	    		   return;
		    	               }
		    	       });		
		    		AlertDialog dialog = builder.create();
		    		dialog.show();	
		    		return;
		 		}else{
					cmd = String.format("insert into sheep_drug_table (sheep_id, drug_id, drug_date_on," +
			  				" drug_time_on, drug_date_off, drug_time_off, drug_dosage, drug_location) values " +
			  				" (%s, '%s', '%s', '%s', '%s', '%s', '%s', %s) ", thissheep_id, i, mytoday, mytime, 
			  				empty_string_field, empty_string_field, empty_string_field, shot_loc);
			  		Log.i("add drug to ", "db cmd is " + cmd);
					dbh.exec(cmd);
					Log.i("add tag ", "after insert into sheep_drug_table");
					
					//	Need to update the alert to include the slaughter withdrawal for this vaccine
					cmd = String.format("Select units_table.units_name, user_meat_withdrawal from drug_table " +
							"inner join units_table on drug_table.meat_withdrawal_units = units_table.id_unitsid where id_drugid = %s", i);
					Log.i("drug withdrawal ", "db cmd is " + cmd);
					crsr = dbh.exec(cmd);
					cursor   = ( Cursor ) crsr;
					// If withdrawal data query fails, we can't do this
					if (cursor.getCount() > 0)
					{
						cursor.moveToFirst();
						//	Initially just set an alert with the number and units from today
						// 2014-07-27 Removed the time stamp as it's almost impossible to clear the alerts with it in there
						temp_string = "Slaughter Withdrawal is " + dbh.getStr(1) + " " + dbh.getStr(0) + " from " + mytoday ;
						Log.i("drug withdrawal ", " new alert is " + temp_string);
						if (alert_text != null){						
							//	temp_string = alert_text + "\n" + temp_string;
							// modified to put the new alert on top and add a newline character. 
							temp_string = temp_string + "\n" + alert_text;
						}
						cmd = String.format("update sheep_table set alert01 = '%s' where sheep_id =%d ", temp_string, thissheep_id ) ;
						Log.i("update alerts ", "before cmd " + cmd);
						dbh.exec( cmd );
						Log.i("update alerts ", "after cmd " + cmd);
					}
					else
					{
						Toast.makeText(getBaseContext(), "Vaccine withdrawal data not set", Toast.LENGTH_SHORT).show();
					}
					// TODO
					// Consider calculating the actual date/time withdrawal and putting that in instead. 
		 		}
			}	    	
//			Get the value of the checkbox for shearing 
					Log.i("before checkbox", " getting ready to get shorn or not ");
					boxshear = (CheckBox) findViewById(R.id.checkBoxShorn);
					// TODO 
					if (boxshear.isChecked()){
						//	go update the database with a shearing date and time add that as a note 
						note_text = "";
						predefined_note01 = 23; // hard coded the code for shorn
						// TODO
						//	This will have to be changed for the general case where shearing is not item 23 in the list
						cmd = String.format("insert into sheep_note_table (sheep_id, note_text, note_date, note_time, id_predefinednotesid01) " +
		    					"values ( %s, '%s', '%s', '%s', %s )", thissheep_id, note_text, mytoday, mytime, predefined_note01);
		    			Log.i("update notes ", "before cmd " + cmd);
		    			dbh.exec( cmd );	
		    			Log.i("update notes ", "after cmd exec");
						Log.i("shorn ", String.valueOf(boxshear));					
					}
					
					
			//	Get the value of the checkbox for trim toes
			Log.i("before checkbox", " getting ready to get trim toes or not ");
			boxtrimtoes = (CheckBox) findViewById(R.id.checkBoxTrimToes);
			if (boxtrimtoes.isChecked()){
				//	go update the database with a toe trimming date and time add that as a note 
				note_text = "";
				predefined_note01 = 14; // hard coded the code for toes trimmed
				// TODO
				//	This will have to be changed for the general case where toes is not item 14 in the list
				cmd = String.format("insert into sheep_note_table (sheep_id, note_text, note_date, note_time, id_predefinednotesid01) " +
    					"values ( %s, '%s', '%s', '%s', %s )", thissheep_id, note_text, mytoday, mytime, predefined_note01);
    			Log.i("update notes ", "before cmd " + cmd);
    			dbh.exec( cmd );	
    			Log.i("update notes ", "after cmd exec");
				Log.i("toes trimmed ", String.valueOf(boxtrimtoes));					
			}
			
			//	Get the value of the checkbox for weaned
			Log.i("before checkbox", " getting ready to get weaned or not ");
			boxweaned = (CheckBox) findViewById(R.id.checkBoxWeaned);
			if (boxweaned.isChecked()){
				//	go update the database with a weaned date
				cmd = String.format("update sheep_table set weaned_date = '%s' where sheep_id = %d ", mytoday, thissheep_id);
    			Log.i("update sheep table ", "before cmd " + cmd);
    			dbh.exec( cmd );	
    			Log.i("update sheep table ", "after cmd exec");
    			// remove the wean alert
    			cmd = String.format("update sheep_table set alert01 = replace " +
						"( alert01, 'Wean', '') where sheep_id =%d ", thissheep_id ) ;	
    			Log.i("update sheep table ", "before cmd " + cmd);
    			dbh.exec( cmd );	
    			Log.i("update sheep table ", "after cmd exec");
			}			
			
//			//	Get the value of the checkbox for take  blood
//			Log.i("before checkbox", " getting ready to see if we collected blood or not ");
			boxblood = (CheckBox) findViewById(R.id.checkBoxBlood);
			if (boxblood.isChecked()){
				blood_spinner = (Spinner) findViewById(R.id.blood_spinner);
				which_blood = blood_spinner.getSelectedItemPosition();
				if (which_blood == 0){
					//	Need to require a value for blood here
					//  Missing data so  display an alert 					
		    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		    		builder.setMessage( R.string.blood_fill_fields )
		    	           .setTitle( R.string.blood_fill_fields );
		    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int idx) {
		    	               	// User clicked OK button 
		    	         		// make update database button normal and enabled so we can try again
		    	           		btn = (Button) findViewById( R.id.update_database_btn );
		    	           		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
		    	            	btn.setEnabled(true);
		     	    		   return;
		    	               }
		    	       });		
		    		AlertDialog dialog = builder.create();
		    		dialog.show();	
		    		return;
				}
				else{
				Log.i("blood ", String.valueOf(which_blood));
				}				
				//	go update the database with blood pull date and time as a note 
				note_text = "Blood for ";
				// Go get the reason for the blood test	
				String temp_text = blood_tests.get(which_blood);
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
				//	Update the sheep record to remove the scrapie blood in alert
				cmd = String.format("update sheep_table set alert01 = replace " +
						"( alert01, 'Scrapie Blood', '') where sheep_id =%d ", thissheep_id ) ;
//				Log.i("update alerts ", "before cmd " + cmd);
				dbh.exec( cmd );
//				Log.i("update alerts ", "after cmd " + cmd);
				}
				catch (Exception e){
					Log.w("scrapie", "No scrapie alert");
				}
				try {
					//	Update the sheep record to remove the Brucellosis blood in alert
					cmd = String.format("update sheep_table set alert01 = replace " +
							"( alert01, 'Brucellosis Blood', '') where sheep_id =%d ", thissheep_id ) ;
//					Log.i("update alerts ", "before cmd " + cmd);
					dbh.exec( cmd );
//					Log.i("update alerts ", "after cmd " + cmd);
					}
					catch (Exception e){
						Log.w("scrapie", "No Brucellosis alert");
					}
				try {
					//	Update the sheep record to remove the OPP blood in alert
					cmd = String.format("update sheep_table set alert01 = replace " +
							"( alert01, 'OPP Blood', '') where sheep_id =%d ", thissheep_id ) ;
//					Log.i("update alerts ", "before cmd " + cmd);
					dbh.exec( cmd );
//					Log.i("update alerts ", "after cmd " + cmd);
					}
					catch (Exception e){
						Log.w("scrapie", "No OPP alert");
					}
			}			
			//	Need to figure out the id_drugid for what we are giving this sheep for wormer
			boxwormer = (CheckBox) findViewById(R.id.checkBoxGiveWormer);
			if (boxwormer.isChecked()){
				//	Go get which wormer was selected in the spinner
				wormer_spinner = (Spinner) findViewById(R.id.wormer_spinner);
				which_wormer = wormer_spinner.getSelectedItemPosition();
				
				if (which_wormer == 0){
					//	Need to require a value for vaccine here
					//  Missing data so  display an alert 					
		    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		    		builder.setMessage( R.string.wormer_fill_fields )
		    	           .setTitle( R.string.wormer_fill_fields );
		    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int idx) {
		    	               	// User clicked OK button 
		    	         		// make update database button normal and enabled so we can try again
		    	           		btn = (Button) findViewById( R.id.update_database_btn );
		    	           		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
		    	            	btn.setEnabled(true);
		     	    		   return;
		    	               }
		    	       });		
		    		AlertDialog dialog = builder.create();
		    		dialog.show();	
		    		return;
				}
				else{
				Log.i("wormer ", String.valueOf(which_wormer));
				}							
//				Log.i("wormer spinner", " position is" + String.valueOf(which_wormer));
				//	go update the database with a drug record for this wormer and this sheep
				// The drug_id is at the same position in the wormer_id_drugid list as the spinner position			
				i = wormer_id_drugid.get(which_wormer);
				Log.i("wormer id", " value is " + String.valueOf(i));
				//	Drug location 5 is by mouth, all wormer given by mouth
				cmd = String.format("insert into sheep_drug_table (sheep_id, drug_id, drug_date_on," +
		  				" drug_time_on, drug_location) values " +
		  				" (%s, '%s', '%s', '%s' , %s) ", thissheep_id, i, mytoday, mytime, 5);
		  		Log.i("add drug to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into sheep_drug_table");
				//	Need to update the alert to include the slaughter withdrawal for this wormer
				cmd = String.format("Select units_table.units_name, user_meat_withdrawal from drug_table " +
						"inner join units_table on drug_table.meat_withdrawal_units = units_table.id_unitsid where id_drugid = %s", i);
				Log.i("drug withdrawal ", "db cmd is " + cmd);
				crsr = dbh.exec(cmd);
				cursor   = ( Cursor ) crsr; 
				if (cursor.getCount() > 0)
				{
					cursor.moveToFirst();
					//	Initially just set an alert with the number and units from today
					temp_string = "Slaughter Withdrawal is " + dbh.getStr(1) + " " + dbh.getStr(0) + " from " + mytoday ;
					Log.i("drug withdrawal ", " new alert is " + temp_string);
					if (alert_text != null){
						temp_string = temp_string + "\n" + alert_text;
	//					temp_string = alert_text + "\n" + temp_string;
					}
	
					cmd = String.format("update sheep_table set alert01 = '%s' where sheep_id =%d ", temp_string, thissheep_id ) ;
					Log.i("update alerts ", "before cmd " + cmd);
					dbh.exec( cmd );
					Log.i("update alerts ", "after cmd " + cmd);
				}
				else{
					Toast.makeText(getBaseContext(), "Wormer withdrawal data not set", Toast.LENGTH_SHORT).show();
					Log.w("Withdrawal: Wormer", "No withdrawal data in db");
				}
				// TODO
				// Consider calculating the actual date/time withdrawal and putting that in instead. 
			}	
			//	Give a drug if checked
			boxdrug = (CheckBox) findViewById(R.id.checkBoxGiveDrug);
			if (boxdrug.isChecked()){
				//	Go get which drug was selected in the spinner
				drug_spinner = (Spinner) findViewById(R.id.drug_spinner);
				which_drug = drug_spinner.getSelectedItemPosition();

				if (which_drug == 0){
					//	Need to require a value for drug here
					//  Missing data so  display an alert 					
		    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		    		builder.setMessage( R.string.drug_fill_fields )
		    	           .setTitle( R.string.drug_fill_fields );
		    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int idx) {
		    	               	// User clicked OK button 
		    	         		// make update database button normal and enabled so we can try again
		    	           		btn = (Button) findViewById( R.id.update_database_btn );
		    	           		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
		    	            	btn.setEnabled(true);
		     	    		   return;
		    	               }
		    	       });		
		    		AlertDialog dialog = builder.create();
		    		dialog.show();	
		    		return;
				}
				else{
				Log.i("drug ", String.valueOf(which_drug));
				}	
								
				//	go update the database with a drug record for this wormer and this sheep
				// The drug_id is at the same position in the wormer_id_drugid list as the spinner position			
				i = drug_id_drugid.get(which_drug);
				Log.i("drug id", " value is " + String.valueOf(i));
				//TODO
				//	Go get a Drug location 
				
				drug_location_spinner = (Spinner) findViewById(R.id.drug_location_spinner);
				drug_loc = drug_location_spinner.getSelectedItemPosition();
		 		if (drug_loc == 0){
		    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		    		builder.setMessage( R.string.drug_loc_fill_fields )
		    	           .setTitle( R.string.drug_loc_fill_fields );
		    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
		    	           public void onClick(DialogInterface dialog, int idx) {
		    	               	// User clicked OK button 
		    	         		// make update database button normal and enabled so we can try again
		    	           		btn = (Button) findViewById( R.id.update_database_btn );
		    	           		btn.getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFF000000));
		    	            	btn.setEnabled(true);
		     	    		   return;
		    	               }
		    	       });		
		    		AlertDialog dialog = builder.create();
		    		dialog.show();	
		    		return;
		 		}else{
					cmd = String.format("insert into sheep_drug_table (sheep_id, drug_id, drug_date_on," +
			  				" drug_time_on, drug_location) values " +
			  				" (%s, '%s', '%s', '%s' , %s) ", thissheep_id, i, mytoday, mytime, drug_loc);
			  		Log.i("add drug to ", "db cmd is " + cmd);
					dbh.exec(cmd);
					Log.i("add tag ", "after insert into sheep_drug_table");
					//	Need to update the alert to include the slaughter withdrawal for this drug
					cmd = String.format("Select units_table.units_name, user_meat_withdrawal from drug_table " +
							"inner join units_table on drug_table.meat_withdrawal_units = units_table.id_unitsid where id_drugid = %s", i);
					Log.i("drug withdrawal ", "db cmd is " + cmd);
					crsr = dbh.exec(cmd);
					if (cursor.getCount() > 0) {
						cursor   = ( Cursor ) crsr; 		    	
						cursor.moveToFirst();
						//	Initially just set an alert with the number and units from today
						// 2014-07-27 Removed the time stamp as it's almost impossible to clear the alerts with it in there
						Log.i("today is ", mytoday);
						temp_string = "Slaughter Withdrawal is " + dbh.getStr(1) + " " + dbh.getStr(0) + " from " + mytoday ;
						Log.i("drug withdrawal ", " new alert is " + temp_string);
						if (alert_text != null){
							temp_string = temp_string + "\n" + alert_text;
	//						temp_string = alert_text + "\n" + temp_string;
						}
						cmd = String.format("update sheep_table set alert01 = '%s' where sheep_id =%d ", temp_string, thissheep_id ) ;
						Log.i("update alerts ", "before cmd " + cmd);
						dbh.exec( cmd );
						Log.i("update alerts ", "after cmd " + cmd);
					}
					else
					{
						Log.w("Withdrawal: Drug", "No withdrawal data in db");
					}
					// TODO
					// Consider calculating the actual date/time withdrawal and putting that in instead. 
					//	get and add Drug Reason
					// Put this back into the sneep_management.xml file when I add in reasons
//					<TextView
//		        	android:layout_width="150dp"
//		       	 	android:layout_height="wrap_content"
//		        	android:layout_column="0"
//		            android:layout_gravity="right"
//		            android:layout_row="7"
//		            android:inputType="none"
//		        	android:text="@string/drug_reason_lbl" />
//				        
//				<EditText
//		            android:id="@+id/DrugReasonText"
//		            android:layout_width="260dp"
//		            android:layout_height="wrap_content"
//		            android:layout_gravity="left|center"
//		            android:layout_column="1"
//		            android:layout_row="7"
//		            android:enabled="true"
//		            android:textSize="18sp"
//		            android:inputType="text"
//		            android:typeface="monospace" >
//					</EditText>
		 		}
			}				
			
			//	Remove a drug if checked
			//	Only valid for things like CIDRs and Sponges
			boxremovedrug = (CheckBox) findViewById(R.id.checkBoxRemoveDrug);
			if (boxremovedrug.isChecked()){
				//	Go get which drug was selected in the spinner
				drug_spinner = (Spinner) findViewById(R.id.drug_spinner);
				which_drug = drug_spinner.getSelectedItemPosition();
				
				
				
				
				
				
				// The drug_id is at the same position in the drug_id_drugid list as the spinner position			
				i = drug_id_drugid.get(which_drug);
				Log.i("drug id", " value is " + String.valueOf(i));
				
				//	Go find the instance of this drug with no remove date for this sheep
				cmd = String.format("select id_sheepdrugid from sheep_drug_table where " +
				" sheep_id = %s and drug_id = %s and drug_date_off = '' ",thissheep_id, i);
				Log.i("remove drug to ", "db cmd is " + cmd);
				crsr = dbh.exec(cmd);
				cursor   = ( Cursor ) crsr; 	
				if (cursor.getCount() > 0) {
					cursor.moveToFirst();
					id_sheepdrugid = dbh.getInt(0);
					Log.i("drug record is ", String.valueOf(id_sheepdrugid));
					Log.i("today is ", mytoday);
					Log.i("remove drug ", "before update the sheep_drug_table");
					cmd = String.format("update sheep_drug_table set drug_date_off = '%s', " +
						"drug_time_off = '%s' where id_sheepdrugid = %s", mytoday, mytime, id_sheepdrugid);
				  	Log.i("remove drug to ", "db cmd is " + cmd);
					dbh.exec(cmd);
				}else{
					Toast.makeText(getBaseContext(), "This drug is not eligible for removal", Toast.LENGTH_SHORT).show();
					Log.w("Removal: Drug", "No removable instance of drug found");
				}
				Log.i("add tag ", "after update sheep_drug_table with remove date");					
			}	
			
			//	Take a weight if checked
					boxweight = (CheckBox) findViewById(R.id.checkBoxTakeWeight);
					if (boxweight.isChecked()){
						//	get a sheep weight
			    		TV = (TextView) findViewById(R.id.trait11_data);
			    		temp_string = TV.getText().toString();
			    		if(TextUtils.isEmpty(temp_string)){
			    	        // EditText was empty
			    	        // so no real data collected just break out
			    			trait11_data = 0.0f;
//			    			Log.i("save trait11", "float data is " + String.valueOf(trait11_data));
			    	    }
			    		else {
			    			trait11_data = Float.valueOf(TV.getText().toString());
			    			Log.i("save trait11", "float data is " + String.valueOf(trait11_data));
			    		}
						
			    		// Calculate the age in days for this sheep for this evaluation to fill the age_in_days field
			    		cmd = String.format("Select julianday(birth_date) from sheep_table where sheep_id = '%s'", thissheep_id);
			    		Log.i("get birthdate eval ", cmd);
			    		dbh.exec( cmd );
			    		crsr2 = dbh.exec( cmd );
			            cursor2   = ( Cursor ) crsr2;
			            dbh.moveToFirstRecord();	            
			            temp_integer = (int) Utilities.GetJulianDate()-(dbh.getInt(0));
			            Log.i("get age in days ", String.valueOf (temp_integer));
			            
			            //	go update the database with a sheep evaluation record for this weight and this sheep		       		
			    		cmd = String.format("insert into sheep_evaluation_table (sheep_id, " +
			    		"trait_name01, trait_score01, trait_name02, trait_score02, trait_name03, trait_score03, " +
			    		"trait_name04, trait_score04, trait_name05, trait_score05, trait_name06, trait_score06," +
			    		"trait_name07, trait_score07, trait_name08, trait_score08, trait_name09, trait_score09, " +
			    		"trait_name10, trait_score10, trait_name11, trait_score11, trait_name12, trait_score12, " +
			    		"trait_name13, trait_score13, trait_name14, trait_score14, trait_name15, trait_score15, " +
			    		"trait_name16, trait_score16, trait_name17, trait_score17, trait_name18, trait_score18, " +
			    		"trait_name19, trait_score19, trait_name20, trait_score20, " +
			    		"trait_units11, trait_units12, trait_units13, trait_units14, trait_units15, eval_date, eval_time, age_in_days) " +
			    		"values (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s," +
			    		"%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,'%s','%s', %s) ", 
			    		thissheep_id, 0, 0, 0, 0, 0, 0,
			    				0, 0, 0, 0, 0, 0,
			    				0, 0, 0, 0, 0, 0, 
			    				0, 0, 16, trait11_data, 0, 0, 
			    				0, 0, 0, 0, 0, 0, 
			    				0, 0, 0, 0, 0, 0,
			    				0, 0, 0, 0, 
			    				1, 0, 0, 0, 0, mytoday, mytime, temp_integer );
			    		Log.i("add evaluation ", "cmd is "+ cmd);
						dbh.exec(cmd);
						Log.i("add evaluation ", "after insert into sheep_evaluation_table");
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
		stopService(new Intent(GroupSheepManagement.this, eidService.class));   	
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
      	this.finish();
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
		// Don't clear the shot location when I clear stuff out
//		radioGroup=(RadioGroup)findViewById(R.id.radioShotLoc);
//		radioGroup.clearCheck();
//		Log.i("clear btn", "after clear radioGroup");
		boxvaccine = (CheckBox) findViewById(R.id.checkBoxGiveVaccine);
		boxvaccine.setChecked(false);
//		Log.i("clear btn", "after clear vaccine checkbox");
		boxwormer = (CheckBox) findViewById(R.id.checkBoxGiveWormer);
		boxwormer.setChecked(false);
		boxshear = (CheckBox) findViewById(R.id.checkBoxShorn);
		boxshear.setChecked(false);
//		Log.i("clear btn", "after clear wormer checkbox");
		boxtrimtoes = (CheckBox) findViewById(R.id.checkBoxTrimToes);
		boxtrimtoes.setChecked(false);
//		Log.i("clear btn", "after clear trim toes checkbox");
		boxblood = (CheckBox) findViewById(R.id.checkBoxBlood);
		boxblood.setChecked(false);
//		Log.i("clear btn", "after blood checkbox");	
		boxweaned = (CheckBox) findViewById(R.id.checkBoxWeaned);
		boxweaned.setChecked(false);
		boxremovedrug = (CheckBox) findViewById(R.id.checkBoxRemoveDrug);
		boxremovedrug.setChecked(false);
		boxdrug = (CheckBox) findViewById(R.id.checkBoxGiveDrug);
		boxdrug.setChecked(false);
//		Log.i("clear btn", "after give drug checkbox");			
		boxweight = (CheckBox) findViewById(R.id.checkBoxTakeWeight);
		boxweight.setChecked(false);
		TV = (TextView) findViewById( R.id.trait11_data );
		TV.setText( "" );
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
		Log.i("SheepMgmt", " OnResume");
		scanEid( null );	
//		Log.i("SheepMgmt", " OnResume after scanEID(null)");
	}

	@Override
	public void onPause (){	
		super.onPause();
		Log.i("SheepMgmt", " OnPause");
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
