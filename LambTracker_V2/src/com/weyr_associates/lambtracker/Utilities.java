package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.LightingColorFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.app.ListActivity;


/**
 * Write a description of class Utilities here.
 * 
 * @author ww added to by em
 * @version 2014-05-11
 */
public class Utilities 
{
    public static int    JGREG        = 15 + 31*(10+12*1582);   // 15 October 1582
    public static double SECS_PER_DAY = 86400.0;
    public static double HALFSECOND   = 0.5;

    public static Spinner predefined_note_spinner01;
	public static Spinner predefined_note_spinner02;
	public static Spinner predefined_note_spinner03;
	public static Spinner predefined_note_spinner04, predefined_note_spinner05;
	public static List<String> predefined_notes;
	public int 		thissheep_id;
	private static DatabaseHandler dbh;
	public static Cursor 	cursor5;
	public static Object	crsr;
	static ArrayAdapter<String> dataAdapter;
	static String     	cmd;
    /**
     * Constructor for objects of class Utilities
     */
    public Utilities()
    {

    }

public static double toJulian( int[] ymd )
    {
    int year       = ymd[0]; // yyyy
    int month      = ymd[1]; // jan=1, feb=2,...
    int day        = ymd[2]; // 1 - 31
    int julianYear = year;
    
    if( year < 0 )  // 10 BCE => -10
        julianYear++;
    
    int julianMonth = month;
    
    if( month > 2 )
        julianMonth++;
    
    else
        {
        julianYear--;
        julianMonth += 13;
        }

    double julian = (java.lang.Math.floor(365.25 * julianYear)
                    + java.lang.Math.floor(30.6001*julianMonth) + day + 1720995.0);
    
    // Gregorian Calendar adopted Oct. 15, 1582 (2299161)
    if( day + 31 * (month + 12 * year) >= JGREG )
        {
        // change over to Gregorian calendar
        int ja  = (int)(0.01 * julianYear);
        julian += 2 - ja + (0.25 * ja);
        }
    
    return java.lang.Math.floor(julian) - 0.5;    // start of civil day 0h UTC
    }

public double toJulianWithTime( int[] ymdhms )
   {
   double jd  = toJulian( ymdhms );
   int    hh  = ymdhms[3],
          mm  = ymdhms[4],
          ss  = ymdhms[5];
   return jd + (hh * 3600 + mm * 60 + ss) / SECS_PER_DAY;
   }


