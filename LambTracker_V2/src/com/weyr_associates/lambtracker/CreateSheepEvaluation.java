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
	public int trait01, trait02, trait03, trait04, trait05, trait06, trait07, trait08, trait09, trait10;
	public int trait11, trait12, trait13, trait14, trait15;
	public int trait16, trait17, trait18, trait19, trait20;
	public int trait11_unitid, trait12_unitid, trait13_unitid, trait14_unitid, trait15_unitid;
	public String trait01_label, trait02_label, trait03_label, trait04_label, trait05_label, trait06_label, 
		trait07_label, trait08_label, trait09_label, trait10_label, trait11_label, trait12_label, 
		trait13_label, trait14_label, trait15_label, trait16_label, trait17_label, trait18_label, trait19_label,
		trait20_label; 
	public String  trait11_units, trait12_units, trait13_units, trait14_units, trait15_units; 
	public Spinner trait01_spinner, trait02_spinner, trait03_spinner, trait04_spinner, 
		trait05_spinner, trait06_spinner,trait07_spinner, trait08_spinner, trait09_spinner, 
		trait10_spinner;
	public Spinner trait11_spinner,trait12_spinner, trait13_spinner, trait14_spinner, trait15_spinner ;
	public Spinner trait16_spinner,trait17_spinner, trait18_spinner, trait19_spinner, trait20_spinner ;
	public Spinner trait11_units_spinner, trait12_units_spinner, trait13_units_spinner, 
		trait14_units_spinner, trait15_units_spinner;
	public List<String> scored_evaluation_traits, data_evaluation_traits, 
		custom_evaluation_traits, trait_units;
	
	ArrayAdapter<String> dataAdapter;
	String     	cmd;
	Integer 	i;
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	scored_evaluation_traits.add("Select a Trait");
        // looping through all rows and adding to list all the scored evaluation types
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		scored_evaluation_traits.add(cursor.getString(1));
    		Log.i("scored ", "traits is " + cursor.getString(1));
    	}
    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, scored_evaluation_traits);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	
	
    	//Fill the 10 scored spinners from the same list of evaluation traits.
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
		
		trait06_spinner = (Spinner) findViewById(R.id.trait06_spinner);
		trait06_spinner.setAdapter (dataAdapter);
		trait06_spinner.setSelection(0);
		
		trait07_spinner = (Spinner) findViewById(R.id.trait07_spinner);
		trait07_spinner.setAdapter (dataAdapter);
		trait07_spinner.setSelection(0);
		
		trait08_spinner = (Spinner) findViewById(R.id.trait08_spinner);
		trait08_spinner.setAdapter (dataAdapter);
		trait08_spinner.setSelection(0);
		
		trait09_spinner = (Spinner) findViewById(R.id.trait09_spinner);
		trait09_spinner.setAdapter (dataAdapter);
		trait09_spinner.setSelection(0);
		
		trait10_spinner = (Spinner) findViewById(R.id.trait10_spinner);
		trait10_spinner.setAdapter (dataAdapter);
		trait10_spinner.setSelection(0);
				
		Log.i("create eval", "got score spinners initialized");
		
		// Now set up for the five real data traits
		data_evaluation_traits = new ArrayList<String>();
        
        // Select All fields from trait table that are real data type and get set to fill the spinners
        cmd = "select * from evaluation_trait_table where trait_type = 2";
        crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	data_evaluation_traits.add("Select a Trait");
        // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		data_evaluation_traits.add(cursor.getString(1));
    	}
//        Log.i("createEval ", "below got eval traits");
    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, data_evaluation_traits);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	

    	trait11_spinner = (Spinner) findViewById(R.id.trait11_spinner);	
		trait11_spinner.setAdapter (dataAdapter);
		trait11_spinner.setSelection(0);
		
		trait12_spinner = (Spinner) findViewById(R.id.trait12_spinner);
		trait12_spinner.setAdapter (dataAdapter);
		trait12_spinner.setSelection(0);
		
    	trait13_spinner = (Spinner) findViewById(R.id.trait13_spinner);	
    	trait13_spinner.setAdapter (dataAdapter);
    	trait13_spinner.setSelection(0);
		
    	trait14_spinner = (Spinner) findViewById(R.id.trait14_spinner);	
    	trait14_spinner.setAdapter (dataAdapter);
    	trait14_spinner.setSelection(0);
		
    	trait15_spinner = (Spinner) findViewById(R.id.trait15_spinner);	
    	trait15_spinner.setAdapter (dataAdapter);
    	trait15_spinner.setSelection(0);
	
		Log.i("create eval", "got real spinners initialized");
		
		trait_units = new ArrayList<String>();
		
        // Select All fields from trait units table and get set to fill the spinners
        cmd = "select * from units_table ";
        crsr = dbh.exec( cmd );
