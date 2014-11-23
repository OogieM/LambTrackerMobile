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
	public String mytoday, alert_text ;
	public int 		thissheep_id;
	public SimpleCursorAdapter myadapter;
	ArrayAdapter<String> dataAdapter;
	public List<String> test_names;
    public List<Integer> test_sheep_id;
    public SparseBooleanArray sp;
    ListView sheep_name_list;
    ListView test_name_list;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_alerts);
		String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("setAlerts", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
    	test_name_list = (ListView) findViewById(android.R.id.list);
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
		test_names = new ArrayList<String>(); 
       	test_sheep_id = new ArrayList<Integer>();
		cursor.moveToFirst();	
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			Log.i("setalerts", (cursor.getString(1)+ " " + cursor.getString(2) + " " + cursor.getString(3)));
			test_names.add (cursor.getString(1) + " " + cursor.getString(2)+ " " + cursor.getString(3));
			test_sheep_id.add(cursor.getInt(0));
    	}
		cursor.moveToFirst();	
		final ListView sheep_name_list = (ListView) findViewById(android.R.id.list);
		
		if (nRecs > 0) {
	    	// format the sheep name records
//	    	String[] fromColumns = new String[ ]{ "flock_name", "sheep_name", "alert01"};
//			Log.i("setalerts", "after setting string array fromColumns for sheep names");
//			//	Set the views for each column for each line. A sheep takes up 1 line on the screen
//			int[] toViews = new int[] { R.id.flock_names, R.id.sheep_names, R.id.sheep_alerts};
//	        Log.i("setalerts", "after setting string array toViews for sheep names");
//	        myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_alerts, cursor ,fromColumns, toViews, 0);
//	        Log.i("setalerts", "after setting myadapter to show names");
//	        final ListView sheep_name_list=getListView();
//	        setListAdapter(myadapter);
//	        Log.i("setalerts", "after setting list adapter to show names");
	        
	        ArrayAdapter<String> adapter = (new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice,test_names));
	        test_name_list.setAdapter(adapter);
	        test_name_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        
	        sheep_name_list.setOnItemClickListener(new OnItemClickListener(){
	            public void onItemClick(AdapterView<?> parent, View view,int position,long id) {
	                View v = sheep_name_list.getChildAt(position);
	                Log.i("in click","I am inside onItemClick and position is:"+String.valueOf(position));
	            }
	        });
	        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		     
	       sp=getListView().getCheckedItemPositions();
		}  		
		else {
			// No sheep data - publish an empty list to clear sheep names
			Log.i("LookForSheep", "no current sheep");
//			myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_alerts, null, null, null, 0);
//			setListAdapter(myadapter);
		} 	
	}
	public void addAlert( View v ){

		boolean temp_value;
		TextView TV;
        int temp_location, temp_size;
        String temp_text;
        temp_size = sp.size();
        Log.i ("before loop", "the sp size is " + String.valueOf(temp_size));
        TV = (TextView) findViewById( R.id.inputText );
        temp_text = TV.getText().toString();
    	for (int i=0; i<temp_size; i++){
    		temp_value = sp.valueAt(i);
    		temp_location = sp.keyAt(i);
    		if (temp_value){
    			Log.i ("for loop", "the sheep " + " " + test_names.get(temp_location)+ " is checked");
    			Log.i ("for loop", "the sheep id is " + String.valueOf(test_sheep_id.get(temp_location)));
    	    	// 	This needs to be in a loop for all sheep_id s that we found. 
    			//	Setting each one to be thissheep_id
    			cmd = String.format("select alert01 from sheep_table where sheep_id = %s", test_sheep_id.get(temp_location));
    			Log.i("get alert ", "before cmd " + cmd);
    			crsr = dbh.exec( cmd);
    			cursor   = ( Cursor ) crsr; 
    			cursor.moveToFirst();
    			alert_text = dbh.getStr(0);
    			Log.i("add alert ", alert_text);
    			alert_text = temp_text + "\n" + alert_text;
    			Log.i("add alert ", alert_text);
    			//	default to adding the alert to the beginning of the current one
    	    	cmd = String.format("update sheep_table set alert01 = '%s' where sheep_id =%d ",
    	    			alert_text, test_sheep_id.get(temp_location) ) ;
    			Log.i("add alert ", "before cmd " + cmd);
    			dbh.exec( cmd );
    			Log.i("add alert ", "after cmd " + cmd);	    			
    		}   		
    	}// for loop
    	// Now need to go back 
		try { 
			cursor.close();
		}
		catch (Exception e) {
//			Log.i("Back Button", " In catch stmt cursor");  
			// In this case there is no adapter so do nothing
		}
       	dbh.closeDB();  	
    	finish();		
    	
}
	public void removeAlert( View v ){

		boolean temp_value;
		TextView TV;
        int temp_location, temp_size;
        String temp_text;
        temp_size = sp.size();
        Log.i ("before loop", "the sp size is " + String.valueOf(temp_size));
        TV = (TextView) findViewById( R.id.inputText );
        temp_text = TV.getText().toString();
    	for (int i=0; i<temp_size; i++){
    		temp_value = sp.valueAt(i);
    		temp_location = sp.keyAt(i);
    		if (temp_value){
    			Log.i ("for loop", "the sheep " + " " + test_names.get(temp_location)+ " is checked");
    			Log.i ("for loop", "the sheep id is " + String.valueOf(test_sheep_id.get(temp_location)));
    	    	// 	This needs to be in a loop for all sheep_id s that we found. 
    			//	Setting each one to be thissheep_id
    			cmd = String.format("select alert01 from sheep_table where sheep_id = %s", test_sheep_id.get(temp_location));
    			Log.i("get alert ", "before cmd " + cmd);
    			crsr = dbh.exec( cmd);
    			cursor   = ( Cursor ) crsr; 
    			cursor.moveToFirst();
    			alert_text = dbh.getStr(0);
    			Log.i("remove alert ", alert_text);
//    			temp_text = "%"+ temp_text + "%";
    			Log.i("remove alert ", temp_text);
    			//	TODO
    			//	Figure out how to remove an alert
    	    	cmd = String.format("update sheep_table set alert01 = replace(alert01, '%s','') where alert01 like ('%'+'%s'+'%') and sheep_id =%d ",
    	    			temp_text, temp_text,test_sheep_id.get(temp_location) ) ;
    			Log.i("remove alert ", "before cmd " + cmd);
//    			dbh.exec( cmd );
    			Log.i("remove alert ", "after cmd " + cmd);	    			
    		}   		
    	}// for loop
    	// Now need to go back 
		try { 
			cursor.close();
		}
		catch (Exception e) {
//			Log.i("Back Button", " In catch stmt cursor");  
			// In this case there is no adapter so do nothing
		}
       	dbh.closeDB();  	
    	finish();		
    	
}
}
