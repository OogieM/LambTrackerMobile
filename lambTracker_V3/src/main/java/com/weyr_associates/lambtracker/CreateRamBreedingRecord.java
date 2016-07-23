package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import java.util.Calendar;
import com.weyr_associates.lambtracker.LambingSheep.IncomingHandler;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.graphics.LightingColorFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TimePicker;

public class CreateRamBreedingRecord extends ListActivity {
	private DatabaseHandler dbh;
	private int year, month, day;
    private int hour, minute, second;
    static final int DATE_PICKER_ID = 1111;
    static final int TIME_PICKER_ID = 999;
	public Spinner service_type_spinner;
	private TextView dateoutput, timeoutput;
    private Button setDate;
    public String currentsetdate, currentsettime;
    public String currentyear;
	public int 		thissheep_id, nRecs, this_service;
	public Cursor 	cursor;
	public Object 	crsr;
	String     	cmd;
	public List<String> service_type;
	ArrayAdapter<String> dataAdapter;
	public List<String> test_names;
    public List<Integer> test_sheep_id;
    public SparseBooleanArray sparse_array;
    ListView test_name_list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_ram_breeding_record);
		String 	dbfile = getString(R.string.real_database_file) ;
	    dbh = new DatabaseHandler( this, dbfile );
//		Added the variable definitions here    	
	    thissheep_id = 0;
	    service_type_spinner = (Spinner) findViewById(R.id.service_type_spinner);
	   	service_type = new ArrayList<String>();      	
	   	
	   	// Select All fields from service types to build the spinner
       cmd = "select service_type from service_type_table";
       crsr = dbh.exec( cmd );  
       cursor   = ( Cursor ) crsr;
       dbh.moveToFirstRecord();
       service_type.add("Select a Service Type");
        // looping through all rows and adding to list
	   	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	   		service_type.add(cursor.getString(0));
			Log.i("RamBreeeding", " the service type is " + cursor.getString(0) ); 
	   	}	   	
	   	// Creating adapter for spinner
	   	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, service_type);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		service_type_spinner.setAdapter (dataAdapter);
		service_type_spinner.setSelection(0);

    	test_name_list = (ListView) findViewById(android.R.id.list);
    	final Calendar c = Calendar.getInstance();
        year  = c.get(Calendar.YEAR);
        currentyear = String.valueOf(year) + "%";
