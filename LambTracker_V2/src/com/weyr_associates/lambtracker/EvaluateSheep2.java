package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.weyr_associates.lambtracker.EvaluateSheep.IncomingHandler;

import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.database.Cursor;

public class EvaluateSheep2 extends Activity {
	
	public Button button;
	
	String     	cmd, tempText;
	String		tempLabel;
	String[] radioBtnText;
	Integer 	i;
	
	public int trait01, trait02, trait03, trait04, trait05, trait06, trait07, trait08, trait09, trait10;
	public int trait11, trait12, trait13, trait14, trait15, trait16, trait17, trait18, trait19, trait20;
	public int trait11_unitid, trait12_unitid, trait13_unitid, trait14_unitid, trait15_unitid;
	public String trait01_label, trait02_label, trait03_label, trait04_label, trait05_label, trait06_label, 
		trait07_label, trait08_label, trait09_label, trait10_label, trait11_label, trait12_label, 
		trait13_label, trait14_label, trait15_label ; 
	public String trait11_units, trait12_units, trait13_units, trait14_units, trait15_units; 
	
	public int sheep_id, thissheep_id;
	public RadioGroup radioGroup;
	int 		id;
	int   		fedtagid, farmtagid, eidtagid;
	private int			    recNo;
	public int             nRecs, nRecs2, nRecs3, nRecs4;
	List<Integer> which_traits;
	List<Float> real_scores;
	public List<String> scored_evaluation_traits, data_evaluation_traits, trait_units, user_evaluation_traits;
	public List <Integer> scored_trait_numbers, data_trait_numbers, user_trait_numbers, user_trait_number_items;
//	public List <Integer> eval_trait_numbers;
	
	
	ArrayAdapter<String> dataAdapter;

	public Float trait01_data, trait02_data, trait03_data, trait04_data, trait05_data, trait06_data, trait07_data ;
	public Float trait08_data, trait09_data, trait10_data;
	public Float trait11_data, trait12_data, trait13_data, trait14_data, trait15_data;
	public int trait16_data, trait17_data, trait18_data, trait19_data, trait20_data;
	
	private DatabaseHandler dbh;
	private Cursor 	cursor;

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
				stopService(new Intent(EvaluateSheep2.this, eidService.class));
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
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

				//Request a status update.
//				msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
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
//		Log.i("Evaluate", "At isRunning?.");
		if (eidService.isRunning()) {
//			Log.i("Evaluate", "is.");
			doBindService();
		} else {
//			Log.i("Evaluate", "is not, start it");
			startService(new Intent(EvaluateSheep2.this, eidService.class));
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
		Integer ii;
	   	//	make the scan eid button red
	   	Button btn = (Button) findViewById( R.id.scan_eid_btn );
	   	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	   	// TODO
	   	clearBtn( null );  
	   	TextView TV = (TextView) findViewById (R.id.eidText);
	   	TV.setText( LastEID );
		Log.i("Evaluate", "Got EID " + LastEID);
		TV = (TextView) findViewById (R.id.inputText);
		TV.setText( LastEID );
		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
				"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off , sheep_table.alert01 " +
				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +	
				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
				"where id_type_table.id_typeid = 2 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", LastEID);
		Log.i("Got EID", " ready for command " + cmd); 
		Object crsr = dbh.exec( cmd ); 
    	cursor   = (Cursor) crsr;
    	dbh.moveToFirstRecord();
    	if( dbh.getSize() == 0 )
			{ // no sheep with that EID tag in the database so clear out and return
			clearBtn( null );
			TV = (TextView) findViewById( R.id.sheepnameText );
	    	TV.setText( "Cannot find this sheep." );
	    	return;
		}
    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	Log.i("Got EID", " got sheep named  " + dbh.getStr(0)); 
    	sheep_id = dbh.getInt(1);
    	Log.i("Got EID", " sheep ID is " + String.valueOf(sheep_id));
    	thissheep_id = sheep_id;
    	Log.i("Got EID", " sheep ID is " + String.valueOf(thissheep_id));
//    	TV = (TextView) findViewById(R.id.eidText)	;
//    	TV.setText(dbh.getStr(3));
    	String alert_text = dbh.getStr(6);
    	Log.i("Got EID ", "Alert Text is " + alert_text);
//    	Now to test of the sheep has an alert and if so then set the alerts button to red
//    	if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
		if (alert_text != null && !alert_text.isEmpty() ){
			// make the alert button red and enable it and pop up the alert text
			btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	showAlert(v);
		}
//		Now we need to get the farm tag for that sheep and fill the display with data
    	
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, " +
		"id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", thissheep_id);

