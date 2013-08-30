package com.weyr_associates.lambtracker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.util.Log;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
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

    public void createDemoDB( View v )
		{
    	String 	dbfile = getString(R.string.demo_database_file) ;
    	Log.i ("createdb", dbfile);
    	DatabaseHandler dbh     = new DatabaseHandler( this, dbfile );
    	String          cmd     = getString( R.string.build_table );
    	String          csv     = getString( R.string.csv_file );
    	int             n       = dbh.createTable( "sheep_table", cmd, csv );
    	String          howMany = String.format( "records written to 'sheep_table': %d", n );
    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
    	txtView.setText( howMany );
    	dbh.close();
		}

    public void showDemoDB( View v )
    	{
    	String 	dbfile = getString(R.string.demo_database_file) ;
    	DatabaseHandler dbh     = new DatabaseHandler( this, dbfile );
    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
    	String          theDump = dbh.dumpTable( "sheep_table" );
    	txtView.setText( theDump );
    	dbh.close();
    	}
   
    public void copyRealDB( View v )
		{
//    	Resources res = getResources();
    	String 	dbfile = getString(R.string.real_database_file) ;
    	DatabaseHandler dbh     = new DatabaseHandler( this, dbfile );
    	try {
    		dbh.copyRealDataBase();
    	}
    	catch (IOException e) {
	}
	
	dbh.close();
	}

    public void showRealDB( View v )
	{
    	String 	dbfile = getString(R.string.real_database_file) ;
    	DatabaseHandler dbh     = new DatabaseHandler( this, dbfile );
	TextView        txtView = (TextView) findViewById( R.id.editText1 );
//	Figure out how to show the entire real LambTracker database by selecting a table to show 
//	and then displaying it
//	get a table to display
	
//	String          theDump = dbh.dumpTable( "sheep_table" );
//	txtView.setText( theDump );
	dbh.close();
	}
    
    }
