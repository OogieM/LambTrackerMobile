package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class RemoveSheep extends Activity implements OnClickListener{
	private DatabaseHandler dbh;
	public Cursor 	cursor;
	public Object	crsr;
	public int 		nRecs;
	public Spinner remove_reason_spinner;
	public List<String> remove_reasons;
	String     	cmd;
	Button button;
	public SimpleCursorAdapter myadapter;
	ArrayAdapter<String> dataAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remove_sheep);
		String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("RemoveSheep", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
		final ListView sheep_name_list = (ListView) findViewById(R.id.sheepnamelist);
		sheep_name_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);   
		sheep_name_list.setItemsCanFocus(false);		
		sheep_name_list.setItemChecked(2, true);
		sheep_name_list.getOnItemClickListener();
		
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
//	        button.setOnClickListener(this)
		}  		
		else {
			// No sheep data - publish an empty list to clear sheep names
			Log.i("LookForSheep", "no current sheep");
			myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_names, null, null, null, 0);
			sheep_name_list.setAdapter(myadapter);
		} 	
		// Fill the Remove Reason Spinner
		remove_reason_spinner = (Spinner) findViewById(R.id.remove_reason_spinner);
    	remove_reasons = new ArrayList<String>();      	
    	
    	// Select All fields from remove reasons to build the spinner
        cmd = "select * from remove_reason_table";
        crsr = dbh.exec( cmd );  
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	remove_reasons.add("Remove Reason");
         // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		remove_reasons.add(cursor.getString(1));
    	}
    	
    	// Creating adapter for spinner
    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, remove_reasons);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		remove_reason_spinner.setAdapter (dataAdapter);
		remove_reason_spinner.setSelection(0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.remove_sheep, menu);
		return true;
	}
	public void onClick(View v) {
        AbsListView sheep_name_list = null;
		SparseBooleanArray checked = sheep_name_list.getCheckedItemPositions();
        ArrayList<String> selectedItems = new ArrayList<String>();
        for (int i = 0; i < checked.size(); i++) {
            // Item position in adapter
            int position = checked.keyAt(i);
            // Add sport if it is checked i.e.) == TRUE!
            if (checked.valueAt(i))
                selectedItems.add((String) myadapter.getItem(position));
        }
 
        String[] outputStrArr = new String[selectedItems.size()];
 
        for (int i = 0; i < selectedItems.size(); i++) {
            outputStrArr[i] = selectedItems.get(i);
        }
 
//        Intent intent = new Intent(getApplicationContext(),
//                ResultActivity.class);
 
        // Create a bundle object
        Bundle b = new Bundle();
        b.putStringArray("selectedItems", outputStrArr);
 
        // Add the bundle to the intent.
//        intent.putExtras(b);
 
        // start the ResultActivity
//        startActivity(intent);
    }

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		
	}

}