//    	Log.i("Evaluate ", cmd);    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a farm tag installed
			TV = (TextView) findViewById( R.id.farmText );
			TV.setText( "No tag" );
    	} else {
    		TextView TV5 = (TextView) findViewById(R.id.farmText)	;
    		TV5.setText(dbh.getStr(3));
    		Log.i(" got EID ", "now got a farm tag " + dbh.getStr(3));
//    		ii = dbh.getInt(1);
    		farmtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
    	}
//		Now we need to get the federal tag for the sheep
		
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", thissheep_id);
    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
    	
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a federal tag installed
			TV = (TextView) findViewById( R.id.fedText );
			TV.setText( "No tag" );
    	} else {
        	fedtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database   	
        	TextView TV5 = (TextView) findViewById(R.id.fedText)	;
        	Log.i(" got EID ", "now got a fed tag " + TV5);
        	TV5.setText(dbh.getStr(3));
        	ii = dbh.getInt(1);
    	}
		// TODO
    	
   }	

	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluate_sheep2);
        String 			dbname = getString(R.string.real_database_file); 
        String          cmd;
        Button 			btn;
        TextView TV;       
        Object 			crsr;
        dbh = new DatabaseHandler( this, dbname );
       
        scored_evaluation_traits = new ArrayList<String>();
    	data_evaluation_traits = new ArrayList<String>();
    	user_evaluation_traits = new ArrayList<String>();

    	scored_trait_numbers = new ArrayList<Integer>();
    	data_trait_numbers = new ArrayList<Integer>();
    	user_trait_numbers = new ArrayList<Integer>();
    	user_trait_number_items = new ArrayList<Integer>();
//    	eval_trait_numbers = new ArrayList<Integer>();
    	
    	nRecs = 0; 	// Number of scored data items using rating bars
    	nRecs2 = 0;	// Number of real data items using float data
    	nRecs3 = 0;	// Number of user defined data items using custom data
    	nRecs4 = 0;	// Number of custom data items to fill the radio buttons
    	
		CheckIfServiceIsRunning();
		cmd = "select * from last_eval_table";
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        
    	trait01 = dbh.getInt(1);
    	cursor.moveToNext();	
    	trait02 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait03 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait04 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait05 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait06 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait07 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait08 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait09 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait10 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait11 = dbh.getInt(1);
    	trait11_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait12 = dbh.getInt(1);
    	trait12_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait13 = dbh.getInt(1);
    	trait13_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait14 = dbh.getInt(1);
    	trait14_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait15 = dbh.getInt(1);
    	trait15_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait16 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait17 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait18 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait19 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait20 = dbh.getInt(1);
    	cursor.close();
		
    	//	Set up the scored traits and inflate the layout
    	cmd = String.format("select evaluation_trait_table.trait_name, evaluation_trait_table.id_traitid " +
	        	"from evaluation_trait_table inner join last_eval_table where " +
        		" evaluation_trait_table.id_traitid = last_eval_table.id_traitid and evaluation_trait_table.trait_type = 1 ") ;
//    	Log.i("evaluate2", " cmd is " + cmd);
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        nRecs    = cursor.getCount();
        dbh.moveToFirstRecord();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
        	scored_trait_numbers.add(cursor.getInt(1));
//        	tempTraitNumber = cursor.getInt(1);
//        	Log.i("evaluate2", " trait number is " + String.valueOf(cursor.getInt(1)));
	    	scored_evaluation_traits.add(cursor.getString(0));
//	    	Log.i("evaluate2", " trait name is " + cursor.getString(0));
    	}
    	cursor.close();    	
    	Log.i("evaluate2", "number of records in scored traits cursor is " + String.valueOf(nRecs));
    	LayoutInflater inflater = getLayoutInflater();	
//    	Log.i ("evaluate2", scored_evaluation_traits.get(0));
    	for( int ii = 0; ii < nRecs; ii++ ){	
//    		Log.i("in for loop" , " ii is " + String.valueOf(ii));
//    		Log.i ("in for loop", " trait name is " + scored_evaluation_traits.get(ii));
			TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);	
//			Log.i("in for loop", " after TableLayout");
	    	TableRow row = (TableRow)inflater.inflate(R.layout.eval_item_entry, table, false);
	    	tempLabel = scored_evaluation_traits.get(ii);
//	    	Log.i("in for loop", " tempLabel is " + tempLabel);
	    	((TextView)row.findViewById(R.id.rb1_lbl)).setText(tempLabel);
