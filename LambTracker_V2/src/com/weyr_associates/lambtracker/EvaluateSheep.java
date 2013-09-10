package com.weyr_associates.lambtracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.database.Cursor;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class EvaluateSheep extends Activity {
	
	List<Float> rating_scores;
	private RatingBar trait01_ratingbar ;
	private RatingBar trait02_ratingbar ;
	private RatingBar trait03_ratingbar ;
	private RatingBar trait04_ratingbar ;
	private RatingBar trait05_ratingbar ;
//	private RatingBar trait06_ratingbar ;
//	private RatingBar trait07_ratingbar ;
//	private RatingBar trait08_ratingbar ;
//	private RatingBar trait09_ratingbar ;
//	private RatingBar trait10_ratingbar ;
//	private RatingBar trait11_ratingbar ;
//	private RatingBar trait12_ratingbar ;
	
	private DatabaseHandler dbh;
	private Cursor 	cursor;
	private ArrayList<String> results = new ArrayList<String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState)	
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evaluate_sheep);
        String 			dbname = getString(R.string.real_database_file); 
        String          cmd;
        
        TextView TV;
        
        Object 			crsr;
        dbh = new DatabaseHandler( this, dbname );

        TV = (TextView) findViewById(R.id.trait01_lbl);
        TV.setText( "Missing Teeth" );
        
        
       	}
    public void saveScores( View v )
    {
    		rating_scores = new ArrayList<Float>();
    		
    		trait01_ratingbar = (RatingBar) findViewById(R.id.trait01_ratingbar);
    		rating_scores.add(trait01_ratingbar.getRating());
    		Log.i("trait01_ratingbar ", String.valueOf(trait01_ratingbar.getRating()));
    		
    		trait02_ratingbar = (RatingBar) findViewById(R.id.trait02_ratingbar);
    		rating_scores.add(trait02_ratingbar.getRating());	
    		Log.i("trait02_ratingbar ", String.valueOf(trait02_ratingbar.getRating()));
    		
    		trait03_ratingbar = (RatingBar) findViewById(R.id.trait03_ratingbar);
    		rating_scores.add(trait03_ratingbar.getRating());	
    		Log.i("trait03_ratingbar ", String.valueOf(trait03_ratingbar.getRating()));
    		
    		trait04_ratingbar = (RatingBar) findViewById(R.id.trait04_ratingbar);
    		rating_scores.add(trait04_ratingbar.getRating());	
    		Log.i("trait04_ratingbar ", String.valueOf(trait04_ratingbar.getRating()));
    		
    		trait05_ratingbar = (RatingBar) findViewById(R.id.trait05_ratingbar);
    		rating_scores.add(trait05_ratingbar.getRating());	
    		Log.i("trait05_ratingbar ", String.valueOf(trait05_ratingbar.getRating()));
//    		
//    		trait06_ratingbar = (RatingBar) findViewById(R.id.trait06_ratingbar);
//    		rating_scores.add(trait06_ratingbar.getRating());	
//    		Log.i("trait06_ratingbar ", String.valueOf(trait06_ratingbar.getRating()));
//    		
//    		trait07_ratingbar = (RatingBar) findViewById(R.id.trait07_ratingbar);
//    		rating_scores.add(trait07_ratingbar.getRating());	
//    		Log.i("trait07_ratingbar ", String.valueOf(trait07_ratingbar.getRating()));
//    		
//    		trait08_ratingbar = (RatingBar) findViewById(R.id.trait08_ratingbar);
//    		rating_scores.add(trait08_ratingbar.getRating());	
//    		Log.i("trait08_ratingbar ", String.valueOf(trait08_ratingbar.getRating()));
//    		
//    		trait09_ratingbar = (RatingBar) findViewById(R.id.trait09_ratingbar);
//    		rating_scores.add(trait09_ratingbar.getRating());	
//    		Log.i("trait09_ratingbar ", String.valueOf(trait09_ratingbar.getRating()));
//    		
//    		trait10_ratingbar = (RatingBar) findViewById(R.id.trait10_ratingbar);
//    		rating_scores.add(trait10_ratingbar.getRating());	
//    		Log.i("trait10_ratingbar ", String.valueOf(trait10_ratingbar.getRating()));
//    		
//    		trait11_ratingbar = (RatingBar) findViewById(R.id.trait11_ratingbar);
//    		rating_scores.add(trait11_ratingbar.getRating());	
//    		Log.i("trait11_ratingbar ", String.valueOf(trait11_ratingbar.getRating()));
//    		
//    		trait12_ratingbar = (RatingBar) findViewById(R.id.trait12_ratingbar);
//    		rating_scores.add(trait12_ratingbar.getRating());	
//    		Log.i("trait12_ratingbar ", String.valueOf(trait12_ratingbar.getRating()));
//    		
    }
	
	   public void backBtn( View v )
	    {
      	dbh.closeDB();
      	clearBtn( null );   	
   		finish();
	    }
	   
	public void helpBtn( View v )
    {
   	// Display help here   	
		AlertDialog.Builder builder = new AlertDialog.Builder( this );
		builder.setMessage( R.string.help_evaluate )
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
	public void clearBtn( View v )
    {
		// clear out the display of everything
		TextView TV = (TextView) findViewById( R.id.inputText );
		TV.setText( "" );		
		TV = (TextView) findViewById( R.id.sheepnameText );
		TV.setText( "" );
    
    }
}
