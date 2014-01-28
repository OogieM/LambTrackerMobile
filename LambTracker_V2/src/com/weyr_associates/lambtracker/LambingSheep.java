package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.List;
import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import com.weyr_associates.lambtracker.ConvertToEID.IncomingHandler;
import android.app.Activity;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

public class LambingSheep extends ListActivity
{

		private DatabaseHandler dbh;
		int             id;
		String 			logmessages;
		public int 		thissheep_id;
		int             fedtagid, farmtagid, eidtagid;
		
		public String 	tag_type_label, tag_color_label, tag_location_label, eid_tag_color_label ;
		public String 	eid_tag_location_label, eidText, alert_text;
		public Cursor 	cursor, cursor2;

		public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner ;
		public List<String> tag_types, tag_locations, tag_colors;
		
		public String[] this_sheeps_tags ;
		
		private int             nRecs;
		private int			    recNo;
		private String[]        colNames;
		
		int[] tagViews;

		ArrayAdapter<String> dataAdapter;
		String     	cmd;
		Integer 	i;	
		public Button btn;
		
		public SimpleCursorAdapter myadapter;

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
					stopService(new Intent(LambingSheep.this, eidService.class));

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
				startService(new Intent(LambingSheep.this, eidService.class));
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
//			View v = null;
			Object crsr;
			//	make the scan eid button red
			btn = (Button) findViewById( R.id.scan_eid_btn );
			btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
//			String eid = this.getIntent().getExtras().getString("com.weyr_associates.lambtracker.LastEID");
//	    	Log.i("LookUpSheep", " before input text " + eid);  
//	    	Log.i("LookUpSheep", " before input text " + LastEID);  
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
//			Added the variable definitions here    	
	      	String          cmd;
	      	String 			results, results2;
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

			// 		here is where I put the actual rcvd eid into the eid variable
//	    	Log.i("LookUpSheep", " Before get extras");
//	    	String eid = this.getIntent().getExtras().getString("com.weyr_associates.lambtracker.LastEID");
//	    	Log.i("LookUpSheep", " before input text " + eid);  
//	        TextView TV = (TextView) findViewById( R.id.inputText );
//	        TV.setText (eid);
//	        eidText = eid;
	       
	        // Moved the rest of the code into a separate method so I can call it from several places.
	        
//	        Log.i("LookUpSheep", " after set text " + eid);         
//	        exists = true;
	       
//	        exists = tableExists("sheep_table");
//	        if (exists){
//	        	if( eid != null && eid.length() > 0 ){
////	        		Get the sheep id from the id table for this EID tag number
//		        	cmd = String.format( "select sheep_id from id_info_table where tag_number='%s'", eid );  
//		        	
//		        	dbh.exec( cmd );
//		        	dbh.moveToFirstRecord();
//		        	thissheep_id = dbh.getInt(0);
//		        
//		        	Log.i("LookUpSheep", "This sheep is record " + String.valueOf(thissheep_id));
//		        	Log.i("LookUpSheep", " Before finding all tags");
//		        	
//		    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
//		    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
//		    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01 " +
//		    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
//		    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
//		    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
//		    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
//		    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", thissheep_id);
	//
//		    		crsr = dbh.exec( cmd ); 
//		    		
//		    		cursor   = ( Cursor ) crsr; 
//		    		startManagingCursor(cursor);
	//
//		    		recNo    = 1;
//					nRecs    = cursor.getCount();
//					colNames = cursor.getColumnNames();
//					nrCols   = colNames.length;
//					
//					cursor.moveToFirst();				
//					TV = (TextView) findViewById( R.id.sheepnameText );
//			        TV.setText (dbh.getStr(0));
//			        
//			    	Log.i("LookUpSheep", " before formatting results");
					
					//	Get set up to try to use the CursorAdapter to display all the tag data
					//	Select only the columns I need for the tag display section
//			        String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
					//	Set the views for each column for each line. A tag takes up 1 line on the screen
//			        int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
//					SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor ,fromColumns, toViews, 0);
//					setListAdapter(adapter);

