package com.weyr_associates.lambtracker;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.database.Cursor;

public class ConvertToEID extends Activity {
	private DatabaseHandler dbh;
	int             id;
	private Cursor 	cursor;
	private int			    recNo;
	private int             nRecs;
	private String[]        colNames; // Left in because I will need it for Convert to EID task
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert_to_eid);
        String dbname = getString(R.string.real_database_file); 
    	dbh = new DatabaseHandler( this, dbname );
    	
    	//	make the remove tag button red
    	Button btn = (Button) findViewById( R.id.remove_tag_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
 
    	//	Disable the Next Record and Prev. Record button until we have multiple records
//       	Button btn2 = (Button) findViewById( R.id.next_rec_btn );
//    	btn2.setEnabled(false); 
//    	Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
//    	btn3.setEnabled(false);
//    	id = 0;
       	}
    
    // user clicked the 'back' button
    public void backBtn( View v )
	    {
       	dbh.closeDB();
    	clearBtn( null );
    	finish();
	    }
    
    // user clicked 'clear' button
    public void clearBtn( View v )
	    {
	    // clear out the display of everything
	    
    }
 // user clicked 'Search Fed' button
    public void searchFedTag( View v )
    	{
    	String          cmd;

    	TextView		TV = (TextView) findViewById( R.id.inputText );
    	String			fed = TV.getText().toString();
 		// Start of the actual code to process the button click
    	if( fed != null && fed.length() > 0 )
    		{
//Need to figure out the proper SQL select for here
    		cmd = String.format( "select sheep_table.sheep_name, id_type_table.idtype_name, " +
    				"tag_colors_table.tag_color_name, id_info_table.tag_number from sheep_table inner" +
    				" join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id" +
    				" left outer join tag_colors_table on id_info_table.tag_color_male = " +
    				"tag_colors_table.tag_colors_id inner join id_type_table on id_info_table.tag_type = " +
    				"id_type_table.id_typeid" +
    				"where id_info_table.tag_date_off is NULL and id_info_table.tag_type = 1 and" +
    				" id_info_table.tag_number='%s'", fed);
    		}	
    	else
    	{
    		return;
     	}
    	Object crsr = dbh.exec( cmd );   	
    	dbh.moveToFirstRecord();
    	if( dbh.getSize() == 0 )
    		{
//    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheeptaskText );
        	TV.setText( "Cannot find requested sheep." );
        	return;
    		}
    	if( dbh.getSize() >1){

// 			Enable the previous and next record buttons
    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
    		btn2.setEnabled(true);  
    		//	Set up the various pointers and cursor data needed to traverse the sequence
    		recNo    = 1;
    		cursor   = (Cursor) crsr;
    		nRecs    = cursor.getCount();
    		colNames = cursor.getColumnNames();
    		cursor.moveToFirst();
    	}
    	id = dbh.getInt( 0 ); // Get the primary key from the first column  
    	// 		Format and show the first record    
 //   	TV = (TextView) findViewById( R.id.eidText );
 //   	TV.setText( dbh.getStr(1) );
    	Log.i("Convert", "got fed ID " + "fed");
    	}
}
