package com.weyr_associates.lambtracker;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class RemoveSheep extends Activity {
	private DatabaseHandler dbh;
	public Cursor 	cursor;
	public Object	crsr;
	public int 		nRecs;
	String     	cmd;
	public SimpleCursorAdapter myadapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remove_sheep);
		String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("RemoveSheep", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
		ListView sheep_name_list = (ListView) findViewById(R.id.sheepnamelist);
		
//		Now go get all the current sheep names and format them
		cmd = String.format( "select sheep_table.sheep_id as _id, flock_prefix_table.flock_name, sheep_table.sheep_name " +
				" from sheep_table inner join flock_prefix_table " +
				"on flock_prefix_table.flock_prefixid = sheep_table.flock_prefix" +
				" where (sheep_table.remove_date IS NULL or sheep_table.remove_date is '') "+
				"order by sheep_table.sheep_name asc ");  	        	
		Log.i("format record", " command is  " + cmd);
		crsr = dbh.exec( cmd );
		cursor   = ( Cursor ) crsr; 
		nRecs    = cursor.getCount();
		Log.i("RemoveSheep", " nRecs is " + String.valueOf(nRecs));
		cursor.moveToFirst();	
//		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
//			Log.i("RemoveSheep", (cursor.getString(1)+ " " + cursor.getString(2) ));
//    	}
		cursor.moveToFirst();	
		if (nRecs > 0) {
	    	// format the sheep name records
	    	String[] fromColumns = new String[ ]{ "flock_name", "sheep_name"};
			Log.i("RemoveSheep", "after setting string array fromColumns for sheep names");
			//	Set the views for each column for each line. A sheep takes up 1 line on the screen
			int[] toViews = new int[] { R.id.flock_names, R.id.sheep_names};
	        Log.i("RemoveSheep", "after setting string array toViews for sheep names");
	        myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_names, cursor ,fromColumns, toViews, 0);
	        Log.i("RemoveSheep", "after setting myadapter to show names");
	        sheep_name_list.setAdapter(myadapter);
	        Log.i("RemoveSheep", "after setting list adapter to show names");			
		}  		
		else {
			// No sheep data - publish an empty list to clear sheep names
			Log.i("LookForSheep", "no current sheep");
			myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_names, null, null, null, 0);
			sheep_name_list.setAdapter(myadapter);
		} 		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.remove_sheep, menu);
		return true;
	}

}
