package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.database.Cursor;

public class EvaluateSheep extends Activity {
	private DatabaseHandler dbh;
	private Cursor 	cursor;
	private ArrayList<String> results = new ArrayList<String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluate_sheep);
        String 			dbname = getString(R.string.real_database_file); 
        String          cmd;
        Object 			crsr;
        dbh = new DatabaseHandler( this, dbname );
    	
        cmd = "select trait_name from evaluation_trait_table order by id_traitid asc";
        crsr = dbh.exec( cmd );   	
        cursor = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
    	
    	if (cursor != null ) {
    	    if  (cursor.moveToFirst()) {
    	        int i = 0;
    	        do {
    	            i++;
    	            String name = cursor.getString(cursor.getColumnIndex("trait_name"));
    	            results.add(name);
    	        }while (cursor.moveToNext());
    	    } 
    	} 
    	// add the  mods to this code to get the list into a list view in the view
    	// need to fix the xml file first
    	//example I am using is from here:
    	// http://stackoverflow.com/questions/12770206/listview-populated-by-sql-db-with-onlistitemclick-to-fill-textviews-in-other-act
    	
//        ListView hotelslist = (ListView) findViewById(android.R.id.list);
//        hotelslist.setAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, results));
//       getListView().setTextFilterEnabled(true);
    	
    	// then add the rest of the stuff for sheep evaluations here:
       	}
	   public void backBtn( View v )
	    {
      	dbh.closeDB();
   	clearBtn( null );   	
   	finish();
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
	        	  
	    		   clearBtn( null );
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();
		
    }
	public void clearBtn( View v )
    {
		// clear out the display of everything
		TextView TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
    
    }
}