					// Now we need to get the alert text for this sheep
//					alert_text = dbh.getStr(8);
//					//	Now to test of the sheep has an alert and if so then display the alert
//					if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
//				       	// Show the alert		  
//						showAlert(v);
//		        	}
//	        	}else{
//		        	return;
//		        }
//		        Log.i("LookUpSheep", " out of the if statement");
//	        	dbh.moveToFirstRecord();
//	        	if( dbh.getSize() == 0 ){
//	        		TV = (TextView) findViewById( R.id.eidText );
//	            	TV.setText( eid );
//	            	TV = (TextView) findViewById( R.id.sheepnameText );
//	            	TV.setText( "Cannot find requested EID tag." );
//	            	return;
//	        	} 
//	        	}
//	    		else {
//	    			clearBtn( null );
//	            	TV = (TextView) findViewById( R.id.sheepnameText );
//	                TV.setText( "Sheep Database does not exist." );    			
//	        	}
//	        	lookForSheep ();
	        }
		public void lookForSheep (View v){

			int     nrCols;
			Object crsr;
			Boolean exists;
			TextView TV;
	        exists = true;
	     // Hide the keyboard when you click the button
	    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
	    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	    	
	        TV = (TextView) findViewById( R.id.inputText );
	    	String	tag_num = TV.getText().toString();
	    	
	        Log.i("LookForSheep", " got to lookForSheep with Tag Number of " + tag_num);
	        exists = tableExists("sheep_table");
	        if (exists){
	        	if( tag_num != null && tag_num.length() > 0 ){
//	        		Get the sheep id from the id table for this tag number and selected tag type
		        	cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
		        			"and id_info_table.tag_type='%s' ", tag_num , tag_type_spinner.getSelectedItemPosition());  	        	
		        	dbh.exec( cmd );
		        	dbh.moveToFirstRecord();
		        	if( dbh.getSize() == 0 )
			    		{ // no sheep with that  tag in the database so clear out and return
			    		clearBtn( v );
			    		TV = (TextView) findViewById( R.id.sheepnameText );
			        	TV.setText( "Cannot find this sheep." );
			        	return;
			    		}
		        	thissheep_id = dbh.getInt(0);
		        
		        	Log.i("LookForSheep", "This sheep is record " + String.valueOf(thissheep_id));
		        	Log.i("LookForSheep", " Before finding all tags");
		        	
		    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
		    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01 " +
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
					nrCols   = colNames.length;
					
					cursor.moveToFirst();				
					TV = (TextView) findViewById( R.id.sheepnameText );
			        TV.setText (dbh.getStr(0));
			        
			    	Log.i("lookForSheep", " before formatting results");
					
					//	Get set up to try to use the CursorAdapter to display all the tag data
					//	Select only the columns I need for the tag display section
			        String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
					//	Set the views for each column for each line. A tag takes up 1 line on the screen
			        int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
					myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor ,fromColumns, toViews, 0);
					setListAdapter(myadapter);

					// Now we need to get the alert text for this sheep
					alert_text = dbh.getStr(8);
					//	Now to test of the sheep has an alert and if so then display the alert
					if (alert_text != null && !alert_text.isEmpty()){
//					if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
							// Show the alert		  			
						showAlert(v);
		        	}
	        	}else{
		        	return;
		        }
//		        Log.i("lookForSheep", " out of the if statement");
	        	}
	    		else {
	    			clearBtn( null );
	            	TV = (TextView) findViewById( R.id.sheepnameText );
	                TV.setText( "Sheep Database does not exist." ); 
	                
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
			   	//	make the scan eid button  0x0000FF00, 0xff00ff00
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
			builder.setMessage( R.string.help_lambing_sheep )
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
			stopService(new Intent(LambingSheep.this, eidService.class));   	
	    	// Added this to close the database if we go back to the main activity  	
	    	stopManagingCursor (cursor);
	    	dbh.closeDB();
	    	clearBtn( null );
	    	//Go back to main
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
			//	Need to clear out the rest of the tags here but only if we've actually looked for tags.
			try {
				myadapter.changeCursor(null);
			}
			catch (Exception e) {
				// In this case there is no adapter so do nothing
			}		
	    }   
	    
}

	

