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

public class RemoveSheep extends ListActivity  {

	private DatabaseHandler dbh;
	public Cursor 	cursor;
	public Object	crsr;
	public int 		nRecs;
	public String mytoday;
	public Spinner remove_reason_spinner;
	public int which_remove_reason;
	public List<String> remove_reasons;
	public int 		thissheep_id;
	String     	cmd;
	Button button;
	public SimpleCursorAdapter myadapter;
	ArrayAdapter<String> dataAdapter;
	private TextView Output;
    private Button changeDate;
    static final int DATE_PICKER_ID = 1111;
    private int year;
    private int month;
    private int day;
    public String removedate, deathdate;
    public List<String> test_names;
    public List<Integer> test_sheep_id;
    public SparseBooleanArray sp;
    ListView sheep_name_list;
    ListView test_name_list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remove_sheep);
		String 	dbfile = getString(R.string.real_database_file) ;
        Log.i("RemoveSheep", " after get database file");
    	dbh = new DatabaseHandler( this, dbfile );
//    	Get the date and time to add to the sheep record these are strings not numbers
    	mytoday = Utilities.TodayIs(); 
    	
//		sheep_name_list = (ListView) findViewById(R.id.sheep_names);
		test_name_list = (ListView) findViewById(android.R.id.list);
		
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
		test_names = new ArrayList<String>(); 
       	test_sheep_id = new ArrayList<Integer>();
       	
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			Log.i("RemoveSheep",cursor.getString(2) );
			// This was in for debugging to see the sheep_id's to verify I had them correct
//			test_names.add (String.valueOf(cursor.getInt(0)) + " "+ cursor.getString(1) + " " + cursor.getString(2));
			test_names.add (cursor.getString(1) + " " + cursor.getString(2));
			test_sheep_id.add(cursor.getInt(0));
			}
		cursor.moveToFirst();	
		final ListView sheep_name_list = (ListView) findViewById(android.R.id.list);
		if (nRecs > 0) {
	    	// format the sheep name records
//	    	String[] fromColumns = new String[ ]{ "flock_name", "sheep_name"};
//			Log.i("RemoveSheep", "after setting string array fromColumns for sheep names");
//			//	Set the views for each column for each line. A sheep takes up 1 line on the screen
//			int[] toViews = new int[] { R.id.flock_names, R.id.sheep_names};
//	        Log.i("RemoveSheep", "after setting string array toViews for sheep names");
//	        myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_names, cursor ,fromColumns, toViews, 0);
////	        Log.i("RemoveSheep", "after setting myadapter to show names");
////	        setListAdapter(myadapter);
//	        Log.i("RemoveSheep", "after setting list adapter to show names");
	        
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
//			myadapter = new SimpleCursorAdapter(this, R.layout.list_entry_names, null, null, null, 0);
//			setListAdapter(myadapter);
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
		
		//	Set the date picker stuff here	
		Output = (TextView) findViewById(R.id.Output);
//        changeDate = (Button) findViewById(R.id.changeDate);
        
     // Get current date by calender       
        final Calendar c = Calendar.getInstance();
        year  = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day   = c.get(Calendar.DAY_OF_MONTH);
 
        // Show current date        
        Output.setText(new StringBuilder()
                // Month is 0 based, just add 1
        		.append(year).append("-").append(Utilities.Make2Digits(month + 1)).append("-").append(Utilities.Make2Digits(day)));
        removedate = String.valueOf(Output.getText());
   }
	public void changeDatePicker (View v) {                
        // On button click show datepicker dialog 
        showDialog(DATE_PICKER_ID);

    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_PICKER_ID:
             
            // open datepicker dialog. 
            // set date picker for current date 
            // add pickerListener listener to date picker
            return new DatePickerDialog(this, pickerListener, year, month, day);
        }
        return null;
    }
 
    private DatePickerDialog.OnDateSetListener pickerListener = new DatePickerDialog.OnDateSetListener() {
        // when dialog box is closed, below method will be called.
        @Override
        public void onDateSet(DatePicker view, int selectedYear,
               int selectedMonth, int selectedDay) {
            
            year  = selectedYear;
            month = selectedMonth;
            day   = selectedDay;
 
            // Show selected date 
            Output.setText(new StringBuilder()
            // Month is 0 based, just add 1
    		.append(year).append("-").append(Utilities.Make2Digits(month + 1)).append("-").append(Utilities.Make2Digits(day)));
            removedate = String.valueOf(Output.getText());
        	}
        };
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
		public void updateDatabase( View v ){
			//	Get the selected remove reason from the spinner
			remove_reason_spinner = (Spinner) findViewById(R.id.remove_reason_spinner);
	    	which_remove_reason = remove_reason_spinner.getSelectedItemPosition();
//	    	removedate has the string of the remove date I want to use
	    	//	Set the death date to be an empty string until we know whether the sheep died
	    	deathdate = "";
	    	//	Check for the remove reason being died
	    	//  TODO
	    	//	This needs to be fixed in case people set up their remove reasons differently
	    	//	Maybe search for died in the string of the reason first?
	    	//	Currently hard coded from the remove_reason_table from us
	    	
	    	switch (remove_reason_spinner.getSelectedItemPosition()){
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
					// 	Sheep Died so need to update with a death date as well as remove date. 
					//	Fill the deathdate with the same date as remove date.
					deathdate = removedate;
					break;
				case 7:
					// Sheep sold for breeding so only need to update remove date					
					break;
				case 8:
				case 9:
				case 10:
//				 	Sheep Died so need to update with a death date as well as remove date. 
					//	Fill the deathdate with the same date as remove date.
					deathdate = removedate;
					break;
	    	} // end of case switch   	
	    	Log.i ("before loop", "remove date  " + removedate);
	    	Log.i ("before loop", "death date  " + deathdate);
	    	
	    	//	Now need to loop through all the sheep and update the sheep_table
	    	//	Set the death date and remove dates and clear all alerts for this sheep
	        boolean temp_value;
	        int temp_location, temp_size;
	        temp_size = sp.size();
	        Log.i ("before loop", "the sp size is " + String.valueOf(temp_size));
	    	for (int i=0; i<temp_size; i++){
	    		temp_value = sp.valueAt(i);
	    		temp_location = sp.keyAt(i);
	    		if (temp_value){
	    			Log.i ("for loop", "the sheep " + " " + test_names.get(temp_location)+ " is checked");
	    			Log.i ("for loop", "the sheep id is " + String.valueOf(test_sheep_id.get(temp_location)));
	    	    	// This needs to be in a loop for all sheep_id s that we found. Setting each one to be thissheep_id
	    	    	cmd = String.format("update sheep_table set alert01 = '', death_date = '%s', remove_date = '%s', " +
	    	    			"remove_reason = '%s' where sheep_id =%d ", deathdate, removedate, which_remove_reason, 
	    	    			test_sheep_id.get(temp_location) ) ;
	    			Log.i("remove sheep ", "before cmd " + cmd);
	    			dbh.exec( cmd );
	    			Log.i("remove sheep ", "after cmd " + cmd);	    			
	    		}   		
	    	}// for loop
	    	// Now need to go back 
			try { 
				cursor.close();
			}
			catch (Exception e) {
//				Log.i("Back Button", " In catch stmt cursor");  
				// In this case there is no adapter so do nothing
			}
	       	dbh.closeDB();  	
	    	finish();		
		}
}