//        Log.i("units ", "executed command " + cmd);
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	trait_units.add("Select Units");
       // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		trait_units.add(cursor.getString(1));
    	}

    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, trait_units);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	
    	
    	trait11_units_spinner = (Spinner) findViewById(R.id.trait11_units_spinner);	
    	trait11_units_spinner.setAdapter (dataAdapter);
    	trait11_units_spinner.setSelection(0);
		
    	trait12_units_spinner = (Spinner) findViewById(R.id.trait12_units_spinner);
    	trait12_units_spinner.setAdapter (dataAdapter);
    	trait12_units_spinner.setSelection(0);

    	trait13_units_spinner = (Spinner) findViewById(R.id.trait13_units_spinner);	
    	trait13_units_spinner.setAdapter (dataAdapter);
    	trait13_units_spinner.setSelection(0);

    	trait14_units_spinner = (Spinner) findViewById(R.id.trait14_units_spinner);	
    	trait14_units_spinner.setAdapter (dataAdapter);
    	trait14_units_spinner.setSelection(0);

    	trait15_units_spinner = (Spinner) findViewById(R.id.trait15_units_spinner);	
    	trait15_units_spinner.setAdapter (dataAdapter);
    	trait15_units_spinner.setSelection(0);

    	Log.i("create eval", "got units spinners initialized");
    	
		// Now set up for the five custom data traits
		custom_evaluation_traits = new ArrayList<String>();
        
        // Select All fields from trait table that are custom data type and get set to fill the spinners
        cmd = "select * from evaluation_trait_table where trait_type = 3";
        crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
    	custom_evaluation_traits.add("Select a Trait");
        // looping through all rows and adding to list
    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
    		custom_evaluation_traits.add(cursor.getString(1));
    	}
        Log.i("createEval ", "below got custom traits");
    	dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, custom_evaluation_traits);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	

    	trait16_spinner = (Spinner) findViewById(R.id.trait16_spinner);	
    	trait16_spinner.setAdapter (dataAdapter);
    	trait16_spinner.setSelection(0);
		
		trait17_spinner = (Spinner) findViewById(R.id.trait17_spinner);
		trait17_spinner.setAdapter (dataAdapter);
		trait17_spinner.setSelection(0);
		
    	trait18_spinner = (Spinner) findViewById(R.id.trait18_spinner);	
    	trait18_spinner.setAdapter (dataAdapter);
    	trait18_spinner.setSelection(0);
		
    	trait19_spinner = (Spinner) findViewById(R.id.trait19_spinner);	
    	trait19_spinner.setAdapter (dataAdapter);
    	trait19_spinner.setSelection(0);
		
    	trait20_spinner = (Spinner) findViewById(R.id.trait20_spinner);	
    	trait20_spinner.setAdapter (dataAdapter);
    	trait20_spinner.setSelection(0);
	
		Log.i("create eval", "got custom spinners initialized");   	
		       	
    	cmd = "select * from last_eval_table";
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        Log.i("create eval", "after select from last eval table");   
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
    	cursor.moveToNext();
    	trait07 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait08 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait09 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait10 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait11 = dbh.getInt(1);
    	trait11_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait12 = dbh.getInt(1);
    	trait12_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait13 = dbh.getInt(1);
    	trait13_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait14 = dbh.getInt(1);
    	trait14_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait15 = dbh.getInt(1);
    	trait15_unitid = dbh.getInt(2);
    	cursor.moveToNext();
    	trait16 = dbh.getInt(1);
    	cursor.moveToNext();	
    	trait17 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait18 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait19 = dbh.getInt(1);
    	cursor.moveToNext();
    	trait20 = dbh.getInt(1);
     	
    	Log.i("after fill", "After fill of the past trait evaluation");
//    	cursor.close();
      	
