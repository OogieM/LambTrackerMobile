package com.weyr_associates.lambtracker;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.LightingColorFilter;
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
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
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
import android.view.inputmethod.InputMethodManager;

import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class TestInterfaceDesigns extends Activity{
		private DatabaseHandler dbh;
		public Spinner tag_type_spinner, tag_location_spinner, tag_color_spinner ;
		private int			    recNo;
		public Button btn;
		public List<String> tag_types, tag_locations;

		private Cursor 	cursor, cursor5;
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
		public int 		thissheep_id;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.test_interface_designs);
			String dbname = getString(R.string.real_database_file); 
	    	dbh = new DatabaseHandler( this, dbname );	

			
			// take note set up
//			final Context context = this;
			
	     	// Fill the Tag Type Spinner
	     	tag_type_spinner = (Spinner) findViewById(R.id.tag_type_spinner);
	    	tag_types = new ArrayList<String>();      	
	    	
	    	// Select All fields from id types to build the spinner
	        cmd = "select * from id_type_table";
	        crsr = dbh.exec( cmd );  
	        cursor5   = ( Cursor ) crsr;
	    	dbh.moveToFirstRecord();
	    	tag_types.add("Select a Type");
	         // looping through all rows and adding to list
	    	for (cursor5.moveToFirst(); !cursor5.isAfterLast(); cursor5.moveToNext()){
	    		tag_types.add(cursor5.getString(1));
	    	}
	    	
	    	// Creating adapter for spinner
	    	dataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, tag_types);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			tag_type_spinner.setAdapter (dataAdapter);
			//	set initial tag type to look for to be federal tag
			tag_type_spinner.setSelection(1);	

	       	// make the alert button normal and disabled
	    	btn = (Button) findViewById( R.id.alert_btn );
	    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFF000000));
	    	btn.setEnabled(false);  
	 // TODO   	
	       	//	Disable the Next Record and Prev. Record button until we have multiple records
	    	btn = (Button) findViewById( R.id.next_rec_btn );
	    	btn.setEnabled(false); 
	    	btn = (Button) findViewById( R.id.prev_rec_btn );
	    	btn.setEnabled(false);
	    	
			//	make the scan eid button red
			btn = (Button) findViewById( R.id.scan_eid_btn );
			btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));

			}
	
		  // user clicked the 'saveScores' button
	    public void saveScores( View v )
	    {
	    		RatingBar ratingBar01;
	    		Float realScore;
	    		// 	get the rating scores
	    		rating_scores = new ArrayList<Float>();
	    		TableLayout table = (TableLayout) findViewById(R.id.TableLayout01);
	    		for( int ii = 0; ii < nRecs; ii++ ){	
	    			TableRow row1= (TableRow)table.getChildAt(ii);
	    			ratingBar01 = (RatingBar) row1.getChildAt(1);
	    			rating_scores.add(ratingBar01.getRating());
	    			Log.i("RatingBar01 ", String.valueOf(ratingBar01.getRating()));    	
	    		}
	    		//	get the real data values  		
	    		table = (TableLayout) findViewById(R.id.TableLayout02);
	    		for( int ii = 0; ii < nRecs2; ii++ ){	
	    			TableRow row1= (TableRow)table.getChildAt(ii);
	    			TV = (EditText ) row1.getChildAt(1);	    			
	    			realScore = Float.valueOf(TV.getText().toString());
	    			Log.i("realscores ", String.valueOf(realScore)); 
	    		}
	    		//	get the radiogroup button selected
	    		
//	    		RadioGroup rg=(RadioGroup)findViewById(R.id.youradio);
//	    		  String radiovalue=  (RadioButton)this.findViewById(rg.getCheckedRadioButtonId())).getText().toString();

//	    		int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
//	    		View radioButton = radioButtonGroup.findViewById(radioButtonID);
//	    		int idx = radioButtonGroup.indexOfChild(radioButton);
	    		
//	    		LinearLayout root = (LinearLayout) findViewById(R.id.linearLayout1);
//	    		private void loopQuestions(ViewGroup parent) {
//	    	        for(int i = 0; i < parent.getChildCount(); i++) {
//	    	            View child = parent.getChildAt(i);
//	    	            if(child instanceof RadioGroup ) {
//	    	                //Support for RadioGroups
//	    	                RadioGroup radio = (RadioGroup)child;
//	    	                storeAnswer(radio.getId(), radio.getCheckedRadioButtonId());
//	    	            }	
	    		
	    		table = (TableLayout) findViewById(R.id.TableLayout03);
	    		for( int ii = 0; ii < nRecs3; ii++ ){	
	    			TableRow row1= (TableRow)table.getChildAt(ii);
	    			Log.i("radio button " , " index of row is " + String.valueOf(row1));
	    			View child = table.getChildAt(ii);
	    			int idx = row1.indexOfChild(child);
	    			Log.i("radio button " , " index of child is " + String.valueOf(idx));
	    			RadioGroup radio = (RadioGroup)child;
	    			Log.i("radio button " , " checked button is " + String.valueOf(radio.getCheckedRadioButtonId()));
	                
//	    			int index = row1.indexOfChild(findViewById(radioGroup1.getCheckedRadioButtonId()));
	    				    	            
//	    			int radioButtonID = radioGroup.getCheckedRadioButtonId();
//	    			View radioButton = radioGroup.findViewById(radioButtonID);
//	    			int idx = radioGroup.indexOfChild(radioButton);
////	    			TV = (EditText ) row1.getChildAt(1);	    			
////	    			realScore = Float.valueOf(TV.getText().toString());
//	    			Log.i("radio button position ", String.valueOf(idx)); 
	    		}
	    		
	    		// testing for the note button
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


		public void lookForSheep (View v){

			Object crsr, crsr2, crsr3, crsr4;
			Boolean exists;
			TextView TV;
			ListView notelist = (ListView) findViewById(R.id.list2);
	        exists = true;
	     // Hide the keyboard when you click the button
	    	InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
	    	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	    	
	        TV = (TextView) findViewById( R.id.inputText );
	    	String	tag_num = TV.getText().toString();
	    	
	        Log.i("LookForSheep", " got to lookForSheep with Tag Number of " + tag_num);
	        Log.i("LookForSheep", " got to lookForSheep with Tag type of " + tag_type_spinner.getSelectedItemPosition());
	        exists = tableExists("sheep_table");
	        if (exists){
	        	if( tag_num != null && tag_num.length() > 0 ){
//	        		Get the sheep id from the id table for this tag number and selected tag type
		        	cmd = String.format( "select sheep_id from id_info_table where tag_number='%s' "+
		        			"and id_info_table.tag_type='%s' and id_info_table.tag_date_off is null", tag_num , tag_type_spinner.getSelectedItemPosition());  	        	
		        	Log.i("LookForSheep", " command is  " + cmd);
		        	crsr = dbh.exec( cmd );
		        	cursor   = ( Cursor ) crsr; 
		        	recNo    = 1;
					nRecs    = cursor.getCount();
					Log.i("LookUpSheep", " nRecs = "+ String.valueOf(nRecs));
//					colNames = cursor.getColumnNames();
//		    		startManagingCursor(cursor);
		        	dbh.moveToFirstRecord();
		        	if( dbh.getSize() == 0 )
			    		{ // no sheep with that tag in the database so clear out and return
			    		clearBtn( v );
			    		TV = (TextView) findViewById( R.id.sheepnameText );
			        	TV.setText( "Cannot find this sheep." );
			        	return;
			    		}
		        	// TODO add the next record previous record stuff in here
		        	if (nRecs >1){
		        		//	Have multiple sheep with this tag so enable next button
		            	btn = (Button) findViewById( R.id.next_rec_btn );
		            	btn.setEnabled(true);       		
		        	}
//		        	Log.i("LookForSheep", "This sheep is record " + String.valueOf(thissheep_id));	        	
		        	//	We need to call the format the record method
		        	formatSheepRecord(v);
					}else{
		        	return;
		        }
		        Log.i("lookForSheep", " out of the if statement");
	        	}
	    		else {
	    			clearBtn( null );
	            	TV = (TextView) findViewById( R.id.sheepnameText );
	                TV.setText( "Sheep Database does not exist." );                
	        	}
		}
		public void formatSheepRecord (View v){
			Object crsr, crsr2, crsr3, crsr4;
			TextView TV;
			ListView notelist = (ListView) findViewById(R.id.list2);
			
			thissheep_id = cursor.getInt(0);	        	
			Log.i("format record", "This sheep is record " + String.valueOf(thissheep_id));	        	
			
//			Log.i("format record", " recNo = "+ String.valueOf(recNo));
			cmd = String.format( "select sheep_table.sheep_name, sheep_table.sheep_id, id_type_table.idtype_name, " +
					"tag_colors_table.tag_color_name, id_info_table.tag_number, id_location_table.id_location_abbrev, " +
					"id_info_table.id_infoid as _id, id_info_table.tag_date_off, sheep_table.alert01,  " +
					"sheep_table.sire_id, sheep_table.dam_id, sheep_table.birth_date, birth_type_table.birth_type," +
					"sheep_sex_table.sex_name " +
					"from sheep_table inner join id_info_table on sheep_table.sheep_id = id_info_table.sheep_id " +
					"inner join birth_type_table on id_birthtypeid = sheep_table.birth_type " +
					"inner join sheep_sex_table on sheep_sex_table.sex_sheepid = sheep_table.sex " +
					"left outer join tag_colors_table on id_info_table.tag_color_male = tag_colors_table.tag_colorsid " +
					"left outer join id_location_table on id_info_table.tag_location = id_location_table.id_locationid " +
					"inner join id_type_table on id_info_table.tag_type = id_type_table.id_typeid " +
					"where id_info_table.sheep_id ='%s' and id_info_table.tag_date_off is null order by idtype_name asc", thissheep_id);

			crsr = dbh.exec( cmd ); 	    		
			cursor5   = ( Cursor ) crsr; 
			cursor5.moveToFirst();				
			TV = (TextView) findViewById( R.id.sheepnameText );
		    TV.setText (dbh.getStr(0));
//		    TV = (TextView) findViewById( R.id.birth_date );
//		    TV.setText (dbh.getStr(11));
//		    TV = (TextView) findViewById( R.id.birth_type );
//		    TV.setText (dbh.getStr(12));
//		    TV = (TextView) findViewById( R.id.sheep_sex );
//		    TV.setText (dbh.getStr(13));
//		    
//		    alert_text = dbh.getStr(8);
//			
//		    //	Get the sire and dam id numbers
//		    thissire_id = dbh.getInt(9);
//		    Log.i("format record", " Sire is " + String.valueOf(thissire_id));
//		    thisdam_id = dbh.getInt(10);
//		    Log.i("format record", " Dam is " + String.valueOf(thisdam_id));
//		    
//		    //	Go get the sire name
//		    if (thissire_id != 0){
//		        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thissire_id);
//		        Log.i("format record", " cmd is " + cmd);		        
//		        crsr2 = dbh.exec( cmd);
//		        Log.i("format record", " after second db lookup");
//		        cursor2   = ( Cursor ) crsr2; 
////				startManagingCursor(cursor2);
//				cursor2.moveToFirst();
//				TV = (TextView) findViewById( R.id.sireName );
//				thissire_name = dbh.getStr(0);
//				TV.setText (thissire_name);	 
//				Log.i("format record", " Sire is " + thissire_name);
//		        Log.i("format record", " Sire is " + String.valueOf(thissire_id));
//		    }
//		    if(thisdam_id != 0){
//		        cmd = String.format( "select sheep_table.sheep_name from sheep_table where sheep_table.sheep_id = '%s'", thisdam_id);
//		        crsr3 = dbh.exec( cmd);
//		        cursor3   = ( Cursor ) crsr3; 
////				startManagingCursor(cursor3);
//				cursor3.moveToFirst();
//				TV = (TextView) findViewById( R.id.damName );
//				thisdam_name = dbh.getStr(0);
//				TV.setText (thisdam_name);	
//				Log.i("format record", " Dam is " + thisdam_name);
//		        Log.i("format record", " Dam is " + String.valueOf(thisdam_id));
//		    }    		
//			Log.i("FormatRecord", " before formatting results");
//			
//			//	Get set up to try to use the CursorAdapter to display all the tag data
//			//	Select only the columns I need for the tag display section
//		    String[] fromColumns = new String[ ]{ "tag_number", "tag_color_name", "id_location_abbrev", "idtype_name"};
//			Log.i("FormatRecord", "after setting string array fromColumns");
//			//	Set the views for each column for each line. A tag takes up 1 line on the screen
//		    int[] toViews = new int[] { R.id.tag_number, R.id.tag_color_name, R.id.id_location_abbrev, R.id.idtype_name};
//		    Log.i("FormatRecord", "after setting string array toViews");
//		    myadapter = new SimpleCursorAdapter(this, R.layout.list_entry, cursor5 ,fromColumns, toViews, 0);
//		    Log.i("FormatRecord", "after setting myadapter");
//		    setListAdapter(myadapter);
//		    Log.i("FormatRecord", "after setting list adapter");
//
//			// Now we need to check and see if there is an alert for this sheep
////		   	Log.i("Alert Text is " , alert_text);
////			Now to test of the sheep has an alert and if so then display the alert & set the alerts button to red
//			if (alert_text != null && !alert_text.isEmpty() && !alert_text.trim().isEmpty()){
//		       	// make the alert button red
//		    	Button btn = (Button) findViewById( R.id.alert_btn );
//		    	btn.getBackground().setColorFilter(new LightingColorFilter(0xFF000000, 0xFFCC0000));
//		    	btn.setEnabled(true); 
//		    	//	testing whether I can put up an alert box here without issues
//		    	showAlert(v);
//			}
//			//	Now go get all the notes for this sheep and format them
//			cmd = String.format( "select sheep_note_table.id_noteid as _id, sheep_note_table.note_date, sheep_note_table.note_time, " +
//					"sheep_note_table.note_text, predefined_notes_table.predefined_note_text " +
//					" from sheep_note_table left join predefined_notes_table " +
//					"on predefined_notes_table.id_predefinednotesid = sheep_note_table.id_predefinednotesid01" +
//					" where sheep_id='%s' "+
//					"order by note_date desc ", thissheep_id);  	        	
//			Log.i("format record", " command is  " + cmd);
//			crsr4 = dbh.exec( cmd );
//			cursor4   = ( Cursor ) crsr4; 
////			startManagingCursor(cursor4);
//			nRecs4    = cursor4.getCount();
//			Log.i("lookForSheep", " nRecs4 is " + String.valueOf(nRecs4));
//			cursor4.moveToFirst();	
//			if (nRecs4 > 0) {
//		    	// format the note records
//				//	Select only the columns I need for the note display section
//		    	String[] fromColumns2 = new String[ ]{ "note_date", "note_time", "note_text", "predefined_note_text"};
//				Log.i("LookForSheep", "after setting string array fromColumns for notes");
//				//	Set the views for each column for each line. A tag takes up 1 line on the screen
//				int[] toViews2 = new int[] { R.id.note_date, R.id.note_time, R.id.note_text, R.id.predefined_note_text};
//		        Log.i("LookForSheep", "after setting string array toViews for notes");
//		        myadapter2 = new SimpleCursorAdapter(this, R.layout.note_entry, cursor4 ,fromColumns2, toViews2, 0);
//		        Log.i("LookForSheep", "after setting myadapter to show notes");
//		        notelist.setAdapter(myadapter2);
//		        Log.i("LookForSheep", "after setting list adapter to show notes");			
//			}   
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
	public boolean tableExists (String table){
		try {
	        dbh.exec("select * from "+ table);   
	        return true;
		} catch (SQLiteException e) {
			return false;
	        		}
	        	}
    public void doNote( View v )
    {	 
    	Log.i("testing interface ", "In doNote method");
    	String testing;
    	testing = Utilities.takeNote(v, thissheep_id, this);
    	Log.i("testing interface ", "take note string is " + testing);
    }
    public void clearBtn( View v )
    {
	thissheep_id = 0;
	TextView TV ;
	TV = (TextView) findViewById( R.id.inputText );
	TV.setText( "" );		
	TV = (TextView) findViewById( R.id.sheepnameText );
	TV.setText( "" );

    }
    }
    
