package com.weyr_associates.lambtracker;

import java.util.List;

import android.database.Cursor;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.weyr_associates.lambtracker.MainActivity.SpinnerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.content.res.Resources;
import android.view.LayoutInflater;

import android.view.ViewGroup;

import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.weyr_associates.lambtracker.MainActivity.SpinnerActivity;

public class CreateSheepEvaluation extends Activity {

	private DatabaseHandler dbh;
	private Cursor 	cursor;
	
	public Button button;
	public Spinner trait01_spinner, trait02_spinner, trait03_spinner, trait04_spinner, 
		trait05_spinner;
	List<String> scored_evaluation_traits, data_evaluation_traits;
	
	ArrayAdapter<String> dataAdapter;
	String     	cmd;
	Integer 	i;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTitle(R.string.app_name_long);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_sheep_evaluation);
		String dbname = getString(R.string.real_database_file); 
    	dbh = new DatabaseHandler( this, dbname );	
    	
    	scored_evaluation_traits = new ArrayList<String>();
         
        // Select All fields from trait table that are score type and get set to fill the spinners
        cmd = "select * from evaluation_trait_table where trait_type = 1";
        Object crsr = dbh.exec( cmd ); ;
//       Log.i("testing", "executed command " + cmd);
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	scored_evaluation_traits.add("Select a Trait");
//    	 Log.i("testinterface", "in onCreate below got evaluation straits table");
        // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		scored_evaluation_traits.add(cursor.getString(1));
    	}
    	cursor.close();
//        Log.i("createEval ", "below for loop");
    	
    	
    	
    	trait01_spinner = (Spinner) findViewById(R.id.trait01_spinner);
    	
//    	getResources().getResourceEntryName();
//		getResources().getResourceName(which_spinner2);
    	
    	dataAdapter = new ArrayAdapter<String>(this,
	                android.R.layout.simple_spinner_item, scored_evaluation_traits);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	
		trait01_spinner.setAdapter (dataAdapter);
		trait01_spinner.setSelection(0);
		trait01_spinner.setOnItemSelectedListener(new SpinnerActivity());
		
//		String trait01_id = String.valueOf(findViewById(R.id.trait01_spinner));
//		Log.i("Trait 01 id ", trait01_id )	;
		
		trait02_spinner = (Spinner) findViewById(R.id.trait02_spinner);
		dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scored_evaluation_traits);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		trait02_spinner.setAdapter (dataAdapter);
		trait02_spinner.setSelection(0);
		trait02_spinner.setOnItemSelectedListener(new SpinnerActivity());
		
		trait03_spinner = (Spinner) findViewById(R.id.trait03_spinner);
		dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scored_evaluation_traits);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		trait03_spinner.setAdapter (dataAdapter);
		trait03_spinner.setSelection(0);
		trait03_spinner.setOnItemSelectedListener(new SpinnerActivity());
		
		trait04_spinner = (Spinner) findViewById(R.id.trait04_spinner);
		dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scored_evaluation_traits);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		trait04_spinner.setAdapter (dataAdapter);
		trait04_spinner.setSelection(0);
		trait04_spinner.setOnItemSelectedListener(new SpinnerActivity());
		
		trait05_spinner = (Spinner) findViewById(R.id.trait05_spinner);
		dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scored_evaluation_traits);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		trait05_spinner.setAdapter (dataAdapter);
		trait05_spinner.setSelection(0);
		trait05_spinner.setOnItemSelectedListener(new SpinnerActivity());
	
	}
	private class SpinnerActivity extends Activity implements OnItemSelectedListener {
		
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

//			Log.i("Activity", "In Spinner activity before the case statement");
			String string_position;
			String which_spinner ; 
			int which_spinner2;
			
			string_position = String.valueOf (parent.getSelectedItemPosition());
			which_spinner = String.valueOf (parent);
			Log.i("Which Spinner ", which_spinner);
			Log.i("Spinner Position ", string_position);
			
			which_spinner2 = parent.getId();
			Log.i("Which Spinner ", which_spinner);
			Log.i("Which Spinner ID ", String.valueOf(which_spinner2));
			Log.i("Spinner Position ", string_position);
			
//			String res = getResources().getResourceEntryName(which_spinner2);
//			Log.i("Which Spinner name ", res);
//			getResources().getResourceName(which_spinner2);
			
//			Log.i("Which Spinner ", which_spinner);
//			Log.i("Which Spinner ID ", String.valueOf (which_spinner2));
//			Log.i("Spinner Position ", string_position);
			
			
//			switch (parent){
//			case 0:
//				
//			}

				switch (parent.getSelectedItemPosition()){		
				case 0:
			        // Don't want to do anything until something is selected so just break at position zero
					break;
			    case 1:
			    	// Save the spinner and position into an array
			    	
			    	break;
				case 2:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 3:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 4:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 5:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 6:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 7:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 8:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 9:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 10:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 11:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 12:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 13:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 14:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 15:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 16:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 17:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 18:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
			        break;
				case 19:
//					teststring = String.valueOf (parent.getSelectedItemPosition());
//			    	Log.i("Spinner", "Position = "+teststring);
//			    	teststring = trait01_spinner.getSelectedItem().toString();
//			    	Log.i("Spinner", "Position = "+teststring);
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
	   public void helpBtn( View v )
	    {
	   	// Display help here   	
			AlertDialog.Builder builder = new AlertDialog.Builder( this );
			builder.setMessage( R.string.help_create_evaluate )
		           .setTitle( R.string.help_warning );
			builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int idx) {
		               // User clicked OK button 
		        	  
		    		   clearBtn( null );
		               }
		       });		
			AlertDialog dialog = builder.create();
			dialog.show();
			
	    }
	    // user clicked 'clear' button
	    public void clearBtn( View v )
		    {
		    // clear out the display of everything

	    }	   

	    public void createEval( View v )
	    {
	    	int trait01, trait02, trait03, trait04, trait05;
	   // Need to get the position and text for every spinner and the real data points and use
	   // this to fill the actual evaluation task screen with what we are looking at.
	    	trait01_spinner = (Spinner) findViewById(R.id.trait01_spinner);
	    	trait02_spinner = (Spinner) findViewById(R.id.trait02_spinner);
	    	trait03_spinner = (Spinner) findViewById(R.id.trait03_spinner);
	    	trait04_spinner = (Spinner) findViewById(R.id.trait04_spinner);
	    	trait05_spinner = (Spinner) findViewById(R.id.trait05_spinner);
	    	trait01 =  trait01_spinner.getSelectedItemPosition();
	    	Log.i("trait01_spinner ", "Position = "+String.valueOf(trait01));	    	
	    	
	    	trait02 =  trait02_spinner.getSelectedItemPosition();
	    	Log.i("trait02_spinner ", "Position = "+String.valueOf(trait02));	   
	    	
	    	trait03 =  trait03_spinner.getSelectedItemPosition();
	    	Log.i("trait03_spinner ", "Position = "+String.valueOf(trait03));	   
	    	
	    	trait04 =  trait04_spinner.getSelectedItemPosition();
	    	Log.i("trait04_spinner ", "Position = "+String.valueOf(trait04));	   
	    	
	    	trait05 =  trait05_spinner.getSelectedItemPosition();
	    	Log.i("trait05_spinner ", "Position = "+String.valueOf(trait05));	   
	    	
	    	
	    	
    }	 
}