//	    	Log.i("in for loop", " after set text view");
	    	table.addView(row);
    	}
    	
    	//	Set up the data traits and inflate the layout
    	cmd = String.format("select evaluation_trait_table.trait_name, evaluation_trait_table.id_traitid " +
	        	"from evaluation_trait_table inner join last_eval_table where " +
        		" evaluation_trait_table.id_traitid = last_eval_table.id_traitid and evaluation_trait_table.trait_type = 2 ") ;
//    	Log.i("test designs", " cmd is " + cmd);
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        nRecs2    = cursor.getCount();
        dbh.moveToFirstRecord();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
        	data_trait_numbers.add(cursor.getInt(1));
//        	tempTraitNumber = cursor.getInt(1);
//        	Log.i("evaluate2", " trait number is " + String.valueOf(cursor.getInt(1)));
	    	data_evaluation_traits.add(cursor.getString(0));
//	    	Log.i("evaluate2", " trait name is " + cursor.getString(0));
    	}
    	cursor.close();    	
    	Log.i("evaluate2", "number of records in data traits cursor is " + String.valueOf(nRecs2));
    	inflater = getLayoutInflater();	
//    	Log.i ("evaluate2", scored_evaluation_traits.get(0));
    	for( int ii = 0; ii < nRecs2; ii++ ){	
//    		Log.i("in for loop" , " ii is " + String.valueOf(ii));
//    		Log.i ("in for loop", " trait name is " + data_evaluation_traits.get(ii));
			TableLayout table = (TableLayout) findViewById(R.id.TableLayout02);	
//			Log.i("in for loop", " after TableLayout");
	    	TableRow row = (TableRow)inflater.inflate(R.layout.eval_data_item_entry, table, false);
	    	tempLabel = data_evaluation_traits.get(ii);
//	    	Log.i("in for loop", " tempLabel is " + tempLabel);
	    	((TextView)row.findViewById(R.id.data_lbl)).setText(tempLabel);
//	    	Log.i("in for loop", " after set text view");
	    	table.addView(row);
    	}

    	// Set up the user traits  
    	// 	TODO
    	
    	cmd = String.format("select evaluation_trait_table.trait_name, evaluation_trait_table.id_traitid, " +
	        	"custom_evaluation_name_table.custom_eval_number " +
    			"from evaluation_trait_table inner join last_eval_table on " +
	        	" evaluation_trait_table.id_traitid = last_eval_table.id_traitid" +
	        	" inner join custom_evaluation_name_table on evaluation_trait_table.id_traitid = " +
        		" custom_evaluation_name_table.id_traitid where evaluation_trait_table.trait_type = 3 ") ;
//    	Log.i("evaluate2", " cmd is " + cmd);
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        nRecs3    = cursor.getCount();
    	dbh.moveToFirstRecord();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
        	user_evaluation_traits.add(cursor.getString(0));
	    	Log.i("evaluate2", " trait name is " + cursor.getString(0));
	    	user_trait_numbers.add(cursor.getInt(1));
	    	Log.i("evaluate2", " trait id number is " + String.valueOf(cursor.getInt(1)));
	    	user_trait_number_items.add(cursor.getInt(2));
	    	Log.i("evaluate2", " number of items for this trait is " + String.valueOf(cursor.getInt(2)));		    	
    	}
    	cursor.close();  
    	
    	Log.i("evaluate2", "number of records in user traits cursor is " + String.valueOf(nRecs3));
    	inflater = getLayoutInflater();	
    	for( int ii = 0; ii < nRecs3; ii++ ){	
//    		Log.i("in for loop" , " ii is " + String.valueOf(ii));
    		Log.i ("in for loop", " user trait number is " + String.valueOf(user_trait_numbers.get(ii)));
//    		Log.i ("in for loop", " trait name is " + user_evaluation_traits.get(ii));
    		Log.i ("in for loop", " number of trait entries is " + String.valueOf(user_trait_number_items.get(ii)));
//			TableLayout table = (TableLayout) findViewById(R.id.TableLayout03);	
//			Log.i("in for loop", " after TableLayout");		    	
	    	//	Get the text for the buttons
	    	tempText = String.valueOf(user_trait_numbers.get(ii));
	    	Log.i("in for loop", "trait numbers is " + tempText);
	    	cmd = String.format("select custom_evaluation_traits_table.custom_evaluation_item " +
	    			" from custom_evaluation_traits_table " +
	    			" where custom_evaluation_traits_table.id_traitid = '%s' "+
	    			" order by custom_evaluation_traits_table.custom_evaluation_order ASC ", tempText);
//	    	Log.i("evaluate2", " cmd is " + cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        nRecs4    = cursor.getCount();
	        dbh.moveToFirstRecord();		        
	        ArrayList buttons = new ArrayList();
	        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	        	buttons.add (cursor.getString(0));
		    	Log.i("evaluate2", " radio button text is " + cursor.getString(0));
	    	}
