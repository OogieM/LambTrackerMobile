package com.weyr_associates.lambtracker;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import com.weyr_associates.lambtracker.LookUpSheep.IncomingHandler;
import com.weyr_associates.lambtracker.Utilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AddLamb extends Activity {
	private DatabaseHandler dbh;
	String     	cmd;
	Integer 	i;	
	public Button btn;
	public String alert_text;
	public Cursor 	cursor, cursor2;
	public Object 	crsr, crsr2;
	public Spinner tag_type_spinner, tag_type_spinner2, tag_location_spinner, tag_color_spinner, lamb_ease_spinner;
	public List<String> tag_types, tag_locations, tag_colors, lambing_ease;
	public List<Integer> lambeaseid;
	ArrayAdapter<String> dataAdapter;
	public int 		thissheep_id;
	public int	rear_type, birth_type, sex, lambease, codon171, codon136, codon154;
	public String dam_name, sire_name, lamb_name;
	public String lamb_alert_text, death_date, remove_date;
	public String nsip_id, sale_price, sell_price_units, purchase_price, purchase_price_units;
	public String weaned_date, inbreeding;
	public String tag_country_code;
	public int remove_reason, official_id, id_breederid, lambing_contact;
	public int dam_id, dam_codon171, dam_codon154, dam_codon136;
	public int sire_id, sire_codon171, sire_codon154, sire_codon136;
	public int lamb_id, lamb_codon171, lamb_codon154, lamb_codon136;
	public int flock_prefix, id_sheepbreedid, id_locationid, id_ownerid, acquire_reason, management_group;
	public int	service_type, birth_weight_units, lamb_birth_record;
	public CheckBox 	stillbornbox, markewebox;
	public boolean stillborn, markewe;
	public Float birth_weight;
	public double	real_gestation_length;
	public RadioGroup radioGroup;
	public String mytoday;
	public String mytime;
	public Integer lambing_historyid, lamb01_id, lamb02_id, lamb03_id, lambs_born;
	public String lambing_notes, lambing_date, lambing_time, sex_abbrev;
	
	int		fedtagid, farmtagid, eidtagid ; // These are record IDs not sheep IDs
	public int new_tag_type, new_tag_color, new_tag_location;
	
	public String tag_type_label, tag_color_label, tag_location_label, new_tag_number, eid_tag_color_label ;
	public String eid_tag_location_label, eidText;
	public Spinner eid_tag_color_spinner, eid_tag_location_spinner;
	private int             nRecs;
	
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
				stopService(new Intent(AddLamb.this, eidService.class));

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
			startService(new Intent(AddLamb.this, eidService.class));
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
		View v = null;
		//	make the scan eid button red
		btn = (Button) findViewById( R.id.scan_eid_btn );
		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
		
		addNewTag(v );
		// 	Display the EID number
		TextView TV = (TextView) findViewById (R.id.inputText);
		TV.setText( LastEID );
		Log.i("in gotEID ", "with LastEID of " + LastEID);
				
	  	// Put the EID Tag data into the add tag section of the screen
	  	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
	  	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
	  	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
	  	TV = (TextView) findViewById( R.id.new_tag_number );
	  	TV.setText( LastEID );
	  	//	Set tag type, color and location to defaults for EID tags
	  	//	Electronic, Yellow, Right Ear
	  	tag_type_spinner2.setSelection(2);
//	  	Log.i("updateTag", "Tag type is " + tag_type_label);
	  	tag_color_spinner.setSelection(1);
//	  	Log.i("updateTag", "Tag color is " + tag_color_label);
	  	tag_location_spinner.setSelection(1);
//	  	Log.i("updateTag", "Tag location is " + tag_location_label);
	  	// Put the last EID into the new tag number
	  	new_tag_number = LastEID;	  	
	}	
	/////////////////////////////////////////////////////
	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lamb);
        Log.i("AddLamb", " after set content view");
        View v = null;
        String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("AddLamb", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
    	Object crsr;
    	ArrayList radiobtnlist;
    	String[] radioBtnText;
//       	Boolean			exists;
       	TextView TV;
       	float temp_ram_in, temp_ram_out;
       	double	gestation_length, first_gestation_possible, last_gestation_possible;
       	double temp_julian_today;
       	int [] jintdate = new int[] {0,0,0};

   	 //////////////////////////////////// 
		CheckIfServiceIsRunning();
		Log.i("Convert", "back from isRunning");  	
		////////////////////////////////////    	
		
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
			
	    //	make the scan eid button red
	    btn = (Button) findViewById( R.id.scan_eid_btn );
	    btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	
	    // make the alert button normal and disabled
	   	btn = (Button) findViewById( R.id.alert_btn );
	   	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
	   	btn.setEnabled(false);  
	   	
		//	Disable the Next Record and Prev. Record button until we have multiple records
	   	btn = (Button) findViewById( R.id.next_rec_btn );
	   	btn.setEnabled(false); 
	   	btn = (Button) findViewById( R.id.prev_rec_btn );
	   	btn.setEnabled(false);
	
	 	//	Disable the Take Note button
		btn = (Button) findViewById( R.id.take_note );
		btn.setEnabled(false);
		
		//	Disable the Look Up Sheep Button
		btn = (Button) findViewById( R.id.look_up_sheep_btn );
		btn.setEnabled(false);
		
		//	Disable the bottom update tag button until we choose to add or update
	   	btn = (Button) findViewById( R.id.update_display_btn );
		btn.setEnabled(false); 
	
		// Select All fields from sheep sex to fill the lamb sex radio group
		radiobtnlist = new ArrayList();
	   	cmd = "select * from sheep_sex_table";
	   	crsr = dbh.exec( cmd );  
		cursor   = ( Cursor ) crsr;
	   	dbh.moveToFirstRecord();
	    // looping through all rows and adding to list
	   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	   		radiobtnlist.add(cursor.getString(1));
	   	}
	    radioBtnText = (String[]) radiobtnlist.toArray(new String [radiobtnlist.size()]);
		// Build the radio buttons here
		radioGroup = ((RadioGroup) findViewById(R.id.radioGroupSex));
		addRadioButtons(4, radioBtnText);
		radiobtnlist.clear ();
		
		//	Fill the rear type radio group
   		radiobtnlist.add ("Single");
   		radiobtnlist.add ("Twin");
   		radiobtnlist.add ("Triplet");
	    radioBtnText = (String[]) radiobtnlist.toArray(new String [radiobtnlist.size()]);
		// 	Build the radio buttons here
		radioGroup = ((RadioGroup) findViewById(R.id.radioRearType));
		addRadioButtons(3, radioBtnText);
		radiobtnlist.clear ();
		
    	//	Get the text for the lamb ease buttons  
		lamb_ease_spinner = (Spinner) findViewById(R.id.lamb_ease_spinner);
    	cmd = String.format("select custom_evaluation_traits_table.custom_evaluation_item, custom_evaluation_traits_table.id_custom_traitid " +
    			" from custom_evaluation_traits_table " +
    			" where custom_evaluation_traits_table.id_traitid = '%s' "+
    			" order by custom_evaluation_traits_table.custom_evaluation_order ASC ", 24);
    	//    	Log.i("evaluate2", " cmd is " + cmd);	    	
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        lambing_ease = new ArrayList<String>();
        lambing_ease.add("Lambing Ease");
        lambeaseid = new ArrayList <Integer>();
        lambeaseid.add(0);
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
        	lambing_ease.add (cursor.getString(0));
