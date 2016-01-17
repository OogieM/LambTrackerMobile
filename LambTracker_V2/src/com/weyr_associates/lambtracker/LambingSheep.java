package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.ListActivity;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import com.weyr_associates.lambtracker.IDManagement.IncomingHandler;
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
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

public class LambingSheep extends ListActivity
{
		private DatabaseHandler dbh;
		int             id;
		String 			logmessages;
		public int 		thissheep_id;
		public int		codon171, codon154, codon136;
		int             fedtagid, farmtagid, eidtagid;
		
		public String 	tag_type_label, tag_color_label, tag_location_label, eid_tag_color_label ;
		public String 	eid_tag_location_label, eidText, alert_text;
		public Cursor 	cursor, cursor2, cursor3, cursor4, cursor5;
		public Object 	crsr, crsr2, crsr3, crsr4, crsr5;
		public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner ;
		public Spinner predefined_note_spinner01, predefined_note_spinner02, predefined_note_spinner03;
		public Spinner predefined_note_spinner04, predefined_note_spinner05;
		public List<String> predefined_notes;
		public List<String> tag_types, tag_locations, tag_colors;
		
		public String[] this_sheeps_tags ;
		
		private int             nRecs, nRecs5;
		private int			    recNo;
		private String[]        colNames;
		
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
					//We have a good whole EID number	
					Log.i ("in handler case" , "got eid of " + LastEID);
					gotEID ();	
					break;			
				case eidService.MSG_UPDATE_LOG_APPEND:
					//Bundle b3 = msg.getData();
					//Log.i("Lambing ", "Add to Log.");

					break;
				case eidService.MSG_UPDATE_LOG_FULL:
					//Log.i("Lambing ", "Log Full.");

					break;
				case eidService.MSG_THREAD_SUICIDE:
					Log.i("Lambing ", "Service informed Activity of Suicide.");
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
				Log.i("Lambing ", "At Service.");
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
			Log.i("lambing sheep ", "At isRunning?.");
			if (eidService.isRunning()) {
				Log.i("Lambing ", "is running.");
				doBindService();
			} else {
				Log.i("lambing", "is not, start it");
				startService(new Intent(LambingSheep.this, eidService.class));
				doBindService();
			}
			Log.i("Lambing ", "Done isRunning.");
		} 	

		void doBindService() {
			// Establish a connection with the service.  We use an explicit
			// class name because there is no reason to be able to let other
			// applications replace our component.
			Log.i("lambing", "At doBind1.");
			bindService(new Intent(this, eidService.class), mConnection, Context.BIND_AUTO_CREATE);
			Log.i("lambing ", "At doBind2.");

			mIsBound = true;

			if (mService != null) {
				Log.i("Lambing ", "At doBind3.");
				try {
					//Request status update
					Message msg = Message.obtain(null, eidService.MSG_UPDATE_STATUS, 0, 0);
					msg.replyTo = mMessenger;
					mService.send(msg);
					Log.i("Lambing ", "At doBind4.");
					//Request full log from service.
					msg = Message.obtain(null, eidService.MSG_UPDATE_LOG_FULL, 0, 0);
					mService.send(msg);
				} catch (RemoteException e) {}
			}
			Log.i("Lambing ", "At doBind5.");
		}
		void doUnbindService() {
			Log.i("Lambing ", "At DoUnbindservice");
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
//			String eid = this.getIntent().getExtras().getString("com.weyr_associates.lambtracker.LastEID");
//	    	Log.i("LookUpSheep", " before input text " + eid);  
	    	Log.i("LookUpSheep", " before input text " + LastEID);  
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
	        setContentView(R.layout.lambing_sheep);
	        Log.i("LambingSheep", " after set content view");
	        View v = null;

//			Added the variable definitions here    	
	      	String          cmd;
	      	
	    	 //////////////////////////////////// 
			CheckIfServiceIsRunning();
//			LoadPreferences (true);
			Log.i("Lambing ", "back from isRunning");  	
			////////////////////////////////////    	
			thissheep_id = 0;

			Log.i("lambing"," onCreate before setting buttons");
	       	// make the alert button normal and disabled
	    	btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
	    	btn.setEnabled(false);  
	    	
	       	//	Disable the Next Record and Prev. Record button until we have multiple records
	    	btn = (Button) findViewById( R.id.next_rec_btn );
	    	btn.setEnabled(false); 
	    	btn = (Button) findViewById( R.id.prev_rec_btn );
	    	btn.setEnabled(false);
	    	
//	    	make the scan eid button red
			btn = (Button) findViewById( R.id.scan_eid_btn );
			btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
			Log.i("lambing"," onCreate after setting buttons");
	        }
		
		public void lookForSheep (View v){
			Log.i("lookforsheep"," at beginning of look for sheep");
			int 	lamb01_id, lamb02_id, lamb03_id, lambs_born;
			Boolean exists;
			TextView TV;
			String 	lambingdate ;
			String tempString;
	        exists = true;
	     // Hide the keyboard when you click the button
	    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
	    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	    	//	empty the lamb records
	    	lamb01_id = 0;
	    	lamb02_id = 0;
	    	lamb03_id = 0;
	    	lambs_born = 0;
	    	
	    	//	empty the codon records
	    	codon171 = 0;
	    	codon154 = 0;
	    	codon136 = 0;
	    	
	        TV = (TextView) findViewById( R.id.inputText );	        
	    	String	tag_num = TV.getText().toString();
	    	
	    	// Clear fields before we do anything
	    	clearBtn(v);
	    	// Set back tag_num for cosmetic appearance
	    	TV.setText(tag_num);

	    	ListView historylist = (ListView) findViewById(R.id.list2);
	    	ListView lambtags01 = (ListView) findViewById(R.id.list3);
	    	ListView lambtags02 = (ListView) findViewById(R.id.list4);
	    	ListView lambtags03 = (ListView) findViewById(R.id.list5);
	    	
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
//					        	formatSheepRecord(v);   	
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
//				        	formatSheepRecord(v);   	
					        break;
	    				} // end of case switch 
	    	
    	
	    	
