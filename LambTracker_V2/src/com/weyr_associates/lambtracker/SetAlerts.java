package com.weyr_associates.lambtracker;

import android.app.Activity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.weyr_associates.lambtracker.LambingSheep.IncomingHandler;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.app.ListActivity;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout.LayoutParams;
import com.google.zxing.client.android.Intents;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.ListView;

public class SetAlerts extends ListActivity {
	private DatabaseHandler dbh;
	String     	cmd;
	public Cursor 	cursor;
	public Object	crsr;
	public int 		nRecs;
	public String mytoday;
	public int 		thissheep_id;
	public SimpleCursorAdapter myadapter;
	ArrayAdapter<String> dataAdapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_alerts);
		String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("RemoveSheep", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
    	
//		Now go get all the current sheep names and format them
		cmd = String.format( "select sheep_table.sheep_id as _id, flock_prefix_table.flock_name, sheep_table.sheep_name " +
				", sheep_table.alert01 " +
				" from sheep_table inner join flock_prefix_table " +
				"on flock_prefix_table.flock_prefixid = sheep_table.flock_prefix" +
				" where (sheep_table.remove_date IS NULL or sheep_table.remove_date is '') "+
				"order by sheep_table.sheep_name asc ");  	        	
		Log.i("format record", " command is  " + cmd);
		crsr = dbh.exec( cmd );
		cursor   = ( Cursor ) crsr; 
		nRecs    = cursor.getCount();
		Log.i("setalerts", " nRecs is " + String.valueOf(nRecs));
		cursor.moveToFirst();	
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			Log.i("setalerts", (cursor.getString(1)+ " " + cursor.getString(2) ));
    	}
		cursor.moveToFirst();	
		if (nRecs > 0) {
	    	// format the sheep name records
	    	String[] fromColumns = new String[ ]{ "flock_name", "sheep_name", "alert01"};
			Log.i("setalerts", "after setting string array fromColumns for sheep names");
			//	Set the views for each column for each line. A sheep takes up 1 line on the screen
			int[] toViews = new int[] { R.id.flock_names, R.id.sheep_names, R.id.sheep_alerts};
	        Log.i("setalerts", "after setting string array toViews for sheep names");
	        myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_alerts, cursor ,fromColumns, toViews, 0);
	        Log.i("setalerts", "after setting myadapter to show names");
	        final ListView sheep_name_list=getListView();
	        setListAdapter(myadapter);
	        Log.i("setalerts", "after setting list adapter to show names");
	        
	        sheep_name_list.setOnItemClickListener(new OnItemClickListener(){
	            public void onItemClick(AdapterView<?> parent, View view,int position,long id) {
	                View v = sheep_name_list.getChildAt(position);
	                CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.sheep_names);
	                Log.i("in click","I am inside onItemClick and position is:"+String.valueOf(position));
//	                CheckedTextView ctv = (CheckedTextView)view;
	            }
	        });
	        SparseBooleanArray sp=getListView().getCheckedItemPositions();
		}  		
		else {
			// No sheep data - publish an empty list to clear sheep names
			Log.i("LookForSheep", "no current sheep");
			myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_alerts, null, null, null, 0);
			setListAdapter(myadapter);
		} 	
    	
	}
	
}
