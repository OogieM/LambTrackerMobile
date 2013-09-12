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
	public int trait01, trait02, trait03, trait04, trait05, trait06, trait07;
	public String trait01_label, trait02_label, trait03_label, trait04_label, trait05_label, trait06_label, trait07_label; 
	public Spinner trait01_spinner, trait02_spinner, trait03_spinner, trait04_spinner, 
		trait05_spinner, trait06_spinner, trait07_spinner;
	public List<String> scored_evaluation_traits, data_evaluation_traits;
	
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
        // enable the Create an evaluation button when we come in to start this task
    	Button btn2 = (Button) findViewById( R.id.create_evaluation_task_btn );
    	btn2.setEnabled(true);
    	
        // Select All fields from trait table that are scored type and get set to fill the spinners
        cmd = "select * from evaluation_trait_table where trait_type = 1";
        Object crsr = dbh.exec( cmd ); ;
//       Log.i("testing", "executed command " + cmd);
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	scored_evaluation_traits.add("Select a Trait");
//    	 Log.i("testinterface", "in onCreate below got evaluation straits table");
        // looping through all rows and adding to list all the scored evaluatin types
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		scored_evaluation_traits.add(cursor.getString(1));
    	}
    	cursor.close();
    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scored_evaluation_traits);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	
	
    	//Fill the 5 scored spinners from the same list of evaluation traits.
    	trait01_spinner = (Spinner) findViewById(R.id.trait01_spinner);	
		trait01_spinner.setAdapter (dataAdapter);
		trait01_spinner.setSelection(0);
		
		trait02_spinner = (Spinner) findViewById(R.id.trait02_spinner);
		trait02_spinner.setAdapter (dataAdapter);
		trait02_spinner.setSelection(0);
		
		trait03_spinner = (Spinner) findViewById(R.id.trait03_spinner);
		trait03_spinner.setAdapter (dataAdapter);
		trait03_spinner.setSelection(0);
		
		trait04_spinner = (Spinner) findViewById(R.id.trait04_spinner);
		trait04_spinner.setAdapter (dataAdapter);
		trait04_spinner.setSelection(0);
		
		trait05_spinner = (Spinner) findViewById(R.id.trait05_spinner);
		trait05_spinner.setAdapter (dataAdapter);
		trait05_spinner.setSelection(0);
		
		// Now set up for the two real data traits
		data_evaluation_traits = new ArrayList<String>();
        
        // Select All fields from trait table that are real data type and get set to fill the spinners
        cmd = "select * from evaluation_trait_table where trait_type = 2";
        crsr = dbh.exec( cmd );
//       Log.i("testing", "executed command " + cmd);
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	data_evaluation_traits.add("Select a Trait");
//    	 Log.i("testinterface", "in onCreate below got evaluation traits table");
        // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		data_evaluation_traits.add(cursor.getString(1));
    	}
    	cursor.close();
//        Log.i("createEval ", "below for loop");
    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, data_evaluation_traits);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	

    	trait06_spinner = (Spinner) findViewById(R.id.trait06_spinner);	
		trait06_spinner.setAdapter (dataAdapter);
		trait06_spinner.setSelection(0);
		
		trait07_spinner = (Spinner) findViewById(R.id.trait07_spinner);
		trait07_spinner.setAdapter (dataAdapter);
		trait07_spinner.setSelection(0);
	
	}
//	private class SpinnerActivity extends Activity implements OnItemSelectedListener {
//		becasue we only get the spinner data when the user selects the create an evaluation 
//		this class is not needed.
//			}
//		@Override
//		public void onNothingSelected(AdapterView<?> arg0) {
//			
//		}
//	}
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
	    	Object 			crsr;
	    	String cmd;
	   // Need to get the position and text for every spinner and the real data points and use
	   // this to fill the actual evaluation task screen with what we are looking at.
	    	trait01_spinner = (Spinner) findViewById(R.id.trait01_spinner);
	    	trait02_spinner = (Spinner) findViewById(R.id.trait02_spinner);
	    	trait03_spinner = (Spinner) findViewById(R.id.trait03_spinner);
	    	trait04_spinner = (Spinner) findViewById(R.id.trait04_spinner);
	    	trait05_spinner = (Spinner) findViewById(R.id.trait05_spinner);
	    	trait06_spinner = (Spinner) findViewById(R.id.trait06_spinner);
	    	trait07_spinner = (Spinner) findViewById(R.id.trait07_spinner);
	    	
	    	// fill the labels with the contents of the various spinners
	    	trait01_label = trait01_spinner.getSelectedItem().toString();
