package com.weyr_associates.lambtracker;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
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

    public void createDB( View v )
		{
    	DatabaseHandler dbh     = new DatabaseHandler( this, "weyr_associates" );
    	String          cmd     = getString( R.string.build_table );
    	String          csv     = getString( R.string.csv_file );
    	int             n       = dbh.createTable( "sheep_table", cmd, csv );
    	String          howMany = String.format( "records written to 'sheep-table': %d", n );
    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
    	txtView.setText( howMany );
    	dbh.close();
		}

    public void dumpDB( View v )
    	{
    	DatabaseHandler dbh     = new DatabaseHandler( this, "weyr_associates" );
    	TextView        txtView = (TextView) findViewById( R.id.editText1 );
    	String          theDump = dbh.dumpTable( "sheep_table" );
    	txtView.setText( theDump );
    	dbh.close();
    	}
   
    public void copyRealDB( View v )
	{
	DatabaseHandler dbh     = new DatabaseHandler( this, "lambtracker_db.sqlite" );
	try {
		dbh.copyRealDataBase();
	}
	catch (IOException e) {
		
	}
	
	dbh.close();
	}

    
    
    }
