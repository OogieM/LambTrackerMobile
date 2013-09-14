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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
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
	public int trait01, trait02, trait03, trait04, trait05, trait06, trait07, trait06_unitid, trait07_unitid;
	public String trait01_label, trait02_label, trait03_label, trait04_label, trait05_label; 
	public String trait06_label, trait07_label, trait06_units, trait07_units; 
	public Spinner trait01_spinner, trait02_spinner, trait03_spinner, trait04_spinner, 
		trait05_spinner, trait06_spinner, trait07_spinner;
	public Spinner trait06_units_spinner, trait07_units_spinner;
	public List<String> scored_evaluation_traits, data_evaluation_traits, trait_units;
	
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
        Log.i("testing", "executed command " + cmd);
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	scored_evaluation_traits.add("Select a Trait");
    	 Log.i("testinterface", "in onCreate below got evaluation straits table");
        // looping through all rows and adding to list all the scored evaluation types
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
		
		Log.i("create eval", "got score spinners initialized");
		
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
//        Log.i("createEval ", "below got eval traits");
    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, data_evaluation_traits);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	

    	trait06_spinner = (Spinner) findViewById(R.id.trait06_spinner);	
		trait06_spinner.setAdapter (dataAdapter);
		trait06_spinner.setSelection(0);
		
		trait07_spinner = (Spinner) findViewById(R.id.trait07_spinner);
		trait07_spinner.setAdapter (dataAdapter);
		trait07_spinner.setSelection(0);
	
		Log.i("create eval", "got real spinners initialized");
		
		trait_units = new ArrayList<String>();
		
        // Select All fields from trait units table and get set to fill the spinners
        cmd = "select * from units_table ";
        crsr = dbh.exec( cmd );
        Log.i("units ", "executed command " + cmd);
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	trait_units.add("Select a Unit");
       // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		trait_units.add(cursor.getString(1));
    	}
    	cursor.close();

    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, trait_units);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	
    	
    	trait06_units_spinner = (Spinner) findViewById(R.id.trait06_units_spinner);	
    	trait06_units_spinner.setAdapter (dataAdapter);
    	trait06_units_spinner.setSelection(0);
		
    	trait07_units_spinner = (Spinner) findViewById(R.id.trait07_units_spinner);
    	trait07_units_spinner.setAdapter (dataAdapter);
    	trait07_units_spinner.setSelection(0);
    	Log.i("create eval", "got units spinners initialized");
		       	
    	cmd = "select * from last_eval_table";
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        
    	trait01 = dbh.getInt(1);
    	cursor.moveToNext();	
    	trait02 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait03 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait04 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait05 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait06 = dbh.getInt(1);
    	trait06_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait07 = dbh.getInt(1);
    	trait07_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	cursor.close();
      	
    	Log.i("results last ","eval trait01 "+String.valueOf(trait01));
    	Log.i("results last ","eval trait02 "+String.valueOf(trait02));
    	Log.i("results last ","eval trait03 "+String.valueOf(trait03));
    	Log.i("results last ","eval trait04 "+String.valueOf(trait04));
    	Log.i("results last ","eval trait05 "+String.valueOf(trait05));
    	Log.i("results last ","eval trait06 "+String.valueOf(trait06));
    	Log.i("results last ","eval trait06 units "+String.valueOf(trait06_unitid));
    	Log.i("results last ","eval trait07 "+String.valueOf(trait07));
    	Log.i("results last ","eval trait07 units "+String.valueOf(trait07_unitid));
    	
        	// need to get what position within the current scored_evaluation_traits this trait is 
	        // and set the spinner position to be that position
    	if (trait01!=0) {
        	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait01);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait01_label = dbh.getStr(0);
	        i = scored_evaluation_traits.indexOf(trait01_label);
	        trait01_spinner.setSelection(i);
	        cursor.close();
    	}
    	if (trait02!=0) {    
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait02);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait02_label = dbh.getStr(0);
	        i = scored_evaluation_traits.indexOf(trait02_label);
	        trait02_spinner.setSelection(i);
	        cursor.close();
    	}
    	if (trait03!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait03);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait03_label = dbh.getStr(0);
	        i = scored_evaluation_traits.indexOf(trait03_label);
	        trait03_spinner.setSelection(i);
	        cursor.close();
    	}
    	if (trait04!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait04);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait04_label = dbh.getStr(0);
	        i = scored_evaluation_traits.indexOf(trait04_label);
	        trait04_spinner.setSelection(i);
	        cursor.close();
    	}
    	if (trait05!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait05);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait05_label = dbh.getStr(0);
	        i = scored_evaluation_traits.indexOf(trait05_label);
	        trait05_spinner.setSelection(i);
	        cursor.close();
    	}
    	if (trait06!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait06);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait06_label = dbh.getStr(0);
	        i = data_evaluation_traits.indexOf(trait06_label);
	        trait06_spinner.setSelection(i);
	        cursor.close();
	        // need to also get the units for stored trait06
	        cmd = String.format("select units_table.units_name from units_table where " +
	        "id_unitsid='%s'", trait06_unitid);
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait06_units = dbh.getStr(0);
	        i = trait_units.indexOf(trait06_units) ;
	        trait06_units_spinner.setSelection(i); 
	        cursor.close();
    	}
    	if (trait07!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait07);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait07_label = dbh.getStr(0);
	        i = data_evaluation_traits.indexOf(trait07_label);
	        trait07_spinner.setSelection(i);
	        cursor.close();
	     // need to also get the units for stored trait07
	        cmd = String.format("select units_table.units_name from units_table where " +
	        "id_unitsid='%s'", trait07_unitid);
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait07_units = dbh.getStr(0);
	        i = trait_units.indexOf(trait07_units) ;
	        trait07_units_spinner.setSelection(i); 
	        cursor.close();
    	}   
    	   
    	
	        Log.i("Create eval", "The selected traits are: ");
	    	Log.i("Create eval", "trait01 " + trait01_label);
	    	Log.i("Create eval", "trait02 " + trait02_label);
	    	Log.i("Create eval", "trait03 " + trait03_label);
	    	Log.i("Create eval", "trait04 " + trait04_label);
	    	Log.i("Create eval", "trait05 " + trait05_label);
	    	Log.i("Create eval", "trait06 " + trait06_label);
	    	Log.i("Create eval", "trait07 " + trait07_label);
        	
	}
 