//	        TableRow row = (TableRow)inflater.inflate(R.layout.eval_custom_item, table, false);
	        
	        radioBtnText = (String[]) buttons.toArray(new String [buttons.size()]);
	    	cursor.close();  
	    	// Build the radio buttons here
//	    	radioGroup = ((RadioGroup) row.findViewById(R.id.radioGroup1));
	    	radioGroup = ((RadioGroup) findViewById(R.id.radioGroup1));
	    	addRadioButtons(user_trait_number_items.get(ii), radioBtnText);
    	}
    	
       	// make the alert button normal and disabled
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false);    
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
  	  	Log.i("addradiobuttons", radioBtnText[i]);
  	  	radioBtn.setId(i);

  	    //add it to the group.
  	    radioGroup.addView(radioBtn, i);
  	  }
  	}        
    public void saveScores( View v )
    {    	
    	String 			dbname = getString(R.string.real_database_file); 
        String          cmd;    
        Object 			crsr;
        List<Float> 	rating_scores;
    	TextView 		TV;
    	String 			temp_string;

    	rating_scores = new ArrayList<Float>();
    	real_scores = new ArrayList<Float>();
//    	eval_trait_numbers = new ArrayList<Integer>();
		RatingBar ratingBar;
    	
    	// I got the sheep id from the search by federal or farm or EID tag
    	// it's in the sheep_id variable
    	
    	Log.i("in save scores", " sheep id is " + String.valueOf(sheep_id));  
    	
		// 	get the rating bar scores and fill the rating_scores array
		TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
		Log.i("in save scores", " number rating bars is " + String.valueOf(nRecs)); 
		if (nRecs != 0) {
			for( int ii = 0; ii < nRecs; ii++ ){	
				Log.i("in save scores", " in 1st for loop ii is" + String.valueOf(ii)); 
				TableRow row1= (TableRow)table.getChildAt(ii);
				ratingBar = (RatingBar) row1.getChildAt(1);
				rating_scores.add(ratingBar.getRating());			
				Log.i("RatingBar01 ", String.valueOf(ratingBar.getRating()));  
			}
			//	Fill the rest of the array with zeros
			for( int ii = nRecs; ii < 10; ii++ ){	
				rating_scores.add((float) 0.0);	
			}
		}else{
			//	Nothing to add so fill the entire thing with zeros
			for( int ii = 0; ii < 10; ii++ ){	
				rating_scores.add((float) 0.0);	
			}
		}
		
    	// Fill the rating bar score variables from the rating_scores array			
    		trait01_data = rating_scores.get(0);
    		Log.i("trait01_ratingbar ", String.valueOf(trait01_data));
     		trait02_data = rating_scores.get(1);
    		Log.i("trait02_ratingbar ", String.valueOf(trait02_data));
    		trait03_data = rating_scores.get(2);	
    		Log.i("trait03_ratingbar ", String.valueOf(trait03_data));    		
    		trait04_data = rating_scores.get(3);
    		Log.i("trait04_ratingbar ", String.valueOf(trait04_data));    		
    		trait05_data = rating_scores.get(4);
    		Log.i("trait05_ratingbar ", String.valueOf(trait05_data));    		
    		trait06_data = rating_scores.get(5);
    		Log.i("trait06_ratingbar ", String.valueOf(trait06_data));    		
    		trait07_data = rating_scores.get(6);
    		Log.i("trait07_ratingbar ", String.valueOf(trait07_data));    		
    		trait08_data = rating_scores.get(7);
    		Log.i("trait08_ratingbar ", String.valueOf(trait08_data));    		
    		trait09_data = rating_scores.get(8);
    		Log.i("trait09_ratingbar ", String.valueOf(trait09_data));    		
    		trait10_data = rating_scores.get(9);
    		Log.i("trait10_ratingbar ", String.valueOf(trait10_data));    				
    		
    		//	get the real data values  	
//    		Log.i("in save scores", " number real data points is " + String.valueOf(nRecs2));
    		table = (TableLayout) findViewById(R.id.TableLayout02);
//    		Log.i("in save scores", " after find tablelayout02 ");
    		if (nRecs2 != 0) {
    			for( int ii = 0; ii < nRecs2; ii++ ){	
    			TableRow row1= (TableRow)table.getChildAt(ii);
    			TV = (EditText ) row1.getChildAt(1);
    			Float tempData = Float.valueOf(TV.getText().toString());
    			real_scores.add(ii,tempData);
//    			Log.i("index ii ", String.valueOf(ii));
//    			Log.i("real_score ", String.valueOf(real_scores.get(ii)));
//    			Log.i("realscore ", String.valueOf(tempData)); 
    			}
    	    	for( int ii = nRecs2; ii < 5; ii++ ){	
    	    		real_scores.add((float) 0.0);
    			}
    		}else {
    			for( int ii = 0; ii < 5; ii++ ){	
    	    		real_scores.add((float) 0.0);
    			}
    		}
    		//	Fill the real score variables from the real_scores array
    		trait11_data = real_scores.get(0);
    		Log.i("trait11_data ", String.valueOf(trait11_data));
    		trait12_data = real_scores.get(1);
    		Log.i("trait12_data ", String.valueOf(trait12_data));
    		trait13_data = real_scores.get(2);
       		Log.i("trait13_data ", String.valueOf(trait13_data));
    		trait14_data = real_scores.get(3);
       		Log.i("trait14_data ", String.valueOf(trait14_data));
    		trait15_data = real_scores.get(4);
       		Log.i("trait15_data ", String.valueOf(trait15_data));
 	    	
    		//	Get the radiogroup selected for the user data 
    		//	This will need to be fixed for more than one user defined group but will work for Dr. Purdy's test
    		RadioGroup rg=(RadioGroup)findViewById(R.id.radioGroup1);
     		trait16_data = rg.getCheckedRadioButtonId();
    		Log.i("trait16_data ", String.valueOf(trait16_data));
//    		RadioButton b = (RadioButton)findViewById(selected);
//    		b.getText().toString();    		
    		    		
    		String mytoday = TodayIs();
    		// added time stamp here for Dr. Purdy in function TodayIs()
//    		Log.i("Date is ", mytoday);
   		
    		// Now that I have all the data I need to write it into the sheep_evaluation_table
    		
//	    	Log.i("number ","eval trait01 "+String.valueOf(trait01));
//	    	Log.i("number ","eval trait02 "+String.valueOf(trait02));
//	    	Log.i("number ","eval trait03 "+String.valueOf(trait03));
//	    	Log.i("number ","eval trait04 "+String.valueOf(trait04));
//	    	Log.i("number ","eval trait05 "+String.valueOf(trait05));
//	    	Log.i("number ","eval trait06 "+String.valueOf(trait06));
//	    	Log.i("number ","eval trait07 "+String.valueOf(trait07));
//	    	Log.i("number ","eval trait08 "+String.valueOf(trait08));
//	    	Log.i("number ","eval trait09 "+String.valueOf(trait09));
//	    	Log.i("number ","eval trait10 "+String.valueOf(trait10));
//    		
//	    	Log.i("number ","eval trait11 "+String.valueOf(trait11));
//	    	Log.i("number ","eval trait11 units "+String.valueOf(trait11_unitid));
//	    	Log.i("number ","eval trait12 "+String.valueOf(trait12));
//	    	Log.i("number ","eval trait12 units "+String.valueOf(trait12_unitid));
//	    	Log.i("number ","eval trait13 "+String.valueOf(trait13));
//	    	Log.i("number ","eval trait13 units "+String.valueOf(trait13_unitid));
//	    	Log.i("number ","eval trait14 "+String.valueOf(trait14));
//	    	Log.i("number ","eval trait14 units "+String.valueOf(trait14_unitid));
//	    	Log.i("number ","eval trait15 "+String.valueOf(trait15));
//	    	Log.i("number ","eval trait15 units "+String.valueOf(trait15_unitid));
    		
    		Log.i("number ","eval trait16 "+String.valueOf(trait16_data));
//    		Log.i("number ","eval trait17 "+String.valueOf(trait17_data));
//    		Log.i("number ","eval trait18 "+String.valueOf(trait18_data));
//    		Log.i("number ","eval trait19 "+String.valueOf(trait19_data));
//    		Log.i("number ","eval trait20 "+String.valueOf(trait20_data));
    		
    		       		
    		cmd = String.format("insert into sheep_evaluation_table (sheep_id, " +
    		"trait_name01, trait_score01, trait_name02, trait_score02, trait_name03, trait_score03, " +
    		"trait_name04, trait_score04, trait_name05, trait_score05, trait_name06, trait_score06," +
    		"trait_name07, trait_score07, trait_name08, trait_score08, trait_name09, trait_score09, " +
    		"trait_name10, trait_score10, trait_name11, trait_score11, trait_name12, trait_score12, " +
    		"trait_name13, trait_score13, trait_name14, trait_score14, trait_name15, trait_score15, " +
    		"trait_name16, trait_score16, trait_name17, trait_score17, trait_name18, trait_score18, " +
    		"trait_name19, trait_score19, trait_name20, trait_score20, " +
    		"trait_units11, trait_units12, trait_units13, trait_units14, trait_units15, eval_date) " +
    		"values (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s," +
    		"%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,'%s') ", 
    				sheep_id, trait01, trait01_data, trait02, trait02_data, trait03, trait03_data,
    				trait04, trait04_data, trait05, trait05_data, trait06, trait06_data,
    				trait07, trait07_data, trait08, trait08_data, trait09, trait09_data, 
    				trait10, trait10_data, trait11, trait11_data, trait12, trait12_data, 
    				trait13, trait13_data, trait14, trait14_data, trait15, trait15_data, 
    				trait16, trait16_data, trait17, trait17_data, trait18, trait18_data,
    				trait19, trait19_data, trait20, trait20_data, 
    				trait11_unitid, trait12_unitid, trait13_unitid, trait14_unitid, trait15_unitid, mytoday );
    		
//    		Log.i("save eval ", cmd);
    		dbh.exec( cmd );
    		cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_table.sheep_id=%d", sheep_id);    		
    		crsr = dbh.exec( cmd );
            cursor   = ( Cursor ) crsr;
            dbh.moveToFirstRecord();
            
            String alert_text = (dbh.getStr(0));
//            Log.i ("Evaluate Alert", " Alert Text is " + alert_text);
    		alert_text = alert_text + "\n" + "Evaluation Done";
    		
//            Log.i ("Evaluate Alert", " Alert Text is " + alert_text);

    		cmd = String.format("update sheep_table set alert01='%s' where sheep_id=%d", alert_text, sheep_id);
//    		Log.i("test alert ", cmd);   
    		dbh.exec( cmd );
    		cursor.close();
    		clearBtn( null );
    }
	
	   public void backBtn( View v )
	    {
//		   	Log.i("evaluate", " Back button pressed before close DB");
		   	dbh.closeDB();
//		   	Log.i("evaluate", " Back button pressed after close DB");
		   	doUnbindService();
			stopService(new Intent(EvaluateSheep2.this, eidService.class));
			clearBtn( null );   	
			finish();
	    }
	 
	public void showAlert(View v)
	{
		String	alert_text;
		String 			dbname = getString(R.string.real_database_file); 
        String          cmd;    
        Object 			crsr;
 //       	dbh = new DatabaseHandler( this, dbname );
		// Display alerts here   	
				AlertDialog.Builder builder = new AlertDialog.Builder( this );
				cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_id =%d", sheep_id);
//				Log.i("evalGetAlert ", cmd);  
				crsr = dbh.exec( cmd );
		        cursor   = ( Cursor ) crsr;
		        dbh.moveToFirstRecord();		       
		        alert_text = (dbh.getStr(0));
//		        Log.i("evalShowAlert ", alert_text); 
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
	
	
	public void helpBtn( View v )
    {
   	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_evaluate )
	           .setTitle( R.string.help_warning );
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
		// clear out the display of everything
		TextView TV ;
		Button btn;
		TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
		TV = (TextView) findViewById( R.id.fedText );
		TV.setText( "" );
		TV = (TextView) findViewById( R.id.farmText );
		TV.setText( "" );
		TV = (TextView) findViewById( R.id.eidText );
		TV.setText( "" );
//		Log.i("Clear btn", "Before clear rating bars");
//		trait01_ratingbar = (RatingBar) findViewById(R.id.trait01_ratingbar);
//		trait01_ratingbar.setRating(0.0f);
//		trait02_ratingbar = (RatingBar) findViewById(R.id.trait02_ratingbar);
//		trait02_ratingbar.setRating(0.0f);
//		trait03_ratingbar = (RatingBar) findViewById(R.id.trait03_ratingbar);
//		trait03_ratingbar.setRating(0.0f);
//		trait04_ratingbar = (RatingBar) findViewById(R.id.trait04_ratingbar);
//		trait04_ratingbar.setRating(0.0f);
//		trait05_ratingbar = (RatingBar) findViewById(R.id.trait05_ratingbar);
//		trait05_ratingbar.setRating(0.0f);
//		trait06_ratingbar = (RatingBar) findViewById(R.id.trait06_ratingbar);
//		trait06_ratingbar.setRating(0.0f);
//		trait07_ratingbar = (RatingBar) findViewById(R.id.trait07_ratingbar);
//		trait07_ratingbar.setRating(0.0f);
//		trait08_ratingbar = (RatingBar) findViewById(R.id.trait08_ratingbar);
//		trait08_ratingbar.setRating(0.0f);
//		trait09_ratingbar = (RatingBar) findViewById(R.id.trait09_ratingbar);
//		trait09_ratingbar.setRating(0.0f);
//		trait10_ratingbar = (RatingBar) findViewById(R.id.trait10_ratingbar);
//		trait10_ratingbar.setRating(0.0f);
////		Log.i("Clear btn", "After clear rating bars");
//		TV = (TextView) findViewById( R.id.trait11_data );
//		TV.setText ( "" );
//		TV = (TextView) findViewById( R.id.trait12_data );
//		TV.setText ( "" );
//		TV = (TextView) findViewById( R.id.trait13_data );
//		TV.setText ( "" );
//		TV = (TextView) findViewById( R.id.trait14_data );
//		TV.setText ( "" );
//		TV = (TextView) findViewById( R.id.trait15_data );
//		TV.setText ( "" );
       	// make the alert button normal and disabled
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false);  
    }
	   private String TodayIs() {
			Calendar calendar = Calendar.getInstance();
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
	        //12 hour format
//			int hour = cal.get(Calendar.HOUR);
	        //24 hour format
			int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			int second = calendar.get(Calendar.SECOND);
			  
			return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) + "_" + hourofday + ":" + minute + ":" + second ;
		}
	    private String Make2Digits(int i) {
			if (i < 10) {
				return "0" + i;
			} else {
				return Integer.toString(i);
			}
		}	
