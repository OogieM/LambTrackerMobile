package com.weyr_associates.lambtracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;


public class LoadSheepList extends Activity {
	
	 int FILE_LIST = 1;
     
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_sheeplist);
        }

    public void allDone( View v )
		{
    	finish();
		}

     public void copyRealDB( View v )
		{
    	String temp;
    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
//    	Resources res = getResources();
    	String 	dbfile = getString(R.string.real_database_file) ;
    	Log.i("LoadSheepList ", " got this as a file " + dbfile);
    	// Moved this below the copy per note from Eric Coker on DXR
    	DatabaseHandler dbh     = new DatabaseHandler( this, dbfile );
    	try {
    		Log.i("in try ", " before going to DBH");
    		dbh.copyRealDataBase(dbfile);
    	}
    	catch (IOException e) {
    		Log.i("in try ", " got an exception ");
    	} 	
    	temp = "Created Database from Copy in Assets.";
    	
    	String cmd = String.format( "select count(*) from %s", "sheep_table" );
        Cursor crsr = ((Cursor) dbh.exec( cmd ));
        crsr.moveToFirst();
        temp = String.format(temp + "\n" + "Records created in sheep_table = " + String.valueOf(crsr.getInt( 0 )));
        txtView.setText(temp);
        dbh.closeDB();
	}

  
     public void selectFileDB( View v )
		{

    	 Log.i("in try ", " File DB");
    	 
    			Intent fileList = new Intent(this, FileList.class);

    			CharSequence fileList_label = "";
    			String path = fileList_label.toString();

    			fileList.putExtra("fileName", path);
    			startActivityForResult(fileList, FILE_LIST);	   	    	     	 
	}
  
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		super.onActivityResult(requestCode, resultCode, data);
 		if(requestCode == FILE_LIST){
 			if(resultCode == RESULT_OK){
 		    	String temp;
 		    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
//// 		    	Resources res = getResources();
 		    	String 	dbfile = data.getStringExtra ("fileName") ;
 		    			
 		    	Log.i("LoadSheepList ", " got this as a file " + dbfile);
		    	
 		        String outFileName = "/data/data/com.weyr_associates.lambtracker/databases/" + "lambtracker_db.sqlite";

 		        File dbInFile = new File(dbfile);
   		        File dbOutFile = new File(outFileName);
 		      		        
		    	DatabaseHandler dbh = new DatabaseHandler( this, outFileName ); 		    	
 		    	try {
 		    		Log.i("in try ", " before going to DBH");
// 		    		dbh.copyRealDataBase(dbfile);
 	 		        dbh.copy(dbInFile,dbOutFile);
 		    	}
 		    	catch (IOException e) {
 		    		Log.i("in try ", " got an exception ");
 		    	}		    			
 		    			
 		    	temp = "Created Database from file in " + dbfile;
 		    	
 		    	String cmd = String.format( "select count(*) from %s", "sheep_table" );
 		        Cursor crsr = ((Cursor) dbh.exec( cmd ));
 		        crsr.moveToFirst();
 		        temp = String.format(temp + "\n" + "Records created in sheep_table = " + String.valueOf(crsr.getInt( 0 )));
// 		        txtView.setText("Records created in sheep_table = " + String.valueOf(crsr.getInt( 0 )));
 		        txtView.setText(temp);
 		        dbh.closeDB();
 			}
 		}
 		else /*if(requestCode == TEMPLATE_EVENT){*/
 			if(resultCode == RESULT_OK){

 			}
 		}    
    
    public void showRealDB( View v )
	{
    	String 	dbfile = getString(R.string.real_database_file) ;
    	DatabaseHandler dbh     = new DatabaseHandler( this, dbfile );
	TextView        txtView = (TextView) findViewById( R.id.editText1 );
//	Figure out how to show the entire real LambTracker database by selecting a table to show 
//	and then displaying it
// 	Decided to just dump the sheep table as a first cut	
	String          theDump = dbh.dumpTable( "sheep_table" );
	txtView.setText( theDump );
	dbh.closeDB();
	}
    
    public void backupRealDB( View v ) throws IOException{
    	String 	dbfile = getString(R.string.real_database_file) ;
    	String 	dbpath = getString(R.string.database_path) ;
    
    	String	fullPath = dbpath + dbfile;
        File dbInFile = new File(fullPath);
        FileInputStream fis;
		try {
			fis = new FileInputStream(dbInFile);
		} catch (FileNotFoundException e) {
			Log.i("backup", "input database file not found" + fullPath);
			return;
		}
		String today = TodayIs();
		String currentTimeString = new SimpleDateFormat("HH-mm-ss").format(new Date());
        String outFileName = Environment.getExternalStorageDirectory()+"/lambtracker_db_"+today+"_"+currentTimeString+ ".sqlite";
        Log.i("backup", outFileName);
        // Open the empty db as the output stream
        OutputStream output;
		try {
			output = new FileOutputStream(outFileName);
		} catch (FileNotFoundException e) {
			Log.i("backup", "output database file not found");
			fis.close();
			return;
		}

        // Transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
			while ((length = fis.read(buffer))>0){
			    output.write(buffer, 0, length);
			}

        // Close the streams
        output.flush();
        output.close();
        fis.close();
    
    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
    	txtView.setText("Real Data backed up to External Storage as file "+ outFileName );
        }
    private String TodayIs() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		return year + "-" + Make2Digits(month + 1) + "-" +  Make2Digits(day) ;
	}
    private String Make2Digits(int i) {
		if (i < 10) {
			return "0" + i;
		} else {
			return Integer.toString(i);
		}
	}
    }