//	    	Log.i("addlamb", " Lambing ease text is " + cursor.getString(0));
	    	lambeaseid.add (cursor.getInt(1));
//	    	Log.i("addlamb", " Lambing ease id is " + String.valueOf(cursor.getInt(1)));
    	}        

    	// Creating adapter for lambease spinner
     	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, lambing_ease);
     	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     	lamb_ease_spinner.setAdapter (dataAdapter);
		lamb_ease_spinner.setSelection(0);
		Log.i("addlamb", " after set selection" );
		
		//	Fill birth_weight with 0 until we get a weight
		birth_weight = 0.0f;		
		//	Fill the lamb id's with nothing until we search for them
		lamb01_id = 0;
		lamb02_id = 0;
		lamb03_id = 0;
		//	Fill the lambs born and rear type fields to have no lamb until we start
		lambs_born = 0;
		rear_type = 0;
		//	The lambing_history record is empty until we create one
		lambing_historyid = 0;
		//	Fill lambing_notes with empty string until we read them from the history
		lambing_notes = "";
		
		//	Fill the mark ewe box default to checked 
		markewebox = (CheckBox) findViewById(R.id.checkBoxMarkEwe);
		markewebox.setChecked (false);
		
		Bundle extras = getIntent().getExtras();
		// get extras here from the lambing screen. Mostly ewe's data for scrapie genetics	
		if (extras!= null){
			dam_id = extras.getInt("dam_id");
			dam_name = extras.getString("dam_name");
			TV = (TextView) findViewById( R.id.damName );
            TV.setText(dam_name); 
            Log.i("add a lamb ", " dam is " + dam_name);
            dam_codon171 = extras.getInt("codon171");
            dam_codon154 = extras.getInt("codon154");
            dam_codon136 = extras.getInt("codon136");
		}
		//	Now need to figure out who the sire is based on date and breeding records.
		//  First put an empty string in as sire name
		sire_name = "Sire not found";
		Calendar calendar = Calendar.getInstance();
		Log.i("add a lamb ", " after getting a calendar");
			jintdate [0] = calendar.get(Calendar.YEAR);
			jintdate [1] = calendar.get(Calendar.MONTH) +1;
			jintdate [2] = calendar.get(Calendar.DAY_OF_MONTH);
		
