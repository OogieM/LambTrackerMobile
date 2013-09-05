package com.weyr_associates.lambtracker;

import com.weyr_associates.lambtracker.MainActivity.SpinnerActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.util.Log;

import android.content.Context;
import android.view.LayoutInflater;

import android.view.ViewGroup;

import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class TestInterfaceDesigns extends Activity{
		private DatabaseHandler dbh;
		private Cursor 	cursor;
		
		public Button button;
		public Spinner test_dynamic_spinner;

		String          cmd;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			setTitle(R.string.app_name_long);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.test_interface_designs);
			String dbname = getString(R.string.real_database_file); 
	    	dbh = new DatabaseHandler( this, dbname );	
	    	
	    	test_dynamic_spinner = (Spinner) findViewById(R.id.test_dynamic_spinner);
	    	Log.i("testinterface", "in onCreate below test spinner");
	    	List<String> labels = new ArrayList<String>();
	         
	        // Select All fields from Query
	        cmd = "select * from tag_colors_table";
	        Object crsr = dbh.exec( cmd );  
	        cursor   = ( Cursor ) crsr;
	    	dbh.moveToFirstRecord();
	    	labels.add("Select a Color");
	    	// Log.i("testinterface", "in onCreate below got tag color table");
	        // looping through all rows and adding to list
	    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	    		labels.add(cursor.getString(2));
	    	}
	    	cursor.close();
	        Log.i("testinterface", "below if loop");
		        // Creating adapter for spinner
		        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
		                android.R.layout.simple_spinner_item, labels);
	
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			test_dynamic_spinner.setAdapter (dataAdapter);
			test_dynamic_spinner.setSelection(0);
//			Log.i("Activity", "In Spinner");
			test_dynamic_spinner.setOnItemSelectedListener(new SpinnerActivity());		  
		}
		
	private class SpinnerActivity extends Activity implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

//			Log.i("Activity", "In Spinner");
			Intent i = null;
			String teststring;
			teststring = String.valueOf (parent.getSelectedItemPosition());
			// Log.i("Spinner", "Position = "+teststring);

				switch (parent.getSelectedItemPosition()){		
				case 0:
			        // Don't want to do anything until something is selected so just break at position zero
					break;
			    case 1:
			    	teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			    	break;
				case 2:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 3:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 4:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 5:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 6:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 7:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 8:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 9:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 10:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 11:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 12:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 13:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 14:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 15:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 16:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 17:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 18:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 19:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	Log.i("Spinner", "Position = "+teststring);
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	Log.i("Spinner", "Position = "+teststring);
			        break;
			        
			}
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	
	}
	  // user clicked the 'back' button
    public void backBtn( View v )
    {
    	dbh.closeDB();   	
    	finish();
    }
	  // user clicked the 'saveScores' button
    public void saveScores( View v )
    {
    
//    	RadioButton negativeone = null;
//    	
//    	negativeone = (RadioButton) findViewById(R.id.radio_negativeone);
//    	
//    	Log.i("saveScores","Add saving here"); 		
//    	int id = ((RadioGroup)findViewById( R.id.test_radio_button )).getCheckedRadioButtonId();	
//    	Log.i("saveScores", "First radio button" + String.valueOf(id));
//    	if (negativeone.isChecked()){
//    		Log.i("saveScores", "Value is -1");
//    		}
     }
    
    public void onRadioButtonClicked( View v )
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();
        
        // Check which radio button was clicked
        // Need to add all the testing to get the data for the correct trait for saving into the database
        // this needs to be a generic onClicked somehow
        // perhaps add a variable to pass a link to the trait in the database?
        switch(v.getId()) {
            case R.id.radio_negativeone:
                if (checked)
                    // save a -1 score
                	Log.i("radio", "Value = -1");
                break;
            case R.id.radio_zero:
                if (checked)
                    // save a 0 score
                break;
            case R.id.radio_one:
                if (checked)
                    // save a 1 score
                break;
            case R.id.radio_two:
                if (checked)
                    // save a 2 score
                break;
            case R.id.radio_three:
                if (checked)
                    // save a 3 score
                break;
            case R.id.radio_four:
                if (checked)
                    // save a 4 score
                break;
            case R.id.radio_five:
                if (checked)
                    // save a 5 score
                break;
        }
    }
    
    }
