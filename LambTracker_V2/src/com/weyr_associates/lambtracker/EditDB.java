package com.weyr_associates.lambtracker;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EditDB extends Activity
	{
	private DatabaseHandler dbh;
	private Cursor          cursor;
	private int             nRecs;
	private int			    recNo;
	private String[]        colNames;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
		{
	    super.onCreate(savedInstanceState);
	    setContentView( R.layout.edit_db );
	    dbh = new DatabaseHandler( this, "lambtracker_db.sqlite" );
		}
	
	public void execSQL( View v )
		{
		String results;
		
		// get the SQL text
		TextView sqlTV = (TextView) findViewById( R.id.sqlInput );
		String   sql   = sqlTV.getText().toString();
		
		// execute the command
		Object crsr =  dbh.exec( sql );
		
		// report the results
		if( crsr instanceof Integer  )
			{
			int n   = ((Integer) crsr).intValue();
			results = String.format( "Records affected: %d", n );
			nRecs   = 0;
			cursor  = null;
			}
		
		else	// format the first record (subsequent records: use moveToNextRecord( null ) )
			{
			recNo    = 1;
			cursor   = (Cursor) crsr;
			nRecs    = cursor.getCount();
			colNames = cursor.getColumnNames();
			cursor.moveToFirst();
			results = formatRecord( cursor );
			}
		
		// display the results of the SQL execution
		sqlTV = (TextView) findViewById( R.id.sqlOutput );
		sqlTV.setText( results );
		}

	public void moveToNextRecord( View v )
		{
		if( cursor.moveToNext() )
			{
			recNo         += 1;
			TextView sqlTV = (TextView) findViewById( R.id.sqlOutput );
			sqlTV.setText( formatRecord(cursor) );
			}
		}
	public void moveToPrevRecord( View v )
	{
	if( cursor.moveToPrevious() )
		{
		recNo         -= 1;
		TextView sqlTV = (TextView) findViewById( R.id.sqlOutput );
		sqlTV.setText( formatRecord(cursor) );
		}
	}

	public void clearBuffers( View v )
		{
		// clear the text buffers
		((TextView) findViewById( R.id.sqlInput )).setText( "" );
		((TextView) findViewById( R.id.sqlOutput )).setText( "" );
		}
	
	public void goBack( View v )
		{
		// return to main
		finish();
		}
	
	private String formatRecord( Cursor crsr )
		{
		StringBuilder sb       = new StringBuilder();
		int           nrCols   = colNames.length;
		String        line     = String.format( "Record %d of %d:\n", recNo, nRecs );
		sb.append( line );
		
		for( int i = 0; i < nrCols; i++ )
			{
			switch( cursor.getType(i) )
				{
				case Cursor.FIELD_TYPE_FLOAT:
					line = String.format( "  %s: %f\n", colNames[i], cursor.getFloat(i) );
					break;
				
				case Cursor.FIELD_TYPE_INTEGER:
					line = String.format( "  %s: %d\n", colNames[i], cursor.getInt(i) );
					break;
				
				case Cursor.FIELD_TYPE_NULL:
					line = String.format( "  %s: null\n", colNames[i] );
					break;
				
				case Cursor.FIELD_TYPE_STRING:
					line = String.format( "  %s: %s\n", colNames[i], cursor.getString(i) );
					break;
					
				default:
					line = String.format( "  %s: ?? %s ??", colNames[i], cursor.getString(i) );
					break;
				}
			
			sb.append( line );
			}
		
		return sb.toString();
		}
	}