//			Log.i("add a lamb ", " before getting julian of today");
		temp_julian_today = Utilities.toJulian(jintdate);
		Log.i("addlamb", " julian today is " + String.valueOf(temp_julian_today));
		
		//	Then go get the breeding records for this ewe.
		cmd = String.format("select sheep_breeding_table.ewe_id, " +
				" sheep_breeding_table.breeding_id, " +
				" breeding_record_table.ram_id, " +
				" sheep_table.sheep_name, " +
				" julianday(breeding_record_table.date_ram_in), " +
    			" julianday(breeding_record_table.date_ram_out),  " +
    			" breeding_record_table.service_type " +
    			" from breeding_record_table " +
    			" left outer join sheep_breeding_table on " +
    			" sheep_breeding_table.breeding_id = breeding_record_table.id_breedingid " +
    			" inner join sheep_table on sheep_id = ram_id " +
    			" where sheep_breeding_table.ewe_id = '%s' ", dam_id);		  					
    	Log.i("add a lamb ", " cmd is " + cmd);	    	
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        nRecs    = cursor.getCount();
        Log.i("addlamb", "number of breeding records is " + String.valueOf (nRecs));
        dbh.moveToFirstRecord();
        if (nRecs > 0) {
	        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	        	Log.i("addlamb", " in for loop checking breeding dates ");
	        	Log.i("addlamb", " in for loop this breeding record is cursor " + String.valueOf(cursor.getPosition()));	
	        	Log.i("addlamb", " in for loop this breeding record is id " + String.valueOf(dbh.getInt(1)));	
	        	Log.i("addlamb", " in top loop sire is " + sire_name);	
	        	Log.i("addlamb", " in top loop sire_id is " + String.valueOf(sire_id));	
	        	// Check the dates and see if this is the right record
	        	// Get the date ram in and date ram out
	        	temp_ram_in = dbh.getReal(4);
	        	Log.i("addlamb", " julian ram in " + String.valueOf(temp_ram_in));
	        	temp_ram_out = dbh.getReal(5);
	        	Log.i("addlamb", " julian ram out " + String.valueOf(temp_ram_out));
	        	// need to figure out if the date is within early date 141 from ram in
	        	// and end date 159 from ram out
	        	//	Calculate the first possible and last possible for this breeding record
	        	//	Should make these dates a preference or settings in LambTracker
	        	// TODO
	        	first_gestation_possible = temp_ram_in + 140.0;
	        	Log.i("addlamb", " julian first gestation is " + String.valueOf(first_gestation_possible));
	        	last_gestation_possible = temp_ram_out + 153.0;
	        	Log.i("addlamb", " julian last gestation is " + String.valueOf(last_gestation_possible));        	
	        	// First calculate how many days gestation this is from date ram in
	        	gestation_length = temp_julian_today - temp_ram_in;
	        	Log.i("addlamb", " calculated gestation length is " + String.valueOf(gestation_length));
	        	
	        	if  (temp_julian_today > first_gestation_possible && temp_julian_today < last_gestation_possible) {
	        		//	This is the correct record so save the data and bump out
	        		Log.i("addlamb", " correct breeding record is this cursor " + String.valueOf(cursor.getPosition()));
	        		sire_name = dbh.getStr(3);
	        		sire_id = dbh.getInt(2);
	        		service_type = dbh.getInt(6);
	        		//	make the gestation length an integer so it can be added to the lambing history record. 
	        		real_gestation_length = (int)gestation_length;
	        		Log.i("addlamb", " gestation length is " + String.valueOf(real_gestation_length));
	        		Log.i("addLamb" , "before break out of for loop");
	        		break;
	        	}        	
        	// The sire we have is 
        	Log.i("addlamb", " in for loop sire is " + sire_name);	
        	Log.i("addlamb", " in for loop sire_id is " + String.valueOf(sire_id));	
	        }        
		//	Handle the sire data here
        TV = (TextView) findViewById( R.id.sireName );
        TV.setText(sire_name); 
        Log.i("addlamb", " after set display of sire name " + sire_name);
        //	Go get the sire Codon171,154 and 136 values
        if (sire_id == 0){
        	//	Sire not found so set codon values to unknown
            sire_codon171 = 6;
        	sire_codon154 = 1;
        	sire_codon136 = 6; 
        	
        }else{
	        cmd = String.format("select sheep_table.codon171, sheep_table.codon154, " +
	        		" sheep_table.codon136 from sheep_table where sheep_id = '%s' ", sire_id);
	        Log.i("addlamb", " getting codon data cmd is " + cmd);	
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        sire_codon171 = dbh.getInt(0);
	        Log.i("addlamb", " codon171 " + String.valueOf(sire_codon171));
	        sire_codon154 = dbh.getInt(1);
	//        Log.i("addlamb", " codon154 " + String.valueOf(sire_codon154));
	        sire_codon136 = dbh.getInt(2);  
	//        Log.i("addlamb", " codon154 " + String.valueOf(sire_codon136));        
        }
        }
    }
    public void updateDatabase( View v ){
    	RadioGroup 	rg;
    	TextView 	TV;
  		TextView temp_tag_num, temp_tag_color, temp_tag_loc, temp_tag_type;
  		String tag_num;
  		int tag_color, tag_loc, tag_type, tag_flock;
  		Integer temp_id;
  		String year = Utilities.YearIs();
  		TableLayout tl;
    	Object crsr;
    	Context context = this;
		String title;
		String message;
    	
		// Disable Update Database button and make it red to prevent getting 2 records at one time
    	btn = (Button) findViewById( R.id.update_database_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	btn.setEnabled(false);
    	//	added this to set alerts and death and remove dates to empty so we don't get a false null     	
    	lamb_alert_text = "";
    	death_date = "";
		remove_date = "";
		remove_reason = -1;
		//	Set the lamb name to be year plus dam name until we change it
		lamb_name = Utilities.YearIs()+ "-" + dam_name + "-" + "lamb";
		//	Get the date and time to add to the lamb record these are strings not numbers
		mytoday = Utilities.TodayIs(); 
//		Log.i("add a lamb ", " today is " + mytoday);	
		mytime = Utilities.TimeIs();
//		Log.i("add a lamb ", " time is " + mytime);
    	
    	//	Get the data for this lamb   	 		
   		//	Get the radio group selected for the rear type
		Log.i("before radio group", " getting ready to get the rear type ");
		rg=(RadioGroup)findViewById(R.id.radioRearType);
 		rear_type = rg.getCheckedRadioButtonId()+1;
		Log.i("rear_type ", String.valueOf(rear_type));
		if (rear_type == 0){
			//	Need to require a value for rearing type here Nothing selected to set default to be single
			// TODO Tried to get an alert here but can't have 2 in one activity 
//			title = getResources().getString(R.string.add_lamb_fill_title);
//			message = getResources().getString(R.string.add_lamb_fill_reartype);;
//			Utilities.alertDialogShow( context, title, message);
			// make update database button normal and enabled so we can try again
       		btn = (Button) findViewById( R.id.update_database_btn );
       		btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
        	btn.setEnabled(true);
        	//	Defaulted to rear-type of 1 so the later query won't crash
			rear_type = 1;	
        	
		}
			
  		//	Get the radio group selected for the sex
		Log.i("before radio group", " getting ready to get the sex ");
		rg=(RadioGroup)findViewById(R.id.radioGroupSex);
		sex = rg.getCheckedRadioButtonId();
		Log.i("sex radio button id ", String.valueOf(rg.getCheckedRadioButtonId()));		
		if (sex == -1){
			//	Set the sex to be unknown if not filled in
			sex_abbrev = "U";
			sex = 4;
		}else{
			sex = sex + 1;
			Log.i("sex ", String.valueOf(sex));
	 		cmd = String.format("select sheep_sex_table.sex_abbrev from sheep_sex_table " +
	 		"where sheep_sex_table.sex_sheepid = %s", sex);
	 		crsr = dbh.exec( cmd );  
			cursor   = ( Cursor ) crsr;
	  		dbh.moveToFirstRecord();
	  		sex_abbrev = dbh.getStr(0);	
		}			
		Log.i("sex abbrev ", sex_abbrev);
		
		//	Get the value of the checkbox for stillborn
		Log.i("before checkbox", " getting ready to get stillborn or not ");
		stillbornbox = (CheckBox) findViewById(R.id.checkBoxStillborn);
		if (stillbornbox.isChecked()){
			//	Set the values for death dates for stillborn lambs.
			//	Set the remove reason to be the value for Died Stillborn
			stillborn = true;
			remove_reason = 6;
			Log.i("stillborn ", String.valueOf(stillborn));
			death_date = mytoday;
			remove_date = mytoday;
			lamb_name = Utilities.YearIs()+ "-" + "Stillborn";
		}
		
		//	Get the value of the checkbox for mark ewe
		Log.i("before checkbox", " getting ready to get mark ewe or not ");
		markewebox = (CheckBox) findViewById(R.id.checkBoxMarkEwe);
		if (markewebox.isChecked()){
			markewe = true;
			Log.i("markewe ", String.valueOf(markewe));
			// When we get to adding tags for the lamb we'll add a paint brand for ewe if there is one
			//	for the lamb.
		}
		
		//	Get the Birth Weight
		Log.i("before weight", " getting ready to get birth weight ");
		TV = (TextView) findViewById(R.id.birth_weight);
		try {
			birth_weight = Float.valueOf(TV.getText().toString());
		} catch (Exception e) {
			//	set birth_weight to 0 because nothing filled in
			birth_weight = 0.0f;
		}	
		Log.i("birth_weight ", String.valueOf(birth_weight));
		
		//	Get the lambease score
		Log.i("before lambease", " getting ready to get lambease ");
		lamb_ease_spinner = (Spinner) findViewById(R.id.lamb_ease_spinner);
		lambease = lamb_ease_spinner.getSelectedItemPosition();
		Log.i("lambease", " spinner position is " + String.valueOf(lambease));
		lambease = lambeaseid.get(lambease);
		Log.i("lambease", " database ID for Lambease position is " + String.valueOf(lambease));
		if (lambease == 0){
			//	Need to require a value for lambease here
			//  Missing data so  display an alert 	
			
			//	testing my utility alert
//			 context = this;
//			 title = ("Testing fill lambease");
//			 message = "testing this is a message";
//			Utilities.alertDialogShow( context, title, message);
			
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.add_lamb_fill_lambease )
    	           .setTitle( R.string.add_lamb_fill_lambease );
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
		}
		else{
		Log.i("lambease ", String.valueOf(lambease));
		
		//	Calculate codon171 value based on sire and dam if possible
		if (dam_codon171 == 1) {
			// Dam is QQ so test the sire with a case statement
			Log.i("codon171 ", "Starting case statement for Dam is QQ");
			switch (sire_codon171){
			case 1:
				//	we have dam is QQ and Sire is QQ so lamb is QQ
				Log.i("codon171 ", "Case 1: dam is QQ and Sire is QQ so lamb is QQ");
				lamb_codon171 = 1;
				break;
			case 2:
			case 3:
			case 4:
				// 	Dam is QQ but sire is Q? or QR or R? so lamb is Q?
				Log.i("codon171 ", "Case 2,3,4: Dam is QQ but sire is Q? or QR or R? so lamb is Q?");
				lamb_codon171 = 2;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
				break;
			case 5:
				//	Dam is QQ but sire is RR so lamb is QR
				Log.i("codon171 ", "Case 5: Dam is QQ but sire is RR so lamb is QR");
				lamb_codon171 = 3;
				break;
			case 6:
				//	Dam is QQ but sire is ?? so lamb is Q?
				Log.i("codon171 ", "Case 6: Dam is QQ but sire is ?? so lamb is Q?");
				lamb_codon171 = 2;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
			default:
				//	We do not test for H or K alleles so the rest of the options are all set to do nothing
				break;
			}
		}
		if (dam_codon171 == 5) {
			// Dam is RR so test the sire with a case statement
			Log.i("codon171 ", "In case statement with Dam is RR");
			switch (sire_codon171){
			case 1:
				//	we have Dam is RR and Sire is QQ so lamb is QR
				Log.i("codon171 ", "Case 1: Dam is RR and Sire is QQ so lamb is QR");
				lamb_codon171 = 3;
				break;
			case 2:
			case 3:
			case 4:
				// 	Dam is RR but sire is Q? or QR or R? so lamb is R?
				Log.i("codon171 ", "Case 2,3,4: Dam is RR but sire is Q? or QR or R? so lamb is R?");
				lamb_codon171 = 4;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}		
				break;
			case 5:
				//	Dam is RR and sire is RR so lamb is RR
				Log.i("codon171 ", "Case 5: Dam is RR and sire is RR so lamb is RR");
				lamb_codon171 = 5;
				break;
			case 6:
				//	Dam is RR but sire is ?? so lamb is R?
				Log.i("codon171 ", "Case 6: Dam is RR but sire is ?? so lamb is R?");
				lamb_codon171 = 4;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
			default:
				//	We do not test for H or K alleles so the rest of the options are all set to do nothing
				break;
			}
		}
		if (dam_codon171 == 2 || dam_codon171 == 4) {
			// Dam is Q? or R? 
			Log.i("codon171 ", "Dam is Q? or R? so test sire with case statement");
			switch (sire_codon171){
			case 1:
				//	we have Dam is Q? or R? and Sire is QQ so lamb is Q?
				Log.i("codon171 ", "Case 1: Dam is Q? or R? and Sire is QQ so lamb is Q?");
				lamb_codon171 = 2;
				//	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
				break;
			case 2:
			case 3:
			case 4:	
				// 	Dam is Q? or R? but sire is Q? or QR or R? so lamb is ??
				Log.i("codon171 ", "Case 2,3,4: Dam is Q? or R? but sire is Q? or QR or R? so lamb is ??");
				lamb_codon171 = 6;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}	
				break;
			case 5:
				//	Dam is Q? or R? and sire is RR so lamb is R?
				Log.i("codon171 ", "Case 5: Dam is Q? or R? and sire is RR so lamb is R?");
				lamb_codon171 = 4;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
				break;
			case 6:
				//	Dam is RR but sire is ?? so lamb is R?
				Log.i("codon171 ", "Case 6: Dam is RR but sire is ?? so lamb is R?");
				lamb_codon171 = 4;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
			default:
				//	We do not test for H or K alleles so the rest of the options are all set to do nothing
				break;
			}		
		}
		if (dam_codon171 == 3) {
			// Dam is QR 
			Log.i("codon171 ", "Dam is QR so test sire with case statement");
			switch (sire_codon171){
			case 1:
				//	we have Dam is QR and Sire is QQ so lamb is Q?
				Log.i("codon171 ", "Case 1: Dam is QR and Sire is QQ so lamb is Q?");
				lamb_codon171 = 2;
				//	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
				break;
			case 2: //sire is Q?
			case 3: // sire is QR
			case 4:	// sire is R?
				// 	Dam is QR but sire is Q? or QR or R? so lamb is ??
				Log.i("codon171 ", "Case 2,3,4: Dam is QR but sire is Q? or QR or R? so lamb is ??");
				lamb_codon171 = 6;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}		
				break;
			case 5:
				//	Dam is QR and sire is RR so lamb is R?
				Log.i("codon171 ", "Case 5: Dam is QR and sire is RR so lamb is R?");
				lamb_codon171 = 4;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
				break;
			case 6:
				//	Dam is QR but sire is ?? so lamb is ??
				Log.i("codon171 ", "Case 6: Dam is QR but sire is ?? so lamb is ??");
				lamb_codon171 = 6;
				// 	next need to set alert text to get scrapie blood for this lamb.
				if (lamb_alert_text != null){
					lamb_alert_text = lamb_alert_text + "Scrapie Blood";	
				}else{
					//	Alert has null so just reset it to be the new one
					lamb_alert_text = "Scrapie Blood";
				}
			default:
				//	We do not test for H or K alleles so the rest of the options are all set to do nothing
				break;
			}		
		}
		// TODO Add all the testing for codon154 and codon 136 here but left off for testing
		//	For now set these to ?? because we don't know what this lamb is
		lamb_codon154 = 1;
		lamb_codon136 = 6;
		//	Fill all the misc variables for the sheep record
		// TODO
		//	Set breed based on sire and dam breed
		// Need to get the default breed from settings or preferences
		//	Need to fix for the general case of crossbred lambs but for now set to crossbred if dam is Sooner
		//	Sooner is sheep_id 58
		//	otherwise the default set to be Black Welsh
