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
	public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner, lamb_ease_spinner;
	public List<String> tag_types, tag_locations, tag_colors, lambing_ease;
	ArrayAdapter<String> dataAdapter;
	public int 		thissheep_id;
	public RadioGroup radioGroup;
	
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
   	cursor.close();    	
   	
   	// Creating adapter for spinner
   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		tag_type_spinner.setAdapter (dataAdapter);
		tag_type_spinner.setSelection(2);	

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
	
   	//	Fill the lamb sex radio group
   		radiobtnlist = new ArrayList();  
   		radiobtnlist.add ("Ram");
   		radiobtnlist.add ("Ewe");
   		radiobtnlist.add ("Unknown");
	    radioBtnText = (String[]) radiobtnlist.toArray(new String [radiobtnlist.size()]);
		cursor.close();  
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
     	cursor.close();  
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
    }
    public void updateDatabase( View v ){

    	
    	
    	
    }
    public void addNewTag( View v ){
    	
   
	//	Get set up to try to use the CursorAdapter to display all the tag data
	//	Select only the columns I need for the tag display section
//    String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
//	Log.i("LookForSheep", "after setting string array fromColumns");
//	//	Set the views for each column for each line. A tag takes up 1 line on the screen
//    int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
//    Log.i("LookForSheep", "after setting string array toViews");
//    myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor ,fromColumns, toViews, 0);
//    Log.i("LookForSheep", "after setting myadapter");
//    setListAdapter(myadapter);
//    Log.i("LookForSheep", "after setting list adapter");
    
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

}
