package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class RemoveSheep extends ListActivity {

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
    	
//		ListView sheep_name_list = (ListView) findViewById(R.id.list);
//		sheep_name_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);   
//		sheep_name_list.setItemsCanFocus(false);		
//		sheep_name_list.setItemChecked(2, true);
//		sheep_name_list.getOnItemClickListener();
//		
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
//		final ListView sheep_name_list = (ListView) findViewById(R.id.list);
		if (nRecs > 0) {
	    	// format the sheep name records
	    	String[] fromColumns = new String[ ]{ "flock_name", "sheep_name"};
			Log.i("RemoveSheep", "after setting string array fromColumns for sheep names");
			//	Set the views for each column for each line. A sheep takes up 1 line on the screen
			int[] toViews = new int[] { R.id.flock_names, R.id.sheep_names};
	        Log.i("RemoveSheep", "after setting string array toViews for sheep names");
	        myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_names, cursor ,fromColumns, toViews, 0);
	        Log.i("RemoveSheep", "after setting myadapter to show names");
	        final ListView sheep_name_list=getListView();
	        setListAdapter(myadapter);
	        Log.i("RemoveSheep", "after setting list adapter to show names");
	        
	        sheep_name_list.setOnItemClickListener(new OnItemClickListener(){
	            public void onItemClick(AdapterView<?> parent, View view,int position,long id) {
	                View v = sheep_name_list.getChildAt(position);
	                CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.sheep_names);
	                Log.i("in click","I am inside onItemClick and position is:"+String.valueOf(position));
//	                CheckedTextView ctv = (CheckedTextView)view;
	            }
	        });
	        
//	        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//	        OnItemClickListener itemClickListener = new OnItemClickListener();
//	        {
//	        OnClickListener itemClickListener = new OnClickListener(){
	        
//	        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id){
//	        	@Override
//	        	public void onClick(AdapterView<?> arg0, View arg1, int position, long id){
//	        	ListView lv = (ListView) arg0;
//	        	if (lv.isItemChecked(position)){
//	        		Log.i("RemoveSheep", "Got a checked item");}
//	        	else{
//	        		Log.i("RemoveSheep", "removed a check");
//	        	}
//	        }
	     

	        
	        SparseBooleanArray sp=getListView().getCheckedItemPositions();
	        

		}  		
		else {
			// No sheep data - publish an empty list to clear sheep names
			Log.i("LookForSheep", "no current sheep");
			myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_names, null, null, null, 0);
			setListAdapter(myadapter);
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
		
//		//	Set the date picker stuff here
//		
//		Output = (TextView) findViewById(R.id.Output);
//        changeDate = (Button) findViewById(R.id.changeDate);
// 
//        // Get current date by calender
//         
//        final Calendar c = Calendar.getInstance();
//        year  = c.get(Calendar.YEAR);
//        month = c.get(Calendar.MONTH);
//        day   = c.get(Calendar.DAY_OF_MONTH);
// 
//        // Show current date
//         
//        Output.setText(new StringBuilder()
//                // Month is 0 based, just add 1
//                .append(month + 1).append("-").append(day).append("-")
//                .append(year).append(" "));
//  
        // Button listener to show date picker dialog
         
//        changeDate.setOnClickListener(new OnClickListener() {
// 
////            @Override
//            public void onClick(View v) {
//                 
//                // On button click show datepicker dialog 
//                showDialog(DATE_PICKER_ID);
// 
//            }
// 
//	    @Override
//	    protected Dialog onCreateDialog(int id) {
//	        switch (id) {
//	        case DATE_PICKER_ID:
//	             
//	            // open datepicker dialog. 
//	            // set date picker for current date 
//	            // add pickerListener listner to date picker
//	            return new DatePickerDialog(this, pickerListener, year, month,day);
//	        }
//	        return null;
//	    }
	 
//	    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
//	 
//	        // when dialog box is closed, below method will be called.
//	        @Override
//	        public void onDateSet(DatePicker view, int selectedYear,
//	                int selectedMonth, int selectedDay) {
//	             
//	            year  = selectedYear;
//	            month = selectedMonth;
//	            day   = selectedDay;
//	 
//	            // Show selected date 
//	            Output.setText(new StringBuilder().append(month + 1)
//	                    .append("-").append(day).append("-").append(year)
//	                    .append(" "));
//	     
//	           };
//	           

	    	}
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id){
           Log.d(getLocalClassName(), "onItemClick(" + arg1 + ","
                    + position + "," + id + ")");
            ListView lv = (ListView) arg0;
            if (lv.isItemChecked(position)){
            	Log.i("RemoveSheep", "Got a checked item");}
            else{
            	Log.i("RemoveSheep", "removed a check");
            }  
	}
}