//	private class SpinnerActivity extends Activity implements OnItemSelectedListener {
//		because we only get the spinner data when the user selects the create an evaluation 
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
	    	trait06_units_spinner = (Spinner) findViewById(R.id.trait06_units_spinner);	
	    	trait07_units_spinner = (Spinner) findViewById(R.id.trait07_units_spinner);
	    	
	    	// fill the labels with the contents of the various spinners
	    	trait01_label = trait01_spinner.getSelectedItem().toString();
	    	trait02_label = trait02_spinner.getSelectedItem().toString();
	    	trait03_label = trait03_spinner.getSelectedItem().toString();
	    	trait04_label = trait04_spinner.getSelectedItem().toString();
	    	trait05_label = trait05_spinner.getSelectedItem().toString();
	    	trait06_label = trait06_spinner.getSelectedItem().toString();
	    	trait07_label = trait07_spinner.getSelectedItem().toString();
	    	trait06_units = trait06_units_spinner.getSelectedItem().toString();
	    	trait07_units = trait07_units_spinner.getSelectedItem().toString();
	    	
	    	Log.i("Create eval", "The selected traits are: ");
	    	Log.i("Create eval", "trait01 " + trait01_label);
	    	Log.i("Create eval", "trait02 " + trait02_label);
	    	Log.i("Create eval", "trait03 " + trait03_label);
	    	Log.i("Create eval", "trait04 " + trait04_label);
	    	Log.i("Create eval", "trait05 " + trait05_label);
	    	Log.i("Create eval", "trait06 " + trait06_label);
	    	Log.i("Create eval", "trait06 units " + trait06_units);
	    	Log.i("Create eval", "trait07 " + trait07_label);
	    	Log.i("Create eval", "trait07 units " + trait07_units);
	    	
	    	// Need to get the id_traitid from the evaluation trait table and store
	    	// that as the actual thing we reference in the evaluate sheep section since it won't change
	    	// from time to time
	    	
	    	// Should be able to enclose each of these into an IF statement to see if a trait was selected
	    	// and if not then do not do the database lookup but not implemented yet
	    	if (trait01_label == "Select a Trait") {
	    			trait01 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait01_label);
//	    	Log.i("query trait1", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait01 = dbh.getInt(0);
	        }

	    	if (trait02_label == "Select a Trait") {
    			trait02 = 0;
	    	}else
	    	{
	    		cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait02_label);
//	    		Log.i("query trait2", cmd);
	    		crsr = dbh.exec( cmd );
	    		cursor   = ( Cursor ) crsr;
	    		dbh.moveToFirstRecord();
	    		trait02 = dbh.getInt(0);
	    	}
	    	if (trait03_label == "Select a Trait") {
    			trait03 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait03_label);
//	    	Log.i("query trait3", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait03 = dbh.getInt(0);
	    	}
	    	if (trait04_label == "Select a Trait") {
    			trait04 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait04_label);
