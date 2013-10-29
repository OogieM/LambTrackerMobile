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

public class LookUpSheep extends ListActivity
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

	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup_sheep);
        View v = null;
        String 	dbfile = getString(R.string.real_database_file) ;
    	dbh = new DatabaseHandler( this, dbfile );
    	Object crsr;
    	int     nrCols;
//		Added the variable definitions here    	
      	String          cmd;
      	String 			results, results2;
    	Boolean			exists;
    	
//    	//	make the scan button normal
//    	btn = (Button) findViewById( R.id.scan_eid_btn );
//    	btn.getBackground().setColorFilter(null);
//    	
//     	// Fill the Tag Type Spinner
//     	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
//    	tag_types = new ArrayList<String>();      	
//    	
//    	// Select All fields from id types to build the spinner
//        cmd = "select * from id_type_table";
//        crsr = dbh.exec( cmd );  
//        cursor   = ( Cursor ) crsr;
//    	dbh.moveToFirstRecord();
//    	tag_types.add("Select a Type");
//         // looping through all rows and adding to list
//    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
//    		tag_types.add(cursor.getString(2));
//    	}
//    	cursor.close();    	
//    	
//    	// Creating adapter for spinner
//    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
//		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		tag_type_spinner.setAdapter (dataAdapter);
//		tag_type_spinner.setSelection(2);	
    			
