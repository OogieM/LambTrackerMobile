package com.weyr_associates.lambtracker;


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

public class ConvertToEID extends Activity {
	private DatabaseHandler dbh;
	int             fedtagid, farmtagid;
	private Cursor 	cursor;
	private int			    recNo;
	private int             nRecs;
//	private String[]        colNames; 
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert_to_eid);
        String dbname = getString(R.string.real_database_file); 
    	dbh = new DatabaseHandler( this, dbname );
    	
    	//	make the remove tag buttons red
    	Button btn = (Button) findViewById( R.id.remove_tag_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	btn = (Button) findViewById( R.id.remove_fedtag_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	btn = (Button) findViewById( R.id.remove_farmtag_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
    	
    	//	Disable the Next Record and Prev. Record button until we have multiple records
    	Button btn2 = (Button) findViewById( R.id.next_rec_btn );
    	btn2.setEnabled(false); 
    	Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
    	btn3.setEnabled(false);
    	fedtagid = 0;
    	farmtagid = 0;
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
    	TextView TV10 = (TextView) findViewById( R.id.inputText );
    	TV10.setText( "" );		
    	TextView TV = (TextView) findViewById( R.id.sheepnameText );
	    TV.setText( "" );
	    TextView TV1  = (TextView) findViewById( R.id.eidText );
	    TV1.setText( "" );
	    TextView TV2  = (TextView) findViewById( R.id.fedText );
	    TV2.setText( "" );
	    TextView TV3  = (TextView) findViewById( R.id.farmText );
	    TV3.setText( "" );
	    TextView TV4 = (TextView) findViewById( R.id.fed_colorText );
	    TV4.setText( "" );
	    TextView TV5 = (TextView) findViewById( R.id.fed_locationText );
	    TV5.setText( "" );
	    TextView TV6 = (TextView) findViewById( R.id.farm_colorText );
	    TV6.setText( "" );
	    TextView TV7 = (TextView) findViewById( R.id.farm_locationText);
	    TV7.setText( "" );
	    TextView TV8 = (TextView) findViewById( R.id.eid_colorText );
	    TV8.setText( "" );
	    TextView TV9 = (TextView) findViewById( R.id.eid_locationText );
	    TV9.setText( "" );
	  	fedtagid = 0;
    	farmtagid = 0;
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
    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
    				"id_info_table.id_infoid " +
    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
    				"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off = NULL and id_info_table.tag_number='%s'", fed);
    		
//    		Log.i("Convert", "building command ");
    		}	
    	else
    	{
    		return;
     	}
    	Object crsr = dbh.exec( cmd );   	
    	dbh.moveToFirstRecord();
    	fedtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database
    	if( dbh.getSize() == 0 )
    		{ // no sheep with that federal tag in the database so clear out and return
    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheepnameText );
        	TV.setText( "Cannot find requested sheep." );
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

    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	TextView TV2 = (TextView) findViewById(R.id.fedText)	;
    	TV2.setText(dbh.getStr(4));
    	TextView TV3 = (TextView) findViewById(R.id.fed_colorText);
    	TV3.setText(dbh.getStr(3));
    	TextView TV4 = (TextView) findViewById(R.id.fed_locationText);
    	TV4.setText(dbh.getStr(5));
    	ii = dbh.getInt(1);
    	
//		Now we need to get the farm tag for that sheep and fill the display with data
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
		"id_info_table.id_infoid " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
		"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off = NULL and id_info_table.sheep_id='%s'", ii);
    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
    	
    	TextView TV5 = (TextView) findViewById(R.id.farmText)	;
    	TV5.setText(dbh.getStr(4));
    	TextView TV6 = (TextView) findViewById(R.id.farm_colorText);
    	TV6.setText(dbh.getStr(3));
    	TextView TV7 = (TextView) findViewById(R.id.farm_locationText);
    	TV7.setText(dbh.getStr(5));
    	ii = dbh.getInt(1);
    	farmtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database
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
    				"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
    				"id_info_table.id_infoid " +
    				"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
    				"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
    				"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
    				"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
    				"where id_type_table.id_typeid = 4 and id_info_table.tag_date_off = NULL and id_info_table.tag_number='%s'", farm);
    		