//  user clicked 'Scan' button    
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
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0x0000FF00, 0xFFCC0000));
			
		} catch (RemoteException e) {
			// In this case the service has crashed before we could even do anything with it
		}
		}    	    	
 }
 
	 // user clicked 'Search Fed' button
    public void searchFedTag( View v )
    	{
    	String          cmd;
    	TextView		TV = (TextView) findViewById( R.id.inputText );
    	String			fed = TV.getText().toString();
    	Log.i("Evaluate ", " federal tag is " + fed);
    	Integer			ii;
    	// Hide the keyboard when you click the button
    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    	
// 		Start of the actual code to process the button click
    	if( fed != null && fed.length() > 0 )
    		{
//			Search for the sheep with the entered federal tag number. 
//    		assumes no duplicate federal tag numbers, ok for our flock not ok for the general case
    		
    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
    				"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off , sheep_table.alert01 " +
    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +	
    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
    				"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", fed);
//    		Log.i("Evaluate ", cmd);
    		}	
    	else
    	{
    		return;
     	}
    	Object crsr = dbh.exec( cmd ); 
    	cursor   = (Cursor) crsr;
    	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
    		{ // no sheep with that federal tag in the database so clear out and return
    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheepnameText );
        	TV.setText( "Cannot find this sheep." );
        	return;
    		}