 /**
* Converts a Julian day to a calendar date
* ref :
* Numerical Recipes in C, 2nd ed., Cambridge University Press 1992
*/
public int[] fromJulian( double injulian )
    {
    int    jalpha, ja, jb, jc, jd, je, year,month, day;
    double julian = injulian + (HALFSECOND / 86400.0);
    ja = (int) (julian + 0.5);
    
    if( ja >= JGREG )
        {    
        jalpha = (int) (((ja - 1867216) - 0.25) / 36524.25);
        ja = ja + 1 + jalpha - jalpha / 4;
        }

    jb    = ja + 1524;
    jc    = (int) (6680.0 + ((jb - 2439870) - 122.1) / 365.25);
    jd    = 365 * jc + jc / 4;
    je    = (int) ((jb - jd) / 30.6001);
    day   = jb - jd - (int) (30.6001 * je);
    month = je - 1;
    
    if( month > 12 )
        month = month - 12;
    
    year = jc - 4715;
    
    if( month > 2 )
        year--;
    
    if( year <= 0 )
        year--;

    return new int[] {year, month, day};
    }

public int[] fromJulianWithTime( double julian )
    {
    double jd = julian + (HALFSECOND / 86400.0);
    jd = Math.floor( julian + 0.5 ) - 0.5;
    double time = (julian + (HALFSECOND / 86400.0)) - jd;
    int[]  date = fromJulian( jd );
    int    secs = (int) Math.floor( SECS_PER_DAY * time );
    int    hrs  = secs / 3600;
    secs -= hrs * 3600;
    int mins = secs / 60;
    secs -= mins * 60;
    return new int[] { date[0], date[1], date[2], hrs, mins, secs };
    }


public static String takeNote( View v, final Integer thissheep_id, final Context context )
{	 
    String 	dbfile = context.getString (R.string.real_database_file) ;
    Log.i("takeNote", " after get database file in Utilities");
	dbh = new DatabaseHandler( context, dbfile );

//	
	Log.i ("takeNote", " in beginning of take a note the sheep id is " + String.valueOf(thissheep_id));
	if (thissheep_id == 0) {
		Log.i ("takeNote", " no sheep selected " + String.valueOf(thissheep_id));
		return "no sheep";
	}
	else {
		//	First fill the predefined note spinner with possibilities
    	predefined_notes = new ArrayList<String>();
		predefined_notes.add("Select a Predefined Note");
		Log.i ("takeNote", " after adding Select a Predefined Note");
    	// Select All fields from predefined_notes_table to build the spinner
        cmd = "select * from predefined_notes_table";
        Log.i ("takeNote", " cmd is " + cmd);
        crsr = dbh.exec( cmd ); 
        Log.i ("takeNote", " after executing the dbh command " + cmd);
        cursor5   = ( Cursor ) crsr;
    	dbh.moveToFirstRecord();
         // looping through all rows and adding to list
    	for (cursor5.moveToFirst(); !cursor5.isAfterLast(); cursor5.moveToNext()){
    		predefined_notes.add(cursor5.getString(1));
    		Log.i ("takeNote", " in for loop predefined note id is " + String.valueOf(cursor5.getString(1)));
    	}
    	cursor5.close();    
    	Log.i ("takeNote", " after set the predefined note spinner ");
    	Log.i ("takeNote", " this sheep is " + String.valueOf(thissheep_id));
    	//Implement take a note stuff here
//		Log.i ("takeNote", " got a sheep, need to get a note to add");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
//		Log.i ("takeNote", " after getting new alertdialogbuilder");
		
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.note_prompt, null);
//		Log.i ("takeNote", " after inflating layout");	

		// set view note_prompt to alertdialog builder
		alertDialogBuilder.setView(promptsView);
		Log.i ("takeNote", " after setting view");
	   	// Creating adapter for predefined notes spinners
    	dataAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, predefined_notes);
    	dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	predefined_note_spinner01 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner01);
    	predefined_note_spinner01.setAdapter (dataAdapter);
		predefined_note_spinner01.setSelection(0);
		
    	predefined_note_spinner02 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner02);
    	predefined_note_spinner02.setAdapter (dataAdapter);
		predefined_note_spinner02.setSelection(0);

    	predefined_note_spinner03 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner03);
    	predefined_note_spinner03.setAdapter (dataAdapter);
		predefined_note_spinner03.setSelection(0);

    	predefined_note_spinner04 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner04);
    	predefined_note_spinner04.setAdapter (dataAdapter);
		predefined_note_spinner04.setSelection(0);

    	predefined_note_spinner05 = (Spinner) promptsView.findViewById(R.id.predefined_note_spinner05);
    	predefined_note_spinner05.setAdapter (dataAdapter);
		predefined_note_spinner05.setSelection(0);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.note_text);

		// set dialog message
		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Save Note",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				// get user input and set it to result
				// edit text
				String note_text = dbh.fixApostrophes(String.valueOf(userInput.getText()));
				Log.i("update notes ", "note text is " + note_text);
				//	Get id_predefinednotesid from a spinner here 
				int predefined_note01 = predefined_note_spinner01.getSelectedItemPosition();
				int predefined_note02 = predefined_note_spinner02.getSelectedItemPosition();
				int predefined_note03 = predefined_note_spinner03.getSelectedItemPosition();
				int predefined_note04 = predefined_note_spinner04.getSelectedItemPosition();
				int predefined_note05 = predefined_note_spinner05.getSelectedItemPosition();
				// Update the notes table with the data
				if (predefined_note01 > 0) {
					cmd = String.format("insert into sheep_note_table (sheep_id, note_text, note_date, note_time, " +
						"id_predefinednotesid01) " +
						"values ( %s, '%s', '%s', '%s', %s )",
    					thissheep_id, note_text, TodayIs(), TimeIs(), predefined_note01);
	    			Log.i("update notes ", "before cmd " + cmd);
	    			dbh.exec( cmd );	
	    			Log.i("update notes ", "after cmd exec");
	    			Log.i("take note","first note written with predefined note");
			    }else{
			    	//	no predefined note so write one without it
			    	cmd = String.format("insert into sheep_note_table (sheep_id, note_text, note_date, note_time) " +
 							"values ( %s, '%s', '%s', '%s')",
 	    					thissheep_id, note_text, TodayIs(), TimeIs());
	    			Log.i("update notes ", "before cmd " + cmd);
	    			dbh.exec( cmd );	
	    			Log.i("update notes ", "after cmd exec");
	    			Log.i("take note","first note written ");
			    }
    			if (predefined_note02 > 0) {
    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
 							"id_predefinednotesid01) " +
 							"values ( %s, '%s', '%s', %s)",
 	    					thissheep_id, TodayIs(), TimeIs(), predefined_note02 );
 	    			Log.i("update notes ", "before cmd " + cmd);
 	    			dbh.exec( cmd );
 	    			Log.i("take note","second note written");
    	 		}
    			if (predefined_note03 > 0) {
    	 			Log.i("take note","third note written");
    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
 							"id_predefinednotesid01) " +
 							"values ( %s, '%s', '%s', %s)",
 	    					thissheep_id, TodayIs(), TimeIs(), predefined_note03 );
 	    			Log.i("update notes ", "before cmd " + cmd);
 	    			dbh.exec( cmd );	
    	 		}
    			if (predefined_note04 > 0) {
    	 			Log.i("take note","fourth note written");
    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
 							"id_predefinednotesid01) " +
 							"values ( %s, '%s', '%s', %s)",
 	    					thissheep_id, TodayIs(), TimeIs(), predefined_note04 );
 	    			Log.i("update notes ", "before cmd " + cmd);
 	    			dbh.exec( cmd );	
    	 		}
    			if (predefined_note05 > 0) {
    	 			Log.i("take note","fifth note written");
    	 			cmd = String.format("insert into sheep_note_table (sheep_id, note_date, note_time, " +
 							"id_predefinednotesid01) " +
 							"values ( %s, '%s', '%s', %s)",
 	    					thissheep_id, TodayIs(), TimeIs(), predefined_note05 );
 	    			Log.i("update notes ", "before cmd " + cmd);
 	    			dbh.exec( cmd );	
    	 		}
			    }
			  })
			.setNegativeButton("Cancel",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			    }
			  });
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
	} 
	return "got note";
}
public static String TimeIs() {
	Calendar calendar = Calendar.getInstance();
    //12 hour format
//	int hour = cal.get(Calendar.HOUR);
    //24 hour format
	int hourofday = calendar.get(Calendar.HOUR_OF_DAY);
	int minute = calendar.get(Calendar.MINUTE);
	int second = calendar.get(Calendar.SECOND);
	  
	return Make2Digits(hourofday) + ":" + Make2Digits(minute) + ":" + Make2Digits(second) ;
}
public static String TodayIs() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) ;
	}
 public static String Make2Digits(int i) {
		if (i < 10) {
			return "0" + i;
		} else {
			return Integer.toString(i);
		}
	}
 public static String YearIs() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		return Integer.toString(year) ;
	}
 
 public static void alertDialogShow(Context context, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder( context );
		builder.setMessage( message)
	           .setTitle( title );
		builder.setPositiveButton( R.string.ok, new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int idx) {
	               // User clicked OK button 
	               }
	       });		
		AlertDialog dialog = builder.create();
		dialog.show();		 
 }
}
 
 