//    	Log.i("results last ","eval trait01 "+String.valueOf(trait01));
//    	Log.i("results last ","eval trait02 "+String.valueOf(trait02));
//    	Log.i("results last ","eval trait03 "+String.valueOf(trait03));
//    	Log.i("results last ","eval trait04 "+String.valueOf(trait04));
//    	Log.i("results last ","eval trait05 "+String.valueOf(trait05));
//    	Log.i("results last ","eval trait08 "+String.valueOf(trait08));
//    	Log.i("results last ","eval trait06 "+String.valueOf(trait06));
//    	Log.i("results last ","eval trait06 units "+String.valueOf(trait06_unitid));
//    	Log.i("results last ","eval trait07 "+String.valueOf(trait07));
//    	Log.i("results last ","eval trait07 units "+String.valueOf(trait07_unitid));
    	
        	// need to get what position within the current scored_evaluation_traits this trait is 
	        // and set the spinner position to be that position
    	if (trait01!=0){
        	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait01);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait01_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait01 "+ trait01_label);
	        i = scored_evaluation_traits.indexOf(trait01_label);
	        trait01_spinner.setSelection(i);
    	}
    	if (trait02!=0) {    
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait02);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait02_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait02 "+ trait02_label);
	        i = scored_evaluation_traits.indexOf(trait02_label);
	        trait02_spinner.setSelection(i);
    	}
    	if (trait03!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait03);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait03_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait03 "+ trait03_label);
	        i = scored_evaluation_traits.indexOf(trait03_label);
	        trait03_spinner.setSelection(i);
    	}
    	if (trait04!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait04);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait04_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait04 "+ trait04_label);
	        i = scored_evaluation_traits.indexOf(trait04_label);
	        trait04_spinner.setSelection(i);
    	}
    	if (trait05!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait05);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait05_label = dbh.getStr(0);
	        i = scored_evaluation_traits.indexOf(trait05_label);
	        Log.i("set spinner ","eval trait05 "+ trait05_label);
	        trait05_spinner.setSelection(i);
    	}
    	if (trait06!=0){
        	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait06);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait06_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait06 "+ trait06_label);
	        i = scored_evaluation_traits.indexOf(trait06_label);
	        trait06_spinner.setSelection(i);
    	}
    	if (trait07!=0){
        	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait07);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait07_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait07 "+ trait07_label);
	        i = scored_evaluation_traits.indexOf(trait07_label);
	        trait07_spinner.setSelection(i);
    	}
 	   	if (trait08!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait08);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait08_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait08 "+ trait08_label);
	        i = scored_evaluation_traits.indexOf(trait08_label);
	        trait08_spinner.setSelection(i);
    	}
 	   if (trait09!=0){
       	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait09);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait09_label = dbh.getStr(0);
	        Log.i("set spinner ","eval trait09 "+ trait09_label);
	        i = scored_evaluation_traits.indexOf(trait09_label);
	        trait09_spinner.setSelection(i);
   	}
 	  if (trait10!=0){
      	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait10);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait10_label = dbh.getStr(0);
	        i = scored_evaluation_traits.indexOf(trait10_label);
	        trait10_spinner.setSelection(i);
 	  }
// 	   	    Log.i("After trait08", "The selected traits are: ");
//	    	Log.i("After trait08l", "trait01 " + trait01_label);
//	    	Log.i("After trait08", "trait02 " + trait02_label);
//	    	Log.i("After trait08", "trait03 " + trait03_label);
//	    	Log.i("After trait08", "trait04 " + trait04_label);
//	    	Log.i("After trait08", "trait05 " + trait05_label);
//	    	Log.i("After trait08", "trait06 " + trait06_label);
//	    	Log.i("After trait08", "trait07 " + trait07_label);
//	    	Log.i("After trait08", "trait08 " + trait08_label);  
//	    	
//	    	Log.i("number ","eval trait01 "+String.valueOf(trait01));
//	    	Log.i("number ","eval trait02 "+String.valueOf(trait02));
//	    	Log.i("number ","eval trait03 "+String.valueOf(trait03));
//	    	Log.i("number ","eval trait04 "+String.valueOf(trait04));
//	    	Log.i("number ","eval trait05 "+String.valueOf(trait05));
//	    	Log.i("number ","eval trait08 "+String.valueOf(trait08));
//	    	Log.i("number ","eval trait06 "+String.valueOf(trait06));
//	    	Log.i("number ","eval trait06 units "+String.valueOf(trait06_unitid));
//	    	Log.i("number ","eval trait07 "+String.valueOf(trait07));
//	    	Log.i("number ","eval trait07 units "+String.valueOf(trait07_unitid));
	    	    	
    	if (trait11!=0) {
//    		Log.i("inside if ","trait11 string "+String.valueOf(trait11));
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait11);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait11_label = dbh.getStr(0);
//	        Log.i("in if ", "trait11 label " + trait11_label);
	        i = data_evaluation_traits.indexOf(trait11_label);
	        trait11_spinner.setSelection(i);