// This section would allow for multiple sheep with same tag if we implement next and previous
//    	buttons but is commented out for now as our sheep have unique federal tags
//    	if( dbh.getSize() >1){
//
// 			Enable the previous and next record buttons
//    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
//    		btn2.setEnabled(true);  
//    		//	Set up the various pointers and cursor data needed to traverse the sequence
//    		recNo    = 1;
//    		cursor   = (Cursor) crsr;
//    		nRecs    = cursor.getCount();
//    		colNames = cursor.getColumnNames();
//    		cursor.moveToFirst();
//    	}
		
		// TODO
    	fedtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
		Log.i("Evaluate", " id infor table id is " + String.valueOf(fedtagid));
		
    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	Log.i("Evaluate", " sheep name is " + dbh.getStr(0));
    	sheep_id = dbh.getInt(1);
    	thissheep_id = sheep_id;
    	Log.i("Evaluate", " sheep id is " + String.valueOf(thissheep_id));
    	TV = (TextView) findViewById(R.id.fedText)	;
    	TV.setText(dbh.getStr(3));
    	Log.i("Evaluate", " sheep fed tag is " + dbh.getStr(3));
    	String alert_text = dbh.getStr(6);
    	Log.i("Evaluate", " sheep alert text is " + alert_text);
//    	Now to test of the sheep has an alert and if so then set the alerts button to red
		if (alert_text != null && !alert_text.isEmpty() ){
//		if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
			// make the alert button red and enable it and pop up the alert text
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	showAlert(v);
		}
    	
