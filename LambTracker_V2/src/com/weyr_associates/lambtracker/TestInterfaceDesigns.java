package com.weyr_associates.lambtracker;


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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.content.Context;
import android.view.LayoutInflater;

import android.view.ViewGroup;

import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class TestInterfaceDesigns extends Activity{
		private DatabaseHandler dbh;
		private Cursor 	cursor;
		public RadioGroup radioGroup;
		public Spinner test_dynamic_spinner, trait_spinner;
		List<String> tag_colors, evaluation_traits;
		List<Float> rating_scores;
		ArrayAdapter<String> dataAdapter;
		List<String> scored_evaluation_traits, data_evaluation_traits, user_evaluation_traits;
		private int	nRecs, nRecs2, nRecs3, nRecs4;
		String[] radioBtnText;
		Object crsr;
		List <Integer> scored_trait_numbers, data_trait_numbers, user_trait_numbers, user_trait_number_items;
		ArrayList<Item> data = new ArrayList<Item>(); 
		GridView gridview;
		TextView TV;
	    GridViewAdapter gridviewAdapter;
		String          cmd;
		String	tempLabel, tempText;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.test_interface_designs);
			String dbname = getString(R.string.real_database_file); 
	    	dbh = new DatabaseHandler( this, dbname );	
	    	scored_evaluation_traits = new ArrayList<String>();
	    	data_evaluation_traits = new ArrayList<String>();
	    	user_evaluation_traits = new ArrayList<String>();

	    	scored_trait_numbers = new ArrayList<Integer>();
	    	data_trait_numbers = new ArrayList<Integer>();
	    	user_trait_numbers = new ArrayList<Integer>();
	    	user_trait_number_items = new ArrayList<Integer>();
	    	
	    	//	Set up the scored traits and inflate the layout
	    	cmd = String.format("select evaluation_trait_table.trait_name, evaluation_trait_table.id_traitid " +
		        	"from evaluation_trait_table inner join last_eval_table where " +
	        		" evaluation_trait_table.id_traitid = last_eval_table.id_traitid and evaluation_trait_table.trait_type = 1 ") ;
	    	Log.i("test designs", " cmd is " + cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        nRecs    = cursor.getCount();
	        dbh.moveToFirstRecord();
	        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	        	scored_trait_numbers.add(cursor.getInt(1));
//	        	tempTraitNumber = cursor.getInt(1);
	        	Log.i("test designs", " trait number is " + String.valueOf(cursor.getInt(1)));
		    	scored_evaluation_traits.add(cursor.getString(0));
		    	Log.i("test designs", " trait name is " + cursor.getString(0));
	    	}
	    	cursor.close();    	
//	    	Log.i("test designs", "number of records in cursor is " + String.valueOf(nRecs));
	    	LayoutInflater inflater = getLayoutInflater();	
//	    	Log.i ("test designs", scored_evaluation_traits.get(0));
	    	for( int ii = 0; ii < nRecs; ii++ ){	
	    		Log.i("in for loop" , " ii is " + String.valueOf(ii));
	    		Log.i ("in for loop", " trait name is " + scored_evaluation_traits.get(ii));
    			TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);	
    			Log.i("in for loop", " after TableLayout");
		    	TableRow row = (TableRow)inflater.inflate(R.layout.eval_item_entry, table, false);
		    	tempLabel = scored_evaluation_traits.get(ii);
//		    	Log.i("in for loop", " tempLabel is " + tempLabel);
		    	((TextView)row.findViewById(R.id.rb1_lbl)).setText(tempLabel);
//		    	Log.i("in for loop", " after set text view");
		    	table.addView(row);
	    	}
	    	
	    	//	Set up the data traits and inflate the layout
	    	cmd = String.format("select evaluation_trait_table.trait_name, evaluation_trait_table.id_traitid " +
		        	"from evaluation_trait_table inner join last_eval_table where " +
	        		" evaluation_trait_table.id_traitid = last_eval_table.id_traitid and evaluation_trait_table.trait_type = 2 ") ;
	    	Log.i("test designs", " cmd is " + cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        nRecs2    = cursor.getCount();
	        dbh.moveToFirstRecord();
	        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	        	data_trait_numbers.add(cursor.getInt(1));
//	        	tempTraitNumber = cursor.getInt(1);
	        	Log.i("test designs", " trait number is " + String.valueOf(cursor.getInt(1)));
		    	data_evaluation_traits.add(cursor.getString(0));
		    	Log.i("test designs", " trait name is " + cursor.getString(0));
	    	}
	    	cursor.close();    	
//	    	Log.i("test designs", "number of records in cursor is " + String.valueOf(nRecs));
	    	inflater = getLayoutInflater();	