//	        cursor.close();
	        if (trait11_unitid!=0){
	        // need to also get the units for stored trait11
//	        Log.i("inside 2nd if ","trait11 units id "+String.valueOf(trait11_unitid));
	        cmd = String.format("select units_table.units_name from units_table where " +
	        "id_unitsid='%s'", trait11_unitid);
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait11_units = dbh.getStr(0);
//	        Log.i("in if ", "trait11 units " + trait11_units);
	        i = trait_units.indexOf(trait11_units) ;
	        trait11_units_spinner.setSelection(i); 
	        }
	        else{
//	        	Log.i("units were 0", "Set the trait11 spinner to zero as units required");
	        	trait11_spinner.setSelection(0);
	        }
    	}	    	
    	if (trait12!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait12);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait12_label = dbh.getStr(0);
	        i = data_evaluation_traits.indexOf(trait12_label);
	        trait12_spinner.setSelection(i);
	        if (trait12_unitid!=0){
	     // need to also get the units for stored trait12
	        	cmd = String.format("select units_table.units_name from units_table where " +
	        			"id_unitsid='%s'", trait12_unitid);
	        	crsr = dbh.exec( cmd );
	        	cursor   = ( Cursor ) crsr;
	        	dbh.moveToFirstRecord();
	        	trait12_units = dbh.getStr(0);
	        	i = trait_units.indexOf(trait12_units) ;
	        	trait12_units_spinner.setSelection(i); 
	        	}
	        else{
//	        	Log.i("units were 0", "Set the trait12 spinner to zero as units required");
	        	trait12_spinner.setSelection(0);
	        }
    	}
    	if (trait13!=0) {
//    		Log.i("inside if ","trait13 string "+String.valueOf(trait13));
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait13);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait13_label = dbh.getStr(0);
//	        Log.i("in if ", "trait13 label " + trait13_label);
	        i = data_evaluation_traits.indexOf(trait13_label);
	        trait13_spinner.setSelection(i);
	        if (trait13_unitid!=0){
	        // need to also get the units for stored trait13
//	        Log.i("inside 2nd if ","trait13 units id "+String.valueOf(trait3_unitid));
	        cmd = String.format("select units_table.units_name from units_table where " +
	        "id_unitsid='%s'", trait13_unitid);
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait13_units = dbh.getStr(0);
//	        Log.i("in if ", "trait11 units " + trait13_units);
	        i = trait_units.indexOf(trait13_units) ;
	        trait13_units_spinner.setSelection(i); 
//	        cursor.close();
	        }
	        else{
//	        	Log.i("units were 0", "Set the trait11 spinner to zero as units required");
	        	trait13_spinner.setSelection(0);
	        }
    	}	    	
    	if (trait14!=0) {
//    		Log.i("inside if ","trait14 string "+String.valueOf(trait14));
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait14);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait14_label = dbh.getStr(0);
//	        Log.i("in if ", "trait14 label " + trait14_label);
	        i = data_evaluation_traits.indexOf(trait14_label);
	        trait14_spinner.setSelection(i);
	        if (trait14_unitid!=0){
	        // need to also get the units for stored trait11
//	        Log.i("inside 2nd if ","trait14 units id "+String.valueOf(trait14_unitid));
	        cmd = String.format("select units_table.units_name from units_table where " +
	        "id_unitsid='%s'", trait14_unitid);
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait14_units = dbh.getStr(0);
//	        Log.i("in if ", "trait14 units " + trait14_units);
	        i = trait_units.indexOf(trait14_units) ;
	        trait14_units_spinner.setSelection(i); 
	        }
	        else{
//	        	Log.i("units were 0", "Set the trait14 spinner to zero as units required");
	        	trait14_spinner.setSelection(0);
	        }
    	}	    	

    	if (trait15!=0) {
//    		Log.i("inside if ","trait15 string "+String.valueOf(trait15));
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait15);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait15_label = dbh.getStr(0);
//	        Log.i("in if ", "trait15 label " + trait15_label);
	        i = data_evaluation_traits.indexOf(trait15_label);
	        trait15_spinner.setSelection(i);
	        if (trait15_unitid!=0){
	        // need to also get the units for stored trait11
//	        Log.i("inside 2nd if ","trait15 units id "+String.valueOf(trait15_unitid));
	        cmd = String.format("select units_table.units_name from units_table where " +
	        "id_unitsid='%s'", trait15_unitid);
	        crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait15_units = dbh.getStr(0);
//	        Log.i("in if ", "trait15 units " + trait15_units);
	        i = trait_units.indexOf(trait15_units) ;
	        trait15_units_spinner.setSelection(i); 
	        }
	        else{
//	        	Log.i("units were 0", "Set the trait11 spinner to zero as units required");
	        	trait15_spinner.setSelection(0);
	        }
    	}	 
    	if (trait16!=0){
        	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait16);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait16_label = dbh.getStr(0);
	        i = custom_evaluation_traits.indexOf(trait16_label);
	        trait16_spinner.setSelection(i);
    	}
    	if (trait17!=0){
        	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait17);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait17_label = dbh.getStr(0);
	        i = custom_evaluation_traits.indexOf(trait17_label);
	        trait17_spinner.setSelection(i);
    	}
 	   	if (trait18!=0) {
	        cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait18);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait18_label = dbh.getStr(0);
	        i = custom_evaluation_traits.indexOf(trait18_label);
	        trait18_spinner.setSelection(i);
    	}
 	   if (trait19!=0){
       	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait19);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait19_label = dbh.getStr(0);
	        i = custom_evaluation_traits.indexOf(trait19_label);
	        trait19_spinner.setSelection(i);
   	}
 	  if (trait20!=0){
      	cmd = String.format("select evaluation_trait_table.trait_name from evaluation_trait_table " +
	    			"where id_traitid='%s'", trait20);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait20_label = dbh.getStr(0);
	        i = custom_evaluation_traits.indexOf(trait20_label);
	        trait20_spinner.setSelection(i);
 	  }