//	    	Log.i("trait01_spinner ", "Contents = "+ trait01_label);
	    	trait02_label = trait02_spinner.getSelectedItem().toString();
	    	trait03_label = trait03_spinner.getSelectedItem().toString();
	    	trait04_label = trait04_spinner.getSelectedItem().toString();
	    	trait05_label = trait05_spinner.getSelectedItem().toString();
	    	trait06_label = trait06_spinner.getSelectedItem().toString();
	    	trait07_label = trait07_spinner.getSelectedItem().toString();

	    	// Need to get the id_traitid from the evaluation trait table and store
	    	// that as the actual thing we reference in the evaluate sheep section since it won't change
	    	// from time to time
	    	
	    	// Should be able to enclose each of these into an IF statement to see if a trait was selected
	    	// and if not then do not do the database lookup but not implemented yet
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait01_label);
//	    	Log.i("query trait1", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait01 = dbh.getInt(0);

	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait02_label);
	    	Log.i("query trait2", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait02 = dbh.getInt(0);
	        
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait03_label);
	    	Log.i("query trait3", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait03 = dbh.getInt(0);
	        
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait04_label);
	    	Log.i("query trait4", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait04 = dbh.getInt(0);
	        
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait05_label);
	    	Log.i("query trait5", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait05 = dbh.getInt(0);
	        
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait06_label);
	        Log.i("query trait6", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait06 = dbh.getInt(0);

	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait07_label);
	        Log.i("query trait7", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait07 = dbh.getInt(0);

	        // We have all the actual traits now to get their id_traitid and store it for look-up later
	    	cmd = "drop table if exists temp_table";
	    	dbh.exec (cmd);
	    	
	    	cmd = "CREATE TABLE temp_table (id_temp INTEGER PRIMARY KEY " +
	    			"AUTOINCREMENT, temp_eval INTEGER NOT NULL)";
//	    	Log.i("db create ", cmd);
	    	dbh.exec (cmd);
	    	
	    	cmd = String.format( "insert into temp_table (temp_eval) values('%s')",trait01);
//	    	Log.i("db cmd ", cmd);	    	
	    	dbh.exec( cmd );
	    	cmd = String.format( "insert into temp_table (temp_eval) values('%s')",trait02);
//	    	Log.i("db cmd ", cmd);
	    	dbh.exec( cmd );
	    	cmd = String.format( "insert into temp_table (temp_eval) values('%s')",trait03);
//	    	Log.i("db cmd ", cmd);
	    	dbh.exec( cmd );
	    	cmd = String.format( "insert into temp_table (temp_eval) values('%s')",trait04);
//	    	Log.i("db cmd ", cmd);
	    	dbh.exec( cmd );
	    	cmd = String.format( "insert into temp_table (temp_eval) values('%s')",trait05);
//	    	Log.i("db cmd ", cmd);
	    	dbh.exec( cmd );
	    	cmd = String.format( "insert into temp_table (temp_eval) values('%s')",trait06);
//	    	Log.i("db cmd ", cmd);
	    	dbh.exec( cmd );
	    	cmd = String.format( "insert into temp_table (temp_eval) values('%s')",trait07);
//	    	Log.i("db cmd ", cmd);
	    	dbh.exec( cmd );
	    	// All done need to disable the create create_evaluation_task_btn so we don't do it twice
	    	
//	    	Disable the Next Record and Prev. Record button until we have multiple records
	       	Button btn2 = (Button) findViewById( R.id.create_evaluation_task_btn );
	    	btn2.setEnabled(false); 
	    	
    }	 
}
