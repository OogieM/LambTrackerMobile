package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.weyr_associates.lambtracker.LookUpSheep.IncomingHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AddLamb extends Activity {
	private DatabaseHandler dbh;
	String     	cmd;
	Integer 	i;	
	public Button btn;
	public String alert_text;
	public Cursor 	cursor;
	public Spinner tag_type_spinner, tag_type_spinner2, tag_location_spinner, tag_color_spinner, lamb_ease_spinner;
	public List<String> tag_types, tag_locations, tag_colors, lambing_ease;
	ArrayAdapter<String> dataAdapter;
	public int 		thissheep_id;
	public int	rear_type, birth_type, sex, lambease, codon171, codon136, codon154;
	public String dam_name;
	public int dam_id, dam_codon171, dam_codon154, dam_codon136;
	public int sire_id, sire_codon171, sire_codon154, sire_codon136;
	public CheckBox 	stillbornbox;
	public boolean stillborn;
	public Float birth_weight;
	public RadioGroup radioGroup;
	public String mytoday;
	public String mytime;
	
	int             fedtagid, farmtagid, eidtagid ; // These are record IDs not sheep IDs
	public int new_tag_type, new_tag_color, new_tag_location;
	
	public String tag_type_label, tag_color_label, tag_location_label, new_tag_number, eid_tag_color_label ;
	public String eid_tag_location_label, eidText;
	public Spinner eid_tag_color_spinner, eid_tag_location_spinner;
	
	
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
//		View v = null;
		Object crsr;
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
        setContentView(R.layout.add_lamb);
        Log.i("AddLamb", " after set content view");
        View v = null;
        String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("AddLamb", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
    	Object crsr;
    	ArrayList radiobtnlist;
    	String[] radioBtnText;
       	Boolean			exists;
       	TextView TV;
       	String temp_ram_in, temp_ram_out;

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
//   	cursor.close();    	
   	
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
	
   	//	Fill the lamb sex radio group
   		radiobtnlist = new ArrayList();  
   		radiobtnlist.add ("Ram");
   		radiobtnlist.add ("Ewe");
   		radiobtnlist.add ("Unknown");
	    radioBtnText = (String[]) radiobtnlist.toArray(new String [radiobtnlist.size()]);
//		cursor.close();  
		// Build the radio buttons here
		radioGroup = ((RadioGroup) findViewById(R.id.radioGroupSex));
		addRadioButtons(3, radioBtnText);
		radiobtnlist.clear ();
		
	//	Fill the birth and rear type radio group
//   		radiobtnlist = new ArrayList();  
   		radiobtnlist.add ("Single");
   		radiobtnlist.add ("Twin");
   		radiobtnlist.add ("Triplet");
	    radioBtnText = (String[]) radiobtnlist.toArray(new String [radiobtnlist.size()]);
		 
		// Build the radio buttons here
		radioGroup = ((RadioGroup) findViewById(R.id.radioBirthType));
		addRadioButtons(3, radioBtnText);
		//	Fill the rear type radio group   		
//	    radioBtnText = (String[]) radiobtnlist.toArray(new String [radiobtnlist.size()]);
//		cursor.close();  
		// Build the radio buttons here
		radioGroup = ((RadioGroup) findViewById(R.id.radioRearType));
		addRadioButtons(3, radioBtnText);
		radiobtnlist.clear ();
		
    	//	Get the text for the lamb ease buttons  
		lamb_ease_spinner = (Spinner) findViewById(R.id.lamb_ease_spinner);
    	cmd = String.format("select custom_evaluation_traits_table.custom_evaluation_item " +
    			" from custom_evaluation_traits_table " +
    			" where custom_evaluation_traits_table.id_traitid = '%s' "+
    			" order by custom_evaluation_traits_table.custom_evaluation_order ASC ", 24);
//    	Log.i("evaluate2", " cmd is " + cmd);	    	
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        lambing_ease = new ArrayList<String>();
        lambing_ease.add("Lambing Ease");
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
        	lambing_ease.add (cursor.getString(0));
	    	Log.i("addlamb", " Lambing ease text is " + cursor.getString(0));
    	}        
//     	cursor.close();  
    	// Creating adapter for spinner
     	Log.i("addlamb", " before create lambing ease adapter" );    	
     	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, lambing_ease);
     	Log.i("addlamb", " after create lambing ease adapter" );
     	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     	Log.i("addlamb", " after set resource" );
     	lamb_ease_spinner.setAdapter (dataAdapter);
     	Log.i("addlamb", " after set adapter" );
		lamb_ease_spinner.setSelection(0);
		Log.i("addlamb", " after set selection" );
		Bundle extras = getIntent().getExtras();
		// TODO get extras here
		if (extras!= null){
			dam_id = extras.getInt("dam_id");
			dam_name = extras.getString("dam_name");
			TV = (TextView) findViewById( R.id.damName );
            TV.setText(dam_name); 
            dam_codon171 = extras.getInt("codon171");
            dam_codon154 = extras.getInt("codon154");
            dam_codon136 = extras.getInt("codon136");
		}
		//	Now need to figure out who the sire is based on date and breeding records.
		//	First go get the breeding records for this ewe.
		cmd = String.format("select sheep_breeding_table.ewe_id, " +
				" sheep_breeding_table.breeding_id, " +
				" breeding_record_table.ram_id, " +
				" breeding_record_table.date_ram_in, " +
    			" breeding_record_table.date_ram_out,  " +
    			" breeding_record_table.service_type " +
    			" from breeding_record_table " +
    			" left outer join sheep_breeding_table on " +
    			" sheep_breeding_table.breeding_id = breeding_record_table.id_breedingid " +
    			" where sheep_breeding_table.ewe_id = '%s' ", dam_id);
		//	TODO
		//		Get the date and time to add to the record
			mytoday = TodayIs();
			mytime = TimeIs();
		
    	Log.i("add a lamb ", " cmd is " + cmd);	    	
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
        	Log.i("addlamb", " in for loop checking breeding dates ");
        	// Check the dates and see if this is the right record
        	// Get the date ram in and date ram out
        	temp_ram_in = dbh.getStr(3);
        	temp_ram_out = dbh.getStr(4);
        	// mytoday is the current date
        	// need to figure out if the date is within early date 142 probable start date 147 
        	//	probable end date 150 end date 155
        	
        	
        	//  If it is within the dates then this is the record need  to save the data
        	//	and break out of the for loop
        	
	    	
    	}        
		//	Handle the sire data here
        
        
    }
    public void updateDatabase( View v ){
    	RadioGroup rg;
    	TextView 		TV;
		// Disable Update Database button and make it red to prevent getting 2 records at one time
    	btn = (Button) findViewById( R.id.update_database_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	btn.setEnabled(false);   	
    	//	Get the data for this lamb
    	
   		//	Get the radio group selected for the birth type
		Log.i("before radio group", " getting ready to get the birth type ");
		rg=(RadioGroup)findViewById(R.id.radioBirthType);
 		birth_type = rg.getCheckedRadioButtonId();
		Log.i("birth_type ", String.valueOf(birth_type));
 		
   		//	Get the radio group selected for the rear type
		Log.i("before radio group", " getting ready to get the rear type ");
		rg=(RadioGroup)findViewById(R.id.radioRearType);
 		rear_type = rg.getCheckedRadioButtonId();
		Log.i("rear_type ", String.valueOf(rear_type));
		
  		//	Get the radio group selected for the sex
		Log.i("before radio group", " getting ready to get the sex ");
		rg=(RadioGroup)findViewById(R.id.radioGroupSex);
 		sex = rg.getCheckedRadioButtonId();
		Log.i("sex ", String.valueOf(sex));
		
		//	Get the value of the checkbox for stillborn
		Log.i("before checkbox", " getting ready to get stillborn or not ");
		stillbornbox = (CheckBox) findViewById(R.id.checkBoxStillborn);
		if (stillbornbox.isChecked()){
			stillborn = true;
			Log.i("stillborn ", String.valueOf(stillborn));
		}
		//	Get the Birth Weight
		Log.i("before weight", " getting ready to get birth weight ");
		TV = (TextView) findViewById(R.id.birth_weight);
		birth_weight = Float.valueOf(TV.getText().toString());
		Log.i("birth_weight ", String.valueOf(birth_weight));
		
		//	Get the lambease score
		Log.i("before lambease", " getting ready to get lambease ");
		TV = (TextView) findViewById(R.id.lamb_ease_spinner);
		lambease = lamb_ease_spinner.getSelectedItemPosition();
		Log.i("lambease ", String.valueOf(lambease));
		
		//	Calculate codon171 based on sire and dam if possible
		
		//	If the Codon171 cannot be determined set an alert for this lamb that it needs scrapie blood
		
		
		
		
		
    	
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
		tag_type_spinner2.setSelection(1);	
    	
    	// Fill the Tag Color Spinner
    	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
     	tag_colors = new ArrayList<String>();       	
        // Select All fields from tag colors to build the spinner
        cmd = "select * from tag_colors_table";
        crsr = dbh.exec( cmd );  
        cursor   = ( Cursor ) crsr;
        startManagingCursor(cursor);
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
		tag_color_spinner.setSelection(5);
				
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
		tag_location_spinner.setSelection(2);
    
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
    	// Added this to close the database if we go back to the main activity  	
    	stopManagingCursor (cursor);
    	cursor.close();
    	dbh.closeDB();
    	clearBtn( null );
    	//Go back to lambing data
      	finish();
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
		TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
		//	Need to clear out the rest of the data here but only if we've actually got some.
		try {
//	clear out as required here
			}
		catch (Exception e) {
			// In this case there is no adapter so do nothing
		}		
    }  
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
//			int hour = cal.get(Calendar.HOUR);
	        //24 hour format
			int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			  
			return Make2Digits(hourofday) + ":" + Make2Digits(minute) + ":" + Make2Digits(second) ;
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
  // TODO
  public void updateTag( View v ){
  	Object 			crsr;
  	String 			cmd;
  	TextView 		TV;
  	// Get the data from the add tag section of the screen
  	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
  	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
  	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
  	
  	tag_type_label = tag_type_spinner2.getSelectedItem().toString();
//  	Log.i("updateTag", "Tag type is " + tag_type_label);
  	tag_color_label = tag_color_spinner.getSelectedItem().toString();
//  	Log.i("updateTag", "Tag color is " + tag_color_label);
  	tag_location_label = tag_location_spinner.getSelectedItem().toString();
//  	Log.i("updateTag", "Tag location is " + tag_location_label);
  	
  	TV  = (TextView) findViewById( R.id.new_tag_number);
  	new_tag_number = TV.getText().toString();
//  	Log.i("before if", " new tag number " + new_tag_number);    	
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
  		startManagingCursor(cursor);
  		dbh.moveToFirstRecord();
  		new_tag_type = dbh.getInt(0);
//  		cursor.close();
  		
     		cmd = String.format("select tag_colors_table.tag_colorsid from tag_colors_table " +
     				"where tag_color_name='%s'", tag_color_label);
     	    crsr = dbh.exec( cmd );
  		cursor   = ( Cursor ) crsr;
  		startManagingCursor(cursor);
  		dbh.moveToFirstRecord();
  		new_tag_color = dbh.getInt(0);
//  		cursor.close();

  		cmd = String.format("select id_location_table.id_locationid, id_location_table.id_location_abbrev from id_location_table " +
			"where id_location_name='%s'", tag_location_label);
  		crsr = dbh.exec( cmd );
  		cursor   = ( Cursor ) crsr;
  		startManagingCursor(cursor);
  		dbh.moveToFirstRecord();
  		new_tag_location = dbh.getInt(0);
//  		Log.i("New Location ID ", String.valueOf(new_tag_location));
   		tag_location_label = dbh.getStr(1);
//  		Log.i("New Location ", tag_location_label);
//  		cursor.close();
  		
  	   	// 	Fill the new tag data with where it is in the screen display
      	//	Integers to hold the info new_tag_type, new_tag_color, new_tag_location
  		
      	if (new_tag_type == 1){
      		//	Federal Tag so update list and set needs database update
      		// 	by setting id of 0 meaning either no tag or needs update
      		Log.i("in if", "Got a new federal tag type");
      		// Need to figure out how to update the cursor adapter for the tag data here
//      	    TV  = (TextView) findViewById( R.id.fedText );
//      	    TV.setText(new_tag_number);
//      	    TV = (TextView) findViewById( R.id.fed_colorText );
//      	    TV.setText(tag_color_label);
//      	    TV = (TextView) findViewById( R.id.fed_locationText );
//      	    TV.setText(tag_location_label);
      	    fedtagid = 0;
       	}
      	if (new_tag_type == 4){
      		//	Farm Tag so update farm section and set needs database update
      		//	by setting id of 0 meaning either no tag or needs update       		
      		Log.i("in if", "Got a new farm tag type");
      		// Need to figure out how to update the cursor adapter for the tag data here
//      	    TV  = (TextView) findViewById( R.id.farmText );
//      	    TV.setText(new_tag_number);
//      	    TV = (TextView) findViewById( R.id.farm_colorText );
//      	    TV.setText(tag_color_label);
//      	    TV = (TextView) findViewById( R.id.farm_locationText );
//      	    TV.setText(tag_location_label);
      	    farmtagid = 0;
      	}
      	//	Clear out the add tag section    	
      	tag_type_spinner2 = (Spinner) findViewById(R.id.tag_type_spinner2);
      	tag_color_spinner = (Spinner) findViewById(R.id.tag_color_spinner);
      	tag_location_spinner = (Spinner) findViewById(R.id.tag_location_spinner);
      	TV  = (TextView) findViewById( R.id.new_tag_number);
      	tag_type_spinner2.setSelection(1);
      	tag_color_spinner.setSelection(5);
      	tag_location_spinner.setSelection(2);
      	TV.setText( "" );
      	}
   	}
}