//	        exists = tableExists("sheep_table");
//	        if (exists){
//	        	if( tag_num != null && tag_num.length() > 0 ){
////	        		Get the sheep id from the id table for this tag number and selected tag type
//		        	cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
//		        			"and id_info_table.tag_type='%s' ", tag_num , tag_type_spinner.getSelectedItemPosition());  	        	
//		        	
//		        	crsr = dbh.exec( cmd ); 	    		
//		    		cursor   = ( Cursor ) crsr; 
////		    		startManagingCursor(cursor);
//					cursor.moveToFirst();	
//					nRecs    = cursor.getCount();
////					dbh.exec( cmd );
////		        	dbh.moveToFirstRecord();
//		        	if( nRecs == 0 )
//			    		{ // no sheep with that tag in the database so clear out and return
//			    		clearBtn( v );
//			    		TV = (TextView) findViewById( R.id.sheepnameText );
//			        	TV.setText( "Cannot find this sheep." );
//			        	return;
//			    		}
//		        	thissheep_id = dbh.getInt(0);
		        	Log.i("LookForSheep", "This sheep is record " + String.valueOf(thissheep_id));
		        	//	Go get the sex of this sheep
		        	cmd = String.format( "select sheep_table.sex, sheep_table.remove_date from sheep_table where sheep_id = %s",thissheep_id);
		        	crsr = dbh.exec( cmd ); 	    		
		    		cursor   = ( Cursor ) crsr; 
//		    		startManagingCursor(cursor);
					cursor.moveToFirst();				
					i =  (dbh.getInt(0));
					tempString = (dbh.getStr(1));
					Log.i("LookForSheep", "This sheep is sex " + String.valueOf(i));
					Log.i("LookForSheep", "This sheep was removed on " + tempString);
					Log.i("LookForSheep", "Remove string length is " + String.valueOf(tempString.length()));
					if (i != 2) {
						// This is not a ewe so set the name to not a ewe, clear out and return
						clearBtn( v );
			    		TV = (TextView) findViewById( R.id.sheepnameText );
			        	TV.setText( "This is not a ewe." );
			        	return;
					}

					if (!tempString.trim().equals("")) {				
						// This is a removed sheep so set the name to removed, clear out and return
						clearBtn( v );
			    		TV = (TextView) findViewById( R.id.sheepnameText );
			        	TV.setText( "This sheep was Removed on " + tempString);
			        	return;
					}