//    		Log.i("Convert", "building command ");
    		}	
    	else
    	{
    		return;
     	}
    	Object crsr = dbh.exec( cmd );   	
    	dbh.moveToFirstRecord();
    	farmtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database
    	if( dbh.getSize() == 0 )
    		{ // no sheep with that farm tag in the database so clear out and return
    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheepnameText );
        	TV.setText( "Cannot find requested sheep." );
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

    	TV = (TextView) findViewById(R.id.sheepnameText);
    	TV.setText(dbh.getStr(0));
    	TextView TV2 = (TextView) findViewById(R.id.farmText)	;
    	TV2.setText(dbh.getStr(4));
    	TextView TV3 = (TextView) findViewById(R.id.farm_colorText);
    	TV3.setText(dbh.getStr(3));
    	TextView TV4 = (TextView) findViewById(R.id.farm_locationText);
    	TV4.setText(dbh.getStr(5));
    	ii = dbh.getInt(1);
    	
//		Now we need to get the rest of the tags and fill the display with data
    	cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
		"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
		"id_info_table.id_infoid " +
		"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
		"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
		"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
		"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
		"where id_type_table.id_typeid = 1 and id_info_table.tag_date_off = NULL and id_info_table.sheep_id='%s'", ii);
    	
    	crsr = dbh.exec( cmd );
    	dbh.moveToFirstRecord();
    	fedtagid = dbh.getInt( 6 ); // Get the id_info_table.id_infoid from the database   	
    	TextView TV5 = (TextView) findViewById(R.id.fedText)	;
    	TV5.setText(dbh.getStr(4));
    	TextView TV6 = (TextView) findViewById(R.id.fed_colorText);
    	TV6.setText(dbh.getStr(3));
    	TextView TV7 = (TextView) findViewById(R.id.fed_locationText);
    	TV7.setText(dbh.getStr(5));
    	ii = dbh.getInt(1);
    	
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

    // user clicked 'remove fed tag' button   
    public void removeFedTag( View v )
    	{
    	if( fedtagid != 0 )
    		{
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.delete_tag )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- delete the federal tag
    	        	   //add a tag_date_off of today to the tag   	       		
    	        	   String today = TodayIs();
//    	        	   Log.i("removefedtag", today);
    	        	   Log.i("removefedtag", String.valueOf(fedtagid));
    	       		   String cmd = String.format( "update id_info_table SET tag_date_off = '" + today + "' where id_infoid=%d", fedtagid );
    	    		   dbh.exec( cmd );
    	    		   clearBtn( null );
    	               }
    	       });
    		builder.setNegativeButton( R.string.cancel_btn, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User cancelled the dialog
    	           }
    	       });
    		
    		AlertDialog dialog = builder.create();
    		dialog.show();
    		}
    	}    
    
    // user clicked 'remove farm tag' button   
    public void removeFarmTag( View v )
    	{
    	if( farmtagid != 0 )
    		{
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.delete_tag )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- delete the farm tag
    	        	   //add a tag_date_off of today to the tag
    	        	   String today = TodayIs();
 //   	        	   Log.i("removefarmtag", today);
    	        	   Log.i("removefarmtag", String.valueOf(farmtagid));
 //   	       		   String cmd = String.format( "update id_info_table SET tag_date_off = today where id_infoid=%d", farmtagid );
 //   	    		   dbh.exec( cmd );
    	    		   clearBtn( null );
    	               }
    	       });
    		builder.setNegativeButton( R.string.cancel_btn, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User cancelled the dialog
    	           }
    	       });
    		
    		AlertDialog dialog = builder.create();
    		dialog.show();
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