//		
// 		here is where I put the actual rcvd eid into the eid variable
        String eid = this.getIntent().getExtras().getString("com.weyr_associates.lambtracker.LASTEID");
        TextView TV = (TextView) findViewById( R.id.inputText );
        TV.setText (eid);
        Log.i("LookUpSheep", eid);         
        exists = true;
       
        exists = tableExists("sheep_table");
        if (exists){
        	if( eid != null && eid.length() > 0 ){
//        		Get the sheep id from the id table for this EID tag number
	        	cmd = String.format( "select sheep_id from id_info_table where tag_number='%s'", eid );      	
	        	Log.i("LookUpSheep", cmd);
	        	dbh.exec( cmd );
//	        	Log.i("LookUpSheep", " after the command");
	        	dbh.moveToFirstRecord();
	        	thissheep_id = dbh.getInt(0);
	        
	        	Log.i("LookUpSheep", "This sheep is record " + String.valueOf(thissheep_id));
	        	Log.i("LookUpSheep", " Before finding all tags");
	        	
	    		cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
	    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
	    				"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01 " +
	    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
	    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
	    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
	    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
	    				"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", thissheep_id);
	    		Log.i("LookUpSheep", cmd);
	    		crsr = dbh.exec( cmd ); 
	    		//TODO
	    		cursor   = ( Cursor ) crsr; 
	    		startManagingCursor(cursor);
	    		Log.i("LookUpSheep", " Before FOR loop where I need to read the tag data from cursor and fill display");
	    		
	    		// Print a log of all the retrieved data here to see what we really got and make sure the query is correct
	    		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(0));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(1));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(2));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(3));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(4));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(5));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(6));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(7));
	    			Log.i("LookUpSheep", " FOR Loop " + dbh.getStr(8));
	         		}
	    		Log.i("LookUpSheep", " After finding all tags");
	    		recNo    = 1;
				nRecs    = cursor.getCount();
				colNames = cursor.getColumnNames();
				nrCols   = colNames.length;
				
				for( int i = 0; i < nrCols; i++ )
				{
					//	verify the column names are correct and that I have a _id field so I can use a cursorAdapter
					Log.i("LookUpSheep", String.valueOf (colNames[i]));
				}
				cursor.moveToFirst();				
				TV = (TextView) findViewById( R.id.sheepnameText );
		        TV.setText (dbh.getStr(0));
		        
		    	Log.i("LookUpSheep", " before formatting results");
		    	// TODO
		    	//	Put this in to verify that the data really is there and can display in a regular textView
		    	results = "";		    
		    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			    	results = results + formatRecord( cursor );
			    	cursor.moveToNext();
        		}
		    	TV = (TextView) findViewById( R.id.TextView1 );
				TV.setText( results );
				
				//	Get set up to try to use the CursorAdapter to display all the tag data
				cursor.moveToFirst();
				//	Select only the columns I need for the tag display section
		        String[] fromColumns = new String[ ]{"_id", "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
				//	Set the views for each column for each line. A tag takes up 1 line on the screen
		        int[] toViews = new int[] {R.id.record_id, R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
				SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor ,fromColumns, toViews);
				
				// various things I've tried but nothing is working
//				ListView list = getListView();
//				ListView list = (ListView) findViewById(R.id.list);  		    
				setListAdapter(adapter);

				// Now we need to check and see if there is an alert for this sheep
				alert_text = dbh.getStr(8);
				Log.i("LookUpSheep", " alert text is " + alert_text);
				//	Now to test of the sheep has an alert and if so then display the alert
				if (alert_text != null){
			       	// Show the alert		  
					showAlert(v);
	        	}
        	}else{
	        	return;
	        }
	        Log.i("LookUpSheep", " out of the if statement");
        	dbh.moveToFirstRecord();
        	if( dbh.getSize() == 0 ){
        		TV = (TextView) findViewById( R.id.eidText );
            	TV.setText( eid );
            	TV = (TextView) findViewById( R.id.sheepnameText );
            	TV.setText( "Cannot find requested EID tag." );
            	return;
        	} 
        	}
    		else {
    			clearBtn( null );
            	TV = (TextView) findViewById( R.id.sheepnameText );
                TV.setText( "Sheep Database does not exist." );    			
        	}
         cursor.close();
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
		builder.setMessage( R.string.help_ground_truth_tags )
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
    	// Added this to close the database if we go back to the main activity  	
    	dbh.closeDB();
    	clearBtn( null );
    	//Go back to main
      	finish();
	    }
 
    public void showAlert (View v){
//    		String	alert_text;
            String          cmd;    
            Object 			crsr2;
            
    		// Display alerts here   	
    				AlertDialog.Builder builder = new AlertDialog.Builder( this );
//    				cmd = String.format("select sheep_table.alert01 from sheep_table where sheep_id =%d", thissheep_id);
//    				Log.i("get alert ", cmd);  
//    				crsr2 = dbh.exec( cmd );
//    		        cursor2   = ( Cursor ) crsr2;
//    		        dbh.moveToFirstRecord();		       
//    		        alert_text = (dbh.getStr(0));
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
//    				cursor2.close();	
    	}

   
    // user clicked 'clear' button
    public void clearBtn( View v )
	    {
//	    TextView TVeid  = (TextView) findViewById( R.id.eidText );
//	    TVeid.setText( "" );
//	    TextView TVfed  = (TextView) findViewById( R.id.fedText );
//	    TVfed.setText( "" );
//	    TextView TVfarm  = (TextView) findViewById( R.id.farmText );
//	    TVfarm.setText( "" );
//	    TextView TV = (TextView) findViewById( R.id.sheepnameText );
//	    TV.setText( "" );
//	    TextView TVbirthtype = (TextView) findViewById( R.id.sheepbirthtypeText );
//	    TVbirthtype.setText( "" );
//	    TextView TVbirthweight = (TextView) findViewById( R.id.sheepbirthweightText );
//	    TVbirthweight.setText( "" );
//	    TV = (TextView) findViewById( R.id.sheeptaskText );
//	    TV.setText( "" );
//	    TextView TVlambing2012 = (TextView) findViewById( R.id.lambing2012Text );
//	    TVlambing2012.setText( "" );
//	    TextView TVlambing2013 = (TextView) findViewById( R.id.lambing2013Text );
//	    TVlambing2013.setText( "" );
	    id = 0;
	    
    }
    private String formatRecord( Cursor crsr )
	{
    	String        line;
    	Log.i("formatRecord", " Got to the format record section");
	StringBuilder sb       = new StringBuilder();
	Log.i("formatRecord", " After the String Builder definition");
	int           nrCols   = colNames.length;
	Log.i("formatRecord", " number of columns is " + String.valueOf (nrCols));
//	line     = String.format( "Record %d of %d:\n", recNo, nRecs );
	Log.i("formatRecord", " number of records is " + String.valueOf (nRecs));
//	sb.append( line );
	
	Log.i("formatRecord", " number of columns is " + String.valueOf (nrCols));
	
	for( int ii = 0; ii < nRecs; ii++ )
	{	
		for( int i = 0; i < nrCols; i++ )
			{
			switch( cursor.getType(i) )
				{
				case Cursor.FIELD_TYPE_FLOAT:
					line = String.format( "  %s: %f\n", colNames[i], cursor.getFloat(i) );
//					line = String.format( "%f\n", cursor.getFloat(i) );
					break;
				
				case Cursor.FIELD_TYPE_INTEGER:
					line = String.format( "  %s: %d\n", colNames[i], cursor.getInt(i) );
//					line = String.format( "%d\n", cursor.getInt(i) );
					break;
				
				case Cursor.FIELD_TYPE_NULL:
					line = String.format( "  %s: null\n", colNames[i] );
//					line = String.format( "null\n", colNames[i] );
					break;
				
				case Cursor.FIELD_TYPE_STRING:
					line = String.format( "  %s: %s\n", colNames[i], cursor.getString(i) );
//					line = String.format( "%s\n", cursor.getString(i) );
					break;
					
				default:
					line = String.format( "  %s: ?? %s ??", colNames[i], cursor.getString(i) );
//					line = String.format( "%s ", cursor.getString(i) );
					break;
				}			
			sb.append( line );
			}
	}
	return sb.toString();
	}   
    
	}