//	    	Log.i("query trait4", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait04 = dbh.getInt(0);
	    	}
	    	if (trait05_label == "Select a Trait") {
    			trait05 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait05_label);
//	    	Log.i("query trait5", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait05 = dbh.getInt(0);
	    	}
	    	if (trait06_label == "Select a Trait") {
    			trait06 = 0;
	    	}else
	    	{
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait06_label);
//	        Log.i("query trait6", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait06 = dbh.getInt(0);
	    	}
	    	if (trait07_label == "Select a Trait") {
    			trait07 = 0;
	    	}else
	    	{
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait07_label);
//	        Log.i("query trait7", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait07 = dbh.getInt(0);
	    	}
	        // Now get the units the user selected as well
	        
	    	if (trait06_units == "Select a Unit") {
	    		trait06_unitid = 0;
	    	}else
	    	{
	        cmd = String.format("select units_table.id_unitsid from units_table " +
	    			"where units_name='%s'", trait06_units);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait06_unitid = dbh.getInt(0);
	    	}
	    	if (trait07_units == "Select a Unit") {
	    		trait07_unitid = 0;
	    	}else
	    	{
	        cmd = String.format("select units_table.id_unitsid from units_table " +
	    			"where units_name='%s'", trait07_units);
	        Log.i("query trait7", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait07_unitid = dbh.getInt(0);
	    	}
	    	
	        Log.i("results new ","eval trait01 "+String.valueOf(trait01));
	    	Log.i("results new ","eval trait02 "+String.valueOf(trait02));
	    	Log.i("results new ","eval trait03 "+String.valueOf(trait03));
	    	Log.i("results new ","eval trait04 "+String.valueOf(trait04));
	    	Log.i("results new ","eval trait05 "+String.valueOf(trait05));
	    	Log.i("results new ","eval trait06 "+String.valueOf(trait06));
	    	Log.i("results new ","units trait06 "+String.valueOf(trait06_unitid));
	    	Log.i("results new ","eval trait07 "+String.valueOf(trait07));
	    	Log.i("results new ","units trait07 "+String.valueOf(trait07_unitid));
	    	

	        // We have all the actual traits now to save their id_traitid and store it for look-up later
   
	        cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=1", trait01 );
	    	dbh.exec( cmd );
	        cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=2", trait02 );
	    	dbh.exec( cmd );
	        cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=3", trait03 );
	    	dbh.exec( cmd );	    	
	        cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=4", trait04 );
	    	dbh.exec( cmd );
	        cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=5", trait05 );
	    	dbh.exec( cmd );
	        cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=6", trait06);
	        dbh.exec( cmd );
	        cmd  = String.format( "update last_eval_table set id_unitsid=%s where id_lastevalid=6", trait06_unitid );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=7", trait07);
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_unitsid=%s where id_lastevalid=7", trait07_unitid );
	    	dbh.exec( cmd );
	    	
	    	// verify what we stored used in debugging
	    	cmd = "select * from last_eval_table ";
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        
	        trait01 = dbh.getInt(1);
	    	cursor.moveToNext();	
	    	trait02 = dbh.getInt(1);
	    	cursor.moveToNext();
	    	trait03 = dbh.getInt(1);
	    	cursor.moveToNext();
	    	trait04 = dbh.getInt(1);
	    	cursor.moveToNext();
	    	trait05 = dbh.getInt(1);
	    	cursor.moveToNext();
	    	trait06 = dbh.getInt(1);
	    	trait06_unitid = dbh.getInt(2);
	    	cursor.moveToNext();
	    	trait07 = dbh.getInt(1);
	    	trait07_unitid = dbh.getInt(2);
	    	cursor.close();
	      	
	    	Log.i("results saved ","eval trait01 "+String.valueOf(trait01));
	    	Log.i("results saved ","eval trait02 "+String.valueOf(trait02));
	    	Log.i("results saved ","eval trait03 "+String.valueOf(trait03));
	    	Log.i("results saved ","eval trait04 "+String.valueOf(trait04));
	    	Log.i("results saved ","eval trait05 "+String.valueOf(trait05));
	    	Log.i("results saved ","eval trait06 "+String.valueOf(trait06));
	    	Log.i("results saved ","units trait06 "+String.valueOf(trait06_unitid));
	    	Log.i("results saved ","eval trait07 "+String.valueOf(trait07));
	    	Log.i("results saved ","units trait07 "+String.valueOf(trait07_unitid));
	    	
	    	// All done need to disable the create create_evaluation_task_btn so we don't do it twice
	       	Button btn2 = (Button) findViewById( R.id.create_evaluation_task_btn );
	    	btn2.setEnabled(false); 
	    	
    }	

}