//		if (dam_id == 58) {
//			id_sheepbreedid = 2;
//		}else {
			//	need to test here if sire and dam are the same breed id and if so set lamb to that
			//	For now set to be Black Welsh if not a child of Sooner
//			id_sheepbreedid = 1;
//		}		
		Log.i("after ", "Case statement Codon 171");
		id_sheepbreedid = 1;
		//	Set the location to be East Orchard Pasture but will need to modify to be real one based on location of dam
		id_locationid = 1;
		//	The following things should be modified to be the value from default settings 
		//	but I haven't implemented settings yet so hard coding them
		// TODO
		//	Set the birth_weight_units to be decimal pounds 
		birth_weight_units = 1;
		//	Set the owner_id to be Desert Weyr
		id_ownerid = 1;	
		//	Set the flock_prefix to be Desert Weyr 
		flock_prefix = 1;
		// Set the acquire reason to be Natural Addition
		acquire_reason = 1;
		// Set the lambing contact to be Garvin Mesa Address
		lambing_contact = 1;
		
		if (!stillborn) {
		// Set the management group based on sex of lamb
			// needs to change based on how the groups are set up hard coded for us
			if (sex == 1){management_group = 2;}
			if (sex == 2){management_group = 1;}
			if (sex == 4){management_group = 0;}
		} else {management_group = 0;}
		
		// Safer than relying on a null->int->string cast to work properly
		String remove_reasonString = "";
		if (remove_reason != -1) { remove_reasonString = String.format("%d", remove_reason);}
		// set breeder to Desert Weyr
		id_breederid = 1;
		// Fill empty data
		nsip_id = "";
		sale_price= "";
		sell_price_units= "";
		purchase_price= "";
		purchase_price_units= "";
		weaned_date= ""; 
		inbreeding= "";
		Log.i("add a lamb ", "before insert cmd ");
		//	Ready to build the insert statement for this lamb.
		cmd = String.format("insert into sheep_table (sheep_name, flock_prefix, sex, " +
			"birth_date, birth_time, birth_type, birth_weight, rear_type, death_date, remove_date, " +
			"remove_reason, lambease, sire_id, dam_id, alert01, acquire_date, sheep_birth_record, " +
			"codon171, codon154, codon136, id_sheepbreedid, id_locationid, " +
			"id_ownerid, birth_weight_units, acquire_reason, management_group," +
			"nsip_id, sale_price, sell_price_units, purchase_price, purchase_price_units, weaned_date, " +
			"inbreeding, id_breederid) values " +
			"('%s', %s, %s,'%s','%s',%s,%s,%s,'%s','%s','%s', %s, %s, %s,'%s','%s',%s,%s,%s,%s,%s,%s,%s,%s,%s,%s," +
			"'%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s') ",
			lamb_name, flock_prefix, sex, mytoday, mytime, birth_type, birth_weight, 
			rear_type, death_date, remove_date, remove_reasonString, lambease, sire_id, dam_id, 
			lamb_alert_text, mytoday, lamb_birth_record,
			lamb_codon171, lamb_codon154, lamb_codon136, id_sheepbreedid, id_locationid,
			id_ownerid,birth_weight_units, acquire_reason, management_group, 
			nsip_id, sale_price, sell_price_units, purchase_price, purchase_price_units, weaned_date, 
			inbreeding, id_breederid );
		
		Log.i("add a lamb ", "cmd is " + cmd);
		dbh.exec(cmd);
		Log.i("add a lamb ", "after insert ");
		//  now we have a sheep record for the lamb. 
		//	We need to get the sheep_id of this lamb for use in the birth record
		cmd = String.format("select last_insert_rowid()");
		crsr = dbh.exec( cmd );  
		cursor   = ( Cursor ) crsr;
  		dbh.moveToFirstRecord();
  		lamb_id = dbh.getInt(0);		
		Log.i("add a lamb ", "the lamb_id is " + String.valueOf(lamb_id));
		
		// Set the flock ID to be nothing initially
		tag_flock = 0;
		
		//	Go get all the tag data for this lamb
		Log.i("before ", "getting the lamb tag info");
  		tl = (TableLayout) findViewById(R.id.tag_table);
  		Log.i("in table ", "after get the table view");
  		int i;
  		for (i = 0; i < tl.getChildCount(); i++){
  			Log.i("in for loop ", "after start" + String.valueOf(tl.getChildCount()));
	  		TableRow tr = (TableRow) tl.getChildAt(i);
	  		Log.i("in table ", "after get the table row");
	  		temp_tag_num = (TextView) tr.getChildAt (0);
	  		temp_tag_color = (TextView) tr.getChildAt(1);
	  		temp_tag_loc = (TextView) tr.getChildAt(2);
	  		temp_tag_type = (TextView) tr.getChildAt(3);
	  		Log.i("in table ", "after get the children of that row values");
	  		//	Get what tag number this is
	  		tag_num = temp_tag_num.getText().toString();
	  		//	Get the color for the tag
	  		tag_color_label = temp_tag_color.getText().toString();
	  		Log.i("before ", "getting tag color looking for " + tag_color_label);
	  		cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
	     				"where tag_color_name='%s'", tag_color_label);
	     	crsr = dbh.exec( cmd );
	  		cursor   = ( Cursor ) crsr;
	  		dbh.moveToFirstRecord();
	  		tag_color = dbh.getInt(0);
	  		Log.i("after ", "getting tag color");
	  		//	Get the location for the tag
	  		tag_location_label = temp_tag_loc.getText().toString();
	  		Log.i("before ", "getting tag location looking for " + tag_location_label);
	  		cmd = String.format("select id_location_table.id_locationid, id_location_table.id_location_abbrev from id_location_table " +
				"where id_location_abbrev='%s'", tag_location_label);
	  		crsr = dbh.exec( cmd );
	  		cursor   = ( Cursor ) crsr;
	  		dbh.moveToFirstRecord();
	  		tag_loc = dbh.getInt(0);
	  		//	Get what tag type it is
	  		tag_type_label = temp_tag_type.getText().toString(); 		
	  		cmd = String.format("select id_type_table.id_typeid from id_type_table " +
				"where idtype_name='%s'", tag_type_label);
	  		crsr = dbh.exec( cmd );
	  		cursor   = ( Cursor ) crsr;
	  		dbh.moveToFirstRecord();
	  		tag_type = dbh.getInt(0);
	  		Log.i("after ", "getting tag type");
	  		
	  		// TODO
	  		//	If the tag is a federal tag then make the flock ID 1 for Desert Weyr 
	  		//	Should be whatever the default is in settings
	  		//	Also set the lamb name to be this year plus fed tag until we change it
	  		// 	once the EID is the federal tag the lamb name has to be the farm tag 
			//	Names cannot be the EID tag number, that is too long. 
			//	Still need to handle the case of the EID being the official federal tag	  		
	  		// 	Current plan is to add a field the database that denotes 
	  		//	whether this tag is an official governmental ID or not 0 for false or not official and 
	  		//	1 for true or yes is the official ID
	  		
	  		// Decided to try a switch statement here since the multiple if's are not working
	  		
	  		switch (tag_type){		
			case 1: //	Tag is a federal tag
				//	Set the flock to be the flock ID Should be a preference default not a fixed number
				lamb_name = year + "-" + tag_num;
				tag_flock = 1;
	  			cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male," +
		  				" tag_color_female, tag_location, tag_date_on, tag_number, id_flockid) values " +
		  				" (%s, %s, %s,%s,%s, '%s', '%s', %s) ", lamb_id, tag_type, tag_color, tag_color, 
		  				tag_loc, mytoday, tag_num, tag_flock);
		  		Log.i("add fed tag ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add fed tag ", "after insert into id_info_table");
				break;
			case 2:	//	Tag is an electronic tag 
					// test for USA official tag	
				tag_country_code = tag_num.substring(0, 3);
				Log.i("tag_country_code ", "is " + tag_country_code);
				if (tag_country_code.equals("840")){
					Log.i("official_id ", " supposed to be USA" );
					official_id = 1;
					}else 
					{official_id = 0;}
				cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male," +
		  				" tag_color_female, tag_location, tag_date_on, tag_number, official_id) values " +
		  				" (%s, %s, %s,%s,%s, '%s', '%s', %s) ", lamb_id, tag_type, tag_color, tag_color, 
		  				tag_loc, mytoday, tag_num, official_id);
		  		Log.i("add tag to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into id_info_table");
				break;
			case 3:	// Tag is a paint brand
		  		if (markewe){
		  			// We have a paint mark for the lamb and the checkbox for the ewe is true
		  			// Go see if there is a current active paint mark for the ewe
		  			try {
		  				cmd = String.format("select sheep_id from id_info_table where sheep_id = '%s' and " +
			  					"tag_type = 3 and tag_date_off is null", dam_id);
		  				// if command works we have a tag already so break out
		  				Log.i("in try ewe tag ", "db cmd is " + cmd);
		  				crsr2 = dbh.exec( cmd );  
		  				Log.i("in try ewe tag ", "after execute db command" + cmd);
		  				cursor2   = ( Cursor ) crsr2;
		  		  		dbh.moveToFirstRecord();
		  		  		temp_id = dbh.getInt(0);
		  		  		Log.i("in try ewe tag ", "after try to read a record");
		  			}
		  				 catch (Exception e) {
		  					//	No record found so insert one 
		  					Log.i("in catch ", " need to add a ewe tag");
		  					cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male," +
		  			  				" tag_color_female, tag_location, tag_date_on, tag_number) values " +
		  			  				" (%s, %s, %s,%s,%s, '%s', '%s') ", dam_id, tag_type, tag_color, tag_color, 
		  			  				tag_loc, mytoday, tag_num);
		  			  		Log.i("add ewe tag to ", "db cmd is " + cmd);
		  					dbh.exec(cmd);
		  					Log.i("add tag ", "after insert into id_info_table");		 
		  			} 	
		  		}
  				//	Add a lamb paint brand record for sure no matter what. 
  				cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male," +
  		  				" tag_color_female, tag_location, tag_date_on, tag_number) values " +
  		  				" (%s, %s, %s,%s,%s, '%s', '%s') ", lamb_id, tag_type, tag_color, tag_color, 
  		  				tag_loc, mytoday, tag_num);
  		  		Log.i("add tag to ", "db cmd is " + cmd);
  				dbh.exec(cmd);
  				Log.i("add tag ", "after insert into id_info_table");
		  		break;
			case 4:	// Tag is a farm tag
				//	Set the lamb name to be the farm tag plus the year of birth
				lamb_name = year + "-" + tag_num;
				cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male," +
		  				" tag_color_female, tag_location, tag_date_on, tag_number) values " +
		  				" (%s, %s, %s,%s,%s, '%s', '%s') ", lamb_id, tag_type, tag_color, tag_color, 
		  				tag_loc, mytoday, tag_num);
		  		Log.i("add tag to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into id_info_table");
				break;
			case 5:	// Tag is a tattoo
				cmd = String.format("insert into id_info_table (sheep_id, tag_type, tag_color_male," +
		  				" tag_color_female, tag_location, tag_date_on, tag_number) values " +
		  				" (%s, %s, %s,%s,%s, '%s', '%s') ", lamb_id, tag_type, tag_color, tag_color, 
		  				tag_loc, mytoday, tag_num);
		  		Log.i("add tag to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into id_info_table");
				break;
			case 6:	// Tag is a split
				cmd = String.format("insert into id_info_table (sheep_id, tag_type, " +
		  				"  tag_location, tag_date_on, tag_number) values " +
		  				" (%s, %s, %s, '%s', '%s') ", lamb_id, tag_type, tag_loc, mytoday, tag_num);
		  		Log.i("add tag to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into id_info_table");
				break;
			case 7:	// Tag is a notch
				cmd = String.format("insert into id_info_table (sheep_id, tag_type, " +
		  				"  tag_location, tag_date_on, tag_number) values " +
		  				" (%s, %s, %s, '%s', '%s') ", lamb_id, tag_type, tag_loc, mytoday, tag_num);
		  		Log.i("add tag to ", "db cmd is " + cmd);
				dbh.exec(cmd);
				Log.i("add tag ", "after insert into id_info_table");
				break;
	  		
	  		}
  		}
		//	End of what has to loop through all IDs for the lamb being added 
		
		//	Now update the sheep record with the new sheep name
		cmd = String.format("update sheep_table set sheep_name = '%s' " +
  		  		" where sheep_id = %s ", lamb_name, lamb_id);
		Log.i("add sheep_name ", "to db cmd is " + cmd);
		dbh.exec(cmd);
		Log.i("add name ", "after update into sheep_table");
  		
		//	Create a lambing history record by first seeing if there is one already for this year
		//	If so then update the existing one
		//	If not then insert a new one
		
		// Make the year string able to be used in a query to get this year's lambing records. 
		year = year + "%";		
		Log.i("before try block ", " lambing year is " + year);
		
		try { //	We have a record so need to add this lamb at the end and update the lambing_notes
			cmd = String.format("select * from lambing_history_table where lambing_history_table.dam_id = %s and " +
					" lambing_history_table.lambing_date like '%s' ", dam_id, year);
			Log.i("in try block ", " cmd is " + cmd);
			crsr = dbh.exec( cmd );
//			Log.i("in try block ", "after try the first DB select ");
			cursor   = ( Cursor ) crsr;
//			Log.i("in try block ", "after cursor ");
	  		dbh.moveToFirstRecord();
//	  		Log.i("in try block ", "after move to first ");
	  		lambing_historyid = dbh.getInt(0);
//	  		Log.i("in try block ", "after get first variable ");
	  		lambing_date = dbh.getStr(1);
	  		lambing_time = dbh.getStr(10);
	  		lambing_notes = dbh.getStr(4);
	  		lambs_born = dbh.getInt(5);
	  		lamb01_id = dbh.getInt(7);
	  		Log.i("in try block ", "after get lamb01_id " + String.valueOf(lamb01_id));
	  		lamb02_id = dbh.getInt(8);
	  		Log.i("in try block ", "after get lamb02_id " + String.valueOf(lamb02_id));
	  		lamb03_id = dbh.getInt(9);
	  		Log.i("in try block ", "after get lamb03_id " + String.valueOf(lamb03_id));
	  		//	First add this lamb as the next in the lambing notes field
	  		if (stillborn){
	  			lambing_notes = lambing_notes + "S"; 
	  		}else{
	  			lambing_notes = lambing_notes + sex_abbrev;
	  		
	  			Log.i("update ", "rear_type " + String.valueOf(rear_type));
	  		}
	  		Log.i("add a lamb ", "the lambing_notes are " + lambing_notes);	  		
	  		// Then update the record by adding this lambs' ID in the next slot
	  		//	presumes we have one lamb in there already so the new one is either lamb02 or lamb03
	  		if (lamb02_id != 0 ){
	  			//	have 2 lambs already
	  			Log.i("in if stmt ", "lamb02_id " + String.valueOf(lamb02_id));
	  			Log.i("in try block ", " have 2 lambs so add a third to record");
//	  			Update the lambs born
	  			lambs_born = lambs_born +1;
	  			cmd = String.format("update lambing_history_table set " +
		  				"lambing_notes = '%s', lambs_born = %s, " +
		  				" lamb03_id = %s " +
		  				"where lambing_historyid = %s",
		  				lambing_notes, lambs_born,  lamb_id, lambing_historyid);
	  			Log.i("in try block ", " cmd is " + cmd);
	  			dbh.exec( cmd );
	  			Log.i("in try block ", " after update with third lamb");
				//	Now need to go back and update this birth record reference to the lamb records
	  			cmd = String.format("update sheep_table set sheep_birth_record = %s," +
						"birth_type = %s, rear_type = %s " +
		  		  		" where sheep_id = %s ", lambing_historyid, lambs_born, rear_type, lamb_id);
		  		dbh.exec( cmd ); 
		  		Log.i("in try block ", " after update sheep record  for third lamb to add birth record");
		  		cmd = String.format("update sheep_table set sheep_birth_record = %s," +
						"birth_type = %s " +
		  		  		" where sheep_id = %s ", lambing_historyid, lambs_born, lamb02_id);
		  		Log.i("in try block ", " cmd is " + cmd);
		  		dbh.exec( cmd ); 
		  		Log.i("in try block ", " after update sheep record for second lamb to add birth record");
		  		cmd = String.format("update sheep_table set sheep_birth_record = %s," +
						"birth_type = %s " +
		  		  		" where sheep_id = %s ", lambing_historyid, lambs_born, lamb01_id);
		  		Log.i("in try block ", " cmd is " + cmd);
		  		dbh.exec( cmd ); 
		  		Log.i("in try block ", " after update sheep record for first lamb to add birth record");
				//	done with this lamb so force back to the ewe screen
				backBtn(v);
	  		}
	  		else{
	  			//	only have 1 lamb so far
	  			Log.i("in try block ", " have only 1 lamb so add a second to record");
	  			//	Update the lambs born and lambs weaned fields
	  			lambs_born = lambs_born +1;
	  			cmd = String.format("update lambing_history_table set " +
		  				"lambing_notes = '%s', lambs_born = %s, " +
		  				" lamb02_id = %s " +
		  				"where lambing_historyid = %s",
		  				lambing_notes, lambs_born, lamb_id, lambing_historyid);
	  			Log.i("in try block ", " cmd is " + cmd);
	  			dbh.exec( cmd );
	  			Log.i("in try block ", " after update of second lamb");
				//	Now need to go back and add this birth record reference to the lamb record
	  			cmd = String.format("update sheep_table set sheep_birth_record = %s," +
						"birth_type = %s, rear_type = %s " +
		  		  		" where sheep_id = %s ", lambing_historyid, lambs_born, rear_type, lamb_id);
		  		Log.i("in try block ", " cmd is " + cmd);
		  		dbh.exec( cmd ); 
		  		Log.i("in try block ", " after update sheep record to add birth record");
		  		cmd = String.format("update sheep_table set sheep_birth_record = %s," +
						"birth_type = %s, rear_type = %s " +
		  		  		" where sheep_id = %s ", lambing_historyid, lambs_born, rear_type,lamb01_id);
		  		Log.i("in try block ", " cmd is " + cmd);
		  		dbh.exec( cmd ); 
		  		Log.i("in try block ", " after update sheep record for first lamb to correct birth type");
	  			
				//	done with this lamb so force back to the ewe screen
				backBtn(v);
				}
//	  		Log.i("after if ", " after end of if statement for more lambs ");
		} catch (Exception e) {
			//	No record found so insert one
			lambing_date = mytoday;
			lambing_time = mytime;
//			Update the lambs born and rear type fields
  			lambs_born = lambs_born +1;
//			Log.i("in catch block ", " after setting date and time");
  			if (stillborn){
	  			lambing_notes = "S"; 
	  		}else{
	  			lambing_notes = sex_abbrev;
	  		}
  			Log.i("in catch block ", " after setting lambing_notes " + lambing_notes);
			cmd = String.format("insert into lambing_history_table (lambing_date, dam_id, sire_id, " +
			"lambing_notes, lambs_born, lamb01_id, lambing_time, gestation_length, lambing_contact) " +
			"values ('%s', %s, %s,'%s', %s, %s, '%s', %s, %s) ", 
			mytoday, dam_id, sire_id, lambing_notes, lambs_born, lamb_id, mytime, real_gestation_length,lambing_contact);
			Log.i("in catch block ", " cmd is " + cmd);
			dbh.exec( cmd );
			Log.i("in catch block ", "after cmd to create a new record");
			cmd = String.format("select last_insert_rowid()");
			crsr = dbh.exec( cmd );  
			cursor   = ( Cursor ) crsr;
	  		dbh.moveToFirstRecord();
	  		lambing_historyid = dbh.getInt(0);		
			Log.i("add a lamb ", "the lambing_historyid is " + String.valueOf(lambing_historyid));
			//	Now need to go back and add this birth record reference to the lamb record
			cmd = String.format("update sheep_table set sheep_birth_record = %s," +
					"birth_type = %s, rear_type = %s " +
	  		  		" where sheep_id = %s ", lambing_historyid, lambs_born, rear_type, lamb_id);
	  		Log.i("in try block ", " cmd is " + cmd);
	  		dbh.exec( cmd ); 
			//	done with this lamb so force back to the ewe screen
			backBtn(v);
		}
		}
		// Enable Update Database button and make it normal 
    	btn = (Button) findViewById( R.id.update_database_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(true);
    	
    }
    public void addNewTag( View v ){
    	Object crsr;   	
       	btn = (Button) findViewById( R.id.update_display_btn );
    	btn.setEnabled(true); 
    	new_tag_number = null;
       	// Fill the Tag Type Spinner
     	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
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
		tag_type_spinner2.setAdapter (dataAdapter);
		tag_type_spinner2.setSelection(4);	
    	
    	// Fill the Tag Color Spinner
    	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
     	tag_colors = new ArrayList<String>();       	
        // Select All fields from tag colors to build the spinner
        cmd = "select * from tag_colors_table";
        crsr = dbh.exec( cmd );  
        cursor   = ( Cursor ) crsr;
//        startManagingCursor(cursor);
    	dbh.moveToFirstRecord();
    	tag_colors.add("Select a Color");
         // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		tag_colors.add(cursor.getString(2));
    	}
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_colors);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_color_spinner.setAdapter (dataAdapter);
		//	TODO
		//	Set the tag color to be the default should be from settings but not implemented yet.
		tag_color_spinner.setSelection(5); // set to orange for non-EID tags
				
    	// Fill the Tag Location Spinner
		tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
		tag_locations = new ArrayList<String>();        
		tag_locations.add("Select a Location");		
        // Select All fields from tag locations to build the spinner
        cmd = "select * from id_location_table";
        crsr = dbh.exec( cmd );  
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	
         // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		tag_locations.add(cursor.getString(2));
    	}
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_locations);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_location_spinner.setAdapter (dataAdapter);
		//	TODO
		//	Set the tag location to be the left ear by default should be from settings
		tag_location_spinner.setSelection(2); // left ear for non EID tags   
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
//	  	  	Log.i("addradiobuttons", radioBtnText[i]);
	  	  	radioBtn.setId(i);

	  	    //add it to the group.
	  	    radioGroup.addView(radioBtn, i);
	  	  }
	  	}        
    public void helpBtn( View v )
    {
   	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_add_lamb )
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
    
    // user clicked the 'back' button
    public void backBtn( View v )
	    {
    	doUnbindService();
		stopService(new Intent(AddLamb.this, eidService.class)); 
		Log.i("in add lamb", " in back btn after stop EID service");
		clearBtn( null );
		// Added this to close the database if we go back to the lambing activity  	
    	cursor.close();
    	Log.i("in add lamb", " in back btn after close cursor");
    	dbh.closeDB();   	
    	//Go back to ewe lambing data
    	Log.i("in add lamb", " before finish this activity");
      	finish();
      	Log.i("in add lamb", " after finish this activity");
	    }
 
    public void showAlert (View v){    		
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
    	}
   
    // user clicked 'clear' button
    public void clearBtn( View v )
	    {
		TextView TV ;
		RadioGroup rg;
		CheckBox cb;
		TableLayout tl;
  		
		TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
		//	Clear the radio group checks
		Log.i("in add lamb", "in clear button ready to clear the radio groups "); 
		rg=(RadioGroup)findViewById(R.id.radioRearType);
		rg.clearCheck();
		rg=(RadioGroup)findViewById(R.id.radioGroupSex);
		rg.clearCheck();
		Log.i("in add lamb", " in clear btn resetting the stillborn checkbox");
		//	Clear the stillborn box
		cb=(CheckBox)findViewById(R.id.checkBoxStillborn);
		cb.setChecked(false);
		TV = (TextView) findViewById( R.id.birth_weight);
		TV.setText( "" );
		lamb_ease_spinner = (Spinner) findViewById(R.id.lamb_ease_spinner);
		lamb_ease_spinner.setSelection(0);
     	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
      	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
      	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
      	TV  = (TextView) findViewById( R.id.new_tag_number);
      	tag_type_spinner2.setSelection(0);
      	tag_color_spinner.setSelection(0);
      	tag_location_spinner.setSelection(0);
      	TV.setText( "" );	
      	tl = (TableLayout) findViewById(R.id.tag_table);
      	tl.removeAllViews();
      	Log.i("in add lamb", " in clear btn after removing the table views");
    }  
    public void doNote( View v )
    {	 
    	Utilities.takeNote(v, thissheep_id, this);
    }