//	    	Log.i ("test designs", scored_evaluation_traits.get(0));
	    	for( int ii = 0; ii < nRecs2; ii++ ){	
	    		Log.i("in for loop" , " ii is " + String.valueOf(ii));
	    		Log.i ("in for loop", " trait name is " + data_evaluation_traits.get(ii));
    			TableLayout table = (TableLayout) findViewById(R.id.TableLayout02);	
    			Log.i("in for loop", " after TableLayout");
		    	TableRow row = (TableRow)inflater.inflate(R.layout.eval_data_item_entry, table, false);
		    	tempLabel = data_evaluation_traits.get(ii);
//		    	Log.i("in for loop", " tempLabel is " + tempLabel);
		    	((TextView)row.findViewById(R.id.data_lbl)).setText(tempLabel);
//		    	Log.i("in for loop", " after set text view");
		    	table.addView(row);
	    	}
	        
	    	// Set up the user traits
	  
	    	cmd = String.format("select evaluation_trait_table.trait_name, custom_evaluation_name_table.id_custom_eval_nameid , " +
		        	"custom_evaluation_name_table.custom_eval_number " +
	    			"from evaluation_trait_table inner join last_eval_table on " +
		        	" evaluation_trait_table.id_traitid = last_eval_table.id_traitid" +
		        	" inner join custom_evaluation_name_table on evaluation_trait_table.trait_name = " +
	        		" custom_evaluation_name_table.custom_eval_name where evaluation_trait_table.trait_type = 3 ") ;
	    	Log.i("test designs", " cmd is " + cmd);
	    	crsr = dbh.exec( cmd );
	        cursor   = ( Cursor ) crsr;
	        nRecs3    = cursor.getCount();
	        dbh.moveToFirstRecord();
	        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	        	user_evaluation_traits.add(cursor.getString(0));
		    	Log.i("test designs", " trait name is " + cursor.getString(0));
		    	user_trait_numbers.add(cursor.getInt(1));
	        	Log.i("test designs", " trait id number is " + String.valueOf(cursor.getInt(1)));
		    	user_trait_number_items.add(cursor.getInt(2));
		    	Log.i("test designs", " number of items for this trait is " + String.valueOf(cursor.getInt(2)));		    	
	    	}
	    	cursor.close();  
	    	
//	    	Log.i("test designs", "number of records in cursor is " + String.valueOf(nRecs));
	    	inflater = getLayoutInflater();	
//	    	Log.i ("test designs", scored_evaluation_traits.get(0));
	    	for( int ii = 0; ii < nRecs3; ii++ ){	
	    		Log.i("in for loop" , " ii is " + String.valueOf(ii));
	    		Log.i ("in for loop", " user trait number is " + String.valueOf(user_trait_numbers.get(ii)));
	    		Log.i ("in for loop", " trait name is " + user_evaluation_traits.get(ii));
	    		Log.i ("in for loop", " number of trait entries is " + String.valueOf(user_trait_number_items.get(ii)));
    			TableLayout table = (TableLayout) findViewById(R.id.TableLayout03);	
    			Log.i("in for loop", " after TableLayout");		    	
		    	//	Get the text for the buttons
		    	tempText = String.valueOf(user_trait_numbers.get(ii));
		    	Log.i("in for loop", "trait numbers is " + tempText);
		    	cmd = String.format("select custom_evaluation_traits_table.custom_evaluation_item " +
		    			" from custom_evaluation_traits_table " +
		    			" where custom_evaluation_traits_table.custom_evaluation_id = '%s' "+
		    			" order by custom_evaluation_traits_table.custom_evaluation_order ASC ", tempText);
		    	Log.i("test designs", " cmd is " + cmd);
		    	crsr = dbh.exec( cmd );
		        cursor   = ( Cursor ) crsr;
		        nRecs4    = cursor.getCount();
		        dbh.moveToFirstRecord();		        
		        ArrayList buttons = new ArrayList();
		        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
		        	buttons.add (cursor.getString(0));
			    	Log.i("test designs", " radio button text is " + cursor.getString(0));
		    	}
		        TableRow row = (TableRow)inflater.inflate(R.layout.eval_custom_item, table, false);
		        
		        radioBtnText = (String[]) buttons.toArray(new String [buttons.size()]);
		    	cursor.close();  
		    	// Build the radio buttons here
		    	radioGroup = ((RadioGroup)row.findViewById(R.id.radioGroup1));
		    	addRadioButtons(user_trait_number_items.get(ii), radioBtnText);
		    	table.addView(row);
	    	}
	    	
	    	test_dynamic_spinner = (Spinner) findViewById(R.id.test_dynamic_spinner);