//    	    Log.i("Create eval", "The selected traits are: ");
//	    	Log.i("Create eval", "trait01 " + trait01_label);
//	    	Log.i("Create eval", "trait02 " + trait02_label);
//	    	Log.i("Create eval", "trait03 " + trait03_label);
//	    	Log.i("Create eval", "trait04 " + trait04_label);
//	    	Log.i("Create eval", "trait05 " + trait05_label);
//	    	Log.i("Create eval", "trait06 " + trait06_label);
//	    	Log.i("Create eval", "trait07 " + trait07_label);
//	    	Log.i("Create eval", "trait08 " + trait08_label);    	
	}

		 // user clicked the 'back' button
		public void backBtn( View v )
		{
			cursor.close();
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
	    	trait01_spinner.setSelection(0);
	    	trait02_spinner.setSelection(0);
	    	trait03_spinner.setSelection(0);
	    	trait04_spinner.setSelection(0);
	    	trait05_spinner.setSelection(0);
	    	trait06_spinner.setSelection(0);
	    	trait07_spinner.setSelection(0);
	    	trait08_spinner.setSelection(0);
	    	trait09_spinner.setSelection(0);
	    	trait10_spinner.setSelection(0);
	    	trait11_spinner.setSelection(0);
	    	trait12_spinner.setSelection(0);
	    	trait13_spinner.setSelection(0);
	    	trait14_spinner.setSelection(0);
	    	trait15_spinner.setSelection(0);
	    	trait11_units_spinner.setSelection(0);
	    	trait12_units_spinner.setSelection(0);
	    	trait13_units_spinner.setSelection(0);
	    	trait14_units_spinner.setSelection(0);
	    	trait15_units_spinner.setSelection(0);
	    	trait16_spinner.setSelection(0);
	    	trait17_spinner.setSelection(0);
	    	trait18_spinner.setSelection(0);
	    	trait19_spinner.setSelection(0);
	    	trait20_spinner.setSelection(0);
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
	    	trait08_spinner = (Spinner) findViewById(R.id.trait08_spinner);
	    	trait09_spinner = (Spinner) findViewById(R.id.trait09_spinner);
	    	trait10_spinner = (Spinner) findViewById(R.id.trait10_spinner);
	    	trait11_spinner = (Spinner) findViewById(R.id.trait11_spinner);
	    	trait12_spinner = (Spinner) findViewById(R.id.trait12_spinner);
	    	trait13_spinner = (Spinner) findViewById(R.id.trait13_spinner);
	    	trait14_spinner = (Spinner) findViewById(R.id.trait14_spinner);
	    	trait15_spinner = (Spinner) findViewById(R.id.trait15_spinner);
	    	
	    	trait11_units_spinner = (Spinner) findViewById(R.id.trait11_units_spinner);	
	    	trait12_units_spinner = (Spinner) findViewById(R.id.trait12_units_spinner);
	    	trait13_units_spinner = (Spinner) findViewById(R.id.trait13_units_spinner);
	    	trait14_units_spinner = (Spinner) findViewById(R.id.trait14_units_spinner);
	    	trait15_units_spinner = (Spinner) findViewById(R.id.trait15_units_spinner);
	    	
	    	trait16_spinner = (Spinner) findViewById(R.id.trait16_spinner);
	    	trait17_spinner = (Spinner) findViewById(R.id.trait17_spinner);
	    	trait18_spinner = (Spinner) findViewById(R.id.trait18_spinner);
	    	trait19_spinner = (Spinner) findViewById(R.id.trait19_spinner);
	    	trait20_spinner = (Spinner) findViewById(R.id.trait20_spinner);
	    	
	    	// fill the labels with the contents of the various spinners
	    	trait01_label = trait01_spinner.getSelectedItem().toString();
	    	trait02_label = trait02_spinner.getSelectedItem().toString();
	    	trait03_label = trait03_spinner.getSelectedItem().toString();
	    	trait04_label = trait04_spinner.getSelectedItem().toString();
	    	trait05_label = trait05_spinner.getSelectedItem().toString();
	    	trait06_label = trait06_spinner.getSelectedItem().toString();
	    	trait07_label = trait07_spinner.getSelectedItem().toString();
	    	trait08_label = trait08_spinner.getSelectedItem().toString();
	    	trait09_label = trait09_spinner.getSelectedItem().toString();
	    	trait10_label = trait10_spinner.getSelectedItem().toString();
	    	trait11_label = trait11_spinner.getSelectedItem().toString();
	    	trait12_label = trait12_spinner.getSelectedItem().toString();
	    	trait13_label = trait13_spinner.getSelectedItem().toString();
	    	trait14_label = trait14_spinner.getSelectedItem().toString();
	    	trait15_label = trait15_spinner.getSelectedItem().toString();
	    	
	    	trait11_units = trait11_units_spinner.getSelectedItem().toString();
	    	trait12_units = trait12_units_spinner.getSelectedItem().toString();
	    	trait13_units = trait13_units_spinner.getSelectedItem().toString();
	    	trait14_units = trait14_units_spinner.getSelectedItem().toString();
	    	trait15_units = trait15_units_spinner.getSelectedItem().toString();
	    	
	    	trait16_label = trait16_spinner.getSelectedItem().toString();
	    	trait17_label = trait17_spinner.getSelectedItem().toString();
	    	trait18_label = trait18_spinner.getSelectedItem().toString();
	    	trait19_label = trait19_spinner.getSelectedItem().toString();
	    	trait20_label = trait20_spinner.getSelectedItem().toString();
	    	
//	    	Log.i("Create eval", "The selected traits are: ");
//	    	Log.i("Create eval", "trait01 " + trait01_label);
//	    	Log.i("Create eval", "trait02 " + trait02_label);
//	    	Log.i("Create eval", "trait03 " + trait03_label);
//	    	Log.i("Create eval", "trait04 " + trait04_label);
//	    	Log.i("Create eval", "trait05 " + trait05_label);
//	    	Log.i("Create eval", "trait06 " + trait06_label);
//	    	Log.i("Create eval", "trait07 " + trait07_label);
//	    	Log.i("Create eval", "trait08 " + trait08_label);
//	    	Log.i("Create eval", "trait09 " + trait09_label);
//	    	Log.i("Create eval", "trait10 " + trait10_label);
//	    	
//	    	Log.i("Create eval", "trait11 " + trait11_label);	    		    	
//	    	Log.i("Create eval", "trait11 units " + trait11_units);
//	    	Log.i("Create eval", "trait12 " + trait12_label);
//	    	Log.i("Create eval", "trait12 units " + trait12_units);
//	    	
//	    	Log.i("Create eval", "trait16 " + trait16_label);
//	    	Log.i("Create eval", "trait17 " + trait17_label);	    
//	    	Log.i("Create eval", "trait18 " + trait18_label);
//	    	Log.i("Create eval", "trait19 " + trait19_label);
//	    	Log.i("Create eval", "trait20 " + trait20_label);
	    	
	    	// Need to get the id_traitid from the evaluation trait table and store
	    	// that as the actual thing we reference in the evaluate sheep section since it won't change
	    	// from time to time
	    	
	    	if (trait01_label == "Select a Trait") {
	    			trait01 = 0;
	    	}else{
		    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
		    			"where trait_name='%s'", trait01_label);
		    	Log.i("query trait1", cmd);
		    	crsr = dbh.exec( cmd );
		        cursor   = ( Cursor ) crsr;
		        dbh.moveToFirstRecord();
		        trait01 = dbh.getInt(0);
		        Log.i("number ","eval trait01 "+String.valueOf(trait01));
	        }

	    	if (trait02_label == "Select a Trait") {
    			trait02 = 0;
	    	}else
	    	{
	    		cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait02_label);
	    		Log.i("query trait2", cmd);
	    		crsr = dbh.exec( cmd );
	    		cursor   = ( Cursor ) crsr;
	    		dbh.moveToFirstRecord();
	    		trait02 = dbh.getInt(0);
	    		Log.i("number ","eval trait02 "+String.valueOf(trait02));
	    	}
	    	if (trait03_label == "Select a Trait") {
    			trait03 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait03_label);
	    	Log.i("query trait3", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait03 = dbh.getInt(0);
	        Log.i("number ","eval trait03 "+String.valueOf(trait03));
	    	}
	    	if (trait04_label == "Select a Trait") {
    			trait04 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait04_label);
	    	Log.i("query trait4", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait04 = dbh.getInt(0);
	        Log.i("number ","eval trait04 "+String.valueOf(trait04));
	    	}
	    	if (trait05_label == "Select a Trait") {
    			trait05 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait05_label);
	    	Log.i("query trait5", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait05 = dbh.getInt(0);
	        Log.i("number ","eval trait05 "+String.valueOf(trait05));
	    	}
	    	if (trait06_label == "Select a Trait") {
    			trait06 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait06_label);
	    	Log.i("query trait6", cmd);
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
	    	Log.i("query trait7", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait07 = dbh.getInt(0);
	    	}

	    	if (trait08_label == "Select a Trait") {
    			trait08 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait08_label);
	    	Log.i("query trait8", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait08 = dbh.getInt(0);
	    	}
	    	
	    	if (trait09_label == "Select a Trait") {
    			trait09 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait09_label);
	    	Log.i("query trait9", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait09 = dbh.getInt(0);
	    	}
	    	
	    	if (trait10_label == "Select a Trait") {
    			trait10 = 0;
	    	}else
	    	{
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait10_label);
	    	Log.i("query trait10", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait10 = dbh.getInt(0);
	    	}
	    	
	     	if (trait11_label == "Select a Trait") {
    			trait11 = 0;
	    	}else
	    	{
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait11_label);
	        Log.i("query trait11", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait11 = dbh.getInt(0);
	        Log.i("number ","eval trait11 "+String.valueOf(trait11));
	    	}
	    	if (trait12_label == "Select a Trait") {
    			trait12 = 0;
	    	}else
	    	{
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait12_label);
	        Log.i("query trait12", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait12 = dbh.getInt(0);
	    	}
	        
	    	if (trait13_label == "Select a Trait") {
    			trait13 = 0;
	    	}else
	    	{
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait13_label);
	        Log.i("query trait13", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait13 = dbh.getInt(0);
	    	}
	    	
	    	if (trait14_label == "Select a Trait") {
    			trait14 = 0;
	    	}else
	    	{
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait14_label);
	        Log.i("query trait14", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait14 = dbh.getInt(0);
	    	}
	    	
	    	if (trait15_label == "Select a Trait") {
    			trait15 = 0;
	    	}else
	    	{
	        cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait15_label);
	        Log.i("query trait15", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait15 = dbh.getInt(0);
	    	}
	    	
	    	// Now get the units the user selected as well
	        
	    	if (trait11_units == "Select Units") {
	    		trait11_unitid = 0;
	    	}else
	    	{
	        cmd = String.format("select units_table.id_unitsid from units_table " +
	    			"where units_name='%s'", trait11_units);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait11_unitid = dbh.getInt(0);
	        Log.i("number ","units trait11 "+String.valueOf(trait11_unitid));
	    	}
	    	if (trait12_units == "Select Units") {
	    		trait12_unitid = 0;
	    	}else
	    	{
	        cmd = String.format("select units_table.id_unitsid from units_table " +
	    			"where units_name='%s'", trait12_units);
	        Log.i("query trait12", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait12_unitid = dbh.getInt(0);
	    	}
	    	
	    	if (trait13_units == "Select Units") {
	    		trait13_unitid = 0;
	    	}else
	    	{
	        cmd = String.format("select units_table.id_unitsid from units_table " +
	    			"where units_name='%s'", trait13_units);
	        Log.i("query trait13", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait13_unitid = dbh.getInt(0);
	    	}
	    	
	    	if (trait14_units == "Select Units") {
	    		trait14_unitid = 0;
	    	}else
	    	{
	        cmd = String.format("select units_table.id_unitsid from units_table " +
	    			"where units_name='%s'", trait14_units);
//	        Log.i("query trait14", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait14_unitid = dbh.getInt(0);
	    	}
	    	
	    	if (trait15_units == "Select Units") {
	    		trait15_unitid = 0;
	    	}else
	    	{
	        cmd = String.format("select units_table.id_unitsid from units_table " +
	    			"where units_name='%s'", trait15_units);
//	        Log.i("query trait15", cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        dbh.moveToFirstRecord();
	        trait15_unitid = dbh.getInt(0);
	    	}
	    //	Now go get the user defined traits 
	    Log.i("before ","get user evaluation traits");	
	    if (trait16_label == "Select a Trait") {
    			trait16 = 0;
    	}else
    	{
    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
    			"where trait_name='%s'", trait16_label);
    	Log.i("query trait16", cmd);
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
         dbh.moveToFirstRecord();
        trait16 = dbh.getInt(0);
        Log.i("number ","eval trait161 "+String.valueOf(trait16));
        }
	    if (trait17_label == "Select a Trait") {
    			trait17 = 0;
    	}else
    	{
    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
    			"where trait_name='%s'", trait17_label);
    	Log.i("query trait1", cmd);
    	crsr = dbh.exec( cmd );
        cursor   = ( Cursor ) crsr;
        dbh.moveToFirstRecord();
        trait17 = dbh.getInt(0);
        Log.i("number ","eval trait17 "+String.valueOf(trait17));
        }
	    if (trait18_label == "Select a Trait") {
			trait18 = 0;
	    }else
	    {
	    	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
	    			"where trait_name='%s'", trait18_label);
	    	crsr = dbh.exec( cmd );
	    	cursor   = ( Cursor ) crsr;
	    	dbh.moveToFirstRecord();
    	trait18 = dbh.getInt(0);
    }
	if (trait19_label == "Select a Trait") {
			trait19 = 0;
	}else
	{
	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
			"where trait_name='%s'", trait19_label);
	crsr = dbh.exec( cmd );
    cursor   = ( Cursor ) crsr;
    dbh.moveToFirstRecord();
    trait19 = dbh.getInt(0);
    }
	if (trait20_label == "Select a Trait") {
			trait20 = 0;
	}else
	{
	cmd = String.format("select evaluation_trait_table.id_traitid from evaluation_trait_table " +
			"where trait_name='%s'", trait20_label);
//	Log.i("query trait1", cmd);
	crsr = dbh.exec( cmd );
    cursor   = ( Cursor ) crsr;
    dbh.moveToFirstRecord();
    trait20 = dbh.getInt(0);
    }

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
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=6", trait06 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=7", trait07 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=8", trait08 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=9", trait09 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=10", trait10 );
	    	dbh.exec( cmd );
	        cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=11", trait11);
	        dbh.exec( cmd );
	        cmd  = String.format( "update last_eval_table set id_unitsid=%s where id_lastevalid=11", trait11_unitid );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=12", trait12);
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_unitsid=%s where id_lastevalid=12", trait12_unitid );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=13", trait13);
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_unitsid=%s where id_lastevalid=13", trait13_unitid );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=14", trait14);
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_unitsid=%s where id_lastevalid=14", trait14_unitid );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=15", trait15);
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_unitsid=%s where id_lastevalid=15", trait15_unitid );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=16", trait16 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=17", trait17 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=18", trait18 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=19", trait19 );
	    	dbh.exec( cmd );
	    	cmd  = String.format( "update last_eval_table set id_traitid=%s where id_lastevalid=20", trait20 );
	    	dbh.exec( cmd );
	      	
//	    	Log.i("results saved ","eval trait01 "+String.valueOf(trait01));
//	    	Log.i("results saved ","eval trait02 "+String.valueOf(trait02));
//	    	Log.i("results saved ","eval trait03 "+String.valueOf(trait03));
//	    	Log.i("results saved ","eval trait04 "+String.valueOf(trait04));
//	    	Log.i("results saved ","eval trait05 "+String.valueOf(trait05));
//	    	Log.i("results saved ","eval trait08 "+String.valueOf(trait08));
//	    	Log.i("results saved ","eval trait06 "+String.valueOf(trait06));
//	    	Log.i("results saved ","units trait06 "+String.valueOf(trait06_unitid));
//	    	Log.i("results saved ","eval trait07 "+String.valueOf(trait07));
//	    	Log.i("results saved ","units trait07 "+String.valueOf(trait07_unitid));
	    	
	    	// All done need to disable the create create_evaluation_task_btn so we don't do it twice
	       	Button btn2 = (Button) findViewById( R.id.create_evaluation_task_btn );
	    	btn2.setEnabled(false); 
	    	
    }	
}