//		Now we need to get the farm tag for that sheep and fill the display with data
    	
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, " +
		"id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", thissheep_id);

//    	Log.i("Evaluate ", "ready to get farm tags cmd is " + cmd);    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a farm tag installed
			TV = (TextView) findViewById( R.id.farmText );
			TV.setText( "No tag" );
    	} else {
    		TextView TV5 = (TextView) findViewById(R.id.farmText)	;
    		TV5.setText(dbh.getStr(3));
    		ii = dbh.getInt(1);
    		farmtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
    	}
    	}
// 	user clicked 'Search Farm Tag' button
    public void searchFarmTag( View v )
    	{
    	String          cmd;
    	TextView		TV = (TextView) findViewById( R.id.inputText );
    	String			farm = TV.getText().toString();
    	Integer			ii;
    	// Hide the keyboard when you click the button
    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    	
// 		Start of the actual code to process the button click
    	if( farm != null && farm.length() > 0 )
    		{
//			Search for the sheep with the entered farm tag number. 
//    		assumes no duplicate farm tag numbers, ok for our flock not ok for the general case  
    		
    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
    				"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off, sheep_table.alert01 " +
    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
    				"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.tag_number='%s'", farm);
    		
//    		Log.i("Evaluate", "building command search for farm tag ");
    		}	
    	else
    	{
    		return;
     	}
    	Object crsr = dbh.exec( cmd );   	
    	dbh.moveToFirstRecord();
    	if( dbh.getSize() == 0 )
    		{ // no sheep with that farm tag in the database so clear out and return
    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheepnameText );
        	TV.setText( "Cannot find this sheep." );
        	return;
    		}