//	    	Log.i("testinterface", "in onCreate below test spinner");
	    	tag_colors = new ArrayList<String>();
	         
	        // Select All fields from tag colors to build the spinner
	        cmd = "select * from tag_colors_table";
	        Object crsr = dbh.exec( cmd );  
	        cursor   = ( Cursor ) crsr;
	    	dbh.moveToFirstRecord();
	    	tag_colors.add("Select a Color");
	    	// Log.i("testinterface", "in onCreate below got tag color table");
	        // looping through all rows and adding to list
	    	for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
	    		tag_colors.add(cursor.getString(2));
	    	}
	    	cursor.close();
	        Log.i("testinterface", "below if loop");
		        // Creating adapter for spinner
		        dataAdapter = new ArrayAdapter<String>(this,
		                android.R.layout.simple_spinner_item, tag_colors);
	
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			test_dynamic_spinner.setAdapter (dataAdapter);
			test_dynamic_spinner.setSelection(0);
//			Log.i("Activity", "In Spinner");
			test_dynamic_spinner.setOnItemSelectedListener(new SpinnerActivity());	
			
			
			}
	
		  // user clicked the 'saveScores' button
	    public void saveScores( View v )
	    {
	    		RatingBar ratingBar01;
	    		Float realScore;
	    		
	    		rating_scores = new ArrayList<Float>();
	    		TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
	    		for( int ii = 0; ii < nRecs; ii++ ){	
	    			TableRow row1= (TableRow)table.getChildAt(ii);
	    			ratingBar01 = (RatingBar) row1.getChildAt(1);
	    			rating_scores.add(ratingBar01.getRating());
	    			Log.i("RatingBar01 ", String.valueOf(ratingBar01.getRating()));    	
	    		}
	    			    		
	    		table = (TableLayout) findViewById(R.id.TableLayout02);
	    		for( int ii = 0; ii < nRecs2; ii++ ){	
	    			TableRow row1= (TableRow)table.getChildAt(ii);
	    			TV = (EditText ) row1.getChildAt(1);	    			
	    			realScore = Float.valueOf(TV.getText().toString());
	    			Log.i("realscores ", String.valueOf(realScore)); 
	    		}
	    		
//	    		RadioGroup rg=(RadioGroup)findViewById(R.id.youradio);
//	    		  String radiovalue=  (RadioButton)this.findViewById(rg.getCheckedRadioButtonId())).getText().toString();

//	    		int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
//	    		View radioButton = radioButtonGroup.findViewById(radioButtonID);
//	    		int idx = radioButtonGroup.indexOfChild(radioButton);
	    		
	    		
	    		table = (TableLayout) findViewById(R.id.TableLayout03);
	    		for( int ii = 0; ii < nRecs3; ii++ ){	
	    			TableRow row1= (TableRow)table.getChildAt(ii);
//	    			int radioButtonID = radioGroup.getCheckedRadioButtonId();
//	    			View radioButton = radioGroup.findViewById(radioButtonID);
//	    			int idx = radioGroup.indexOfChild(radioButton);
////	    			TV = (EditText ) row1.getChildAt(1);	    			
////	    			realScore = Float.valueOf(TV.getText().toString());
//	    			Log.i("radio button position ", String.valueOf(idx)); 
	    		}
	    }
	    private void addRadioButtons(int numButtons, String[] radioBtnText) {
	    	  int i;

	    	  for(i = 0; i < numButtons; i++){
	    	    //instantiate...
	    	    RadioButton radioBtn = new RadioButton(this);

	    	    //set the values that you would otherwise hardcode in the xml...
	    	  	radioBtn.setLayoutParams 
	    	      (new LayoutParams 
	    	      (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	    	    //label the button...
	    	  	radioBtn.setText(radioBtnText[i]);
	    	  	radioBtn.setId(i);

	    	    //add it to the group.
	    	    radioGroup.addView(radioBtn, i);
	    	  }
	    	}        
	private class SpinnerActivity extends Activity implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

			Log.i("Activity", "In Spinner activity before the case statement");
			String teststring;
			teststring = String.valueOf (parent.getSelectedItemPosition());
			// Log.i("Spinner", "Position = "+teststring);

				switch (parent.getSelectedItemPosition()){		
				case 0:
			        // Don't want to do anything until something is selected so just break at position zero
					break;
			    case 1:
			    	teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			    	break;
				case 2:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 3:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 4:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 5:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 6:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 7:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 8:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 9:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 10:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 11:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 12:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 13:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 14:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 15:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 16:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 17:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 18:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
			        break;
				case 19:
					teststring = String.valueOf (parent.getSelectedItemPosition());
			    	teststring = test_dynamic_spinner.getSelectedItem().toString();
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


    // Set the Data Adapter
    private void setDataAdapter()
    {
        gridviewAdapter = new GridViewAdapter(getApplicationContext(), R.layout.eval_item_entry, data);
        gridview.setAdapter(gridviewAdapter);
    }

    }
    
