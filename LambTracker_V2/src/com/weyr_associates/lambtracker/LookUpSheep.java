package com.weyr_associates.lambtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteException;

public class LookUpSheep extends Activity
	{
	DatabaseHandler dbh;
	int             id;
	String 			logmessages;
	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup_sheep);
        String 	dbfile = getString(R.string.real_database_file) ;
    	dbh = new DatabaseHandler( this, dbfile );
    	
//		Added the variable definitions here    	
      	String          cmd;
    	String          sheepName;
    	Boolean			exists;
//    	String 			mymsg;
//		make the delete button red
    	Button btn = (Button) findViewById( R.id.delete_task_btn );
    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
 // make update and enter buttons active
		Button btn2 = (Button) findViewById( R.id.update_task_btn );
    	btn2.setEnabled(true); 
		Button btn4 = (Button) findViewById( R.id.task_enter_btn );
    	btn4.setEnabled(true);     	
// 		here is where I put the actual rcvd eid into the eid variable
        TextView        TV  = (TextView) findViewById( R.id.eidText );
        String eid = this.getIntent().getExtras().getString("com.weyr_associates.lambtracker.LASTEID");
//      Log.i("LookUpSheep", eid);         
        exists = true;
//        mymsg="exists is true";
//        Log.i ("LookUpSheep2", mymsg);
//		added a way to verify the sheep_table exists here
        exists = tableExists("demo_sheep_table");
        if (exists)
        	{
 //       	mymsg = "in if loop w/sheep table present";
 //       	Log.i ("LookUpSheep", mymsg);
        	if( eid != null && eid.length() > 0 ){
        	cmd = String.format( "select *  from demo_sheep_table where eid_tag='%s'", eid );      	
  //      	Log.i("LookUpSheep2", cmd);
        	}	
        	else
        	{
        	return;
        	}
        	dbh.exec( cmd );
        	dbh.moveToFirstRecord();
        	if( dbh.getSize() == 0 )
        	{
        		// disable update button     		
        		Button btn3 = (Button) findViewById( R.id.update_task_btn );
            	btn3.setEnabled(false); 
        		TV = (TextView) findViewById( R.id.eidText );
            	TV.setText( eid );
            	TV = (TextView) findViewById( R.id.sheeptaskText );
            	TV.setText( "Cannot find requested sheep. Use Enter to add this sheep." );
            return;
        	} 
    		Button btn5 = (Button) findViewById( R.id.task_enter_btn );
        	btn5.setEnabled(false); 
        	id = dbh.getInt( 0 );
        	TV = (TextView) findViewById( R.id.eidText );
        	TV.setText( dbh.getStr(1) );
        	TV = (TextView) findViewById( R.id.fedText );
        	TV.setText( dbh.getStr(2) );
        	TV = (TextView) findViewById( R.id.farmText );
        	TV.setText( dbh.getStr(3) );
        	TV = (TextView) findViewById( R.id.sheepnameText );
        	TV.setText( dbh.getStr(4));        
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
    			clearBtn( null );
            	TV = (TextView) findViewById( R.id.sheeptaskText );
                TV.setText( "Sheep Database does not exist." );    			
        	}
        }
    
	public boolean tableExists (String table){
//		String 	mymsg;
//		mymsg = "nothing yet";
		try {
	        dbh.exec("select * from "+ table);   
//	        mymsg = "table does exist";
//	        Log.i ("tableExists", mymsg);
	        return true;
		} catch (SQLiteException e) {
//			mymsg = "table not here";
//			Log.i ("tableExists", mymsg);
			return false;
	        		}
	        	}
	        
    // user clicked the 'back' button
    public void backBtn( View v )
	    {
    	// Added this to close the database if we go back to the main activity  	
    	dbh.closeDB();
    	//Go back to main
      	finish();
//    	Intent toMain = new Intent( this, MainActivity.class );
//		startActivity( toMain );
	    }
    
	// user clicked 'enter' button
    public void enterBtn( View v )
    	{
    	TextView TV  = (TextView) findViewById( R.id.eidText );
    	String   eid = TV.getText().toString();
//    	Log.i("DoSheepTaskEnter", eid);
    	TextView TV2  = (TextView) findViewById( R.id.fedText );
    	String   fed = TV2.getText().toString();
//    	Log.i("DoSheepTaskEnter", fed);
    	// added section here to include a farm ID tag	
    	TextView TV3 = (TextView) findViewById( R.id.farmText );
    	String	 farm = TV3.getText().toString();
//    	Log.i("DoSheepTaskEnter", farm);
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
        	
    	String cmd = String.format( "insert into demo_sheep_table(eid_tag,fed_tag,farm_tag,sheep_name,sheep_task,birth_type,birth_weight,lambing_2012,lambing_2013) values('%s','%s','%s','%s','%s','%s','%s','%s','%s')",
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
    	dbh.exec( "select max(id) from demo_sheep_table" );
    	dbh.moveToFirstRecord();
    	id = dbh.getInt( 0 ); // Get the primary key from the first record
    	clearBtn( v );
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
		
		String cmd  = String.format( "update demo_sheep_table set %s where id=%d", sets, id );
		dbh.exec( cmd );
		clearBtn( v );
		}
	}
    
    // user clicked 'delete' button
    // need to add how to handle if no EID here
    
    public void deleteBtn( View v )
    	{
    	if( id != 0 )
    		{
    		AlertDialog.Builder builder = new AlertDialog.Builder( this );
    		builder.setMessage( R.string.delete_sheep )
    	           .setTitle( R.string.delete_warning );
    		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int idx) {
    	               // User clicked OK button -- delete the sheep
    	       		   String cmd = String.format( "delete from demo_sheep_table where id=%d", id );
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
    
    // user clicked 'close' button
    public void closeTaskBtn( View v )
    	{   	
    	// Added this to close the database if we go back to the main activity  	
    	dbh.closeDB();
    	// clear the form
    	clearBtn( null );
    	}
    
    }