//Need to add next and previous buttons if we have duplicate farm tags
//    	if( dbh.getSize() >1){
//
// 			Enable the previous and next record buttons
//    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
//    		btn2.setEnabled(true);  
//    		//	Set up the various pointers and cursor data needed to traverse the sequence
//    		recNo    = 1;
//    		cursor   = (Cursor) crsr;
//    		nRecs    = cursor.getCount();
////    		colNames = cursor.getColumnNames();
//    		cursor.moveToFirst();
//    	}
    	
    	farmtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	sheep_id = dbh.getInt(1);
    	TV = (TextView) findViewById(R.id.farmText)	;
    	TV.setText(dbh.getStr(3));
    	ii = dbh.getInt(1);
    	
//    	Now to test of the sheep has an alert and if so then set the alerts button to red
    	String alert_text = dbh.getStr(6);
//    	if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
    	if (alert_text != null && !alert_text.isEmpty() ){
			// make the alert button red and enable it and pop up the alert text
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true); 
	    	showAlert(v);
		}
//		Now we need to get the rest of the tags and fill the display with data
		
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", ii);
    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
    	
		if( dbh.getSize() == 0 )
		{ // This sheep does not have a federal tag installed
			TV = (TextView) findViewById( R.id.fedText );
			TV.setText( "No tag" );
    	} else {
        	fedtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database   	
        	TextView TV5 = (TextView) findViewById(R.id.fedText)	;
        	TV5.setText(dbh.getStr(3));
        	ii = dbh.getInt(1);
    	}
    	}    
        
    // user clicked the "next record" button
    public void nextBtn( View v)
    {
    	TextView 	TV;
    	Integer		ii;
    	String		cmd;
    	if (recNo == (nRecs-1)) {
    		// at end so disable next record button
    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
        	btn2.setEnabled(false);   		
    	}
    	if ( cursor.moveToNext() ){
    		// I've moved forward so I need to enable the previous record button
    		Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
    		btn3.setEnabled(true);
//        	id = dbh.getInt( 0 ); // Get the primary key from the current record
//        	Log.i ("DoSheepTask", "In if statement next button and the record id is " + String.valueOf(id) );
    		recNo         += 1;
//display stuff here
		}
    	else {
    		//At the end so disable the next button
           	Button btn2 = (Button) findViewById( R.id.next_rec_btn );
        	btn2.setEnabled(false); 
        	recNo         -= 1;
    	}
    }

    // user clicked the "previous record" button
    public void prevBtn( View v)
    {
    	TextView TV;
    	Integer		ii;
    	String		cmd;
    	if ( cursor.moveToPrevious() ){
    		// I've moved back so enable the next record button
    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
    		btn2.setEnabled(true);  
//        	id = dbh.getInt( 0 ); // Get the primary key from the current record
 //       	Log.i ("DoSheepTask", "In if statement prev button and the record id is " + String.valueOf(id) );
    		recNo  -= 1;
 //display stuff here
		}
    	else {
    		// at beginning so disable the previous button
        	Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
        	btn3.setEnabled(false);
        	recNo         += 1;
    	}
    	if (recNo == 1) {
    		// at beginning so disable prev record button
    		Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
        	btn3.setEnabled(false);   		
    	}
    }


}