//					
		        	Log.i("LookForSheep", " Before finding all tags");		        	
		    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
		    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01, " +
		    				"sheep_table.codon171, sheep_table.codon154, sheep_table.codon136 " +
		    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
		    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
		    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null " +
		    				" order by idtype_name asc", thissheep_id);

		    		crsr = dbh.exec( cmd ); 	    		
		    		cursor   = ( Cursor ) crsr; 
		    		recNo    = 1;
					nRecs    = cursor.getCount();
					colNames = cursor.getColumnNames();
//					nrCols   = colNames.length;
					cursor.moveToFirst();				
					TV = (TextView) findViewById( R.id.sheepnameText );
			        TV.setText (dbh.getStr(0));
			        dam_name = dbh.getStr(0);
					// Now we need to get the alert text for this sheep
			        alert_text = dbh.getStr(8);
			        //	Get the Codon 171, Codon 154 and Codon 136 values for this sheep
			        codon171 = dbh.getInt(9);
			        codon154 = dbh.getInt(10);
			        codon136 = dbh.getInt(11);
			        
			    	Log.i("lookForSheep", " before formatting results");
					
					//	Get set up to try to use the CursorAdapter to display all the tag data
					//	Select only the columns I need for the tag display section
			        String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
					//	Set the views for each column for each line. A tag takes up 1 line on the screen
			        int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
					myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor ,fromColumns, toViews, 0);
					setListAdapter(myadapter);
					
					Log.i("lookForSheep", " after filling tag data");

					//	Add display the lambing history for this ewe here					
					cmd = String.format( "select lambing_history_table.lambing_historyid as _id, lambing_history_table.lambing_date, " +
							"lambing_history_table.lambing_notes, lambing_history_table.lamb01_id, lambing_history_table.lamb02_id, lambing_history_table.lamb03_id, " +
							"lambing_history_table.lambs_born as lambs_born " +
							"from lambing_history_table inner join sheep_table on sheep_table.sheep_id = lambing_history_table.dam_id " +
							" where lambing_history_table.dam_id = '%s'  " +
							"order by lambing_history_table.lambing_date desc", thissheep_id );					
					crsr2 = dbh.exec( cmd ); 	
					Log.i("lookForSheep", " after run 2nd sqlite command");
		    		cursor2   = ( Cursor ) crsr2; 
					nRecs    = cursor2.getCount();
					Log.i("lookForSheep", " nRecs is " + String.valueOf(nRecs));
					cursor2.moveToFirst();	
					if (nRecs > 0) {
						lambingdate = dbh.getStr(1);
						
						lamb01_id = dbh.getInt(3);
//						Log.i("lookForSheep", " first lamb is id " + String.valueOf(lamb01_id));
						lamb02_id = dbh.getInt(4);
//						Log.i("lookForSheep", " second lamb is id " + String.valueOf(lamb02_id));
						lamb03_id = dbh.getInt(5);
//						Log.i("lookForSheep", " third lamb is id " + String.valueOf(lamb03_id));
						lambs_born = dbh.getInt(6);
						String[] fromColumns2 = new String[ ]{ "lambing_date", "lambing_notes"};
//						Log.i("lookForSheep", " after set string array second time");
						int[] toViews2 = new int[] { R.id.lambing_date, R.id.lambing_notes};
//						Log.i("lookForSheep", " after set integer array second time");
						myadapter2 = new SimpleCursorAdapter(this, R.layout.list_entry2, cursor2 ,fromColumns2, toViews2, 0);
//						Log.i("lookForSheep", " after set myadapter2");
						historylist.setAdapter(myadapter2);
						};

					// No lambs
					lambtags01.setAdapter(null);
					lambtags02.setAdapter(null);
					lambtags03.setAdapter(null);
						
					// Add display current year lambs here if there are any
					// Don't bother looking for lambs that don't exist
					Log.i("lookForSheep", String.format("Looking for %d lambs", lambs_born));	
					switch (lambs_born) {
						
						case 3:
						// third lamb
						cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, sheep_sex_table.sex_name, id_type_table.idtype_name, " +
			    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
			    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off " +
			    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
			    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
			    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
			    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
			    				"inner join sheep_sex_table on sheep_table.sex = sheep_sex_table.sex_sheepid " +
			    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", lamb03_id);
//						Log.i("lookForSheep", " command is" + cmd);
			    		crsr5 = dbh.exec( cmd ); 
			    		Log.i("lookForSheep", " after run 4th sqlite command");
			    		cursor5   = ( Cursor ) crsr5; 
						nRecs    = cursor5.getCount();
						Log.i("lookForSheep", " number of lamb tags is "+ String.valueOf(nRecs));
						cursor5.moveToFirst();				
						if (nRecs > 0) {
							// put the lamb name up and perhaps the lamb's sex
							// lambs name is dbh.getStr (0)
							Log.i("lookForSheep", "Lamb Name is " + dbh.getStr(0));
							Log.i("lookForSheep", "Lamb sex is " + dbh.getStr(2));
							TV = (TextView) findViewById( R.id.lamb03nameText );
				        	TV.setText(dbh.getStr(0));
				        	TV = (TextView) findViewById( R.id.lamb03sexText );
				        	TV.setText(dbh.getStr(2));
							//	Get set up to try to use the CursorAdapter to display all the tag data
							//	Select only the columns I need for the tag display section
					        String[] fromColumns5 = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
							//	Set the views for each column for each line. A tag takes up 1 line on the screen
					        int[] toViews5 = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
							myadapter5 = new SimpleCursorAdapter(this, R.layout.list_entry, cursor4 ,fromColumns5, toViews5, 0);
							lambtags03.setAdapter(myadapter5);	
						}else {
							//	Clear out the id data for lamb03 in the ewe lambing display 	
							lambtags03.setAdapter(null);
						}
						
						case 2:
						// second lamb
						cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, sheep_sex_table.sex_name, id_type_table.idtype_name, " +
			    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
			    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off " +
			    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
			    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
			    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
			    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
			    				"inner join sheep_sex_table on sheep_table.sex = sheep_sex_table.sex_sheepid " +
			    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", lamb02_id);
//						Log.i("lookForSheep", " command is" + cmd);
			    		crsr4 = dbh.exec( cmd ); 
			    		Log.i("lookForSheep", " after run 4th sqlite command");
			    		cursor4   = ( Cursor ) crsr4; 
						nRecs    = cursor4.getCount();
						Log.i("lookForSheep", " number of lamb tags is "+ String.valueOf(nRecs));
						cursor4.moveToFirst();				
						if (nRecs > 0) {
							// put the lamb name up and perhaps the lamb's sex
							// lambs name is dbh.getStr (0)
							Log.i("lookForSheep", "Lamb Name is " + dbh.getStr(0));
							Log.i("lookForSheep", "Lamb sex is " + dbh.getStr(2));
							TV = (TextView) findViewById( R.id.lamb02nameText );
				        	TV.setText(dbh.getStr(0));
				        	TV = (TextView) findViewById( R.id.lamb02sexText );
				        	TV.setText(dbh.getStr(2));
							//	Get set up to try to use the CursorAdapter to display all the tag data
							//	Select only the columns I need for the tag display section
					        String[] fromColumns4 = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
							//	Set the views for each column for each line. A tag takes up 1 line on the screen
					        int[] toViews4 = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
							myadapter4 = new SimpleCursorAdapter(this, R.layout.list_entry, cursor4 ,fromColumns4, toViews4, 0);
							lambtags02.setAdapter(myadapter4);	
						}else {
							//	Clear out the id data for lamb02 in the ewe lambing display 	
							lambtags02.setAdapter(null);
						}
						
						case 1:
						// First lamb
						cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, sheep_sex_table.sex_name, id_type_table.idtype_name, " +
			    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
			    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off " +
			    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
			    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
			    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
			    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
			    				"inner join sheep_sex_table on sheep_table.sex = sheep_sex_table.sex_sheepid " +
			    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", lamb01_id);
//						Log.i("lookForSheep", " command is" + cmd);
			    		crsr3 = dbh.exec( cmd ); 
//			    		Log.i("lookForSheep", " after run 3rd sqlite command");
			    		cursor3   = ( Cursor ) crsr3; 
						nRecs    = cursor3.getCount();
//						Log.i("lookForSheep", " number of lamb tags is "+ String.valueOf(nRecs));
//						colNames = cursor3.getColumnNames();
//						nrCols   = colNames.length;
						cursor3.moveToFirst();				
						if (nRecs > 0) {
							// put the lamb name up and the lamb's sex
							// lambs name is dbh.getStr (0)
							Log.i("lookForSheep", "Lamb Name is " + dbh.getStr(0));
							Log.i("lookForSheep", "Lamb sex is " + dbh.getStr(2));
							TV = (TextView) findViewById( R.id.lamb01nameText );
				        	TV.setText(dbh.getStr(0));
				        	TV = (TextView) findViewById( R.id.lamb01sexText );
				        	TV.setText(dbh.getStr(2));
				        	
							//	Get set up to try to use the CursorAdapter to display all the tag data
							//	Select only the columns I need for the tag display section
					        String[] fromColumns3 = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
							//	Set the views for each column for each line. A tag takes up 1 line on the screen
					        int[] toViews3 = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
							myadapter3 = new SimpleCursorAdapter(this, R.layout.list_entry, cursor3 ,fromColumns3, toViews3, 0);
							lambtags01.setAdapter(myadapter3);	
						}else {
							//	Clear out the id data for lamb01 in the ewe lambing display 	
							lambtags01.setAdapter(null);
						}
						default:
					}
						
						
						
					//	Now to test of the sheep has an alert and if so then display the alert
					if (alert_text != null && !alert_text.isEmpty()){
						// if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
							// Show the alert		  			
						showAlert(v);
						
						// make the alert button red and enabled
				    	btn = (Button) findViewById( R.id.alert_btn );
				    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
				    	btn.setEnabled(true);  				       
		        	}
	        	}else{
		        	return;
		        }
