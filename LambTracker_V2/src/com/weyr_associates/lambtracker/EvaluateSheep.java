package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.weyr_associates.lambtracker.EvaluateSheep.IncomingHandler;

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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.database.Cursor;


public class EvaluateSheep extends Activity {
	
	public Button button;
	
	String     	cmd;
	Integer 	i;
	
	public int trait01, trait02, trait03, trait04, trait05, trait06, trait07;
	public int trait06_units, trait07_units;
	public int sheep_id;
	int 		id;
	int   		fedtagid, farmtagid, eidtagid;
	private int			    recNo;
	private int             nRecs;
	List<String> scored_evaluation_traits, data_evaluation_traits;
	List<Integer> which_traits;
	
	ArrayAdapter<String> dataAdapter;
	
	public RatingBar trait01_ratingbar ;
	public RatingBar trait02_ratingbar ;
	public RatingBar trait03_ratingbar ;
	public RatingBar trait04_ratingbar ;
	public RatingBar trait05_ratingbar ;

	public Float trait01_data, trait02_data, trait03_data, trait04_data, trait05_data;
	public Float trait06_data, trait07_data;
	
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
				gotEID ();	
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
				stopService(new Intent(EvaluateSheep.this, eidService.class));
				
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
			startService(new Intent(EvaluateSheep.this, eidService.class));
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
	public void gotEID( )
   {
	   	//	make the scan eid button red
   	Button btn = (Button) findViewById( R.id.scan_eid_btn );
   	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
   	
   	TextView TV = (TextView) findViewById (R.id.eidText);
   	TV.setText( LastEID );
//		Log.i("Evaluate", "Got EID");
		
	}	

	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluate_sheep);
        String 			dbname = getString(R.string.real_database_file); 
        String          cmd;
        Button 			btn;
        TextView TV;       
        Object 			crsr;
        dbh = new DatabaseHandler( this, dbname );
       
		CheckIfServiceIsRunning();
		
        cmd = "select * from last_eval_table ";
        crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        
        trait01 = cursor.getInt(1);
//        Log.i("EvaluateSheep ", "trait01 = " + String.valueOf(trait01));
        cursor.moveToNext();
        
        trait02 = cursor.getInt(1);
//        Log.i("EvaluateSheep ", "trait02 = " + String.valueOf(trait02));
        cursor.moveToNext();
        
        trait03 = cursor.getInt(1);
//        Log.i("EvaluateSheep ", "trait03 = " + String.valueOf(trait03));
        cursor.moveToNext();
        
        trait04 = cursor.getInt(1);
//        Log.i("EvaluateSheep ", "trait04 = " + String.valueOf(trait04));
        cursor.moveToNext();
        
        trait05 = cursor.getInt(1);
//        Log.i("EvaluateSheep ", "trait05 = " + String.valueOf(trait05));
        cursor.moveToNext();
        
        trait06 = cursor.getInt(1);
//        Log.i("EvaluateSheep ", "trait06 = " + String.valueOf(trait06));
        trait06_units = cursor.getInt(2);
//        Log.i("EvaluateSheep ","units trait06 "+String.valueOf(trait06_units));
        cursor.moveToNext();
        
        trait07 = cursor.getInt(1);
//        Log.i("EvaluateSheep ", "trait07 = " + String.valueOf(trait07));
        trait07_units = cursor.getInt(2); 
//        Log.i("EvaluateSheep ","units trait07 "+String.valueOf(trait07_units));
        cursor.close();
           
        if (trait01!=0) {
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait01 );
//        Log.i("get name ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after get name");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait01_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        }
        if (trait02!=0) {
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait02 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait02_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        }
        if (trait03!=0) {
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait03 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait03_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        }
        if (trait04!=0) {
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait04 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait04_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        }
        if (trait05!=0) {
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait05 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait05_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        }
        if (trait06!=0) {
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait06 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait06_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        }
        if (trait07!=0) {
        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table where " +
        		"evaluation_trait_table.id_traitid=%s", trait07 );
