package com.weyr_associates.lambtracker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.StringTokenizer;

import android.R.string;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class provides a set of methods to facilitate using the
 * SQLite database under Android. 
 * @author ww
 */
@SuppressLint("DefaultLocale")

public class DatabaseHandler extends SQLiteOpenHelper
    {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    
    // active table
    private String activeTable = null;
    
    // build table sql
    private String buildTable = null;
    
    // the CSV file to be used
    private String theCSVFile = null;
    
    // the context
    private Context context;
    
    // cursor associated with last exec() call
    private Cursor currentCursor;
    
    // the active database
    private SQLiteDatabase db = null;
  
//	private static String db_path = getString(R.string.database_path);

//    private static String db_name = getString(R.string.database_file);
    
    
    /**
     * Constructor for the DatabaseHandler
     * @param context the context of the caller (usually 'this')
     * @param dbName  the name of the database to open
     */
    public DatabaseHandler( Context context, String dbName )
        {
        super(context, dbName, null, DATABASE_VERSION);
        this.context = context;
        }
    
    /**
     * Required by Android, not used in this class
     * @param db a SQLite database object
     */
    public void onCreate( SQLiteDatabase db )
    	{
    	}
    
    /**
     * Create a table in the database from a CSV file. 
     * @param tableName the name of the table to create
     * @param createTableSQL the SQL required to actually *create* the table
     * @param csvFile the name of the file to be used as the CSV source (in 'assets/')
     * @return int (number of records in table)
     */
    public int createTable( String tableName, String createTableSQL, String csvFile )
        {
        String line;
        
        activeTable = tableName;
        buildTable  = createTableSQL;
        theCSVFile  = csvFile;
        
        if( db == null )
            db = this.getWritableDatabase();
        
        db.execSQL( "drop table if exists " + tableName );
        db.execSQL( createTableSQL );
        
        // load from .csv file in assets/
        if( csvFile != null )
            {
        	try {
	            InputStream       is     = context.getAssets().open( csvFile );
	            InputStreamReader isr    = new InputStreamReader( is );
	            BufferedReader    in     = new BufferedReader(isr);
	            String            header = in.readLine();
	            String            cmd    = String.format( "insert into %s(%s) ", tableName, header );
	            
	            try {
	                while(  (line = in.readLine()) != null )
	                    {
	                    if( line.length() == 0 )
	                        continue;
	                    
	                    StringBuilder sb = new StringBuilder();
	                    
	                    // get the data
	                    String[] values = line.split( "," );
	                    
	                    sb.append( "values(" );
	                    
	                    for( int i = 0; i < values.length; i++ )
	                        {
	                        sb.append( "'" );
	                        sb.append( values[i] );
	                        sb.append( "'," );
	                        }
	                    
	                    String vals = sb.toString();
	                    int lastIx  = vals.lastIndexOf( "," );
	                    vals        = vals.substring( 0, lastIx );
	                    String sql  = cmd + vals + ")";
	                    db.execSQL( sql );
	                    }
	                }
	            
	            finally
	                {
	                in.close();
	                }
        		}
        	
        	catch( Exception ex )
        		{
        		Log.e( "DatabaseHandler", ex.getMessage() );
        		return -1;
        		}
        	}
        
        String cmd = String.format( "select count(*) from %s", tableName );
        Cursor crsr = (Cursor)exec( cmd );
        crsr.moveToFirst();
        return crsr.getInt( 0 );
        }

    /**
     * Execute an SQL command. There are two cases:
     *     1) a 'select' (which should create a result set of data)
     *     2) all other SQL commands (e.g. delete, update, drop, alter, ...)
     * @param sqlStmt the SQL statement to be executed
     * @return a Cursor object in the case of 'select'; an Integer otherwise
     */
    @SuppressLint("UseValueOf")
	public Object exec( String sqlStmt )
        {
    	StringTokenizer tok = new StringTokenizer(sqlStmt, " " );
    	String          cmd = tok.nextToken();
        
        if( db == null )
            db = this.getWritableDatabase();
        
        if( cmd.toLowerCase().equals("select") )
            {
            currentCursor = db.rawQuery( sqlStmt, null );
            return currentCursor;
            }
        
        else    // non-select commands (do not return any 'data' but a count)
            {
            db.execSQL( sqlStmt );
            Cursor crsr = db.rawQuery( "select changes()", null );
            crsr.moveToFirst();
            int nr        = crsr.getInt( 0 );
            currentCursor = null;
            return new Integer( nr );
            }
        }
    
    /**
     * Gets the names of the columns, in the same order that they were defined
     * for the table.
     * @param tableName the name of the table
     * @return array of table column names
     */
    public String[] getColumnNames( String tableName )
    	{
        if( db == null )
            db = this.getWritableDatabase();
        
    	String cmd  = String.format( "select * from %s limit 1", tableName );
        Cursor crsr = db.rawQuery( cmd, null );
        return crsr.getColumnNames();
        }
    
    /**
     *
     * @return
     */
    public String[] getColumnNames()
    {
    	if( currentCursor != null )
    		return currentCursor.getColumnNames();
    	
    	throw new NullPointerException( "getColumnNames: No cursor from last exec()" );

    }
    /**
     *
     * @param colIndex
     * @return
     */
    public int getInt( int colIndex )
    	{
    	if( currentCursor != null )
    		return currentCursor.getInt(colIndex);
    	
    	throw new NullPointerException( "getInt: No cursor from last exec()" );
    	}
    
    /**
     *
     * @param colName
     * @return
     */
    public int getInt( String colName )
    	{
    	if( currentCursor != null )
    		return currentCursor.getInt( colIndexFromName(colName) );
    	
    	throw new NullPointerException( "getInt: No cursor from last exec()" );
    	}
    
    /**
     *
     * @param colIndex
     * @return
     */
    public String getStr( int colIndex )
    	{
    	if( currentCursor != null )
    		return currentCursor.getString(colIndex);
    	
    	throw new NullPointerException( "getStr: No cursor from last exec()" );
    	}
    
    /**
     *
     * @param colName
     * @return
     */
    public String getStr( String colName  )
    	{
    	if( currentCursor != null )
    		return currentCursor.getString( colIndexFromName(colName) );
    	
    	throw new NullPointerException( "getStr: No cursor from last exec()" );
    	}
    
    /**
     *
     * @param colIndex
     * @return
     */
    public float getReal( int colIndex )
    	{
    	if( currentCursor != null )
    		return currentCursor.getFloat(colIndex);
    	
    	throw new NullPointerException( "getReal: No cursor from last exec()" );
    	}
    
    /**
     *
     * @param colName
     * @return
     */
    public float getReal( String colName )
    	{
    	if( currentCursor != null )
    		return currentCursor.getFloat( colIndexFromName(colName) );
    	
    	throw new NullPointerException( "getReal: No cursor from last exec()" );
    	}
    
    /**
     *
     * @return
     */
    public int getSize()
    	{
    	if( currentCursor != null )
    		return currentCursor.getCount();
    	
    	throw new NullPointerException( "getSize: No cursor from last exec()" );
    	}
    
    /**
     *
     * @return
     */
    public int getNrcols()
    	{
    	if( currentCursor != null )
    		return currentCursor.getColumnCount();
    	
    	throw new NullPointerException( "getNrCols: No cursor from last exec()" );

    	}
    
    /**
     *
     * @param name
     * @return
     */
    public int colIndexFromName( String name )
    {
    	if( currentCursor != null )
    		return currentCursor.getColumnIndex( name );
    	
    	throw new NullPointerException( "colIndexFromName: No cursor from last exec()" );
    }
    
    /**
     *
     * @return
     */
    public Cursor getCursor()
    	{
    	return currentCursor;
    	}
    
    /**
     *
     * @return
     */
    public boolean advanceCursor()
    	{
    	if( currentCursor != null )
    		return currentCursor.moveToNext();
    	
    	throw new NullPointerException( "advanceCursor: No cursor from last exec()" );
    	}
    
    /**
     *
     * @return
     */
    public boolean moveToFirstRecord()
    	{
    	if( currentCursor != null )
    		return currentCursor.moveToFirst();
    	
    	throw new NullPointerException( "moveToFirstRecord: No cursor from last exec()" );
    	}
    
    /**
     *
     * @param fileName
     * @return
     */
    public boolean backup(String fileName )
    	{
    	int            length;
    	ContextWrapper ctxWrapper = new ContextWrapper( context );
    	
        // Local database
        if( db == null )
            db = this.getWritableDatabase();
        
    	String  from  = db.getPath();
        String  dPath = (ctxWrapper.getDir( "data", 'w' )).getPath();	// has terminating '/' ?
        Log.d( "DatabaseHandler", "dPath=" + dPath );
        
        try {
            InputStream input = new FileInputStream( from );
            
            // Path to the external backup
            OutputStream output = new FileOutputStream( dPath + "/" + fileName );	// guessing no terminating '/'

            // transfer bytes from the Input File to the Output File
            byte[] buffer = new byte[1024];

            while( (length = input.read(buffer)) > 0 )
            	{
                output.write(buffer, 0, length);
            	}

            output.flush();
            output.close();
            input.close();        	
        	}
        
        catch( FileNotFoundException fnf )
        	{
        	Log.e("DatabaseHandler", "File not found: " + fileName );
        	Log.e("DatabaseHandler", "  " + fnf.getLocalizedMessage() );
        	return false;
        	}
        
        catch( IOException ioe )
        	{
        	Log.e("DatabaseHandler", "I/O error: " + ioe.getLocalizedMessage() );
        	return false;
        	}
        
        return true;
    	}
    
    /**
     *
     * @return
     */
    public boolean restore()
    	{
    	// bring up file selection dialog
    	// get path to selected file
    	// get path to database location
    	// close the active db if necessary
    	// rename the target db file to something (e.g. currendb.ren)
    	// copy backup to database location
    	// rename backup file to name of original db file
    	// if copy succeeded delete the renamed database file, reopen a connection, return true
    	// if copy failed restore renamed db file to original name and return false
    	return false;
    	}
    
    /**
     *
     * @param tableName
     * @return
     */
    public String dumpTable( String tableName )
    	{
    	String[] cols = getColumnNames( tableName );
    	
    	if( cols == null || cols.length == 0 )
    		{
    		return "No column names for '" + tableName + "'";
    		}
    	
    	StringBuilder sb   = new StringBuilder();
    	String        cmd  = String.format( "select * from %s", tableName );
    	Cursor        crsr = (Cursor) exec( cmd );
    	int           n    = cols.length;
    	int           nRec = 0;
    	
    	crsr.moveToFirst();
    	
    	do  {
    		nRec += 1;
    		String line = String.format( "Record %d:\n", nRec );
			sb.append( line );
			Log.d( "DatabaseHandler", line );
    		
    		for( int i = 0; i < n; i++ )
    			{
    			line = String.format( "  %s: %s\n", cols[i], crsr.getString(i) );
    			sb.append( line );
    			Log.d( "DatabaseHandler", line );
    			}
    		} while( crsr.moveToNext() );
    	
    	return sb.toString();
    	}
    
    /**
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
    	{
        Log.w( "DatabaseHandler", "Upgrading database from version " + oldVersion + " to " + 
        		newVersion + " (destroys all current data)" );

        // rebuild the table
        if( activeTable != null &&
        	buildTable  != null &&
        	theCSVFile  != null )
        	createTable( activeTable, buildTable, theCSVFile );
    	}
    
    /**
     *
     * @param tblName
     * @param tblSQL
     * @param tblCSV
     */
    public void setTableInfo( String tblName, String tblSQL, String tblCSV )
    	{
    	// use this before doing an 'onUpgrade'
    	activeTable = tblName;
    	buildTable  = tblSQL;
    	theCSVFile  = tblCSV;
    	}
    	
    /**
     *
     */
    public void closeDB()
        {
        if( db == null )
            return;
        
        db.close();
        db = null;
        }
    
    /**
     *
     * @param src
     * @return
     */
    public String fixApostrophes( String src )
    	{
    	return src.replaceAll( "'", "''" );
    	}
    
    public void copyRealDataBase() throws IOException
    {
        //Open your local db as the input stream
        InputStream myInput = context.getAssets().open("lambtracker_db.sqlite");
 
        // Path to the just created empty db
        String outFileName = "/data/data/com.weyr_associates.lambtracker/databases/" + "lambtracker_db.sqlite";
 
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
 
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0)
        {
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
    }