//			Now go get all the current sheep names and format them
		cmd = String.format( "select sheep_table.sheep_id as _id, flock_prefix_table.flock_name, sheep_table.sheep_name " +
				" from sheep_table inner join flock_prefix_table " +
				"on flock_prefix_table.flock_prefixid = sheep_table.flock_prefix" +
				" where (sheep_table.remove_date IS NULL or sheep_table.remove_date is '') and sheep_table.sex = 1 " +
				"and sheep_table.birth_date not like '%s' "+
				"order by sheep_table.sheep_name asc ", currentyear );  	        	
		Log.i("format record", " command is  " + cmd);
		crsr = dbh.exec( cmd );
		cursor   = ( Cursor ) crsr; 
		nRecs    = cursor.getCount();
		Log.i("setalerts", " nRecs is " + String.valueOf(nRecs));
		test_names = new ArrayList<String>(); 
       	test_sheep_id = new ArrayList<Integer>();
		cursor.moveToFirst();	
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
			test_names.add (cursor.getString(1) + " " + cursor.getString(2));
			test_sheep_id.add(cursor.getInt(0));
			Log.i("RamBreeeding", " the current sheep is " + cursor.getString(1)+ " " + cursor.getString(2) );
    	}
		cursor.moveToFirst();				
		if (nRecs > 0) {
	    	// format the sheep name records
	        ArrayAdapter<String> adapter = (new ArrayAdapter<String>(this, R.layout.list_entry_rams,test_names));
		    test_name_list.setAdapter(adapter);
	        test_name_list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        test_name_list.setOnItemClickListener(new OnItemClickListener(){
	            public void onItemClick(AdapterView<?> parent, View view,int position,long id) {
	                View v = test_name_list.getChildAt(position);
	                Log.i("in click","I am inside onItemClick and position is:"+String.valueOf(position));
	            }
	        
	        });
	        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	        sparse_array=getListView().getCheckedItemPositions();
		}  		
		else {
			// No sheep data - publish an empty list to clear sheep names
			Log.i("LookForSheep", "no current sheep");
		} 	
		//	Set the date picker stuff here	
		dateoutput = (TextView) findViewById(R.id.dateoutput);
        timeoutput = (TextView) findViewById(R.id.timeoutput);
     // Get current date and time by calender       
     //   final Calendar c = Calendar.getInstance();
        year  = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day   = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        // Show current date        
        dateoutput.setText(new StringBuilder()
                // Month is 0 based, just add 1
        		.append(year).append("-").append(Utilities.Make2Digits(month + 1)).append("-").append(Utilities.Make2Digits(day)));
        currentsetdate = String.valueOf(dateoutput.getText());
        Log.i("onCreate"," "+ currentsetdate);
        timeoutput.setText(new StringBuilder()
        .append(Utilities.Make2Digits(hour)).append(":").append(Utilities.Make2Digits(minute)).append(":00"));
        currentsettime = String.valueOf(timeoutput.getText());
        Log.i("onCreate"," "+ currentsettime);
	}
	public void updateDatabase( View v ){
		boolean temp_value;
		int temp_location, temp_size;
		service_type_spinner = (Spinner) findViewById(R.id.service_type_spinner);
		this_service = service_type_spinner.getSelectedItemPosition();
		currentsetdate = String.valueOf(dateoutput.getText());
        temp_size = sparse_array.size();
//        Log.i ("before loop", "the sp size is " + String.valueOf(temp_size));
    	for (int i=0; i<temp_size; i++){
    		temp_value = sparse_array.valueAt(i);
    		temp_location = sparse_array.keyAt(i);
    		if (temp_value){
    			thissheep_id = test_sheep_id.get(temp_location);
    			Log.i ("for loop", "the sheep " + " " + test_names.get(temp_location)+ " is checked");
    			Log.i ("for loop", "the sheep id is " + String.valueOf(test_sheep_id.get(temp_location)));
     			cmd = String.format("insert into breeding_record_table (ram_id, date_ram_in, time_ram_in, date_ram_out, time_ram_out, service_type) " +
     					"	values (%s,'%s','%s','%s','%s',%s)" , thissheep_id, currentsetdate, currentsettime, "", "", this_service );
    			Log.i("add record ", "before cmd " + cmd);
    			dbh.exec( cmd);
    			Log.i("add record ", "after cmd " + cmd);	    			
    		}   		
    	}// for loop
    	Log.i("after for ", "loop in add record.");  
    	// Now need to go back 
		try { 
			cursor.close();
		}
		catch (Exception e) {
			Log.i("end of ", "add record. In catch stmt cursor");  
					}
       	dbh.closeDB();  	
       	this.finish();			   	
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_ram_breeding_record, menu);
		return true;
	}
	public void setDatePicker (View v) {                
        // On button click show datepicker dialog 
        showDialog(DATE_PICKER_ID);

    }
	public void setTimePicker (View v) {                
        // On button click show timepicker dialog 
        showDialog(TIME_PICKER_ID);

    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_PICKER_ID:
             
            // open datepicker dialog. 
            // set date picker for current date 
            // add pickerListener listener to date picker
            return new DatePickerDialog(this, pickerListener, year, month, day);
        case TIME_PICKER_ID:
        	return new TimePickerDialog(this, timePickerListener, hour, minute, false);
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
            dateoutput.setText(new StringBuilder()
            // Month is 0 based, just add 1
    		.append(year).append("-").append(Utilities.Make2Digits(month + 1)).append("-").append(Utilities.Make2Digits(day)));
            currentsetdate = String.valueOf(dateoutput.getText());
            Log.i("set date"," "+ currentsetdate);
        	}
        };
    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        // when dialog box is closed, below method will be called.
        @Override
        public void onTimeSet(TimePicker view, int selectedHour,
               int selectedMinute) {
            
            hour  = selectedHour;
            minute = selectedMinute;
            second = 00;
            // Show selected time 
            timeoutput.setText(new StringBuilder()
            .append(Utilities.Make2Digits(hour)).append(":").append(Utilities.Make2Digits(minute)).append(":00"));
            currentsettime = String.valueOf(timeoutput.getText());
            Log.i("set time"," "+ currentsettime);
        	}
        };
}
