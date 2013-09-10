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
import java.util.Calendar;

import android.util.Log;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

public class LoadSheepList extends Activity {
     
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
    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
//    	Resources res = getResources();
    	String 	dbfile = getString(R.string.real_database_file) ;
    	DatabaseHandler dbh     = new DatabaseHandler( this, dbfile );
    	try {
    		dbh.copyRealDataBase();
    	}
    	catch (IOException e) {
    	} 	
    	txtView.setText("Created Real Database from Copy in Assets.");
    	dbh.close();
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
	dbh.close();
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
        String outFileName = Environment.getExternalStorageDirectory()+"/lambtracker_copy.db";
        Log.i("backup", outFileName);
        // Open the empty db as the output stream
        OutputStream output;
		try {
			output = new FileOutputStream(outFileName);
		} catch (FileNotFoundException e) {
			Log.i("backup", "output database file not found");
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
        }
    }