//		        Log.i("lookForSheep", " out of the if statement");
//	        	}
//	    		else {
//	    			clearBtn( null );
//	            	TV = (TextView) findViewById( R.id.sheepnameText );
//	                TV.setText( "Sheep Database does not exist." ); 
//	        	}
		}
	
		@Override
		public void onResume (){
	    	
			Log.i("Start resume", " of lambing sheep");
			super.onResume();
			Log.i("after ", "super onResume of lambing sheep");
			
			String 	dbfile = getString(R.string.real_database_file) ;
	        Log.i("LookUpSheep", " after get database file");
	    	dbh = new DatabaseHandler( this, dbfile );
	    	
			int 	lamb01_id, lamb02_id, lamb03_id, lambs_born;
//			Object crsr, crsr2, crsr3, crsr4, crsr5;
			TextView TV;
			String 	lambingdate ;
			Log.i("in resume", " of lambing sheep");
			
			Log.i("in lambing", " this sheepid is " + thissheep_id); 
					
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
			TV = (TextView) findViewById( R.id.inputText );

			
			TV = (TextView) findViewById( R.id.lamb01nameText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb01sexText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb02nameText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb02sexText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb03nameText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb03sexText );
			TV.setText( "" );
	    	lamb01_id = 0;
	    	lamb02_id = 0;
	    	lamb03_id = 0;
	    	lambs_born = 0;
	    	Log.i("in resume", " before get listviews of all lists");
	    	ListView historylist = (ListView) findViewById(R.id.list2);
	    	ListView lambtags01 = (ListView) findViewById(R.id.list3);
	    	ListView lambtags02 = (ListView) findViewById(R.id.list4);
	    	ListView lambtags03 = (ListView) findViewById(R.id.list5);
	    	Log.i("in resume", " after get listviews of all lists");
	    	
		Log.i("inResume", " before get lamb history for this ewe");	
//		Add display the lambing history for this ewe here					
		cmd = String.format( "select lambing_history_table.lambing_historyid as _id, lambing_history_table.lambing_date, " +
				"lambing_history_table.lambing_notes, lambing_history_table.lamb01_id, lambing_history_table.lamb02_id, lambing_history_table.lamb03_id, " +
				"lambing_history_table.lambs_born as lambs_born " +
				"from lambing_history_table inner join sheep_table on sheep_table.sheep_id = lambing_history_table.dam_id " +
				" where lambing_history_table.dam_id = '%s'  " +
				"order by lambing_history_table.lambing_date desc", thissheep_id );					
		crsr2 = dbh.exec( cmd ); 	
		Log.i("inResume", " after get lamb history for this ewe");
		cursor2   = ( Cursor ) crsr2; 
//		startManagingCursor(cursor2);
		nRecs    = cursor2.getCount();
		Log.i("inResume", " nRecs is " + String.valueOf(nRecs));
		cursor2.moveToFirst();	
		if (nRecs > 0) {
			lambingdate = dbh.getStr(1);			
			lamb01_id = dbh.getInt(3);
			Log.i("lookForSheep", " first lamb is id " + String.valueOf(lamb01_id));
			lamb02_id = dbh.getInt(4);
//			Log.i("lookForSheep", " second lamb is id " + String.valueOf(lamb02_id));
			lamb03_id = dbh.getInt(5);
//			Log.i("lookForSheep", " third lamb is id " + String.valueOf(lamb03_id));
			lambs_born = dbh.getInt("lambs_born");
			String[] fromColumns2 = new String[ ]{ "lambing_date", "lambing_notes"};
			Log.i("lookForSheep", " after set string array second time");
			int[] toViews2 = new int[] { R.id.lambing_date, R.id.lambing_notes};
			Log.i("lookForSheep", " after set integer array second time");
			myadapter2 = new SimpleCursorAdapter(this, R.layout.list_entry2, cursor2 ,fromColumns2, toViews2, 0);
			Log.i("lookForSheep", " after set myadapter2");
			historylist.setAdapter(myadapter2);
			};

			// No lambs
				lambtags01.setAdapter(null);
				lambtags02.setAdapter(null);
				lambtags03.setAdapter(null);
				
			// Add display current year lambs here if there are any
			// Don't bother looking for lambs that don't exist
			Log.i("lookForSheep", String.format("Looking for %d lambs", lambs_born));	
			switch (lambs_born) {
			
			case 3:			
			// third lamb
				cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, sheep_sex_table.sex_name, id_type_table.idtype_name, " +
	    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
	    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off " +
	    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
	    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
	    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
	    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
	    				"inner join sheep_sex_table on sheep_table.sex = sheep_sex_table.sex_sheepid " +
	    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", lamb03_id);
//						Log.i("lookForSheep", " command is" + cmd);
	    		crsr5 = dbh.exec( cmd ); 
	    		Log.i("lookForSheep", " after run get this year third lamb sqlite command");
	    		cursor5   = ( Cursor ) crsr5; 
				nRecs    = cursor5.getCount();
				Log.i("lookForSheep", " number of lamb tags is "+ String.valueOf(nRecs));
				cursor5.moveToFirst();				
				if (nRecs > 0) {
					// put the lamb name up and the lamb's sex
					Log.i("lookForSheep", "Lamb Name is " + dbh.getStr(0));
					Log.i("lookForSheep", "Lamb sex is " + dbh.getStr(2));
					TV = (TextView) findViewById( R.id.lamb03nameText );
		        	TV.setText(dbh.getStr(0));
		        	TV = (TextView) findViewById( R.id.lamb03sexText );
		        	TV.setText(dbh.getStr(2));
					//	Get set up to try to use the CursorAdapter to display all the tag data
					//	Select only the columns I need for the tag display section
			        String[] fromColumns5 = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
					//	Set the views for each column for each line. A tag takes up 1 line on the screen
			        int[] toViews5 = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
					myadapter5 = new SimpleCursorAdapter(this, R.layout.list_entry, cursor4 ,fromColumns5, toViews5, 0);
					lambtags03.setAdapter(myadapter5);	
				}else {
					//	Clear out the id data for lamb03 in the ewe lambing display
					lambtags03.setAdapter(null);
				}
			
			case 2:			
			// second lamb
				cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, sheep_sex_table.sex_name, id_type_table.idtype_name, " +
	    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
	    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off " +
	    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
	    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
	    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
	    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
	    				"inner join sheep_sex_table on sheep_table.sex = sheep_sex_table.sex_sheepid " +
	    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", lamb02_id);
	//			Log.i("lookForSheep", " command is" + cmd);
	    		crsr4 = dbh.exec( cmd ); 
	    		Log.i("lookForSheep", " after run get this year second lamb sqlite command");
	    		cursor4   = ( Cursor ) crsr4; 
				nRecs    = cursor4.getCount();
				Log.i("lookForSheep", " number of lamb tags is "+ String.valueOf(nRecs));
				cursor4.moveToFirst();				
				if (nRecs > 0) {
					// put the lamb name up and the lamb's sex
					Log.i("lookForSheep", "Lamb Name is " + dbh.getStr(0));
					Log.i("lookForSheep", "Lamb sex is " + dbh.getStr(2));
					TV = (TextView) findViewById( R.id.lamb02nameText );
		        	TV.setText(dbh.getStr(0));
		        	TV = (TextView) findViewById( R.id.lamb02sexText );
		        	TV.setText(dbh.getStr(2));
					//	Get set up to try to use the CursorAdapter to display all the tag data
					//	Select only the columns I need for the tag display section
			        String[] fromColumns4 = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
					//	Set the views for each column for each line. A tag takes up 1 line on the screen
			        int[] toViews4 = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
					myadapter4 = new SimpleCursorAdapter(this, R.layout.list_entry, cursor4 ,fromColumns4, toViews4, 0);
					lambtags02.setAdapter(myadapter4);	
				}else {
					//	Clear out the id data for lamb02 in the ewe lambing display
					lambtags02.setAdapter(null);
				}
			
			case 1:
			// First lamb
				cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, sheep_sex_table.sex_name, id_type_table.idtype_name, " +
	    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
	    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off " +
	    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
	    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
	    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
	    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
	    				"inner join sheep_sex_table on sheep_table.sex = sheep_sex_table.sex_sheepid " +
	    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", lamb01_id);
	//						Log.i("lookForSheep", " command is" + cmd);
	    		crsr3 = dbh.exec( cmd ); 
	    		Log.i("lookForSheep", " after run get this year first lamb sqlite command");
	    		cursor3   = ( Cursor ) crsr3; 
				nRecs    = cursor3.getCount();
				Log.i("lookForSheep", " number of lamb tags is "+ String.valueOf(nRecs));
				cursor3.moveToFirst();				
				if (nRecs > 0) {
					// put the lamb name up and the lamb's sex
					Log.i("lookForSheep", "Lamb Name is " + dbh.getStr(0));
					Log.i("lookForSheep", "Lamb sex is " + dbh.getStr(2));
					TV = (TextView) findViewById( R.id.lamb01nameText );
		        	TV.setText(dbh.getStr(0));
		        	TV = (TextView) findViewById( R.id.lamb01sexText );
		        	TV.setText(dbh.getStr(2));
		        	
					//	Get set up to try to use the CursorAdapter to display all the tag data
					//	Select only the columns I need for the tag display section
			        String[] fromColumns3 = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
					//	Set the views for each column for each line. A tag takes up 1 line on the screen
			        int[] toViews3 = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
					myadapter3 = new SimpleCursorAdapter(this, R.layout.list_entry, cursor3 ,fromColumns3, toViews3, 0);
					lambtags01.setAdapter(myadapter3);	
				}else {
					//	Clear out the id data for lamb01 in the ewe lambing display 	
					lambtags01.setAdapter(null);
				}
				break;
			}
		}
		public void addLamb (View v){
			Log.i("addLamb btn", " at the beginning");	    	
			Intent i = null;
						
			if (thissheep_id == 0){
				Log.i("in lambing", " no sheep defined? ");
				return;
			}
			i = new Intent(LambingSheep.this, AddLamb.class);
			Log.i("addLamb btn", "before put extras ");
			i.putExtra("dam_name", dam_name);
			i.putExtra("dam_id", thissheep_id);
			//	Put the codon values for this ewe 
			//	Used to calculate the codon for the lambs if possible
			i.putExtra("codon171", codon171);
			i.putExtra("codon154", codon154);
			i.putExtra("codon136", codon136);
			Log.i("addLamb btn", "before start activity ");
			LambingSheep.this.startActivity(i);
			
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
			   	//	make the scan eid button green 0x0000FF00, 0xff00ff00
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
//	    	stopManagingCursor (cursor);
			try {
	    		cursor.close();
	    	}catch (Exception r)
	    	{
	    		Log.i("back btn", "cursor RunTimeException: " + r);
	    	}
	    	dbh.closeDB();
	    	clearBtn( null );
	    	//Go back to menu
	    	this.finish();
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
			TV = (TextView) findViewById( R.id.lamb01nameText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb01sexText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb02nameText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb02sexText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb03nameText );
			TV.setText( "" );
			TV = (TextView) findViewById( R.id.lamb03sexText );
			TV.setText( "" );
			//	Need to clear out the rest of the tags here but only if we've actually looked for tags.
			try {
				Log.i("lambing clrbtn", " before set ewe tags to null");
				myadapter.changeCursor(null);
			} catch (Exception e) {
				// In this case there is no adapter so do nothing
				Log.i("lambing clrbtn", " exception setting ewe tags to null");
			}	
			try {
				Log.i("lambing clrbtn", " before set lambing history to null");
				myadapter2.changeCursor(null);
			} catch (Exception e) {
				// In this case there is no adapter so do nothing
				Log.i("lambing clrbtn", " exception setting lambing history to null");
			}
			try {
				Log.i("lambing clrbtn", " before set myadapter3 first lamb tags to null");
				myadapter3.changeCursor(null);
			} catch (Exception e) {
				// In this case there is no adapter so do nothing
				Log.i("lambing clrbtn", " exception setting myadapter3 first lamb tags to null");
			}
			try {
				Log.i("lambing clrbtn", " before set myadapter4 second lamb tags to null");
				myadapter4.changeCursor(null);
			} catch (Exception e) {
				// In this case there is no adapter so do nothing
				Log.i("lambing clrbtn", " exception setting myadapter4 second lamb tags to null");
			}
			try {
				Log.i("lambing clrbtn", " before set myadapter5 third lamb tags to null");
				myadapter5.changeCursor(null);
			} catch (Exception e) {
				// In this case there is no adapter so do nothing
				Log.i("lambing clrbtn", " exception setting myadapter5 third lamb tags to null");
			}
	       	// make the alert button normal and disabled
	    	btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
	    	btn.setEnabled(false);  
	    	
	       	//	Disable the  and Prev. Record button until we have multiple records
	    	btn = (Button) findViewById( R.id.next_rec_btn );
	    	btn.setEnabled(false); 
	    	btn = (Button) findViewById( R.id.prev_rec_btn );
	    	btn.setEnabled(false);
	    	
//	    	make the scan eid button red
			btn = (Button) findViewById( R.id.scan_eid_btn );
			btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
			Log.i("lambing"," clear button after setting buttons");
			
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
//	     	thissheep_name = cursor.getString(1);
	     	Log.i("in next record", "this sheep ID is " + String.valueOf(thissheep_id));
//	     	Log.i("in next record", "this sheep name is " + thissheep_name);
	     	recNo         += 1;
//	     	findTagsShowAlert(v, thissheep_id);
//	 		// I've moved forward so I need to enable the previous record button
	 		Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
	 		btn3.setEnabled(true);	    	
	     	if (recNo == (nRecs)) {
	     		// at end so disable next record button
	     		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
	         	btn2.setEnabled(false);   		
	     	}
	     }

	     // user clicked the "previous record" button
	     public void previousRecord( View v){
//	     	Clear out the display first
	     	clearBtn( v );
	     	Log.i("in prev record", "this sheep ID is " + String.valueOf(thissheep_id));
	     	cursor.moveToPrevious();
	     	Log.i("in prev record", "after moving the cursor ");
	     	thissheep_id = cursor.getInt(0);
	     	Log.i("in prev record", "this sheep ID is " + String.valueOf(thissheep_id));
	     	recNo         -= 1;
//	     	findTagsShowAlert(v, thissheep_id);
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