//        Log.i("EvaluateSheep ", cmd);
        crsr = dbh.exec( cmd );
//        Log.i("EvaluateSheep ", "after");
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
//        Log.i("EvaluateSheep ", "after move to first");
        TV = (TextView) findViewById( R.id.trait07_lbl );
        TV.setText(dbh.getStr(0));
//        Log.i("EvaluateSheep ", "after get the text");
        }
        cursor.close();
       	// make the alert button normal and disabled
    	btn = (Button) findViewById( R.id.alert_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
    	btn.setEnabled(false);        
       	}
    public void saveScores( View v )
    {    	
    	String 			dbname = getString(R.string.real_database_file); 
        String          cmd;    
        Object 			crsr;
        dbh = new DatabaseHandler( this, dbname );
    	TextView TV;
    	String temp_string;
    	trait06_data = 0.0f;
    	trait07_data = 0.0f;
    	// I got the sheep id from the search by federal or farm or EID tag
    	// it's in the sheep_id variable
    	
//    	Log.i("in save scores", " sheep id is " + String.valueOf(sheep_id));    
    	// Get the rating bar scores
    		trait01_ratingbar = (RatingBar) findViewById(R.id.trait01_ratingbar);
    		trait01_data = trait01_ratingbar.getRating();
//    		Log.i("trait01_ratingbar ", String.valueOf(trait01_data));
    		
    		trait02_ratingbar = (RatingBar) findViewById(R.id.trait02_ratingbar);
    		trait02_data = trait02_ratingbar.getRating();
//    		Log.i("trait02_ratingbar ", String.valueOf(trait02_data));
    		
    		trait03_ratingbar = (RatingBar) findViewById(R.id.trait03_ratingbar);
    		trait03_data = trait03_ratingbar.getRating();	
//    		Log.i("trait03_ratingbar ", String.valueOf(trait03_data));
    		
    		trait04_ratingbar = (RatingBar) findViewById(R.id.trait04_ratingbar);
    		trait04_data = trait04_ratingbar.getRating();
//    		Log.i("trait04_ratingbar ", String.valueOf(trait04_data));
    		
    		trait05_ratingbar = (RatingBar) findViewById(R.id.trait05_ratingbar);
    		trait05_data = trait05_ratingbar.getRating();
//    		Log.i("trait05_ratingbar ", String.valueOf(trait05_data));
    		
    		// get the real data scores
    		
    		TV = (TextView) findViewById(R.id.trait06_data);
    		temp_string = TV.getText().toString();
    		if(TextUtils.isEmpty(temp_string)){
    	        // EditText was empty
    	        // so no real data collected just break out
    			trait06_data = 0.0f;
//    			Log.i("save trait6", "float data is " + String.valueOf(trait06_data));
    			trait06_units = 0;
    	    }
    		else {
    			trait06_data = Float.valueOf(TV.getText().toString());
//    			Log.i("save trait6", "float data is " + String.valueOf(trait06_data));
//    			Log.i("trait06_units ", String.valueOf(trait06_units));
    		}
    		
    		TV = (TextView) findViewById(R.id.trait07_data);
    		temp_string = TV.getText().toString();
    		if(TextUtils.isEmpty(temp_string)){
    	        // EditText was empty
    	        // so no real data collected just break out
    			trait07_data = 0.0f;
//    			Log.i("save trait7", "float data is " + String.valueOf(trait07_data));
    			trait07_units = 0;
    	    }
    		else {
    			trait07_data = Float.valueOf(temp_string);
//        		Log.i("save trait7", "float data is " + String.valueOf(trait07_data));
//        		Log.i("trait07_units ", String.valueOf(trait07_units));
    		}
    		// I need to get the traits scored for this pass here:
    		String mytoday = TodayIs();
//    		Log.i("Date is ", mytoday);
    		//	Set the alert for this sheep so there is a note that the evaluation is done
    		
    		
    		
    		// Now that I have all the data I need to write it into the sheep_evaluation_table
       		
    		cmd = String.format("insert into sheep_evaluation_table (sheep_id, " +
    		"trait_name01, trait_score01, trait_name02,trait_score02, " +
    		"trait_name03,trait_score03, trait_name04, trait_score04, trait_name05, trait_score05, " +
    		"trait_name06,trait_score06, trait_name07, trait_score07, " +
    		"trait_units06, trait_units07, eval_date) " +
    		"values (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,'%s') ", 
    				sheep_id, trait01, trait01_data, trait02, trait02_data, trait03, trait03_data,
    				trait04, trait04_data, trait05, trait05_data, trait06, trait06_data,
    				trait07, trait07_data, trait06_units, trait07_units, mytoday );
    		
//    		Log.i("save eval ", cmd);
    		dbh.exec( cmd );
    		String alert_text = "Evaluation Done";
    		cmd = String.format("update sheep_table set alert01='%s' where sheep_id=%d", alert_text, sheep_id);
//    		Log.i("test alert ", cmd);   
    		dbh.exec( cmd );
    		clearBtn( null );
    }
	
	   public void backBtn( View v )
	    {
			doUnbindService();
			stopService(new Intent(EvaluateSheep.this, eidService.class));
			dbh.closeDB();
			clearBtn( null );   	
			finish();
	    }
	 
	public void showAlert(View v)
	{
		String	alert_text;
		String 			dbname = getString(R.string.real_database_file); 
        String          cmd;    
        Object 			crsr;
        	dbh = new DatabaseHandler( this, dbname );
		// Display alerts here   	
				AlertDialog.Builder builder = new AlertDialog.Builder( this );
				cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_id =%d", sheep_id);
//				Log.i("get alert ", cmd);  
				crsr = dbh.exec( cmd );
		        cursor   = ( Cursor ) crsr;
		        dbh.moveToFirstRecord();		       
		        alert_text = (dbh.getStr(0));
				builder.setMessage( alert_text )
			           .setTitle( R.string.alert_warning );
				builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int idx) {
			               // User clicked OK button   	  
			               }
			       });		
				AlertDialog dialog = builder.create();
				dialog.show();
				cursor.close();
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
		trait01_ratingbar = (RatingBar) findViewById(R.id.trait01_ratingbar);
		trait01_ratingbar.setRating(0.0f);
		trait02_ratingbar = (RatingBar) findViewById(R.id.trait02_ratingbar);
		trait02_ratingbar.setRating(0.0f);
		trait03_ratingbar = (RatingBar) findViewById(R.id.trait03_ratingbar);
		trait03_ratingbar.setRating(0.0f);
		trait04_ratingbar = (RatingBar) findViewById(R.id.trait04_ratingbar);
		trait04_ratingbar.setRating(0.0f);
		trait05_ratingbar = (RatingBar) findViewById(R.id.trait05_ratingbar);
		trait05_ratingbar.setRating(0.0f);
//		Log.i("Clear btn", "After clear rating bars");
		TV = (TextView) findViewById( R.id.trait06_data );
		TV.setText ( "" );
		TV = (TextView) findViewById( R.id.trait07_data );
		TV.setText ( "" );
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
			return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) ;
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
//   	String LastEID ;
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
    	fedtagid = dbh.getInt(4); // Get the id_info_table.id_infoid from the database
//		Log.i("Evaluate", String.valueOf(fedtagid));
		
    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	sheep_id = dbh.getInt(1);
    	TV = (TextView) findViewById(R.id.fedText)	;
    	TV.setText(dbh.getStr(3));
    	String alert_text = dbh.getStr(6);
//    	Now to test of the sheep has an alert and if so then set the alerts button to red
		if (alert_text != null){
	       	// make the alert button red
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true);   
		}
		
    	ii = dbh.getInt(1);
    	
//		Now we need to get the farm tag for that sheep and fill the display with data
    	
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"id_info_table.tag_number, " +
		"id_info_table.id_infoid, id_info_table.tag_date_off " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off is null and id_info_table.sheep_id='%s'", ii);

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
    	if (alert_text != null){
	       	// make the alert button red
	    	Button btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
	    	btn.setEnabled(true);   
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