//   user clicked 'Scan' button    
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
 
  public void updateTag( View v ){
  	Object 			crsr;
  	String 			cmd;
  	TextView 		TV;
  	Boolean 	tagok;
  	//	Initially set tag to not ok until we verify tag entry is good
  	tagok = false;
  	// Get the data from the add tag section of the screen
  	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
  	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
  	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
  	
  	tag_type_label = tag_type_spinner2.getSelectedItem().toString();
  	Log.i("updateTag", "Tag type is " + tag_type_label);
  	tag_color_label = tag_color_spinner.getSelectedItem().toString();
  	Log.i("updateTag", "Tag color is " + tag_color_label);
  	tag_location_label = tag_location_spinner.getSelectedItem().toString();
  	Log.i("updateTag", "Tag location is " + tag_location_label);
  	
  	TV  = (TextView) findViewById( R.id.new_tag_number);
  	new_tag_number = TV.getText().toString();
  	Log.i("before if", " new tag number " + new_tag_number);    	
   	if (tag_type_label == "Select a Type" || tag_location_label == "Select a Location" || tag_color_label == "Select a Color"
  			|| TV.getText().toString().isEmpty()) {
  		new_tag_type = 0;
  		// Missing data so  display an alert 	
  		AlertDialog.Builder builder = new AlertDialog.Builder( this );
  		builder.setMessage( R.string.convert_fill_fields )
  	           .setTitle( R.string.enter_tag_data );
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
  		Log.i("before ", "getting tag type looking for " + tag_type_label);
  		cmd = String.format("select id_type_table.id_typeid from id_type_table " +
			"where idtype_name='%s'", tag_type_label);
  		crsr = dbh.exec( cmd );
  		cursor   = ( Cursor ) crsr;
  		dbh.moveToFirstRecord();
  		new_tag_type = dbh.getInt(0);
  		Log.i("after ", "getting tag type" + String.valueOf(new_tag_type));  		
     	
  		Log.i("before ", "getting tag color looking for " + tag_color_label);
  		cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
     				"where tag_color_name='%s'", tag_color_label);
     	crsr = dbh.exec( cmd );
  		cursor   = ( Cursor ) crsr;
  		dbh.moveToFirstRecord();
  		new_tag_color = dbh.getInt(0);
  		Log.i("after ", "getting tag color" + String.valueOf(new_tag_color));
  		
  		Log.i("before ", "getting tag location looking for " + tag_location_label);
  		cmd = String.format("select id_location_table.id_locationid, id_location_table.id_location_abbrev from id_location_table " +
			"where id_location_abbrev='%s'", tag_location_label);
  		crsr = dbh.exec( cmd );
  		cursor   = ( Cursor ) crsr;
  		dbh.moveToFirstRecord();
  		new_tag_location = dbh.getInt(0);
  		Log.i("New Location ID ", String.valueOf(new_tag_location));
   		tag_location_label = dbh.getStr(1);
  		Log.i("New Location ", tag_location_label);
  		
  		//	Need to test if the type is an ear tag and the location is not an ear then must request change
  		switch (new_tag_type){		
		case 1:
		case 2:
		case 4:		
		case 6:
		case 7:
			// Tag Type is Federal, Electronic, Farm, Split or Notch so require an ear location
			switch (new_tag_location){
			case 1:
			case 2:
				// Ear locations so ok
				tagok = true;
				break;
			case 3:
			case 4:
			case 5:
				// flank or side not allowed for tags, split or notches
				tagok = false;
		  		break;
			}
			break;
		case 3:
			// paint brand only location allowed is side
			switch (new_tag_location){
			case 1:
			case 2:
			case 3:
			case 4:
				// Ear and flank locations not allowed
				tagok = false;
		  		break;
			case 5:
				// side so ok
				tagok = true;
		  		break;
			}
			break;
		case 5:
			// tattoo so all except side
			switch (new_tag_location){
			case 1:
			case 2:
			case 3:
			case 4:
				// Ear and flank locations ok
				tagok = true;
		  		break;
			case 5:
				// side not allowed
				tagok = false;
		  		break;
			}
			break;
  		}
  		if (tagok) {
  			// all tag data is ok so can put in the table
  	  		Log.i("Before tag ", "Before creating the tag table layout");
  	  		TableLayout tl;
  	  		TableRow tr;
  	  		tl = (TableLayout) findViewById(R.id.tag_table);
  	  		tr = new TableRow (this);

  	  		TV = new TextView(this);
  	   		TV.setText (new_tag_number);
  	   		TV.setWidth(200);
  	   		tr.addView(TV);

  	   		Log.i("Before ", "setting next text view TV2");  		
  	   		TV = new TextView(this);
  	  		TV.setText (tag_color_label);
  	  		TV.setWidth(75);
  	  		tr.addView(TV); 		
  	 
  	   		TV = new TextView(this);
  	   		TV.setText (tag_location_label);
  	   		TV.setWidth(50);
  	  		tr.addView(TV);

  	  		TV = new TextView(this);
  	  		TV.setText (tag_type_label); 
  	  		TV.setWidth(90);
  	  		tr.addView(TV);
  	 
  	  		tl.addView(tr);
  	  		Log.i("after tag ", "after creating the tag table layout");
  	  		if ((new_tag_type == 1) || (new_tag_type == 4)){
  	  			//	tag type is either Federal or Farm so set defaults to be paint, yellow on the side
  	  			//	TODO should be set via preferences or settings
  	 	      	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
  	  	      	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
  	  	      	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
  	  	      	TV  = (TextView) findViewById( R.id.new_tag_number);
  	  	      	tag_type_spinner2.setSelection(3);	// Paint type
  	  	      	tag_color_spinner.setSelection(1);	//	Yellow
  	  	      	tag_location_spinner.setSelection(5);	// Side 
  	  	      	TV.setText( "" ); 	  			
  	  		}else{
  	  			//	tag type is something else like tattoo or paint so set defaults for a farm tag
  	  			//	TODO
  	  			//	Needs to be fixed to be defaults or settings. 
	   	      	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
	  	      	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
	  	      	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
	  	      	TV  = (TextView) findViewById( R.id.new_tag_number);
	  	      	tag_type_spinner2.setSelection(4);	// Farm type
	  	      	tag_color_spinner.setSelection(5);	//	Orange color
	  	      	tag_location_spinner.setSelection(2);	//	Left ear
	  	      	TV.setText( "" );
  	  		}
  		}else{
  			//	bad tag location to ask for a new one
	  		AlertDialog.Builder builder = new AlertDialog.Builder( this );
	  		builder.setMessage( R.string.wrong_id_location )
	  	           .setTitle( R.string.enter_tag_data );
	  		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	  	           public void onClick(DialogInterface dialog, int idx) {
	  	               // User clicked OK button 
	   	    		   return;
	  	               }
	  	       });		
	  		AlertDialog dialog = builder.create();
	  		dialog.show();
  			
  		}
  	
      	}
   	}
}
