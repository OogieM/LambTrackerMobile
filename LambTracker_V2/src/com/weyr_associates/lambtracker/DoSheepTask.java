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

public class DoSheepTask extends Activity
	{
	private DatabaseHandler dbh;
	int             id;
	String 			logmessages;
	//	added from edit DB to try to get it working for multiple records
	private Cursor 	cursor;
	private int			    recNo;
	private int             nRecs;
	private String[]        colNames; // Left in because I will need it for Convert to EID task
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.do_sheeptask);
    	dbh = new DatabaseHandler( this, "weyr_associates" );
    	
    	//	make the delete button red
    	Button btn = (Button) findViewById( R.id.delete_task_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
 
    	//	Disable the Next Record and Prev. Record button until we have multiple records
       	Button btn2 = (Button) findViewById( R.id.next_rec_btn );
    	btn2.setEnabled(false); 
    	Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
    	btn3.setEnabled(false);
    	id = 0;
       	}
    
    // user clicked the 'back' button
    public void backBtn( View v )
	    {
       	dbh.closeDB();
    	clearBtn( null );
    	finish();
	    }
    
	// user clicked 'enter' button
    public void enterBtn( View v )
    	{
    	TextView TV  = (TextView) findViewById( R.id.eidText );
    	String   eid = TV.getText().toString();
    	TextView TV2  = (TextView) findViewById( R.id.fedText );
    	String   fed = TV2.getText().toString();
    	// added section here to include a farm ID tag	
    	TextView TV3 = (TextView) findViewById( R.id.farmText );
    	String	 farm = TV3.getText().toString();
    	TV = (TextView) findViewById( R.id.sheepnameText );
    	String sheepName = dbh.fixApostrophes( TV.getText().toString() );
    	
    	TV = (TextView) findViewById( R.id.sheeptaskText );
    	String sheepTask = dbh.fixApostrophes( TV.getText().toString() );

        TV = (TextView) findViewById( R.id.sheepbirthtypeText );
        String sheepbirthtype = dbh.fixApostrophes( TV.getText().toString() );
      
        TV = (TextView) findViewById( R.id.sheepbirthweightText );
        String sheepbirthweight = dbh.fixApostrophes( TV.getText().toString() ); 	
        
    	TV = (TextView) findViewById( R.id.lambing2012Text );
    	String lambing2012 = dbh.fixApostrophes( TV.getText().toString() );
    	
    	TV = (TextView) findViewById( R.id.lambing2013Text );
    	String lambing2013 = dbh.fixApostrophes( TV.getText().toString() );
        	
    	String cmd = String.format( "insert into sheep_table(eid_tag,fed_tag,farm_tag,sheep_name,sheep_task,birth_type,birth_weight,lambing_2012,lambing_2013) values('%s','%s','%s','%s','%s','%s','%s','%s','%s')",
    								eid,
    								fed,
    								farm,
    								dbh.fixApostrophes(sheepName), 	
    								dbh.fixApostrophes(sheepTask),
    								dbh.fixApostrophes(sheepbirthtype),
    								dbh.fixApostrophes(sheepbirthweight),
    								dbh.fixApostrophes(lambing2012),
    								dbh.fixApostrophes(lambing2013)
    								);
    	dbh.exec( cmd );
    	
    	// get the id
    	dbh.exec( "select max(id) from sheep_table" );
    	dbh.moveToFirstRecord();
    	id = dbh.getInt( 0 ); // Get the primary key from the first record
    	clearBtn( v );
    	}
    
    // user clicked 'view' button
    public void viewBtn( View v )
    	{
    	String          cmd;
    	TextView        TV  = (TextView) findViewById( R.id.eidText );
    	String 			eid = TV.getText().toString();  	
    	// added section here to include a federal ID tag
    	TextView		TV2 = (TextView) findViewById( R.id.fedText );
    	String			fed = TV2.getText().toString();
    	// added section here to include a farm ID tag	
    	TextView		TV3 = (TextView) findViewById( R.id.farmText );
    	String			farm = TV3.getText().toString();
    	// Moved the sheep name stuff to here for readability
		TextView 		TV4 = (TextView) findViewById( R.id.sheepnameText );
		String 			sheepName = dbh.fixApostrophes( TV4.getText().toString() );
		// Start of the actual code to process the buttons
    	if( eid != null && eid.length() > 0 )
    		{
    		cmd = String.format( "select * from sheep_table where eid_tag='%s'", eid);
    		}	
    	else
    	{
    		//added this if statement to also check using federal id tag
        	if( fed != null && fed.length() > 0 )
    		{
    		cmd = String.format( "select * from sheep_table where fed_tag='%s'", fed );
    		}
    		//added this if statement to also check using farm id tag
        	else
        	{
        		if( farm != null && farm.length() > 0 )
        		{
        			cmd = String.format( "select * from sheep_table where farm_tag='%s'", farm );
        		}
        		else
        		{   	
        			if( sheepName != null && sheepName.length() > 0 )
        			{
        				cmd = String.format("select * from sheep_table where sheep_name='%s'",
    								sheepName );
        			}
        			else
        				return;
        		}
        		}
    	}
    	Object crsr = dbh.exec( cmd );   	
    	dbh.moveToFirstRecord();
    	if( dbh.getSize() == 0 )
    		{
//    		clearBtn( v );
    		TV = (TextView) findViewById( R.id.sheeptaskText );
        	TV.setText( "Cannot find requested sheep. Use Enter to add this sheep." );
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
    	TV = (TextView) findViewById( R.id.eidText );
    	TV.setText( dbh.getStr(1) );
       	TV = (TextView) findViewById( R.id.fedText );
    	TV.setText( dbh.getStr(2) );
    	TV = (TextView) findViewById( R.id.farmText );
    	TV.setText( dbh.getStr(3) );
    	TV = (TextView) findViewById( R.id.sheepnameText );
    	TV.setText( dbh.getStr(4) );       
        TV = (TextView) findViewById( R.id.sheepbirthtypeText );
        TV.setText( dbh.getStr(6));
        TV = (TextView) findViewById( R.id.sheepbirthweightText );
        TV.setText( dbh.getStr(8));       
    	TV = (TextView) findViewById( R.id.sheeptaskText );
    	TV.setText( dbh.getStr(7) );  	
    	TV = (TextView) findViewById( R.id.lambing2012Text );
    	TV.setText( dbh.getStr(9) );
    	TV = (TextView) findViewById( R.id.lambing2013Text );
    	TV.setText( dbh.getStr(10) ); 
    	}
    
    // user clicked the "next record" button
    public void nextBtn( View v)
    {
    	TextView TV;
    	if (recNo == (nRecs-1)) {
    		// at end so disable next record button
    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
        	btn2.setEnabled(false);   		
    	}
    	if ( cursor.moveToNext() ){
    		// I've moved forward so I need to enable the previous record button
    		Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
    		btn3.setEnabled(true);
    		id = dbh.getInt( 0 );
        	Log.i ("DoSheepTask", "In if statement next button and the record id is " + String.valueOf(id) );
    		recNo         += 1;
    		TV = (TextView) findViewById( R.id.eidText );
    		TV.setText( dbh.getStr(1) );
    		TV = (TextView) findViewById( R.id.fedText );
    		TV.setText( dbh.getStr(2) );
    		TV = (TextView) findViewById( R.id.farmText );
    		TV.setText( dbh.getStr(3) );
    		TV = (TextView) findViewById( R.id.sheepnameText );
    		TV.setText( dbh.getStr(4) );       
    		TV = (TextView) findViewById( R.id.sheepbirthtypeText );
    		TV.setText( dbh.getStr(6));
    		TV = (TextView) findViewById( R.id.sheepbirthweightText );
    		TV.setText( dbh.getStr(8));       
    		TV = (TextView) findViewById( R.id.sheeptaskText );
    		TV.setText( dbh.getStr(7) );  	
    		TV = (TextView) findViewById( R.id.lambing2012Text );
    		TV.setText( dbh.getStr(9) );
    		TV = (TextView) findViewById( R.id.lambing2013Text );
    		TV.setText( dbh.getStr(10) ); 
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
    	if ( cursor.moveToPrevious() ){
    		// I've moved back so enable the next record button
    		Button btn2 = (Button) findViewById( R.id.next_rec_btn );
    		btn2.setEnabled(true);  
        	id = dbh.getInt( 0 ); // Get the primary key from the current record
        	Log.i ("DoSheepTask", "In if statement prev button and the record id is " + String.valueOf(id) );
    		recNo  -= 1;
      		TV = (TextView) findViewById( R.id.eidText );
    		TV.setText( dbh.getStr(1) );
    		TV = (TextView) findViewById( R.id.fedText );
    		TV.setText( dbh.getStr(2) );
    		TV = (TextView) findViewById( R.id.farmText );
    		TV.setText( dbh.getStr(3) );
    		TV = (TextView) findViewById( R.id.sheepnameText );
    		TV.setText( dbh.getStr(4) );       
    		TV = (TextView) findViewById( R.id.sheepbirthtypeText );
    		TV.setText( dbh.getStr(6));
    		TV = (TextView) findViewById( R.id.sheepbirthweightText );
    		TV.setText( dbh.getStr(8));       
    		TV = (TextView) findViewById( R.id.sheeptaskText );
    		TV.setText( dbh.getStr(7) );  	
    		TV = (TextView) findViewById( R.id.lambing2012Text );
    		TV.setText( dbh.getStr(9) );
    		TV = (TextView) findViewById( R.id.lambing2013Text );
    		TV.setText( dbh.getStr(10) ); 
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

    // user clicked 'clear' button
    public void clearBtn( View v )
	    {
	    TextView TVeid  = (TextView) findViewById( R.id.eidText );
	    TVeid.setText( "" );
	    TextView TVfed  = (TextView) findViewById( R.id.fedText );
	    TVfed.setText( "" );
	    TextView TVfarm  = (TextView) findViewById( R.id.farmText );
	    TVfarm.setText( "" );
	    TextView TV = (TextView) findViewById( R.id.sheepnameText );
	    TV.setText( "" );
	    TextView TVbirthtype = (TextView) findViewById( R.id.sheepbirthtypeText );
	    TVbirthtype.setText( "" );
	    TextView TVbirthweight = (TextView) findViewById( R.id.sheepbirthweightText );
	    TVbirthweight.setText( "" );
	    TV = (TextView) findViewById( R.id.sheeptaskText );
	    TV.setText( "" );
	    TextView TVlambing2012 = (TextView) findViewById( R.id.lambing2012Text );
	    TVlambing2012.setText( "" );
	    TextView TVlambing2013 = (TextView) findViewById( R.id.lambing2013Text );
	    TVlambing2013.setText( "" );
	    id = 0;
      	Button btn2 = (Button) findViewById( R.id.next_rec_btn );
    	btn2.setEnabled(false); 
    	Button btn3 = (Button) findViewById( R.id.prev_rec_btn );
    	btn3.setEnabled(false);
    }
    
    // user clicked 'update' button
    public void updateBtn( View v )
    	{
    	if( id != 0 )
    		{
    		String	eid  = ((TextView) findViewById(R.id.eidText) ).getText().toString();
    		String	fed  = ((TextView) findViewById(R.id.fedText) ).getText().toString();
    		String  farm = ((TextView) findViewById(R.id.farmText) ).getText().toString();
    		String	name = ((TextView) findViewById(R.id.sheepnameText) ).getText().toString();
    		String	birthtype = ((TextView) findViewById(R.id.sheepbirthtypeText) ).getText().toString();
    		String	task = ((TextView) findViewById(R.id.sheeptaskText) ).getText().toString();
    		String	birthweight = ((TextView) findViewById(R.id.sheepbirthweightText) ).getText().toString();
    		String	lambing2012 = ((TextView) findViewById(R.id.lambing2012Text) ).getText().toString();
    		String	lambing2013 = ((TextView) findViewById(R.id.lambing2013Text) ).getText().toString();
    		StringBuilder sb   = new StringBuilder();
    		
    		if( eid.length() > 0 )
    			{
    			sb.append( "eid_tag='" );
    			sb.append( eid );
    			sb.append( "'," );
    			}
    		
    		if( fed.length() > 0 )
			{
			sb.append( "fed_tag='" );
			sb.append( fed );
			sb.append( "'," );
			}
		
    		if( farm.length() > 0 )
			{
			sb.append( "farm_tag='" );
			sb.append( farm );
			sb.append( "'," );
			}
    		
    		if( name.length() > 0 )
    			{
    			sb.append( "sheep_name='" );
    			sb.append( dbh.fixApostrophes(name) );
    			sb.append( "'," );
    			}
    		
    		if( birthtype.length() > 0 )
			{
			sb.append( "birth_type='" );
			sb.append( birthtype );
			sb.append( "'," );
			}
    		
    		if( task.length() > 0 )
    			{
    			sb.append( "sheep_task='");
    			sb.append( dbh.fixApostrophes(task) );
    			sb.append( "'," );
    			}
    		if( birthweight.length() > 0 )
			{
			sb.append( "birth_weight='" );
			sb.append( birthweight );
			sb.append( "'," );
			}
    		
    		if( lambing2012.length() > 0 )
			{
			sb.append( "lambing_2012='" );
			sb.append( dbh.fixApostrophes(lambing2012) );
			sb.append( "'," );
			}
    		if( lambing2013.length() > 0 )
			{
			sb.append( "lambing_2013='" );
			sb.append( dbh.fixApostrophes(lambing2013) );
			sb.append( "'" );
			}
    		String sets = sb.toString();
    		
    		if( sets.endsWith(",") )
    			sets = sets.replaceFirst( ",$", "" );
    		
    		String cmd  = String.format( "update sheep_table set %s where id=%d", sets, id );
    		dbh.exec( cmd );
    		clearBtn( v );
    		}
    	}
    
    // user clicked 'delete' button   
    public void deleteBtn( View v )
    	{
    	Log.i ("DoSheep", "In delete button with id " + String.valueOf(id));
    	if( id != 0 )
    		{
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.delete_sheep )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- delete the sheep
    	       		   String cmd = String.format( "delete from sheep_table where id=%d", id );
    	    		   dbh.exec( cmd );
    	    		   clearBtn( null );
    	               }
    	       });
    		builder.setNegativeButton( R.string.cancel, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User cancelled the dialog
    	           }
    	       });
    		
    		AlertDialog dialog = builder.create();
    		dialog.show();
    		}
    	}
    }

